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
package io.arenadata.dtm.query.execution.core.configuration.calcite;

import io.arenadata.dtm.common.configuration.core.DtmConfig;
import io.arenadata.dtm.common.service.DeltaService;
import io.arenadata.dtm.query.calcite.core.configuration.CalciteCoreConfiguration;
import io.arenadata.dtm.query.calcite.core.service.DefinitionService;
import io.arenadata.dtm.query.calcite.core.service.DeltaInformationExtractor;
import io.arenadata.dtm.query.calcite.core.service.DeltaQueryPreprocessor;
import io.arenadata.dtm.query.calcite.core.service.impl.DeltaInformationExtractorImpl;
import io.arenadata.dtm.query.calcite.core.service.impl.DeltaQueryPreprocessorImpl;
import org.apache.calcite.avatica.util.Casing;
import org.apache.calcite.avatica.util.Quoting;
import org.apache.calcite.config.Lex;
import org.apache.calcite.sql.SqlDialect;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.sql.parser.SqlParserImplFactory;
import org.apache.calcite.sql.validate.SqlConformanceEnum;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
public class CalciteConfiguration {

    @PostConstruct
    public void init() {
        System.setProperty("calcite.default.charset", "UTF-8");
        System.setProperty("calcite.default.nationalcharset", "UTF-8");
        System.setProperty("calcite.default.collation.name", "UTF-8$ru_RU");
    }

    @Bean("coreParser")
    public SqlParserImplFactory getSqlParserFactory() {
        return new CalciteCoreConfiguration().eddlParserImplFactory();
    }

    @Bean("coreParserConfig")
    public SqlParser.Config configEddlParser(@Qualifier("coreParser") SqlParserImplFactory factory) {
        return SqlParser.configBuilder()
                .setParserFactory(factory)
                .setConformance(SqlConformanceEnum.DEFAULT)
                .setLex(Lex.MYSQL)
                .setCaseSensitive(false)
                .setUnquotedCasing(Casing.TO_LOWER)
                .setQuotedCasing(Casing.TO_LOWER)
                .setQuoting(Quoting.DOUBLE_QUOTE)
                .build();
    }

    @Bean
    public DeltaInformationExtractor deltaInformationExtractor(DtmConfig dtmSettings) {
        return new DeltaInformationExtractorImpl(dtmSettings);
    }

    @Bean
    public DeltaQueryPreprocessor deltaQueryPreprocessor(
            @Qualifier("coreCalciteDefinitionService") DefinitionService<SqlNode> definitionService,
            DeltaService deltaService,
            DeltaInformationExtractor deltaInformationExtractor) {
        return new DeltaQueryPreprocessorImpl(definitionService,
                deltaService,
                deltaInformationExtractor);
    }

    @Bean("coreSqlDialect")
    public SqlDialect coreSqlDialect() {
        return new SqlDialect(SqlDialect.EMPTY_CONTEXT);
    }
}
