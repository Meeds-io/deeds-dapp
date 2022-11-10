/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2020 - 2022 Meeds Association contact@meeds.io
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package io.meeds.deeds.elasticsearch;

import java.time.Duration;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.ClientConfiguration.ClientConfigurationBuilderWithRequiredEndpoint;
import org.springframework.data.elasticsearch.client.ClientConfiguration.MaybeSecureClientConfigurationBuilder;
import org.springframework.data.elasticsearch.client.RestClients;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

import io.meeds.deeds.model.DeedMetadata;
import io.meeds.deeds.model.DeedTenant;
import io.meeds.deeds.model.DeedTenantEvent;

@Configuration
@EnableElasticsearchRepositories(basePackages = {
    "io.meeds",
})
public class ElasticSearchConfig {

  @Value("${meeds.elasticsearch.username:}")
  private String  esUsername;

  @Value("${meeds.elasticsearch.password:}")
  private String  esPassword;

  @Value("${meeds.elasticsearch.url:http://127.0.0.1:9200}")
  private String  esUrl;

  @Value("${meeds.elasticsearch.connectTimeout:10}")
  private int     connectionTimeout;

  @Value("${meeds.elasticsearch.autoCreateIndex:true}")
  private boolean createDeedIndexes;

  @Bean
  public RestHighLevelClient client() {
    ClientConfigurationBuilderWithRequiredEndpoint builder = ClientConfiguration.builder();
    String hostAndPort = esUrl.split("//")[1];
    MaybeSecureClientConfigurationBuilder connectionBuilder = builder.connectedTo(hostAndPort);
    if (esUrl.contains("https://")) {
      connectionBuilder.usingSsl();
    }
    if (StringUtils.isNotBlank(esPassword) && StringUtils.isNotBlank(esUsername)) {
      connectionBuilder.withBasicAuth(esUsername, esPassword);
    }
    connectionBuilder.withConnectTimeout(Duration.ofSeconds(connectionTimeout));
    ClientConfiguration clientConfiguration = connectionBuilder.build();
    return RestClients.create(clientConfiguration).rest();// NOSONAR
  }

  @Bean
  public ElasticsearchOperations elasticsearchTemplate(RestHighLevelClient client) {
    ElasticsearchRestTemplate elasticsearchTemplate = new ElasticsearchRestTemplate(client);
    createIndex(elasticsearchTemplate, DeedTenant.class);
    createIndex(elasticsearchTemplate, DeedMetadata.class);
    createIndex(elasticsearchTemplate, DeedTenantEvent.class);
    return elasticsearchTemplate;
  }

  private void createIndex(ElasticsearchRestTemplate elasticsearchTemplate, Class<?> esDocumentModelClass) {
    IndexOperations indexOperations = elasticsearchTemplate.indexOps(esDocumentModelClass);
    boolean indexExists = indexOperations.exists();
    if (!indexExists) {
      if (createDeedIndexes) {
        indexOperations.createWithMapping();
      } else {
        // Prevent Deed Tenants from creating indexes in their respective ES
        throw new IllegalStateException("Make sure the `meeds.elasticsearch.*` configuration properties are set properly");
      }
    }
  }

}
