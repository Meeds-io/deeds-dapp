<template>
  <v-card flat>
    <h3 class="d-flex flex-nowrap">
      {{ $t('deedsToRedeem') }}
      <v-divider class="my-auto ms-4" />
    </h3>
    <v-card-text v-html="$t('deedsToRedeemIntroduction')" />
    <h4>{{ currentCityName }}</h4>
    <small>
      {{ $t('cityPopulation') }}:
      <v-chip class="ms-2">{{ currentCityPopulation }} / {{ currentCityMaxPopulation }}</v-chip>
    </small>
    <template v-if="currentCardTypes">
      <v-container class="grey lighten-5 mt-2">
        <v-row class="mx-auto" no-gutters>
          <v-col
            v-for="card in currentCardTypes"
            :key="card.name">
            <deeds-redeem-card :card="card" />
          </v-col>
        </v-row>
      </v-container>
    </template>
  </v-card>
</template>
<script>
export default {
  computed: Vuex.mapState({
    currentCity: state => state.currentCity,
    currentCardTypes: state => state.currentCardTypes,
    currentCityName() {
      return this.currentCity && this.currentCity.name;
    },
    currentCityPopulation() {
      return this.currentCity && this.currentCity.population;
    },
    currentCityMaxPopulation() {
      return this.currentCity && this.currentCity.maxPopulation;
    },
  }),
};
</script>