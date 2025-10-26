<script setup lang="ts">
import {ref, onMounted} from 'vue'
import axios from 'axios'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import Button from 'primevue/button'
import Dialog from 'primevue/dialog'
import InputText from 'primevue/inputtext'
import Dropdown from 'primevue/dropdown'
import Toast from 'primevue/toast'
import {useToast} from 'primevue/usetoast'

import type {UserType, UserStatus} from '../../interfaces/app'

// const form = reactive<User>(
//     {
//       email:'',
//       password:''
//     });

const API_URL = 'http://localhost:8081'
const toast = useToast()

// =================== STATE ===================
const userList = ref<any[]>([])
const selectedUser = ref<any>(null)
const visibleDialog = ref(false)
const editMode = ref(false)

const userTypeList = ref<UserType[]>([])
const userStatusList = ref<UserStatus[]>([])

// =================== FUNCTIONS ===================

// 📦 Load danh sách user
const loadUsers = async () => {
  try {
    const res = await axios.get(`${API_URL}/user`)
    userList.value = res.data
  } catch (err) {
    toast.add({severity: 'error', summary: 'Lỗi', detail: 'Không thể tải user', life: 2000})
  }
}

// 📦 Load danh sách type + status
const loadMetaData = async () => {
  try {
    const [typeRes, statusRes] = await Promise.all([
      axios.get(`${API_URL}/usertype/all`),
      axios.get(`${API_URL}/userstatus/all`)
    ])
    userTypeList.value = typeRes.data
    userStatusList.value = statusRes.data
  } catch (err) {
    toast.add({severity: 'error', summary: 'Lỗi', detail: 'Không thể tải UserType/UserStatus', life: 2000})
  }
}

// 🧮 Format thời gian "YYYY-MM-DD HH:mm:ss"
const formatDateTime = () => {
  const now = new Date()
  const pad = (n: number) => n.toString().padStart(2, '0')
  return `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())} ${pad(now.getHours())}:${pad(now.getMinutes())}:${pad(now.getSeconds())}`
}

// 🆕 Mở form thêm
const openAddDialog = () => {
  selectedUser.value = {
    id: null,
    nameUser: '',
    phone: '',
    email: '',
    address: '',
    password: '',
    verify: '',
    regtime: formatDateTime(),
    salt: Math.random().toString(36).substring(2, 8), // random 6 ký tự salt
    userType: null,
    userStatus: null
  }
  editMode.value = false
  visibleDialog.value = true
}

// ✏️ Mở form sửa
const openEditDialog = (user: any) => {
  selectedUser.value = {
    ...user,
    regtime: formatDateTime(),
    salt: Math.random().toString(36).substring(2, 8)
  } // random 6 ký tự salt }
  editMode.value = true
  visibleDialog.value = true
}

// ❌ Xóa user
const deleteUser = async (id: number) => {
  if (!confirm('Bạn có chắc chắn muốn xóa user này?')) return
  try {
    await axios.delete(`${API_URL}/delete/${id}`)
    toast.add({severity: 'success', summary: 'Thành công', detail: 'Đã xóa user', life: 1500})
    loadUsers()
  } catch {
    toast.add({severity: 'error', summary: 'Lỗi', detail: 'Xóa thất bại', life: 2000})
  }
}

// 💾 Lưu user (thêm / sửa)
const saveUser = async () => {
  try {
    // 🧩 Kiểm tra rỗng
    if (!selectedUser.value.nameUser) {
      toast.add({severity: 'warn', summary: 'Thiếu thông tin', detail: 'Vui lòng nhập tên người dùng!', life: 2000})
      return
    }
    if (!selectedUser.value.email) {
      toast.add({severity: 'warn', summary: 'Thiếu thông tin', detail: 'Vui lòng nhập email!', life: 2000})
      return
    }
    if (!selectedUser.value.password) {
      toast.add({severity: 'warn', summary: 'Thiếu thông tin', detail: 'Vui lòng nhập password!', life: 2000})
      return
    }
    if (!selectedUser.value.userType) {
      toast.add({severity: 'warn', summary: 'Thiếu thông tin', detail: 'Vui lòng nhập loại người dùng!', life: 2000})
      return
    }
    if (!selectedUser.value.userStatus) {
      toast.add({severity: 'warn', summary: 'Thiếu thông tin', detail: 'Vui lòng nhập status người dùng!', life: 2000})
      return
    }

    // 🧠 Validate định dạng email (ví dụ example@gmail.com)
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
    if (!emailRegex.test(selectedUser.value.email)) {
      toast.add({
        severity: 'error',
        summary: 'Email không hợp lệ',
        detail: 'Vui lòng nhập đúng định dạng email (vd: example@gmail.com)',
        life: 2500
      })
      return
    }

    // 📦 Dữ liệu gửi lên backend
    const payload = {
      nameUser: selectedUser.value.nameUser,
      phone: selectedUser.value.phone,
      email: selectedUser.value.email,
      address: selectedUser.value.address,
      password: selectedUser.value.password,
      verify: selectedUser.value.verify,
      regtime: selectedUser.value.regtime,
      salt: selectedUser.value.salt,
      userType: selectedUser.value.userType ? {id: selectedUser.value.userType.id} : null,
      userStatus: selectedUser.value.userStatus ? {id: selectedUser.value.userStatus.id} : null
    }

    // ⚙️ Gọi API thêm hoặc sửa
    if (editMode.value) {
      await axios.put(`${API_URL}/update/${selectedUser.value.id}`, payload)
      toast.add({severity: 'success', summary: 'Cập nhật', detail: 'Sửa user thành công', life: 1500})
    } else {
      await axios.post(`${API_URL}/add`, payload)
      toast.add({severity: 'success', summary: 'Thêm mới', detail: 'Thêm user thành công', life: 1500})
    }

    // ✅ Đóng dialog và reload
    visibleDialog.value = false
    loadUsers()

  } catch (err) {
    console.error('❌ Save error:', err)
    toast.add({severity: 'error', summary: 'Lỗi', detail: 'Không thể lưu user', life: 2000})
  }
}

onMounted(() => {
  loadUsers()
  loadMetaData()
})
</script>

<template>
  <div class="p-6">
    <h2 class="text-2xl font-bold mb-4">👤 Quản lý người dùng</h2>

    <Button label="➕ Thêm user" icon="pi pi-plus" class="mb-3" @click="openAddDialog"/>

    <DataTable :value="userList" paginator :rows="20" stripedRows responsiveLayout="scroll" style="width: 100%;">
      <Column field="id" header="ID" sortable/>
      <Column field="nameUser" header="Tên" sortable/>
      <Column field="phone" header="SĐT" sortable/>
      <Column field="email" header="Email" sortable/>
      <Column field="address" header="Địa chỉ" sortable/>
      <Column field="userType.name" header="Loại tài khoản"/>
      <Column field="userStatus.name" header="Trạng thái"/>
      <Column field="regtime" header="Thời gian"/>
      <Column header="Hành động" style="width: 90px; text-align: center;">
        <template #body="{ data }">
          <div class="action-buttons">
            <Button icon="pi pi-pencil" class="p-button-warning mr-2 p-button-sm" @click="openEditDialog(data)"/>
            <Button icon="pi pi-trash" class="p-button-danger p-button-sm" @click="deleteUser(data.id)"/>
          </div>
        </template>
      </Column>
    </DataTable>

    <!-- Dialog thêm/sửa -->
    <Dialog v-model:visible="visibleDialog"
            :header="editMode ? '✏️ Sửa người dùng' : '🆕 Thêm người dùng'"
            modal style="width:600px">
      <div class="flex flex-col gap-3">
        <InputText v-model="selectedUser.nameUser" placeholder="Tên user"/>
        <InputText v-model="selectedUser.phone" placeholder="Số điện thoại"/>
        <InputText v-model="selectedUser.email" placeholder="Email" type="email"/>
        <InputText v-model="selectedUser.salt" placeholder="Salt"/>
        <InputText v-model="selectedUser.password" placeholder="Mật khẩu" type="password"/>
        <InputText v-model="selectedUser.verify" placeholder="Verify " type="hidden"/>
        <InputText v-model="selectedUser.regtime" placeholder="Regtime (YYYY-MM-DD HH:mm:ss)"/>
        <InputText v-model="selectedUser.address" placeholder="Địa chỉ" style="width:500px"/>
        <Dropdown v-model="selectedUser!.userType"
                  :options="userTypeList"
                  optionLabel="name"
                  placeholder="Chọn loại tài khoản"
                  style="width:250px"/>

        <Dropdown v-model="selectedUser.userStatus"
                  :options="userStatusList"
                  optionLabel="name"
                  placeholder="Chọn trạng thái"
                  style="width:250px"/>
      </div>

      <template #footer>
        <Button label="Hủy" icon="pi pi-times" class="p-button-text" @click="visibleDialog = false"/>
        <Button label="Lưu" icon="pi pi-check" @click="saveUser"/>
      </template>
    </Dialog>

    <Toast/>
  </div>
</template>

<style scoped>

.p-datatable {
  font-size: 14px;
  width: 100%;
  table-layout: auto; /* Tự điều chỉnh độ rộng cột */
  white-space: nowrap; /* Giữ chữ trên 1 dòng, tránh xuống hàng */
}

.p-datatable table {
  width: 100%;
}

.p-datatable .p-column-title {
  text-align: center;
}

/* Các nút hành động */
.action-buttons {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}


</style>
