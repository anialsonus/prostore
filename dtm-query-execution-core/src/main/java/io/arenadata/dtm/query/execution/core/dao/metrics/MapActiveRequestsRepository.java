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
package io.arenadata.dtm.query.execution.core.dao.metrics;

import io.arenadata.dtm.common.metrics.RequestMetrics;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Repository("mapActiveRequestsRepository")
public class MapActiveRequestsRepository implements ActiveRequestsRepository<RequestMetrics> {

    private final Map<UUID, RequestMetrics> requestsMap = new ConcurrentHashMap<>();

    @Override
    public void add(RequestMetrics request) {
        requestsMap.put(request.getRequestId(), request);
    }

    @Override
    public void remove(RequestMetrics request) {
        requestsMap.remove(request.getRequestId());
    }

    @Override
    public RequestMetrics get(UUID requestId) {
        return requestsMap.get(requestId);
    }

    @Override
    public List<RequestMetrics> getList() {
        return new ArrayList<>(requestsMap.values());
    }

    @Override
    public void deleteAll() {
        requestsMap.clear();
    }
}
