<template>
  <div class="not-found">
    <h1>404</h1>
    <p>页面未找到</p>
    <el-button type="primary" @click="router.push('/')">返回首页</el-button>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()
</script>

<style scoped>
.not-found {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100%;
  background: white;
}

.not-found h1 {
  font-size: 100px;
  color: #409eff;
  margin-bottom: 20px;
}

.not-found p {
  font-size: 20px;
  color: #666;
  margin-bottom: 30px;
}
</style>
