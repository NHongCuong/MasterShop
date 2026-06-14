<template>
  <div class="p-4">
    <div class="card shadow-sm border-0 rounded-4">
      <div class="card-header bg-white border-0 py-3">
        <h4 class="mb-0 fw-bold">Chăm sóc khách hàng</h4>
        <p class="text-muted small mb-0">Khách hàng đang online: {{ onlineUsers.length }}</p>
      </div>
      <div class="card-body">
        <div class="table-responsive">
          <table class="table table-hover align-middle">
            <thead class="bg-light">
              <tr>
                <th>STT</th>
                <th>Ảnh</th>
                <th>Tên người dùng</th>
                <th>Email</th>
                <th>Số điện thoại</th>
                <th>Địa chỉ</th>
                <th>Ngày sinh</th>
                <th>Giới tính</th>
                <th>Ngày tạo</th>
                <th>Tình trạng</th>
                <th>Thao tác</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(user, index) in onlineUsers" :key="user.id">
                <td>{{ index + 1 }}</td>
                <td>
                  <img :src="user.avatar || 'https://www.w3schools.com/howto/img_avatar.png'" class="rounded-circle" width="40" height="40" alt="avatar">
                </td>
                <td>{{ user.name }}</td>
                <td>{{ user.email }}</td>
                <td>{{ user.phone || '--' }}</td>
                <td>{{ user.address || '--' }}</td>
                <td>{{ user.dob || '--' }}</td>
                <td>{{ user.gender || '--' }}</td>
                <td>{{ formatDate(user.createdAt) }}</td>
                <td>
                  <span class="badge bg-success-subtle text-success px-3 py-2 rounded-pill">
                    <i class="pi pi-circle-fill me-1" style="font-size: 8px"></i> ONLINE
                  </span>
                </td>
                <td>
                  <button class="btn btn-warning btn-sm rounded-pill px-3" @click="openChat(user.id)">
                    <i class="pi pi-comments me-1"></i> Chat
                  </button>
                </td>
              </tr>
              <tr v-if="onlineUsers.length === 0">
                <td colspan="11" class="text-center py-5 text-muted">Không có khách hàng nào đang online</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <div v-if="onlineUsers.length === 0">
      <!-- ... -->
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue';
import { io } from 'socket.io-client';
import { state } from '../../app/MyApp';
import AdminChatWidget from '../../components/AdminChatWidget.vue';

const onlineUsers = ref([]);
const socket = ref(null);

const formatDate = (date) => {
  if (!date) return '--';
  return new Date(date).toLocaleString();
};

const openChat = (userId) => {
  state.selectedChatUserId = null;
  setTimeout(() => {
    state.selectedChatUserId = userId;
  }, 0);
};

onMounted(() => {
  socket.value = io('http://localhost:9092');

  socket.value.on('connect', () => {
    socket.value.emit('login', {
      id: 'admin',
      name: state.user?.nameUser || 'Admin',
      role: 'admin'
    });
    socket.value.emit('request_user_list');
  });

  socket.value.on('update_user_list', (users) => {
    onlineUsers.value = users.filter(u => u.role !== 'admin');
  });
});

onUnmounted(() => {
  if (socket.value) {
    socket.value.disconnect();
  }
});
</script>

<style scoped>
.table th {
  font-weight: 600;
  font-size: 13px;
  color: #666;
  border-top: none;
}
.table td {
  font-size: 14px;
}
.bg-success-subtle {
  background-color: #d1e7dd;
}
.btn-warning {
  background-color: #f1b42f;
  border-color: #f1b42f;
  color: white;
}
.btn-warning:hover {
  background-color: #d99f26;
  border-color: #d99f26;
  color: white;
}
</style>
