import axios from "axios";
import { Ref } from "vue";
import { User } from "../interfaces/app";

export class MyApp{
    private static instance: MyApp;
    private constructor()
    {
    }
    authenticate(isAuthenticated: Ref<boolean>, user: Ref<User | undefined>)
    {
        const token = localStorage.getItem("token");
        axios.post("http://localhost:8081/authenticate", undefined, {
            headers:{
                "Authorization": `Bearer ${token}`
            }
        }).then(res=>{
            if(res.status == 200)
            {
                isAuthenticated.value = true;
                user.value = res.data.user;
            }
        });
    }


    public static getIntance()
    {
        if(!MyApp.instance)
            MyApp.instance = new MyApp();
        return MyApp.instance;
    }
}