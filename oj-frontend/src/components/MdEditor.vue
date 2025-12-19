<template>
  <Editor :value="value" :plugins="plugins" @change="changeValue" />
</template>

<script setup lang="ts">
import gfm from "@bytemd/plugin-gfm";
import { Editor, Viewer } from "@bytemd/vue-next";
import { ref, defineProps } from "vue";
const plugins = [
  gfm(),
  // Add more plugins here
];
defineProps({
  value: {
    type: String,
    default: "",
  },
  changeValue: {
    type: Function,
    default: (value: string) => {
      console.log(value);
    },
  },
});
</script>
