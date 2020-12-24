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
package io.arenadata.dtm.query.execution.plugin.adg.exception;

import org.tarantool.TarantoolException;

public class DtmTarantoolException extends TarantoolException {

  public DtmTarantoolException(long code, String message, Throwable cause) {
    super(code, message, cause);
  }

  public DtmTarantoolException(long code, String message) {
    super(code, message);
  }

  public DtmTarantoolException(String message) {
    super(0, message);
  }
}
