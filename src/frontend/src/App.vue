<script setup lang="ts">
import { onMounted } from 'vue';
import { MyApp } from './app/MyApp';

onMounted(() => {
  MyApp.getInstance().loadGeneralImages();
});
</script>

<template>
  <router-view></router-view>
</template>

