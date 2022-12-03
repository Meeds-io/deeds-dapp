<!--

 This file is part of the Meeds project (https://meeds.io/).

 Copyright (C) 2020 - 2022 Meeds Association contact@meeds.io

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
  <v-textarea
    v-model="text"
    :rules="rules"
    :placeholder="placeholder"
    :rows="rows"
    :row-height="rowHeight"
    autofocus
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
    autofocus: {
      type: Boolean,
      default: false
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