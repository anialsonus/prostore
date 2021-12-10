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
package io.arenadata.dtm.query.execution.plugin.adg.base.service.converter;

import io.arenadata.dtm.common.converter.SqlTypeConverter;
import io.arenadata.dtm.common.converter.transformer.ColumnTransformer;
import io.arenadata.dtm.common.model.ddl.ColumnType;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;

@Component("adgTypeToSqlTypeConverter")
public class AdgTypeToSqlTypeConverter implements SqlTypeConverter {

    @Override
    public Map<ColumnType, Map<Class<?>, ColumnTransformer>> getTransformerMap() {
        return Collections.emptyMap();
    }

    @Override
    public Object convert(ColumnType type, Object value) {
        return value;
    }
}
