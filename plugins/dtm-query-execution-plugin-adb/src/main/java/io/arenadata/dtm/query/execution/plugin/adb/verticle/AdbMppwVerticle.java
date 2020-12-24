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
package io.arenadata.dtm.query.execution.plugin.adb.verticle;

import io.arenadata.dtm.query.execution.plugin.adb.configuration.properties.MppwProperties;
import io.arenadata.dtm.query.execution.plugin.adb.service.impl.mppw.dto.MppwKafkaRequestContext;
import io.arenadata.dtm.query.execution.plugin.adb.service.impl.mppw.handler.AdbMppwHandler;
import io.arenadata.dtm.query.execution.plugin.adb.verticle.worker.AdbMppwWorker;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class AdbMppwVerticle extends AbstractVerticle {

    private final Map<String, MppwKafkaRequestContext> requestMap = new ConcurrentHashMap<>();
    private final Map<String, Future> resultMap = new ConcurrentHashMap<>();
    private final MppwProperties mppwProperties;
    private final AdbMppwHandler mppwTransferDataHandler;

    @Autowired
    public AdbMppwVerticle(MppwProperties mppwProperties,
                           @Qualifier("adbMppwTransferDataHandler") AdbMppwHandler mppwTransferDataHandler) {
        this.mppwProperties = mppwProperties;
        this.mppwTransferDataHandler = mppwTransferDataHandler;
    }

    @Override
    public void start() throws Exception {
        val options = new DeploymentOptions()
                .setWorkerPoolSize(this.mppwProperties.getPoolSize())
                .setWorker(true);
        for (int i = 0; i < this.mppwProperties.getPoolSize(); i++) {
            vertx.deployVerticle(new AdbMppwWorker(this.requestMap, this.resultMap,
                    this.mppwTransferDataHandler), options, ar -> {
                if (ar.succeeded()) {
                    log.debug("Mppw workers deployed successfully");
                } else {
                    log.error("Error deploying mppw workers", ar.cause());
                }
            });
        }
    }
}
