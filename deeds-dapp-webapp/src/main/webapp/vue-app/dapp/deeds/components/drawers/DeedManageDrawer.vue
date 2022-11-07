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
    v-model="drawer"
    :first-level="secondLevelOpened"
    @opened="$emit('opened')"
    @closed="$emit('closed')">
    <template #title>
      <h4>{{ $t('manageDeedTitle', {0: cardTypeName, 1: nftId}) }}</h4>
    </template>
    <template v-if="cardTypeInfo" #content>
      <v-list>
        <v-list-item two-line>
          <v-list-item-avatar>
            <v-img :src="cardImage" />
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
          <v-list-item-subtitle class="text-color">{{ $t('deedTenantStartedOn') }}: {{ deedTenantStartDate }}</v-list-item-subtitle>
        </v-list-item>
        <template v-if="showAccessPart">
          <v-list-item>
            <v-list-item-content>
              <v-list-item-title class="font-weight-bold">{{ $t('deedTenantAccess') }}</v-list-item-title>
            </v-list-item-content>
          </v-list-item>
          <v-list-item
            v-if="showDeedLinkPart"
            class="ms-4"
            two-line
            dense>
            <v-list-item-content>
              <v-list-item-subtitle
                :title="$t('deedMoveInDescription')"
                class="text-color text-truncate-2">
                {{ $t('deedAccessDescription') }}
              </v-list-item-subtitle>
            </v-list-item-content>
            <v-list-item-action>
              <v-btn
                :href="deedTenantLink"
                :min-width="minButtonsWidth"
                target="_blank"
                rel="nofollow noreferrer noopener"
                class="ms-auto"
                color="primary"
                depressed
                dark>
                <span class="text-capitalize">{{ $t('deedTenantAccessButton') }}</span>
              </v-btn>
            </v-list-item-action>
          </v-list-item>
          <v-list-item
            v-if="showMovePart"
            class="ms-4"
            two-line
            dense>
            <v-list-item-content>
              <v-list-item-subtitle
                v-if="stopped"
                :title="$t('deedMoveInDescription')"
                class="text-color text-truncate-2">
                {{ $t('deedMoveInDescription') }}
              </v-list-item-subtitle>
              <v-list-item-subtitle
                v-else-if="started"
                :title="$t('deedMoveOutDescription')"
                class="text-color text-truncate-2">
                {{ $t('deedMoveOutDescription') }}
              </v-list-item-subtitle>
            </v-list-item-content>
            <v-list-item-action>
              <v-btn
                v-if="started"
                :loading="loadingMoveDeed"
                :min-width="minButtonsWidth"
                class="ms-auto"
                color="secondary"
                outlined
                depressed
                dark
                @click="openMoveOutDrawer">
                <span class="text-capitalize">{{ $t('moveOut') }}</span>
              </v-btn>
              <v-btn
                v-else-if="stopped"
                :loading="loadingMoveDeed"
                :min-width="minButtonsWidth"
                class="ms-auto"
                color="primary"
                depressed
                dark
                @click="openMoveInDrawer">
                <span class="text-capitalize">{{ $t('moveIn') }}</span>
              </v-btn>
            </v-list-item-action>
          </v-list-item>
          <v-list-item
            v-if="showRentPart"
            class="ms-4"
            two-line
            dense>
            <v-list-item-content>
              <v-list-item-subtitle
                :title="rentingDescription"
                class="text-color text-truncate-2">
                {{ rentingDescription }}
              </v-list-item-subtitle>
            </v-list-item-content>
            <v-list-item-action>
              <v-btn
                :disabled="!isRentingEnabled"
                :loading="loadingRentDrawer"
                :min-width="minButtonsWidth"
                class="ms-auto"
                color="primary"
                depressed
                outlined
                @click="openRentDrawer">
                <span class="text-capitalize">{{ $t('deedRentButton') }}</span>
              </v-btn>
            </v-list-item-action>
          </v-list-item>
          <v-list-item
            v-if="showSellPart"
            class="ms-4"
            two-line
            dense>
            <v-list-item-content>
              <v-list-item-subtitle
                :title="$t('deedSellDescription')"
                class="text-color text-truncate-2">
                {{ $t('deedSellDescription') }}
              </v-list-item-subtitle>
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
      </v-list>
      <v-spacer />
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
            color="primary"
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
    value: {
      type: Boolean,
      default: false,
    },
  },
  data: () => ({
    nft: null,
    drawer: false,
    deedInfo: null,
    minButtonsWidth: 120,
    loadingMoveDeed: false,
    loadingRentDrawer: false,
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
    authenticated() {
      return this.value;
    },
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
    deedTenantDate() {
      return this.deedInfo?.provisioningDate;
    },
    deedTenantStatus() {
      return this.deedInfo?.tenantStatus;
    },
    provisioningStatus() {
      return this.deedInfo?.provisioningStatus;
    },
    deedTenantStartDate() {
      return this.loading && '-'
        || this.started && this.deedTenantDate && (!this.authenticated || this.deedTenantStatus === 'DEPLOYED') && this.$ethUtils.formatDate(new Date(this.deedTenantDate * 1000), this.language)
        || this.$t('deedTenantNotStartedYet');
    },
    cityName() {
      return this.nft?.cityName;
    },
    isProvisioningManager() {
      return this.nft?.provisioningManager;
    },
    isOwner() {
      return this.nft?.owner;
    },
    status() {
      return this.nft?.status;
    },
    loading() {
      return this.status === 'loading' || this.provisioningStatus === 'PROVISIONING_STOP_IN_PROGRESS' || this.provisioningStatus === 'PROVISIONING_START_IN_PROGRESS';
    },
    stopped() {
      return !this.loading && this.status === 'STOPPED';
    },
    started() {
      return !this.loading && this.status === 'STARTED';
    },
    rented() {
      return this.isOwner && !this.isProvisioningManager;
    },
    showSellPart() {
      return this.authenticated && this.isOwner;
    },
    showMovePart() {
      return this.authenticated && this.isProvisioningManager && (this.started || this.stopped);
    },
    showRentPart() {
      return this.authenticated && this.isOwner;
    },
    isRentingEnabled() {
      return this.showRentPart && !this.started && !this.loading;
    },
    rentingDescription() {
      return this.isRentingEnabled && this.$t('deedRentEnabledDescription') ||  this.$t('deedRentDisabledDescription');
    },
    showDeedLinkPart() {
      return this.showMovePart && this.started;
    },
    showAccessPart() {
      return this.showSellPart || this.showMovePart;
    },
    deedTenantLink() {
      return `https://${this.cityName}-${this.nftId}.meeds.io`;
    },
  }),
  watch: {
    status(newVal, oldVal) {
      if (this.drawer && oldVal !== newVal) {
        this.refreshDeedInfo();
      }
    },
  },
  created() {
    this.$root.$on('deeds-manage-drawer', this.open);
    this.$root.$on('deeds-manage-drawer-close', this.close);
    this.$root.$on('deed-nft-updated', this.refreshNft);

    this.refreshAuthentication();
  },
  methods: {
    refreshNft(nft) {
      if (this.drawer && this.nftId === nft.id) {
        this.nft = Object.assign({}, nft);
      }
    },
    refreshDeedInfo() {
      if (this.authenticated) {
        return this.$tenantManagement.getTenantInfo(this.nftId)
          .then(deedInfo => this.deedInfo = deedInfo)
          .catch(() => {
            return this.logout()
              .then(() => this.$tenantManagement.getTenantStartDate(this.nftId))
              .then(startDate => this.deedInfo = {provisioningDate: startDate});
          });
      } else {
        return this.$tenantManagement.getTenantStartDate(this.nftId)
          .then(startDate => this.deedInfo = {provisioningDate: startDate});
      }
    },
    openMoveOutDrawer() {
      this.loadingMoveDeed = true;
      this.openDrawer(false, 'deeds-move-out-drawer')
        .finally(() => this.loadingMoveDeed = false);
    },
    openMoveInDrawer() {
      this.loadingMoveDeed = true;
      this.openDrawer(true, 'deeds-move-in-drawer')
        .finally(() => this.loadingMoveDeed = false);
    },
    openRentDrawer() {
      this.loadingRentDrawer = true;
      this.openDrawer(true, 'deeds-rent-drawer')
        .finally(() => this.loadingRentDrawer = false);
    },
    openDrawer(sendNft, eventName) {
      return this.refreshDeedInfo()
        .then(() => this.$nextTick())
        .then(() => {
          if (this.authenticated && this.nft?.id) {
            if (sendNft) {
              this.$root.$emit(eventName, this.nft);
            } else {
              this.$root.$emit(eventName, this.nft.id);
            }
          } else {
            this.$root.$emit('alert-message', this.$t('loggedOutPleaseLoginAgain'), 'warning');
          }
        });
    },
    open(nft) {
      this.nft = nft;
      this.transactionHash = null;
      this.deedInfo = null;
      this.sending = false;
      this.$store.commit('loadCardInfo', this.cardType);
      this.refreshDeedInfo()
        .then(() => this.$nextTick())
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
      return this.$authentication.logout(this.address)
        .finally(() => this.refreshAuthentication())
        .then(() => this.refreshDeedInfo());
    },
    login() {
      const token = document.querySelector('[name=loginMessage]').value;
      const message = this.$t('signMessage', {0: token}).replace(/\\n/g, '\n');
      this.$root.$emit('close-alert-message');
      return this.$ethUtils.signMessage(this.provider, this.address, message)
        .then(signedMessage => this.$authentication.login(this.address, message, signedMessage))
        .finally(() => this.refreshAuthentication())
        .then(() => this.refreshDeedInfo());
    },
    refreshAuthentication() {
      this.$emit('input', this.$authentication.isAuthenticated(this.address));
    },
  },
};
</script>