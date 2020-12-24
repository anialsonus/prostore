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
package io.arenadata.dtm.query.execution.plugin.adqm.configuration.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("adqm.ddl")
public class DdlProperties {
    private String cluster;
    private Integer ttlSec;
    private String archiveDisk;

    public String getCluster() {
        return cluster;
    }

    public void setCluster(String cluster) {
        this.cluster = cluster;
    }

    public Integer getTtlSec() {
        return ttlSec;
    }

    public void setTtlSec(Integer ttlSec) {
        this.ttlSec = ttlSec;
    }

    public String getArchiveDisk() {
        return archiveDisk;
    }

    public void setArchiveDisk(String archiveDisk) {
        this.archiveDisk = archiveDisk;
    }
}
