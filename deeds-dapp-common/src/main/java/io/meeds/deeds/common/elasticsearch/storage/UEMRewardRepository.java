/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io
 *
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
package io.meeds.deeds.common.elasticsearch.storage;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import io.meeds.deeds.common.elasticsearch.model.UEMRewardEntity;
import io.meeds.wom.api.constant.UEMRewardStatusType;

public interface UEMRewardRepository extends ElasticsearchRepository<UEMRewardEntity, String> {

  Page<UEMRewardEntity> findByHubAddresses(String hubAddress, Pageable pageable);

  Optional<UEMRewardEntity> findByFromDateIsAndToDateIs(Instant from, Instant to);

  Stream<UEMRewardEntity> findByStatusIn(List<UEMRewardStatusType> notSentStatuses);

}
