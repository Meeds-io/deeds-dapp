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
package io.meeds.deeds.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;

import io.meeds.deeds.constant.DisplayType;
import io.meeds.deeds.constant.ObjectNotFoundException;
import io.meeds.deeds.model.DeedMetadata;
import io.meeds.deeds.model.DeedMetadataAttribute;
import io.meeds.deeds.storage.DeedMetadataRepository;

@SpringBootTest(
    classes = {
        DeedMetadataService.class,
    }
)
@TestPropertySource(
    properties = {
        "meeds.deed.metadatas.path=metadatas-test.json",
        "meeds.deed.contract.metadatas.path=deedCollection-test.json",
        "meeds.deed.metadata.serverBase=https://wom.meeds.io/dapp",
    }
)
class DeedMetadataServiceTest {

  @MockBean
  private BlockchainService      blockchainService;

  @MockBean
  private DeedMetadataRepository deedMetadataRepository;

  @Autowired
  private DeedMetadataService    deedMetadataService;

  @Test
  void testInit() throws Exception {
    assertNotNull(deedMetadataService);
    assertNotNull(deedMetadataService.getContractMetadata());
    assertNotNull(deedMetadataService.getDeedMetadataOfCard(0, 0));
  }

  @Test
  void testGetContractMetadata() throws Exception {
    DeedMetadata contractMetadata = deedMetadataService.getContractMetadata();
    assertNotNull(contractMetadata);
    assertEquals("Meeds DAO", contractMetadata.getName());
    assertEquals("Deed Collection", contractMetadata.getDescription());
    assertEquals("https://meeds.io/investors", contractMetadata.getExternalLink());
    assertEquals("https://wom.meeds.io/dapp/static/images/nft/deed-collection-avatar.png", contractMetadata.getImageUrl());
  }

  @Test
  void testGetDeedMetadataOfCard0() throws Exception {
    assertNotNull(deedMetadataService.getDeedMetadataOfCard(0, 0));
    assertNotNull(deedMetadataService.getDeedMetadataOfCard(0, 1));
    assertNotNull(deedMetadataService.getDeedMetadataOfCard(0, 2));
    assertNotNull(deedMetadataService.getDeedMetadataOfCard(0, 3));
    assertNull(deedMetadataService.getDeedMetadataOfCard(0, 4));
  }

  @Test
  void testGetDeedMetadataOfCard1() throws Exception {
    assertNotNull(deedMetadataService.getDeedMetadataOfCard(1, 0));
    assertNotNull(deedMetadataService.getDeedMetadataOfCard(1, 1));
    assertNotNull(deedMetadataService.getDeedMetadataOfCard(1, 2));
    assertNotNull(deedMetadataService.getDeedMetadataOfCard(1, 3));
    assertNull(deedMetadataService.getDeedMetadataOfCard(1, 4));
  }

  @Test
  void testGetDeedMetadataOfCard2() throws Exception {
    assertNotNull(deedMetadataService.getDeedMetadataOfCard(2, 0));
    assertNotNull(deedMetadataService.getDeedMetadataOfCard(2, 1));
    assertNotNull(deedMetadataService.getDeedMetadataOfCard(2, 2));
    assertNotNull(deedMetadataService.getDeedMetadataOfCard(2, 3));
    assertNull(deedMetadataService.getDeedMetadataOfCard(2, 4));
  }

  @Test
  void testGetDeedMetadataOfCard3() throws Exception {
    assertNotNull(deedMetadataService.getDeedMetadataOfCard(3, 0));
    assertNotNull(deedMetadataService.getDeedMetadataOfCard(3, 1));
    assertNotNull(deedMetadataService.getDeedMetadataOfCard(3, 2));
    assertNotNull(deedMetadataService.getDeedMetadataOfCard(3, 3));
    assertNull(deedMetadataService.getDeedMetadataOfCard(3, 4));
  }

  @Test
  void testGetDeedMetadataOfCard4() throws Exception {
    assertNotNull(deedMetadataService.getDeedMetadataOfCard(4, 0));
    assertNotNull(deedMetadataService.getDeedMetadataOfCard(4, 1));
    assertNotNull(deedMetadataService.getDeedMetadataOfCard(4, 2));
    assertNotNull(deedMetadataService.getDeedMetadataOfCard(4, 3));
    assertNull(deedMetadataService.getDeedMetadataOfCard(4, 4));
  }

  @Test
  void testGetDeedMetadataOfCard5() throws Exception {
    assertNotNull(deedMetadataService.getDeedMetadataOfCard(5, 0));
    assertNotNull(deedMetadataService.getDeedMetadataOfCard(5, 1));
    assertNotNull(deedMetadataService.getDeedMetadataOfCard(5, 2));
    assertNotNull(deedMetadataService.getDeedMetadataOfCard(5, 3));
    assertNull(deedMetadataService.getDeedMetadataOfCard(5, 4));
  }

  @Test
  void testGetDeedMetadataOfCard6() throws Exception {
    assertNotNull(deedMetadataService.getDeedMetadataOfCard(6, 0));
    assertNotNull(deedMetadataService.getDeedMetadataOfCard(6, 1));
    assertNotNull(deedMetadataService.getDeedMetadataOfCard(6, 2));
    assertNotNull(deedMetadataService.getDeedMetadataOfCard(6, 3));
    assertNull(deedMetadataService.getDeedMetadataOfCard(6, 4));
    assertNull(deedMetadataService.getDeedMetadataOfCard(7, 0));
  }

  @Test
  void testGetDeedMetadataOfCardContent() throws Exception {
    DeedMetadata metadata = deedMetadataService.getDeedMetadataOfCard(0, 0);
    assertEquals("Tanit - Common", metadata.getName());
    assertEquals("Deed NFT : Tanit - Common", metadata.getDescription());
    assertEquals("#ffffff", metadata.getBackgroundColor());
    assertEquals("https://wom.meeds.io/dapp/static/images/nft/tanit-common.png", metadata.getImageUrl());
    assertNotNull(metadata.getAttributes());
    assertEquals(2, metadata.getAttributes().size());

    DeedMetadataAttribute mintingPower = metadata.getAttributes()
                                                 .stream()
                                                 .filter(attr -> StringUtils.equals("Minting Power", attr.getTraitType()))
                                                 .findFirst()
                                                 .orElse(null);
    assertNotNull(mintingPower);
    assertEquals(DisplayType.BOOST_NUMBER, mintingPower.getDisplayType());
    assertEquals(1, mintingPower.getValue());
    assertEquals(2, mintingPower.getMaxValue());
  }

  @Test
  void testGetDeedMetadataFromDB() throws Exception {
    long nftId = 1l;

    DeedMetadata metadata = new DeedMetadata();
    metadata.setNftId(nftId);

    when(deedMetadataRepository.findById(nftId)).thenReturn(Optional.of(metadata));

    DeedMetadata deedMetadata = deedMetadataService.getDeedMetadata(nftId);
    assertEquals(metadata, deedMetadata);
  }

  @Test
  void testGetDeedMetadataFromBlockchain() throws Exception {
    long nftId = 2l;
    short cityIndex = 1;
    short cardType = 2;

    when(blockchainService.getDeedCityIndex(anyLong())).then(invocation -> {
      Long argument = invocation.getArgument(0, Long.class);
      if (argument == nftId) {
        return cityIndex;
      } else {
        throw new ObjectNotFoundException();
      }
    });

    when(blockchainService.getDeedCardType(anyLong())).then(invocation -> {
      Long argument = invocation.getArgument(0, Long.class);
      if (argument == nftId) {
        return cardType;
      } else {
        throw new ObjectNotFoundException();
      }
    });

    assertNull(deedMetadataService.getDeedMetadata(1l));

    DeedMetadata metadata = deedMetadataService.getDeedMetadata(nftId);
    assertNotNull(metadata);
    assertEquals(nftId, metadata.getNftId());

    assertEquals("Reshef - Rare", metadata.getName());
    assertEquals("Deed NFT : Reshef - Rare", metadata.getDescription());
    assertEquals("#ffffff", metadata.getBackgroundColor());
    assertEquals("https://wom.meeds.io/dapp/static/images/nft/reshef-rare.png", metadata.getImageUrl());
    assertNotNull(metadata.getAttributes());
    assertEquals(2, metadata.getAttributes().size());

    DeedMetadataAttribute mintingPower = metadata.getAttributes()
                                                 .stream()
                                                 .filter(attr -> StringUtils.equals("Minting Power", attr.getTraitType()))
                                                 .findFirst()
                                                 .orElse(null);
    assertNotNull(mintingPower);
    assertEquals(DisplayType.BOOST_NUMBER, mintingPower.getDisplayType());
    assertEquals(1.3, mintingPower.getValue());
    assertEquals(2, mintingPower.getMaxValue());

    DeedMetadataAttribute maxUsers = metadata.getAttributes()
                                             .stream()
                                             .filter(attr -> StringUtils.equals("Max users", attr.getTraitType()))
                                             .findFirst()
                                             .orElse(null);
    assertNotNull(maxUsers);
    assertEquals(DisplayType.NUMBER, maxUsers.getDisplayType());
    assertEquals(10000, maxUsers.getValue());
    assertNull(maxUsers.getMaxValue());
  }
}
