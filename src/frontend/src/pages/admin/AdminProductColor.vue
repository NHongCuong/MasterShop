<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import axios from 'axios'
import Toast from 'primevue/toast'
import { useToast } from 'primevue/usetoast'
import Dialog from 'primevue/dialog'
import Dropdown from 'primevue/dropdown'
import Button from 'primevue/button'

const toast = useToast()
const API_URL = 'http://localhost:8081/admin/product-color'

// Data
const items = ref<any[]>([])
const totalItems = ref(0)
const totalPages = ref(0)
const loading = ref(false)

// Options for dropdowns
const colorOptions = ref<any[]>([])
const productOptions = ref<any[]>([])

// Dialog state
const displayDialog = ref(false)
const isEdit = ref(false)
const oldKeys = ref({ idColor: 0, idProduct: 0 })
const currentItem = ref({
  idColor: null,
  idProduct: null
})

// Import state
const importFileRef = ref<HTMLInputElement | null>(null)
const importPreviewList = ref<any[]>([])
const visibleImportDialog = ref(false)
const isConfirmingImport = ref(false)

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
    console.error('Lỗi tải dữ liệu:', err)
    toast.add({ severity: 'error', summary: 'Lỗi', detail: 'Không thể tải danh sách Product Color', life: 3000 })
  } finally {
    loading.value = false
  }
}

const loadBaseOptions = async () => {
  try {
    const [cRes, pRes] = await Promise.all([
      axios.get('http://localhost:8081/color/list'),
      axios.get('http://localhost:8081/product/all')
    ])
    const colorData = Array.isArray(cRes.data) ? cRes.data : (cRes.data.content || [])
    const productData = Array.isArray(pRes.data) ? pRes.data : (pRes.data.content || [])
    colorOptions.value = colorData.map((c: any) => ({ label: c.nameColor, value: c.id }))
    productOptions.value = productData.map((p: any) => ({ label: p.name, value: p.id }))
  } catch (err) {
    console.error('Lỗi tải options:', err)
  }
}

// Add / Edit
const openAdd = () => {
  currentItem.value = { idColor: null, idProduct: null }
  isEdit.value = false
  displayDialog.value = true
}

const openEdit = (item: any) => {
  currentItem.value = { idColor: item.idColor, idProduct: item.idProduct }
  oldKeys.value = { idColor: item.idColor, idProduct: item.idProduct }
  isEdit.value = true
  displayDialog.value = true
}

const saveItem = async () => {
  if (!currentItem.value.idColor || !currentItem.value.idProduct) {
    toast.add({ severity: 'warn', summary: 'Cảnh báo', detail: 'Vui lòng chọn đầy đủ màu và sản phẩm', life: 2000 })
    return
  }

  try {
    if (isEdit.value) {
      await axios.put(`${API_URL}/update`, currentItem.value, {
        params: {
          oldIdColor: oldKeys.value.idColor,
          oldIdProduct: oldKeys.value.idProduct
        }
      })
      toast.add({ severity: 'success', summary: 'Thành công', detail: 'Đã cập nhật Product Color', life: 2000 })
    } else {
      await axios.post(`${API_URL}/add`, currentItem.value)
      toast.add({ severity: 'success', summary: 'Thành công', detail: 'Đã thêm mới Product Color', life: 2000 })
    }
    displayDialog.value = false
    loadData()
  } catch (err: any) {
    const msg = err.response?.data || 'Lưu thất bại'
    toast.add({ severity: 'error', summary: 'Lỗi', detail: msg, life: 3000 })
  }
}

// Delete
const deleteItem = async (idColor: number, idProduct: number) => {
  if (!confirm('Bạn có chắc muốn xóa mục này?')) return
  try {
    await axios.delete(`${API_URL}/delete`, { params: { idColor, idProduct } })
    toast.add({ severity: 'success', summary: 'Thành công', detail: 'Đã xóa Product Color', life: 2000 })
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
    link.setAttribute('download', 'product_colors.xlsx')
    document.body.appendChild(link)
    link.click()
    link.remove()
    window.URL.revokeObjectURL(url)
    toast.add({ severity: 'success', summary: 'Thành công', detail: 'Đã tải file Excel', life: 2000 })
  } catch (err) {
    toast.add({ severity: 'error', summary: 'Lỗi', detail: 'Xuất Excel thất bại', life: 2000 })
  }
}

// Import Excel logic
const triggerImportFile = () => importFileRef.value?.click()
const onImportFileSelect = async (event: Event) => {
    const input = event.target as HTMLInputElement
    if (!input.files || input.files.length === 0) return
    const file = input.files[0]
    const formData = new FormData()
    formData.append('file', file)
    loading.value = true
    try {
        const res = await axios.post(`${API_URL}/preview-import`, formData, {
            headers: { 'Content-Type': 'multipart/form-data' }
        })
        importPreviewList.value = res.data
        visibleImportDialog.value = true
    } catch (err: any) {
        toast.add({ severity: 'error', summary: 'Lỗi', detail: err.response?.data || 'Không thể đọc file Excel', life: 3000 })
    } finally {
        loading.value = false
        input.value = ''
    }
}
const confirmImport = async () => {
    const validItems = importPreviewList.value.filter(x => x.isValid && !x.exists)
    if (validItems.length === 0) {
        toast.add({ severity: 'warn', summary: 'Thông báo', detail: 'Không có dữ liệu mới hợp lệ để nhập', life: 3000 })
        return
    }
    isConfirmingImport.value = true
    try {
        const res = await axios.post(`${API_URL}/confirm-import`, validItems)
        toast.add({ severity: 'success', summary: 'Thành công', detail: res.data, life: 2500 })
        visibleImportDialog.value = false
        loadData()
    } catch (err: any) {
        toast.add({ severity: 'error', summary: 'Lỗi', detail: err.response?.data || 'Import thất bại', life: 3000 })
    } finally {
        isConfirmingImport.value = false
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

// Pagination
const pages = computed(() => {
  const arr: number[] = []
  const maxVisible = 5
  let start = Math.max(0, currentPage.value - Math.floor(maxVisible / 2))
  let end = Math.min(totalPages.value, start + maxVisible)
  if (end - start < maxVisible) start = Math.max(0, end - maxVisible)
  for (let i = start; i < end; i++) arr.push(i)
  return arr
})

const goToPage = (p: number) => {
  if (p < 0 || p >= totalPages.value) return
  currentPage.value = p
}

// Watchers
let searchTimeout: any = null
watch(search, () => {
  clearTimeout(searchTimeout)
  searchTimeout = setTimeout(() => {
    currentPage.value = 0
    loadData()
  }, 400)
})
watch([pageSize, sortOption], () => {
  currentPage.value = 0
  loadData()
})
watch(currentPage, () => {
  loadData()
})

// Showing info
const showingFrom = computed(() => totalItems.value === 0 ? 0 : currentPage.value * pageSize.value + 1)
const showingTo = computed(() => Math.min((currentPage.value + 1) * pageSize.value, totalItems.value))

onMounted(() => {
  loadData()
  loadBaseOptions()
})
</script>

<template>
  <div class="pc-page">
    <Toast />

    <!-- Header -->
    <div class="pc-header">
      <div class="pc-header-left">
        <i class="fas fa-palette pc-icon"></i>
        <h2>Quản lý Product Color</h2>
      </div>
      <div class="pc-header-actions">
        <button class="pc-btn pc-btn-add" @click="openAdd">
          <i class="fas fa-plus"></i> Thêm mới
        </button>
        <button class="pc-btn pc-btn-import" @click="triggerImportFile">
          <i class="fas fa-file-import"></i> Nhập Excel
        </button>
        <input type="file" ref="importFileRef" hidden accept=".xlsx, .xls" @change="onImportFileSelect" />
        <button class="pc-btn pc-btn-export" @click="exportExcel">
          <i class="fas fa-file-excel"></i> Export Excel
        </button>
      </div>
    </div>

    <!-- Toolbar: search, sort, pageSize -->
    <div class="pc-toolbar">
      <div class="pc-toolbar-left">
        <div class="pc-select-group">
          <label>Hiển thị:</label>
          <select v-model="pageSize">
            <option v-for="opt in pageSizeOptions" :key="opt" :value="opt">{{ opt }}</option>
          </select>
        </div>
        <div class="pc-select-group">
          <label>Sắp xếp:</label>
          <select v-model="sortOption">
            <option value="newest">Mới nhất</option>
            <option value="oldest">Cũ nhất</option>
            <option value="az">A - Z</option>
            <option value="za">Z - A</option>
          </select>
        </div>
      </div>
      <div class="pc-toolbar-right">
        <div class="pc-search-box">
          <i class="fas fa-search"></i>
          <input type="text" v-model="search" placeholder="Tìm theo tên màu, tên sản phẩm...">
        </div>
      </div>
    </div>

    <!-- Table -->
    <div class="pc-table-wrapper">
      <table class="pc-table">
        <thead>
          <tr>
            <th style="width:60px">STT</th>
            <th>Tên màu</th>
            <th>Tên sản phẩm</th>
            <th style="width:160px">Ngày tạo</th>
            <th style="width:160px">Ngày sửa</th>
            <th style="width:110px">Thao tác</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading">
            <td colspan="6" class="pc-empty">
              <i class="fas fa-spinner fa-spin"></i> Đang tải...
            </td>
          </tr>
          <tr v-else-if="items.length === 0">
            <td colspan="6" class="pc-empty">
              <i class="fas fa-inbox"></i> Không có dữ liệu
            </td>
          </tr>
          <tr v-for="(item, index) in items" :key="item.idColor + '-' + item.idProduct" class="pc-row">
            <td class="text-center">{{ currentPage * pageSize + index + 1 }}</td>
            <td>
              <span class="pc-color-badge">
                {{ item.nameColor || '—' }}
              </span>
            </td>
            <td>{{ item.productName || '—' }}</td>
            <td class="text-center">{{ formatDate(item.createdAt) }}</td>
            <td class="text-center">{{ formatDate(item.updatedAt) }}</td>
            <td class="text-center">
              <div class="pc-actions">
                <button class="pc-action-btn pc-action-edit" title="Sửa" @click="openEdit(item)">
                  <i class="fas fa-edit"></i>
                </button>
                <button class="pc-action-btn pc-action-delete" title="Xóa"
                        @click="deleteItem(item.idColor, item.idProduct)">
                  <i class="fas fa-trash-alt"></i>
                </button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Pagination -->
    <div class="pc-pagination-bar" v-if="totalPages > 0">
      <div class="pc-pagination-info">
        Hiển thị {{ showingFrom }} - {{ showingTo }} / {{ totalItems }} mục
      </div>
      <div class="pc-pagination">
        <button class="pc-page-btn" :disabled="currentPage === 0" @click="goToPage(0)">
          <i class="fas fa-angle-double-left"></i>
        </button>
        <button class="pc-page-btn" :disabled="currentPage === 0" @click="goToPage(currentPage - 1)">
          <i class="fas fa-angle-left"></i>
        </button>
        <button v-for="p in pages" :key="p"
                class="pc-page-btn" :class="{ 'pc-page-active': p === currentPage }"
                @click="goToPage(p)">
          {{ p + 1 }}
        </button>
        <button class="pc-page-btn" :disabled="currentPage >= totalPages - 1" @click="goToPage(currentPage + 1)">
          <i class="fas fa-angle-right"></i>
        </button>
        <button class="pc-page-btn" :disabled="currentPage >= totalPages - 1" @click="goToPage(totalPages - 1)">
          <i class="fas fa-angle-double-right"></i>
        </button>
      </div>
    </div>

    <!-- Dialog Thêm/Sửa -->
    <Dialog v-model:visible="displayDialog" :header="isEdit ? '✏️ Chỉnh sửa Product Color' : '➕ Thêm mới Product Color'" 
            :modal="true" class="pc-dialog" :style="{ width: '500px' }">
      <div class="pc-form">
        <div class="pc-field">
          <label class="pc-label">Sản phẩm</label>
          <Dropdown v-model="currentItem.idProduct" :options="productOptions" optionLabel="label" optionValue="value" 
                    placeholder="Chọn sản phẩm" filter class="pc-dropdown" />
        </div>
        <div class="pc-field">
          <label class="pc-label">Màu sắc</label>
          <Dropdown v-model="currentItem.idColor" :options="colorOptions" optionLabel="label" optionValue="value" 
                    placeholder="Chọn màu sắc" filter class="pc-dropdown" />
        </div>
      </div>
      <template #footer>
        <div class="pc-dialog-footer">
          <Button label="Hủy" icon="pi pi-times" class="p-button-text p-button-secondary" @click="displayDialog = false"/>
          <Button :label="isEdit ? 'Cập nhật' : 'Thêm mới'" :icon="isEdit ? 'pi pi-check' : 'pi pi-plus'" 
                  class="p-button-primary pc-save-btn" @click="saveItem" :loading="loading" />
        </div>
      </template>
    </Dialog>

    <!-- Dialog Preview Import -->
    <Dialog v-model:visible="visibleImportDialog" modal class="pc-import-dialog" :style="{ width: '800px' }" header="📥 Nhập liên kết Sản phẩm - Màu sắc">
        <div class="pc-import-review">
            <div class="pc-info-bar">
                <i class="fas fa-info-circle"></i>
                <span>Xem trước <b>{{ importPreviewList.length }}</b> sản phẩm màu sắc sẽ được nhập. Hệ thống sẽ tự động bỏ qua màu đã tồn tại.</span>
            </div>
            
            <div class="pc-import-table-wrapper">
                <table class="pc-import-table">
                    <thead>
                        <tr>
                            <th style="width: 60px">STT</th>
                            <th>Tên màu sắc</th>
                            <th>Tên sản phẩm</th>
                            <th style="width: 200px">Trạng thái</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr v-for="(item, idx) in importPreviewList" :key="idx">
                            <td class="text-center font-bold">{{ idx + 1 }}</td>
                            <td class="font-semibold">{{ item.colorName }}</td>
                            <td>{{ item.productName }}</td>
                            <td class="text-center">
                                <span v-if="!item.isValid" class="p-tag-error">{{ item.errors }}</span>
                                <span v-else-if="item.exists" class="p-tag-warn">Đã tồn tại</span>
                                <span v-else class="p-tag-new">Mới</span>
                            </td>
                        </tr>
                    </tbody>
                </table>
            </div>
        </div>
        
        <template #footer>
            <div class="pc-modal-footer">
                <button class="pc-footer-btn pc-btn-cancel" @click="visibleImportDialog = false" :disabled="isConfirmingImport">
                    <i class="fas fa-times mr-2"></i>Hủy bỏ
                </button>
                <button class="pc-footer-btn pc-btn-confirm-import" @click="confirmImport" :disabled="isConfirmingImport">
                    <template v-if="isConfirmingImport">
                        <i class="fas fa-spinner fa-spin mr-2"></i>Đang nhập...
                    </template>
                    <template v-else>
                        <i class="fas fa-check-circle mr-2"></i>Xác nhận nhập
                    </template>
                </button>
            </div>
        </template>
    </Dialog>
  </div>
</template>

<style scoped>
.pc-page {
  padding: 24px 28px;
  font-family: 'Inter', 'Segoe UI', sans-serif;
  color: #1e293b;
}

/* Header */
.pc-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 2px solid #e2e8f0;
}
.pc-header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}
.pc-header-left h2 {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: #1e293b;
}
.pc-icon {
  font-size: 28px;
  color: #6366f1;
}

/* Buttons */
.pc-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  color: white;
}
.pc-btn-export {
  background: linear-gradient(135deg, #059669, #10b981);
  box-shadow: 0 2px 8px rgba(16, 185, 129, 0.3);
}
.pc-btn-import {
    background: #6366f1;
    box-shadow: 0 2px 8px rgba(99, 102, 241, 0.3);
}
.pc-btn-add {
  background: linear-gradient(135deg, #4f46e5, #6366f1);
  box-shadow: 0 2px 8px rgba(99, 102, 241, 0.3);
}
.pc-btn:hover {
  transform: translateY(-1px);
  opacity: 0.95;
}
.pc-header-actions {
  display: flex;
  gap: 12px;
}

/* Toolbar */
.pc-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 16px;
  padding: 12px 16px;
  background: #f8fafc;
  border-radius: 10px;
  border: 1px solid #e2e8f0;
}
.pc-toolbar-left {
  display: flex;
  align-items: center;
  gap: 16px;
}
.pc-select-group {
  display: flex;
  align-items: center;
  gap: 6px;
}
.pc-select-group label {
  font-size: 13px;
  color: #64748b;
  font-weight: 500;
  white-space: nowrap;
}
.pc-select-group select {
  padding: 7px 12px;
  border: 1px solid #cbd5e1;
  border-radius: 6px;
  font-size: 13px;
  background: white;
  color: #334155;
  cursor: pointer;
  outline: none;
}
.pc-search-box {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 7px 14px;
  background: white;
  border: 1px solid #cbd5e1;
  border-radius: 8px;
  min-width: 280px;
}
.pc-search-box input {
  border: none;
  outline: none;
  font-size: 13px;
  color: #334155;
  flex: 1;
}

/* Table */
.pc-table-wrapper {
  overflow-x: auto;
  border-radius: 10px;
  border: 1px solid #e2e8f0;
}
.pc-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 14px;
}
.pc-table thead {
  background: linear-gradient(135deg, #4f46e5, #6366f1);
}
.pc-table thead th {
  color: white;
  padding: 14px 16px;
  text-align: left;
  font-weight: 600;
  font-size: 13px;
  text-transform: uppercase;
}
.pc-table tbody td {
  padding: 12px 16px;
  border-bottom: 1px solid #f1f5f9;
  color: #334155;
}
.pc-row:hover { background: #f1f5f9; }
.text-center { text-align: center; }

/* Badges */
.pc-color-badge {
  display: inline-block;
  padding: 4px 12px;
  background: #ede9fe;
  color: #5b21b6;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 600;
  border: 1px solid #ddd6fe;
}

/* Import Dialog Styles */
.pc-import-review { padding: 10px 20px 20px; }
.pc-info-bar { background: #eff6ff; border: 1px solid #bfdbfe; color: #1e40af; padding: 12px 16px; border-radius: 10px; margin-bottom: 16px; display: flex; align-items: center; gap: 10px; font-size: 13px; }
.pc-import-table-wrapper { border: 1px solid #e2e8f0; border-radius: 10px; overflow: auto; max-height: 400px; }
.pc-import-table { width: 100%; border-collapse: collapse; }
.pc-import-table th { background: #f8fafc; padding: 12px; font-size: 11px; color: #64748b; text-transform: uppercase; border-bottom: 2px solid #e2e8f0; text-align: left; }
.pc-import-table td { padding: 10px 12px; border-bottom: 1px solid #f1f5f9; font-size: 13px; }

.p-tag-new { background: #f0fdf4; color: #166534; padding: 2px 8px; border-radius: 4px; font-size: 11px; font-weight: 700; }
.p-tag-warn { background: #fffbeb; color: #92400e; padding: 2px 8px; border-radius: 4px; font-size: 11px; font-weight: 700; }
.p-tag-error { background: #fef2f2; color: #991b1b; padding: 2px 8px; border-radius: 4px; font-size: 11px; font-weight: 700; }

.pc-modal-footer { display: flex; justify-content: flex-end; gap: 10px; padding: 12px 0; }
.pc-footer-btn { padding: 8px 20px; border-radius: 8px; border: none; font-weight: 700; cursor: pointer; display: flex; align-items: center; }
.pc-btn-cancel { background: #f1f5f9; color: #475569; }
.pc-btn-confirm-import { background: #6366f1; color: white; }

.pc-pagination-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 16px;
}
.pc-pagination { display: flex; gap: 4px; }
.pc-page-btn { padding: 8px 12px; border: 1px solid #e2e8f0; border-radius: 6px; background: white; cursor: pointer; }
.pc-page-active { background: #6366f1; color: white; }

/* Actions */
.pc-actions { display: flex; justify-content: center; gap: 6px; }
.pc-action-btn { width: 34px; height: 34px; border: none; border-radius: 8px; cursor: pointer; display: flex; align-items: center; justify-content: center; }
.pc-action-edit { background: #fef3c7; color: #b45309; }
.pc-action-delete { background: #fee2e2; color: #dc2626; }
</style>
