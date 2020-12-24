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
package io.arenadata.dtm.query.execution.plugin.adg.service.impl.check;

import io.arenadata.dtm.query.execution.plugin.adg.dto.AdgTables;
import io.arenadata.dtm.query.execution.plugin.adg.model.cartridge.schema.*;
import io.arenadata.dtm.query.execution.plugin.adg.service.AdgCartridgeClient;
import io.arenadata.dtm.query.execution.plugin.api.check.CheckContext;
import io.arenadata.dtm.query.execution.plugin.api.check.CheckException;
import io.arenadata.dtm.query.execution.plugin.api.ddl.DdlRequestContext;
import io.arenadata.dtm.query.execution.plugin.api.request.DdlRequest;
import io.arenadata.dtm.query.execution.plugin.api.service.check.CheckTableService;
import io.arenadata.dtm.query.execution.plugin.api.factory.CreateTableQueriesFactory;
import io.vertx.core.Future;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service("adgCheckTableService")
public class AdgCheckTableService implements CheckTableService {
    public static final String SPACE_INDEXES_ERROR_TEMPLATE = "\tSpace indexes are not equal expected [%s], got [%s].";
    private final AdgCartridgeClient adgCartridgeClient;
    private final CreateTableQueriesFactory<AdgTables<AdgSpace>> createTableQueriesFactory;

    @Autowired
    public AdgCheckTableService(AdgCartridgeClient adgCartridgeClient,
                                CreateTableQueriesFactory<AdgTables<AdgSpace>> createTableQueriesFactory) {
        this.adgCartridgeClient = adgCartridgeClient;
        this.createTableQueriesFactory = createTableQueriesFactory;
    }

    @Override
    public Future<Void> check(CheckContext context) {
        AdgTables<AdgSpace> tableEntities = createTableQueriesFactory
                .create(new DdlRequestContext(new DdlRequest(context.getRequest().getQueryRequest(),
                        context.getEntity())));
        Map<String, Space> expSpaces = Stream.of(
                tableEntities.getActual(),
                tableEntities.getHistory(),
                tableEntities.getStaging())
                .collect(Collectors.toMap(AdgSpace::getName, AdgSpace::getSpace));
        return check(expSpaces);
    }

    private Future<Void> check(Map<String, Space> expSpaces) {
        return Future.future(promise -> adgCartridgeClient.getSpaceDescriptions(expSpaces.keySet())
                .onSuccess(spaces -> {
                    String errors = expSpaces.entrySet().stream()
                            .map(entry -> compare(entry.getKey(), spaces.get(entry.getKey()), entry.getValue()))
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .collect(Collectors.joining("\n"));
                    if (errors.isEmpty()) {
                        promise.complete();
                    } else {
                        promise.fail(new CheckException("\n" + errors));
                    }
                })
                .onFailure(promise::fail));
    }

    private Optional<String> compare(String spaceName, Space space, Space expSpace) {
        List<String> errors = new ArrayList<>();

        if (!Objects.equals(getIndexNames(space), getIndexNames(expSpace))) {
            errors.add(String.format(SPACE_INDEXES_ERROR_TEMPLATE,
                    space.getIndexes().stream()
                            .map(SpaceIndex::getName)
                            .collect(Collectors.joining(", ")),
                    expSpace.getIndexes().stream()
                            .map(SpaceIndex::getName)
                            .collect(Collectors.joining(", "))));
        }

        expSpace.getFormat().forEach(expAttr -> {
            Optional<SpaceAttribute> optAttr = space.getFormat().stream()
                    .filter(attr -> attr.getName().equals(expAttr.getName()))
                    .findAny();
            if (optAttr.isPresent()) {
                SpaceAttributeTypes type = optAttr.get().getType();
                if (!Objects.equals(type, expAttr.getType())) {
                    errors.add(String.format("\tColumn`%s`:", expAttr.getName()));
                    errors.add(String.format(FIELD_ERROR_TEMPLATE, DATA_TYPE, expAttr.getType().getName(),
                            type.getName()));
                }
            } else {
                errors.add(String.format(COLUMN_NOT_EXIST_ERROR_TEMPLATE, expAttr.getName()));
            }
        });

        return errors.isEmpty()
                ? Optional.empty()
                : Optional.of(String.format("Table `%s`:\n%s", spaceName, String.join("\n", errors)));
    }

    private List<String> getIndexNames(Space space) {
        return space.getIndexes().stream()
                .map(SpaceIndex::getName)
                .collect(Collectors.toList());
    }
}
