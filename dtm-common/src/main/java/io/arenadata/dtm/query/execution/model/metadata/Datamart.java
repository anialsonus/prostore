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
package io.arenadata.dtm.query.execution.model.metadata;

import io.arenadata.dtm.common.model.ddl.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * Schema Description SchemaDescription
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Datamart implements Serializable {
    /**
     * Schema name
     */
    private String mnemonic;
    /**
     * default showcase attribute
     */
    private Boolean isDefault = false;
    /**
     * Description of tables in the schema
     */
    private List<Entity> entities;
}



