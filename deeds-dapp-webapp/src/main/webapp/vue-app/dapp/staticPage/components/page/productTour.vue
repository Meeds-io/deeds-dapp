<template>
  <div class="d-flex flex-column mt-8 mt-sm-10">
    <v-card flat>
      <v-card-title class="py-0 mb-10 justify-center font-weight-bold dark-grey-color">{{ $t('productTour.title') }}</v-card-title>
    </v-card>
    <div class="d-flex flex-column flex-md-row py-4 mt-10">
      <div class="d-flex flex-column my-auto">
        <div class="ps-0 dark-grey-color font-weight-normal v-card__title">{{ $t('productTour.description') }}</div>
        <div class="d-flex justify-center">
          <v-btn
            href="./tenants"
            class="primary mx-5"
            min-width="250px"
            depressed
            dark
            x-large
            outlined>
            <span class="headline font-weight-black">{{ $t('startYourHub') }}</span>
          </v-btn>
          <v-btn
            href="https://builders.meeds.io"
            target="_blank"
            min-width="250px"
            class="dark-grey-border-color dark-grey-color d-flex justify-center mx-5" 
            x-large
            outlined>
            <span class="headline font-weight-black">{{ $t('exploreAHub') }}</span>
          </v-btn>
        </div>
      </div>
      <video
        class="mt-5 mt-md-0"
        width="611px"
        controls>
        <source :src="`${videoBuildersLink}`" type="video/mp4">
      </video>
    </div>
    <div>
      <div class="d-flex flex-column flex-md-row my-16">
        <div class="d-flex flex-column my-auto mx-0 mx-md-14">
          <span class="headline font-weight-black dark-grey-color">{{ $t('contributionPrograms.title') }}</span>
          <span class="dark-grey-color mt-5 mb-5 mb-md-0">{{ $t('contributionPrograms.description') }}</span>
        </div>
        <video
          class="mx-14"
          width="487px"
          controls>
          <source :src="`${videoBuildersLink}`" type="video/mp4">
        </video>
      </div>
      <div class="d-flex flex-column-reverse flex-md-row my-16">
        <video
          class="mx-14"
          width="487px"
          controls>
          <source :src="`${videoBuildersLink}`" type="video/mp4">
        </video>
        <div class="d-flex flex-column my-auto mx-0 mx-md-14">
          <span class="headline font-weight-black dark-grey-color">{{ $t('rewards.title') }}</span>
          <span class="dark-grey-color mt-5 mb-5 mb-md-0">{{ $t('rewards.description') }}</span>
        </div>
      </div>
      <div class="d-flex flex-column flex-md-row my-16">
        <div class="d-flex flex-column my-auto mx-0 mx-md-14">
          <span class="headline font-weight-black dark-grey-color">{{ $t('overview.title') }}</span>
          <span class="dark-grey-color mt-5 mb-5 mb-md-0">{{ $t('overview.description') }}</span>
        </div>
        <video
          class="mx-14"
          width="487px"
          controls>
          <source :src="`${videoBuildersLink}`" type="video/mp4">
        </video>
      </div>
      <div class="d-flex flex-column-reverse flex-md-row my-16">
        <video
          class="mx-14"
          width="487px"
          controls>
          <source :src="`${videoBuildersLink}`" type="video/mp4">
        </video>
        <div class="d-flex flex-column my-auto mx-0 mx-md-14">
          <span class="headline font-weight-black dark-grey-color">{{ $t('perks.title') }}</span>
          <span class="dark-grey-color mt-5 mb-5 mb-md-0">{{ $t('perks.description') }}</span>
        </div>
      </div>
      <div class="d-flex flex-column flex-md-row my-16">
        <div class="d-flex flex-column my-auto mx-0 mx-md-14">
          <span class="headline font-weight-black dark-grey-color">{{ $t('teamwork.title') }}</span>
          <span class="dark-grey-color mt-5 mb-5 mb-md-0">{{ $t('teamwork.description') }}</span>
        </div>
        <video
          class="mx-14"
          width="487px"
          controls>
          <source :src="`${videoBuildersLink}`" type="video/mp4">
        </video>
      </div>
    </div>
    <div class="light-grey-background ">
      <span class="headline d-flex justify-center font-weight-black dark-grey-color mt-4 mb-7">{{ $t('andMore') }}</span>
      <div class="d-flex flex-column flex-md-row mb-10">
        <div class="dark-grey-border-color dark-grey-color mx-5 my-4 my-md-0 rounded-lg">
          <div class="d-flex my-8 mx-5">
            <div>
              <v-icon size="75" color="primary">fas fa-award</v-icon>
            </div>
            <div class="d-flex flex-column ms-5">
              <div class="headline font-weight-black">
                {{ $t('kudos') }}
              </div>
              <div class="font-size-normal">
                {{ $t('kudos.description') }}
              </div>
            </div>
          </div>
        </div>
        <div class="dark-grey-border-color dark-grey-color mx-5 my-4 my-md-0 rounded-lg">
          <div class="d-flex my-8 mx-5">
            <div>
              <v-icon size="75" color="primary">fas fa-graduation-cap</v-icon>
            </div>
            <div class="d-flex flex-column ms-5">
              <div class="headline font-weight-black">
                {{ $t('badges') }}
              </div>
              <div class="font-size-normal">
                {{ $t('badges.description') }}
              </div>
            </div>
          </div>
        </div>
        <div class="dark-grey-border-color dark-grey-color mx-5 my-4 my-md-0 rounded-lg">
          <div class="d-flex my-8 mx-5">
            <div>
              <v-icon size="75" color="primary">fas fa-wallet</v-icon>
            </div>
            <div class="d-flex flex-column ms-5">
              <div class="headline font-weight-black">
                {{ $t('wallet') }}
              </div>
              <div class="font-size-normal">
                {{ $t('wallet.description') }}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    <div class="d-flex flex-column">
      <span class="headline text-center font-weight-black dark-grey-color mt-8 mb-5">{{ $t('startNow') }}</span>
      <span class="headline text-center font-weight-normal dark-grey-color mt-3 mb-10">{{ $t('startNow.description') }}</span>
      <div class="d-flex justify-center mb-16">
        <v-btn
          href="./tenants"
          class="primary mt-3"
          min-width="250px"
          depressed
          dark
          x-large
          outlined>
          <h4 class="font-weight-black">{{ $t('startAHub') }}</h4>
        </v-btn>
      </div>
    </div>
  </div>
</template>
<script>
export default {
  computed: Vuex.mapState({
    parentLocation: state => state.parentLocation,
    videoBuildersLink: state => state.videoBuildersLink,
  })
};
</script>