<template>
  <v-card-title class="d-flex ps-0 py-2">
    <deeds-lease-card-image
      :city="city"
      :card-type="cardType"
      class="mt-0 hidden-md-and-up" />
    <span class="text-capitalize">
      {{ $t('deedRentalNftTypeTitle', {
        0: cardType,
        1: nftId
      }) }}
    </span>
  </v-card-title>
</template>
<script>
export default {
  props: {
    nftId: {
      type: Number,
      default: null,
    },
    city: {
      type: String,
      default: null,
    },
    cardType: {
      type: String,
      default: null,
    },
  },
};
</script>