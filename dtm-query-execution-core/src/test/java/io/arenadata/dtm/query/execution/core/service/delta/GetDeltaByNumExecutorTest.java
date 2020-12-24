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
package io.arenadata.dtm.query.execution.core.service.delta;

import io.arenadata.dtm.common.reader.QueryRequest;
import io.arenadata.dtm.common.reader.QueryResult;
import io.arenadata.dtm.query.execution.core.dao.ServiceDbFacade;
import io.arenadata.dtm.query.execution.core.dao.ServiceDbFacadeImpl;
import io.arenadata.dtm.query.execution.core.dao.delta.zookeeper.DeltaServiceDao;
import io.arenadata.dtm.query.execution.core.dao.delta.zookeeper.impl.DeltaServiceDaoImpl;
import io.arenadata.dtm.query.execution.core.dto.delta.OkDelta;
import io.arenadata.dtm.query.execution.core.dto.delta.query.GetDeltaByNumQuery;
import io.arenadata.dtm.query.execution.core.dto.delta.query.GetDeltaOkQuery;
import io.arenadata.dtm.query.execution.core.factory.DeltaQueryResultFactory;
import io.arenadata.dtm.query.execution.core.factory.impl.delta.BeginDeltaQueryResultFactory;
import io.arenadata.dtm.query.execution.core.service.delta.impl.GetDeltaByNumExecutor;
import io.arenadata.dtm.query.execution.core.service.delta.impl.GetDeltaOkExecutor;
import io.arenadata.dtm.query.execution.core.utils.DeltaQueryUtil;
import io.arenadata.dtm.query.execution.core.utils.QueryResultUtils;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GetDeltaByNumExecutorTest {

    private final ServiceDbFacade serviceDbFacade = mock(ServiceDbFacadeImpl.class);
    private final DeltaServiceDao deltaServiceDao = mock(DeltaServiceDaoImpl.class);
    private final DeltaQueryResultFactory deltaQueryResultFactory = mock(BeginDeltaQueryResultFactory.class);
    private DeltaExecutor deltaByNumExecutor;
    private QueryRequest req = new QueryRequest();
    private String datamart;

    @BeforeEach
    void setUp() {
        datamart = "test_datamart";
        req.setDatamartMnemonic(datamart);
        req.setRequestId(UUID.fromString("6efad624-b9da-4ba1-9fed-f2da478b08e8"));
        when(serviceDbFacade.getDeltaServiceDao()).thenReturn(deltaServiceDao);
    }

    @Test
    void executeSuccess() {
        deltaByNumExecutor = new GetDeltaByNumExecutor(serviceDbFacade, deltaQueryResultFactory);
        Promise promise = Promise.promise();
        final LocalDateTime deltaDate = LocalDateTime.parse("2020-06-15 14:00:11",
                DeltaQueryUtil.DELTA_DATE_TIME_FORMATTER);
        final long cnFrom = 0L;
        final long deltaNum = 1L;
        GetDeltaByNumQuery deltaQuery = GetDeltaByNumQuery.builder()
                .request(req)
                .deltaNum(deltaNum)
                .datamart(datamart)
                .build();

        OkDelta deltaOk = OkDelta.builder()
                .cnFrom(cnFrom)
                .deltaNum(deltaNum)
                .deltaDate(deltaDate)
                .build();

        QueryResult queryResult = new QueryResult();
        queryResult.setRequestId(req.getRequestId());
        queryResult.setResult(createResult(deltaNum, deltaDate, cnFrom, null));

        when(deltaServiceDao.getDeltaByNum(eq(datamart), eq(deltaNum)))
                .thenReturn(Future.succeededFuture(deltaOk));

        when(deltaQueryResultFactory.create(any()))
                .thenReturn(queryResult);

        deltaByNumExecutor.execute(deltaQuery, handler -> {
            if (handler.succeeded()) {
                promise.complete(handler.result());
            } else {
                promise.fail(handler.cause());
            }
        });

        assertEquals(deltaNum, ((QueryResult) promise.future().result()).getResult()
                .get(0).get(DeltaQueryUtil.NUM_FIELD));
        assertEquals(deltaDate, ((QueryResult) promise.future().result()).getResult()
                .get(0).get(DeltaQueryUtil.DATE_TIME_FIELD));
        assertEquals(cnFrom, ((QueryResult) promise.future().result()).getResult()
                .get(0).get(DeltaQueryUtil.CN_FROM_FIELD));
        assertNull(((QueryResult) promise.future().result()).getResult()
                .get(0).get(DeltaQueryUtil.CN_TO_FIELD));
    }

    @Test
    void executeEmptySuccess() {
        deltaByNumExecutor = new GetDeltaByNumExecutor(serviceDbFacade, deltaQueryResultFactory);
        Promise promise = Promise.promise();
        final long deltaNum = 1L;
        GetDeltaByNumQuery deltaQuery = GetDeltaByNumQuery.builder()
                .request(req)
                .deltaNum(deltaNum)
                .datamart(datamart)
                .build();

        QueryResult queryResult = new QueryResult();
        queryResult.setRequestId(req.getRequestId());
        queryResult.setResult(new ArrayList<>());

        when(deltaServiceDao.getDeltaByNum(eq(datamart), eq(deltaNum)))
                .thenReturn(Future.succeededFuture(null));

        when(deltaQueryResultFactory.createEmpty())
                .thenReturn(queryResult);

        deltaByNumExecutor.execute(deltaQuery, handler -> {
            if (handler.succeeded()) {
                promise.complete(handler.result());
            } else {
                promise.fail(handler.cause());
            }
        });

        assertTrue(promise.future().succeeded());
    }


    @Test
    void executeDeltaByNumError() {
        deltaByNumExecutor = new GetDeltaByNumExecutor(serviceDbFacade, deltaQueryResultFactory);
        Promise promise = Promise.promise();
        final long deltaNum = 1L;
        GetDeltaByNumQuery deltaQuery = GetDeltaByNumQuery.builder()
                .request(req)
                .deltaNum(deltaNum)
                .datamart(datamart)
                .build();

        when(deltaServiceDao.getDeltaByNum(eq(datamart), eq(deltaNum)))
                .thenReturn(Future.failedFuture(new RuntimeException("")));

        deltaByNumExecutor.execute(deltaQuery, handler -> {
            if (handler.succeeded()) {
                promise.complete(handler.result());
            } else {
                promise.fail(handler.cause());
            }
        });
        assertTrue(promise.future().failed());
    }

    @Test
    void executeDeltaQueryResultFactoryError() {
        deltaByNumExecutor = new GetDeltaByNumExecutor(serviceDbFacade, deltaQueryResultFactory);
        Promise promise = Promise.promise();
        final LocalDateTime deltaDate = LocalDateTime.parse("2020-06-15 14:00:11",
                DeltaQueryUtil.DELTA_DATE_TIME_FORMATTER);
        final long cnFrom = 0L;
        final long deltaNum = 1L;
        GetDeltaByNumQuery deltaQuery = GetDeltaByNumQuery.builder()
                .request(req)
                .deltaNum(deltaNum)
                .datamart(datamart)
                .build();

        OkDelta deltaOk = OkDelta.builder()
                .cnFrom(cnFrom)
                .deltaNum(deltaNum)
                .deltaDate(deltaDate)
                .build();

        when(deltaServiceDao.getDeltaByNum(eq(datamart), eq(deltaNum)))
                .thenReturn(Future.succeededFuture(deltaOk));

        when(deltaQueryResultFactory.create(any()))
                .thenThrow(new RuntimeException(""));

        deltaByNumExecutor.execute(deltaQuery, handler -> {
            if (handler.succeeded()) {
                promise.complete(handler.result());
            } else {
                promise.fail(handler.cause());
            }
        });

        assertTrue(promise.future().failed());
    }

    private List<Map<String, Object>> createResult(long deltaNum, LocalDateTime deltaDate, long cnFrom, Long cnTo) {
        return QueryResultUtils.createResultWithSingleRow(Arrays.asList(
                DeltaQueryUtil.NUM_FIELD,
                DeltaQueryUtil.DATE_TIME_FIELD,
                DeltaQueryUtil.CN_FROM_FIELD,
                DeltaQueryUtil.CN_TO_FIELD),
                Arrays.asList(deltaNum, deltaDate, cnFrom, cnTo));
    }
}