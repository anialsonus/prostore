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
package io.arenadata.dtm.query.execution.model.metadata;

import io.arenadata.dtm.common.model.ddl.ColumnType;
import io.arenadata.dtm.common.model.ddl.SystemMetadata;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ColumnMetadata {
    /**
     * Column name
     */
    private String name;
    /**
     * System column type
     */
    private SystemMetadata systemMetadata;
    /**
     * Column data type
     */
    private ColumnType type;

    /**
     * Column size
     */
    private Integer size;

    public ColumnMetadata(String name, ColumnType type) {
        this.name = name;
        this.type = type;
    }

    public ColumnMetadata(String name, SystemMetadata systemMetadata, ColumnType type) {
        this.name = name;
        this.systemMetadata = systemMetadata;
        this.type = type;
    }

    public ColumnMetadata(String name, ColumnType type, Integer size) {
        this.name = name;
        this.type = type;
        this.size = size;
    }
}
