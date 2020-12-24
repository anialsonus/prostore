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
package io.arenadata.dtm.query.execution.plugin.adqm.factory.impl;

import io.arenadata.dtm.common.model.ddl.Entity;
import io.arenadata.dtm.common.model.ddl.EntityField;
import io.arenadata.dtm.common.plugin.sql.PreparedStatementRequest;
import io.arenadata.dtm.query.execution.plugin.adqm.configuration.properties.DdlProperties;
import io.arenadata.dtm.query.execution.plugin.adqm.dto.AdqmRollbackRequest;
import io.arenadata.dtm.query.execution.plugin.api.factory.RollbackRequestFactory;
import io.arenadata.dtm.query.execution.plugin.api.request.RollbackRequest;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AdqmRollbackRequestFactory implements RollbackRequestFactory<AdqmRollbackRequest> {

    private static final String DROP_TABLE_TEMPLATE = "DROP TABLE IF EXISTS %s.%s_%s ON CLUSTER %s";
    private static final String SYSTEM_FLUSH_TEMPLATE = "SYSTEM FLUSH DISTRIBUTED %s.%s_actual";
    private static final String INSERT_INTO_TEMPLATE = "INSERT INTO <dbname>.<tablename>_actual\n" +
        "  SELECT <fields>, sys_from, sys_to, sys_op, close_date, -1\n" +
        "  FROM <dbname>.<tablename>_actual FINAL\n" +
        "  WHERE sys_from = <sys_cn> AND sign = 1\n" +
        "  UNION ALL\n" +
        "  SELECT <fields>, sys_from, toInt64(<maxLong>) AS sys_to, 0 AS sys_op, toDateTime('9999-12-31 00:00:00') AS close_date, arrayJoin([-1, 1])\n" +
        "  FROM <dbname>.<tablename>_actual FINAL\n" +
        "  WHERE sys_to = <prev_sys_cn> AND sign = 1";
    private static final String OPTIMIZE_TABLE_TEMPLATE = "OPTIMIZE TABLE %s.%s_actual_shard ON CLUSTER %s FINAL";

    private final DdlProperties ddlProperties;

    @Override
    public AdqmRollbackRequest create(RollbackRequest rollbackRequest) {
        val cluster = ddlProperties.getCluster();
        Entity entity = rollbackRequest.getEntity();
        val entityName = entity.getName();
        val dbName = getDbName(rollbackRequest);
        val sysCn = rollbackRequest.getSysCn();
        return new AdqmRollbackRequest(
            Arrays.asList(
                PreparedStatementRequest.onlySql(getDropTableSql(dbName, entityName, "ext_shard", cluster)),
                PreparedStatementRequest.onlySql(getDropTableSql(dbName, entityName, "buffer_loader_shard", cluster)),
                PreparedStatementRequest.onlySql(getDropTableSql(dbName, entityName, "buffer", cluster)),
                PreparedStatementRequest.onlySql(getDropTableSql(dbName, entityName, "buffer_shard", cluster)),
                PreparedStatementRequest.onlySql(String.format(SYSTEM_FLUSH_TEMPLATE, dbName, entityName)),
                PreparedStatementRequest.onlySql(gerInsertSql(dbName, entity, sysCn)),
                PreparedStatementRequest.onlySql(String.format(SYSTEM_FLUSH_TEMPLATE, dbName, entityName)),
                PreparedStatementRequest.onlySql(String.format(OPTIMIZE_TABLE_TEMPLATE, dbName, entityName, cluster))
            )
        );
    }

    private String getDbName(RollbackRequest rollbackRequest) {
        return rollbackRequest.getQueryRequest().getEnvName() + "__" + rollbackRequest.getDatamart();
    }

    private String gerInsertSql(String datamart, Entity entity, long sysCn) {
        val fields = entity.getFields().stream()
            .map(EntityField::getName)
            .collect(Collectors.joining(","));
        return INSERT_INTO_TEMPLATE
            .replaceAll("<dbname>", datamart)
            .replaceAll("<tablename>", entity.getName())
            .replaceAll("<fields>", fields)
            .replaceAll("<maxLong>", String.valueOf(Long.MAX_VALUE))
            .replaceAll("<sys_cn>", String.valueOf(sysCn))
            .replaceAll("<prev_sys_cn>", String.valueOf(sysCn - 1));
    }

    private String getDropTableSql(String datamart, String entity, String tableSuffix, String cluster) {
        return String.format(DROP_TABLE_TEMPLATE, datamart, entity, tableSuffix, cluster);
    }
}
