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
package io.arenadata.dtm.common.model.ddl;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class EntityFieldUtils {

	private static final List<String> pkSystemField = Arrays.asList("sys_from");

	public static List<EntityField> getPrimaryKeyList(final List<EntityField> fields) {
		return fields.stream()
				.filter(f -> f.getPrimaryOrder() != null)
				.sorted(Comparator.comparing(EntityField::getPrimaryOrder))
				.collect(toList());
	}

	public static List<EntityField> getPrimaryKeyListWithSysFields(final List<EntityField> fields) {
		return fields.stream()
				.filter(f -> f.getPrimaryOrder() != null || isSystemFieldForPk(f.getName()))
				.sorted(Comparator.comparing(EntityField::getPrimaryOrder))
				.collect(toList());
	}

	public static List<EntityField> getShardingKeyList(final List<EntityField> fields) {
		return fields.stream()
				.filter(f -> f.getShardingOrder() != null)
				.sorted(Comparator.comparing(EntityField::getShardingOrder))
				.collect(toList());
	}

	private static boolean isSystemFieldForPk(final String fieldName) {
		return pkSystemField.contains(fieldName);
	}

}
