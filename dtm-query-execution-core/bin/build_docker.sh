#!/usr/bin/env bash
#
# Copyright © 2020 ProStore
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#


DTM_JAR_NAME=$(find target -iname 'dtm-query-execution-core-*.jar' | head -n 1)
if [ -z "${DTM_JAR_NAME}" ]; then
  echo 'Cannot find DTM JAR. Exiting'
  exit 1
fi

docker build --build-arg DTM_JAR="${DTM_JAR_NAME}" -t ci.arenadata.io/dtm-core:latest .
