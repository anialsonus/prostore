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
package io.arenadata.dtm.query.execution.plugin.adqm.check;

import io.arenadata.dtm.common.model.ddl.Entity;
import io.arenadata.dtm.query.execution.plugin.adqm.check.service.AdqmCheckDataService;
import io.arenadata.dtm.query.execution.plugin.adqm.query.service.DatabaseExecutor;
import io.arenadata.dtm.query.execution.plugin.api.dto.CheckDataByCountRequest;
import io.arenadata.dtm.query.execution.plugin.api.dto.CheckDataByHashInt32Request;
import io.vertx.core.Future;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

public class AdqmCheckDataServiceTest {
    private final static Long RESULT = 1L;
    private final DatabaseExecutor adqmQueryExecutor = mock(DatabaseExecutor.class);
    private final AdqmCheckDataService adqmCheckDataService = new AdqmCheckDataService(adqmQueryExecutor);

    @Test
    void testCheckByHash() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("sum", RESULT);
        when(adqmQueryExecutor.execute(any()))
                .thenReturn(Future.succeededFuture(Collections.singletonList(result)));
        Entity entity = Entity.builder()
                .name("entity")
                .schema("schema")
                .fields(Collections.emptyList())
                .build();
        CheckDataByHashInt32Request request = CheckDataByHashInt32Request.builder()
                .sysCn(1L)
                .envName("env")
                .datamart("schema")
                .columns(Collections.singleton("column"))
                .entity(entity)
                .build();
        adqmCheckDataService.checkDataByHashInt32(request)
                .onComplete(ar -> {
                    assertTrue(ar.succeeded());
                    assertEquals(RESULT, ar.result());
                    verify(adqmQueryExecutor, times(1)).execute(any());
                });
    }

    @Test
    void testCheckByCount() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("count", RESULT);
        when(adqmQueryExecutor.execute(any()))
                .thenReturn(Future.succeededFuture(Collections.singletonList(result)));
        CheckDataByCountRequest request = CheckDataByCountRequest.builder()
                .sysCn(1L)
                .envName("env")
                .datamart("schema")
                .entity(Entity.builder().build())
                .build();
        adqmCheckDataService.checkDataByCount(request)
                .onComplete(ar -> {
                    assertTrue(ar.succeeded());
                    assertEquals(RESULT, ar.result());
                    verify(adqmQueryExecutor, times(1)).execute(any());
                });
    }
}
