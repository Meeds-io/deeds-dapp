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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import io.meeds.deeds.model.MeedTokenMetric;
import io.meeds.deeds.storage.MeedTokenMetricsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CirculatingSupplyService {

	private static final Logger LOG = LoggerFactory.getLogger(ExchangeService.class);

	private static final LocalDate         CIRCULATING_SUPPLY_DATE            = LocalDate.now();

	private static final String xMEEDS_TOKEN_ADDRESS = "0x44D6d6aB50401Dd846336e9C706A492f06E1Bcd4";

	private static final String SUSHIPOOL_TOKEN_ADDRESS = "0x960Bd61D0b960B107fF5309A2DCceD4705567070";

	private static final String COMETHSWAP_TOKEN_ADDRESS = "0x6acA77CF3BaB0C4E8210A09B57B07854a995289a";

	private static final String VESTING_TOKEN_ADDRESS = "0x440701Ca5817b5847438da2EC2cA3b9fdBF37DFa";

	private static final String USERS_RESERVES ="0xBa5e4D55CA96bf25c35Fc65D9251355Dcd120655";

	private static final String BUILDERS_RESERVES ="0x8f4660498E79c771f93316f09da98E1eBF94c576";

	private static final String INVESTORS_RESERVES ="0x70CAd5d439591Ea7f496B69DcB22521685015853";

	private static final Map<String,BigDecimal> lockedValues = new HashMap<>() ;

	private static final Map<String,BigDecimal> totalReserves = new HashMap<>() ;
	@Autowired(required = false)
	private BlockchainService blockchainService;

	@Autowired
	private MeedTokenMetricsRepository meedTokenMetricsRepository;

	private BigDecimal getVestedMeeds() {
		try {
			return blockchainService.balanceOf(VESTING_TOKEN_ADDRESS);
		} catch (Exception e) {
			LOG.warn("Error retrieving contract vested supply", e);
		}
		return null;
	}

	private BigDecimal getXMeedsStakes() {
		try {
			return blockchainService.balanceOf(xMEEDS_TOKEN_ADDRESS);
		} catch (Exception e) {
			LOG.warn("Error retrieving contract XMeedsStakes supply", e);
		}
		return null;
	}

	private BigDecimal getSushiPoolMeeds() {
		try {
			return blockchainService.balanceOf(SUSHIPOOL_TOKEN_ADDRESS);
		} catch (Exception e) {
			LOG.warn("Error retrieving contract sushiPoolMeeds supply", e);
		}
		return null;
	}

	private BigDecimal getComethSwapPoolMeeds() {
		try {
			return blockchainService.balanceOf(COMETHSWAP_TOKEN_ADDRESS);
		} catch (Exception e) {
			LOG.warn("Error retrieving contract sushiPoolMeeds supply", e);
		}
		return null;
	}

	public BigDecimal getCircualtingSupply() {
		try {
			return getTotalSupply()
					.subtract(getVestedMeeds())
					.subtract(getXMeedsStakes())
					.subtract(getSushiPoolMeeds())
					.subtract(getComethSwapPoolMeeds())
					.subtract(blockchainService.balanceOf(USERS_RESERVES))
					.subtract(blockchainService.balanceOf(INVESTORS_RESERVES))
					.subtract(blockchainService.balanceOf(BUILDERS_RESERVES));
		} catch (Exception e) {
			LOG.warn("Error retrieving Meed total supply", e);
		}
		return null;
	}

	private BigDecimal getTotalSupply() {
		try {
			return blockchainService.totalSupply();
		} catch (Exception e) {
			LOG.warn("Error retrieving contract total supply", e);
		}
		return null;
	}

	private Map<String, BigDecimal> getTotalReserves() {
		lockedValues.put("USERS_RESERVES",blockchainService.balanceOf(USERS_RESERVES)) ;
		lockedValues.put("INVESTORS_RESERVES",blockchainService.balanceOf(INVESTORS_RESERVES)) ;
		lockedValues.put("BUILDERS_RESERVES",blockchainService.balanceOf(BUILDERS_RESERVES)) ;
		return totalReserves;
	}

	private Map<String, BigDecimal> getTotalLocked() {
		lockedValues.put("VESTING_TOKEN_ADDRESS",getVestedMeeds()) ;
		lockedValues.put("xMEEDS_TOKEN_ADDRESS",getXMeedsStakes()) ;
		lockedValues.put("SUSHIPOOL_TOKEN_ADDRESS",getSushiPoolMeeds()) ;
		lockedValues.put("COMETHSWAP_TOKEN_ADDRESS",getComethSwapPoolMeeds()) ;
		return lockedValues;
	}

	/**
	 * Retrieves list of total circulating Meeds supply from adding total Meeds
	 * supply, total Meeds reserves and total Meeds locked tokens
	 *
	 */
	public void computeCirculatingSupply() {
		LocalDate indexDate = circulatingSupplyDate();
			MeedTokenMetric metric = meedTokenMetricsRepository.findById(indexDate)
					.orElse(new MeedTokenMetric());
			if (!metric.getDate().equals(indexDate)) {
				metric.setTotalSupply(getTotalSupply());
				metric.setReserveValues(getTotalReserves());
				metric.setLockedValues(getTotalLocked());
				metric.setCirculatingSupply(getCircualtingSupply());
				meedTokenMetricsRepository.save(metric);
			}
	}

	private LocalDate circulatingSupplyDate() {
		return CIRCULATING_SUPPLY_DATE;
	}

}
