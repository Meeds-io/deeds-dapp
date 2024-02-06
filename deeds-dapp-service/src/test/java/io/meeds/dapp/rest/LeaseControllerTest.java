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
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package io.meeds.dapp.rest;

import static io.meeds.deeds.common.constant.CommonConstants.CODE_VERIFICATION_HTTP_HEADER;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.Collections;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import io.meeds.dapp.web.rest.LeaseController;
import io.meeds.dapp.web.security.DeedAccessDeniedHandler;
import io.meeds.dapp.web.security.DeedAuthenticationProvider;
import io.meeds.dapp.web.security.WebSecurityConfig;
import io.meeds.deeds.common.constant.DeedCard;
import io.meeds.deeds.common.constant.DeedCity;
import io.meeds.deeds.common.constant.NoticePeriod;
import io.meeds.deeds.common.constant.RentalPaymentPeriodicity;
import io.meeds.deeds.common.constant.TenantProvisioningStatus;
import io.meeds.deeds.common.constant.TenantStatus;
import io.meeds.deeds.common.constant.TransactionStatus;
import io.meeds.deeds.common.constant.UnauthorizedOperationException;
import io.meeds.deeds.common.model.DeedTenantLeaseDTO;
import io.meeds.deeds.common.service.AuthorizationCodeService;
import io.meeds.deeds.common.service.LeaseService;
import io.meeds.wom.api.constant.ObjectNotFoundException;

@SpringBootTest(classes = {
                            LeaseController.class,
                            DeedAuthenticationProvider.class,
                            WebSecurityConfig.class,
                            DeedAccessDeniedHandler.class,
})
@AutoConfigureWebMvc
@AutoConfigureMockMvc(addFilters = false)
public class LeaseControllerTest {

  private static final String      OWNER_ADDRESS_PARAM    = "ownerAddress";

  private static final String      PAID_MONTHS_PARAM      = "paidMonths";

  private static final String      OFFER_ID_PARAM         = "offerId";

  private static final String      TRANSACTION_HASH_PARAM = "transactionHash";

  private static final String      EMAIL                  = "email";

  private static final String      USERNAME               = "0x609a6f01b7976439603356e41d5456b42df957b7";

  private static final String      API_LEASES             = "/api/leases";

  @MockBean
  private LeaseService             leaseService;

  @MockBean
  private AuthorizationCodeService authorizationCodeService;

  @Autowired
  private WebApplicationContext    context;

  private MockMvc                  mockMvc;

  private String                   offerId                = OFFER_ID_PARAM;

  private int                      code                   = 564889;

  private long                     leaseId                = 5l;

  private long                     nftId                  = 56l;

  private DeedCity                 city                   = DeedCity.RESHEF;

  private DeedCard                 cardType               = DeedCard.RARE;

  private boolean                  onlyConfirmed          = true;

  private boolean                  owner                  = true;

  private String                   address                = USERNAME;

  private TenantStatus             tenantStatus           = TenantStatus.DEPLOYED;

  private TenantProvisioningStatus provisioningStatus     = TenantProvisioningStatus.START_CONFIRMED;

  private int                      months                 = 6;

  private int                      paidMonths             = 3;

  private int                      monthPaymentInProgress = 1;

  private String                   ownerAddress           = "0x530417D6909834f9Ebfe5d98b649433B616Efb38";

  private String                   managerAddress         = "0xB36b174DC531B8055631A4E8d32f44eADC1B9695";

  private RentalPaymentPeriodicity paymentPeriodicity     = RentalPaymentPeriodicity.ONE_MONTH;

  private double                   amount                 = 56648.36;

  private double                   allDurationAmount      = 89648.36;

  private double                   distributedAmount      = 5.89;

  private NoticePeriod             noticePeriod           = NoticePeriod.ONE_MONTH;

  private int                      ownerMintingPercentage = 60;

  private double                   mintingPower           = 1.3;

  private TransactionStatus        transactionStatus      = TransactionStatus.VALIDATED;

  private Instant                  startDate              = Instant.now();

  private Instant                  endDate                = Instant.now();

  private Instant                  noticeDate             = Instant.now();

  private Instant                  paidRentsDate          = Instant.now();

  private boolean                  confirmed              = true;

  private boolean                  endingLease            = true;

  private String                   endingLeaseAddress     = ownerAddress;

  private String                   transactionHash        = "0xef4e9db309b5dd7020ce463ae726b4d0759e1de0635661de91d8d98e83ae2862";

  @BeforeEach
  public void setup() {
    mockMvc = MockMvcBuilders.webAppContextSetup(context)
                             .apply(springSecurity())
                             .build();
  }

  @Test
  @WithAnonymousUser
  void getLeases() throws Exception {
    when(leaseService.getLeases(argThat(leaseFilter -> leaseFilter.getNftId() == nftId
        && StringUtils.equals(leaseFilter.getCurrentAddress(), address)
        && leaseFilter.getOwner().booleanValue() == owner
        && leaseFilter.isExcludeNotConfirmed() == onlyConfirmed
        && !leaseFilter.isIncludeOutdated()
        && leaseFilter.getCardTypes().get(0) == cardType), any())).thenReturn(new PageImpl<>(Collections.singletonList(newDeedTenantLease())));
    ResultActions response = mockMvc.perform(get(API_LEASES)
                                             .param("nftId", String.valueOf(nftId))
                                             .param("cardType", cardType.name())
                                             .param("onlyConfirmed", String.valueOf(onlyConfirmed))
                                             .param("owner", String.valueOf(owner))
                                             .param("address", address));
    response.andExpect(status().isOk())
    .andExpect(jsonPath("$.page.totalPages",is(1)));
  }

  @Test
  @WithAnonymousUser
  void getLeaseWhenNotFound() throws Exception {
    ResultActions response = mockMvc.perform(get(API_LEASES + "/" + leaseId));
    response.andExpect(status().isNotFound());
  }

  @Test
  @WithAnonymousUser
  void getLeaseWhenObjectNotFoundException() throws Exception {
    when(leaseService.getLease(leaseId, null, false)).thenThrow(ObjectNotFoundException.class);
    ResultActions response = mockMvc.perform(get(API_LEASES + "/" + leaseId));
    response.andExpect(status().isNotFound());
  }

  @Test
  @WithAnonymousUser
  void getLeaseWhenUnauthorizedOperationException() throws Exception {
    when(leaseService.getLease(leaseId, null, false)).thenThrow(UnauthorizedOperationException.class);
    ResultActions response = mockMvc.perform(get(API_LEASES + "/" + leaseId));
    response.andExpect(status().isNotFound());
  }

  @Test
  @WithAnonymousUser
  void getLeaseWhenException() throws Exception {
    when(leaseService.getLease(leaseId, null, false)).thenThrow(RuntimeException.class);
    ResultActions response = mockMvc.perform(get(API_LEASES + "/" + leaseId));
    response.andExpect(status().isInternalServerError());
  }

  @Test
  @WithAnonymousUser
  void getLease() throws Exception {
    when(leaseService.getLease(leaseId, null, false)).thenReturn(newDeedTenantLease());
    ResultActions response = mockMvc.perform(get(API_LEASES + "/" + leaseId));
    response.andExpect(status().isOk());
  }

  @Test
  @WithAnonymousUser
  void createLeaseWithAnonymous() throws Exception {
    ResultActions response = mockMvc.perform(post(API_LEASES).header(CODE_VERIFICATION_HTTP_HEADER, String.valueOf(code))
                                                             .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                                                             .param(OFFER_ID_PARAM, offerId)
                                                             .param(TRANSACTION_HASH_PARAM, transactionHash));
    response.andExpect(status().is3xxRedirection());
  }

  @Test
  @WithMockUser(username = USERNAME, authorities = DeedAuthenticationProvider.USER_ROLE_NAME)
  void createLease() throws Exception {
    when(authorizationCodeService.validateAndGetData(USERNAME, code)).thenReturn(EMAIL);
    ResultActions response = mockMvc.perform(post(API_LEASES).header(CODE_VERIFICATION_HTTP_HEADER, String.valueOf(code))
                                                             .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                                                             .param(OFFER_ID_PARAM, offerId)
                                                             .param(TRANSACTION_HASH_PARAM, transactionHash));
    response.andExpect(status().isOk());
    verify(leaseService).createLease(USERNAME, EMAIL, offerId, transactionHash);
  }

  @Test
  @WithMockUser(username = USERNAME, authorities = DeedAuthenticationProvider.USER_ROLE_NAME)
  void createLeaseWhenIllegalAccessException() throws Exception {
    when(authorizationCodeService.validateAndGetData(USERNAME, code)).thenThrow(IllegalAccessException.class);
    ResultActions response = mockMvc.perform(post(API_LEASES).header(CODE_VERIFICATION_HTTP_HEADER, String.valueOf(code))
                                             .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                                             .param(OFFER_ID_PARAM, offerId)
                                             .param(TRANSACTION_HASH_PARAM, transactionHash));
    response.andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(username = USERNAME, authorities = DeedAuthenticationProvider.USER_ROLE_NAME)
  void createLeaseWhenUnauthorizedOperationException() throws Exception {
    when(authorizationCodeService.validateAndGetData(USERNAME, code)).thenReturn(EMAIL);
    when(leaseService.createLease(USERNAME, EMAIL, offerId, transactionHash)).thenThrow(UnauthorizedOperationException.class);
    ResultActions response = mockMvc.perform(post(API_LEASES).header(CODE_VERIFICATION_HTTP_HEADER, String.valueOf(code))
                                             .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                                             .param(OFFER_ID_PARAM, offerId)
                                             .param(TRANSACTION_HASH_PARAM, transactionHash));
    response.andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(username = USERNAME, authorities = DeedAuthenticationProvider.USER_ROLE_NAME)
  void createLeaseWhenObjectNotFoundException() throws Exception {
    when(authorizationCodeService.validateAndGetData(USERNAME, code)).thenReturn(EMAIL);
    when(leaseService.createLease(USERNAME, EMAIL, offerId, transactionHash)).thenThrow(ObjectNotFoundException.class);
    ResultActions response = mockMvc.perform(post(API_LEASES).header(CODE_VERIFICATION_HTTP_HEADER, String.valueOf(code))
                                                             .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                                                             .param(OFFER_ID_PARAM, offerId)
                                                             .param(TRANSACTION_HASH_PARAM, transactionHash));
    response.andExpect(status().isNotFound());
  }

  @Test
  @WithAnonymousUser
  void payRentWithAnonymous() throws Exception {
    ResultActions response = mockMvc.perform(patch(API_LEASES + "/" + leaseId).contentType(MediaType.APPLICATION_FORM_URLENCODED)
                                                                              .param(PAID_MONTHS_PARAM,
                                                                                     String.valueOf(paidMonths))
                                                                              .param(TRANSACTION_HASH_PARAM,
                                                                                     String.valueOf(transactionHash))
                                                                              .param(OWNER_ADDRESS_PARAM, ownerAddress));
    response.andExpect(status().is3xxRedirection());
  }

  @Test
  @WithMockUser(username = USERNAME, authorities = DeedAuthenticationProvider.USER_ROLE_NAME)
  void payRent() throws Exception {
    ResultActions response = mockMvc.perform(patch(API_LEASES + "/" + leaseId).contentType(MediaType.APPLICATION_FORM_URLENCODED)
                                                                              .param(PAID_MONTHS_PARAM,
                                                                                     String.valueOf(paidMonths))
                                                                              .param(TRANSACTION_HASH_PARAM,
                                                                                     String.valueOf(transactionHash))
                                                                              .param(OWNER_ADDRESS_PARAM, ownerAddress));
    response.andExpect(status().isOk());
    verify(leaseService).payRents(USERNAME, ownerAddress, leaseId, paidMonths, transactionHash);
  }

  @Test
  @WithMockUser(username = USERNAME, authorities = DeedAuthenticationProvider.USER_ROLE_NAME)
  void payRentWhenBadLeaseId() throws Exception {
    ResultActions response = mockMvc.perform(patch(API_LEASES + "/0").contentType(MediaType.APPLICATION_FORM_URLENCODED)
                                                                     .param(PAID_MONTHS_PARAM, String.valueOf(paidMonths))
                                                                     .param(TRANSACTION_HASH_PARAM,
                                                                            String.valueOf(transactionHash))
                                                                     .param(OWNER_ADDRESS_PARAM, ownerAddress));
    response.andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser(username = USERNAME, authorities = DeedAuthenticationProvider.USER_ROLE_NAME)
  void payRentWhenBadPaidMonths() throws Exception {
    ResultActions response = mockMvc.perform(patch(API_LEASES + "/" + leaseId).contentType(MediaType.APPLICATION_FORM_URLENCODED)
                                                                              .param(PAID_MONTHS_PARAM, String.valueOf(0))
                                                                              .param(TRANSACTION_HASH_PARAM,
                                                                                     String.valueOf(transactionHash))
                                                                              .param(OWNER_ADDRESS_PARAM, ownerAddress));
    response.andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser(username = USERNAME, authorities = DeedAuthenticationProvider.USER_ROLE_NAME)
  void payRentWhenUnauthorizedOperationException() throws Exception {
    when(leaseService.payRents(USERNAME, ownerAddress, leaseId, paidMonths, transactionHash)).thenThrow(UnauthorizedOperationException.class);
    ResultActions response = mockMvc.perform(patch(API_LEASES + "/" + leaseId).contentType(MediaType.APPLICATION_FORM_URLENCODED)
                                             .param(PAID_MONTHS_PARAM, String.valueOf(paidMonths))
                                             .param(TRANSACTION_HASH_PARAM, String.valueOf(transactionHash))
                                             .param(OWNER_ADDRESS_PARAM, ownerAddress));
    response.andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(username = USERNAME, authorities = DeedAuthenticationProvider.USER_ROLE_NAME)
  void payRentWhenObjectNotFoundException() throws Exception {
    when(leaseService.payRents(USERNAME, ownerAddress, leaseId, paidMonths, transactionHash)).thenThrow(ObjectNotFoundException.class);
    ResultActions response = mockMvc.perform(patch(API_LEASES + "/" + leaseId).contentType(MediaType.APPLICATION_FORM_URLENCODED)
                                             .param(PAID_MONTHS_PARAM, String.valueOf(paidMonths))
                                             .param(TRANSACTION_HASH_PARAM, String.valueOf(transactionHash))
                                             .param(OWNER_ADDRESS_PARAM, ownerAddress));
    response.andExpect(status().isNotFound());
  }

  public DeedTenantLeaseDTO newDeedTenantLease() {
    return new DeedTenantLeaseDTO(leaseId,
                                  nftId,
                                  city,
                                  cardType,
                                  tenantStatus,
                                  provisioningStatus,
                                  months,
                                  paidMonths,
                                  monthPaymentInProgress,
                                  ownerAddress,
                                  managerAddress,
                                  paymentPeriodicity,
                                  amount,
                                  allDurationAmount,
                                  distributedAmount,
                                  noticePeriod,
                                  ownerMintingPercentage,
                                  mintingPower,
                                  transactionStatus,
                                  startDate,
                                  endDate,
                                  noticeDate,
                                  paidRentsDate,
                                  confirmed,
                                  endingLease,
                                  endingLeaseAddress);
  }
}
