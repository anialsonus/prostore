/*
 * Copyright © 2020 ProStore
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
package io.arenadata.dtm.query.calcite.core.service.impl;

import io.arenadata.dtm.common.delta.DeltaInformation;
import io.arenadata.dtm.common.delta.DeltaType;
import io.arenadata.dtm.common.delta.SelectOnInterval;
import io.arenadata.dtm.common.exception.DeltaRangeInvalidException;
import io.arenadata.dtm.common.reader.InformationSchemaView;
import io.arenadata.dtm.common.reader.QueryRequest;
import io.arenadata.dtm.common.service.DeltaService;
import io.arenadata.dtm.query.calcite.core.service.DefinitionService;
import io.arenadata.dtm.query.calcite.core.service.DeltaInformationExtractor;
import io.arenadata.dtm.query.calcite.core.service.DeltaQueryPreprocessor;
import io.arenadata.dtm.query.calcite.core.util.CalciteUtil;
import io.vertx.core.*;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.calcite.sql.SqlNode;
import org.apache.commons.lang3.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class DeltaQueryPreprocessorImpl implements DeltaQueryPreprocessor {
    private final DeltaInformationExtractor deltaInformationExtractor;
    private final DefinitionService<SqlNode> definitionService;
    private final DeltaService deltaService;

    public DeltaQueryPreprocessorImpl(DefinitionService<SqlNode> definitionService,
                                      DeltaService deltaService,
                                      DeltaInformationExtractor deltaInformationExtractor) {
        this.definitionService = definitionService;
        this.deltaService = deltaService;
        this.deltaInformationExtractor = deltaInformationExtractor;
    }

    @Override
    public Future<QueryRequest> process(QueryRequest request) {
        return Future.future(handler -> {
            try {
                if (request == null || StringUtils.isEmpty(request.getSql())) {
                    log.error("Unspecified request {}", request);
                    handler.fail(String.format("Undefined request%s", request));
                } else {
                    val sqlNode = definitionService.processingQuery(request.getSql());
                    val deltaInfoRes = deltaInformationExtractor.extract(sqlNode);
                    calculateDeltaValues(deltaInfoRes.getDeltaInformations(), ar -> {
                        if (ar.succeeded()) {
                            try {
                                QueryRequest copyRequest = request.copy();
                                copyRequest.setDeltaInformations(deltaInfoRes.getDeltaInformations());
                                copyRequest.setSql(deltaInfoRes.getSqlWithoutSnapshots());
                                copyRequest.setDeltaInformations(ar.result());
                                handler.complete(copyRequest);
                            } catch (Exception e) {
                                log.error("Request parsing error", e);
                                handler.fail(e);
                            }
                        } else {
                            handler.fail(ar.cause());
                        }
                    });
                }
            } catch (Exception e) {
                log.error("Request parsing error", e);
                handler.fail(e);
            }
        });
    }

    private void calculateDeltaValues(List<DeltaInformation> deltas, Handler<AsyncResult<List<DeltaInformation>>> handler) {
        final Set<String> errors = new HashSet<>();
        CompositeFuture.join(deltas.stream()
                .map(deltaInformation -> getCalculateDeltaInfoFuture(errors, deltaInformation))
                .collect(Collectors.toList()))
                .onSuccess(deltaResult -> handler.handle(Future.succeededFuture(deltaResult.list())))
                .onFailure(error -> {
                    errors.add(error.getMessage());
                    handler.handle(Future.failedFuture(createDeltaRangeInvalidException(errors)));
                });
    }

    private Future<DeltaInformation> getCalculateDeltaInfoFuture(Set<String> errors, DeltaInformation deltaInformation) {
        return Future.future((Promise<DeltaInformation> deltaInfoPromise) -> {
            if (InformationSchemaView.SCHEMA_NAME.equalsIgnoreCase(deltaInformation.getSchemaName())) {
                deltaInfoPromise.complete(deltaInformation);
            } else if (deltaInformation.isLatestUncommittedDelta()) {
                deltaService.getCnToDeltaHot(deltaInformation.getSchemaName())
                        .onSuccess(deltaCnTo -> {
                            deltaInformation.setSelectOnNum(deltaCnTo);
                            deltaInfoPromise.complete(deltaInformation);
                        })
                        .onFailure(deltaInfoPromise::fail);
            } else {
                if (DeltaType.FINISHED_IN.equals(deltaInformation.getType()) || DeltaType.STARTED_IN.equals(deltaInformation.getType())) {
                    calculateSelectOnInterval(deltaInformation, ar -> {
                        if (ar.succeeded()) {
                            deltaInformation.setSelectOnInterval(ar.result());
                            deltaInfoPromise.complete(deltaInformation);
                        } else {
                            errors.add(ar.cause().getMessage());
                            deltaInfoPromise.fail(ar.cause());
                        }
                    });
                } else {
                    calculateSelectOnNum(deltaInformation, ar -> {
                        if (ar.succeeded()) {
                            deltaInformation.setSelectOnNum(ar.result());
                            deltaInfoPromise.complete(deltaInformation);
                        } else {
                            errors.add(ar.cause().getMessage());
                            deltaInfoPromise.fail(ar.cause());
                        }
                    });
                }
            }
        });
    }

    private DeltaRangeInvalidException createDeltaRangeInvalidException(Set<String> errors) {
        return new DeltaRangeInvalidException(String.join(";", errors));
    }

    private void calculateSelectOnNum(DeltaInformation deltaInformation, Handler<AsyncResult<Long>> handler) {
        switch (deltaInformation.getType()) {
            case NUM:
                deltaService.getCnToByDeltaNum(deltaInformation.getSchemaName(), deltaInformation.getSelectOnNum())
                        .onComplete(handler);
                break;
            case DATETIME:
                val localDateTime = deltaInformation.getDeltaTimestamp().replace("\'", "");
                deltaService.getCnToByDeltaDatetime(deltaInformation.getSchemaName(), CalciteUtil.parseLocalDateTime(localDateTime))
                        .onComplete(handler);
                break;
            default:
                handler.handle(Future.failedFuture(new UnsupportedOperationException("Delta type not supported")));
                break;
        }
    }


    private void calculateSelectOnInterval(DeltaInformation deltaInformation, Handler<AsyncResult<SelectOnInterval>> handler) {
        Long deltaFrom = deltaInformation.getSelectOnInterval().getSelectOnFrom();
        Long deltaTo = deltaInformation.getSelectOnInterval().getSelectOnTo();
        deltaService.getCnFromCnToByDeltaNums(deltaInformation.getSchemaName(), deltaFrom, deltaTo)
                .onComplete(handler);
    }
}
