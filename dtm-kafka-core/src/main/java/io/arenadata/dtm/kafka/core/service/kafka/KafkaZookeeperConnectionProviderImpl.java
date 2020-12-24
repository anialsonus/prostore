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
package io.arenadata.dtm.kafka.core.service.kafka;

import io.arenadata.dtm.common.dto.KafkaBrokerInfo;
import io.arenadata.dtm.kafka.core.configuration.kafka.KafkaZookeeperProperties;
import io.vertx.core.json.Json;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.zookeeper.ZooKeeper;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import static org.apache.zookeeper.Watcher.Event.KeeperState.SyncConnected;

@Slf4j
public class KafkaZookeeperConnectionProviderImpl implements KafkaZookeeperConnectionProvider {

    private static final String BROKERS_IDS_PATH = "/brokers/ids";
    private final KafkaZookeeperProperties properties;
    private ZooKeeper connection;
    private boolean synConnected;

    public KafkaZookeeperConnectionProviderImpl(KafkaZookeeperProperties properties) {
        this.properties = properties;
    }

    @Override
    public ZooKeeper getOrConnect() {
        return synConnected && connection.getState().isConnected() ? connection : connect(this.properties.getConnectionString());
    }

    @SneakyThrows
    private synchronized ZooKeeper connect(String connectionString) {
        if (connection != null) {
            if (connection.getState().isConnected()) {
                return connection;
            } else {
                connection.close();
            }
        }
        val connectionLatch = new CountDownLatch(1);
        connection = new ZooKeeper(connectionString,
                properties.getSessionTimeoutMs(),
                we -> {
                    log.debug("ZooKeeper connection: [{}]", we);
                    if (we.getState() == SyncConnected) {
                        synConnected = true;
                        connectionLatch.countDown();
                    } else {
                        synConnected = false;
                    }
                });
        connectionLatch.await(properties.getConnectionTimeoutMs(), TimeUnit.MILLISECONDS);
        if (!synConnected) {
            val errMsg = String.format("Zookeeper connection timed out: [%d] ms", properties.getConnectionTimeoutMs());
            log.error(errMsg);
            throw new TimeoutException(errMsg);
        }
        return connection;
    }

    @Override
    public List<KafkaBrokerInfo> getKafkaBrokers() {
        if (connection == null || connection.getState() == ZooKeeper.States.CLOSED) {
            connection = this.getOrConnect();
        }
        try {
            final List<String> brokersIds = connection.getChildren(getBrokersIdsPath(), false);
            return brokersIds.stream()
                    .map(id -> getKafkaBrokerInfo(connection, id))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Error getting kafka broker info list", e);
        }
    }

    private KafkaBrokerInfo getKafkaBrokerInfo(ZooKeeper zk, String id) {
        try {
            return Json.decodeValue(new String(zk.getData(getBrokersIdsPath() + "/" + id, false, null)),
                    KafkaBrokerInfo.class);
        } catch (Exception e) {
            throw new RuntimeException("Error decode response from zk for getting kafka brokers", e);
        }
    }

    private String getBrokersIdsPath() {
        return properties.getChroot() +  BROKERS_IDS_PATH;
    }

    @Override
    @SneakyThrows
    public void close() {
        if (synConnected) {
            connection.close();
            synConnected = false;
        }
    }
}
