<script setup lang="ts">
import { useRoute } from 'vue-router';
import { Product, MyImage } from '../../interfaces/app';
import { computed, onMounted, reactive, ref } from 'vue';
import axios from 'axios';
import Helper from '../../helper/helper';
import Galleria from 'primevue/Galleria';
import Image from 'primevue/image';


const myProduct = ref<Product>();
const route = useRoute();
const idProduct = route.params.id;
const images = ref<MyImage[]>([]);
const checkingAmount = ref(false);
const visibleCount = computed(() => {
    return images.value.length > 3 ? 3 : images.value.length;
});
interface FormState{
    ID_Product: number,
    ID_Color: number|null,
    ID_Material: number|null,
    ID_Dimensions: number|null,
    Amount: number
}
const initForm = {
    ID_Product: -1,
    Amount: 1,
    ID_Color: null,
    ID_Material: null,
    ID_Dimensions: null
}
let form = reactive<FormState>(JSON.parse(JSON.stringify(initForm)));

const responsiveOptions = ref([
    {
        breakpoint: '991px',
        numVisible: 4
    },
    {
        breakpoint: '767px',
        numVisible: 3
    },
    {
        breakpoint: '575px',
        numVisible: 1
    }
]);
function loadProduct()
{
    axios.get("http://localhost:8081/product/" + idProduct).then(res=>{
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
const changeAmount = (event:Event) => {
    let button = event.target as HTMLElement;

    if(button.className.includes('btn-minuse'))
        form.Amount = form.Amount > 1 ? form.Amount -1 : form.Amount;
    else
        form.Amount++;
}
function BuyNow(){}
function AddCart(){}
onMounted(()=>{
    loadProduct();
})
</script>
<template>
    <div class="p-5 mt-5 bg-light" style="border-radius: 25px;">
        <div class="row w-100">
            <div class="col-lg-5">
                <div class="p-3">
                        <Galleria v-if="myProduct" :value="images" :responsiveOptions="responsiveOptions" :numVisible="visibleCount" containerStyle="max-width: 640px">
                            <template #item="slotProps">
                                <Image :src="'http://localhost:8081' + slotProps.item.itemImageSrc" height="400"  :alt="slotProps.item.alt" :id="'product-avt'"  preview />
                            </template>
                            <template #thumbnail="slotProps">
                                <img :src="'http://localhost:8081' + slotProps.item.thumbnailImageSrc" width="100" height="100" :alt="slotProps.item.alt" />
                            </template>
                        </Galleria>
                        <div v-else>
                            <Skeleton height="20rem" class="mb-2"></Skeleton>
                            <Skeleton height="10rem" class="mb-2"></Skeleton>

                        </div>


                    <!-- <div class="row border">
                        <div class="d-flex justify-content-center w3-animate-zoom">
                            <img :src="myProduct?.Avatar" style="width: 100%;">
                        </div>
                        <div class="d-flex justify-content-around" style="width: 100%;">
                            <img v-for="img in myProduct?.detail_product_image.slice(0,3)" width="100" class="border w3-animate-zoom" :src="img.Image">
                        </div>

                    </div>	 -->
                </div>
            </div>
            <div class="col-lg-7 w3-animate-bottom">
                <div class="pl-3 ml-3">
                    <input value="<?=$data['Product']['ID_Product']?>" id="product_id" hidden>
                    <h1 v-if="myProduct">{{ myProduct?.name }}</h1>
                    <Skeleton v-else height="4rem" class="mb-2"></Skeleton>
                    <!-- <div v-if="myProduct && myProduct.detail_sale_of_product.length>0">
                        <del class="text-primary h3"><strong>{{ LazyConvert.ToMoney(myProduct?.Price) }}</strong></del>
                        <p class="text-danger h3"><strong>{{ LazyConvert.ToMoney(myProduct?.Price_SaleOff) }}</strong></p>

                    </div>
                    <p v-else-if="myProduct" class="text-danger h3"><strong>{{ LazyConvert.ToMoney(myProduct?.Price) }}</strong></p> -->
                    <p v-if="myProduct" class="text-danger h3"><strong>{{ Helper.ToMoney(myProduct!.price) }}</strong></p>
                    <Skeleton v-else height="2rem" class="mb-2"></Skeleton>

                    <hr>
                    <div v-if="myProduct">
                        <div>
                            <span><b>Color:</b></span>
                            <!-- <span v-if="isNotHaveColor()">
                                {{ ' Không xác định' }}
                            </span>
                            <SelectButton  v-else @change="CheckAmount()" v-model="form.ID_Color" optionValue="ID_Color" class="mt-3" :options="myProduct?.detail_product_color" optionLabel="color.Name_Color"  /> -->
                        </div>

                        <div class="mt-4">
                            <span><b>Material:</b></span>
                            <!-- <span v-if="isNotHaveMaterial()">
                                {{ ' Không xác định' }}
                            </span>
                            <SelectButton  v-else @change="CheckAmount()" v-model="form.ID_Material" optionValue="ID_Material" class="mt-3" :options="myProduct?.detail_product_material" optionLabel="material.Name_Material"  /> -->
                        </div>
                        <div class="mt-4">
                            <span><b>Dimensions:</b></span>
                            <!-- <span v-if="isNotHaveSize()">
                                {{ ' Không xác định' }}
                            </span>
                            <SelectButton  v-else @change="CheckAmount()" v-model="form.ID_Dimensions" optionValue="ID_D" class="mt-3" :options="myProduct?.dimensions" optionLabel="Name_D"  /> -->
                        </div>

                        <div class="row g-3 align-items-center mt-4">
                            <div class="col-auto">
                                <label for="inputPassword6" class="col-form-label">Amount</label>
                            </div>
                            <div class="col-auto">
                                <div class="input-group" style="width:50%">
                                    <span class="input-group-btn">
                                        <button class="btn btn-white btn-minuse" @click="changeAmount" type="button">-</button>
                                    </span>
                                    <input v-model="form.Amount" type="text" class="form-control no-padding add-color text-center height-25" maxlength="3">
                                    <span class="input-group-btn">
                                        <button class="btn btn-red btn-pluss" @click="changeAmount" type="button">+</button>
                                    </span>
                                </div>
                            </div>

                        </div>

                        <div class="row mt-3">
                            <button class="btn btn-primary" @click="AddCart"><i class="fas fa-shopping-cart mr-2" ></i>Add to cart</button>

                            <button @click="BuyNow" class="btn btn-success ml-3" :disabled="myProduct.amount <= 0" value="Buy now" type="submit" >Buy now</button>

                        </div>
                        <div class="row mt-3">
                            <b class="mr-2">Stock:</b> <Badge v-if="checkingAmount" value="Checking..."></Badge>
                                     <Badge v-else-if="myProduct.amount == -1" value="Chưa xác định"></Badge>
                                     <Badge v-else :value="myProduct.amount"></Badge>
                        </div>
                    </div>
                    <Skeleton v-else height="20rem" class="mb-2"></Skeleton>

                </div>

            </div>
        </div>
    </div>
</template>