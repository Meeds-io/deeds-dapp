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
package io.meeds.dapp.storage;

import java.util.List;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import io.meeds.dapp.elasticsearch.model.DeedTenantOffer;
import io.meeds.deeds.common.constant.TransactionStatus;

public interface OfferRepository extends ElasticsearchRepository<DeedTenantOffer, String> {

  List<DeedTenantOffer> findByOwnerNotAndNftIdAndEnabledTrue(String owner, long nftId);

  List<DeedTenantOffer> findByNftId(long nftId);

  List<DeedTenantOffer> findByOfferTransactionStatusInOrderByCreatedDateAsc(List<TransactionStatus> transactionStatus);

  List<DeedTenantOffer> findByOfferId(long offerId);

  List<DeedTenantOffer> findByOfferIdAndParentIdIsNull(long offerId);

  void deleteByNftId(long nftId);

  void deleteByParentId(String id);

  DeedTenantOffer findByOfferTransactionHash(String lowerCase);

}
