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
package io.meeds.deeds.common.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.meeds.deeds.common.elasticsearch.model.DeedSetting;
import io.meeds.deeds.common.storage.SettingRepository;

@Component
public class SettingService {

  @Autowired
  private SettingRepository settingRepository;

  public void save(String key, String value) {
    settingRepository.save(new DeedSetting(key, value));
  }

  public String get(String key) {
    DeedSetting setting = settingRepository.findById(key).orElse(null);
    return setting == null ? null : setting.getValue();
  }

  public String remove(String key) {
    DeedSetting setting = settingRepository.findById(key).orElse(null);
    if (setting == null) {
      return null;
    } else {
      return setting.getValue();
    }
  }

}
