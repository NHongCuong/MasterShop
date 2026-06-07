<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'

const props = withDefaults(defineProps<{
  bottom?: number
  threshold?: number
}>(), {
  bottom: 28,
  threshold: 300,
})

const visible = ref(false)

const onScroll = () => {
  visible.value = window.scrollY > props.threshold
}

const scrollToTop = () => {
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

onMounted(() => {
  window.addEventListener('scroll', onScroll, { passive: true })
  onScroll()
})

onUnmounted(() => {
  window.removeEventListener('scroll', onScroll)
})
</script>

<template>
  <Transition name="btt-fade">
    <button
      v-if="visible"
      type="button"
      class="btt-btn"
      :style="{ bottom: `${bottom}px` }"
      title="Lên đầu trang"
      aria-label="Lên đầu trang"
      @click="scrollToTop"
    >
      <i class="fas fa-chevron-up"></i>
    </button>
  </Transition>
</template>

<style scoped>
.btt-btn {
  position: fixed;
  right: 28px;
  width: 52px;
  height: 52px;
  border-radius: 50%;
  border: none;
  background: #f1c40f;
  color: #fff;
  font-size: 20px;
  cursor: pointer;
  z-index: 1049;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 14px rgba(0, 0, 0, 0.18);
  transition: transform 0.2s ease, box-shadow 0.2s ease, background 0.2s ease;
}

.btt-btn:hover {
  background: #f39c12;
  transform: scale(1.08);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.22);
}

.btt-fade-enter-active,
.btt-fade-leave-active {
  transition: opacity 0.25s ease, transform 0.25s ease;
}

.btt-fade-enter-from,
.btt-fade-leave-to {
  opacity: 0;
  transform: translateY(12px);
}

@media (max-width: 576px) {
  .btt-btn {
    right: 16px;
    width: 46px;
    height: 46px;
    font-size: 18px;
  }
}
</style>
