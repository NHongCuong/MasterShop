<script setup lang="ts">
import {ref, onMounted} from 'vue'
import axios from 'axios'
import {Category, Supplier} from '../../interfaces/app.ts'
import Helper from '../../helper/helper'

import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import Button from 'primevue/button'
import Dialog from 'primevue/dialog'
import InputText from 'primevue/inputtext'
import InputNumber from 'primevue/inputnumber'
import Textarea from 'primevue/textarea'
import Dropdown from 'primevue/dropdown'
import Toast from 'primevue/toast'
import {useToast} from 'primevue/usetoast'

const productList = ref<any[]>([])
const categoryList = ref<Category[]>([])
const supplierList = ref<Supplier[]>([])
const selectedProduct = ref<any>(null)
const visibleDialog = ref(false)
const editMode = ref(false)
const toast = useToast()
const API_URL = 'http://localhost:8081/product'
const CATEGORY_API = 'http://localhost:8081/category'
const SUPPLIER_API = 'http://localhost:8081/supplier'
const STORAGE_API = 'http://localhost:8081/storage'

// ========== IMAGE UPLOAD STATE ==========
const selectedFiles = ref<File[]>([])
const previewImages = ref<string[]>([])
const uploadedImageUrls = ref<string[]>([])
const isUploading = ref(false)
const fileInputRef = ref<HTMLInputElement | null>(null)

// ========== FILE HANDLING ==========
const onFileSelect = (event: Event) => {
  const input = event.target as HTMLInputElement
  if (!input.files) return

  const files = Array.from(input.files)
  files.forEach(file => {
    if (!file.type.startsWith('image/')) {
      toast.add({severity: 'warn', summary: 'Cảnh báo', detail: `${file.name} không phải là ảnh`, life: 2000})
      return
    }
    if (file.size > 5 * 1024 * 1024) {
      toast.add({severity: 'warn', summary: 'Cảnh báo', detail: `${file.name} vượt quá 5MB`, life: 2000})
      return
    }
    selectedFiles.value.push(file)

    // Tạo preview
    const reader = new FileReader()
    reader.onload = (e) => {
      previewImages.value.push(e.target?.result as string)
    }
    reader.readAsDataURL(file)
  })

  // Reset input để cho phép chọn lại cùng file
  input.value = ''
}

const removeImage = (index: number) => {
  selectedFiles.value.splice(index, 1)
  previewImages.value.splice(index, 1)
}

const removeUploadedImage = (index: number) => {
  uploadedImageUrls.value.splice(index, 1)
}

const triggerFileInput = () => {
  fileInputRef.value?.click()
}

// Upload tất cả ảnh lên Cloudinary
const uploadAllImages = async (): Promise<string[]> => {
  const urls: string[] = []

  for (const file of selectedFiles.value) {
    const formData = new FormData()
    formData.append('file', file)
    formData.append('folder', 'products')

    try {
      const res = await axios.post(`${STORAGE_API}/upload`, formData, {
        headers: {'Content-Type': 'multipart/form-data'}
      })
      urls.push(res.data.url)
    } catch (err) {
      console.error('Upload error:', err)
      toast.add({severity: 'error', summary: 'Lỗi', detail: `Upload ${file.name} thất bại`, life: 2000})
    }
  }

  return urls
}

// ========== LOAD DATA ==========
const loadProducts = async () => {
  try {
    const res = await axios.get(`${API_URL}/all`)
    productList.value = res.data
  } catch (err) {
    toast.add({severity: 'error', summary: 'Lỗi', detail: 'Không thể tải sản phẩm', life: 2000})
  }
}

const loadCategories = async () => {
  const res = await axios.get(`${CATEGORY_API}/all`)
  categoryList.value = res.data
}

const loadSuppliers = async () => {
  const res = await axios.get(`${SUPPLIER_API}/all`)
  supplierList.value = res.data
}

// ========== ADD ==========
const openAddDialog = () => {
  selectedProduct.value = {
    id: null,
    name: '',
    description: '',
    price: 0,
    avatar: '',
    amount: 0,
    category: null,
    supplier: null
  }
  selectedFiles.value = []
  previewImages.value = []
  uploadedImageUrls.value = []
  editMode.value = false
  visibleDialog.value = true
}

// ========== EDIT ==========
const openEditDialog = (product: any) => {
  selectedProduct.value = {...product}
  if (product.category && product.category.id) {
    selectedProduct.value.category = categoryList.value.find(c => c.id === product.category.id) || {id: product.category.id}
  }
  if (product.supplier && product.supplier.id) {
    selectedProduct.value.supplier = supplierList.value.find(s => s.id === product.supplier.id) || {id: product.supplier.id}
  }

  // Load ảnh hiện tại
  selectedFiles.value = []
  previewImages.value = []
  uploadedImageUrls.value = []
  
  if (product.avatar) {
    uploadedImageUrls.value.push(product.avatar)
  }
  
  // Load thêm các ảnh phụ từ danh sách productImages
  if (product.productImages && Array.isArray(product.productImages)) {
    product.productImages.forEach((img: any) => {
      if (img.imageUrl && img.imageUrl !== product.avatar) {
        uploadedImageUrls.value.push(img.imageUrl)
      }
    })
  }

  editMode.value = true
  visibleDialog.value = true
}

// ========== DELETE ==========
const deleteProduct = async (id: number) => {
  if (!confirm('Bạn có chắc chắn muốn xóa sản phẩm này?')) return
  try {
    await axios.delete(`${API_URL}/delete/${id}`)
    toast.add({severity: 'success', summary: 'Thành công', detail: 'Đã xóa sản phẩm', life: 1500})
    loadProducts()
  } catch {
    toast.add({severity: 'error', summary: 'Lỗi', detail: 'Xóa thất bại', life: 2000})
  }
}

// ========== SAVE (THÊM / SỬA) ==========
const saveProduct = async () => {
  try {
    isUploading.value = true

    // Upload ảnh mới từ file đã chọn
    let newUrls: string[] = []
    if (selectedFiles.value.length > 0) {
      newUrls = await uploadAllImages()
    }

    // Gộp ảnh cũ (đã upload) + ảnh mới upload
    const allUrls = [...uploadedImageUrls.value, ...newUrls]

    // Lấy ảnh đầu tiên làm avatar chính
    const avatarUrl = allUrls.length > 0 ? allUrls[0] : ''
    
    // Tạo danh sách productImages cho backend
    const productImages = allUrls.map(url => ({ imageUrl: url }))

    const payload = {
      name: selectedProduct.value.name,
      description: selectedProduct.value.description,
      price: selectedProduct.value.price,
      avatar: avatarUrl,
      amount: selectedProduct.value.amount,
      category: selectedProduct.value.category ? {id: selectedProduct.value.category.id} : null,
      supplier: selectedProduct.value.supplier ? {id: selectedProduct.value.supplier.id} : null,
      productImages: productImages
    }

    if (editMode.value) {
      await axios.put(`${API_URL}/update/${selectedProduct.value.id}`, payload)
      toast.add({severity: 'success', summary: 'Cập nhật', detail: 'Sửa sản phẩm thành công', life: 1500})
    } else {
      await axios.post(`${API_URL}/add`, payload)
      toast.add({severity: 'success', summary: 'Thêm mới', detail: 'Thêm sản phẩm thành công', life: 1500})
    }

    visibleDialog.value = false
    loadProducts()
  } catch (err) {
    console.error('❌ Save error:', err)
    toast.add({severity: 'error', summary: 'Lỗi', detail: 'Không thể lưu sản phẩm', life: 2000})
  } finally {
    isUploading.value = false
  }
}

onMounted(() => {
  loadProducts()
  loadCategories()
  loadSuppliers()
})
</script>

<template>
  <div class="p-6">
    <h2 class="text-2xl font-bold mb-4">📦 Quản lý sản phẩm</h2>

    <Button label="➕ Thêm sản phẩm" icon="pi pi-plus" class="mb-3" @click="openAddDialog"/>

    <DataTable :value="productList" paginator :rows="20" stripedRows tableStyle="min-width: 60rem">
      <Column field="id" header="ID" sortable/>
      <Column field="name" header="Tên sản phẩm" sortable/>
      <Column field="category.name" header="Danh mục" sortable/>
      <Column field="supplier.name" header="Nhà cung cấp" sortable/>
      <Column field="price" header="Giá" sortable/>
      <Column field="amount" header="Số lượng"/>
      <Column field="description" header="Mô tả"/>
      <Column header="Ảnh">
        <template #body="{ data }">
          <img :src="Helper.GetImageUrl(data.avatar)" alt="Ảnh" width="60" height="60" class="rounded" style="object-fit:cover;"/>
        </template>
      </Column>
      <Column header="Thao tác" style="width: 150px">
        <template #body="{ data }">
          <Button icon="pi pi-pencil" class="p-button-warning mr-2" @click="openEditDialog(data)"/>
          <Button icon="pi pi-trash" class="p-button-danger" @click="deleteProduct(data.id)"/>
        </template>
      </Column>
    </DataTable>

    <!-- Dialog -->
    <Dialog
        v-model:visible="visibleDialog"
        :header="editMode ? '✏️ Sửa sản phẩm' : '🆕 Thêm sản phẩm'"
        modal
        class="w-[30rem]" style="width:600px"
    >
      <div class="flex flex-col gap-3">
        <span class="p-float-label">
          <InputText style="width:500px" v-model="selectedProduct!.name"/>
          <label style="font-size:15px">Tên sản phẩm</label>
        </span>
        <br>
        <span class="p-float-label">
          <Dropdown
              v-model="selectedProduct!.category"
              :options="categoryList"
              optionLabel="name"
              placeholder="Chọn danh mục"
              style="width:500px"
          />
          <label style="font-size:15px">Danh mục</label>
        </span>
        <br>
        <span class="p-float-label">
          <Dropdown
              v-model="selectedProduct!.supplier"
              :options="supplierList"
              optionLabel="name"
              placeholder="Chọn nhà cung cấp"
              style="width:500px"
          />
          <label style="font-size:15px">Nhà cung cấp</label>
        </span>
        <br>

        <!-- ========== IMAGE UPLOAD SECTION ========== -->
        <div class="image-upload-section">
          <label class="upload-label">Ảnh sản phẩm</label>

          <!-- Uploaded/Existing images -->
          <div class="image-grid" v-if="uploadedImageUrls.length > 0 || previewImages.length > 0">
            <!-- Already uploaded images -->
            <div v-for="(url, index) in uploadedImageUrls" :key="'uploaded-' + index" class="image-preview-item">
              <img :src="Helper.GetImageUrl(url)" alt="Uploaded" />
              <button class="remove-btn" @click="removeUploadedImage(index)" title="Xóa ảnh">
                <i class="pi pi-times"></i>
              </button>
              <span class="image-badge">Đã lưu</span>
            </div>
            <!-- New preview images -->
            <div v-for="(src, index) in previewImages" :key="'preview-' + index" class="image-preview-item">
              <img :src="src" alt="Preview" />
              <button class="remove-btn" @click="removeImage(index)" title="Xóa ảnh">
                <i class="pi pi-times"></i>
              </button>
              <span class="image-badge new">Mới</span>
            </div>
          </div>

          <!-- Upload button area -->
          <div class="upload-area" @click="triggerFileInput">
            <input
              ref="fileInputRef"
              type="file"
              accept="image/*"
              multiple
              hidden
              @change="onFileSelect"
            />
            <div class="upload-placeholder">
              <i class="pi pi-cloud-upload" style="font-size: 2rem; color: #007bff;"></i>
              <p>Nhấn để chọn ảnh hoặc kéo thả ảnh vào đây</p>
              <span class="upload-hint">Hỗ trợ: JPG, PNG, WEBP — Tối đa 5MB/ảnh</span>
            </div>
          </div>
        </div>

        <br>
        <span class="p-float-label">
          <InputNumber style="width:400px" v-model="selectedProduct!.price" mode="currency" currency="VND"
                       locale="vi-VN"/>
          <label style="font-size:15px">Giá</label>
        </span>
        <br>
        <span class="p-float-label">
          <InputNumber style="width:400px" v-model="selectedProduct!.amount"/>
          <label style="font-size:15px">Số lượng</label>
        </span>
        <br>
        <span class="p-float-label">
          <Textarea style="width:500px" v-model="selectedProduct!.description" rows="4"/>
          <label style="font-size:15px">Mô tả</label>
        </span>
      </div>

      <template #footer>
        <Button label="Hủy" icon="pi pi-times" class="p-button-text" @click="visibleDialog = false" :disabled="isUploading"/>
        <Button :label="isUploading ? 'Đang tải ảnh...' : 'Lưu'" :icon="isUploading ? 'pi pi-spin pi-spinner' : 'pi pi-check'" @click="saveProduct" :disabled="isUploading"/>
      </template>
    </Dialog>

    <Toast/>

  </div>

</template>
<style scoped>
.p-datatable {
  font-size: 14px;
}

/* ========== Image Upload Styles ========== */
.image-upload-section {
  width: 100%;
}

.upload-label {
  display: block;
  font-size: 14px;
  font-weight: 600;
  color: #555;
  margin-bottom: 8px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.image-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 12px;
}

.image-preview-item {
  position: relative;
  width: 100px;
  height: 100px;
  border-radius: 10px;
  overflow: hidden;
  border: 2px solid #e0e0e0;
  transition: all 0.2s ease;
  box-shadow: 0 2px 6px rgba(0,0,0,0.08);
}

.image-preview-item:hover {
  border-color: #007bff;
  box-shadow: 0 4px 12px rgba(0,123,255,0.2);
}

.image-preview-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.remove-btn {
  position: absolute;
  top: 4px;
  right: 4px;
  width: 22px;
  height: 22px;
  border-radius: 50%;
  background: rgba(220, 53, 69, 0.9);
  color: white;
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 10px;
  opacity: 0;
  transition: opacity 0.2s;
}

.image-preview-item:hover .remove-btn {
  opacity: 1;
}

.image-badge {
  position: absolute;
  bottom: 4px;
  left: 4px;
  font-size: 9px;
  padding: 2px 6px;
  border-radius: 4px;
  background: rgba(40, 167, 69, 0.85);
  color: white;
  font-weight: 600;
  text-transform: uppercase;
}

.image-badge.new {
  background: rgba(0, 123, 255, 0.85);
}

.upload-area {
  border: 2px dashed #ccc;
  border-radius: 12px;
  padding: 24px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
  background: #fafbfc;
}

.upload-area:hover {
  border-color: #007bff;
  background: #f0f6ff;
}

.upload-placeholder p {
  margin: 8px 0 4px;
  font-size: 14px;
  color: #555;
  font-weight: 500;
}

.upload-hint {
  font-size: 12px;
  color: #999;
}
</style>