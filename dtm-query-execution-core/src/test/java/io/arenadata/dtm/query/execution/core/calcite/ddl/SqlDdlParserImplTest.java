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
package io.arenadata.dtm.query.execution.core.calcite.ddl;

import io.arenadata.dtm.common.reader.SourceType;
import io.arenadata.dtm.query.calcite.core.configuration.CalciteCoreConfiguration;
import io.arenadata.dtm.query.calcite.core.extension.check.SqlCheckData;
import io.arenadata.dtm.query.calcite.core.extension.check.SqlCheckDatabase;
import io.arenadata.dtm.query.calcite.core.extension.check.SqlCheckTable;
import io.arenadata.dtm.query.calcite.core.extension.ddl.*;
import io.arenadata.dtm.query.calcite.core.service.DefinitionService;
import io.arenadata.dtm.query.execution.core.configuration.calcite.CalciteConfiguration;
import io.arenadata.dtm.query.execution.core.service.impl.CoreCalciteDefinitionService;
import org.apache.calcite.sql.SqlDialect;
import org.apache.calcite.sql.SqlIdentifier;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.parser.SqlParseException;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SqlDdlParserImplTest {
    private static final String CREATE_TABLE_QUERY = "create table test.table_name\n" +
            "(\n" +
            "    account_id bigint,\n" +
            "    account_type varchar(1), -- D/C (дебет/кредит)\n" +
            "    primary key (account_id)\n" +
            ") distributed by (account_id)";
    private static final String DROP_TABLE_QUERY = "drop table test.table_name";
    private CalciteConfiguration calciteConfiguration = new CalciteConfiguration();
    private CalciteCoreConfiguration calciteCoreConfiguration = new CalciteCoreConfiguration();
    private final DefinitionService<SqlNode> definitionService =
            new CoreCalciteDefinitionService(calciteConfiguration.configEddlParser(
                    calciteCoreConfiguration.eddlParserImplFactory()));

    @Test
    void parseAlter() {
        SqlNode sqlNode = definitionService.processingQuery("ALTER VIEW test.view_a AS SELECT * FROM test.tab_1");
        assertTrue(sqlNode instanceof SqlAlterView);
        assertEquals(Arrays.asList("test", "view_a"),
                ((SqlIdentifier) ((SqlAlterView) sqlNode).getOperandList().get(0)).names);
        assertEquals(3, ((SqlAlterView) sqlNode).getOperandList().size());
    }

    @Test
    void parseUseSchema() {
        SqlNode sqlNode = definitionService.processingQuery("USE shares");
        assertTrue(sqlNode instanceof SqlUseSchema);
        assertEquals("shares", ((SqlIdentifier) ((SqlUseSchema) sqlNode).getOperandList().get(0)).names.get(0));
        assertEquals("USE shares", sqlNode.toSqlString(new SqlDialect(SqlDialect.EMPTY_CONTEXT)).toString());
    }

    @Test
    void parseUseSchemaWithQuotes() {
        SqlNode sqlNode = definitionService.processingQuery("USE \"shares\"");
        assertTrue(sqlNode instanceof SqlUseSchema);
        assertEquals("shares", ((SqlIdentifier) ((SqlUseSchema) sqlNode).getOperandList().get(0)).names.get(0));
        assertEquals("USE shares", sqlNode.toSqlString(new SqlDialect(SqlDialect.EMPTY_CONTEXT)).toString());
    }

    @Test
    void parseIncorrectUseSchema() {
        assertThrows(SqlParseException.class, () -> {
            definitionService.processingQuery("USEshares");
        });
        assertThrows(SqlParseException.class, () -> {
            definitionService.processingQuery("USE shares t");
        });
        assertThrows(SqlParseException.class, () -> {
            definitionService.processingQuery("USE 'shares'");
        });
    }

    @Test
    void parseAlterWithoutFromClause() {
        assertThrows(SqlParseException.class, () -> {
            definitionService.processingQuery("ALTER VIEW test.view_a AS SELECT * ");
        });
    }

    @Test
    void parseCreateViewSuccess() {
        SqlNode sqlNode = definitionService.processingQuery("CREATE VIEW test.view_a AS SELECT * FROM test.tab_1");
        assertTrue(sqlNode instanceof SqlCreateView);
    }

    @Test
    void parseCreateViewWithoutFromClause() {
        assertThrows(SqlParseException.class, () -> {
            definitionService.processingQuery("CREATE VIEW test.view_a AS SELECT * ft");
        });
    }

    @Test
    void createTable() {
        createTable(CREATE_TABLE_QUERY);
    }

    @Test
    void createTableWithDestination() {
        Set<SourceType> selectedSourceTypes = new HashSet<>();
        selectedSourceTypes.add(SourceType.ADB);
        selectedSourceTypes.add(SourceType.ADG);
        String query = String.format(CREATE_TABLE_QUERY + " DATASOURCE_TYPE (%s)",
                selectedSourceTypes.stream().map(SourceType::name).collect(Collectors.joining(", ")));
        createTable(query, sqlCreateTable -> assertEquals(selectedSourceTypes, sqlCreateTable.getDestination()));
    }

    @Test
    void createTableWithInformationSchema() {
        String query = String.format(CREATE_TABLE_QUERY + " DATASOURCE_TYPE (%s)",
                SourceType.INFORMATION_SCHEMA.name());
        assertThrows(SqlParseException.class, () -> createTable(query));
    }

    @Test
    void createTableWithInvalidDestination() {
        String query = String.format(CREATE_TABLE_QUERY + " DATASOURCE_TYPE (%s)", "adcvcb");
        assertThrows(SqlParseException.class, () -> createTable(query));
    }

    void createTable(String query) {
        createTable(query, sqlCreateTable -> {
        });
    }

    void createTable(String query, Consumer<SqlCreateTable> consumer) {
        SqlNode sqlNode = definitionService.processingQuery(query);
        assertTrue(sqlNode instanceof SqlCreateTable);
        SqlCreateTable sqlCreateTable = (SqlCreateTable) sqlNode;
        assertEquals(Arrays.asList("test", "table_name"),
                ((SqlIdentifier) sqlCreateTable.getOperandList().get(0)).names);
        consumer.accept(sqlCreateTable);
    }

    @Test
    void dropTable() {
        dropTable(DROP_TABLE_QUERY);
    }

    @Test
    void dropTableWithDestination() {
        Set<SourceType> selectedSourceTypes = new HashSet<>();
        selectedSourceTypes.add(SourceType.ADB);
        selectedSourceTypes.add(SourceType.ADG);
        String query = String.format(DROP_TABLE_QUERY + " DATASOURCE_TYPE (%s)",
                selectedSourceTypes.stream().map(SourceType::name).collect(Collectors.joining(", ")));
        dropTable(query, sqlDropTable -> assertEquals(selectedSourceTypes, sqlDropTable.getDestination()));
    }

    @Test
    void dropTableWithInformationSchema() {
        String query = String.format(DROP_TABLE_QUERY + " DATASOURCE_TYPE (%s)",
                SourceType.INFORMATION_SCHEMA.name());
        assertThrows(SqlParseException.class, () -> dropTable(query));
    }

    @Test
    void dropTableWithInvalidDestination() {
        String query = String.format(DROP_TABLE_QUERY + " DATASOURCE_TYPE (%s)", "adcvcb");
        assertThrows(SqlParseException.class, () -> dropTable(query));
    }

    @Test
    void checkDatabase() {
        String schema = "test";
        String correctSchema = "CHECK_DATABASE(test)";
        String withoutSchema = "CHECK_DATABASE()";
        String withoutSchema2 = "CHECK_DATABASE";
        String incorrectSchema = "CHECK_DATABASE('77')";
        String incorrectSchema2 = "CHECK_DATABASE(test.ttt)";

        SqlNode sqlNode1 = definitionService.processingQuery(correctSchema);
        SqlNode sqlNode2 = definitionService.processingQuery(withoutSchema);
        SqlNode sqlNode3 = definitionService.processingQuery(withoutSchema2);
        assertEquals(schema, ((SqlCheckDatabase) sqlNode1).getSchema());
        assertNull(((SqlCheckDatabase) sqlNode2).getSchema());
        assertNull(((SqlCheckDatabase) sqlNode3).getSchema());
        assertThrows(SqlParseException.class, () -> definitionService.processingQuery(incorrectSchema));
        assertThrows(SqlParseException.class, () -> definitionService.processingQuery(incorrectSchema2));
    }

    @Test
    void checkTable() {
        String schema = "test";
        String table = "test_table";
        String withSchema = "CHECK_TABLE(test.test_table)";
        String withoutSchema = "CHECK_TABLE(test_table)";
        String incorrectTable = "CHECK_TABLE()";
        SqlNode sqlNode1 = definitionService.processingQuery(withSchema);
        SqlNode sqlNode2 = definitionService.processingQuery(withoutSchema);

        assertEquals(schema, ((SqlCheckTable) sqlNode1).getSchema());
        assertEquals(table, ((SqlCheckTable) sqlNode1).getTable());
        assertNull(((SqlCheckTable) sqlNode2).getSchema());
        assertThrows(SqlParseException.class, () -> definitionService.processingQuery(incorrectTable));
    }

    @Test
    void checkData() {
        String schema = "test";
        String table = "testtable";
        Long deltaNum = 1L;
        Set<String> columns = new HashSet<>(Arrays.asList("id", "name"));
        String withSchema = "CHECK_DATA(test.testTable, 1, [id, name])";
        String withoutSchema = "CHECK_DATA(testTable, 1, [id, name])";
        String withoutColumns = "CHECK_DATA(test.testTable, 1)";
        String withIncorrectColumns = "CHECK_DATA(test.testTable, 1, [id,, name])";
        String withIncorrectDelta = "CHECK_DATA(test.testTable, a, [id, name])";
        SqlNode sqlNode1 = definitionService.processingQuery(withSchema);
        assertEquals(schema, ((SqlCheckData) sqlNode1).getSchema());
        assertEquals(table, ((SqlCheckData) sqlNode1).getTable());
        assertEquals(columns, ((SqlCheckData) sqlNode1).getColumns());
        assertEquals(deltaNum, ((SqlCheckData) sqlNode1).getDeltaNum());

        SqlNode sqlNode2 = definitionService.processingQuery(withoutSchema);
        assertNull(((SqlCheckData) sqlNode2).getSchema());
        assertEquals(table, ((SqlCheckData) sqlNode2).getTable());
        assertEquals(columns, ((SqlCheckData) sqlNode2).getColumns());
        assertEquals(deltaNum, ((SqlCheckData) sqlNode2).getDeltaNum());

        SqlNode sqlNode3 = definitionService.processingQuery(withoutColumns);
        assertEquals(schema, ((SqlCheckData) sqlNode3).getSchema());
        assertEquals(table, ((SqlCheckData) sqlNode3).getTable());
        assertNull(((SqlCheckData) sqlNode3).getColumns());
        assertEquals(deltaNum, ((SqlCheckData) sqlNode3).getDeltaNum());

        assertThrows(SqlParseException.class, () -> definitionService.processingQuery(withIncorrectColumns));
        assertThrows(SqlParseException.class, () -> definitionService.processingQuery(withIncorrectDelta));

    }

    void dropTable(String query) {
        dropTable(query, sqlDropTable -> {
        });
    }

    void dropTable(String query, Consumer<SqlDropTable> consumer) {
        SqlNode sqlNode = definitionService.processingQuery(query);
        assertTrue(sqlNode instanceof SqlDropTable);
        SqlDropTable sqlDropTable = (SqlDropTable) sqlNode;
        assertEquals(Arrays.asList("test", "table_name"),
                ((SqlIdentifier) sqlDropTable.getOperandList().get(0)).names);
        consumer.accept(sqlDropTable);
    }
}
