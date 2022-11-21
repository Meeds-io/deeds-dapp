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
    @opened="$emit('opened')"
    @closed="$emit('closed')">
    <template #title>
      <h4 class="text-capitalize">{{ $t('manageDeedTitle', {0: cardName, 1: nftId}) }}</h4>
    </template>
    <template v-if="cardTypeInfo" #content>
      <v-list>
        <v-list-item two-line>
          <v-list-item-avatar class="deed-avatar">
            <v-img :src="cardImage" />
          </v-list-item-avatar>
          <v-list-item-content>
            <v-list-item-title class="font-weight-bold">{{ $t('deedName') }}:</v-list-item-title>
            <v-list-item-subtitle class="text-truncate text-capitalize">{{ cardName }} #{{ nftId }}</v-list-item-subtitle>
          </v-list-item-content>
        </v-list-item>
        <v-list-item>
          <v-list-item-content>
            <v-list-item-title class="font-weight-bold">{{ $t('deedCharacteristics') }}</v-list-item-title>
          </v-list-item-content>
        </v-list-item>
        <v-list-item class="ms-4 my-n2" dense>
          <v-list-item-subtitle class="text-color text-capitalize">{{ $t('deedCharacteristicsCity') }}: {{ cityName }}</v-list-item-subtitle>
        </v-list-item>
        <v-list-item class="ms-4 my-n2" dense>
          <v-list-item-subtitle class="text-color text-capitalize">{{ $t('deedCharacteristicsScarcity') }}: {{ cardScarcity }}</v-list-item-subtitle>
        </v-list-item>
        <v-list-item class="ms-4 my-n2" dense>
          <v-list-item-subtitle class="text-color">{{ $t('deedCharacteristicsPowerMinting') }}: {{ cardMintingPower }}</v-list-item-subtitle>
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
                :title="$t('deedAccessDescription')"
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
                <v-icon class="me-2" small>fa-external-link</v-icon>
                {{ $t('deedTenantAccessButton') }}
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
                :title="moveInDescription"
                class="text-color text-truncate-2">
                {{ moveInDescription }}
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
                {{ $t('moveOut') }}
              </v-btn>
              <v-btn
                v-else-if="stopped"
                :disabled="hasRentOffers"
                :loading="loadingMoveDeed"
                :min-width="minButtonsWidth"
                :outlined="hasRentOffers"
                class="ms-auto"
                color="primary"
                depressed
                dark
                @click="openMoveInDrawer">
                {{ $t('moveIn') }}
              </v-btn>
            </v-list-item-action>
          </v-list-item>
          <v-list-item
            v-if="showSeeRentPart"
            class="ms-4"
            two-line
            dense>
            <v-list-item-content>
              <v-list-item-subtitle
                :title="$t('deedRentSeeRentDescription')"
                class="text-color text-truncate-2">
                {{ $t('deedRentSeeRentDescription') }}
              </v-list-item-subtitle>
            </v-list-item-content>
            <v-list-item-action>
              <v-btn
                :min-width="minButtonsWidth"
                class="ms-auto"
                color="primary"
                outlined
                depressed
                @click="openRentDetails">
                {{ $t('deedRentSeeRentButton') }}
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
                :class="hasOnlyExpiredOffers && 'error--text'"
                class="text-truncate-2">
                {{ rentingDescription }}
              </v-list-item-subtitle>
            </v-list-item-content>
            <v-list-item-action>
              <v-btn
                :disabled="!isRentingEnabled"
                :loading="loadingRentDrawer"
                :min-width="minButtonsWidth"
                :outlined="!hasRentOffers"
                :dark="hasRentOffers"
                class="ms-auto"
                color="primary"
                depressed
                @click="openRentDrawer">
                {{ rentingButtonName }}
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
                <v-icon class="me-2" small>fa-external-link</v-icon>
                {{ $t('deedSellButton') }}
              </v-btn>
            </v-list-item-action>
          </v-list-item>
        </template>
      </v-list>
      <v-spacer />
    </template>
    <template v-if="isProvisioningManager || isOwner" #footer>
      <deeds-login-button
        ref="loginButton"
        :login-tooltip="$t('authenticateToCommandTenantTooltip')"
        @logged-in="refreshDeedInfo"
        @logged-out="refreshDeedInfo" />
    </template>
  </deeds-drawer>
</template>
<script>
export default {
  data: () => ({
    nft: null,
    drawer: false,
    deedInfo: null,
    minButtonsWidth: 120,
    loadingMoveDeed: false,
    loadingRentDrawer: false,
    loadingRentalOffers: false,
    index: 1,
    isProvisioningManager: false,
    rentalOffers: null,
    location: 'manage-deed-drawer',
  }),
  computed: Vuex.mapState({
    parentLocation: state => state.parentLocation,
    address: state => state.address,
    networkId: state => state.networkId,
    language: state => state.language,
    openSeaBaseLink: state => state.openSeaBaseLink,
    cardTypeInfos: state => state.cardTypeInfos,
    authenticated: state => state.authenticated,
    tenantProvisioningContract: state => state.tenantProvisioningContract,
    now: state => state.now,
    cardTypeInfo() {
      return this.cardTypeInfos[this.cardType];
    },
    nftId() {
      return this.nft?.id;
    },
    cardType() {
      return this.nft?.cardType;
    },
    cardName() {
      return this.nft?.cardName;
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
    cardMintingPower() {
      return this.cardTraits.find(attr => attr.trait_type === 'Minting Power')?.value || '-';
    },
    cardScarcity() {
      return this.cardName;
    },
    cityVotingRights() {
      return this.cardTraits.find(attr => attr.trait_type === 'City voting rights')?.value || '-';
    },
    deedTenantDate() {
      return this.deedInfo?.provisioningDate;
    },
    deedTenantStatus() {
      return this.deedInfo?.status;
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
    isOwner() {
      return this.nft?.owner;
    },
    status() {
      return this.index && this.nft?.status;
    },
    loading() {
      return  this.status === 'loading' || this.provisioningStatus === 'PROVISIONING_STOP_IN_PROGRESS' || this.provisioningStatus === 'PROVISIONING_START_IN_PROGRESS';
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
      return this.authenticated && this.isOwner && !this.rented && (!this.rentalOffer || this.rentalOffer.offerId);
    },
    showSeeRentPart() {
      return this.authenticated && this.isOwner && !this.rented && this.hasAvailableRentOffers;
    },
    hasRentOffers() {
      return !!this.rentalOffers?.length;
    },
    availableRentOffers() {
      return this.rentalOffers?.filter(offer => !offer.expirationDate || new Date(offer.expirationDate).getTime() > this.now);
    },
    hasAvailableRentOffers() {
      return !!this.availableRentOffers?.length;
    },
    hasOnlyExpiredOffers() {
      return this.hasRentOffers && !this.hasAvailableRentOffers;
    },
    rentalOffer() {
      return this.hasAvailableRentOffers && this.availableRentOffers[0]
        || (this.hasRentOffers && this.rentalOffers[0]);
    },
    isRentingEnabled() {
      return this.showRentPart && !this.started && !this.loading && this.isProvisioningManager;
    },
    moveInDescription() {
      return this.hasRentOffers
        && this.$t('deedMoveInDisabledDescription')
        || this.$t('deedMoveInDescription');
    },
    rentingDescription() {
      return this.hasOnlyExpiredOffers
        && this.$t('deedRentOfferExpired')
        || (this.hasRentOffers
        && this.$t('deedRentEditDescription')
        || (this.isRentingEnabled
            && this.$t('deedRentEnabledDescription')
            ||  this.$t('deedRentDisabledDescription')));
    },
    rentingButtonName() {
      return this.hasRentOffers
        && this.$t('deedRentEditButton')
        || this.$t('deedRentButton');
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
    loadingRentalOffers() {
      this.loadingRentDrawer = this.loadingRentalOffers;
    },
    nft: {
      handler() {
        this.index++;
      },
      deep: true
    },
  },
  created() {
    this.$root.$on('deeds-manage-drawer', this.open);
    this.$root.$on('deeds-manage-drawer-close', this.close);
    this.$root.$on('deed-nft-updated', this.refreshNft);
    this.$root.$on('deed-offer-renting-created', this.refreshOffers);
    this.$root.$on('deed-offer-renting-updated', this.refreshOffers);
    this.$root.$on('deed-offer-renting-deleted', this.refreshOffers);
  },
  beforeDestroy() {
    this.$root.$off('deeds-manage-drawer', this.open);
    this.$root.$off('deeds-manage-drawer-close', this.close);
    this.$root.$off('deed-nft-updated', this.refreshNft);
    this.$root.$off('deed-offer-renting-created', this.refreshOffers);
    this.$root.$off('deed-offer-renting-updated', this.refreshOffers);
    this.$root.$off('deed-offer-renting-deleted', this.refreshOffers);
  },
  methods: {
    refreshNft(nft) {
      if (this.drawer && this.nftId === nft?.id) {
        this.nft = Object.assign({}, nft);
      }
    },
    refreshOffers() {
      this.loadingRentalOffers = true;
      return this.$deedTenantOfferService.getOffers({
        nftId: this.nftId,
        address: this.address,
        onlyOwned: true,
        excludeExpired: true,
        excludeNotStarted: false,
      }, this.networkId)
        .then(offers => this.rentalOffers = offers?._embedded?.offers)
        .finally(() => this.loadingRentalOffers = false);
    },
    refreshDeedInfo() {
      if (this.authenticated) {
        return this.$tenantManagement.getTenantInfo(this.nftId)
          .then(deedInfo => {
            this.deedInfo = deedInfo;
          })
          .catch(() => {
            if (this.$refs.loginButton) {
              return this.$refs.loginButton.logout(true)
                .then(() => this.$tenantManagement.getTenantStartDate(this.nftId))
                .then(startDate => this.deedInfo = {provisioningDate: startDate});
            }
          })
          .then(() => this.refreshOffers())
          .then(() => this.refreshProvisioningManager());
      } else {
        return this.$tenantManagement.getTenantStartDate(this.nftId)
          .then(startDate => this.deedInfo = {provisioningDate: startDate});
      }
    },
    refreshProvisioningManager() {
      if (this.authenticated && this.nftId && this.address) {
        return this.tenantProvisioningContract.isProvisioningManager(this.address, this.nftId)
          .then(isManager => this.isProvisioningManager = isManager);
      }
    },
    openMoveOutDrawer() {
      this.loadingMoveDeed = true;
      this.openDrawer(false, 'deeds-move-out-drawer')
        .finally(() => this.loadingMoveDeed = false);
    },
    openMoveInDrawer() {
      if (this.hasRentOffers) {
        return;
      }
      this.loadingMoveDeed = true;
      this.openDrawer(true, 'deeds-move-in-drawer')
        .finally(() => this.loadingMoveDeed = false);
    },
    openRentDrawer() {
      this.loadingRentDrawer = true;
      this.openDrawer(false, 'deeds-rent-drawer', this.rentalOffer)
        .finally(() => this.loadingRentDrawer = false);
    },
    openDrawer(sendNft, eventName, obj) {
      return this.refreshDeedInfo()
        .finally(() =>
          this.$nextTick().then(() => {
            if (this.authenticated && this.nft?.id) {
              if (sendNft) {
                this.$root.$emit(eventName, this.nft, obj);
              } else {
                this.$root.$emit(eventName, this.nft.id, obj);
              }
            } else {
              this.$root.$emit('alert-message', this.$t('loggedOutPleaseLoginAgain'), 'warning');
            }
          })
        );
    },
    open(nft) {
      this.nft = nft;
      this.transactionHash = null;
      this.deedInfo = null;
      this.sending = false;
      this.$store.commit('loadCardInfo', this.cardType);
      this.refreshDeedInfo()
        .finally(() =>
          this.$nextTick().then(() => this.$refs.drawer?.open())
        );
    },
    close(nftId) {
      if (nftId === this.nftId) {
        this.$refs.drawer?.close();
      }
    },
    openRentDetails() {
      this.$store.commit('setStandaloneOfferId', this.rentalOffers[0].id);
      this.$root.$emit('switch-page', 'marketplace', true);
    },
  },
};
</script>