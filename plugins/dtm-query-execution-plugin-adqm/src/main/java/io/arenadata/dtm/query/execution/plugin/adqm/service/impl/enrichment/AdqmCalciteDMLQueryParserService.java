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
package io.arenadata.dtm.query.execution.plugin.adqm.service.impl.enrichment;

import io.arenadata.dtm.common.model.ddl.Entity;
import io.arenadata.dtm.query.calcite.core.provider.CalciteContextProvider;
import io.arenadata.dtm.query.calcite.core.service.impl.CalciteDMLQueryParserService;
import io.arenadata.dtm.query.execution.model.metadata.Datamart;
import io.vertx.core.Vertx;
import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static io.arenadata.dtm.query.execution.plugin.adqm.service.impl.enrichment.AdqmSchemaExtenderImpl.getExtendedColumns;

@Service("adqmCalciteDMLQueryParserService")
public class AdqmCalciteDMLQueryParserService extends CalciteDMLQueryParserService {
    public AdqmCalciteDMLQueryParserService(
        @Qualifier("adqmCalciteContextProvider") CalciteContextProvider contextProvider,
        @Qualifier("coreVertx") Vertx vertx
    ) {
        super(contextProvider, vertx);
    }

    @Override
    protected List<Datamart> extendSchemes(List<Datamart> datamarts) {
        return super.extendSchemes(datamarts.stream()
            .map(this::withSystemFields)
            .collect(Collectors.toList()));
    }

    private Datamart withSystemFields(Datamart logicalSchema) {
        Datamart extendedSchema = new Datamart();
        extendedSchema.setMnemonic(logicalSchema.getMnemonic());
        List<Entity> extendedDatamartClasses = new ArrayList<>();
        logicalSchema.getEntities().forEach(entity -> {
            val extendedFields = new ArrayList<>(entity.getFields());
            extendedFields.addAll(getExtendedColumns());
            extendedDatamartClasses.add(entity.toBuilder()
                .fields(extendedFields)
                .build());
        });
        extendedSchema.setEntities(extendedDatamartClasses);
        return extendedSchema;
    }
}
