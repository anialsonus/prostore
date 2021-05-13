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
package io.arenadata.dtm.query.execution.plugin.adqm.enrichment.service;

import io.arenadata.dtm.common.calcite.CalciteContext;
import io.arenadata.dtm.common.delta.DeltaInformation;
import io.arenadata.dtm.query.execution.plugin.adqm.enrichment.dto.EnrichQueryRequest;
import io.vertx.core.Future;
import org.apache.calcite.rel.RelRoot;

import java.util.List;

/**
 * DML query converters
 */
public interface QueryGenerator {
    /**
     * Convert Query
     */
    Future<String> mutateQuery(RelRoot sqlNode,
                               List<DeltaInformation> deltaInformations,
                               CalciteContext calciteContext,
                               EnrichQueryRequest queryRequest);
}
