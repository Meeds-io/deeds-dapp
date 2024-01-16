<!--
 This file is part of the Meeds project (https://meeds.io/).
 
 Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io
 
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
    :permanent="sending"
    @opened="$emit('opened')"
    @closed="$emit('closed')">
    <template #title>
      <h4>{{ $t('deedsRentEvictTenantTitle', {0: cardTypeI18N, 1: nftId}) }}</h4>
    </template>
    <template v-if="lease" #content>
      <v-card color="transparent" flat>
        <v-card-text v-if="isDefaultPayment">
          {{ $t('deedsRentEndRentingDefaultPaymentParagraph') }}
        </v-card-text>
        <v-card-text v-else>
          {{ hasNoticePeriod && $t('deedsRentEndRentingWithNoticePeriodParagraph') || $t('deedsRentEndRentingNoNoticePeriodParagraph') }}
          <div class="d-flex my-4">
            <div class="flex-grow-1">
              {{ $t('deedRentNewEndDate') }}
            </div>
            <deeds-date-format :value="lastPaidDate" />
          </div>
        </v-card-text>
      </v-card>
    </template>
    <template #footer>
      <v-btn
        :disabled="sending"
        :min-width="minButtonsWidth"
        name="cancelEndRate"
        class="ms-auto me-2"
        outlined
        text
        @click="close">
        {{ $t('cancel') }}
      </v-btn>
      <v-btn
        :min-width="minButtonsWidth"
        :loading="sending"
        name="moveInConfirmButton"
        color="primary"
        class="primary"
        depressed
        dark
        @click="confirm">
        {{ $t('confirm') }}
      </v-btn>
    </template>
  </deeds-drawer>
</template>
<script>
export default {
  data: () => ({
    lease: null,
    drawer: false,
    sending: false,
    minButtonsWidth: 120,
  }),
  computed: Vuex.mapState({
    provider: state => state.provider,
    maxGasLimit: state => state.maxGasLimit,
    tenantRentingContract: state => state.tenantRentingContract,
    address: state => state.address,
    nftId() {
      return this.lease?.nftId;
    },
    leaseId() {
      return this.lease?.id;
    },
    cardType() {
      return this.lease?.cardType;
    },
    cardTypeI18N() {
      return this.cardType && this.$t(this.cardType.toLowerCase());
    },
    noticeDate() {
      return this.lease?.noticeDate;
    },
    paidRentsDate() {
      return this.lease?.paidRentsDate;
    },
    lastPaidDate() {
      return this.noticeDate && new Date(this.noticeDate)
        || (this.paidRentsDate && new Date(this.paidRentsDate));
    },
    isDefaultPayment() {
      return this.lastPaidDate && this.lastPaidDate.getTime() < Date.now();
    },
    hasNoticePeriod() {
      return this.lease?.noticePeriod && this.lease.noticePeriod !== 'NO_PERIOD';
    },
  }),
  created() {
    this.$root.$on('deed-evict-tenant-open', this.open);
  },
  beforeDestroy() {
    this.$root.$off('deed-evict-tenant-open', this.open);
  },
  methods: {
    open(lease) {
      this.lease = lease;
      this.$refs.drawer.open();
    },
    close() {
      this.$refs.drawer.close();
      this.lease = null;
    },
    confirm() {
      this.sending = true;
      const options = {
        from: this.address,
        gasLimit: this.maxGasLimit,
      };
      return this.$ethUtils.sendTransaction(
        this.provider,
        this.tenantRentingContract,
        'evictTenant',
        options,
        [this.leaseId]
      ).then(receipt => {
        const transactionHash = receipt?.hash;
        if (transactionHash) {
          this.$emit('transaction-sent', transactionHash);
          return this.confirmEndRenting(transactionHash)
            .finally(() => {
              this.sending = false;
              this.$nextTick().then(() => this.drawer = false);
            });
        } else {
          this.sending = false;
        }
      }).catch(() => this.sending = false);
    },
    confirmEndRenting(transactionHash) {
      return this.$deedTenantLeaseService.endLease(this.leaseId, transactionHash)
        .then(lease => this.$root.$emit('deed-lease-ended', lease))
        .catch(() => {
          this.$root.$emit('alert-message', this.$t('deedRentEndError'), 'error');
        });
    },
  },
};
</script>