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
package io.arenadata.dtm.query.execution.core.factory;

import io.arenadata.dtm.common.configuration.core.DtmConfig;
import io.vertx.core.json.jackson.DatabindCodec;
import lombok.SneakyThrows;

public abstract class AbstractMetricsEventFactory<IN> implements MetricsEventFactory<IN> {

    private final Class<IN> inClass;
    private final DtmConfig dtmSettings;

    public AbstractMetricsEventFactory(Class<IN> inClass, DtmConfig dtmSettings) {
        this.inClass = inClass;
        this.dtmSettings = dtmSettings;
    }

    @SneakyThrows
    @Override
    public IN create(String eventData) {
        return DatabindCodec.mapper().readValue(eventData, inClass);
    }
}
