<script setup lang="ts">
import axios from 'axios';
import {Category, Product} from '../../interfaces/app';
import { onMounted, ref } from 'vue';
import Helper from '../../helper/helper';
import '../../shop.css';
let myIndex = 0;
const isSearching = ref(false);
const categories = ref<Category[]>([]);
const products = ref<Product[]>([]);
function carousel() {
  var i;
  var timeOut = 4000;
  var x = document.getElementsByClassName("mySlides") as HTMLCollectionOf<HTMLElement>;;
  for (i = 0; i < x.length; i++) {
    x[i].style.display = "none";
  }
  myIndex++;
  if (myIndex > x.length) {myIndex = 1}
  x[myIndex-1].style.display = "block";
  var dataTimeout = x[myIndex-1].getAttribute('data-timeout');
  if(dataTimeout!=null)
    timeOut = parseInt(dataTimeout);
  setTimeout(carousel, timeOut); // Change image every 2 seconds

}
function loadCateogory()
{
    axios.get("http://localhost:8081/category/all").then(res=>{
        if(res.status == 200)
        {
            categories.value = res.data;
        }
    });
}
function loadProduct()
{
    axios.get("http://localhost:8081/product/all").then(res=>{
        if(res.status == 200)
        {
            products.value = res.data;
        }
    });
}
onMounted(()=>{
    carousel();
    loadCateogory();
    loadProduct();
})
</script>
<template>
    <div class="w3-content w3-display-container w3-animate-right-08">
      <img class="mySlides" src="/images/1.png" style="width:100%">
      <img class="mySlides" src="/images/2.png" style="width:100%;display:none;">
      <img class="mySlides" data-timeout="8000" src="/images/3.png" style="width:100%;display:none;">
      <img class="mySlides" data-timeout="8000" src="/images/4.png" style="width:100%;display:none;">
    </div>

    <div class="menu-product container bg-light rounded pt-2 mt-3 change-background" data-background="#9adbf6" style="justify-content: center;">
        <div class="row mt-4 mb-4" >
            <div v-for="ca in categories" class="col hover w3-animate-left-08">
                <router-link :to="'/?category=' + ca.name" class="text-reset text-decoration-none">
                    <img class="mx-auto d-block" :src="'http://localhost:8081' + ca.icon">
                    <p class="text-center">{{ ca.name }}</p>
                </router-link>
            </div>
        </div>
    </div>
    <hr>
    
    <div class="container bg-light rounded pt-2 mt-3 change-background" data-background="#f6d9d9">

        <!-- <div v-if="search.by=='_'" class="row mb-3">
                <h3 class="h3 w3-animate-opacity">Sản phẩm bán chạy <img width="50" src="/new.gif" /></h3>
        </div>
        <div v-if="search.by=='_'" class="row mb-3">
            <div class="card">

                <Carousel   :value="productsHot" :numVisible="3" :numScroll="1" :responsiveOptions="responsiveOptions" circular :autoplayInterval="3000">
                    <template #item="slotProps">
                        <div style="height: 350px;" class="border-1 surface-border border-round m-2 text-center py-5 px-3">
                            <div class="mb-3">
                                <router-link :to="'/product/' + slotProps.data.ID_Product" >
                                    <img style="width: 200px;height: 220px" :src="slotProps.data.Avatar" :alt="slotProps.data.Avatar" class="w-6 shadow-2">
                                </router-link>
                            </div>
                            <div>
                                <h4 class="mb-1"><router-link :to="'/product/' + slotProps.data.ID_Product">{{  slotProps.data.Name_Product  }}</router-link></h4>

                                <h6 v-if="slotProps.data.detail_sale_of_product.length>0" class="mt-0 mb-3">
                                    <del>{{ LazyConvert.ToMoney( slotProps.data.Price) }}</del>
                                </h6>
                                <h6 class="mt-0 mb-3">${{ LazyConvert.ToMoney( slotProps.data.Price) }}</h6>

                            </div>
                        </div>
                    </template>
                </Carousel>
            </div>
        </div>
        <hr>

        <div class="row mb-3 d-flex justify-content-between">
            <Toast />
            <div class="d-flex align-items-center">
                <span class="mr-2">Tìm kiếm bằng hình ảnh</span>
                <FileUpload mode="basic"   accept="image/*" customUpload @uploader="chooseUploadImage($event)" />
            </div>

            <div>
                <Button icon="fas fa-filter"></Button>
                <Dropdown v-model="filter_by" :options="options_filter" optionLabel="name"  placeholder="Sắp xếp theo" ></Dropdown>
            </div>

        </div>
        <hr>
        <div class="row">
            <h3 v-if=" search.by == 'category' " class="h3 w3-animate-opacity">Sản phẩm trong danh mục <b>"{{ search.keyword }}"</b></h3>
            <h3 v-else-if=" search.by == 'all' " class="h3 w3-animate-opacity">Kết quả tìm kiếm cho <b>"{{ search.keyword }}"</b></h3>
            <h3 v-else class="h3 w3-animate-opacity">Danh sách sản phẩm</h3>
        </div> 
        <ProgressSpinner v-if="isSearching" />-->
        <div v-if="products?.length==0 && !isSearching" class="py-5">
            <h4>No products !</h4>
        </div>
        <div v-else class="row w-100">


            <div v-for="pr in products" class="col-md-2 col-sm-6 bg-light pt-3 border-end border-2 rounded rounded-3 w3-animate-bottom-08 mt-3 mb-4">
                <div class="product-grid2">
                    <div class="product-image2">
                        <router-link :to="'/product/' + pr.id" >
                            <img class="pic-1" :src="'http://localhost:8081' + pr.avatar">
                            <!-- <div v-if="pr.detail_sale_of_product.length>0" class="position-absolute" style="top:5px; right:5px">
                                <Badge severity="danger" value="Giảm giá"></Badge>
                            </div> -->
                        </router-link>

                    </div>
                    <div class="product-content">
                        <h3 class="title"><router-link :to="'/product/' + pr.id">{{ pr.name }}</router-link></h3>
                        <!-- <div v-if="pr.detail_sale_of_product.length>0">
                            <span class="mt-0 mb-3">
                                <del>{{ LazyConvert.ToMoney( pr.Price) }}</del>
                            </span><br>
                            <span class="price">{{ LazyConvert.ToMoney( pr.Price_SaleOff) }}</span>
                        </div> -->

                        <!-- <span v-else class="price">{{ LazyConvert.ToMoney(pr.Price) }}</span> -->
                        <span class="price">{{ Helper.ToMoney(pr.price) }}</span>
                    </div>
                </div>
            </div>


        </div>
    </div>
</template>