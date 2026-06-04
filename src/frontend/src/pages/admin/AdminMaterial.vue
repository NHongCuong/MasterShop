<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import axios from 'axios'
import Toast from 'primevue/toast'
import { useToast } from 'primevue/usetoast'
import Dialog from 'primevue/dialog'
import Button from 'primevue/button'
import InputText from 'primevue/inputtext'

const toast = useToast()
const API_URL = 'http://localhost:8081/material'

// Data
const items = ref<any[]>([])
const totalItems = ref(0)
const totalPages = ref(0)
const loading = ref(false)

// Dialog state
const displayDialog = ref(false)
const isEdit = ref(false)
const currentItem = ref({ id: null, nameMaterial: '' })

// Filters
const search = ref('')
const pageSize = ref(10)
const currentPage = ref(0)
const sortOption = ref('newest')
const pageSizeOptions = [10, 20, 50, 100, 200]

// Load data
const loadData = async () => {
  loading.value = true
  try {
    const params: any = {
      page: currentPage.value,
      size: pageSize.value,
      sort: sortOption.value
    }
    if (search.value.trim()) {
      params.search = search.value.trim()
    }
    const res = await axios.get(`${API_URL}/all`, { params })
    items.value = res.data.content || []
    totalItems.value = res.data.totalElements || 0
    totalPages.value = res.data.totalPages || 0
  } catch (err) {
    console.error(err)
    toast.add({ severity: 'error', summary: 'Lỗi', detail: 'Tải danh sách thất bại', life: 3000 })
  } finally {
    loading.value = false
  }
}

// Add / Edit
const openAdd = () => {
  currentItem.value = { id: null, nameMaterial: '' }
  isEdit.value = false
  displayDialog.value = true
}

const openEdit = (item: any) => {
  currentItem.value = { id: item.id, nameMaterial: item.nameMaterial }
  isEdit.value = true
  displayDialog.value = true
}

const saveItem = async () => {
  if (!currentItem.value.nameMaterial.trim()) {
    toast.add({ severity: 'warn', summary: 'Cảnh báo', detail: 'Vui lòng nhập tên nguyên liệu', life: 2000 })
    return
  }

  try {
    if (isEdit.value) {
      await axios.put(`${API_URL}/update/${currentItem.value.id}`, currentItem.value)
      toast.add({ severity: 'success', summary: 'Thành công', detail: 'Đã cập nhật', life: 2000 })
    } else {
      await axios.post(`${API_URL}/add`, currentItem.value)
      toast.add({ severity: 'success', summary: 'Thành công', detail: 'Đã thêm mới', life: 2000 })
    }
    displayDialog.value = false
    loadData()
  } catch (err: any) {
    const msg = err.response?.data || 'Lưu thất bại'
    toast.add({ severity: 'error', summary: 'Lỗi', detail: msg, life: 3000 })
  }
}

// Delete
const deleteItem = async (id: number) => {
  if (!confirm('Bạn có chắc muốn xóa nguyên liệu này?')) return
  try {
    await axios.delete(`${API_URL}/delete/${id}`)
    toast.add({ severity: 'success', summary: 'Thành công', detail: 'Đã xóa', life: 2000 })
    loadData()
  } catch (err) {
    toast.add({ severity: 'error', summary: 'Lỗi', detail: 'Xóa thất bại', life: 2000 })
  }
}

// Export Excel
const exportExcel = async () => {
  try {
    const res = await axios.get(`${API_URL}/export-excel`, { responseType: 'blob' })
    const url = window.URL.createObjectURL(new Blob([res.data]))
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', 'materials.xlsx')
    document.body.appendChild(link)
    link.click()
    link.remove()
    window.URL.revokeObjectURL(url)
    toast.add({ severity: 'success', summary: 'Thành công', detail: 'Đã tải file Excel', life: 2000 })
  } catch (err) {
    toast.add({ severity: 'error', summary: 'Lỗi', detail: 'Xuất Excel thất bại', life: 2000 })
  }
}

// Format date
const formatDate = (date: string | null) => {
  if (!date) return '—'
  return new Date(date).toLocaleDateString('vi-VN', {
    day: '2-digit', month: '2-digit', year: 'numeric',
    hour: '2-digit', minute: '2-digit'
  })
}

// Pagination logic
const pages = computed(() => {
  const arr: number[] = []
  const max = 5
  let start = Math.max(0, currentPage.value - 2)
  let end = Math.min(totalPages.value, start + max)
  if (end - start < max) start = Math.max(0, end - max)
  for (let i = start; i < end; i++) arr.push(i)
  return arr
})

const goToPage = (p: number) => {
  if (p >= 0 && p < totalPages.value) currentPage.value = p
}

let timeout: any = null
watch(search, () => {
  clearTimeout(timeout)
  timeout = setTimeout(() => { currentPage.value = 0; loadData() }, 400)
})
watch([pageSize, sortOption], () => { currentPage.value = 0; loadData() })
watch(currentPage, () => loadData())

onMounted(() => {
  loadData()
})
</script>

<template>
  <div class="m-page">
    <Toast />

    <div class="m-header">
      <div class="m-header-left">
        <i class="fas fa-box-open m-icon"></i>
        <h2>Quản lý Master Materials</h2>
      </div>
      <div class="m-header-actions">
        <button class="m-btn m-btn-add" @click="openAdd">
          <i class="fas fa-plus"></i> Thêm nguyên liệu
        </button>
        <button class="m-btn m-btn-export" @click="exportExcel">
          <i class="fas fa-file-excel"></i> Xuất Excel
        </button>
      </div>
    </div>

    <div class="m-toolbar">
      <div class="m-toolbar-left">
        <div class="m-select-group">
          <label>Hiển thị:</label>
          <select v-model="pageSize">
            <option v-for="o in pageSizeOptions" :key="o" :value="o">{{ o }}</option>
          </select>
        </div>
        <div class="m-select-group">
          <label>Sắp xếp:</label>
          <select v-model="sortOption">
            <option value="newest">Mới nhất</option>
            <option value="oldest">Cũ nhất</option>
            <option value="az">Tên A-Z</option>
            <option value="za">Tên Z-A</option>
          </select>
        </div>
      </div>
      <div class="m-toolbar-right">
        <div class="m-search-box">
          <i class="fas fa-search"></i>
          <input type="text" v-model="search" placeholder="Tìm theo ID, tên nguyên liệu...">
        </div>
      </div>
    </div>

    <div class="m-table-wrapper">
      <table class="m-table">
        <thead>
          <tr>
            <th style="width:60px">STT</th>
            <th style="width:100px">ID</th>
            <th>Tên nguyên liệu</th>
            <th style="width:180px">Ngày tạo</th>
            <th style="width:180px">Ngày sửa</th>
            <th style="width:120px">Thao tác</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading">
            <td colspan="6" class="text-center py-5"><i class="fas fa-spinner fa-spin"></i> Đang tải...</td>
          </tr>
          <tr v-else-if="items.length === 0">
            <td colspan="6" class="text-center py-5">Không tìm thấy dữ liệu</td>
          </tr>
          <tr v-for="(item, idx) in items" :key="item.id" class="m-row">
            <td class="text-center">{{ currentPage * pageSize + idx + 1 }}</td>
            <td class="text-center">{{ item.id }}</td>
            <td><span class="m-badge">{{ item.nameMaterial }}</span></td>
            <td class="text-center">{{ formatDate(item.createdAt) }}</td>
            <td class="text-center">{{ formatDate(item.updatedAt) }}</td>
            <td class="text-center">
              <div class="m-actions">
                <button class="m-action-btn m-edit" @click="openEdit(item)"><i class="fas fa-edit"></i></button>
                <button class="m-action-btn m-delete" @click="deleteItem(item.id)"><i class="fas fa-trash"></i></button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div class="m-pagination-bar" v-if="totalPages > 0">
      <div>Hiển thị {{ currentPage * pageSize + 1 }} - {{ Math.min((currentPage+1)*pageSize, totalItems) }} / {{ totalItems }} mục</div>
      <div class="m-pagination">
        <button class="m-page-btn" :disabled="currentPage === 0" @click="goToPage(currentPage - 1)"><i class="fas fa-chevron-left"></i></button>
        <button v-for="p in pages" :key="p" class="m-page-btn" :class="{ 'm-page-active': p === currentPage }" @click="goToPage(p)">{{ p + 1 }}</button>
        <button class="m-page-btn" :disabled="currentPage >= totalPages - 1" @click="goToPage(currentPage + 1)"><i class="fas fa-chevron-right"></i></button>
      </div>
    </div>

    <Dialog v-model:visible="displayDialog" :header="isEdit ? '✏️ Sửa nguyên liệu' : '➕ Thêm nguyên liệu mới'" :modal="true" class="m-dialog" :style="{ width: '400px' }">
      <div class="m-form">
        <div class="m-field">
          <label>Tên nguyên liệu</label>
          <InputText v-model="currentItem.nameMaterial" placeholder="VD: Cotton, Polyester, Da..." class="m-input" />
        </div>
      </div>
      <template #footer>
        <div class="m-dialog-footer">
          <Button label="Hủy" icon="pi pi-times" class="p-button-text p-button-secondary" @click="displayDialog = false"/>
          <Button label="Lưu lại" icon="pi pi-check" class="p-button-primary m-save-btn" @click="saveItem" :loading="loading" />
        </div>
      </template>
    </Dialog>
  </div>
</template>

<style scoped>
.m-page { padding: 24px; font-family: 'Inter', sans-serif; }
.m-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; border-bottom: 2px solid #f1f5f9; padding-bottom: 16px; }
.m-header-left { display: flex; align-items: center; gap: 12px; }
.m-icon { font-size: 28px; color: #f59e0b; }
.m-header-left h2 { margin: 0; font-size: 22px; font-weight: 700; color: #1e293b; }

.m-btn { display: flex; align-items: center; gap: 8px; padding: 10px 18px; border: none; border-radius: 8px; font-weight: 600; cursor: pointer; transition: 0.2s; }
.m-btn-add { background: #f59e0b; color: white; }
.m-btn-export { background: #10b981; color: white; margin-left: 10px; }
.m-header-actions { display: flex; }

.m-toolbar { display: flex; justify-content: space-between; align-items: center; background: #f8fafc; padding: 12px 16px; border-radius: 12px; margin-bottom: 16px; border: 1px solid #e2e8f0; }
.m-toolbar-left { display: flex; gap: 20px; }
.m-select-group { display: flex; align-items: center; gap: 8px; font-size: 14px; }
.m-select-group select { padding: 6px 12px; border-radius: 6px; border: 1px solid #cbd5e1; outline: none; }
.m-search-box { display: flex; align-items: center; gap: 8px; background: white; border: 1px solid #cbd5e1; padding: 8px 16px; border-radius: 8px; width: 300px; }
.m-search-box input { border: none; outline: none; flex: 1; font-size: 14px; }

.m-table-wrapper { border-radius: 12px; overflow: hidden; border: 1px solid #e2e8f0; box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1); }
.m-table { width: 100%; border-collapse: collapse; background: white; }
.m-table thead { background: #f1f5f9; }
.m-table th { padding: 14px 16px; text-align: left; font-size: 13px; font-weight: 600; color: #475569; text-transform: uppercase; }
.m-table td { padding: 14px 16px; border-top: 1px solid #f1f5f9; color: #1e293b; font-size: 14px; }
.m-row:hover { background: #f8fafc; }
.m-badge { background: #fef3c7; color: #92400e; padding: 4px 10px; border-radius: 6px; font-weight: 600; font-size: 12px; }

.m-actions { display: flex; gap: 8px; justify-content: center; }
.m-action-btn { width: 32px; height: 32px; border: none; border-radius: 6px; cursor: pointer; transition: 0.2s; display: flex; align-items: center; justify-content: center; font-size: 14px; }
.m-edit { background: #fef3c7; color: #92400e; }
.m-delete { background: #fee2e2; color: #991b1b; }
.m-edit:hover { background: #fde68a; }
.m-delete:hover { background: #fecaca; }

.m-pagination-bar { display: flex; justify-content: space-between; align-items: center; margin-top: 20px; font-size: 14px; color: #64748b; }
.m-pagination { display: flex; gap: 6px; }
.m-page-btn { padding: 6px 12px; border: 1px solid #e2e8f0; border-radius: 6px; background: white; cursor: pointer; }
.m-page-active { background: #f59e0b; color: white; border-color: #f59e0b; }
.text-center { text-align: center; }

.m-form { display: flex; flex-direction: column; gap: 16px; padding: 10px 0; }
.m-field { display: flex; flex-direction: column; gap: 6px; }
.m-field label { font-size: 14px; font-weight: 600; color: #475569; }
.m-input { width: 100%; border-radius: 8px; }
.m-dialog-footer { display: flex; justify-content: flex-end; gap: 10px; }
.m-save-btn { border-radius: 8px; padding: 8px 20px; }
</style>
