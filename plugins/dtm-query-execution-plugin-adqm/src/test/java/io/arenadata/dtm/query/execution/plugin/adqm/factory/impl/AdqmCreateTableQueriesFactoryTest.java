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

import io.arenadata.dtm.common.model.ddl.ColumnType;
import io.arenadata.dtm.common.model.ddl.Entity;
import io.arenadata.dtm.common.model.ddl.EntityField;
import io.arenadata.dtm.common.reader.QueryRequest;
import io.arenadata.dtm.query.execution.plugin.adqm.configuration.properties.DdlProperties;
import io.arenadata.dtm.query.execution.plugin.adqm.dto.AdqmTables;
import io.arenadata.dtm.query.execution.plugin.api.ddl.DdlRequestContext;
import io.arenadata.dtm.query.execution.plugin.api.request.DdlRequest;
import io.arenadata.dtm.query.execution.plugin.api.factory.CreateTableQueriesFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class AdqmCreateTableQueriesFactoryTest {

    private static final String EXPECTED_CREATE_SHARD_TABLE_QUERY = "CREATE TABLE " +
            "env__test_schema.test_table_actual_shard ON CLUSTER test_arenadata\n" +
            "(id Int64, sk_key2 Int64, pk2 Int64, sk_key3 Int64, VARCHAR_type Nullable(String), " +
            "CHAR_type Nullable(String), BIGINT_type Nullable(Int64), INT_type Nullable(Int64), " +
            "DOUBLE_type Nullable(Float64), FLOAT_type Nullable(Float32), DATE_type Nullable(Int64), " +
            "TIME_type Nullable(Int64), TIMESTAMP_type Nullable(DateTime64(3)), BOOLEAN_type Nullable(UInt8), " +
            "UUID_type Nullable(String), sys_from Int64, sys_to Int64, sys_op Int8, close_date DateTime, sign Int8)\n" +
            "ENGINE = CollapsingMergeTree(sign)\n" +
            "ORDER BY (id, pk2, sys_from)\n" +
            "TTL close_date + INTERVAL 3600 SECOND TO DISK 'default'";

    private static final String EXPECTED_CREATE_DISTRIBUTED_TABLE_QUERY = "CREATE TABLE " +
            "env__test_schema.test_table_actual ON CLUSTER test_arenadata\n" +
            "(id Int64, sk_key2 Int64, pk2 Int64, sk_key3 Int64, VARCHAR_type Nullable(String), " +
            "CHAR_type Nullable(String), BIGINT_type Nullable(Int64), INT_type Nullable(Int64), " +
            "DOUBLE_type Nullable(Float64), FLOAT_type Nullable(Float32), DATE_type Nullable(Int64), " +
            "TIME_type Nullable(Int64), TIMESTAMP_type Nullable(DateTime64(3)), BOOLEAN_type Nullable(UInt8), " +
            "UUID_type Nullable(String), sys_from Int64, sys_to Int64, sys_op Int8, close_date DateTime, sign Int8)\n" +
            "Engine = Distributed(test_arenadata, env__test_schema, test_table_actual_shard, id)";

    private AdqmTables<String> adqmTables;

    @BeforeEach
    void setUp() {
        Entity entity = getEntity();
        DdlRequestContext context = new DdlRequestContext(new DdlRequest(new QueryRequest(), entity));
        context.getRequest().getQueryRequest().setEnvName("env");
        DdlProperties ddlProperties = new DdlProperties();
        ddlProperties.setTtlSec(3600);
        ddlProperties.setCluster("test_arenadata");
        ddlProperties.setArchiveDisk("default");
        CreateTableQueriesFactory<AdqmTables<String>> adqmCreateTableQueriesFactory =
                new AdqmCreateTableQueriesFactory(ddlProperties, new AdqmTableEntitiesFactory());
        adqmTables = adqmCreateTableQueriesFactory.create(context);
    }

    @Test
    void createShardTableQueryTest() {
        assertEquals(EXPECTED_CREATE_SHARD_TABLE_QUERY, adqmTables.getShard());
    }

    @Test
    void createDistributedTableQueryTest() {
        assertEquals(EXPECTED_CREATE_DISTRIBUTED_TABLE_QUERY, adqmTables.getDistributed());
    }

    public static Entity getEntity() {
        List<EntityField> keyFields = Arrays.asList(
                new EntityField(0, "id", ColumnType.INT.name(), false, 1, 1, null),
                new EntityField(1, "sk_key2", ColumnType.INT.name(), false, null, 2, null),
                new EntityField(2, "pk2", ColumnType.INT.name(), false, 2, null, null),
                new EntityField(3, "sk_key3", ColumnType.INT.name(), false, null, 3, null)
        );
        ColumnType[] types = ColumnType.values();
        List<EntityField> fields = new ArrayList<>();
        for (int i = 0; i < types.length; i++) {
            ColumnType type = types[i];
            if (Arrays.asList(ColumnType.BLOB, ColumnType.ANY).contains(type)) {
                continue;
            }

            EntityField.EntityFieldBuilder builder = EntityField.builder()
                    .ordinalPosition(i + keyFields.size())
                    .type(type)
                    .nullable(true)
                    .name(type.name() + "_type");
            if (Arrays.asList(ColumnType.CHAR, ColumnType.VARCHAR).contains(type)) {
                builder.size(20);
            } else if (Arrays.asList(ColumnType.TIME, ColumnType.TIMESTAMP).contains(type)) {
                builder.accuracy(5);
            }
            fields.add(builder.build());
        }
        fields.addAll(keyFields);
        return new Entity("test_schema.test_table", fields);
    }
}
