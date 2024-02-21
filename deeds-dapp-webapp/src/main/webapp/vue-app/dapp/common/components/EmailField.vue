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
  <v-form ref="form" @submit.stop.prevent="$emit('submit', $event)">
    <v-text-field
      ref="email"
      v-model="email"
      :placeholder="placeholder"
      :loading="emailLoading"
      :readonly="readonly"
      :disabled="disabledField"
      :autofocus="autofocus"
      :prepend-inner-icon="emailPrependIcon"
      :class="extraClass"
      :rules="isValid"
      name="email"
      type="email"
      autocomplete="off"
      class="align-center"
      hide-details
      large
      outlined
      dense
      required
      @focus="edit"
      @blur="cancelEdit">
      <template #append>
        <v-slide-x-reverse-transition mode="out-in">
          <v-icon :key="emailAppendIcon" :color="emailAppendIconColor">
            {{ emailAppendIcon }}
          </v-icon>
        </v-slide-x-reverse-transition>
      </template>
    </v-text-field>
    <div v-if="emailConfirmationNeeded">
      <div class="caption font-italic">{{ $t('deedEmailConfirmationSentTitle') }}</div>
      <v-text-field
        ref="emailCode"
        v-model="code"
        :placeholder="$t('deedEmailConfirmationPlaceHolder')"
        :disabled="validCode"
        :readonly="sendingCode"
        :loading="sendingCode"
        :autofocus="!isMobile"
        :prepend-inner-icon="codePrependIcon"
        :class="extraClass"
        name="emailCode"
        maxlength="6"
        class="align-center"
        autocomplete="off"
        hide-details
        large
        outlined
        dense
        mandatory />
    </div>
  </v-form>
</template>
<script>
export default {
  props: {
    value: {
      type: String,
      default: null,
    },
    placeholder: {
      type: String,
      default: null,
    },
    readonly: {
      type: Boolean,
      default: false,
    },
    disabled: {
      type: Boolean,
      default: false,
    },
    reset: {
      type: Boolean,
      default: false,
    },
    extraClass: {
      type: String,
      default: null
    },
    emailPrependIcon: {
      type: String,
      default: null
    },
    codePrependIcon: {
      type: String,
      default: null
    },
    autofocus: {
      type: Boolean,
      default: false,
    },
    anonymous: {
      type: Boolean,
      default: false,
    }
  },
  data: () => ({
    email: null,
    code: null,
    isEditing: false,
    emailConfirmationNeeded: false,
    sending: false,
    sendingCode: false,
    validCode: false,
    emailLoading: false,
    validEmailCheckIndex: 1,
  }),
  computed: Vuex.mapState({
    isMobile: state => state.isMobile,
    disabledField() {
      return this.disabled || this.sending || this.emailConfirmationNeeded;
    },
    validEmail() {
      return this.validEmailCheckIndex && this.email?.trim().length && this.isValid();
    },
    validForm() {
      return this.validEmail && (!this.emailConfirmationNeeded || this.validCode);
    },
    emailAppendDisplay() {
      return this.email && !this.isEditing;
    },
    emailAppendIcon() {
      return !this.emailAppendDisplay && ' ' || this.validEmail && 'fas fa-check' || 'fas fa-xmark';
    },
    emailAppendIconColor() {
      return this.validEmail && 'success' || 'error';
    },
  }),
  watch: {
    code() {
      if (this.code && Number.isInteger(Number(this.code)) && Number.isFinite(Number(this.code)) && this.code.length === 6) {
        this.sendingCode = true;
        this.$authorizationCodeService.isCodeValid(this.code, this.email)
          .then(() => this.validCode = true)
          .catch(() => this.validCode = false)
          .finally(() => this.sendingCode = false);
      }
    },
    sendingCode() {
      if (!this.sendingCode && this.code) {
        if (this.validCode) {
          this.$emit('email-confirmation-success', this.code);
          this.$root.$emit('alert-message',
            this.$t('deedEmailConfirmationSuccess'),
            'success');
        } else {
          this.$emit('email-confirmation-error', this.code);
          this.$root.$emit('alert-message',
            this.$t('deedEmailConfirmationError'),
            'error');
        }
      }
    },
    email() {
      this.$emit('input', this.email);
    },
    validEmail() {
      this.$emit('valid-email', this.validForm);
    },
    sending() {
      this.$emit('loading', this.sending);
    },
    reset() {
      if (this.reset) {
        this.resetForm();
      }
    },
  },
  created() {
    this.resetForm();
  },
  methods: {
    resetForm() {
      this.email = this.value;
      this.isEditing = false;
      this.code = null;
      this.emailConfirmationNeeded = false;
      this.sending = false;
      this.sendingCode = false;
      this.validCode = false;

      if (!this.anonymous) {
        this.loadWalletEmail();
      }
    },
    loadWalletEmail() {
      this.emailLoading = true;
      this.$userProfileService.getEmail()
        .then(email => {
          this.email = email;
          this.$nextTick().then(() => this.validEmailCheckIndex++);
        })
        .catch(() => this.validEmailCheckIndex++)
        .finally(() => {
          this.emailLoading = false;
          this.$emit('email-found', !!this.email);
        });
    },
    edit() {
      this.isEditing = true;
    },
    cancelEdit() {
      this.isEditing = false;
      this.$refs.form?.$el.reportValidity();
    },
    isValid() {
      const pattern = /^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[a-z]{2,4}$/;
      return this.email?.trim().length && pattern.test(this.email); 
    },
    sendConfirmation() {
      if (!this.email?.length) {
        this.$refs.form?.$el.reportValidity();
        this.$root.$emit('alert-message', this.$t('deedEmailConfirmMandatory'), 'warning');
        return this.$nextTick();
      }
      this.sending = true;
      this.$nextTick()
        .then(() => this.$authorizationCodeService.sendEmailConfirmation(this.email))
        .then(() => {
          this.emailConfirmationNeeded = true;
          this.$root.$emit('alert-message',
            this.$t('deedEmailConfirmationSent'),
            'info');
          return this.$nextTick();
        })
        .then(() => {
          if (this.$refs.emailCode) {
            this.$refs.emailCode.$el.scrollIntoView({
              block: 'start',
            });
          }
        })
        .catch(() => {
          this.$root.$emit('alert-message',
            this.$t('deedEmailConfirmationSendingMaxReached'),
            'error');
        })
        .finally(() => this.sending = false);
    },
  },
};
</script>