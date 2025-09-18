<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '../stores/user';
import { message } from 'ant-design-vue';
import api from '../utils/api';
import ParticlesBg from '../components/ParticlesBg.vue';
import theme from "../../public/theme.json"

const router = useRouter();
const userStore = useUserStore();
const loading = ref(false);
const form = ref({ username: '', password: '' });
const touched = ref({ username: false, password: false });
const triedSubmit = ref(false);

const onLogin = async () => {
  triedSubmit.value = true;
  if (!form.value.username.trim() || !form.value.password.trim()) {
    message.warning('请输入用户名和密码');
    return;
  }
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

</script>

<template>
  <!-- fix theme not apply -->
  <a-config-provider :theme = theme></a-config-provider>
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
        <a-form-item
          label="用户名"
          :validate-status="(!form.username.trim() && (touched.username || triedSubmit)) ? 'error' : undefined"
          :help="(!form.username.trim() && (touched.username || triedSubmit)) ? '用户名不能为空' : undefined"
        >
          <a-input
            v-model:value="form.username"
            placeholder="请输入用户名"
            :status="(!form.username.trim() && (touched.username || triedSubmit)) ? 'error' : undefined"
            @blur="touched.username = true"
          />
        </a-form-item>
        <a-form-item
          label="密码"
          :validate-status="(!form.password.trim() && (touched.password || triedSubmit)) ? 'error' : undefined"
          :help="(!form.password.trim() && (touched.password || triedSubmit)) ? '密码不能为空' : undefined"
        >
          <a-input-password
            v-model:value="form.password"
            placeholder="请输入密码"
            :status="(!form.password.trim() && (touched.password || triedSubmit)) ? 'error' : undefined"
            @blur="touched.password = true"
          />
        </a-form-item>
        <div style="margin: 10px;">
          <a href="/register">还没有账号？注册一个新账号</a>
        </div>
        <a-space>
          <a-button type="primary" size="large" :loading="loading" @click="onLogin">登录</a-button>
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

<style scoped>
:deep(.ant-form-item-explain-error) {
  position: absolute;
}
</style>
