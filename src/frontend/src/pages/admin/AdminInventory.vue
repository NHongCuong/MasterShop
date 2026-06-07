<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import axios from 'axios'
import Toast from 'primevue/toast'
import { useToast } from 'primevue/usetoast'
import Helper from '../../helper/helper'

const toast = useToast()
const route = useRoute()
const API = 'http://localhost:8081/product'
const LOW_STOCK_THRESHOLD = 10

const list = ref<any[]>([])
const totalItems = ref(0)
const totalPages = ref(0)
const lowStockCount = ref(0)
const loading = ref(false)

const search = ref('')
const pageSize = ref(10)
const currentPage = ref(0)
const sortOption = ref('newest')
const lowStockOnly = ref(false)
const pageSizeOptions = [10, 20, 50, 100]

const editingId = ref<number | null>(null)
const editStockValue = ref(0)

const parseStock = (val: any) => {
  const n = Number(String(val ?? '').replace(/[^0-9]/g, ''))
  return Number.isNaN(n) ? 0 : n
}

const isLowStock = (p: any) => parseStock(p?.amount) <= LOW_STOCK_THRESHOLD

const loadLowStockCount = async () => {
  try {
    const res = await axios.get(`${API}/inventory/low-stock-count`)
    lowStockCount.value = res.data?.count ?? 0
  } catch {
    lowStockCount.value = 0
  }
}

const loadInventory = async () => {
  loading.value = true
  try {
    const params: Record<string, string | number | boolean> = {
      page: currentPage.value,
      size: pageSize.value,
      sort: sortOption.value,
      lowStockOnly: lowStockOnly.value,
    }
    if (search.value.trim()) params.search = search.value.trim()

    const res = await axios.get(`${API}/inventory`, { params })
    list.value = res.data.content || []
    totalItems.value = res.data.totalElements || 0
    totalPages.value = res.data.totalPages || 0
  } catch {
    toast.add({ severity: 'error', summary: 'Lỗi', detail: 'Không thể tải tồn kho', life: 2000 })
  } finally {
    loading.value = false
  }
}

const refreshAll = async () => {
  await Promise.all([loadInventory(), loadLowStockCount()])
}

const showLowStockOnly = () => {
  lowStockOnly.value = true
  currentPage.value = 0
  loadInventory()
}

const clearLowStockFilter = () => {
  lowStockOnly.value = false
  currentPage.value = 0
  loadInventory()
}

const startEdit = (p: any) => {
  editingId.value = p.id
  editStockValue.value = parseStock(p.amount)
}

const cancelEdit = () => {
  editingId.value = null
  editStockValue.value = 0
}

const saveStock = async (p: any) => {
  if (editStockValue.value < 0) {
    toast.add({ severity: 'warn', summary: 'Cảnh báo', detail: 'Tồn kho không được âm', life: 2000 })
    return
  }
  try {
    await axios.put(`${API}/stock/${p.id}`, { amount: editStockValue.value })
    toast.add({ severity: 'success', summary: 'Thành công', detail: 'Đã cập nhật tồn kho', life: 1500 })
    cancelEdit()
    await refreshAll()
  } catch (err: any) {
    const detail = typeof err.response?.data === 'string'
      ? err.response.data
      : 'Cập nhật tồn kho thất bại'
    toast.add({ severity: 'error', summary: 'Lỗi', detail, life: 3000 })
  }
}

const deleteProduct = async (id: number) => {
  if (!confirm('Bạn có chắc muốn xóa sản phẩm này?')) return
  try {
    await axios.delete(`${API}/delete/${id}`)
    toast.add({ severity: 'success', summary: 'Thành công', detail: 'Đã xóa sản phẩm', life: 1500 })
    await refreshAll()
  } catch {
    toast.add({ severity: 'error', summary: 'Lỗi', detail: 'Xóa thất bại', life: 2000 })
  }
}

const exportExcel = async () => {
  try {
    const res = await axios.get(`${API}/export-excel`, { responseType: 'blob' })
    const url = window.URL.createObjectURL(new Blob([res.data]))
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', 'inventory.xlsx')
    document.body.appendChild(link)
    link.click()
    link.remove()
    window.URL.revokeObjectURL(url)
  } catch {
    toast.add({ severity: 'error', summary: 'Lỗi', detail: 'Xuất Excel thất bại', life: 2000 })
  }
}

const pages = computed(() => {
  const arr: number[] = []
  for (let i = 0; i < totalPages.value; i++) arr.push(i)
  return arr
})

const goToPage = (p: number) => {
  if (p < 0 || p >= totalPages.value) return
  currentPage.value = p
  loadInventory()
}

watch([pageSize, sortOption], () => {
  currentPage.value = 0
  loadInventory()
})

let searchTimer: ReturnType<typeof setTimeout>
watch(search, () => {
  clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    currentPage.value = 0
    loadInventory()
  }, 400)
})

onMounted(() => {
  if (route.query.lowStockOnly === 'true') {
    lowStockOnly.value = true
  }
  refreshAll()
})
</script>

<template>
  <Toast />
  <div class="inv-page">
    <div class="inv-header">
      <div class="inv-header-left">
        <i class="fas fa-warehouse inv-icon"></i>
        <h2>Quản lý tồn kho</h2>
      </div>
    </div>

    <div v-if="lowStockCount > 0" class="inv-alert">
      <div class="inv-alert-text">
        <i class="fas fa-exclamation-triangle"></i>
        Có <strong>{{ lowStockCount }}</strong> sản phẩm sắp hết hàng!
      </div>
      <button v-if="!lowStockOnly" class="inv-alert-btn" @click="showLowStockOnly">Xem sắp hết</button>
      <button v-else class="inv-alert-btn inv-alert-btn-clear" @click="clearLowStockFilter">Xem tất cả</button>
    </div>

    <div class="inv-toolbar">
      <div class="inv-toolbar-left">
        <div class="inv-select-group">
          <span>Hiển thị:</span>
          <select v-model.number="pageSize">
            <option v-for="n in pageSizeOptions" :key="n" :value="n">{{ n }}</option>
          </select>
        </div>
        <button class="inv-btn inv-btn-export" @click="exportExcel">
          <i class="fas fa-file-excel"></i> Export Excel
        </button>
        <div class="inv-select-group">
          <span>Sắp xếp:</span>
          <select v-model="sortOption">
            <option value="newest">Mới nhất</option>
            <option value="oldest">Cũ nhất</option>
            <option value="az">Tên A-Z</option>
            <option value="stock_asc">Tồn kho tăng dần</option>
            <option value="stock_desc">Tồn kho giảm dần</option>
          </select>
        </div>
      </div>
      <div class="inv-search-box">
        <i class="fas fa-search"></i>
        <input v-model="search" type="text" placeholder="Tìm theo tên hoặc danh mục">
      </div>
    </div>

    <div class="inv-table-wrapper">
      <table class="inv-table">
        <thead>
          <tr>
            <th style="width:60px">STT</th>
            <th>Tên sản phẩm</th>
            <th style="width:160px">Danh mục</th>
            <th style="width:180px">Tồn kho</th>
            <th style="width:100px">Đã bán</th>
            <th style="width:170px">Ngày tạo</th>
            <th style="width:170px">Ngày sửa</th>
            <th style="width:120px">Thao tác</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading">
            <td colspan="8" class="text-center py-5"><i class="fas fa-spinner fa-spin"></i> Đang tải...</td>
          </tr>
          <tr v-else-if="list.length === 0">
            <td colspan="8" class="text-center py-5">Không tìm thấy sản phẩm</td>
          </tr>
          <tr v-for="(p, idx) in list" :key="p.id" class="inv-row">
            <td class="text-center">{{ currentPage * pageSize + idx + 1 }}</td>
            <td class="inv-name">{{ p.name }}</td>
            <td>{{ p.category?.name || '—' }}</td>
            <td>
              <div v-if="editingId === p.id" class="inv-stock-edit">
                <input v-model.number="editStockValue" type="number" min="0" class="inv-stock-input" />
                <button class="inv-stock-save" title="Lưu" @click="saveStock(p)"><i class="fas fa-check"></i></button>
                <button class="inv-stock-cancel" title="Hủy" @click="cancelEdit"><i class="fas fa-times"></i></button>
              </div>
              <div v-else class="inv-stock-display" :class="{ 'inv-stock-low': isLowStock(p) }">
                <i v-if="isLowStock(p)" class="fas fa-exclamation-triangle inv-warn-icon"></i>
                <span>{{ parseStock(p.amount) }}</span>
              </div>
            </td>
            <td class="text-center font-bold">{{ p.soldQuantity ?? 0 }}</td>
            <td class="inv-date">{{ Helper.DateFormat(p.createdAt) }}</td>
            <td class="inv-date">{{ Helper.DateFormat(p.updatedAt) }}</td>
            <td class="text-center">
              <div class="inv-actions">
                <button class="inv-action-btn inv-edit" title="Sửa tồn kho" @click="startEdit(p)">
                  <i class="fas fa-edit"></i>
                </button>
                <button class="inv-action-btn inv-delete" title="Xóa" @click="deleteProduct(p.id)">
                  <i class="fas fa-trash"></i>
                </button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div class="inv-pagination-bar" v-if="totalPages > 0">
      <div>
        Hiển thị {{ currentPage * pageSize + 1 }} -
        {{ Math.min((currentPage + 1) * pageSize, totalItems) }} / {{ totalItems }} sản phẩm
      </div>
      <div class="inv-pagination">
        <button class="inv-page-btn" :disabled="currentPage === 0" @click="goToPage(currentPage - 1)">
          <i class="fas fa-chevron-left"></i>
        </button>
        <button
          v-for="pg in pages"
          :key="pg"
          class="inv-page-btn"
          :class="{ 'inv-page-active': pg === currentPage }"
          @click="goToPage(pg)"
        >{{ pg + 1 }}</button>
        <button class="inv-page-btn" :disabled="currentPage >= totalPages - 1" @click="goToPage(currentPage + 1)">
          <i class="fas fa-chevron-right"></i>
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.inv-page { padding: 24px; font-family: 'Inter', sans-serif; }
.inv-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; border-bottom: 2px solid #f1f5f9; padding-bottom: 16px; }
.inv-header-left { display: flex; align-items: center; gap: 12px; }
.inv-icon { font-size: 28px; color: #2563eb; }
.inv-header-left h2 { margin: 0; font-size: 22px; font-weight: 700; color: #1e293b; }

.inv-alert {
  display: flex; align-items: center; justify-content: space-between; gap: 16px;
  background: linear-gradient(90deg, #fef9c3, #fef08a); border: 1px solid #fde047;
  border-radius: 10px; padding: 14px 18px; margin-bottom: 16px;
}
.inv-alert-text { display: flex; align-items: center; gap: 10px; color: #854d0e; font-size: 15px; }
.inv-alert-text i { color: #ca8a04; font-size: 18px; }
.inv-alert-btn {
  background: #fff; border: 1px solid #eab308; color: #854d0e; padding: 8px 16px;
  border-radius: 8px; font-weight: 600; cursor: pointer; white-space: nowrap;
}
.inv-alert-btn:hover { background: #fef08a; }
.inv-alert-btn-clear { border-color: #94a3b8; color: #475569; }

.inv-toolbar {
  display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 12px;
  background: #f8fafc; padding: 12px 16px; border-radius: 12px; margin-bottom: 16px; border: 1px solid #e2e8f0;
}
.inv-toolbar-left { display: flex; align-items: center; flex-wrap: wrap; gap: 16px; }
.inv-select-group { display: flex; align-items: center; gap: 8px; font-size: 14px; color: #475569; }
.inv-select-group select { padding: 6px 12px; border-radius: 6px; border: 1px solid #cbd5e1; outline: none; background: #fff; }
.inv-btn { display: inline-flex; align-items: center; gap: 8px; padding: 8px 16px; border: none; border-radius: 8px; font-weight: 600; cursor: pointer; font-size: 14px; }
.inv-btn-export { background: #16a34a; color: #fff; }
.inv-btn-export:hover { background: #15803d; }
.inv-search-box {
  display: flex; align-items: center; gap: 8px; background: #fff; border: 1px solid #cbd5e1;
  padding: 8px 16px; border-radius: 8px; width: 300px;
}
.inv-search-box input { border: none; outline: none; flex: 1; font-size: 14px; }

.inv-table-wrapper {
  border-radius: 12px; overflow-x: auto; overflow-y: hidden;
  border: 1px solid #e2e8f0; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.08);
  -webkit-overflow-scrolling: touch;
}
.inv-table-wrapper::-webkit-scrollbar { height: 10px; }
.inv-table-wrapper::-webkit-scrollbar-track { background: #f1f5f9; }
.inv-table-wrapper::-webkit-scrollbar-thumb { background: #cbd5e1; border-radius: 5px; }
.inv-table { width: 100%; min-width: 1100px; border-collapse: collapse; background: #fff; }
.inv-table thead { background: #facc15; }
.inv-table th {
  padding: 14px 16px; text-align: left; font-size: 13px; font-weight: 700;
  color: #1e293b; text-transform: uppercase; white-space: nowrap;
}
.inv-table td { padding: 14px 16px; border-top: 1px solid #fef3c7; color: #1e293b; font-size: 14px; vertical-align: middle; }
.inv-row:hover { background: #fffbeb; }
.inv-name { font-weight: 600; max-width: 280px; }
.inv-date { font-size: 13px; color: #64748b; white-space: nowrap; }

.inv-stock-display { display: flex; align-items: center; gap: 8px; font-weight: 700; }
.inv-stock-low { color: #b45309; }
.inv-warn-icon { color: #f59e0b; font-size: 16px; }

.inv-stock-edit { display: flex; align-items: center; gap: 6px; }
.inv-stock-input {
  width: 80px; padding: 6px 10px; border: 2px solid #3b82f6; border-radius: 6px;
  font-weight: 700; font-size: 14px; outline: none;
}
.inv-stock-save, .inv-stock-cancel {
  width: 32px; height: 32px; border: none; border-radius: 6px; cursor: pointer;
  display: flex; align-items: center; justify-content: center; font-size: 14px;
}
.inv-stock-save { background: #22c55e; color: #fff; }
.inv-stock-save:hover { background: #16a34a; }
.inv-stock-cancel { background: #e2e8f0; color: #64748b; }
.inv-stock-cancel:hover { background: #cbd5e1; }

.inv-actions { display: flex; gap: 8px; justify-content: center; }
.inv-action-btn {
  width: 34px; height: 34px; border: none; border-radius: 8px; cursor: pointer;
  display: flex; align-items: center; justify-content: center; font-size: 14px; transition: 0.2s;
}
.inv-edit { background: #dbeafe; color: #1d4ed8; }
.inv-edit:hover { background: #bfdbfe; }
.inv-delete { background: #fee2e2; color: #991b1b; }
.inv-delete:hover { background: #fecaca; }

.inv-pagination-bar { display: flex; justify-content: space-between; align-items: center; margin-top: 20px; font-size: 14px; color: #64748b; flex-wrap: wrap; gap: 12px; }
.inv-pagination { display: flex; gap: 6px; flex-wrap: wrap; }
.inv-page-btn { padding: 8px 14px; border: 1px solid #e2e8f0; border-radius: 8px; background: #fff; cursor: pointer; font-weight: 600; }
.inv-page-btn:disabled { opacity: 0.4; cursor: not-allowed; }
.inv-page-active { background: #3b82f6; color: #fff; border-color: #3b82f6; }
.text-center { text-align: center; }
.py-5 { padding: 40px 0; }
.font-bold { font-weight: 700; }
</style>
