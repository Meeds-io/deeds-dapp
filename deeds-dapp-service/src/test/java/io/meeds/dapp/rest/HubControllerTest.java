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

import static io.meeds.wom.api.utils.JsonUtils.toJsonStringNoCheckedEx;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.web3j.crypto.Credentials;

import io.meeds.dapp.web.rest.HubController;
import io.meeds.dapp.web.security.DeedAccessDeniedHandler;
import io.meeds.dapp.web.security.DeedAuthenticationProvider;
import io.meeds.dapp.web.security.WebSecurityConfig;
import io.meeds.deeds.common.model.FileBinary;
import io.meeds.deeds.common.model.ManagedDeed;
import io.meeds.deeds.common.service.HubService;
import io.meeds.wom.api.constant.ObjectNotFoundException;
import io.meeds.wom.api.constant.WomAuthorizationException;
import io.meeds.wom.api.constant.WomException;
import io.meeds.wom.api.constant.WomParsingException;
import io.meeds.wom.api.constant.WomRequestException;
import io.meeds.wom.api.model.Hub;
import io.meeds.wom.api.model.HubUpdateRequest;
import io.meeds.wom.api.model.WomConnectionRequest;
import io.meeds.wom.api.model.WomConnectionResponse;
import io.meeds.wom.api.model.WomDisconnectionRequest;

@SpringBootTest(classes = {
                            HubController.class,
                            DeedAuthenticationProvider.class,
                            WebSecurityConfig.class,
                            DeedAccessDeniedHandler.class,
})
@AutoConfigureWebMvc
@AutoConfigureMockMvc(addFilters = false)
public class HubControllerTest {

  private static final String   AVATAR_PATH            = "/avatar";                                                                             // NOSONAR

  private static final String   API_HUBS               = "/api/hubs";                                                                           // NOSONAR

  private static final String   HUB_ADDRESS_PARAM      = "hubAddress";

  private static final String   TOKEN                  = "token";

  private static final String   RAW_MESSAGE            = "rawMessage";

  private static final String   SIGNED_MESSAGE         = "signedMessage";

  @MockBean
  private HubService            hubService;

  @Autowired
  private WebApplicationContext context;

  private MockMvc               mockMvc;

  private Credentials           hubCredentials         =
                                               Credentials.create("0x1da4ef21b864d2cc526dbdb2a120bd2874c36c9d0a1fb7f8c63d7f7a8b41de8f");

  private String                hubAddress             = hubCredentials.getAddress();

  private long                  deedId                 = 3l;

  private short                 city                   = 1;

  private short                 cardType               = 2;

  private String                deedOwnerAddress       = "0x530417D6909834f9Ebfe5d98b649433B616Efb38";

  private Credentials           deedManagerCredentials =
                                                       Credentials.create("0x3ba4ef21b864d2cc526dbdb2a120bd2874c36c9d0a1fb7f8c63d7f7a8b41de8f");

  private String                deedManagerAddress     = deedManagerCredentials.getAddress();

  private String                hubOwnerAddress        = deedManagerAddress;

  private Map<String, String>   name                   = Collections.singletonMap("en", "name");

  private Map<String, String>   description            = Collections.singletonMap("en", "description");

  private String                url                    = "url";

  private String                color                  = "#144523";

  private long                  usersCount             = 56l;

  private String                rewardsPeriodType      = "WEEK";

  private double                rewardsPerPeriod       = 566.54d;

  private boolean               enabled                = true;

  private double                ownerClaimableAmount   = 25.6d;

  private double                managerClaimableAmount = 12.9d;

  private Instant               createdDate            = Instant.now().minusSeconds(16);

  private Instant               untilDate              = Instant.now().plusSeconds(28);

  private Instant               joinDate               = Instant.now().minusSeconds(10);

  private Instant               updatedDate            = Instant.now();

  private String                womAddress             = "womAddress";

  private String                uemAddress             = "uemAddress";

  private long                  uemNetworkId           = 80001;

  @BeforeEach
  public void setup() {
    mockMvc = MockMvcBuilders.webAppContextSetup(context)
                             .apply(springSecurity())
                             .build();
  }

  @Test
  @WithAnonymousUser
  void getHubs() throws Exception {
    when(hubService.getHubs(eq(0l), any())).thenReturn(new PageImpl<>(Arrays.asList(newHub())));
    ResultActions response = mockMvc.perform(get(API_HUBS));
    response.andExpect(status().isOk())
            .andExpect(jsonPath("$.page.totalPages",is(1)));
  }

  @Test
  @WithAnonymousUser
  void getHubNotFound() throws Exception {
    ResultActions response = mockMvc.perform(get(API_HUBS + "/" + hubAddress));
    response.andExpect(status().isNotFound());
  }

  @Test
  @WithAnonymousUser
  void getHub() throws Exception {
    when(hubService.getHub(hubAddress, false)).thenReturn(newHub());
    ResultActions response = mockMvc.perform(get(API_HUBS + "/" + hubAddress));
    response.andExpect(status().isOk())
            .andExpect(jsonPath("$.deedId",is((int) deedId))) // NOSONAR
            .andExpect(jsonPath("$.city",is((int) city)))
            .andExpect(jsonPath("$.type",is((int) cardType)))
            .andExpect(jsonPath("$.address",is(hubAddress)))
            .andExpect(jsonPath("$.name",is(name)))
            .andExpect(jsonPath("$.description",is(description)))
            .andExpect(jsonPath("$.url",is(url)))
            .andExpect(jsonPath("$.color",is(color)))
            .andExpect(jsonPath("$.hubOwnerAddress",is(hubOwnerAddress.toLowerCase())))
            .andExpect(jsonPath("$.deedOwnerAddress",is(deedOwnerAddress.toLowerCase())))
            .andExpect(jsonPath("$.deedManagerAddress",is(deedManagerAddress.toLowerCase())))
            .andExpect(jsonPath("$.usersCount",is((int) usersCount)))
            .andExpect(jsonPath("$.rewardsPeriodType",is(rewardsPeriodType)))
            .andExpect(jsonPath("$.rewardsPerPeriod",is(rewardsPerPeriod)))
            .andExpect(jsonPath("$.connected",is(enabled)))
            .andExpect(jsonPath("$.ownerClaimableAmount",is(ownerClaimableAmount)))
            .andExpect(jsonPath("$.managerClaimableAmount",is(managerClaimableAmount)));
  }

  @Test
  @WithAnonymousUser
  void getManagedDeeds() throws Exception {
    when(hubService.getManagedDeeds(deedManagerAddress)).thenReturn(Arrays.asList(new ManagedDeed()));
    ResultActions response = mockMvc.perform(get("/api/hubs/managed-deeds/" + deedManagerAddress));
    response.andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());
  }

  @Test
  @WithAnonymousUser
  void saveHubAvatarWhenObjectNotFoundException() throws Exception {
    doThrow(ObjectNotFoundException.class).when(hubService)
                                          .saveHubAvatar(eq(hubAddress), eq(SIGNED_MESSAGE), eq(RAW_MESSAGE), eq(TOKEN), any());
    ResultActions response = mockMvc.perform(multipart(API_HUBS + "/" + hubAddress + AVATAR_PATH)
                                                                                                 .file("file", new byte[0])
                                                                                                 .param(HUB_ADDRESS_PARAM, hubAddress)
                                                                                                 .param(SIGNED_MESSAGE,
                                                                                                        SIGNED_MESSAGE)
                                                                                                 .param(RAW_MESSAGE, RAW_MESSAGE)
                                                                                                 .param(TOKEN, TOKEN));
    response.andExpect(status().isNotFound());
  }

  @Test
  @WithAnonymousUser
  void saveHubAvatarWhenWomException() throws Exception {
    doThrow(WomException.class).when(hubService)
                               .saveHubAvatar(eq(hubAddress), eq(SIGNED_MESSAGE), eq(RAW_MESSAGE), eq(TOKEN), any());
    ResultActions response = mockMvc.perform(multipart(API_HUBS + "/" + hubAddress + AVATAR_PATH)
                                                                                                 .file("file", new byte[0])
                                                                                                 .param(HUB_ADDRESS_PARAM, hubAddress)
                                                                                                 .param(SIGNED_MESSAGE,
                                                                                                        SIGNED_MESSAGE)
                                                                                                 .param(RAW_MESSAGE, RAW_MESSAGE)
                                                                                                 .param(TOKEN, TOKEN));
    response.andExpect(status().isBadRequest());
  }

  @Test
  @WithAnonymousUser
  void saveHubAvatarWhenIOException() throws Exception {
    doThrow(IOException.class).when(hubService)
                              .saveHubAvatar(eq(hubAddress), eq(SIGNED_MESSAGE), eq(RAW_MESSAGE), eq(TOKEN), any());
    ResultActions response = mockMvc.perform(multipart(API_HUBS + "/" + hubAddress + AVATAR_PATH)
                                                                                                 .file("file", new byte[0])
                                                                                                 .param(HUB_ADDRESS_PARAM, hubAddress)
                                                                                                 .param(SIGNED_MESSAGE,
                                                                                                        SIGNED_MESSAGE)
                                                                                                 .param(RAW_MESSAGE, RAW_MESSAGE)
                                                                                                 .param(TOKEN, TOKEN));
    response.andExpect(status().isNotFound());
  }

  @Test
  @WithAnonymousUser
  void saveHubAvatar() throws Exception {
    ResultActions response = mockMvc.perform(multipart(API_HUBS + "/" + hubAddress + AVATAR_PATH)
                                                                                                 .file("file", new byte[0])
                                                                                                 .param(HUB_ADDRESS_PARAM, hubAddress)
                                                                                                 .param(SIGNED_MESSAGE,
                                                                                                        SIGNED_MESSAGE)
                                                                                                 .param(RAW_MESSAGE, RAW_MESSAGE)
                                                                                                 .param(TOKEN, TOKEN));
    response.andExpect(status().isOk());
    verify(hubService).saveHubAvatar(eq(hubAddress), eq(SIGNED_MESSAGE), eq(RAW_MESSAGE), eq(TOKEN), any());
  }

  @Test
  @WithAnonymousUser
  void getHubAvatarNotFound() throws Exception {
    ResultActions response = mockMvc.perform(get(API_HUBS + "/" + hubAddress + AVATAR_PATH));
    response.andExpect(status().isNotFound());
  }

  @Test
  @WithAnonymousUser
  void getHubAvatarNoCache() throws Exception {
    FileBinary file = mock(FileBinary.class);
    when(hubService.getHubAvatar(hubAddress)).thenReturn(file);
    when(file.getBinary()).thenReturn(new ByteArrayInputStream(new byte[0]));
    when(file.getUpdatedDate()).thenReturn(updatedDate);
    when(file.getMimeType()).thenReturn(MediaType.IMAGE_PNG_VALUE);

    ResultActions response = mockMvc.perform(get(API_HUBS + "/" + hubAddress + AVATAR_PATH));
    response.andExpect(status().isOk())
            .andExpect(header().dateValue("last-modified", updatedDate.toEpochMilli()))
            .andExpect(header().string("content-type", MediaType.IMAGE_PNG_VALUE))
            .andExpect(header().string("cache-control", "no-store"));
  }

  @Test
  @WithAnonymousUser
  void getHubAvatarWithCache() throws Exception {
    FileBinary file = mock(FileBinary.class);
    when(hubService.getHubAvatar(hubAddress)).thenReturn(file);
    when(file.getBinary()).thenReturn(new ByteArrayInputStream(new byte[0]));
    when(file.getUpdatedDate()).thenReturn(updatedDate);
    when(file.getMimeType()).thenReturn(MediaType.IMAGE_PNG_VALUE);

    ResultActions response = mockMvc.perform(get(API_HUBS + "/" + hubAddress + AVATAR_PATH).param("v", "lastUpdated"));
    response.andExpect(status().isOk())
            .andExpect(header().dateValue("last-modified", updatedDate.toEpochMilli()))
            .andExpect(header().string("content-type", MediaType.IMAGE_PNG_VALUE))
            .andExpect(header().string("cache-control", "max-age=31536000, public"));
  }

  @Test
  @WithAnonymousUser
  void connectToWomWhenWomRequestException() throws Exception {
    when(hubService.connectToWom(any())).thenThrow(WomRequestException.class);
    
    WomConnectionRequest womConnectionRequest = new WomConnectionRequest();
    ResultActions response = mockMvc.perform(post(API_HUBS).contentType(MediaType.APPLICATION_JSON).content(toJsonStringNoCheckedEx(womConnectionRequest)));
    response.andExpect(status().isBadRequest());
  }

  @Test
  @WithAnonymousUser
  void connectToWomWhenWomParsingException() throws Exception {
    when(hubService.connectToWom(any())).thenThrow(WomParsingException.class);
    
    WomConnectionRequest womConnectionRequest = new WomConnectionRequest();
    ResultActions response = mockMvc.perform(post(API_HUBS).contentType(MediaType.APPLICATION_JSON).content(toJsonStringNoCheckedEx(womConnectionRequest)));
    response.andExpect(status().isBadRequest());
  }

  @Test
  @WithAnonymousUser
  void connectToWomWhenWomAuthorizationException() throws Exception {
    when(hubService.connectToWom(any())).thenThrow(WomAuthorizationException.class);
    
    WomConnectionRequest womConnectionRequest = new WomConnectionRequest();
    ResultActions response = mockMvc.perform(post(API_HUBS).contentType(MediaType.APPLICATION_JSON).content(toJsonStringNoCheckedEx(womConnectionRequest)));
    response.andExpect(status().isUnauthorized());
  }

  @Test
  @WithAnonymousUser
  void connectToWomWhenWomException() throws Exception {
    when(hubService.connectToWom(any())).thenThrow(WomException.class);
    
    WomConnectionRequest womConnectionRequest = new WomConnectionRequest();
    ResultActions response = mockMvc.perform(post(API_HUBS).contentType(MediaType.APPLICATION_JSON).content(toJsonStringNoCheckedEx(womConnectionRequest)));
    response.andExpect(status().isInternalServerError());
  }

  @Test
  @WithAnonymousUser
  void connectToWomWhenException() throws Exception {
    when(hubService.connectToWom(any())).thenThrow(RuntimeException.class);
    
    WomConnectionRequest womConnectionRequest = new WomConnectionRequest();
    ResultActions response = mockMvc.perform(post(API_HUBS).contentType(MediaType.APPLICATION_JSON).content(toJsonStringNoCheckedEx(womConnectionRequest)));
    response.andExpect(status().isInternalServerError());
  }

  @Test
  @WithAnonymousUser
  void connectToWom() throws Exception {
    when(hubService.connectToWom(any())).thenReturn(new WomConnectionResponse(deedId, hubAddress, womAddress, uemAddress, uemNetworkId));
    ResultActions response = mockMvc.perform(post(API_HUBS).contentType(MediaType.APPLICATION_JSON)
                                             .content(toJsonStringNoCheckedEx(new WomConnectionRequest())));
    response.andExpect(status().isOk())
    .andExpect(jsonPath("$.deedId", is((int) deedId)))
    .andExpect(jsonPath("$.hubAddress", is(hubAddress)))
    .andExpect(jsonPath("$.womAddress", is(womAddress)))
    .andExpect(jsonPath("$.uemAddress", is(uemAddress)))
    .andExpect(jsonPath("$.networkId", is((int) uemNetworkId)));
  }

  @Test
  @WithAnonymousUser
  void disconnectFromWomWhenWomRequestException() throws Exception {
    when(hubService.disconnectFromWom(any())).thenThrow(WomRequestException.class);
    
    ResultActions response = mockMvc.perform(delete(API_HUBS).contentType(MediaType.APPLICATION_JSON).content(toJsonStringNoCheckedEx(new WomDisconnectionRequest())));
    response.andExpect(status().isBadRequest());
  }

  @Test
  @WithAnonymousUser
  void disconnectFromWomWhenWomParsingException() throws Exception {
    when(hubService.disconnectFromWom(any())).thenThrow(WomParsingException.class);
    ResultActions response = mockMvc.perform(delete(API_HUBS).contentType(MediaType.APPLICATION_JSON).content(toJsonStringNoCheckedEx(new WomDisconnectionRequest())));
    response.andExpect(status().isBadRequest());
  }

  @Test
  @WithAnonymousUser
  void disconnectFromWomWhenWomAuthorizationException() throws Exception {
    when(hubService.disconnectFromWom(any())).thenThrow(WomAuthorizationException.class);
    ResultActions response = mockMvc.perform(delete(API_HUBS).contentType(MediaType.APPLICATION_JSON).content(toJsonStringNoCheckedEx(new WomDisconnectionRequest())));
    response.andExpect(status().isUnauthorized());
  }

  @Test
  @WithAnonymousUser
  void disconnectFromWomWhenWomException() throws Exception {
    when(hubService.disconnectFromWom(any())).thenThrow(WomException.class);
    ResultActions response = mockMvc.perform(delete(API_HUBS).contentType(MediaType.APPLICATION_JSON).content(toJsonStringNoCheckedEx(new WomDisconnectionRequest())));
    response.andExpect(status().isInternalServerError());
  }

  @Test
  @WithAnonymousUser
  void disconnectFromWomWhenException() throws Exception {
    when(hubService.disconnectFromWom(any())).thenThrow(RuntimeException.class);
    
    ResultActions response = mockMvc.perform(delete(API_HUBS).contentType(MediaType.APPLICATION_JSON).content(toJsonStringNoCheckedEx(new WomDisconnectionRequest())));
    response.andExpect(status().isInternalServerError());
  }

  @Test
  @WithAnonymousUser
  void disconnectFromWom() throws Exception {
    when(hubService.disconnectFromWom(any())).thenReturn(womAddress);
    ResultActions response = mockMvc.perform(delete(API_HUBS).contentType(MediaType.APPLICATION_JSON)
                                             .content(toJsonStringNoCheckedEx(new WomDisconnectionRequest())));
    response.andExpect(status().isOk())
    .andExpect(content().string(womAddress));
  }

  @Test
  @WithAnonymousUser
  void updateHubWhenWomRequestException() throws Exception {
    doThrow(WomRequestException.class).when(hubService).updateHub(any());
    ResultActions response = mockMvc.perform(put(API_HUBS).contentType(MediaType.APPLICATION_JSON)
                                                          .content(toJsonStringNoCheckedEx(new HubUpdateRequest())));
    response.andExpect(status().isBadRequest());
  }

  @Test
  @WithAnonymousUser
  void updateHubWhenWomParsingException() throws Exception {
    doThrow(WomParsingException.class).when(hubService).updateHub(any());
    ResultActions response = mockMvc.perform(put(API_HUBS).contentType(MediaType.APPLICATION_JSON)
                                                          .content(toJsonStringNoCheckedEx(new HubUpdateRequest())));
    response.andExpect(status().isBadRequest());
  }

  @Test
  @WithAnonymousUser
  void updateHubWhenWomAuthorizationException() throws Exception {
    doThrow(WomAuthorizationException.class).when(hubService).updateHub(any());
    ResultActions response = mockMvc.perform(put(API_HUBS).contentType(MediaType.APPLICATION_JSON)
                                                          .content(toJsonStringNoCheckedEx(new HubUpdateRequest())));
    response.andExpect(status().isUnauthorized());
  }

  @Test
  @WithAnonymousUser
  void updateHubWhenWomException() throws Exception {
    doThrow(WomException.class).when(hubService).updateHub(any());
    ResultActions response = mockMvc.perform(put(API_HUBS).contentType(MediaType.APPLICATION_JSON)
                                                          .content(toJsonStringNoCheckedEx(new HubUpdateRequest())));
    response.andExpect(status().isInternalServerError());
  }

  @Test
  @WithAnonymousUser
  void updateHubWhenException() throws Exception {
    doThrow(RuntimeException.class).when(hubService).updateHub(any());
    ResultActions response = mockMvc.perform(put(API_HUBS).contentType(MediaType.APPLICATION_JSON)
                                                          .content(toJsonStringNoCheckedEx(new HubUpdateRequest())));
    response.andExpect(status().isInternalServerError());
  }

  @Test
  @WithAnonymousUser
  void updateHub() throws Exception {
    ResultActions response = mockMvc.perform(put(API_HUBS).contentType(MediaType.APPLICATION_JSON)
                                                          .content(toJsonStringNoCheckedEx(new HubUpdateRequest())));
    response.andExpect(status().isNoContent());
  }

  @Test
  @WithAnonymousUser
  void getHubByNftIdNotFound() throws Exception {
    ResultActions response = mockMvc.perform(get(API_HUBS + "/byNftId/" + deedId));
    response.andExpect(status().isNotFound());
  }

  @Test
  @WithAnonymousUser
  void getHubByNftId() throws Exception {
    when(hubService.getHub(deedId)).thenReturn(newHub());
    ResultActions response = mockMvc.perform(get(API_HUBS + "/byNftId/" + deedId));
    response.andExpect(status().isOk())
            .andExpect(jsonPath("$.deedId",is((int) deedId)))
            .andExpect(jsonPath("$.city",is((int) city)))
            .andExpect(jsonPath("$.type",is((int) cardType)))
            .andExpect(jsonPath("$.address",is(hubAddress)))
            .andExpect(jsonPath("$.name",is(name)))
            .andExpect(jsonPath("$.description",is(description)))
            .andExpect(jsonPath("$.url",is(url)))
            .andExpect(jsonPath("$.color",is(color)))
            .andExpect(jsonPath("$.hubOwnerAddress",is(hubOwnerAddress.toLowerCase())))
            .andExpect(jsonPath("$.deedOwnerAddress",is(deedOwnerAddress.toLowerCase())))
            .andExpect(jsonPath("$.deedManagerAddress",is(deedManagerAddress.toLowerCase())))
            .andExpect(jsonPath("$.usersCount",is((int) usersCount)))
            .andExpect(jsonPath("$.rewardsPeriodType",is(rewardsPeriodType)))
            .andExpect(jsonPath("$.rewardsPerPeriod",is(rewardsPerPeriod)))
            .andExpect(jsonPath("$.connected",is(enabled)))
            .andExpect(jsonPath("$.ownerClaimableAmount",is(ownerClaimableAmount)))
            .andExpect(jsonPath("$.managerClaimableAmount",is(managerClaimableAmount)));
  }

  private Hub newHub() {
    return new Hub(deedId,
                   city,
                   cardType,
                   hubAddress,
                   name,
                   description,
                   url,
                   color,
                   hubOwnerAddress,
                   deedOwnerAddress,
                   deedManagerAddress,
                   createdDate,
                   untilDate,
                   joinDate,
                   updatedDate,
                   usersCount,
                   rewardsPeriodType,
                   rewardsPerPeriod,
                   enabled,
                   ownerClaimableAmount,
                   managerClaimableAmount);
  }

}
