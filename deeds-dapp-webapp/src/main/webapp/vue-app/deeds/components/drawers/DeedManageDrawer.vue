<!--
 This file is part of the Meeds project (https://meeds.io/).
 
 Copyright (C) 2020 - 2022 Meeds Association contact@meeds.io
 
 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.
 
 You should have received a copy of the GNU Lesser General Public License
 along with this program; if not, write to the Free Software Foundation,
 Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
-->
<template>
  <deeds-drawer
    ref="drawer"
    :first-level="secondLevelOpened"
    @opened="$emit('opened')"
    @closed="$emit('closed')">
    <template #title>
      <h4>{{ $t('manageDeedTitle', {0: cardTypeName, 1: nftId}) }}</h4>
    </template>
    <template v-if="cardTypeInfo" #content>
      <v-list-item two-line>
        <v-list-item-avatar height="55" tile>
          <v-img
            :src="cardImage"
            contain />
        </v-list-item-avatar>
        <v-list-item-content>
          <v-list-item-title class="font-weight-bold">{{ $t('deedName') }}:</v-list-item-title>
          <v-list-item-subtitle class="text-truncate">{{ cardTypeName }} #{{ nftId }}</v-list-item-subtitle>
        </v-list-item-content>
      </v-list-item>
      <v-list-item>
        <v-list-item-content>
          <v-list-item-title class="font-weight-bold">{{ $t('deedCharacteristics') }}</v-list-item-title>
        </v-list-item-content>
      </v-list-item>
      <v-list-item class="ms-4 my-n2" dense>
        <v-list-item-subtitle class="text-color">{{ $t('deedCharacteristicsCity') }}: {{ cityName }}</v-list-item-subtitle>
      </v-list-item>
      <v-list-item class="ms-4 my-n2" dense>
        <v-list-item-subtitle class="text-color">{{ $t('deedCharacteristicsScarcity') }}: {{ cardScarcity }}</v-list-item-subtitle>
      </v-list-item>
      <v-list-item class="ms-4 my-n2" dense>
        <v-list-item-subtitle class="text-color">{{ $t('deedCharacteristicsPowerMinting') }}: {{ cardPowerMinting }}</v-list-item-subtitle>
      </v-list-item>
      <v-list-item class="ms-4 my-n2" dense>
        <v-list-item-subtitle class="text-color">{{ $t('deedCharacteristicsMaxUsers') }}: {{ cardMaxUsers }}</v-list-item-subtitle>
      </v-list-item>
      <v-list-item class="ms-4 my-n2" dense>
        <v-list-item-subtitle class="text-color">{{ $t('deedCharacteristicsCityVotingRights') }}: {{ cityVotingRights }}</v-list-item-subtitle>
      </v-list-item>
      <v-list-item>
        <v-list-item-content>
          <v-list-item-title class="font-weight-bold">{{ $t('deedTenantStatus') }}</v-list-item-title>
        </v-list-item-content>
      </v-list-item>
      <v-list-item class="ms-4 my-n2" dense>
        <v-list-item-subtitle class="text-color">{{ $t('deedTenantStartedOn') }}: {{ cityStartDateFormatted }}</v-list-item-subtitle>
      </v-list-item>
      <template v-if="isProvisioningManager && authenticated">
        <v-list-item>
          <v-list-item-content>
            <v-list-item-title class="font-weight-bold">{{ $t('deedTenantAccess') }}</v-list-item-title>
          </v-list-item-content>
        </v-list-item>
        <v-list-item
          class="ms-4"
          two-line
          dense>
          <v-list-item-content>
            <v-list-item-subtitle v-if="stopped" class="text-color">{{ $t('deedMoveInDescription') }}</v-list-item-subtitle>
            <v-list-item-subtitle v-else-if="started" class="text-color">{{ $t('deedMoveOutDescription') }}</v-list-item-subtitle>
          </v-list-item-content>
          <v-list-item-action>
            <v-btn
              v-if="started"
              :min-width="minButtonsWidth"
              class="ms-auto"
              color="tertiary"
              depressed
              dark
              @click="$root.$emit('deeds-move-out-drawer')">
              <span class="text-capitalize">{{ $t('moveOut') }}</span>
            </v-btn>
            <v-btn
              v-else-if="stopped"
              :min-width="minButtonsWidth"
              class="ms-auto"
              color="tertiary"
              depressed
              dark
              @click="$root.$emit('deeds-move-in-drawer')">
              <span class="text-capitalize">{{ $t('moveIn') }}</span>
            </v-btn>
          </v-list-item-action>
        </v-list-item>
        <v-list-item
          class="ms-4"
          two-line
          dense>
          <v-list-item-content>
            <v-list-item-subtitle class="text-color">{{ $t('deedSellDescription') }}</v-list-item-subtitle>
          </v-list-item-content>
          <v-list-item-action>
            <v-btn
              :href="`${openSeaBaseLink}/${nftId}/sell`"
              :min-width="minButtonsWidth"
              target="_blank"
              rel="nofollow noreferrer noopener"
              class="ms-auto"
              outlined
              text>
              <span class="text-capitalize">{{ $t('deedSellButton') }}</span>
            </v-btn>
          </v-list-item-action>
        </v-list-item>
      </template>
    </template>
    <template v-if="isProvisioningManager" #footer>
      <v-btn
        v-if="authenticated"
        :min-width="minButtonsWidth"
        class="ms-auto"
        text
        outlined
        v-bind="attrs"
        v-on="on"
        @click="logout">
        <span class="text-capitalize">{{ $t('signOut') }}</span>
      </v-btn>
      <v-tooltip v-else top>
        <template #activator="{ on, attrs }">
          <v-btn
            :min-width="minButtonsWidth"
            color="tertiary"
            depressed
            dark
            class="ms-auto"
            v-bind="attrs"
            v-on="on"
            @click="login">
            <span class="text-capitalize">{{ $t('signIn') }}</span>
          </v-btn>
        </template>
        <span>{{ $t('authenticateToCommandTenantTooltip') }}</span>
      </v-tooltip>
    </template>
  </deeds-drawer>
</template>
<script>
export default {
  props: {
    secondLevelOpened: {
      type: Boolean,
      default: false,
    },
  },
  data: () => ({
    nft: null,
    authenticated: false,
    deedInfo: null,
    minButtonsWidth: 120,
  }),
  computed: Vuex.mapState({
    parentLocation: state => state.parentLocation,
    address: state => state.address,
    language: state => state.language,
    provider: state => state.provider,
    openSeaBaseLink: state => state.openSeaBaseLink,
    tenantProvisioningContract: state => state.tenantProvisioningContract,
    startTenantGasLimit: state => state.startTenantGasLimit,
    cardTypeInfos: state => state.cardTypeInfos,
    cardTypeInfo() {
      return this.cardTypeInfos[this.cardType];
    },
    nftId() {
      return this.nft?.id;
    },
    cardType() {
      return this.nft?.cardType;
    },
    cardTypeName() {
      return this.nft?.cardTypeName;
    },
    cardImage() {
      return this.cardTypeInfo?.image;
    },
    cardDescription() {
      return this.cardTypeInfo?.description;
    },
    cardTraits() {
      return this.cardTypeInfo?.attributes || [];
    },
    deedTraits() {
      return this.deedInfo?.attributes || [];
    },
    cardMaxUsers() {
      return this.cardTraits.find(attr => attr.trait_type === 'Max users')?.value || '-';
    },
    cardPowerMinting() {
      return this.cardTraits.find(attr => attr.trait_type === 'Minting Power')?.value || '-';
    },
    cardScarcity() {
      return this.cardTypeName;
    },
    cityVotingRights() {
      return this.cardTraits.find(attr => attr.trait_type === 'City voting rights')?.value || '-';
    },
    cityStartDate() {
      return this.deedTraits.find(attr => attr.trait_type === 'Tenant start date')?.value || 0;
    },
    cityStartDateFormatted() {
      return this.loading && '-'
        || this.started && this.cityStartDate && this.$ethUtils.formatDate(new Date(this.cityStartDate * 1000), this.language)
        || this.$t('deedTenantNotStartedYet');
    },
    cityName() {
      return this.nft?.cityName;
    },
    isProvisioningManager() {
      return this.nft?.provisioningManager;
    },
    loading() {
      return !this.nft?.status || this.nft.status === 'loading';
    },
    stopped() {
      return !this.nft?.status || this.nft.status === 'STOPPED';
    },
    started() {
      return !this.nft?.status || this.nft.status === 'STARTED';
    },
  }),
  created() {
    this.$root.$on('deeds-manage-drawer', this.open);
    this.$root.$on('deeds-manage-drawer-close', this.close);

    this.refreshAuthentication();
  },
  methods: {
    open(nft) {
      this.nft = nft;
      this.transactionHash = null;
      this.deedInfo = null;
      this.sending = false;
      this.isEditingEmail = false;
      this.emailChanged = false;
      this.$store.commit('loadCardInfo', this.cardType);
      this.$deedMetadata.getNftInfo(this.nftId)
        .then(deedInfo => this.deedInfo = deedInfo);
      this.$nextTick()
        .then(() => {
          if (this.$refs.drawer) {
            this.$refs.drawer.open();
          }
        });
    },
    close(nftId) {
      if (nftId === this.nftId) {
        this.$refs.drawer.close();
      }
    },
    logout() {
      this.$authentication.logout(this.address)
        .finally(() => this.refreshAuthentication());
    },
    login() {
      const token = document.querySelector('[name=loginMessage]').value;
      const message = this.$t('signMessage', {0: token});
      this.$ethUtils.signMessage(this.provider, this.address, message)
        .then(signedMessage => this.$authentication.login(this.address, message, signedMessage))
        .finally(() => this.refreshAuthentication());
    },
    refreshAuthentication() {
      this.authenticated = this.$authentication.isAuthenticated(this.address);
    },
  },
};
</script>