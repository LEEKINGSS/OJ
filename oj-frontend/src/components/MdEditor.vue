<template>
  <Editor :value="value" :plugins="plugins" @change="changeValue" />
</template>

<script setup lang="ts">
import gfm from "@bytemd/plugin-gfm";
import highlight from "@bytemd/plugin-highlight";
import math from "@bytemd/plugin-math";
import { Editor, Viewer } from "@bytemd/vue-next";
import { ref, defineProps } from "vue";
import "katex/dist/katex.css";

const plugins = [
  gfm(),
  highlight(),
  math(),
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
