<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import axios from 'axios'
import Toast from 'primevue/toast'
import { useToast } from 'primevue/usetoast'
import Dialog from 'primevue/dialog'
import Button from 'primevue/button'
import InputText from 'primevue/inputtext'
import Helper from '../../helper/helper'

const toast = useToast()
const CATEGORY_API = 'http://localhost:8081/category'
const STORAGE_API = 'http://localhost:8081/storage'

const categoryList = ref<any[]>([])
const totalItems = ref(0)
const totalPages = ref(0)
const loading = ref(false)

const selectedCategory = ref<any>(null)
const visibleDialog = ref(false)
const editMode = ref(false)

const search = ref('')
const pageSize = ref(10)
const currentPage = ref(0)
const sortOption = ref('newest')
const pageSizeOptions = [10, 20, 50, 100, 200]

const selectedFile = ref<File | null>(null)
const previewUrl = ref('')
const existingIconUrl = ref('')
const isUploading = ref(false)
const fileInputRef = ref<HTMLInputElement | null>(null)

const resetImageState = () => {
  selectedFile.value = null
  previewUrl.value = ''
  existingIconUrl.value = ''
  if (fileInputRef.value) fileInputRef.value.value = ''
}

const onFileSelect = (event: Event) => {
  const input = event.target as HTMLInputElement
  if (!input.files?.length) return

  const file = input.files[0]
  if (!file.type.startsWith('image/')) {
    toast.add({ severity: 'warn', summary: 'Cảnh báo', detail: 'Vui lòng chọn file ảnh', life: 2000 })
    return
  }
  if (file.size > 5 * 1024 * 1024) {
    toast.add({ severity: 'warn', summary: 'Cảnh báo', detail: 'Ảnh không được vượt quá 5MB', life: 2000 })
    return
  }

  selectedFile.value = file
  const reader = new FileReader()
  reader.onload = (e) => {
    previewUrl.value = e.target?.result as string
  }
  reader.readAsDataURL(file)
  input.value = ''
}

const triggerFileInput = () => {
  fileInputRef.value?.click()
}

const removeImage = () => {
  selectedFile.value = null
  previewUrl.value = ''
  existingIconUrl.value = ''
  if (fileInputRef.value) fileInputRef.value.value = ''
}

const uploadImage = async (file: File): Promise<string> => {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('folder', 'categories')
  const res = await axios.post(`${STORAGE_API}/upload`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
  return res.data.url
}

const displayImage = computed(() => {
  if (previewUrl.value) return previewUrl.value
  if (existingIconUrl.value) return Helper.GetImageUrl(existingIconUrl.value)
  return ''
})

const loadCategories = async () => {
  loading.value = true
  try {
    const params: Record<string, string | number> = {
      page: currentPage.value,
      size: pageSize.value,
      sort: sortOption.value
    }
    if (search.value.trim()) {
      params.search = search.value.trim()
    }
    const res = await axios.get(`${CATEGORY_API}/all`, { params })
    categoryList.value = res.data.content || []
    totalItems.value = res.data.totalElements || 0
    totalPages.value = res.data.totalPages || 0
  } catch {
    toast.add({ severity: 'error', summary: 'Lỗi', detail: 'Không thể tải danh mục', life: 2000 })
  } finally {
    loading.value = false
  }
}

const openAddDialog = () => {
  selectedCategory.value = { id: null, name: '', icon: '' }
  editMode.value = false
  visibleDialog.value = true
}

const openEditDialog = (category: any) => {
  selectedCategory.value = { ...category }
  editMode.value = true
  visibleDialog.value = true
}

const deleteCategory = async (id: number) => {
  if (!confirm('Bạn có chắc chắn muốn xóa danh mục này?')) return
  try {
    await axios.delete(`${CATEGORY_API}/delete/${id}`)
    toast.add({ severity: 'success', summary: 'Thành công', detail: 'Đã xóa danh mục', life: 1500 })
    loadCategories()
  } catch {
    toast.add({ severity: 'error', summary: 'Lỗi', detail: 'Xóa thất bại', life: 2000 })
  }
}

const saveCategory = async () => {
  if (!selectedCategory.value.name?.trim()) {
    toast.add({ severity: 'warn', summary: 'Cảnh báo', detail: 'Vui lòng nhập tên danh mục', life: 2000 })
    return
  }

  let iconUrl = existingIconUrl.value
  if (selectedFile.value) {
    isUploading.value = true
    try {
      iconUrl = await uploadImage(selectedFile.value)
    } catch {
      toast.add({ severity: 'error', summary: 'Lỗi', detail: 'Upload ảnh thất bại', life: 2000 })
      isUploading.value = false
      return
    }
    isUploading.value = false
  }

  if (!iconUrl) {
    toast.add({ severity: 'warn', summary: 'Cảnh báo', detail: 'Vui lòng chọn ảnh danh mục', life: 2000 })
    return
  }

  try {
    const payload = {
      name: selectedCategory.value.name.trim(),
      icon: iconUrl
    }
    if (editMode.value) {
      await axios.put(`${CATEGORY_API}/update/${selectedCategory.value.id}`, payload)
      toast.add({ severity: 'success', summary: 'Cập nhật', detail: 'Sửa danh mục thành công', life: 1500 })
    } else {
      await axios.post(`${CATEGORY_API}/add`, payload)
      toast.add({ severity: 'success', summary: 'Thêm mới', detail: 'Thêm danh mục thành công', life: 1500 })
    }
    visibleDialog.value = false
    loadCategories()
  } catch (err: any) {
    const msg = err.response?.data || 'Không thể lưu danh mục'
    toast.add({ severity: 'error', summary: 'Lỗi', detail: msg, life: 2000 })
  }
}

const exportExcel = async () => {
  try {
    const res = await axios.get(`${CATEGORY_API}/export-excel`, { responseType: 'blob' })
    const url = window.URL.createObjectURL(new Blob([res.data]))
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', 'categories.xlsx')
    document.body.appendChild(link)
    link.click()
    link.remove()
    window.URL.revokeObjectURL(url)
    toast.add({ severity: 'success', summary: 'Thành công', detail: 'Đã tải file Excel', life: 2000 })
  } catch {
    toast.add({ severity: 'error', summary: 'Lỗi', detail: 'Xuất Excel thất bại', life: 2000 })
  }
}

const formatDate = (date: string | null) => {
  if (!date) return '—'
  return new Date(date).toLocaleDateString('vi-VN', {
    day: '2-digit', month: '2-digit', year: 'numeric',
    hour: '2-digit', minute: '2-digit'
  })
}

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

let timeout: ReturnType<typeof setTimeout> | null = null
watch(search, () => {
  if (timeout) clearTimeout(timeout)
  timeout = setTimeout(() => {
    currentPage.value = 0
    loadCategories()
  }, 400)
})
watch([pageSize, sortOption], () => {
  currentPage.value = 0
  loadCategories()
})
watch(currentPage, () => loadCategories())

onMounted(() => loadCategories())
</script>

<template>
  <div class="cat-page">
    <Toast />

    <div class="cat-header">
      <div class="cat-header-left">
        <i class="fas fa-tags cat-icon"></i>
        <h2>Quản lý Danh mục</h2>
      </div>
      <div class="cat-header-actions">
        <button class="cat-btn cat-btn-add" @click="openAddDialog">
          <i class="fas fa-plus"></i> Thêm danh mục
        </button>
        <button class="cat-btn cat-btn-export" @click="exportExcel">
          <i class="fas fa-file-excel"></i> Xuất Excel
        </button>
      </div>
    </div>

    <div class="cat-toolbar">
      <div class="cat-toolbar-left">
        <div class="cat-select-group">
          <label>Hiển thị:</label>
          <select v-model="pageSize">
            <option v-for="o in pageSizeOptions" :key="o" :value="o">{{ o }}</option>
          </select>
        </div>
        <div class="cat-select-group">
          <label>Sắp xếp:</label>
          <select v-model="sortOption">
            <option value="newest">Mới nhất</option>
            <option value="oldest">Cũ nhất</option>
            <option value="az">A-Z</option>
            <option value="za">Z-A</option>
          </select>
        </div>
      </div>
      <div class="cat-toolbar-right">
        <div class="cat-search-box">
          <i class="fas fa-search"></i>
          <input type="text" v-model="search" placeholder="Tìm theo ID, tên danh mục...">
        </div>
      </div>
    </div>

    <div class="cat-table-wrapper">
      <table class="cat-table">
        <thead>
          <tr>
            <th style="width:60px">STT</th>
            <th style="width:80px">ID</th>
            <th>Tên danh mục</th>
            <th style="width:100px">Ảnh</th>
            <th style="width:180px">Ngày tạo</th>
            <th style="width:180px">Ngày sửa</th>
            <th style="width:120px">Thao tác</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading">
            <td colspan="7" class="text-center py-5"><i class="fas fa-spinner fa-spin"></i> Đang tải...</td>
          </tr>
          <tr v-else-if="categoryList.length === 0">
            <td colspan="7" class="text-center py-5">Không tìm thấy dữ liệu</td>
          </tr>
          <tr v-for="(item, idx) in categoryList" :key="item.id" class="cat-row">
            <td class="text-center">{{ currentPage * pageSize + idx + 1 }}</td>
            <td class="text-center">{{ item.id }}</td>
            <td><span class="cat-badge">{{ item.name }}</span></td>
            <td class="text-center">
              <img
                v-if="item.icon"
                :src="Helper.GetImageUrl(item.icon)"
                alt="Ảnh"
                class="cat-thumb"
              />
              <span v-else>—</span>
            </td>
            <td class="text-center">{{ formatDate(item.createdAt) }}</td>
            <td class="text-center">{{ formatDate(item.updatedAt) }}</td>
            <td class="text-center">
              <div class="cat-actions">
                <button class="cat-action-btn cat-edit" @click="openEditDialog(item)"><i class="fas fa-edit"></i></button>
                <button class="cat-action-btn cat-delete" @click="deleteCategory(item.id)"><i class="fas fa-trash"></i></button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div class="cat-pagination-bar" v-if="totalPages > 0">
      <div>
        Hiển thị {{ currentPage * pageSize + 1 }} -
        {{ Math.min((currentPage + 1) * pageSize, totalItems) }} / {{ totalItems }} mục
      </div>
      <div class="cat-pagination">
        <button class="cat-page-btn" :disabled="currentPage === 0" @click="goToPage(currentPage - 1)">
          <i class="fas fa-chevron-left"></i>
        </button>
        <button
          v-for="p in pages"
          :key="p"
          class="cat-page-btn"
          :class="{ 'cat-page-active': p === currentPage }"
          @click="goToPage(p)"
        >{{ p + 1 }}</button>
        <button class="cat-page-btn" :disabled="currentPage >= totalPages - 1" @click="goToPage(currentPage + 1)">
          <i class="fas fa-chevron-right"></i>
        </button>
      </div>
    </div>

    <Dialog
      v-model:visible="visibleDialog"
      :header="editMode ? '✏️ Sửa danh mục' : '➕ Thêm danh mục mới'"
      modal
      class="cat-dialog"
      :style="{ width: '500px' }"
    >
      <div class="cat-form">
        <div v-if="editMode" class="cat-field">
          <label>ID</label>
          <InputText v-model="selectedCategory.id" class="cat-input" disabled />
        </div>
        <div class="cat-field">
          <label>Tên danh mục</label>
          <InputText v-model="selectedCategory.name" placeholder="VD: Tạ, Xe đạp..." class="cat-input" />
        </div>
        <div class="cat-field">
          <label>Ảnh danh mục</label>
          <div class="cat-upload-section">
            <div v-if="displayImage" class="cat-image-preview">
              <img :src="displayImage" alt="Preview" />
              <button type="button" class="cat-remove-img" @click="removeImage" title="Xóa ảnh">
                <i class="pi pi-times"></i>
              </button>
              <span v-if="previewUrl" class="cat-img-badge new">Mới</span>
              <span v-else-if="existingIconUrl" class="cat-img-badge saved">Đã lưu</span>
            </div>
            <div class="cat-upload-area" @click="triggerFileInput">
              <input
                ref="fileInputRef"
                type="file"
                accept="image/*"
                hidden
                @change="onFileSelect"
              />
              <i class="pi pi-cloud-upload"></i>
              <p>{{ displayImage ? 'Nhấn để đổi ảnh khác' : 'Nhấn để chọn ảnh' }}</p>
              <span class="cat-upload-hint">JPG, PNG, WEBP — Tối đa 5MB</span>
            </div>
          </div>
        </div>
      </div>
      <template #footer>
        <div class="cat-dialog-footer">
          <Button
            label="Hủy"
            icon="pi pi-times"
            class="p-button-text p-button-secondary"
            @click="visibleDialog = false"
            :disabled="isUploading"
          />
          <Button
            :label="isUploading ? 'Đang tải ảnh...' : 'Lưu lại'"
            :icon="isUploading ? 'pi pi-spin pi-spinner' : 'pi pi-check'"
            class="p-button-primary cat-save-btn"
            @click="saveCategory"
            :disabled="isUploading"
          />
        </div>
      </template>
    </Dialog>
  </div>
</template>

<style scoped>
.cat-page { padding: 24px; font-family: 'Inter', sans-serif; }
.cat-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; border-bottom: 2px solid #f1f5f9; padding-bottom: 16px; }
.cat-header-left { display: flex; align-items: center; gap: 12px; }
.cat-icon { font-size: 28px; color: #f59e0b; }
.cat-header-left h2 { margin: 0; font-size: 22px; font-weight: 700; color: #1e293b; }

.cat-btn { display: flex; align-items: center; gap: 8px; padding: 10px 18px; border: none; border-radius: 8px; font-weight: 600; cursor: pointer; transition: 0.2s; }
.cat-btn-add { background: #f59e0b; color: white; }
.cat-btn-export { background: #10b981; color: white; margin-left: 10px; }
.cat-header-actions { display: flex; }

.cat-toolbar { display: flex; justify-content: space-between; align-items: center; background: #f8fafc; padding: 12px 16px; border-radius: 12px; margin-bottom: 16px; border: 1px solid #e2e8f0; }
.cat-toolbar-left { display: flex; gap: 20px; }
.cat-select-group { display: flex; align-items: center; gap: 8px; font-size: 14px; }
.cat-select-group select { padding: 6px 12px; border-radius: 6px; border: 1px solid #cbd5e1; outline: none; }
.cat-search-box { display: flex; align-items: center; gap: 8px; background: white; border: 1px solid #cbd5e1; padding: 8px 16px; border-radius: 8px; width: 300px; }
.cat-search-box input { border: none; outline: none; flex: 1; font-size: 14px; }

.cat-table-wrapper { border-radius: 12px; overflow: hidden; border: 1px solid #e2e8f0; box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1); }
.cat-table { width: 100%; border-collapse: collapse; background: white; }
.cat-table thead { background: #f1f5f9; }
.cat-table th { padding: 14px 16px; text-align: left; font-size: 13px; font-weight: 600; color: #475569; text-transform: uppercase; }
.cat-table td { padding: 14px 16px; border-top: 1px solid #f1f5f9; color: #1e293b; font-size: 14px; }
.cat-row:hover { background: #f8fafc; }
.cat-badge { background: #fef3c7; color: #92400e; padding: 4px 10px; border-radius: 6px; font-weight: 600; font-size: 12px; }
.cat-thumb { width: 48px; height: 48px; object-fit: cover; border-radius: 6px; border: 1px solid #e2e8f0; }

.cat-actions { display: flex; gap: 8px; justify-content: center; }
.cat-action-btn { width: 32px; height: 32px; border: none; border-radius: 6px; cursor: pointer; transition: 0.2s; display: flex; align-items: center; justify-content: center; font-size: 14px; }
.cat-edit { background: #fef3c7; color: #92400e; }
.cat-delete { background: #fee2e2; color: #991b1b; }
.cat-edit:hover { background: #fde68a; }
.cat-delete:hover { background: #fecaca; }

.cat-pagination-bar { display: flex; justify-content: space-between; align-items: center; margin-top: 20px; font-size: 14px; color: #64748b; }
.cat-pagination { display: flex; gap: 6px; }
.cat-page-btn { padding: 6px 12px; border: 1px solid #e2e8f0; border-radius: 6px; background: white; cursor: pointer; }
.cat-page-btn:disabled { opacity: 0.5; cursor: not-allowed; }
.cat-page-active { background: #f59e0b; color: white; border-color: #f59e0b; }
.text-center { text-align: center; }
.py-5 { padding: 20px 0; }

.cat-form { display: flex; flex-direction: column; gap: 16px; padding: 10px 0; }
.cat-field { display: flex; flex-direction: column; gap: 6px; }
.cat-field label { font-size: 14px; font-weight: 600; color: #475569; }
.cat-input { width: 100%; border-radius: 8px; }
.cat-upload-section { display: flex; flex-direction: column; gap: 12px; }
.cat-image-preview { position: relative; display: inline-block; width: fit-content; }
.cat-image-preview img { max-width: 160px; max-height: 160px; border-radius: 8px; object-fit: cover; border: 1px solid #e2e8f0; display: block; }
.cat-remove-img { position: absolute; top: 6px; right: 6px; width: 28px; height: 28px; border: none; border-radius: 50%; background: rgba(0,0,0,0.6); color: white; cursor: pointer; display: flex; align-items: center; justify-content: center; }
.cat-img-badge { position: absolute; bottom: 6px; left: 6px; font-size: 11px; padding: 2px 8px; border-radius: 4px; font-weight: 600; }
.cat-img-badge.new { background: #dbeafe; color: #1d4ed8; }
.cat-img-badge.saved { background: #d1fae5; color: #065f46; }
.cat-upload-area { border: 2px dashed #cbd5e1; border-radius: 10px; padding: 24px; text-align: center; cursor: pointer; transition: 0.2s; background: #f8fafc; }
.cat-upload-area:hover { border-color: #f59e0b; background: #fffbeb; }
.cat-upload-area i { font-size: 2rem; color: #f59e0b; }
.cat-upload-area p { margin: 8px 0 4px; font-size: 14px; color: #475569; font-weight: 500; }
.cat-upload-hint { font-size: 12px; color: #94a3b8; }
.cat-dialog-footer { display: flex; justify-content: flex-end; gap: 10px; }
.cat-save-btn { border-radius: 8px; padding: 8px 20px; }
</style>
