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
package io.arenadata.dtm.common.converter.transformer.impl;

import io.arenadata.dtm.common.converter.transformer.AbstractColumnTransformer;
import io.arenadata.dtm.common.model.ddl.ColumnType;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;

public class LocalDateFromLongTransformer extends AbstractColumnTransformer<LocalDate, Long> {

    @Override
    public LocalDate transformValue(Long value) {
        return value == null ? null : LocalDate.ofEpochDay(value);
    }

    @Override
    public Collection<Class<?>> getTransformClasses() {
        return Collections.singletonList(Long.class);
    }

    @Override
    public ColumnType getType() {
        return ColumnType.DATE;
    }
}
