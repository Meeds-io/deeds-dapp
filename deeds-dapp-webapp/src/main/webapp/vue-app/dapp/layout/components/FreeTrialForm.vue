<!--
 This file is part of the Meeds project (https://meeds.io/).
 
 Copyright (C) 2020 - 2023 Meeds Association contact@meeds.io
 
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
  <div class="d-flex flex-column flex-sm-row full-height">
    <v-card
      width="33%"
      height="100%"
      color="primary"
      class="d-none d-sm-flex"
      flat
      tile>
      <div class="d-flex flex-column align-center fill-height mx-auto">
        <span class="display-1 font-weight-bold white--text mt-auto">{{ $t('meeds.freeTrial.form.title') }}</span>
        <span class="text-h6 font-weight-normal white--text mb-auto">{{ $t('meeds.freeTrial.form.subtitle') }}</span>
      </div>
    </v-card>
    <v-card
      height="206px"
      color="primary"
      class="d-sm-none d-block"
      flat
      tile>
      <div class="d-flex flex-column align-center fill-height">
        <span class="display-1 font-weight-bold white--text ma-auto">{{ $t('meeds.freeTrial.form.title') }}</span>
      </div>
    </v-card>
    <v-card
      width="100%"
      flat
      tile>
      <v-card
        width="80%"
        height="100%"
        class="mx-auto d-flex flex-column"
        flat>
        <v-spacer />
        <div v-if="showConfirmationMessage" class="d-flex flex-column align-center my-7">
          <span class="display-1 primary--text mb-7">{{ $t('meeds.freeTrial.form.confirmation.title') }}</span>
          <span class="text-h6 font-weight-normal my-4">{{ $t('meeds.freeTrial.form.confirmation.descriptionPart1') }}</span>
          <span class="text-h6 font-weight-normal my-2">{{ $t('meeds.freeTrial.form.confirmation.descriptionPart2') }}</span>
          <span class="text-h6 font-weight-normal my-4">{{ $t('meeds.freeTrial.form.confirmation.descriptionPart3') }}</span>
          <v-btn
            :href="tourURL"
            class="primary px-5 my-7"
            height="48px"
            text
            outlined>
            <span class="font-weight-bold text-h6 white--text">{{ $t('meeds.freeTrial.form.learnMore') }}</span>
          </v-btn>
        </div>
        <v-form v-else ref="form">
          <div class="d-flex flex-column align-center my-7">
            <span class="display-1 primary--text mb-7">{{ $t('meeds.freeTrial.form.welcome') }}</span>
            <span class="text-h6 font-weight-normal my-6">
              {{ $t('meeds.freeTrial.form.descriptionPart1') }}
            </span>
            <span class="text-h6 font-weight-normal mb-3">
              {{ $t('meeds.freeTrial.form.descriptionPart2') }}
            </span>
          </div>
          <v-card
            :width="isSmallScreen && '80%' || '40%'"
            class="mx-auto"
            flat>
            <v-text-field
              :placeholder="$t('meeds.freeTrial.form.firstname.placeholder')"
              :rules="rules"
              v-model="firstname"
              type="text"
              prepend-inner-icon="fa-user fa-1x"
              class="body-2"
              autofocus
              dense
              outlined
              @keypress="isLetter($event)" />
            <v-text-field
              :placeholder="$t('meeds.freeTrial.form.name.placeholder')"
              :rules="rules"
              v-model="name"
              type="text"
              prepend-inner-icon="fa-user-tie fa-1x"
              class="body-2"
              dense
              outlined
              @keypress="isLetter($event)" />
            <v-text-field
              :rules="rules"
              :placeholder="$t('meeds.freeTrial.form.job.placeholder')"
              v-model="job"
              type="text"
              prepend-inner-icon="fa-user-graduate fa-1x"
              class="body-2"
              dense
              outlined
              @keypress="isLetter($event)" />
            <v-text-field
              :rules="rules"
              :placeholder="$t('meeds.freeTrial.form.organization.placeholder')"
              v-model="organization"
              type="text"
              prepend-inner-icon="fa-city fa-1x"
              class="body-2"
              dense
              outlined
              @keypress="isLetter($event)" />
            <deeds-email-field
              ref="email"
              v-model="email"
              :placeholder="$t('meeds.freeTrial.form.email.placeholder')"
              :readonly="sending"
              :disabled="disabledEmail"
              :code="emailCode"
              extra-class="body-2"
              email-prepend-icon="fa-envelope fa-1x"
              code-prepend-icon="fa-lock fa-1x"
              anonymous
              @valid-email="validEmail = $event"
              @email-confirmation-success="emailCode = $event"
              @email-confirmation-error="emailCodeError = true"
              @submit="confirmTrial" />
          </v-card>
          <div class="d-flex justify-center">
            <v-btn
              :disabled="disabledFormButton"
              :class="disabledFormButton && 'half-opacity'"
              class="primary px-5 my-5"
              height="48px"
              text
              outlined
              @click="confirmTrial">
              <span class="font-weight-bold text-h6 white--text">{{ trialButtonLabel }}</span>
            </v-btn>
          </div>
        </v-form>
        <v-spacer />
      </v-card>
    </v-card>
    <v-card />
  </div>
</template>
<script>
export default {
  data() {
    return {
      rules: [],
      name: null,
      firstname: null,
      job: null,
      organization: null,
      email: null,
      emailCode: null,
      emailCodeSent: false,
      emailCodeError: false,
      validEmail: false,
      showConfirmationMessage: false
    };
  },
  computed: Vuex.mapState({
    tourURL: state => state.tourURL,
    disabledFormButton() {
      return !this.name || !this.firstname || !this.job || !this.organization || !this.validEmail;
    },
    isSmallScreen() {
      return this.$vuetify.breakpoint.smAndDown;
    },
    confirmEmailStep() {
      return !this.emailCode;
    },
    trialButtonLabel() {
      return (this.confirmEmailStep && (this.emailCodeSent && this.$t('resend') || this.$t('meeds.freeTrial.form.button.submit'))) || this.$t('confirm');
    }
  }),
  created() {
    this.rules = [v => !!v || this.$t('meeds.freeTrial.form.valid.field.message')];
  },
  methods: {
    isLetter(e) {
      const char = String.fromCharCode(e.keyCode);
      if (/^[A-Za-z\s\W]+$/.test(char)) {
        return true;
      } else {
        e.preventDefault();
      }
    },
    sendEmailConfirmation() {
      this.$refs.email?.sendConfirmation();
      this.emailCodeSent = true;
    },
    confirmTrial() {
      if (this.confirmEmailStep) {
        this.sendEmailConfirmation();
      } else  {
        this.createTrial();
      } 
    },
    createTrial() {
      this.$trialService.saveTrial(this.firstname, this.name, this.job, this.organization, this.email, this.emailCode)
        .then(() => {
          this.showConfirmationMessage = true;
        })
        .catch(() => {
          this.$root.$emit('alert-message', this.$t('meeds.freeTrial.savingForm.error'), 'error');
        });
      this.emailCode = null;
      this.emailCodeSent = false;
    }
  },
};

</script>