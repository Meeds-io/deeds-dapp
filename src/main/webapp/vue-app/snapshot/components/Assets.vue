<template>
  <v-card flat>
    <h3 class="d-flex flex-nowrap">
      {{ $t('yourAssets') }}
      <v-divider class="my-auto ms-4" />
    </h3>
    <deeds-token-assets />
    <deeds-deed-assets />
  </v-card>
</template>