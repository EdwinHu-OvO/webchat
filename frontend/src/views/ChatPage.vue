<!-- eslint-disable no-empty -->
<script setup>
import { onMounted, onBeforeUnmount, ref, computed, render, watch, nextTick } from 'vue';
import { useUserStore } from '../stores/user';
import io from 'socket.io-client';
import api from '../utils/api';
import { message } from 'ant-design-vue';
import { UserOutlined } from '@ant-design/icons-vue';
import { ExclamationCircleOutlined } from '@ant-design/icons-vue';
import { createVNode } from 'vue';
import { Modal } from 'ant-design-vue';

const userStore = useUserStore();
const me = computed(() => userStore.user);

const socket = ref(null);
const friends = ref([]);
const groups = ref([]);
const modals = ref({ addFriend: false, createGroup: false, invite: false, joinGroup: false });
const addFriendUsername = ref('');
const newGroupName = ref('');
const inviteUsername = ref('');
const joinGroupName = ref('');
const activeSession = ref({ type: 'friend', id: null });
const messages = ref([]);
const input = ref('');
const placeholder = computed(() => {
  if (!activeSession.value?.id) return '请选择好友或群组开始聊天';
  return activeSession.value.type === 'friend' ? '发给好友的消息...(Ctrl+Enter发送)' : '发到群组的消息...(Ctrl+Enter发送)';
});

// 时间格式化与时间线
const pad = (n) => (n < 10 ? '0' + n : '' + n);
const formatTime = (ts) => {
  const d = new Date(ts);
  return `${pad(d.getHours())}:${pad(d.getMinutes())}`;
};
const formatDateTime = (ts) => {
  const d = new Date(ts);
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`;
};
const isSameDay = (a, b) => {
  const da = new Date(a),
    db = new Date(b);
  return (
    da.getFullYear() === db.getFullYear() &&
    da.getMonth() === db.getMonth() &&
    da.getDate() === db.getDate()
  );
};
const renderList = computed(() => {
  const out = [];
  let prev = null;
  const GAP = 5 * 60 * 1000; // 5分钟时间线分割
  for (const m of messages.value) {
    if (!prev || !isSameDay(prev.ts, m.ts) || m.ts - prev.ts > GAP) {
      out.push({ type: 'divider', label: formatDateTime(m.ts), key: 'd' + m.ts });
    }
    out.push({ type: 'msg', ...m, key: 'm' + m.ts });
    prev = m;
  }
  return out;
});

const SESSION_KEY = 'webchat:activeSession';
const SESSION = sessionStorage;

// 会话标题与群成员
const groupMembers = ref([]);
const currentGroupOwnerId = ref(null);
const currentTitle = computed(() => {
  if (!activeSession.value?.id) return '未选择会话';
  if (activeSession.value.type === 'friend') {
    const f = friends.value.find((x) => x.id === activeSession.value.id);
    return f?.username || `好友${activeSession.value.id}`;
  }
  const g = groups.value.find((x) => x.id === activeSession.value.id);
  return g?.name || `群组${activeSession.value.id}`;
});
const currentSubtitle = computed(() => {
  if (activeSession.value.type !== 'group' || !groupMembers.value.length) return '';
  return '成员：' + groupMembers.value.map((m) => m.username).join('、');
});

const loadFriends = async () => {
  if (!me.value?.id) return;
  const { data } = await api.get(`/api/users/${me.value.id}/friends`);
  friends.value = data.map((f) => ({ id: f.friend.id, username: f.friend.username }));
};

const loadGroups = async () => {
  if (!me.value?.id) return;
  const { data } = await api.get(`/api/users/${me.value.id}/groups`);
  groups.value = data;
};

const resolveSenderName = (senderId) => {
  if (!senderId) return '';
  if (me.value && senderId === me.value.id) return me.value.username || '我';
  const f = friends.value.find((x) => x.id === senderId);
  if (f) return f.username;
  return `用户${senderId}`;
};

const sendMessage = () => {
  if (!activeSession.value?.id) {
    message.warning('请先选择一个聊天对象');
    return;
  }
  if (!input.value.trim()) return;
  if (!socket.value || !socket.value.connected) {
    message.error('连接未建立，请稍后重试或刷新页面');
    return;
  }
  if (activeSession.value.type === 'friend') {
    socket.value.emit('private_message', {
      senderId: me.value.id,
      receiverId: activeSession.value.id,
      content: input.value,
    });
  } else if (activeSession.value.type === 'group') {
    socket.value.emit('group_message', {
      senderId: me.value.id,
      groupId: activeSession.value.id,
      content: input.value,
    });
  }
  input.value = '';
};

onMounted(async () => {
  if (!me.value) return;
  await Promise.all([loadFriends(), loadGroups()]);
  // socket 连接
  socket.value = io('http://localhost:9092', {
    transports: ['websocket'],
    query: { userId: String(me.value.id) },
  });

  socket.value.on('connect', () => {});
  socket.value.on('private_message', (msg) => {
    if (activeSession.value.type !== 'friend') return;
    const related =
      (msg.senderId === me.value.id && msg.receiverId === activeSession.value.id) ||
      (msg.receiverId === me.value.id && msg.senderId === activeSession.value.id);
    if (!related) return;
    messages.value.push({
      content: msg.content,
      isMine: msg.senderId === me.value.id,
      senderId: msg.senderId,
      senderName: resolveSenderName(msg.senderId),
      ts: Date.now(),
    });
  });
  socket.value.on('group_message', (msg) => {
    if (activeSession.value.type !== 'group' || activeSession.value.id !== msg.groupId) return;
    messages.value.push({
      content: msg.content,
      isMine: msg.senderId === me.value.id,
      senderId: msg.senderId,
      senderName: resolveSenderName(msg.senderId),
      ts: Date.now(),
    });
  });

  // 恢复上次会话
  try {
    const raw = SESSION.getItem(SESSION_KEY);
    if (raw) {
      const s = JSON.parse(raw);
      if (s?.type === 'friend' && s.id) {
        await onPickFriend(s.id);
      } else if (s?.type === 'group' && s.id) {
        await onPickGroup(s.id);
      }
    }
  } catch (e) {}
});

onBeforeUnmount(() => {
  if (socket.value) socket.value.disconnect();
});

const onPickFriend = async (id) => {
  activeSession.value = { type: 'friend', id };
  try {
    SESSION.setItem(SESSION_KEY, JSON.stringify(activeSession.value));
  } catch (e) {}
  const { data } = await api.get('/api/messages/private', {
    params: { userId: me.value.id, peerId: id },
  });
  messages.value = data.map((m) => ({
    id: m.id,
    content: m.content,
    isMine: m.sender?.id === me.value.id,
    senderId: m.sender?.id,
    senderName: m.sender?.username || resolveSenderName(m.sender?.id),
    ts: new Date(m.createdAt).getTime() || Date.now(),
  }));
};

const onPickGroup = async (id) => {
  activeSession.value = { type: 'group', id };
  try {
    SESSION.setItem(SESSION_KEY, JSON.stringify(activeSession.value));
  } catch (e) {}
  if (socket.value) socket.value.emit('join_group', { groupId: id });
  const { data } = await api.get(`/api/messages/group/${id}`);
  messages.value = data.map((m) => ({
    id: m.id,
    content: m.content,
    isMine: m.sender?.id === me.value.id,
    senderId: m.sender?.id,
    senderName: m.sender?.username || resolveSenderName(m.sender?.id),
    ts: new Date(m.createdAt).getTime() || Date.now(),
  }));
  try {
    const { data: members } = await api.get(`/api/groups/${id}/members`);
    groupMembers.value = members.map((u) => ({ id: u.id, username: u.username }));
  } catch (e) {
    groupMembers.value = [];
  }
  // 获取群主ID（优先从已加载的groups中）
  const g = groups.value.find((x) => x.id === id);
  currentGroupOwnerId.value = g?.ownerId ?? null;
};

const handleAddFriend = async () => {
  if (!addFriendUsername.value.trim()) return;
  try {
    const { data: target } = await api.get('/api/users/search', {
      params: { username: addFriendUsername.value },
    });
    if (!target?.id) throw new Error('用户不存在');
    await api.post('/api/friends', { userId: me.value.id, friendId: target.id });
    await loadFriends();
    modals.value.addFriend = false;
    addFriendUsername.value = '';
    message.success('已添加好友');
  } catch (e) {
    message.error(e?.response?.data?.message || e.message || '添加失败');
  }
};

const handleCreateGroup = async () => {
  if (!newGroupName.value.trim()) return;
  try {
    await api.post('/api/groups', { ownerId: me.value.id, name: newGroupName.value });
    await loadGroups();
    modals.value.createGroup = false;
    newGroupName.value = '';
    message.success('已创建群组');
  } catch (e) {
    message.error(e?.response?.data?.message || '创建失败');
  }
};

const handleInvite = async () => {
  if (activeSession.value.type !== 'group' || !activeSession.value.id) return;
  if (!inviteUsername.value.trim()) return;
  try {
    const { data: target } = await api.get('/api/users/search', {
      params: { username: inviteUsername.value },
    });
    if (!target?.id) throw new Error('用户不存在');
    await api.post(`/api/groups/${activeSession.value.id}/members/${target.id}`);
    inviteUsername.value = '';
    modals.value.invite = false;
    message.success('已邀请加入群组');
  } catch (e) {
    message.error(e?.response?.data?.message || '邀请失败');
  }
};

const handleJoinGroup = async () => {
  if (!joinGroupName.value.trim()) return;
  try {
    await api.post('/api/groups/join', { userId: me.value.id, name: joinGroupName.value });
    // 刷新群列表并选中新加入的群
    await loadGroups();
    const g = groups.value.find((x) => x.name === joinGroupName.value);
    if (g) await onPickGroup(g.id);
    joinGroupName.value = '';
    modals.value.joinGroup = false;
    message.success('已加入群聊');
  } catch (e) {
    message.error(e?.response?.data?.message || '加入失败');
  }
};

const onLogout = () => {
  try {
    SESSION.removeItem(SESSION_KEY);
  } catch (e) {}
  if (socket.value) {
    try {
      socket.value.disconnect();
    } catch (e) {}
  }
  userStore.logout();
  window.location.href = '/login';
};

const onRemoveFriend = async (friendId) => {
  try {
    await api.delete('/api/friends', { params: { userId: me.value.id, friendId } });
    // 如果当前会话正好是该好友，则清空会话与消息
    if (activeSession.value.type === 'friend' && activeSession.value.id === friendId) {
      activeSession.value = { type: 'friend', id: null };
      try {
        SESSION.removeItem(SESSION_KEY);
      } catch (e) {}
      messages.value = [];
    }
    await loadFriends();
    message.success('已删除好友');
  } catch (e) {
    message.error(e?.response?.data?.message || '删除失败');
  }
};

const handleLeaveGroup = async () => {
  if (activeSession.value.type !== 'group' || !activeSession.value.id) return;
  try {
    await api.delete(`/api/groups/${activeSession.value.id}/members/${me.value.id}`);
    // 本地更新：移除该群、清空当前会话与消息
    groups.value = groups.value.filter((g) => g.id !== activeSession.value.id);
    activeSession.value = { type: 'friend', id: null };
    try {
      SESSION.removeItem(SESSION_KEY);
    } catch (e) {}
    groupMembers.value = [];
    messages.value = [];
    message.success('已退出群聊');
  } catch (e) {
    message.error(e?.response?.data?.message || '退出失败');
  }
};

const handleDissolveGroup = async (groupId) => {
  if (!groupId) return;
  try {
    await api.delete(`/api/groups/${groupId}`, { params: { operatorId: me.value.id } });
    groups.value = groups.value.filter((g) => g.id !== groupId);
    if (activeSession.value.type === 'group' && activeSession.value.id === groupId) {
      activeSession.value = { type: 'friend', id: null };
      try {
        SESSION.removeItem(SESSION_KEY);
      } catch (e) {}
      groupMembers.value = [];
      messages.value = [];
    }
    message.success('已解散群聊');
  } catch (e) {
    message.error(e?.response?.data?.message || '解散失败');
  }
};
const deleteFriendConfirm = (id) => {
  Modal.confirm({
    title: '确认删除该好友？',
    icon: createVNode(ExclamationCircleOutlined),
    content: createVNode('div', { style: 'color:red;' }, '确认将该好友从好友列表中删除'),
    onOk() {
      onRemoveFriend(id);
    },
    onCancel() {},
    class: 'deleteFriendConfirm',
  });
};
const exitGroupConfirm = (id) => {
  Modal.confirm({
    title: '确认退出该群聊？',
    icon: createVNode(ExclamationCircleOutlined),
    content: createVNode('div', { style: 'color:red;' }, '确认将从群聊中退出'),
    onOk() {
      handleLeaveGroup(id);
    },
    onCancel() {},
    class: 'exitGroupConfirm',
  });
};
const dissolveGroupConfirm = (id) => {
  Modal.confirm({
    title: '确认解散该群聊？',
    icon: createVNode(ExclamationCircleOutlined),
    content: createVNode('div', { style: 'color:red;' }, '该操作不可恢复，所有成员将被移出'),
    onOk() {
      handleDissolveGroup(id);
    },
    onCancel() {},
    class: 'dissolveGroupConfirm',
  });
};

const inputHeight = ref('32px')
const chatContainer = ref(null);
const scrollToBottom = () => {
  chatContainer.value.scrollTo({
    top: chatContainer.value.scrollHeight,
    behavior: 'smooth',
  });
};
watch(() => renderList.value, () => {
  nextTick(() => {
    scrollToBottom();
  });
});
</script>

<template>
  <a-config-provider :theme = theme>
  </a-config-provider>
  <a-layout style="height: 100vh; width: 100vw">
    <a-layout-sider width="320" theme="light">
      <a-avatar
        :size="64"
        style="
          margin: 1.5rem auto 0.3rem auto;
          display: flex;
          align-items: center;
          justify-content: center;
        "
      >
        <template #icon><UserOutlined /></template>
      </a-avatar>
      <div style="padding: 12px; font-weight: 600">{{ me?.username }}</div>
      <div style="padding: 0 12px 12px 12px">
        <a-space>
          <a-button size="small" @click="modals.addFriend = true">添加好友</a-button>
          <a-button size="small" @click="modals.createGroup = true">创建群组</a-button>
          <a-button size="small" danger @click="onLogout">退出登录</a-button>
        </a-space>
      </div>
      <a-tabs
        size="middle"
        :tab-bar-gutter="60"
        style="padding-left: 1rem; padding-right: 1rem; display: flex; align-items: center"
      >
        <a-tab-pane key="friends" tab="好友">
          <a-list :data-source="friends">
            <template #renderItem="{ item }">
              <a-list-item
                :class="{
                  selected: activeSession.type === 'friend' && activeSession.id === item.id,
                }"
                style="display: flex; justify-content: space-between; align-items: center"
                @click="onPickFriend(item.id)"
              >
                <span>{{ item.username }}</span>
              </a-list-item>
            </template>
          </a-list>
        </a-tab-pane>
        <a-tab-pane key="groups" tab="群组">
          <div style="padding: 0 12px 12px 12px; display: flex; justify-self: center">
            <a-button size="small" @click="modals.joinGroup = true">加入群聊</a-button>
          </div>
          <a-list :data-source="groups">
            <template #renderItem="{ item }">
              <a-list-item
                :class="{
                  selected: activeSession.type === 'group' && activeSession.id === item.id,
                }"
                @click="onPickGroup(item.id)"
                >{{ item.name }}</a-list-item
              >
            </template>
          </a-list>
        </a-tab-pane>
      </a-tabs>
    </a-layout-sider>

    <a-layout>
      <a-page-header
        style="border: 1px solid rgb(235, 237, 240); background-color: #fff"
        :title="currentTitle"
        :sub-title="currentSubtitle"
        :back-icon="false"
        @back="() => null"
      >
        <template #extra>
          <a-button
            v-if="activeSession.type === 'group' && activeSession.id"
            size="small"
            primary
            @click="modals.invite = true"
            >邀请成员</a-button
          >
          <a-button
            v-if="
              activeSession.type === 'group' && activeSession.id && currentGroupOwnerId === me?.id
            "
            size="small"
            danger
            @click="dissolveGroupConfirm(activeSession.id)"
            >解散群聊</a-button
          >
          <a-button
            v-else-if="activeSession.type === 'group' && activeSession.id"
            size="small"
            danger
            @click="exitGroupConfirm(activeSession.id)"
            >退出群聊</a-button
          >
          <a-button
            v-if="activeSession.type === 'friend' && activeSession.id"
            size="small"
            danger
            @click="deleteFriendConfirm(activeSession.id)"
            >删除好友</a-button
          >
        </template>
      </a-page-header>
      <a-layout-content style="display: flex; flex-direction: column; gap: 8px">
        <div ref="chatContainer" style="flex: 1; background-color: #f6f9fe; padding: 30px; overflow: auto">
          <template v-for="item in renderList" :key="item.key">
            <a-divider v-if="item.type === 'divider'" orientation="center" plain>{{
              item.label
            }}</a-divider>
            <div v-else class="msg-row" :class="{ mine: item.isMine }">
              <div class="sender">{{ item.senderName }}</div>
              <div class="msg-bubble" :class="{ mine: item.isMine }">{{ item.content }}</div>
              <div class="msg-time">{{ formatTime(item.ts) }}</div>
            </div>
          </template>
        </div>
        <div style="display: flex; gap: 8px; padding: 8px 16px 20px 16px">
          <a-textarea
v-model:value="input" :placeholder="placeholder" :style="{ height: inputHeight, transition: 'height 0.3s ease', resize: 'none' }"
          @keydown.enter.ctrl="sendMessage"
          @focus="inputHeight = '96px'"
          @blur="inputHeight = '32px'"
           />
          <a-button type="primary" @click="sendMessage">发送</a-button>
        </div>
      </a-layout-content>
    </a-layout>
  </a-layout>
  <a-modal v-model:open="modals.addFriend" title="添加好友" ok-text="添加" @ok="handleAddFriend">
    <a-input v-model:value="addFriendUsername" placeholder="对方用户名" />
  </a-modal>
  <a-modal
    v-model:open="modals.createGroup"
    title="创建群组"
    ok-text="创建"
    @ok="handleCreateGroup"
  >
    <a-input v-model:value="newGroupName" placeholder="群组名称" />
  </a-modal>
  <a-modal
    v-model:open="modals.invite"
    title="邀请成员到当前群组"
    ok-text="邀请"
    @ok="handleInvite"
  >
    <a-input v-model:value="inviteUsername" placeholder="对方用户名" />
  </a-modal>
  <a-modal v-model:open="modals.joinGroup" title="加入群聊" ok-text="加入" @ok="handleJoinGroup">
    <a-input v-model:value="joinGroupName" placeholder="群组名称" />
  </a-modal>
</template>

<style scoped>
:deep(.ant-tabs-nav, .ant-tabs-nav-wrap) {
  width: 100%;
}
:deep(.ant-tabs-nav-wrap) {
  justify-content: center;
}
:deep(.ant-tabs-content-holder) {
  width: 100%;
}
</style>
