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
package io.arenadata.dtm.query.execution.plugin.adb.calcite.factory;

import io.arenadata.dtm.query.calcite.core.factory.impl.DtmSchemaFactory;
import io.arenadata.dtm.query.calcite.core.schema.dialect.DtmConvention;
import io.arenadata.dtm.query.execution.model.metadata.Datamart;
import io.arenadata.dtm.query.execution.plugin.adb.calcite.model.schema.dialect.AdbDtmConvention;
import org.apache.calcite.linq4j.tree.Expression;
import org.springframework.stereotype.Service;

@Service("adbSchemaFactory")
public class AdbSchemaFactory extends DtmSchemaFactory {
    @Override
    protected DtmConvention createDtmConvention(Datamart datamart, Expression expression) {
        return new AdbDtmConvention(datamart, expression);
    }
}
