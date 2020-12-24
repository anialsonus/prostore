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
package io.arenadata.dtm.kafka.core.repository;

import io.arenadata.dtm.kafka.core.configuration.kafka.KafkaZookeeperProperties;
import io.arenadata.dtm.kafka.core.service.kafka.KafkaZookeeperConnectionProvider;
import io.arenadata.dtm.kafka.core.service.kafka.KafkaZookeeperConnectionProviderImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
@Component("mapZkKafkaProviderRepository")
public class ZookeeperKafkaProviderRepositoryImpl implements ZookeeperKafkaProviderRepository {
    private final KafkaZookeeperProperties defaultProperties;
    private final Map<String, KafkaZookeeperConnectionProvider> zkConnProviderMap = new ConcurrentHashMap<>();

    @Override
    public KafkaZookeeperConnectionProvider getOrCreate(String connectionString) {
        final KafkaZookeeperConnectionProvider zkConnProvider = zkConnProviderMap.get(connectionString);
        final KafkaZookeeperProperties zookeeperProperties = new KafkaZookeeperProperties();
        zookeeperProperties.setConnectionString(connectionString);
        zookeeperProperties.setChroot(defaultProperties.getChroot());
        if (zkConnProvider == null) {
            zkConnProviderMap.put(zookeeperProperties.getConnectionString(),
                new KafkaZookeeperConnectionProviderImpl(zookeeperProperties));
        }
        return zkConnProviderMap.get(zookeeperProperties.getConnectionString());
    }
}
