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
package io.meeds.deeds.storage;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import io.meeds.deeds.elasticsearch.model.DeedHub;

public interface HubRepository extends ElasticsearchRepository<DeedHub, String> {

  boolean existsByNftIdAndAddressNotAndEnabledIsTrue(long nftId, String address);

  Optional<DeedHub> findByNftIdAndEnabledIsTrue(long nftId);

  Page<DeedHub> findByEnabledIsTrue(Pageable pageable);

  Optional<DeedHub> findByAddressAndEnabledIsTrue(String address);

}
