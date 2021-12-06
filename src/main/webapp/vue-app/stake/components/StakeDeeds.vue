<template>
  <v-card flat>
    <v-card-title class="ps-0">
      {{ $t('deeds') }}
    </v-card-title>
    <v-card-text class="ps-0">
      {{ $t('deedsIntroduction') }}
    </v-card-text>
    <v-card-actions class="ps-0 justify-center">
      <v-btn
        outlined
        text
        @click="$root.$emit('switch-page', 'deeds')">
        <span class="text-none">{{ $t('myDeeds') }}</span>
      </v-btn>
    </v-card-actions>
  </v-card>
</template>