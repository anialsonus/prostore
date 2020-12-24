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

import io.arenadata.dtm.query.execution.plugin.adqm.configuration.properties.DdlProperties;
import io.arenadata.dtm.query.execution.plugin.adqm.dto.AdqmTableColumn;
import io.arenadata.dtm.query.execution.plugin.adqm.dto.AdqmTableEntity;
import io.arenadata.dtm.query.execution.plugin.adqm.dto.AdqmTables;
import io.arenadata.dtm.query.execution.plugin.api.ddl.DdlRequestContext;
import io.arenadata.dtm.query.execution.plugin.api.factory.CreateTableQueriesFactory;
import io.arenadata.dtm.query.execution.plugin.api.factory.TableEntitiesFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static io.arenadata.dtm.query.execution.plugin.adqm.common.DdlUtils.NOT_NULLABLE_FIELD;
import static io.arenadata.dtm.query.execution.plugin.adqm.common.DdlUtils.NULLABLE_FIELD;

@Service("adqmCreateTableQueriesFactory")
public class AdqmCreateTableQueriesFactory implements CreateTableQueriesFactory<AdqmTables<String>> {

    private final static String CREATE_SHARD_TABLE_TEMPLATE =
            "CREATE TABLE %s__%s.%s ON CLUSTER %s\n" +
                    "(%s)\n" +
                    "ENGINE = CollapsingMergeTree(sign)\n" +
                    "ORDER BY (%s)\n" +
                    "TTL close_date + INTERVAL %d SECOND TO DISK '%s'";

    private final static String CREATE_DISTRIBUTED_TABLE_TEMPLATE =
            "CREATE TABLE %s__%s.%s ON CLUSTER %s\n" +
                    "(%s)\n" +
                    "Engine = Distributed(%s, %s__%s, %s, %s)";

    private final DdlProperties ddlProperties;
    private final TableEntitiesFactory<AdqmTables<AdqmTableEntity>> tableEntitiesFactory;

    @Autowired
    public AdqmCreateTableQueriesFactory(DdlProperties ddlProperties,
                                         TableEntitiesFactory<AdqmTables<AdqmTableEntity>> tableEntitiesFactory) {
        this.ddlProperties = ddlProperties;
        this.tableEntitiesFactory = tableEntitiesFactory;
    }

    @Override
    public AdqmTables<String> create(DdlRequestContext context) {
        String cluster = ddlProperties.getCluster();
        Integer ttlSec = ddlProperties.getTtlSec();
        String archiveDisk = ddlProperties.getArchiveDisk();

        AdqmTables<AdqmTableEntity> tables = tableEntitiesFactory.create(context.getRequest().getEntity(),
                context.getRequest().getQueryRequest().getEnvName());
        AdqmTableEntity shard = tables.getShard();
        AdqmTableEntity distributed = tables.getDistributed();
        return new AdqmTables<>(
                String.format(CREATE_SHARD_TABLE_TEMPLATE,
                        shard.getEnv(),
                        shard.getSchema(),
                        shard.getName(),
                        cluster,
                        getColumnsQuery(shard.getColumns()),
                        String.join(", ", shard.getSortedKeys()),
                        ttlSec,
                        archiveDisk),
                String.format(CREATE_DISTRIBUTED_TABLE_TEMPLATE,
                        distributed.getEnv(),
                        distributed.getSchema(),
                        distributed.getName(),
                        cluster,
                        getColumnsQuery(distributed.getColumns()),
                        cluster,
                        distributed.getEnv(),
                        distributed.getSchema(),
                        shard.getName(),
                        String.join(", ", distributed.getShardingKeys()))
        );
    }

    private String getColumnsQuery(List<AdqmTableColumn> columns) {
        return columns.stream()
                .map(col -> String.format(col.getNullable() ? NULLABLE_FIELD : NOT_NULLABLE_FIELD,
                        col.getName(), col.getType()))
                .collect(Collectors.joining(", "));
    }
}
