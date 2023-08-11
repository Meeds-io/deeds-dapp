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
package io.meeds.dapp.service;

import java.net.URL;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.meeds.deeds.common.constant.DeedCard;
import io.meeds.deeds.common.constant.DeedCity;
import io.meeds.deeds.common.elasticsearch.model.DeedMetadata;
import io.meeds.deeds.common.service.BlockchainService;
import io.meeds.deeds.common.storage.DeedMetadataRepository;
import io.meeds.deeds.constant.ObjectNotFoundException;

import jakarta.annotation.PostConstruct;

@Component
public class DeedMetadataService {

  private static final String       DAPP_IMAGE_SERVER_BASE = "${DAPP_SERVER_BASE}";

  private static final ObjectMapper OBJECT_MAPPER          = new ObjectMapper();

  static {
    // Workaround when Jackson is defined in shared library with different
    // version and without artifact jackson-datatype-jsr310
    OBJECT_MAPPER.registerModule(new JavaTimeModule());
  }

  private static final Logger       LOG = LoggerFactory.getLogger(DeedMetadataService.class);

  @Autowired
  private DeedMetadataRepository    deedMetadataRepository;

  @Autowired
  private BlockchainService         blockchainService;

  @Value("${meeds.deed.metadatas.path:metadatas.json}")
  private String                    metadatasFilePath;

  @Value("${meeds.deed.contract.metadatas.path:deedCollection.json}")
  private String                    deedCollectionMetadataFilePath;

  @Value("${meeds.deed.metadata.serverBase:}")
  private String                    imageServerBase;

  private Map<String, DeedMetadata> deedMetadatas;

  private DeedMetadata              contractMetadata;

  @PostConstruct
  public void init() {
    try {
      URL deedMetadatasResource = getClass().getClassLoader().getResource(metadatasFilePath);
      deedMetadatas = OBJECT_MAPPER.readerForMapOf(DeedMetadata.class).readValue(deedMetadatasResource);
      deedMetadatas.values()
                   .forEach(deedMetadata -> deedMetadata.setImageUrl(deedMetadata.getImageUrl()
                                                                                 .replace(DAPP_IMAGE_SERVER_BASE,
                                                                                          imageServerBase)));

      URL contractMetadataResource = getClass().getClassLoader().getResource(deedCollectionMetadataFilePath);
      contractMetadata = OBJECT_MAPPER.readerFor(DeedMetadata.class).readValue(contractMetadataResource);
      contractMetadata.setImageUrl(contractMetadata.getImageUrl()
                                                   .replace(DAPP_IMAGE_SERVER_BASE,
                                                            imageServerBase));
    } catch (Exception e) {
      LOG.error("Error reading Default NFT mappings", e);
    }
  }

  /**
   * @param  nftId DEED NFT identifier
   * @return       DEED NFT metadatas of type {@link DeedMetadata}. If the
   *               Metadata entry doesn't exist in storage, it will build a new
   *               one base on City and Card Type of the NFT.
   */
  @Cacheable(cacheNames = "deedMetadata")
  public DeedMetadata getDeedMetadata(Long nftId) {
    return deedMetadataRepository.findById(nftId).orElseGet(() -> this.buildDeedMetadata(nftId));
  }

  /**
   * @param  cityIndex     City index of selected Card
   * @param  cardTypeIndex Card Type index of selected Card
   * @return               DEED NFT metadatas from metadata pattern.
   */
  public DeedMetadata getDeedMetadataOfCard(int cityIndex, int cardTypeIndex) {
    if (cityIndex >= DeedCity.values().length || cardTypeIndex >= DeedCard.values().length) {
      return null;
    }
    DeedCity deedCity = DeedCity.values()[cityIndex];
    DeedCard cardType = DeedCard.values()[cardTypeIndex];

    String key = deedCity.name() + "-" + cardType.name();
    return deedMetadatas.get(key).clone();
  }

  /**
   * @return {@link DeedMetadata} representing the contract Metadata used by
   *         OpenSea
   */
  public DeedMetadata getContractMetadata() {
    return contractMetadata.clone();
  }

  private DeedMetadata buildDeedMetadata(long nftId) {
    try {
      short cityIndex = blockchainService.getDeedCityIndex(nftId);
      short cardTypeIndex = blockchainService.getDeedCardType(nftId);

      DeedCity deedCity = DeedCity.values()[cityIndex];
      DeedCard cardType = DeedCard.values()[cardTypeIndex];

      String key = deedCity.name() + "-" + cardType.name();
      DeedMetadata deedMetadata = deedMetadatas.get(key).clone();
      deedMetadata.setNftId(nftId);
      return deedMetadata;
    } catch (ObjectNotFoundException e) {
      return null;
    }
  }

}
