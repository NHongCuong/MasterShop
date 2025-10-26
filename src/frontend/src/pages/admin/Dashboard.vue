<script setup lang="ts">
import axios from 'axios';
import {computed, onMounted, ref} from 'vue'
//import {users,product,bill} from '../../../interfaces/app.ts';
import {User, Product, bill, Category} from '../../interfaces/app';
import Dropdown from 'primevue/dropdown';
import Chart from 'primevue/chart';
import Skeleton from 'primevue/skeleton';


interface Inventory {
  products: Product[],
  totalAmount: number;
}

const users = ref<User[]>();
const userLists = ref<User[]>([])
// Tính tổng số user
const totalUsers = computed(() => userLists.value.length)
// Hàm lấy danh sách user từ backend
const fetchUsers = async () => {
  try {
    const response = await axios.get('http://localhost:8081/user')
    userLists.value = response.data
  } catch (error) {
    console.error('Lỗi khi lấy danh sách user:', error)
  }
}
onMounted(fetchUsers)

const products = ref<Product[]>();
const productLists = ref<Product[]>([])
// Tính tổng số product
const totalProducts = computed(() => productLists.value.length)
// Hàm lấy danh sách user từ backend
const fetchProducts = async () => {
  try {
    const response = await axios.get('http://localhost:8081/product/all')
    productLists.value = response.data
  } catch (error) {
    console.error('Lỗi khi lấy danh sách sản phẩm:', error)
  }
}
onMounted(fetchProducts)
//const categorys = ref<Category[]>();
const categoryLists = ref<Category[]>([])
// Tính tổng số product
const totalCategorys = computed(() => categoryLists.value.length)
// Hàm lấy danh sách user từ backend
const fetchCategorys = async () => {
  try {
    const response = await axios.get('http://localhost:8081/category/all')
    categoryLists.value = response.data
  } catch (error) {
    console.error('Lỗi khi lấy danh sách danh mục:', error)
  }
}
onMounted(fetchCategorys)
const bills = ref<bill[]>();
const inventory = ref<Inventory>({
  products: [],
  totalAmount: 0

});
const FilterOptions = ref([
  {name: 'Toàn bộ thời gian', code: 'A'},
  {name: '1 tháng', code: '1'},
  {name: '3 tháng', code: '3'},
  {name: '6 tháng', code: '6'},
  {name: '1 năm', code: '12'}
]);
const selectedFilter = ref(FilterOptions.value[0]);

const tongDoanhSo = ref(-1);
const top10 = ref<Product[]>([]);

const chartData = ref();
const chartOptions = ref();
const chartData_SoLuongNguoiDatHang = ref();
const chartOptions_SoLuongNguoiDatHang = ref();
const chartDataFromAPI = ref([]);
const chartDataFromAPI_SoLuongNguoiDatHang = ref([]);
const chartDataFromAPI_Bill = ref([]);
const chartData_Bill = ref();
const chartOptions_Bill = ref();

const setChartData = () => {
  let ngayOrThangOrNam = "";
  if (selectedFilter.value.code == "1")
    ngayOrThangOrNam = "Ngày ";
  else if (selectedFilter.value.code == "A")
    ngayOrThangOrNam = "Năm ";
  else
    ngayOrThangOrNam = "Tháng ";


  const labels = Object.keys(chartDataFromAPI.value).map(key => ngayOrThangOrNam + (parseInt(key)));
  return {

    labels: labels,
    datasets: [
      {
        label: 'Doanh số',
        data: Object.values(chartDataFromAPI.value),
        backgroundColor: ['rgba(255, 159, 64, 0.2)', 'rgba(75, 192, 192, 0.2)', 'rgba(54, 162, 235, 0.2)', 'rgba(153, 102, 255, 0.2)'],
        borderColor: ['rgb(255, 159, 64)', 'rgb(75, 192, 192)', 'rgb(54, 162, 235)', 'rgb(153, 102, 255)'],
        borderWidth: 1
      }
    ]
  };
};
const setChartOptions = () => {
  const documentStyle = getComputedStyle(document.documentElement);
  const textColor = documentStyle.getPropertyValue('--text-color');
  const textColorSecondary = documentStyle.getPropertyValue('--text-color-secondary');
  const surfaceBorder = documentStyle.getPropertyValue('--surface-border');

  return {
    plugins: {
      legend: {
        labels: {
          color: textColor
        }
      }
    },
    scales: {
      x: {
        ticks: {
          color: textColorSecondary
        },
        grid: {
          color: surfaceBorder
        }
      },
      y: {
        beginAtZero: true,
        ticks: {
          color: textColorSecondary
        },
        grid: {
          color: surfaceBorder
        }
      }
    }
  };
}


const setChartData_SoLuongNguoiDatHang = () => {
  let ngayOrThangOrNam = "";
  if (selectedFilter.value.code == "1")
    ngayOrThangOrNam = "Ngày ";
  else if (selectedFilter.value.code == "A")
    ngayOrThangOrNam = "Năm ";
  else
    ngayOrThangOrNam = "Tháng ";


  const labels = Object.keys(chartDataFromAPI_SoLuongNguoiDatHang.value).map(key => ngayOrThangOrNam + (parseInt(key) + 1));
  return {

    labels: labels,
    datasets: [
      {
        label: 'Số lần/người đặt',
        data: Object.values(chartDataFromAPI_SoLuongNguoiDatHang.value),
        backgroundColor: ['rgba(255, 159, 64, 0.2)', 'rgba(75, 192, 192, 0.2)', 'rgba(54, 162, 235, 0.2)', 'rgba(153, 102, 255, 0.2)'],
        borderColor: ['rgb(255, 159, 64)', 'rgb(75, 192, 192)', 'rgb(54, 162, 235)', 'rgb(153, 102, 255)'],
        borderWidth: 1
      }
    ]
  };
};
const setChartOptions_SoLuongNguoiDatHang = () => {
  const documentStyle = getComputedStyle(document.documentElement);
  const textColor = documentStyle.getPropertyValue('--text-color');
  const textColorSecondary = documentStyle.getPropertyValue('--text-color-secondary');
  const surfaceBorder = documentStyle.getPropertyValue('--surface-border');

  // Mảng chứa các giá trị số nguyên duy nhất
  const uniqueIntegers = Array.from({length: 10}, (_, i) => i);

  return {
    plugins: {
      legend: {
        labels: {
          color: textColor
        }
      }
    },
    scales: {
      x: {
        ticks: {
          color: textColorSecondary
        },
        grid: {
          color: surfaceBorder
        }
      },
      y: {
        beginAtZero: true,
        ticks: {
          color: textColorSecondary,
          callback: function (value: any, _index: any, _values: any) {
            // Chỉ hiển thị giá trị từ mảng uniqueIntegers
            return uniqueIntegers.includes(value) ? value.toString() : '';
          }
        },
        grid: {
          color: surfaceBorder
        }
      }
    }
  };
};


const setChartData_Bill = () => {
  const documentStyle = getComputedStyle(document.body);

  return {
    labels: Object.keys(chartDataFromAPI_Bill.value),
    datasets: [
      {
        data: Object.values(chartDataFromAPI_Bill.value),
        backgroundColor: [documentStyle.getPropertyValue('--blue-500'), documentStyle.getPropertyValue('--yellow-500'), documentStyle.getPropertyValue('--green-500'), documentStyle.getPropertyValue('--red-500')],
        hoverBackgroundColor: [documentStyle.getPropertyValue('--blue-400'), documentStyle.getPropertyValue('--yellow-400'), documentStyle.getPropertyValue('--green-400')]
      }
    ]
  };
};

const setChartOptions_Bill = () => {
  const documentStyle = getComputedStyle(document.documentElement);
  const textColor = documentStyle.getPropertyValue('--text-color');

  return {
    plugins: {
      legend: {
        labels: {
          usePointStyle: true,
          color: textColor
        }
      }
    }
  };
};

function ChooseFilter() {
  tongDoanhSo.value = -1;
  axios.post('/api/product/tongDoanhSo', {timeCode: selectedFilter.value.code}).then(res => {
    const resDataObject: Record<string, number> = res.data;

    tongDoanhSo.value = res.data;

    chartDataFromAPI.value = res.data;
    tongDoanhSo.value = 0
    Object.values(resDataObject).forEach((value: number) => {
      tongDoanhSo.value += value;
    });
    chartData.value = setChartData();
    chartOptions.value = setChartOptions();
  });
  axios.post('/api/product/tongNguoiDatHang', {timeCode: selectedFilter.value.code}).then(res => {
    chartDataFromAPI_SoLuongNguoiDatHang.value = res.data;

    chartData_SoLuongNguoiDatHang.value = setChartData_SoLuongNguoiDatHang();
    chartOptions_SoLuongNguoiDatHang.value = setChartOptions_SoLuongNguoiDatHang();
  });
}

const InventoryExport = () => {
  axios({
    url: '/export',
    method: 'GET',
    responseType: 'blob', // quan trọng để xử lý dữ liệu nhị phân
  }).then((response) => {
    const url = window.URL.createObjectURL(new Blob([response.data]));
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', 'products.xlsx');
    document.body.appendChild(link);
    link.click();
  });

}
onMounted(() => {

  axios.post('/api/bill/thongke').then(res => {
    chartDataFromAPI_Bill.value = res.data;
    chartData_Bill.value = setChartData_Bill();
    chartOptions_Bill.value = setChartOptions_Bill();
  });
  axios.get('/api/users').then(res => {
    users.value = res.data;
  });
  axios.get('/api/products/_/_').then(res => {
    products.value = res.data;
  });
  axios.get('/api/bill/processing').then(res => {
    bills.value = res.data;
  });
  axios.get('/api/products/inventory').then(res => {
    inventory.value = res.data;
  });
  axios.post('/api/products/top10').then(res => {
    top10.value = res.data;
  });
  ChooseFilter();
});
</script>
<template>
  <div class="content-header">
    <div class="container-fluid">
      <div class="row mb-2">
        <div class="col-sm-6">
          <h1 class="m-0">Tổng quan</h1>
        </div>
        <div class="col-sm-6">
          <ol class="breadcrumb float-sm-right">
            <li class="breadcrumb-item"><a href="/">Home</a></li>
            <li class="breadcrumb-item active">Starter Page</li>
          </ol>
        </div>
      </div>
    </div>
  </div>


  <div class="content">
    <div class="container-fluid">
      <div class="row">
        <div class="col-12 col-sm-6 col-md-6">
          <div class="small-box bg-info p-2">
            <div class="inner">
              <router-link to="/admin/user" active-class="active" class="nav-link">
                <h3 id="total_users">{{ totalUsers }}</h3>
                <p>Total users</p>
              </router-link>
            </div>
            <div class="icon">
              <i class="fas fa-users"></i>
            </div>
          </div>
        </div>
        <div class="col-12 col-sm-6 col-md-6">
          <div class="small-box bg-primary p-2">
            <div class="inner">
              <router-link to="/admin/product" active-class="active" class="nav-link">
                <h3 id="total_users">{{ totalProducts }}</h3>
                <p>Total products</p>
              </router-link>
            </div>
            <div class="icon">
              <i class="fas fa-table"></i>
            </div>
          </div>
        </div>
        <div class="col-12 col-sm-6 col-md-6">
          <div class="small-box bg-primary p-2">
            <div class="inner">
              <router-link to="/admin/category" active-class="active" class="nav-link">
                <h3 id="total_users">{{ totalCategorys }}</h3>
                <p>Total Categorys</p>
              </router-link>
            </div>
            <div class="icon">
              <i class="fas fa-table"></i>
            </div>
          </div>
        </div>
        <div class="col-12 col-sm-6 col-md-6">
          <div class="small-box bg-warning p-2">
            <div class="inner">
              <h3 id="total_users">{{ bills?.length }}</h3>

              <div class="d-flex">
                <p>Prosessing Orders</p>
                <router-link to="/admin/bill/1" class="btn btn-dark ml-3">Xem</router-link>
              </div>
            </div>
            <div class="icon">
              <i class="fas fa-snowboarding"></i>
            </div>
          </div>
        </div>
        <div class="col-12 col-sm-6 col-md-6">
          <div class="small-box bg-secondary p-2">
            <div class="inner">
              <h3 id="total_users">{{ inventory.totalAmount }}</h3>
              <div class="d-flex">
                <p>Total stock products</p>
                <button @click="InventoryExport" class="btn btn-light ml-3">Excel Donwload</button>
              </div>

            </div>
            <div class="icon">
              <i class="fas fa-layer-group"></i>
            </div>
          </div>
        </div>


      </div>
      <div class="row mb-5">
        <div class="d-flex flex-row justify-content-center w-100">
          <Skeleton v-if="!chartOptions_Bill" shape="circle" size="20rem"></Skeleton>
          <Chart v-else type="pie" :data="chartData_Bill" :options="chartOptions_Bill" class="w-full md:w-30rem"/>
        </div>

      </div>
      <div class="row">
        <div>
          Lọc theo
          <Dropdown v-model="selectedFilter" @change="ChooseFilter" :options="FilterOptions" optionLabel="name"
                    placeholder="Chọn 1 tùy chọn" class="w-full md:w-14rem"/>
        </div>

      </div>
      <br>
      <div class="row">

        <div class="col-12 col-sm-6 col-md-6">
          <div class="small-box bg-secondary p-2">
            <div class="inner">
              <h3 v-if="tongDoanhSo == -1">Loading...</h3>
              <h3 v-else>{{ tongDoanhSo.toLocaleString('it-IT', {style: 'currency', currency: 'VND'}) }}</h3>
              <div class="d-flex">
                <p>Doanh số</p>
              </div>

            </div>
            <div class="icon">
              <i class="fas fa-layer-group"></i>
            </div>
          </div>
        </div>
        <!-- Biểu đồ doanh số -->
        <Chart class="w-100" type="bar" :data="chartData" :options="chartOptions"/>
        <!-- Biểu đồ số lượng người mua đặt hàng -->
        <h1>Số lượng lần đặt mua hàng</h1>
        <Chart class="w-100" type="line" :data="chartData_SoLuongNguoiDatHang"
               :options="chartOptions_SoLuongNguoiDatHang"/>
      </div>
      <h1>Top các sản phẩm được mua gần đây</h1>
      <table class="table">
        <thead>
        <tr>
          <th></th>
          <th>Tên sản phẩm</th>
          <th>Mã sản phẩm</th>
        </tr>
        </thead>
        <tbody>

        <tr v-for="(sp, index) in top10" :key="index">
          <td>{{ index + 1 }}</td>
          <td>{{ sp.name }}</td>
          <td>#{{ sp.id }}</td>
        </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>
