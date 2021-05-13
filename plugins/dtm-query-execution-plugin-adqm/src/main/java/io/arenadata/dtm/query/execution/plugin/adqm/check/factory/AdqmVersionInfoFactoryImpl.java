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
package io.arenadata.dtm.query.execution.plugin.adqm.check.factory;

import io.arenadata.dtm.common.version.VersionInfo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.arenadata.dtm.query.execution.plugin.adqm.check.factory.AdqmVersionQueriesFactoryImpl.COMPONENT_NAME_COLUMN;
import static io.arenadata.dtm.query.execution.plugin.adqm.check.factory.AdqmVersionQueriesFactoryImpl.VERSION_COLUMN;

@Service
public class AdqmVersionInfoFactoryImpl implements AdqmVersionInfoFactory {

    @Override
    public List<VersionInfo> create(List<Map<String, Object>> rows) {
        return rows.stream()
                .map(row -> new VersionInfo(row.get(COMPONENT_NAME_COLUMN).toString(),
                        row.get(VERSION_COLUMN).toString()))
                .collect(Collectors.toList());
    }
}
