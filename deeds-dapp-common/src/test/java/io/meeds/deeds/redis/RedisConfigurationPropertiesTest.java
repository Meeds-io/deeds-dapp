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
package io.meeds.deeds.redis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(classes = RedisConfigurationProperties.class)
@EnableConfigurationProperties(value = RedisConfigurationProperties.class)
@TestPropertySource(
    properties = {
        "meeds.redis.channelName=" + RedisConfigurationPropertiesTest.CHANNEL_NAME_VALUE,
    }
)
class RedisConfigurationPropertiesTest {

  public static final String           CHANNEL_NAME_VALUE = "CHANNEL_NAME";

  @Autowired
  private RedisConfigurationProperties redisConfigurationProperties;

  @Test
  void testProperties() {
    assertNotNull(redisConfigurationProperties);
    assertEquals(CHANNEL_NAME_VALUE, redisConfigurationProperties.getChannelName());

    RedisConfigurationProperties properties = new RedisConfigurationProperties(CHANNEL_NAME_VALUE);
    assertEquals(properties, redisConfigurationProperties);
    assertEquals(properties.hashCode(), redisConfigurationProperties.hashCode());
    assertEquals(properties.toString(), redisConfigurationProperties.toString());
  }
}
