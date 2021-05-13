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
package io.arenadata.dtm.query.execution.plugin.adqm.service.mock;

import org.springframework.core.env.AbstractEnvironment;

public class MockEnvironment extends AbstractEnvironment {
    @Override
    public <T> T getProperty(String key, Class<T> targetType) {
        if (key.equals("core.env.name")) {
            return (T) "dev";
        }
        return super.getProperty(key, targetType);
    }
}
