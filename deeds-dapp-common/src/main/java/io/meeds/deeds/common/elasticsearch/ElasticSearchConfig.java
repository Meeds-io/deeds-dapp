/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io
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
package io.meeds.deeds.common.elasticsearch;

import java.time.Duration;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.ClientConfiguration.ClientConfigurationBuilderWithRequiredEndpoint;
import org.springframework.data.elasticsearch.client.ClientConfiguration.MaybeSecureClientConfigurationBuilder;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.RefreshPolicy;
import org.springframework.data.elasticsearch.core.cluster.ClusterHealth;
import org.springframework.data.elasticsearch.core.convert.ElasticsearchConverter;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

import co.elastic.clients.elasticsearch.ElasticsearchClient;

@Configuration
@EnableElasticsearchRepositories(basePackages = {
  "io.meeds.deeds.common",
  "io.meeds.dapp",
})
public class ElasticSearchConfig extends ElasticsearchConfiguration {

  private static final Logger LOG = LoggerFactory.getLogger(ElasticSearchConfig.class);

  @Value("${meeds.elasticsearch.username:}")
  private String              esUsername;

  @Value("${meeds.elasticsearch.password:}")
  private String              esPassword;

  @Value("${meeds.elasticsearch.url:http://127.0.0.1:9200}")
  private String              esUrl;

  @Value("${meeds.elasticsearch.socketTimeout:20}")
  private int                 socketTimeout;

  @Value("${meeds.elasticsearch.connectTimeout:20}")
  private int                 connectionTimeout;

  @Value("${meeds.elasticsearch.connectionRetry:60}")
  private int                 connectionRetry;

  @Override
  public ClientConfiguration clientConfiguration() {
    String hostAndPort = esUrl.split("//")[1];
    ClientConfigurationBuilderWithRequiredEndpoint builder = ClientConfiguration.builder();
    MaybeSecureClientConfigurationBuilder connectionBuilder = builder.connectedTo(hostAndPort);
    if (esUrl.contains("https://")) {
      connectionBuilder.usingSsl();
    }
    if (StringUtils.isNotBlank(esPassword) && StringUtils.isNotBlank(esUsername)) {
      connectionBuilder.withBasicAuth(esUsername, esPassword);
    }
    connectionBuilder.withConnectTimeout(Duration.ofSeconds(connectionTimeout));
    connectionBuilder.withSocketTimeout(Duration.ofSeconds(socketTimeout));
    return connectionBuilder.build();
  }

  @Override
  public ElasticsearchOperations elasticsearchOperations(ElasticsearchConverter elasticsearchConverter,
                                                         ElasticsearchClient elasticsearchClient) {
    ElasticsearchTemplate elasticsearchTemplate = new ElasticsearchTemplate(elasticsearchClient, elasticsearchConverter);
    elasticsearchTemplate.setRefreshPolicy(RefreshPolicy.IMMEDIATE);
    tryConnection(elasticsearchTemplate);
    return elasticsearchTemplate;
  }

  private void tryConnection(ElasticsearchOperations elasticsearchOperations) {
    int i = connectionRetry;
    while (i-- > 0) {
      int tentative = connectionRetry - i;
      try {
        ClusterHealth elasticHealth = elasticsearchOperations.cluster().health();
        if (elasticHealth.isTimedOut()
            || elasticHealth.getActiveShardsPercent() < 1
            || elasticHealth.getActiveShards() == 0
            || !StringUtils.equalsIgnoreCase(elasticHealth.getStatus(), "green")) {
          throw new IllegalStateException(String.format("Elasticsearch Cluster Health Check TimedOut. Active shard = '%s', Percentage = '%s', Status = '%s'",
                                                        elasticHealth.getActiveShards(),
                                                        elasticHealth.getActiveShardsPercent(),
                                                        elasticHealth.getStatus()));
        } else {
          LOG.info("Connection established to ES after {}/{} tentatives", tentative, connectionRetry);
          i = 0;
        }
      } catch (Exception e) {
        if (i == 0) {
          LOG.warn("Connection failure to ES. tentative {}/{}.", tentative, connectionRetry);
        } else {
          LOG.info("Connection failure to ES. tentative {}/{}. Error Message: {}", tentative, connectionRetry, e.getMessage());
          try {
            Thread.sleep(1000);
          } catch (InterruptedException e1) {
            Thread.currentThread().interrupt();
          }
        }
      }
    }
  }

}