
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

import io.meeds.deeds.storage.MeedTokenMetricsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.stereotype.Component;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest(
        classes = {
                CirculatingSupplyTest.CirculatingSupplyServiceNoInit.class,
        }
)
@TestPropertySource(
        properties = {
                "meeds.blockchain.reserveValueEthereumAddresses:{0xBa5e4D55CA96bf25c35Fc65D9251355Dcd120655,0x8f4660498E79c771f93316f09da98E1eBF94c576,0x70CAd5d439591Ea7f496B69DcB22521685015853}",
                "meeds.blockchain.reserveValuePolygonAddresses" ,
                "meeds.blockchain.lockedValueEthereumAddresses:{0x44D6d6aB50401Dd846336e9C706A492f06E1Bcd4,0x960Bd61D0b960B107fF5309A2DCceD4705567070}",
                "meeds.blockchain.lockedValuePolygonAddresses:0x6acA77CF3BaB0C4E8210A09B57B07854a995289a"
        }
)
class CirculatingSupplyTest {

    @MockBean
    private MeedTokenMetricsRepository meedTokenMetricsRepository;

    @Autowired
    private MeedTokenMetricService          meedTokenMetricService;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void init() {
        reset(meedTokenMetricsRepository);
    }

    @Test
    void testRetriveCirculatingSupply() {
        assertNotNull(meedTokenMetricService);

        meedTokenMetricService.retriveTokenMetrics();
        verify(meedTokenMetricsRepository, times(1)).save(any());
    }

    @Test
    void testGetReserveBalances() {
        assertNotNull(meedTokenMetricService);

        meedTokenMetricService.getReserveBalances();

    }

    @Test
    void testGetLockedBalances() {
        assertNotNull(meedTokenMetricService);

        meedTokenMetricService.getLockedBalances();

    }

    @Test
    void testGetTodayMetric() {
        assertNotNull(meedTokenMetricService);

        meedTokenMetricService.getTodayMetric();

    }

    @Test
    void testGetTodayId() {
        assertNotNull(meedTokenMetricService);

        meedTokenMetricService.getTodayId();

    }


    @Component
    public static class CirculatingSupplyServiceNoInit extends MeedTokenMetricService {

    }
}
