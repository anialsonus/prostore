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
package io.arenadata.dtm.query.calcite.core.extension.eddl;

import io.arenadata.dtm.common.plugin.exload.Format;
import org.apache.calcite.sql.*;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.calcite.util.ImmutableNullableList;

import java.util.List;

public class FormatOperator extends SqlCall {

    private static final SqlOperator OPERATOR_FORMAT =
            new SqlSpecialOperator("FORMAT", SqlKind.OTHER_DDL);
    private final Format format;

    FormatOperator(SqlParserPos pos, SqlCharStringLiteral format) {
        super(pos);
        this.format = Format.findByName(format.getNlsString().getValue());

    }

    @Override
    public SqlOperator getOperator() {
        return OPERATOR_FORMAT;
    }

    @Override
    public List<SqlNode> getOperandList() {
        return ImmutableNullableList.of(null);
    }

    @Override
    public void unparse(SqlWriter writer, int leftPrec, int rightPrec) {
        writer.keyword(this.getOperator().getName());
        writer.keyword("'" + this.format.getName() + "'");
    }

    public Format getFormat() {
        return format;
    }
}
