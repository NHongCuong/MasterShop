<script setup lang="ts">
import { useRoute, useRouter } from 'vue-router';
import { Product, MyImage, ShopCart } from '../../interfaces/app';
import { computed, onMounted, reactive, ref } from 'vue';
import axios from 'axios';
import Helper from '../../helper/helper';
import Galleria from 'primevue/galleria';
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
  loadReviews();
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

const reviews = ref<any[]>([]);
const myRating = ref(0);
const hoverRating = ref(0);
const myComment = ref('');

const loadReviews = async () => {
    try {
        const res = await axios.get(`http://localhost:8081/api/reviews/product/${idProduct}`);
        reviews.value = res.data;
    } catch(err) {}
};

const submitReview = async () => {
    if (!state.isAuthenticated) {
        toast.add({ severity: 'warn', summary: 'Yêu cầu đăng nhập', detail: 'Vui lòng đăng nhập để đánh giá', life: 3000 });
        return;
    }
    if (myRating.value === 0) {
        toast.add({ severity: 'warn', summary: 'Chưa đánh giá', detail: 'Vui lòng chọn số sao để đánh giá', life: 3000 });
        return;
    }
    try {
        await axios.post('http://localhost:8081/api/reviews', {
            userId: state.user?.id,
            productId: Number(idProduct),
            rating: myRating.value,
            comment: myComment.value
        });
        toast.add({ severity: 'success', summary: 'Thành công', detail: 'Cảm ơn bạn đã đánh giá!', life: 3000 });
        myComment.value = '';
        myRating.value = 0;
        loadReviews();
    } catch(err) {
        toast.add({ severity: 'error', summary: 'Lỗi', detail: 'Không thể gửi đánh giá', life: 3000 });
    }
};

const averageRating = computed(() => {
    if (reviews.value.length === 0) return 5;
    const sum = reviews.value.reduce((acc, r) => acc + r.rating, 0);
    return Number((sum / reviews.value.length).toFixed(1));
});

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
      if (isBuyNow) {
        // ✅ Cập nhật sessionStorage NGAY LẬP TỨC để Order.vue lấy được dữ liệu mới nhất
        const prod = myProduct.value;
        const qty = form.Amount;
        const total = (prod?.price || 0) * qty;
        
        const buyNowData = {
          cartItems: [{
            idCartDetail: 0,
            idProduct: Number(idProduct),
            productName: prod?.name,
            productPrice: prod?.price,
            productAvatar: prod?.avatar,
            amountCD: qty,
            colorId: form.ID_Color,
            materialId: form.ID_Material,
            dimensionId: form.ID_Dimensions,
            // Các trường đồng bộ cho giao diện Order.vue
            idColor: form.ID_Color,
            idMaterial: form.ID_Material,
            idDimension: form.ID_Dimensions,
            voucherCode: prod?.voucher?.maVoucher || null
          }],
          voucher: null,
          totalPrice: total,
          discountAmount: 0,
          finalPrice: total
        };
        
        sessionStorage.setItem('checkoutData', JSON.stringify(buyNowData));
        router.push('/order');
      } else {
        toast.add({
          severity: "success",
          summary: "Thành công",
          detail: `Đã thêm ${myProduct.value?.name} vào giỏ hàng`,
          life: 3000,
        });
      }
      
      // Cập nhật lại số lượng giỏ hàng trên header sau khi đã chuyển trang hoặc show toast
      MyApp.getInstance().updateCartCount();
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
    if (!state.isAuthenticated) {
        toast.add({ severity: 'warn', summary: 'Yêu cầu đăng nhập', detail: 'Vui lòng đăng nhập để tiếp tục', life: 3000 });
        return;
    }
    if (!myProduct.value) return;

    const prod = myProduct.value;
    const qty = form.Amount;
    
    // Tính toán đơn giá sau khi giảm (nếu có)
    const discountedPrice = prod?.discountPercent 
        ? Math.round((prod.price || 0) * (1 - prod.discountPercent / 100)) 
        : (prod?.price || 0);
    const total = discountedPrice * qty;
    
    // ✅ Tạo dữ liệu thanh toán ảo cho trang Order, KHÔNG gọi API thêm vào giỏ hàng DB
    const buyNowData = {
      cartItems: [{
        idCartDetail: 0,
        idProduct: Number(idProduct),
        productName: prod?.name,
        productPrice: prod?.price,
        productAvatar: prod?.avatar,
        productDiscountPercent: prod?.discountPercent,
        amountCD: qty,
        colorId: form.ID_Color,
        materialId: form.ID_Material,
        dimensionId: form.ID_Dimensions,
        idColor: form.ID_Color,
        idMaterial: form.ID_Material,
        idDimension: form.ID_Dimensions,
        voucherCode: prod?.voucher?.maVoucher || null
      }],
      voucher: null,
      totalPrice: total,
      discountAmount: 0,
      finalPrice: total
    };
    
    sessionStorage.setItem('checkoutData', JSON.stringify(buyNowData));
    router.push('/order');
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
            <div class="price-container">
              <div v-if="myProduct.discountPercent" class="text-muted text-decoration-line-through small">
                {{ Helper.ToMoney(myProduct.price) }}
              </div>
              <h3 class="text-danger fw-bold m-0 me-3">
                {{ Helper.ToMoney(myProduct.discountPercent ? Math.round(myProduct.price * (1 - myProduct.discountPercent / 100)) : myProduct.price) }}
              </h3>
            </div>
            <span class="badge bg-success">Còn hàng: {{ myProduct.amount }}</span>
            <span v-if="myProduct.warranty" class="badge bg-info ms-2">
              <i class="fas fa-shield-alt me-1"></i>Bảo hành: {{ myProduct.warranty }}
            </span>
          </div>
          
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
            <div class="d-flex align-items-center gap-4 text-muted small mb-3">
              <span><i class="fas fa-truck me-1"></i> Giao hàng toàn quốc</span>
              <span><i class="fas fa-undo me-1"></i> Đổi trả trong 7 ngày</span>
              <span><i class="fas fa-shield-alt me-1"></i> Bảo hành chính hãng</span>
            </div>
            
            <div v-if="myProduct.description" class="product-description mt-4">
              <div class="bg-warning text-white text-center py-2 fw-bold text-uppercase mb-3" style="border-radius: 4px;">
                Thông tin chi tiết
              </div>
              <p class="text-muted" style="white-space: pre-line; line-height: 1.6;">
                {{ myProduct.description }}
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Review Section -->
    <div class="row w-100 mt-5">
      <div class="col-12">
        <div class="bg-warning text-white text-center py-2 fw-bold text-uppercase mb-4" style="border-radius: 4px;">
          Đánh giá sản phẩm
        </div>
        
        <div class="d-flex align-items-center mb-4">
          <h2 class="text-warning fw-bold m-0 me-2">{{ averageRating }}<span class="fs-5 text-muted">/5</span></h2>
          <div class="text-warning fs-5 me-3">
            <i class="fas fa-star" v-for="n in Math.round(averageRating)" :key="'s-' + n"></i>
            <i class="far fa-star" v-for="n in (5 - Math.round(averageRating))" :key="'e-' + n"></i>
          </div>
          <span class="text-muted">({{ reviews.length }} đánh giá)</span>
        </div>

        <div class="mb-5">
          <h5 class="fw-bold mb-3">Viết đánh giá của bạn</h5>
          <div class="d-flex align-items-center mb-2">
            <span class="me-2 fw-bold">Đánh giá:</span>
            <div class="cursor-pointer" style="font-size: 1.2rem;">
              <i v-for="n in 5" :key="n" 
                 :class="n <= (hoverRating || myRating) ? 'fas fa-star text-warning' : 'far fa-star text-muted'" 
                 @click="myRating = n" 
                 @mouseover="hoverRating = n"
                 @mouseleave="hoverRating = 0"
                 style="cursor: pointer; transition: color 0.2s;"></i>
            </div>
          </div>
          <textarea v-model="myComment" class="form-control mb-3" rows="3" placeholder="Nhập nhận xét của bạn (không bắt buộc)..."></textarea>
          <button class="btn btn-warning text-white fw-bold px-4" @click="submitReview">Gửi đánh giá</button>
        </div>

        <div>
           <div v-for="rev in reviews" :key="rev.id" class="border-bottom py-3">
              <div class="d-flex justify-content-between">
                <strong>{{ rev.userName }}</strong>
                <span class="text-muted small">{{ new Date(rev.createdAt).toLocaleString('vi-VN') }}</span>
              </div>
              <div class="text-warning my-1" style="font-size: 0.9rem;">
                <i class="fas fa-star" v-for="n in rev.rating" :key="'sr-'+n"></i>
              </div>
              <p class="m-0 text-secondary">{{ rev.comment }}</p>
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