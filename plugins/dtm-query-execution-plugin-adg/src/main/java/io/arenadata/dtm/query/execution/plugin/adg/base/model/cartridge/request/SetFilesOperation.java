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
package io.arenadata.dtm.query.execution.plugin.adg.base.model.cartridge.request;

import io.arenadata.dtm.query.execution.plugin.adg.base.model.cartridge.OperationFile;
import io.arenadata.dtm.query.execution.plugin.adg.base.model.cartridge.variable.FilesVariables;

import java.util.List;

/**
 * Set configuration files
 */
public class SetFilesOperation extends ReqOperation {

    public SetFilesOperation(List<OperationFile> files) {
        super("set_files", new FilesVariables(files),
                "mutation set_files($files: [ConfigSectionInput!]) { cluster { " +
                        "config(sections: $files) { filename content } } } ");
    }
}