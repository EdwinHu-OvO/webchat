<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '../stores/user';
import { message } from 'ant-design-vue';
import api from '../utils/api';
import ParticlesBg from '../components/ParticlesBg.vue';

const router = useRouter();
const userStore = useUserStore();
const loading = ref(false);
const form = ref({ username: '', password: '' });

const onLogin = async () => {
  loading.value = true;
  try {
    const { data } = await api.post('/api/auth/login', form.value);
    userStore.setUser(data);
    router.push('/chat');
  } catch (e) {
    message.error(e?.response?.data?.message || '登录失败');
  } finally {
    loading.value = false;
  }
};

const onRegister = async () => {
  loading.value = true;
  try {
    const { data } = await api.post('/api/auth/register', form.value);
    userStore.setUser(data);
    router.push('/chat');
  } catch (e) {
    message.error(e?.response?.data?.message || '注册失败');
  } finally {
    loading.value = false;
  }
};
</script>

<template>
  <div class="absolute w-100vw h-100vh z-10 translate-x-[-50%] translate-y-[-50%]">
    <a-card
      style="
        width: 360px;
        background-color: rgba(255, 255, 255, 0.56);
        border: none;
        border-radius: 18px;
      "
    >
      <span style="font-size: 32px; font-weight: 300; margin-bottom: 16px">WebChat</span>
      <a-form layout="vertical" @submit.prevent>
        <a-form-item label="用户名">
          <a-input v-model:value="form.username" placeholder="请输入用户名" />
        </a-form-item>
        <a-form-item label="密码">
          <a-input-password v-model:value="form.password" placeholder="请输入密码" />
        </a-form-item>
        <a-space>
          <a-button type="primary" :loading="loading" @click="onLogin">登录</a-button>
          <a-button :loading="loading" @click="onRegister">注册</a-button>
        </a-space>
      </a-form>
    </a-card>
  </div>
  <ParticlesBg
    class="absolute inset-0 w-100vw h-100vh"
    :quantity="70"
    :ease="10"
    color="#113b3b"
    :staticity="80"
    refresh
  />
</template>

<style scoped></style>
