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
package io.meeds.dapp.web.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.meeds.deeds.common.service.UemRewardService;
import io.meeds.wom.api.model.UemReward;

@RestController
@RequestMapping("/api/uem/rewards")
public class UemRewardController {

  @Autowired
  private UemRewardService rewardService;

  @GetMapping
  public ResponseEntity<PagedModel<EntityModel<UemReward>>> getRewards(Pageable pageable,
                                                                       PagedResourcesAssembler<UemReward> assembler,
                                                                       @RequestParam(name = "hubAddress", required = false)
                                                                       String hubAddress) {
    Page<UemReward> rewards = rewardService.getRewards(hubAddress, pageable);
    return ResponseEntity.ok()
                         .cacheControl(CacheControl.noStore())
                         .body(assembler.toModel(rewards));
  }

  @GetMapping("{rewardId}")
  public ResponseEntity<UemReward> getReward(
                                             @PathVariable(name = "rewardId")
                                             long rewardId,
                                             @RequestParam(name = "forceRefresh", required = false)
                                             boolean forceRefresh) {
    UemReward reward = forceRefresh ? rewardService.refreshRewardProperties(rewardId) : rewardService.getRewardById(rewardId);
    return ResponseEntity.ok()
                         .cacheControl(CacheControl.noStore())
                         .body(reward);
  }

  @GetMapping("claimable/{address}")
  public ResponseEntity<String> getClaimableRewards(
                                                    @PathVariable(name = "address")
                                                    String address) {
    return ResponseEntity.ok(String.valueOf(rewardService.getClaimableRewards(address)));
  }

}
