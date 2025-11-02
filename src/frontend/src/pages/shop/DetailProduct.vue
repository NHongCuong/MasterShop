<script setup lang="ts">
import {useRoute} from 'vue-router';
import {User, Product, MyImage, ShopCart} from '../../interfaces/app';
import {computed, onMounted, reactive, ref} from 'vue';
import axios from 'axios';
import Helper from '../../helper/helper';
import Galleria from 'primevue/Galleria';
import Image from 'primevue/image';
import {useToast} from "primevue/usetoast";
import {MyApp} from "../../app/MyApp.ts";
import Toast from "primevue/toast";
import Dropdown from "primevue/dropdown";
import { useRouter } from "vue-router";

const router = useRouter();
const myProduct = ref<Product>();
const route = useRoute();
const idProduct = route.params.id;
const images = ref<MyImage[]>([]);
const checkingAmount = ref(false);
const CART_API = 'http://localhost:8081/cart';
const USER_API = 'http://localhost:8081/user';
const toast = useToast()
const visibleCount = computed(() => (images.value.length > 3 ? 3 : images.value.length));
const isAuthenticated = ref(false);
const selectedUser = ref<any>(null);

const userList = ref<User[]>([]);
const shopcartList = ref<ShopCart[]>([]);

// ✅ Danh sách thuộc tính
const colorList = ref<any[]>([]);
const materialList = ref<any[]>([]);
const dimensionList = ref<any[]>([]);

axios.get(USER_API).then(res => (userList.value = res.data));

onMounted(() => {
  MyApp.getIntance().authenticate(isAuthenticated, selectedUser);
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

// ✅ Load Product
function loadProduct() {
  axios.get(`http://localhost:8081/product/${idProduct}`).then(res => {
    myProduct.value = res.data;
    if (myProduct.value?.avatar) {
      images.value.push({
        itemImageSrc: myProduct.value.avatar,
        thumbnailImageSrc: myProduct.value.avatar,
        alt: "Avatar Image"
      });
    }
  });
}

// ✅ Load Colors
async function loadColors() {
  try {
    const res = await axios.get(`http://localhost:8081/detailcolor/all/${idProduct}`);
    colorList.value = res.data.map((c: any) => ({
      label: c.nameColor || c.name || `Color ${c.idColor}`,
      value: Number(c.idColor)
    }));
  } catch (err) {
    console.error("❌ Lỗi tải màu:", err);
  }
}

// ✅ Load Materials
async function loadMaterials() {
  try {
    const res = await axios.get(`http://localhost:8081/detailmaterial/all/${idProduct}`);
    materialList.value = res.data.map((m: any) => ({
      label: m.nameMaterial || `Material ${m.idMaterial}`,
      value: Number(m.idMaterial)
    }));

    // 🔹 Nếu API chưa có "Không xác định", đảm bảo thêm vào
    if (!materialList.value.some(m => m.value === 0)) {
      materialList.value.unshift({ label: 'Không xác định', value: 0 });
    }
  } catch (err) {
    console.error("❌ Lỗi tải chất liệu:", err);
  }
}

// ✅ Load Dimensions
async function loadDimensions() {
  try {
    const res = await axios.get(`http://localhost:8081/dimensions/all/${idProduct}`);
    dimensionList.value = res.data.map((d: any) => ({
      label: d.nameD || d.name || `Size ${d.id}`,
      value: Number(d.id)
    }));
  } catch (err) {
    console.error("❌ Lỗi tải kích thước:", err);
  }
}

// ✅ Load ShopCart
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
async function AddCart() {
  try {
    const newCart = {
      userSC: { id: selectedUser.value.id },
      cartStatus: { id: 3 },
    };
    const res = await axios.post(`${CART_API}/add`, newCart);
    if (res.status === 200 || res.status === 201) {
      toast.add({
        severity: "success",
        summary: "Thành công",
        detail: "Đã thêm sản phẩm vào giỏ hàng",
        life: 2000,
      });
      await loadShopCart();
    } else {
      toast.add({
        severity: "warn",
        summary: "Cảnh báo",
        detail: "Phản hồi không mong đợi từ server",
        life: 2500,
      });
    }
  } catch (err: any) {
    toast.add({
      severity: "error",
      summary: "Lỗi",
      detail: err.response?.data?.message || "Không thể thêm vào giỏ hàng",
      life: 3000,
    });
  }
}

// ✅ Mua ngay
function BuyNow() {
  // ⚠ Không check 0 vì 0 là “Không xác định”
  // if (form.ID_Color == null || form.ID_Material == null || form.ID_Dimensions == null) {
  //   toast.add({
  //     severity: "warn",
  //     summary: "Thiếu thông tin",
  //     detail: "Vui lòng chọn đủ Màu, Chất liệu và Kích thước trước khi mua!",
  //     life: 2500,
  //   });
  //   return;
  // }

  router.push({
    name: "cart",
    query: {
      id: myProduct.value?.id,
      color: form.ID_Color,
      material: form.ID_Material,
      dimension: form.ID_Dimensions,
      amount: form.Amount,
    },
  });
}

// ✅ Đóng form, quay lại
function Close() {
  router.push({ name: "shophome" });
}

// ✅ Mount
onMounted(() => {
  loadProduct();
  loadColors();
  loadMaterials();
  loadDimensions();
});
</script>

<template>
  <div class="p-5 mt-5 bg-light" style="border-radius: 25px;">
    <div class="row w-100">
      <div class="col-lg-5">
        <div class="p-3">
          <Galleria v-if="myProduct" :value="images" :responsiveOptions="responsiveOptions" :numVisible="visibleCount"
                    containerStyle="max-width: 640px">
            <template #item="slotProps">
              <Image :src="'http://localhost:8081' + slotProps.item.itemImageSrc" height="400"
                     :alt="slotProps.item.alt" preview/>
            </template>
            <template #thumbnail="slotProps">
              <img :src="'http://localhost:8081' + slotProps.item.thumbnailImageSrc" width="100" height="100"
                   :alt="slotProps.item.alt"/>
            </template>
          </Galleria>
        </div>
      </div>

      <div class="col-lg-7 w3-animate-bottom">
        <div class="pl-3 ml-3">
          <h1 v-if="myProduct">{{ myProduct?.name }}</h1>
          <p v-if="myProduct" class="text-danger h3">
            <strong>{{ Helper.formatMoney(myProduct!.price) }}</strong>
          </p>

          <hr>

          <div v-if="myProduct">
            <div class="mt-3">
              <span><b>Color:</b></span>
              <Dropdown v-model="form.ID_Color" :options="colorList" optionLabel="label" optionValue="value"
                        placeholder="Chọn màu" class="mt-2 w-50"/>
            </div>

            <div class="mt-4">
              <span><b>Material:</b></span>
              <Dropdown v-model="form.ID_Material" :options="materialList" optionLabel="label" optionValue="value"
                        placeholder="Chọn chất liệu" class="mt-2 w-50"/>
            </div>

            <div class="mt-4">
              <span><b>Dimensions:</b></span>
              <Dropdown v-model="form.ID_Dimensions" :options="dimensionList" optionLabel="label" optionValue="value"
                        placeholder="Chọn kích thước" class="mt-2 w-50"/>
            </div>

            <div class="row g-3 align-items-center mt-4">
              <div class="col-auto">
                <label class="col-form-label">Amount</label>
              </div>
              <div class="col-auto">
                <div class="input-group" style="width:50%;">
                  <span class="input-group-btn">
                    <button class="btn btn-white btn-minuse" @click="changeAmount" type="button">-</button>
                  </span>
                  <input v-model="form.Amount" type="text" class="form-control text-center" maxlength="3">
                  <span class="input-group-btn">
                    <button class="btn btn-red btn-pluss" @click="changeAmount" type="button">+</button>
                  </span>
                </div>
              </div>
            </div>

            <div class="row mt-3">
              <button class="btn btn-primary" @click="AddCart"><i class="fas fa-shopping-cart mr-2"></i>Add to cart</button>
              <button @click="BuyNow" class="btn btn-success ml-3" :disabled="myProduct.amount <= 0">Buy now</button>
              <button @click="Close" class="btn btn-secondary ml-3">Close</button>
            </div>

            <div class="row mt-3">
              <b class="mr-2">Stock:</b>
              <Badge v-if="checkingAmount" value="Checking..."></Badge>
              <Badge v-else-if="myProduct.amount == -1" value="Chưa xác định"></Badge>
              <Badge v-else :value="myProduct.amount"></Badge>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
  <Toast/>
</template>

<style scoped>
:deep(.p-galleria-thumbnail-wrapper) {
  background-color: #dc3545 !important;

}

:deep(.p-galleria-thumbnail-container) {
  background-color: grey !important;

}

:deep(.p-galleria-thumbnail-item img) {
  border-radius: 10px;
  background-color: white;
  border: 1px solid #ccc;
  width: 100px;
  height: auto;
  object-fit: contain;
}

:deep(.p-galleria) {
  max-width: 640px;
  margin: 0 auto;
}

:deep(.p-galleria-thumbnail-container) {
  justify-content: center;
}

/* Nút điều hướng trái phải */
:deep(.p-galleria-thumbnail-prev),
:deep(.p-galleria-thumbnail-next) {
  color: white !important;
  background-color: rgba(0, 0, 0, 0.3);
  border-radius: 50%;
  width: 40px;
  height: 40px;
}

:deep(.p-galleria-thumbnail-prev:hover),
:deep(.p-galleria-thumbnail-next:hover) {
  background-color: rgba(255, 255, 255, 0.2);
}
</style>