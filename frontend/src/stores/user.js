import { defineStore } from 'pinia';

const STORAGE_KEY = 'webchat:user';

export const useUserStore = defineStore('user', {
  state: () => ({
    user: (() => {
      try {
        const raw = sessionStorage.getItem(STORAGE_KEY);
        return raw ? JSON.parse(raw) : null;
      } catch (e) {
        return null;
      }
    })(),
  }),
  getters: {
    isAuthenticated: (state) => !!state.user,
  },
  actions: {
    setUser(u) {
      this.user = u;
      try {
        sessionStorage.setItem(STORAGE_KEY, JSON.stringify(u));
      } catch (e) { }
    },
    hydrateFromStorage() {
      try {
        const raw = sessionStorage.getItem(STORAGE_KEY);
        this.user = raw ? JSON.parse(raw) : null;
      } catch (e) {
        this.user = null;
      }
    },
    logout() {
      this.user = null;
      try {
        sessionStorage.removeItem(STORAGE_KEY);
      } catch (e) { }
    },
  },
});
