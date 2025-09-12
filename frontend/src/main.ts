import { createApp } from 'vue';
import './style.css';
import App from './App.vue';
import { createPinia } from 'pinia';
import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router';
import Antd from 'ant-design-vue';
import 'ant-design-vue/dist/reset.css';

import LoginPage from './views/LoginPage.vue';
import ChatPage from './views/ChatPage.vue';
import { useUserStore } from './stores/user';

const routes: RouteRecordRaw[] = [
  { path: '/', redirect: '/login' },
  { path: '/login', component: LoginPage },
  { path: '/chat', component: ChatPage },
];

const router = createRouter({ history: createWebHistory(), routes });

router.beforeEach((to, from, next) => {
  const userStore = useUserStore();
  try {
    if (!userStore.user) {
      userStore.hydrateFromStorage();
    }
  } catch (e) {}
  const isAuthed = !!userStore.user;
  if (to.path === '/login' && isAuthed) {
    next('/chat');
  } else if (to.path.startsWith('/chat') && !isAuthed) {
    next('/login');
  } else {
    next();
  }
});

const app = createApp(App);
app.use(createPinia());
app.use(router);
app.use(Antd);
app.mount('#app');
