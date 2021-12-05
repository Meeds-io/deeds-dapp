<template>
  <div class="d-flex flex-column">
    <div class="d-flex flex-column-reverse flex flex-sm-row mb-8">
      <div class="d-flex flex-column">
        <deeds-nft-introduction />
        <deeds-points-simulator />
      </div>
      <deeds-earned-points class="ms-auto flex-shrink-0 flex-grow-0" />
    </div>
    <deeds-redeem />
    <deeds-owned />
  </div>
</template>