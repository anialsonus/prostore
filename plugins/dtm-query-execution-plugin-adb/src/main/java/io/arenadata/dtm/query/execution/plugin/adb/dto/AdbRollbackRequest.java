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
package io.arenadata.dtm.query.execution.plugin.adb.dto;

import io.arenadata.dtm.common.plugin.sql.PreparedStatementRequest;
import io.arenadata.dtm.query.execution.plugin.api.rollback.PluginRollbackRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Arrays;

@Data
@EqualsAndHashCode(callSuper = true)
public class AdbRollbackRequest extends PluginRollbackRequest {
    private final PreparedStatementRequest deleteFromHistory;
    private final PreparedStatementRequest deleteFromActual;
    private final PreparedStatementRequest truncate;
    private final PreparedStatementRequest insert;

    public AdbRollbackRequest(PreparedStatementRequest deleteFromHistory,
                              PreparedStatementRequest deleteFromActual,
                              PreparedStatementRequest truncate,
                              PreparedStatementRequest insert) {
        super(Arrays.asList(
            truncate,
            deleteFromActual,
            insert,
            deleteFromHistory
        ));
        this.deleteFromHistory = deleteFromHistory;
        this.deleteFromActual = deleteFromActual;
        this.truncate = truncate;
        this.insert = insert;
    }
}
