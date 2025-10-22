<script setup lang="ts">
import {ref,onMounted} from 'vue';
const hasLogin = ref(false);


onMounted(()=>{

});
</script>

<template>
    
    <div class="row ps-0">
            
            <div class="top-menu bg-white border ps-0 pe-0 d-flex">
                <div class="flex-grow-1 d-flex align-items-center animate__animated animate__lightSpeedInLeft">
                    <router-link to="/"><img src="/images/icon.png" width="35" height="35" style="margin: 5px 5px 0 5px;    border-radius: 50px;"></router-link>
                    <router-link id="lazycodet" class="btn" to="/" style="font-family: Lucida Console;font-size: 22px;text-align: center;color: #9e84df">LazyCodet</router-link>
                    <div class="d-flex w-100">
                        <div class="search-box-main d-flex position-relative flex-column" id="search_bar">
                            <form class="search_main_form d-flex m-0">
                                <input id="search_main" class="form-control me-2 ff-FontAwesome" type="text" style="height: 50px;" placeholder="&#xF002; Search">
                                <ul id="search-results-main" class="list-group" style="top:40px"></ul>
                                <button id="btn_search_main" class="btn btn-light" type="button" data-bs-toggle="offcanvas" data-bs-target="#sidebar" aria-controls="sidebar"><i class="fas fa-search"></i></button>
                            
                            </form>
                        </div>
                    </div>
                </div>
                <div class='d-flex align-items-center animate__animated animate__lightSpeedInRight' style="margin: 0 10px 0 0;">
                    
                    <div v-if="hasLogin" class="bell-box d-inline-block position-relative">
                        <span id='count_notification' class="position-absolute d-block bg-danger text-light rounded-circle text-center d-none" style="min-width: 20px; right:-5px;top: -13px;"></span>
                        <button data-bs-toggle="dropdown" aria-haspopup="true" aria-expanded="false" style="border-width: 0px;background: transparent;"><i class="fa-solid fa-bell" style="font-size: 20px;    text-shadow: 0 0 3px #000;-webkit-text-fill-color: white;-webkit-text-stroke-width: 1px;-webkit-text-stroke-color: black;" ></i></button>
                        <div class="dropdown-menu p-3" style="width: 300px; height: 500px;overflow-y: auto;">
                            <div>
                            <button id="mark_all" class="float-end btn btn-link">Đánh dấu tất cả đã đọc</button>
                            </div>    
                            <br><br>
                            <div id="notification_box"></div>
                        </div>
                    </div>
                    <div class="d-flex flex-row align-items-center">
                        <div>
                            <label class="form-check-label" for="lightSwitch">
                                <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-moon-stars" viewBox="0 0 16 16">
                                    <path d="M6 .278a.768.768 0 0 1 .08.858 7.208 7.208 0 0 0-.878 3.46c0 4.021 3.278 7.277 7.318 7.277.527 0 1.04-.055 1.533-.16a.787.787 0 0 1 .81.316.733.733 0 0 1-.031.893A8.349 8.349 0 0 1 8.344 16C3.734 16 0 12.286 0 7.71 0 4.266 2.114 1.312 5.124.06A.752.752 0 0 1 6 .278zM4.858 1.311A7.269 7.269 0 0 0 1.025 7.71c0 4.02 3.279 7.276 7.319 7.276a7.316 7.316 0 0 0 5.205-2.162c-.337.042-.68.063-1.029.063-4.61 0-8.343-3.714-8.343-8.29 0-1.167.242-2.278.681-3.286z"/>
                                    <path d="M10.794 3.148a.217.217 0 0 1 .412 0l.387 1.162c.173.518.579.924 1.097 1.097l1.162.387a.217.217 0 0 1 0 .412l-1.162.387a1.734 1.734 0 0 0-1.097 1.097l-.387 1.162a.217.217 0 0 1-.412 0l-.387-1.162A1.734 1.734 0 0 0 9.31 6.593l-1.162-.387a.217.217 0 0 1 0-.412l1.162-.387a1.734 1.734 0 0 0 1.097-1.097l.387-1.162zM13.863.099a.145.145 0 0 1 .274 0l.258.774c.115.346.386.617.732.732l.774.258a.145.145 0 0 1 0 .274l-.774.258a1.156 1.156 0 0 0-.732.732l-.258.774a.145.145 0 0 1-.274 0l-.258-.774a1.156 1.156 0 0 0-.732-.732l-.774-.258a.145.145 0 0 1 0-.274l.774-.258c.346-.115.617-.386.732-.732L13.863.1z"/>
                                </svg>
                            </label>
                        </div>
                        <div class="ms-1 form-check form-switch" >
                            <input class="form-check-input" type="checkbox" id="toggleDarkMode" />
                        </div>
                    </div>
                    
                    <!-- #Menu_Language -->
                    <a class="nav-link dropdown-toggle d-inline" href="#" id="language_menu" role="btn" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
                        &#127760; <span>Language</span></a> 
                    <div class="dropdown-menu dropdown-menu-right" aria-labelledby="language_menu">
                        <a class="dropdown-item" href="/Home/ChangeLanguage/en">English</a>
                        <a class="dropdown-item" href="/Home/ChangeLanguage/vi">Vietnamese</a>
                    </div>
                    <!-- <a  class="btn-donate btn bg-purple color-white" href="/Donate" target="_blank">Donate</a> -->
                    <div style="display: inline-block;">
                        <div v-if="hasLogin">
                            <div id="menu-profle-button-wd" class="hover p-10">
                                <span>NGUYEN VAN A</span>
                                <img src="{{ session('avatar_crop') ?? '/images/icon-profile.png' }}" width="35" style="margin: 0 5px 0 5px;border-radius: 50px;">  
                            </div>
                            <div class="profile-menu" style="display: none;">
                                <router-link to="/Personal/Introduce/{{ $infoUser['username'] }}" class="btn bg-white c-black m-1">My Page</router-link>
                                <a class="btn bg-white c-black m-1" href="{{ route('logout') }}"
                                        onclick="event.preventDefault();
                                                        document.getElementById('logout-form').submit();">
                                        <!-- {{ $lang['log_out'] }} -->
                                        LOGOUT
                                    </a>

                                    <form id="logout-form" action="{{ route('logout') }}" method="POST" class="d-none">
                                        @csrf
                                    </form>
                                
                            </div>
                        </div>
                        <div v-else>
                            <a href="/login" class="btn bg-green c-white">Sign in</a>
                        </div>
                    
                        
                    </div>

                </div>
                <!-- Sidebar Search -->
                <div id="sidebar" class="offcanvas offcanvas-start" style="z-index: 10000;">
                    <div class="d-flex flex-column flex-shrink-0 p-3 text-white bg-light" style="height: 100vh;">
                    <form class="search_main_form">
                        <input id="search_main_mobie" class="form-control me-2 ff-FontAwesome" type="text" style="height: 50px;" placeholder="&#xF002; Search">
                        <ul id="search-results-main-mobie" class="list-group" style="top:40px"></ul>
                    </form>
                    
                    </div>
                </div>
                <!-- // Sidebar Search -->
            </div>
        </div>


    
</template>