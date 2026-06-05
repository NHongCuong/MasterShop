<script setup lang="ts">
import { useRoute, useRouter } from 'vue-router';
import { Product, MyImage, ShopCart } from '../../interfaces/app';
import { computed, onMounted, reactive, ref } from 'vue';
import axios from 'axios';
import Helper from '../../helper/helper';
import Galleria from 'primevue/Galleria';
import Image from 'primevue/image';
import {useToast} from "primevue/usetoast";
import {MyApp, state} from "../../app/MyApp.ts";
import Toast from "primevue/toast";
//import SelectButton from "primevue/selectbutton";   // ✅ dùng để chọn màu / kích thước
import Dropdown from "primevue/dropdown";

const router = useRouter();
const myProduct = ref<Product>();
const route = useRoute();
const idProduct = route.params.id;
const images = ref<MyImage[]>([]);
const checkingAmount = ref(false);
const CART_API = 'http://localhost:8081/cart';
const USER_API = 'http://localhost:8081/user';
const toast = useToast()
const visibleCount = computed(() => (images.value.length > 5 ? 5 : images.value.length));

const shopcartList = ref<ShopCart[]>([]);

// ✅ Các danh sách thuộc tính sản phẩm
const colorList = ref<any[]>([]);
const materialList = ref<any[]>([]);
const dimensionList = ref<any[]>([]);

onMounted(() => {
  MyApp.getInstance().authenticate();
  loadProduct();
  loadColors();
  loadMaterials();
  loadDimensions();
});

interface FormState {
  ID_Product: number,
  ID_Color: number | null,
  ID_Material: number | null,
  ID_Dimensions: number | null,
  Amount: number
}

const initForm = {
  ID_Product: -1,
  Amount: 1,
  ID_Color: null,
  ID_Material: null,
  ID_Dimensions: null
};
let form = reactive<FormState>(JSON.parse(JSON.stringify(initForm)));

const responsiveOptions = ref([
  { breakpoint: '991px', numVisible: 4 },
  { breakpoint: '767px', numVisible: 3 },
  { breakpoint: '575px', numVisible: 1 }
]);

// ✅ Load thông tin sản phẩm
function loadProduct() {
  axios.get(`http://localhost:8081/product/${idProduct}`).then(res => {
    const prod = res.data;
    myProduct.value = prod;
    const imgList: MyImage[] = [];
    
    if (prod.avatar) {
      imgList.push({
        itemImageSrc: prod.avatar,
        thumbnailImageSrc: prod.avatar,
        alt: "Avatar Image"
      });
    }
    
    if (prod.productImages && Array.isArray(prod.productImages)) {
      prod.productImages.forEach((img: any) => {
        if (img.imageUrl && img.imageUrl !== prod.avatar) {
          imgList.push({
            itemImageSrc: img.imageUrl,
            thumbnailImageSrc: img.imageUrl,
            alt: "Product Image"
          });
        }
      });
    }
    images.value = imgList;
  });
}

// ✅ Load danh sách màu theo sản phẩm
async function loadColors() {
  try {
    const res = await axios.get(`http://localhost:8081/detailcolor/all/${idProduct}`);
    colorList.value = res.data.map((c: any) => ({
      label: c.nameColor || c.name || `Color ${c.idColor}`,
      value: c.idColor
    }));
  } catch (err) {
    console.error("❌ Lỗi tải màu:", err);
  }
}

// ✅ Load danh sách chất liệu
async function loadMaterials() {
  try {
    const res = await axios.get(`http://localhost:8081/detailmaterial/all/${idProduct}`);
    materialList.value = res.data.map((m: any) => ({
      label: m.nameMaterial || m.name || `Material ${m.idMaterial}`,
      value: m.idMaterial
    }));
  } catch (err) {
    console.error("❌ Lỗi tải chất liệu:", err);
  }
}

// ✅ Load danh sách kích thước
async function loadDimensions() {
  try {
    const res = await axios.get(`http://localhost:8081/dimensions/all/${idProduct}`);
    dimensionList.value = res.data.map((d: any) => ({
      label: d.nameD || `Size ${d.id || d.Id}`,
      value: d.id || d.Id
    }));
  } catch (err) {
    console.error("❌ Lỗi tải kích thước:", err);
  }
}

// ✅ Load tất cả giỏ hàng
const loadShopCart = async () => {
  try {
    const res = await axios.get(`${CART_API}/all`);
    shopcartList.value = res.data;
  } catch (err) {
    toast.add({ severity: 'error', summary: 'Lỗi', detail: 'Không thể tải shop cart', life: 2000 });
  }
};

// ✅ Tăng giảm số lượng
const changeAmount = (event: Event) => {
  let button = event.target as HTMLElement;
  if (button.className.includes('btn-minuse'))
    form.Amount = form.Amount > 1 ? form.Amount - 1 : form.Amount;
  else
    form.Amount++;
};

// ✅ Thêm vào giỏ hàng
async function AdddCart(isBuyNow = false) {
  if (!state.isAuthenticated) {
     toast.add({ severity: 'warn', summary: 'Yêu cầu đăng nhập', detail: 'Vui lòng đăng nhập để tiếp tục', life: 3000 });
     return;
  }
  
  // Đã bỏ verify màu sắc, chất liệu, kích thước theo yêu cầu khách hàng

  try {
    const payload = {
      userId: state.user?.id,
      productId: idProduct,
      amount: form.Amount,
      colorId: form.ID_Color,
      materialId: form.ID_Material,
      dimensionId: form.ID_Dimensions
    };
    
    const res = await axios.post(`http://localhost:8081/cart/add-item`, payload);
    
    if (res.status === 200 || res.status === 201) {
      // Cập nhật lại số lượng giỏ hàng trên header
      await MyApp.getInstance().updateCartCount();

      if (isBuyNow) {
        // Luồng mua ngay: Chuyển hướng tới trang Order
        router.push('/order');
      } else {
        toast.add({
          severity: "success",
          summary: "Thành công",
          detail: `Đã thêm ${myProduct.value?.name} vào giỏ hàng`,
          life: 3000,
        });
      }
    }
  } catch (err: any) {
    toast.add({
      severity: "error",
      summary: "Lỗi",
      detail: err.response?.data?.message || "Có lỗi xảy ra",
      life: 3000,
    });
  }
}
async function buyNow() {
    await AdddCart(true);
}
</script>

<template>
  <div class="p-5 mt-5 bg-light" style="border-radius: 25px;">
    <Toast />
    <div class="row w-100">
      <div class="col-lg-5">
        <div class="p-3">
          <Galleria v-if="myProduct" :value="images" :responsiveOptions="responsiveOptions" :numVisible="visibleCount"
                    containerStyle="max-width: 100%" class="custom-galleria">
            <template #item="slotProps">
              <div class="main-image-container">
                <Image :src="Helper.GetImageUrl(slotProps.item.itemImageSrc)" :alt="slotProps.item.alt"
                       class="main-image" preview/>
              </div>
            </template>
            <template #thumbnail="slotProps">
              <div class="thumbnail-wrapper">
                <img :src="Helper.GetImageUrl(slotProps.item.thumbnailImageSrc)"
                      :alt="slotProps.item.alt" class="thumbnail-img"/>
              </div>
            </template>
          </Galleria>
        </div>
      </div>
      <div class="col-lg-7">
        <div v-if="myProduct" class="p-3">
          <h2 class="fw-bold mb-3">{{ myProduct.name }}</h2>
          <div class="d-flex align-items-center mb-4">
            <h3 class="text-danger fw-bold m-0 me-3">{{ Helper.ToMoney(myProduct.price) }}</h3>
            <span class="badge bg-success">Còn hàng: {{ myProduct.amount }}</span>
          </div>
          
          <p class="text-muted mb-4">{{ myProduct.description }}</p>

          <div class="row g-3 mb-4">
            <div class="col-md-4">
              <label class="form-label fw-bold">Màu sắc</label>
              <Dropdown v-model="form.ID_Color" :options="colorList" optionLabel="label" optionValue="value" placeholder="Chọn màu" class="w-100" />
            </div>
            <div class="col-md-4">
              <label class="form-label fw-bold">Chất liệu</label>
              <Dropdown v-model="form.ID_Material" :options="materialList" optionLabel="label" optionValue="value" placeholder="Chọn chất liệu" class="w-100" />
            </div>
            <div class="col-md-4">
              <label class="form-label fw-bold">Kích thước</label>
              <Dropdown v-model="form.ID_Dimensions" :options="dimensionList" optionLabel="label" optionValue="value" placeholder="Chọn size" class="w-100" />
            </div>
          </div>

          <div class="d-flex align-items-center gap-3 mb-4">
            <div class="input-group" style="width: 140px;">
              <button class="btn btn-outline-secondary btn-minuse" type="button" @click="changeAmount">-</button>
              <input type="text" class="form-control text-center" v-model="form.Amount">
              <button class="btn btn-outline-secondary btn-pluse" type="button" @click="changeAmount">+</button>
            </div>
            <button class="btn btn-primary px-4 py-2 fw-bold" @click="() => AdddCart(false)">
              <i class="fas fa-cart-plus me-2"></i>Thêm vào giỏ hàng
            </button>
            <button class="btn btn-danger px-4 py-2 fw-bold" @click="buyNow">
              Mua ngay
            </button>
          </div>

          <div class="border-top pt-4">
            <div class="d-flex align-items-center gap-4 text-muted small">
              <span><i class="fas fa-truck me-1"></i> Giao hàng toàn quốc</span>
              <span><i class="fas fa-undo me-1"></i> Đổi trả trong 7 ngày</span>
              <span><i class="fas fa-shield-alt me-1"></i> Bảo hành chính hãng</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
<style scoped>
/* Galleria Overrides */
:deep(.custom-galleria) {
  border: none;
  width: 100%;
}

:deep(.p-galleria-item) {
  background: #fff;
  border: 1px solid #ececec;
  border-radius: 15px;
  overflow: hidden;
  display: flex;
  justify-content: center;
  align-items: center;
  height: 500px; /* Tăng chiều cao khung hình */
}

.main-image-container {
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
}

:deep(.main-image img) {
  max-width: 100%;
  max-height: 480px;
  width: auto !important;
  height: auto !important;
  object-fit: contain !important;
  display: block;
}

:deep(.p-galleria-thumbnail-container) {
  background: transparent !important;
  padding: 1rem 0;
}

:deep(.p-galleria-thumbnail-items-container) {
  height: auto !important;
}

.thumbnail-wrapper {
  overflow: hidden;
  border-radius: 8px;
  border: 2px solid transparent;
  transition: all 0.3s ease;
  width: 80px;
  height: 80px;
  margin: 0 5px;
}

.p-galleria-thumbnail-item-current .thumbnail-wrapper {
  border-color: #ffd700;
}

.thumbnail-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* Chrome fix for Galleria navigators */
:deep(.p-galleria-thumbnail-prev), 
:deep(.p-galleria-thumbnail-next) {
  background: transparent;
  color: #333;
}
</style>