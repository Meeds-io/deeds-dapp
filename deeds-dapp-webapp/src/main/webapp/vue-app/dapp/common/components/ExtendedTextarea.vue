<template>
  <v-textarea
    v-model="text"
    :rules="rules"
    :placeholder="placeholder"
    :rows="rows"
    :row-height="rowHeight"
    counter
    auto-grow
    outlined
    class="extended-textarea">
    <template #counter>
      <div class="v-counter d-flex align-center me-n3">
        <span :class="isValid && 'text--disabled' || 'error--text'" class="me-1">{{ counterMessage }}</span>
        <v-icon :color="isValid && 'success' || 'error'" x-small>fas fa-exclamation-circle</v-icon>
      </div>
    </template>
  </v-textarea>
</template>
<script>
export default {
  props: {
    value: {
      type: String,
      default: null
    },
    placeholder: {
      type: String,
      default: null
    },
    maxLength: {
      type: Number,
      default: () => 50
    },
    rows: {
      type: Number,
      default: () => 5
    },
    rowHeight: {
      type: Number,
      default: () => 24
    },
  },
  data: () => ({
    text: null,
    rules: [],
  }),
  computed: {
    limitMessageLabel() {
      return this.$t('textarea.limitMessage', {0: this.maxLength});
    },
    messageLength() {
      return this.text?.length || 0;
    },
    isValid() {
      return this.messageLength <= this.maxLength;
    },
    counterMessage() {
      return `${this.messageLength} / ${this.maxLength}`;
    },
  },
  watch: {
    text() {
      this.$emit('input', this.text);
    },
  },
  created() {
    this.text = this.value;
    this.rules = [v => !v || v.length <= this.maxLength || this.limitMessageLabel];
  },
};
</script>