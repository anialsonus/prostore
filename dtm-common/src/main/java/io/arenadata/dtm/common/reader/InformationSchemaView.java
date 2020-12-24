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
package io.arenadata.dtm.common.reader;

import java.util.Arrays;

/**
 * Представление из информационной схемы
 */
public enum InformationSchemaView {
  SCHEMATA("logic_schema_datamarts"),
  TABLES("logic_schema_entities"),
  COLUMNS("logic_schema_columns"),
  TABLE_CONSTRAINTS("logic_schema_entity_constraints"),
  KEY_COLUMN_USAGE("logic_schema_key_column_usage");

  public static final String SCHEMA_NAME = "INFORMATION_SCHEMA";
  public static final String DTM_SCHEMA_NAME = "DTM";
  private static final String quiet = "\"";
  private final String realName;

  InformationSchemaView(String realName) {
    this.realName = realName;
  }

  public String getRealName() {
    return realName;
  }

  public String getFullName() {
    return getFullName(false, false);
  }

  public static InformationSchemaView findByFullName(String fullName) {
    return Arrays.stream(InformationSchemaView.values())
      .filter(view -> view.equalsByFullName(fullName))
      .findAny().orElse(null);
  }

  private boolean equalsByFullName(String fullName) {
    boolean[] boolValues = {false, true};
    for (boolean schemaQuotes : boolValues) {
      for (boolean nameQuotes : boolValues) {
        if (getFullName(schemaQuotes, nameQuotes).equalsIgnoreCase(fullName)) {
          return true;
        }
      }
    }
    return false;
  }

  private static String addQuotes(String name) {
    return quiet + name + quiet;
  }

  private String getFullName(boolean schemaQuotes, boolean nameQuotes) {
    return (schemaQuotes ? addQuotes(SCHEMA_NAME) : SCHEMA_NAME) + "." +
      (nameQuotes ? addQuotes(name()) : name());
  }
}
