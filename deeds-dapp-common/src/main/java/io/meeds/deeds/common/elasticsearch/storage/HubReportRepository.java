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

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.data.util.Streamable;

import io.meeds.deeds.common.elasticsearch.model.HubReportEntity;
import io.meeds.wom.api.constant.HubReportStatusType;
import io.meeds.wom.api.model.HubReport;

public interface HubReportRepository extends ElasticsearchRepository<HubReportEntity, Long> {

  Page<HubReportEntity> findByHubAddress(String hubAddress, Pageable pageable);

  Page<HubReportEntity> findByRewardId(String rewardId, Pageable pageable);

  Page<HubReportEntity> findByRewardIdAndHubAddress(String rewardId, String hubAddress, Pageable pageable);

  Streamable<HubReportEntity> findBySentDateBetweenAndStatusNotIn(Instant from,
                                                                  Instant to,
                                                                  List<HubReportStatusType> statuses);

  Page<HubReportEntity> findByHubAddressAndStatusInOrderByFromDateDesc(String address,
                                                                       List<HubReportStatusType> statuses,
                                                                       Pageable pageable);

  Page<HubReportEntity> findByHubAddressAndReportIdNotAndStatusInOrderByFromDateDesc(String hubAddress,
                                                                                     long reportId,
                                                                                     List<HubReportStatusType> statuses,
                                                                                     Pageable pageable);

  Page<HubReportEntity> findByHubAddressAndSentDateBeforeAndStatusInOrderByFromDateDesc(String address,
                                                                                        Instant beforeDate,
                                                                                        List<HubReportStatusType> statuses,
                                                                                        Pageable pageable);

  Page<HubReportEntity> findByHubAddressAndSentDateBeforeAndReportIdNotAndStatusInOrderByFromDateDesc(String hubAddress,
                                                                                                      Instant beforeDate,
                                                                                                      long reportId,
                                                                                                      List<HubReportStatusType> statuses,
                                                                                                      Pageable pageable);

  Optional<HubReportEntity> findByRewardIdAndHubAddressAndStatusNotIn(long rewardId,
                                                                      String hubAddress,
                                                                      List<HubReportStatusType> statuses);

  List<HubReport> findByRewardId(long byRewardId);

}
