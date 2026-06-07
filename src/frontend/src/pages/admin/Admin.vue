<script setup lang="ts">
import {MyApp, state} from '../../app/MyApp';
import {onMounted} from 'vue';
import BackToTop from '../../components/BackToTop.vue';

onMounted(() => {
  MyApp.getInstance().authenticate();
})
</script>
<template>
  <nav class="main-header navbar navbar-expand navbar-white navbar-light">

    <ul class="navbar-nav">
      <li class="nav-item">
        <a class="nav-link" data-widget="pushmenu" href="#" role="button"><i class="fas fa-bars"></i></a>
      </li>
      <li class="nav-item d-none d-sm-inline-block">
        <a href="/" class="nav-link">Shop</a>
      </li>
      <li class="nav-item d-none d-sm-inline-block">
        <a href="#" class="nav-link">Contact</a>
      </li>
    </ul>

    <ul class="navbar-nav ml-auto">

      <li class="nav-item">
        <a class="nav-link" data-widget="navbar-search" href="#" role="button">
          <i class="fas fa-search"></i>
        </a>
        <div class="navbar-search-block">
          <form class="form-inline">
            <div class="input-group input-group-sm">
              <input class="form-control form-control-navbar" type="search" placeholder="Search"
                     aria-label="Search">
              <div class="input-group-append">
                <button class="btn btn-navbar" type="submit">
                  <i class="fas fa-search"></i>
                </button>
                <button class="btn btn-navbar" type="button" data-widget="navbar-search">
                  <i class="fas fa-times"></i>
                </button>
              </div>
            </div>
          </form>
        </div>
      </li>

      <li class="nav-item dropdown">
        <a class="nav-link" data-toggle="dropdown" href="#">
          <i class="far fa-comments"></i>
          <span class="badge badge-danger navbar-badge">3</span>
        </a>
        <div class="dropdown-menu dropdown-menu-lg dropdown-menu-right">
          <a href="#" class="dropdown-item">

            <div class="media">
              <img src="https://adminlte.io/themes/v3/dist/img/user1-128x128.jpg" alt="User Avatar"
                   class="img-size-50 mr-3 img-circle">
              <div class="media-body">
                <h3 class="dropdown-item-title">
                  Brad Diesel
                  <span class="float-right text-sm text-danger"><i class="fas fa-star"></i></span>
                </h3>
                <p class="text-sm">Call me whenever you can...</p>
                <p class="text-sm text-muted"><i class="far fa-clock mr-1"></i> 4 Hours Ago</p>
              </div>
            </div>

          </a>
          <div class="dropdown-divider"></div>
          <a href="#" class="dropdown-item">

            <div class="media">
              <img src="https://adminlte.io/themes/v3/dist/img/user8-128x128.jpg" alt="User Avatar"
                   class="img-size-50 img-circle mr-3">
              <div class="media-body">
                <h3 class="dropdown-item-title">
                  John Pierce
                  <span class="float-right text-sm text-muted"><i class="fas fa-star"></i></span>
                </h3>
                <p class="text-sm">I got your message bro</p>
                <p class="text-sm text-muted"><i class="far fa-clock mr-1"></i> 4 Hours Ago</p>
              </div>
            </div>

          </a>
          <div class="dropdown-divider"></div>
          <a href="#" class="dropdown-item">

            <div class="media">
              <img src="https://adminlte.io/themes/v3/dist/img/user3-128x128.jpg" alt="User Avatar"
                   class="img-size-50 img-circle mr-3">
              <div class="media-body">
                <h3 class="dropdown-item-title">
                  Nora Silvester
                  <span class="float-right text-sm text-warning"><i class="fas fa-star"></i></span>
                </h3>
                <p class="text-sm">The subject goes here</p>
                <p class="text-sm text-muted"><i class="far fa-clock mr-1"></i> 4 Hours Ago</p>
              </div>
            </div>

          </a>
          <div class="dropdown-divider"></div>
          <a href="#" class="dropdown-item dropdown-footer">See All Messages</a>
        </div>
      </li>

      <li class="nav-item dropdown">
        <a class="nav-link" data-toggle="dropdown" href="#">
          <i class="far fa-bell"></i>
          <span class="badge badge-warning navbar-badge">15</span>
        </a>
        <div class="dropdown-menu dropdown-menu-lg dropdown-menu-right">
          <span class="dropdown-header">15 Notifications</span>
          <div class="dropdown-divider"></div>
          <a href="#" class="dropdown-item">
            <i class="fas fa-envelope mr-2"></i> 4 new messages
            <span class="float-right text-muted text-sm">3 mins</span>
          </a>
          <div class="dropdown-divider"></div>
          <a href="#" class="dropdown-item">
            <i class="fas fa-users mr-2"></i> 8 friend requests
            <span class="float-right text-muted text-sm">12 hours</span>
          </a>
          <div class="dropdown-divider"></div>
          <a href="#" class="dropdown-item">
            <i class="fas fa-file mr-2"></i> 3 new reports
            <span class="float-right text-muted text-sm">2 days</span>
          </a>
          <div class="dropdown-divider"></div>
          <a href="#" class="dropdown-item dropdown-footer">See All Notifications</a>
        </div>
      </li>
      <li class="nav-item">
        <a class="nav-link" data-widget="fullscreen" href="#" role="button">
          <i class="fas fa-expand-arrows-alt"></i>
        </a>
      </li>
      <li class="nav-item">
        <a class="nav-link" data-widget="control-sidebar" data-slide="true" href="#" role="button">
          <i class="fas fa-th-large"></i>
        </a>
      </li>
    </ul>
  </nav>
  <aside style="position:fixed; display:flex; flex-direction:column; height:100vh; overflow:hidden;" class="main-sidebar sidebar-dark-primary elevation-4">

    <a href="index3.html" class="brand-link text-center">
      <img src="/images/logotech.png" alt="AdminLTE Logo" class="brand-image img-circle elevation-3"
           style="border-radius:100px">
      <span class="brand-text font-weight-light">HC TECH</span>
    </a>

    <div class="sidebar" style="flex:1; overflow-y:auto; overflow-x:hidden;">

      <div class="user-panel mt-3 pb-3 mb-3 d-flex">
        <div class="image">
          <img src="/images/logotech.png" class="img-circle elevation-2" alt="User Image">
        </div>
        <div class="info">
          <a href="#" class="d-block text-uppercase fw-bold">{{ state.user?.nameUser || state.user?.email.split('@')[0] }}</a>
        </div>
      </div>

      <div class="form-inline">
        <div class="input-group" data-widget="sidebar-search">
          <input class="form-control form-control-sidebar" type="search" placeholder="Search"
                 aria-label="Search">
          <div class="input-group-append">
            <button class="btn btn-sidebar">
              <i class="fas fa-search fa-fw"></i>
            </button>
          </div>
        </div>
      </div>

      <nav class="mt-2">
        <ul class="nav nav-pills nav-sidebar flex-column" data-widget="treeview" role="menu"
            data-accordion="false">
          <li class="nav-item">
            <router-link to="/admin/dashboard" active-class="active" class="nav-link">
              <i class="nav-icon fas fa-th"></i>
              <p>
                Dashboard
                <span class="right badge badge-danger">New</span>
              </p>
            </router-link>
          </li>
          <li class="nav-item">
            <router-link to="/admin/user" active-class="active" class="nav-link">
              <i class="nav-icon fas fa-users"></i>
              <p>
                Users
              </p>
            </router-link>
          </li>
          <li class="nav-item">
            <router-link to="/admin/product" active-class="active" class="nav-link">
              <i class="nav-icon fas fa-shopping-bag"></i>
              <p>
                Products
              </p>
            </router-link>
          </li>
          <li class="nav-item">
            <router-link to="/admin/inventory" active-class="active" class="nav-link">
              <i class="nav-icon fas fa-warehouse"></i>
              <p>
                Quản lý tồn kho
              </p>
            </router-link>
          </li>
          <li class="nav-item">
            <router-link to="/admin/category" active-class="active" class="nav-link">
              <i class="nav-icon fas fa-list"></i>
              <p>
                Category
              </p>
            </router-link>
          </li>
          <li class="nav-item">
            <router-link to="/admin/color" active-class="active" class="nav-link">
              <i class="nav-icon fas fa-paint-brush"></i>
              <p>
                Colors
              </p>
            </router-link>
          </li>
          <li class="nav-item">
            <router-link to="/admin/material" active-class="active" class="nav-link">
              <i class="nav-icon fas fa-box-open"></i>
              <p>
                Materials
              </p>
            </router-link>
          </li>
          <li class="nav-item">
            <router-link to="/admin/product-color" active-class="active" class="nav-link">
              <i class="nav-icon fas fa-palette"></i>
              <p>
                Product Color
              </p>
            </router-link>
          </li>
          <li class="nav-item">
            <router-link to="/admin/product-material" active-class="active" class="nav-link">
              <i class="nav-icon fas fa-industry"></i>
              <p>
                Product Material
              </p>
            </router-link>
          </li>
          <li class="nav-item">
            <router-link to="/admin/product-dimensions" active-class="active" class="nav-link">
              <i class="nav-icon fas fa-ruler-combined"></i>
              <p>
                Product Dimensions
              </p>
            </router-link>
          </li>
          <li class="nav-item">
            <router-link to="/admin/saleoff" active-class="active" class="nav-link">
              <i class="nav-icon fas fa-tag"></i>
              <p>
                Khuyến mãi
              </p>
            </router-link>
          </li>
          <li class="nav-item">
            <router-link to="/admin/supplier" active-class="active" class="nav-link">
              <i class="nav-icon fas fa-truck"></i>
              <p>
                Nhà cung cấp
              </p>
            </router-link>
          </li>
          <li class="nav-item">
            <router-link to="/admin/bills" active-class="active" class="nav-link">
              <i class="nav-icon fas fa-file-invoice-dollar"></i>
              <p>
                Quản lý Hóa đơn
              </p>
            </router-link>
          </li>
          
          <li class="nav-item">
            <router-link to="/admin/bill/1" active-class="active" class="nav-link">
              <i class="nav-icon fas fa-file-invoice"></i>
              <p>
                Đơn hàng
              </p>
            </router-link>
          </li>
          
          <li class="nav-item">
            <a href="#" class="nav-link" @click="() => { MyApp.getInstance().clearState(); $router.push('/admin-login'); }">
              <i class="nav-icon fas fa-sign-out-alt"></i>
              <p>Đăng xuất</p>
            </a>
          </li>
        </ul>
      </nav>

    </div>

  </aside>

  <div class="content-wrapper">
    <router-view></router-view>
  </div>


  <footer class="main-footer">

    <strong>Copyright &copy; 2026 <a href="https://www.linkedin.com/in/cuongnguyenhong96/" target="_blank">Nguyễn Hồng Cường</a></strong>.
  </footer>

  <BackToTop />
</template>

<style scoped>
/* Custom scrollbar for sidebar */
.sidebar::-webkit-scrollbar {
  width: 4px;
}
.sidebar::-webkit-scrollbar-track {
  background: transparent;
}
.sidebar::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.15);
  border-radius: 4px;
}
.sidebar::-webkit-scrollbar-thumb:hover {
  background: rgba(255, 255, 255, 0.35);
}
/* Firefox */
.sidebar {
  scrollbar-width: thin;
  scrollbar-color: rgba(255,255,255,0.15) transparent;
}
</style>