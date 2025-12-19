<template>
  <div id="basicLayout">
    <a-layout style="min-height: 100vh">
      <a-layout-header class="header">
        <GlobalHeader />
      </a-layout-header>
      <a-layout-content class="content" style="background: #efefef">
        <router-view />
      </a-layout-content>
      <!-- <a-layout-footer class="footer">
        Footer
        <a href="https://www.google.com">shabi</a>
      </a-layout-footer> -->
    </a-layout>
  </div>
</template>

<style scoped>
#basicLayout {
}
#basicLayout .content {
  margin-bottom: 10px;
  padding: 20px;
}
#basicLayout .footer {
  background-color: green;
  margin-bottom: 16px;
  padding: 16px;
  position: sticky;
  bottom: 0;
  left: 0;
  right: 0;
  text-align: center;
}
</style>
<script setup>
import GlobalHeader from "@/components/GlobalHeader.vue";
import router from "@/router";
</script>
