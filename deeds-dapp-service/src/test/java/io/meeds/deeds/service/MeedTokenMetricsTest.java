
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

import io.meeds.deeds.constant.Currency;
import io.meeds.deeds.contract.MeedsToken;
import io.meeds.deeds.model.CurrencyExchangeRate;
import io.meeds.deeds.model.MeedExchangeRate;
import io.meeds.deeds.model.MeedTokenMetric;
import io.meeds.deeds.storage.MeedTokenMetricsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.web3j.protocol.core.RemoteFunctionCall;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest(
        classes = {
                MeedTokenMetricService.class,
        }
)
@TestPropertySource(
        properties = {
                "meeds.blockchain.reserveValueEthereumAddresses=0xBa5e4D55CA96bf25c35Fc65D9251355Dcd120655,0x8f4660498E79c771f93316f09da98E1eBF94c576,0x70CAd5d439591Ea7f496B69DcB22521685015853",
                "meeds.blockchain.reserveValuePolygonAddresses=",
                "meeds.blockchain.lockedValueEthereumAddresses=0x44D6d6aB50401Dd846336e9C706A492f06E1Bcd4,0x960Bd61D0b960B107fF5309A2DCceD4705567070",
                "meeds.blockchain.lockedValuePolygonAddresses=0x6acA77CF3BaB0C4E8210A09B57B07854a995289a"
        }
)


class MeedTokenMetricTest {

    @MockBean
    private BlockchainService               blockchainService;

    @MockBean
    private MeedTokenMetric metric;

    @Autowired
    private MeedTokenMetricService          meedTokenMetricService;

    @MockBean
    private MeedTokenMetricsRepository meedTokenMetricsRepository;


    @MockBean(name = "ethereumMeedToken")
    private MeedsToken                 ethereumToken;

    @MockBean(name = "polygonMeedToken")
    private MeedsToken                 polygonToken;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void init() {
        reset(meedTokenMetricsRepository);
    }

    @Value(
            "#{'${meeds.blockchain.reserveValueEthereumAddresses:0xBa5e4D55CA96bf25c35Fc65D9251355Dcd120655,0x8f4660498E79c771f93316f09da98E1eBF94c576,0x70CAd5d439591Ea7f496B69DcB22521685015853}'.split(',')}"
    )
    private List<String> reserveEthereumAddresses;

    @Value(
            "#{'${meeds.blockchain.reserveValuePolygonAddresses:}'.split(',')}"
    )
    private List<String>               reservePolygonAddresses;

    @Value(
            "#{'${meeds.blockchain.lockedValueEthereumAddresses:0x44D6d6aB50401Dd846336e9C706A492f06E1Bcd4,0x960Bd61D0b960B107fF5309A2DCceD4705567070}'.split(',')}"
    )
    private List<String>               lockedEthereumAddresses;

    @Value("#{'${meeds.blockchain.lockedValuePolygonAddresses:0x6acA77CF3BaB0C4E8210A09B57B07854a995289a}'.split(',')}")
    private List<String>               lockedPolygonAddresses;



    @Test
    void testGetReserveBalances(){
        Map<String, RemoteFunctionCall<BigInteger>> reserveEthereumBalances = new HashMap<>();
        Map<String, RemoteFunctionCall<BigInteger>> reservePolygonBalances = new HashMap<>();
        Map<String, RemoteFunctionCall<BigInteger>> reserveBalances = new HashMap<>();
        when(
                reserveEthereumBalances.put(reserveEthereumAddresses.get(1).toLowerCase()
                        , ethereumToken.balanceOf(reserveEthereumAddresses.get(1)))
        ).thenAnswer(invocation -> {
                @SuppressWarnings("unchecked")
                RemoteFunctionCall<Map<String,RemoteFunctionCall<BigDecimal>>> remoteFunctionCall = mock(RemoteFunctionCall.class);
                when(remoteFunctionCall.send()).thenReturn((Map<String, RemoteFunctionCall<BigDecimal>>) reserveEthereumBalances.get(1));
                return remoteFunctionCall;
        });

        when(
                reservePolygonBalances.put(reservePolygonAddresses.get(0).toLowerCase()
                        , polygonToken.balanceOf(reservePolygonAddresses.get(0)))
        ).thenAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            RemoteFunctionCall<Map<String,RemoteFunctionCall<BigDecimal>>> remoteFunctionCall = mock(RemoteFunctionCall.class);
            when(remoteFunctionCall.send()).thenReturn((Map<String, RemoteFunctionCall<BigDecimal>>) reservePolygonBalances.get(1));
            return remoteFunctionCall;
        });
        reserveBalances.putAll(reservePolygonBalances);
        reserveBalances.putAll(reserveEthereumBalances);
        assertEquals(4, meedTokenMetricService.getReserveBalances().size());
    }


    @Test
    void testGetLockedBalances() {
        Map<String, RemoteFunctionCall<BigInteger>> lockedEthereumBalances = new HashMap<>();
        when(
                lockedEthereumBalances.put(lockedEthereumAddresses.get(1).toLowerCase()
                        , ethereumToken.balanceOf(lockedEthereumAddresses.get(1)))
        ).thenAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            RemoteFunctionCall<Map<String,RemoteFunctionCall<BigDecimal>>> remoteFunctionCall = mock(RemoteFunctionCall.class);
            when(remoteFunctionCall.send()).thenReturn((Map<String, RemoteFunctionCall<BigDecimal>>) lockedEthereumBalances.get(1));
            return remoteFunctionCall;
        });
        assertEquals(3, meedTokenMetricService.getLockedBalances().size());
    }

    @Test
    void testComputeTokenMetrics() {
        assertNotNull(meedTokenMetricService);
        when(metric == null)
                .thenAnswer(invocation -> new MeedTokenMetric(LocalDate.now(ZoneOffset.UTC)));
        when(blockchainService.totalSupply()).thenAnswer(invocation -> {
            metric.setTotalSupply(invocation.getArgument(0));
            return blockchainService.totalSupply();
        });
        when(meedTokenMetricService.getReserveBalances()).thenAnswer(invocation -> {
            metric.setReserveBalances(invocation.getArgument(0));
            return meedTokenMetricService.getReserveBalances();
        });
        when(meedTokenMetricService.getLockedBalances()).thenAnswer(invocation -> {
            metric.setLockedBalances(invocation.getArgument(0));
            return meedTokenMetricService.getLockedBalances();
        });
        BigDecimal reserveValue = meedTokenMetricService.getReserveBalances().values().stream().reduce(BigDecimal::add).orElse(BigDecimal.valueOf(0));
        BigDecimal lockedValue = meedTokenMetricService.getLockedBalances().values().stream().reduce(BigDecimal::add).orElse(BigDecimal.valueOf(0));
        BigDecimal circulatingSupply = blockchainService.totalSupply().subtract(reserveValue).subtract(lockedValue);
        metric.setCirculatingSupply(circulatingSupply);
        meedTokenMetricsRepository.save(metric);

        verify(meedTokenMetricsRepository, times(1))
                .save(new MeedTokenMetric(
                                LocalDate.now(),
                                blockchainService.totalSupply(),
                                meedTokenMetricService.getLockedBalances(),
                                meedTokenMetricService.getReserveBalances(),
                                circulatingSupply,
                                null,
                                null
                        )
                );

    }

    private LocalDate getTodayId() {
        return LocalDate.now(ZoneOffset.UTC);
    }
}
