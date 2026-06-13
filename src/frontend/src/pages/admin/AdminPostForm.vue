<template>
  <div class="container-fluid py-4 bg-light min-vh-100">
    <div class="d-flex align-items-center mb-4">
      <button class="btn btn-secondary btn-sm mr-3" @click="$router.push('/admin/post')">
        <i class="fas fa-arrow-left"></i> Quay lại
      </button>
      <h3 class="font-weight-bold m-0">{{ isEdit ? 'Cập nhật bài viết' : 'Thêm bài viết mới' }}</h3>
    </div>

    <div class="card shadow-sm border-0 rounded-lg">
      <div class="card-body p-4">
        <form @submit.prevent="submitForm">
          
          <div class="form-group mb-4">
            <label class="font-weight-bold">Tiêu đề bài viết</label>
            <input type="text" class="form-control" v-model="form.title" @input="generateSlug" placeholder="Nhập tiêu đề..." required>
          </div>

          <div class="form-group mb-4">
            <label class="font-weight-bold d-flex align-items-center">
              URL Bài viết <span class="badge badge-success ml-2 py-1">TỰ ĐỘNG</span>
            </label>
            <div class="input-group">
              <div class="input-group-prepend">
                <span class="input-group-text bg-light border-right-0">/huong-dan/</span>
              </div>
              <input type="text" class="form-control border-left-0" v-model="form.slug" placeholder="tieu-de-bai-viet-cua-ban">
              <div class="input-group-append">
                <button class="btn btn-outline-secondary" type="button" @click="generateSlug" title="Làm mới URL">
                  <i class="fas fa-sync-alt"></i>
                </button>
              </div>
            </div>
          </div>

          <div class="row mb-4">
            <div class="col-md-6">
              <label class="font-weight-bold">Danh mục</label>
              <select class="form-control" v-model="form.categoryId">
                <option :value="null">-- Chọn danh mục --</option>
                <option v-for="cat in categories" :key="cat.id" :value="cat.id">{{ cat.name }}</option>
              </select>
            </div>
            <div class="col-md-6">
              <label class="font-weight-bold">Tác giả (Admin)</label>
              <select class="form-control" v-model="form.authorName">
                <option value="">-- Chọn tác giả --</option>
                <option value="Admin">Admin</option>
                <option v-for="user in authors" :key="user.id" :value="user.nameUser">{{ user.nameUser }}</option>
              </select>
            </div>
          </div>

          <div class="form-group mb-4">
            <label class="font-weight-bold">Hình ảnh (URL)</label>
            <input type="text" class="form-control mb-2" v-model="form.image" placeholder="http://...">
            <input type="file" ref="imageUploadInput" @change="onImageSelected" class="d-none" accept="image/*">
            <button type="button" class="btn btn-outline-primary btn-sm border-dashed" @click="imageUploadInput.click()">
              <i class="fas fa-plus"></i> Thêm ảnh
            </button>
          </div>

          <hr class="my-4">

          <h5 class="font-weight-bold mb-3">Nội dung chi tiết</h5>
          
          <div v-for="(section, index) in form.sections" :key="index" class="card border mb-4 section-card">
            <div class="card-header bg-white font-weight-bold text-success d-flex justify-content-between align-items-center">
              <span>Phần {{ index + 1 }}</span>
              <button v-if="form.sections.length > 1" type="button" class="btn btn-sm btn-outline-danger border-0" @click="removeSection(index)">
                <i class="fas fa-times"></i>
              </button>
            </div>
            <div class="card-body">
              <div class="form-group mb-3">
                <input type="text" class="form-control form-control-lg" v-model="section.heading" placeholder="Heading (Mục lục)...">
              </div>
              
              <div class="d-flex align-items-center mb-3">
                <div class="custom-control custom-radio mr-4">
                  <input type="radio" :id="'manual_'+index" :name="'type_'+index" value="manual" v-model="section.type" class="custom-control-input">
                  <label class="custom-control-label" :for="'manual_'+index">Tự nhập</label>
                </div>
                <div class="custom-control custom-radio">
                  <input type="radio" :id="'ai_'+index" :name="'type_'+index" value="ai" v-model="section.type" class="custom-control-input">
                  <label class="custom-control-label" :for="'ai_'+index">Using AI</label>
                </div>
              </div>

              <div class="form-group mb-4">
                <textarea class="form-control" rows="5" v-model="section.content" placeholder="Nội dung chi tiết..."></textarea>
              </div>

              <div class="bg-light border rounded p-3">
                <div class="d-flex justify-content-between align-items-center mb-3">
                  <h6 class="font-weight-bold text-success m-0"><i class="fas fa-table mr-1"></i> Bảng dữ liệu (tùy chọn)</h6>
                  <button type="button" class="btn btn-sm text-danger font-weight-bold" @click="section.showTable = false" v-if="section.showTable">
                    <i class="fas fa-times"></i> Xóa bảng
                  </button>
                </div>
                
                <div v-if="!section.showTable">
                  <button type="button" class="btn btn-success btn-sm font-weight-bold" @click="addTable(section)">
                    <i class="fas fa-plus mr-1"></i> Thêm bảng
                  </button>
                </div>
                <div v-else>
                  <div class="d-flex mb-3 align-items-center flex-wrap gap-2">
                    <div class="d-flex align-items-center bg-white border rounded px-2 py-1 mr-3">
                      <span class="mr-2 small font-weight-bold">Số hàng</span>
                      <button type="button" class="btn btn-sm btn-light border p-1" @click="decreaseRow(section)"><i class="fas fa-minus" style="font-size:10px;"></i></button>
                      <span class="mx-2 font-weight-bold">{{ section.tableRows }}</span>
                      <button type="button" class="btn btn-sm btn-light border p-1" @click="increaseRow(section)"><i class="fas fa-plus" style="font-size:10px;"></i></button>
                    </div>
                    
                    <div class="d-flex align-items-center bg-white border rounded px-2 py-1">
                      <span class="mr-2 small font-weight-bold">Số cột</span>
                      <button type="button" class="btn btn-sm btn-light border p-1" @click="decreaseCol(section)"><i class="fas fa-minus" style="font-size:10px;"></i></button>
                      <span class="mx-2 font-weight-bold">{{ section.tableCols }}</span>
                      <button type="button" class="btn btn-sm btn-light border p-1" @click="increaseCol(section)"><i class="fas fa-plus" style="font-size:10px;"></i></button>
                    </div>
                  </div>

                  <div class="table-responsive">
                    <table class="table table-bordered bg-white table-sm m-0">
                      <tbody>
                        <tr v-for="r in section.tableRows" :key="'r_'+r">
                          <td v-for="c in section.tableCols" :key="'c_'+c" :class="{'bg-success-light font-weight-bold': r === 1}">
                            <input type="text" class="form-control form-control-sm border-0 bg-transparent" v-model="section.tableData[r-1][c-1]" :placeholder="r===1 ? `Cột ${c}` : 'Nội dung...'">
                          </td>
                        </tr>
                      </tbody>
                    </table>
                  </div>
                  <small class="text-muted mt-2 d-block">* Hàng đầu tiên sẽ là tiêu đề (header) của bảng.</small>
                </div>
              </div>

            </div>
          </div>

          <button type="button" class="btn btn-outline-success btn-block border-dashed-success py-3 mb-4 font-weight-bold" @click="addSection">
            <i class="fas fa-plus mr-1"></i> Thêm đoạn nội dung (Heading & Content)
          </button>

          <button type="submit" class="btn btn-success btn-lg btn-block font-weight-bold shadow-sm" :disabled="saving">
            <span v-if="saving" class="spinner-border spinner-border-sm mr-2"></span>
            <i v-else class="fas fa-save mr-2"></i> Lưu bài viết
          </button>

        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import axios from 'axios';
import { useRoute, useRouter } from 'vue-router';
import { state, MyApp } from '../../app/MyApp';

const route = useRoute();
const router = useRouter();

const isEdit = computed(() => !!route.params.id);
const saving = ref(false);

const categories = ref([]);
const authors = ref([]);
const imageUploadInput = ref(null);

const form = ref({
  id: null,
  title: '',
  slug: '',
  image: '',
  shortDescription: '',
  categoryId: null,
  authorName: '',
  sections: [
    {
      heading: '',
      type: 'manual',
      content: '',
      showTable: false,
      tableRows: 2,
      tableCols: 2,
      tableData: [['', ''], ['', '']]
    }
  ]
});

const loadInitialData = async () => {
  try {
    await MyApp.getInstance().authenticate();
    
    try {
      const resCat = await axios.get('http://localhost:8081/category/list');
      categories.value = resCat.data;
    } catch(e) { console.warn("Lỗi load danh mục", e); }

    const adminName = state.user?.nameUser || state.user?.email || 'Admin';
    authors.value = [{ id: 1, nameUser: adminName }];
    
    if (!isEdit.value) {
        form.value.authorName = adminName;
    }

    if (isEdit.value) {
      const res = await axios.get(`http://localhost:8081/post/${route.params.id}`);
      const data = res.data;
      form.value.id = data.id;
      form.value.title = data.title || '';
      form.value.slug = data.slug || '';
      form.value.image = data.image || '';
      form.value.shortDescription = data.shortDescription || '';
      form.value.categoryId = data.category ? data.category.id : null;
      form.value.authorName = data.authorName || '';
      
      try {
        if (data.content && data.content.startsWith('[')) {
          form.value.sections = JSON.parse(data.content);
        } else {
            // fallback plain text to structured mode
            form.value.sections[0].content = data.content;
        }
      } catch(e) {
          form.value.sections[0].content = data.content;
      }
    }
  } catch (err) {
    console.error(err);
  }
};

const generateSlug = () => {
    if (!form.value.title) return;
    let str = form.value.title.toLowerCase();
    str = str.replace(/(à|á|ạ|ả|ã|â|ầ|ấ|ậ|ẩ|ẫ|ă|ằ|ắ|ặ|ẳ|ẵ)/g, 'a');
    str = str.replace(/(è|é|ẹ|ẻ|ẽ|ê|ề|ế|ệ|ể|ễ)/g, 'e');
    str = str.replace(/(ì|í|ị|ỉ|ĩ)/g, 'i');
    str = str.replace(/(ò|ó|ọ|ỏ|õ|ô|ồ|ố|ộ|ổ|ỗ|ơ|ờ|ớ|ợ|ở|ỡ)/g, 'o');
    str = str.replace(/(ù|ú|ụ|ủ|ũ|ư|ừ|ứ|ự|ử|ữ)/g, 'u');
    str = str.replace(/(ỳ|ý|ỵ|ỷ|ỹ)/g, 'y');
    str = str.replace(/(đ)/g, 'd');
    str = str.replace(/([^0-9a-z-\s])/g, '');
    str = str.replace(/(\s+)/g, '-');
    str = str.replace(/^-+/g, '');
    str = str.replace(/-+$/g, '');
    form.value.slug = str;
};

const onImageSelected = async (e) => {
  const file = e.target.files[0];
  if (!file) return;
  const formData = new FormData();
  formData.append('file', file);
  try {
    const res = await axios.post('http://localhost:8081/storage/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    });
    form.value.image = res.data;
  } catch (err) {
    alert("Lỗi upload ảnh: " + err.message);
  }
};

const addSection = () => {
  form.value.sections.push({
    heading: '',
    type: 'manual',
    content: '',
    showTable: false,
    tableRows: 2,
    tableCols: 2,
    tableData: [['', ''], ['', '']]
  });
};

const removeSection = (index) => {
  form.value.sections.splice(index, 1);
};

const addTable = (section) => {
  section.showTable = true;
};

const increaseRow = (section) => {
  section.tableRows++;
  section.tableData.push(new Array(section.tableCols).fill(''));
};

const decreaseRow = (section) => {
  if (section.tableRows > 1) {
    section.tableRows--;
    section.tableData.pop();
  }
};

const increaseCol = (section) => {
  section.tableCols++;
  section.tableData.forEach(row => row.push(''));
};

const decreaseCol = (section) => {
  if (section.tableCols > 1) {
    section.tableCols--;
    section.tableData.forEach(row => row.pop());
  }
};

const submitForm = async () => {
  saving.value = true;
  try {
    // Generate short description from sect 0 if empty
    if (!form.value.shortDescription && form.value.sections.length > 0) {
        let sc = form.value.sections[0].content || '';
        form.value.shortDescription = sc.length > 150 ? sc.substring(0, 150) + '...' : sc;
    }

    const payload = {
      title: form.value.title,
      slug: form.value.slug,
      image: form.value.image,
      shortDescription: form.value.shortDescription,
      content: JSON.stringify(form.value.sections), // save structured json
      authorName: form.value.authorName,
      category: form.value.categoryId ? { id: form.value.categoryId } : null
    };

    if (isEdit.value) {
      await axios.put(`http://localhost:8081/post/admin/update/${form.value.id}`, payload);
      alert("Cập nhật thành công!");
    } else {
      await axios.post(`http://localhost:8081/post/admin/create`, payload);
      alert("Đăng bài thành công!");
    }
    router.push('/admin/post');
  } catch (error) {
    alert("Lỗi lưu bài viết: " + error.message);
  } finally {
    saving.value = false;
  }
};

onMounted(() => {
  loadInitialData();
});
</script>

<style scoped>
.border-dashed {
  border: 1px dashed #0d6efd;
}
.border-dashed-success {
  border: 2px dashed #198754;
  color: #198754;
  background-color: #f8fff9;
}
.border-dashed-success:hover {
  background-color: #e8f5e9;
  color: #157347;
}
.bg-success-light {
    background-color: #e8f5e9 !important;
}
.section-card {
    border-color: #e2e8f0;
    box-shadow: 0 2px 4px rgba(0,0,0,0.02);
}
</style>
