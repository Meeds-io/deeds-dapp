<template>
  <v-card flat>
    <v-card-title class="ps-0">
      {{ $t('governance') }}
    </v-card-title>
    <v-card-text class="ps-0">
      {{ $t('governanceIntroduction') }}
    </v-card-text>
    <v-card-actions class="ps-0 justify-center">
      <v-btn
        outlined
        text
        class="me-4">
        <span class="text-none">{{ $t('discuss') }}</span>
      </v-btn>
      <v-btn
        outlined
        text
        class="ms-4">
        <span class="text-none">{{ $t('vote') }}</span>
      </v-btn>
    </v-card-actions>
  </v-card>
</template>