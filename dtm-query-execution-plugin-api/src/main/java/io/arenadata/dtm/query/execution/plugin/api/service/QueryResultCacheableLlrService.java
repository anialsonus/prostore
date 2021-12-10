/*
 * Copyright © 2021 ProStore
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.arenadata.dtm.query.execution.plugin.api.service;

import io.arenadata.dtm.async.AsyncUtils;
import io.arenadata.dtm.cache.service.CacheService;
import io.arenadata.dtm.common.cache.QueryTemplateKey;
import io.arenadata.dtm.common.cache.QueryTemplateValue;
import io.arenadata.dtm.common.dto.QueryParserRequest;
import io.arenadata.dtm.common.dto.QueryParserResponse;
import io.arenadata.dtm.common.model.ddl.ColumnType;
import io.arenadata.dtm.common.reader.QueryParameters;
import io.arenadata.dtm.common.reader.QueryResult;
import io.arenadata.dtm.query.calcite.core.service.QueryParserService;
import io.arenadata.dtm.query.calcite.core.service.QueryTemplateExtractor;
import io.arenadata.dtm.query.execution.model.metadata.ColumnMetadata;
import io.arenadata.dtm.query.execution.plugin.api.dml.LlrEstimateUtils;
import io.arenadata.dtm.query.execution.plugin.api.dml.LlrPlanResult;
import io.arenadata.dtm.query.execution.plugin.api.request.LlrRequest;
import io.vertx.core.Future;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.calcite.sql.SqlDialect;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.type.SqlTypeName;

import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;

@Slf4j
public abstract class QueryResultCacheableLlrService implements LlrService<QueryResult> {
    private static final String ESTIMATE_COLUMN_NAME = "estimate";
    protected final CacheService<QueryTemplateKey, QueryTemplateValue> queryCacheService;
    protected final QueryTemplateExtractor templateExtractor;
    protected final SqlDialect sqlDialect;
    private final QueryParserService queryParserService;

    protected QueryResultCacheableLlrService(CacheService<QueryTemplateKey, QueryTemplateValue> queryCacheService,
                                             QueryTemplateExtractor templateExtractor,
                                             SqlDialect sqlDialect,
                                             QueryParserService queryParserService) {
        this.queryCacheService = queryCacheService;
        this.templateExtractor = templateExtractor;
        this.sqlDialect = sqlDialect;
        this.queryParserService = queryParserService;
    }

    @Override
    public Future<QueryResult> execute(LlrRequest request) {
        return AsyncUtils.measureMs(getQueryFromCacheOrInit(request),
                duration -> log.debug("Got query from cache and enriched template for query [{}] in [{}]ms",
                        request.getRequestId(), duration))
                .compose(enrichedQuery -> executeRealOrEstimate(enrichedQuery, request));
    }

    private Future<QueryResult> executeRealOrEstimate(String enrichedQuery, LlrRequest request) {
        if (request.isEstimate()) {
            return estimateQueryExecute(enrichedQuery, getExtendedQueryParameters(request))
                    .map(planResult -> QueryResult.builder()
                            .requestId(request.getRequestId())
                            .metadata(singletonList(new ColumnMetadata(ESTIMATE_COLUMN_NAME, ColumnType.VARCHAR)))
                            .result(singletonList(singletonMap(ESTIMATE_COLUMN_NAME, LlrEstimateUtils.prepareResultJson(
                                    planResult.getSourceType(), enrichedQuery, planResult.getPlan()))))
                            .build());
        }

        return queryExecute(enrichedQuery, getExtendedQueryParameters(request), request.getMetadata())
                .map(result -> QueryResult.builder()
                        .requestId(request.getRequestId())
                        .metadata(request.getMetadata())
                        .result(result)
                        .build());
    }

    protected abstract Future<List<Map<String, Object>>> queryExecute(String enrichedQuery,
                                                                      QueryParameters queryParameters,
                                                                      List<ColumnMetadata> metadata);

    protected abstract Future<LlrPlanResult> estimateQueryExecute(String enrichedQuery,
                                                                  QueryParameters queryParameters);

    protected QueryParameters getExtendedQueryParameters(LlrRequest request) {
        return request.getParameters();
    }

    private Future<String> getQueryFromCacheOrInit(LlrRequest llrRq) {
        return Future.future(promise -> {
            val queryTemplateValue = getQueryTemplateValueFromCache(llrRq);
            if (llrRq.isCachable() && queryTemplateValue != null) {
                promise.complete(getEnrichedSqlFromTemplate(llrRq, queryTemplateValue));
            } else {
                queryParserService.parse(new QueryParserRequest(llrRq.getSourceQueryTemplateResult().getTemplateNode(), llrRq.getSchema()))
                        .map(parserResponse -> {
                            validateQuery(parserResponse);
                            return parserResponse;
                        })
                        .compose(parserResponse -> enrichQuery(llrRq, parserResponse))
                        .map(QueryTemplateValue::new)
                        .compose(templateValue -> queryCacheService.put(getQueryTemplateKey(llrRq), templateValue)
                                .map(r -> getEnrichedSqlFromTemplate(llrRq, templateValue)))
                        .onComplete(promise);
            }
        });
    }

    protected abstract Future<SqlNode> enrichQuery(LlrRequest llrRequest, QueryParserResponse parserResponse);

    protected void validateQuery(QueryParserResponse parserResponse) {
    }

    private QueryTemplateValue getQueryTemplateValueFromCache(LlrRequest llrRq) {
        return queryCacheService.get(getQueryTemplateKey(llrRq));
    }

    private QueryTemplateKey getQueryTemplateKey(LlrRequest llrRq) {
        val template = templateExtractor.extract(llrRq.getOriginalQuery()).getTemplate();
        return QueryTemplateKey.builder()
                .sourceQueryTemplate(template)
                .logicalSchema(llrRq.getSchema())
                .build();
    }

    private String getEnrichedSqlFromTemplate(LlrRequest llrRq, QueryTemplateValue queryTemplateValue) {
        val params = convertParams(llrRq.getSourceQueryTemplateResult().getParams(), llrRq.getParameterTypes());
        val enrichedTemplateNode = templateExtractor.enrichTemplate(queryTemplateValue.getEnrichQueryTemplateNode(), params);
        return enrichedTemplateNode.toSqlString(sqlDialect).getSql();
    }

    protected List<SqlNode> convertParams(List<SqlNode> params, List<SqlTypeName> parameterTypes) {
        return params;
    }
}
