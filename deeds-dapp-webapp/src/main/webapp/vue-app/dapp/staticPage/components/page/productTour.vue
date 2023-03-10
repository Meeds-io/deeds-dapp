<template>
  <div class="d-flex flex-column mt-8 mt-sm-10">
    <v-card flat>
      <v-card-title class="py-0 mb-md-10 mb-0 justify-center font-weight-bold dark-grey-color">{{ $t('productTour.title') }}</v-card-title>
    </v-card>
    <div class="d-flex flex-column flex-md-row py-4 mt-10">
      <div class="d-flex flex-column my-auto col-12 col-md-6">
        <div class="ps-0 dark-grey-color font-weight-normal v-card__title">{{ $t('productTour.description') }}</div>
        <div class="d-none d-md-flex justify-center hidden-sm-and-down">
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
            <v-icon size="24" class="me-2">fas fa-external-link-alt</v-icon>
            <span class="headline font-weight-black">{{ $t('exploreAHub') }}</span>
          </v-btn>
        </div>
      </div>
      <video
        class="mt-5 mt-md-0"
        height="360px"
        loop="true"
        autoplay
        muted>
        <source :src="`${videoBuildersLink}`" type="video/mp4">
      </video>
      <div class="d-flex d-md-none justify-center hidden-sm-and-up mt-7">
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
          <v-icon size="24" class="me-2">fas fa-external-link-alt</v-icon>
          <span class="headline font-weight-black">{{ $t('exploreAHub') }}</span>
        </v-btn>
      </div>
    </div>
    <div>
      <div class="d-flex flex-column flex-md-row my-16">
        <div class="d-flex flex-column my-auto mx-0 mx-md-14">
          <span class="headline font-weight-black dark-grey-color">{{ $t('contributionPrograms.title') }}</span>
          <span class="dark-grey-color mt-5 mb-5 mb-md-0">{{ $t('contributionPrograms.description') }}</span>
        </div>
        <video
          height="350px"
          loop="true"
          autoplay
          muted>
          <source :src="`${videoBuildersLink}`" type="video/mp4">
        </video>
      </div>
      <div class="d-flex flex-column-reverse flex-md-row my-16 mx-0 mx-md-14">
        <video
          height="350px"
          loop="true"
          autoplay
          muted>
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
          height="350px"
          loop="true"
          autoplay
          muted>
          <source :src="`${videoBuildersLink}`" type="video/mp4">
        </video>
      </div>
      <div class="d-flex flex-column-reverse flex-md-row my-16 mx-0 mx-md-14">
        <video
          height="350px"
          loop="true"
          autoplay
          muted>
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
          height="350px"
          loop="true"
          autoplay
          muted>
          <source :src="`${videoBuildersLink}`" type="video/mp4">
        </video>
      </div>
    </div>
    <div class="light-grey-background">
      <span class="headline d-flex justify-center font-weight-black dark-grey-color mt-4 mb-7">{{ $t('andMore') }}</span>
      <v-container class="mb-10">
        <v-row no-gutters>
          <v-col
            cols="12"
            lg="4"
            md="12">
            <div 
              class="dark-grey-border-color dark-grey-color mx-3 my-4 my-md-0 rounded-lg"
              :class="!isSmallScreen && 'fill-height'">
              <div class="d-flex my-8 mx-5">
                <div>
                  <v-icon 
                    size="75" 
                    color="primary" 
                    class="me-md-0 me-9">
                    fas fa-award
                  </v-icon>
                </div>
                <div class="d-flex flex-column ms-5">
                  <span class="headline font-weight-black">
                    {{ $t('kudos') }}
                  </span>
                  <span class="font-size-normal">
                    {{ $t('kudos.description') }}
                  </span>
                </div>
              </div>
            </div>
          </v-col>
          <v-col
            cols="12"
            lg="4"
            md="12">
            <div 
              class="dark-grey-border-color dark-grey-color mx-3 my-4 my-md-0 rounded-lg"
              :class="!isSmallScreen && 'fill-height'">
              <div class="d-flex my-8 mx-5">
                <div>
                  <v-icon size="75" color="primary">fas fa-graduation-cap</v-icon>
                </div>
                <div class="d-flex flex-column ms-5">
                  <span class="headline font-weight-black">
                    {{ $t('badges') }}
                  </span>
                  <span class="font-size-normal">
                    {{ $t('badges.description') }}
                  </span>
                </div>
              </div>
            </div>
          </v-col>
          <v-col
            cols="12"
            lg="4"
            md="12">
            <div 
              class="dark-grey-border-color dark-grey-color mx-3 my-4 my-md-0 rounded-lg"
              :class="!isSmallScreen && 'fill-height'">
              <div class="d-flex my-8 mx-5">
                <div>
                  <v-icon 
                    size="75" 
                    color="primary" 
                    class="me-md-0 me-5">
                    fas fa-wallet
                  </v-icon>
                </div>
                <div class="d-flex flex-column ms-5">
                  <span class="headline font-weight-black">
                    {{ $t('wallet') }}
                  </span>
                  <span class="font-size-normal">
                    {{ $t('wallet.description') }}
                  </span>
                </div>
              </div>
            </div>
          </v-col>
        </v-row>
      </v-container>
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
    isSmallScreen() {
      return this.$vuetify.breakpoint.mdAndDown;
    },
  })
};
</script>