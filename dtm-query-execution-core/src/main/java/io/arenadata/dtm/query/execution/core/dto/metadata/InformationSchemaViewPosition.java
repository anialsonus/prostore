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
package io.arenadata.dtm.query.execution.core.dto.metadata;

import io.arenadata.dtm.common.reader.InformationSchemaView;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Положение представления информационной схемы в запросе
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class InformationSchemaViewPosition {

  /**
   * Представление
   */
  private InformationSchemaView view;

  /**
   * Начало
   */
  private int start;

  /**
   * Конец
   */
  private int end;
}
