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
package io.arenadata.dtm.query.execution.plugin.adqm.service;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import org.junit.jupiter.api.Test;

public class VertxTest {
    @Test
    public void testFailedFuture() {
        Promise<Void> ps = Promise.promise();

        Future<Void> fs = ps.future();
        ps.fail("String fail");
        fs.onComplete(ar -> System.out.println(ar.cause().getMessage()));

        Promise<Void> pe = Promise.promise();

        Future<Void> fe = pe.future();
        pe.fail(new RuntimeException("Exception fail"));
        fe.onComplete(ar -> System.out.println(ar.cause().getMessage()));
    }
}
