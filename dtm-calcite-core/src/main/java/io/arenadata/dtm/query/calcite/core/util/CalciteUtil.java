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
package io.arenadata.dtm.query.calcite.core.util;

import io.arenadata.dtm.common.model.ddl.ColumnType;
import lombok.val;
import org.apache.calcite.sql.type.SqlTypeName;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.apache.calcite.sql.type.SqlTypeName.*;

public class CalciteUtil {
    private static final String LOCAL_DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    public static final DateTimeFormatter LOCAL_DATE_TIME = DateTimeFormatter.ofPattern(LOCAL_DATE_TIME_PATTERN);

    public static LocalDateTime parseLocalDateTime(String localDateTime) {
        try {
            return LocalDateTime.parse(localDateTime, CalciteUtil.LOCAL_DATE_TIME);
        } catch (Exception e) {
            val errMsg = String.format("Time[%s] is not in format: [%s]", localDateTime, LOCAL_DATE_TIME_PATTERN);
            throw new RuntimeException(errMsg, e);
        }
    }

    public static String parseSchemaName(String nameWithSchema) {
        int indexComma = nameWithSchema.indexOf(".");
        return indexComma != -1 ? nameWithSchema.substring(0, indexComma) : null;
    }

    public static String parseTableName(String nameWithSchema) {
        int indexComma = nameWithSchema.indexOf(".");
        return nameWithSchema.substring(indexComma + 1);
    }

    public static SqlTypeName valueOf(ColumnType type) {
        switch (type) {
            case BOOLEAN:
                return BOOLEAN;
            case INT:
                return INTEGER;
            case BIGINT:
                return BIGINT;
            case FLOAT:
                return FLOAT;
            case DOUBLE:
                return DOUBLE;
            case TIME:
                return TIME;
            case DATE:
                return DATE;
            case TIMESTAMP:
                return TIMESTAMP;
            case CHAR:
                return CHAR;
            case VARCHAR:
            case UUID:
                return VARCHAR;
            default:
                return ANY;
        }
    }

    public static ColumnType toColumnType(SqlTypeName type) {
        switch (type) {
            case BOOLEAN:
                return ColumnType.BOOLEAN;
            case INTEGER:
                return ColumnType.INT;
            case BIGINT:
                return ColumnType.BIGINT;
            case FLOAT:
                return ColumnType.FLOAT;
            case DOUBLE:
                return ColumnType.DOUBLE;
            case DATE:
                return ColumnType.DATE;
            case TIME:
                return ColumnType.TIME;
            case TIMESTAMP:
                return ColumnType.TIMESTAMP;
            case VARCHAR:
                return ColumnType.VARCHAR;
            case CHAR:
                return ColumnType.CHAR;
            default:
                return ColumnType.ANY;
        }
    }
}
