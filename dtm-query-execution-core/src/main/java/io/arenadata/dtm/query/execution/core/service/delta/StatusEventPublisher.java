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
package io.arenadata.dtm.query.execution.core.service.delta;

import io.arenadata.dtm.common.eventbus.DataHeader;
import io.arenadata.dtm.common.eventbus.DataTopic;
import io.arenadata.dtm.common.status.StatusEventCode;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.json.jackson.DatabindCodec;
import lombok.SneakyThrows;
import lombok.val;

public interface StatusEventPublisher {

    @SneakyThrows
    default void publishStatus(StatusEventCode eventCode, String datamart, Object eventData) {
        val message = DatabindCodec.mapper().writeValueAsString(eventData);
        val options = new DeliveryOptions();
        options.addHeader(DataHeader.DATAMART.getValue(), datamart);
        options.addHeader(DataHeader.STATUS_EVENT_CODE.getValue(), eventCode.name());
        getVertx().eventBus()
            .send(DataTopic.STATUS_EVENT_PUBLISH.getValue(), message, options);
    }

    Vertx getVertx();
}
