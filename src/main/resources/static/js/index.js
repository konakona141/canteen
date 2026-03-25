const {createApp,ref,computed,onMounted}=Vue;
createApp({
    setup(){
        const user=ref(JSON.parse(localStorage.getItem(`user`)||`{}`));
        const page=ref(`home`),mTab=ref(`current`);
        const menu=ref({presale:[],current:[]}),cart=ref([]),orders=ref([]),curP=ref(1),perP=5;
        const curList=computed(()=>menu.value[mTab.value]||[]);
        const totalQty=computed(()=>cart.value.reduce((s,i)=>s+i.qty,0));
        const totalAmt=computed(()=>cart.value.reduce((s,i)=>s+(i.price*i.qty),0).toFixed(2));
        const totalPages=computed(()=>Math.ceil(orders.value.length/perP)||1);
        const pageOrders=computed(()=>{const s=(curP.value-1)*perP;return orders.value.slice(s,s+perP);});

        const loadMenu=async()=>{
            try{const r=await axios.get(`/api/canteen/menu`);if(r.data.success)menu.value=r.data.data;}
            catch(e){}
        };

        const addCart=d=>{
            if(d.stock<=0)return;
            const ex=cart.value.find(i=>i.dishId===d.id);
            if(ex){if(ex.qty<d.stock)ex.qty++;}
            else cart.value.push({dishId:d.id,name:d.name,price:parseFloat(d.price),qty:1,stock:d.stock,presale:d.saleType===1,deliveryType:`delivery`,deliveryTime:``});
        };

        const chgQty=(item,delta)=>{
            item.qty+=delta;
            if(item.qty<=0)cart.value=cart.value.filter(i=>i.dishId!==item.dishId);
            else if(item.qty>item.stock)item.qty=item.stock;
        };

        const placeOrder=async()=>{
            if(!user.value.id){window.location.href=`login.html`;return;}
            if(!user.value.address){alert(`请先填写配送地址`);page.value=`mine`;return;}
            if(!cart.value.length)return;
            for(const item of cart.value){
                if(!item.deliveryType){alert(`请为 ${item.name} 选择配送方式`);return;}
                if(item.presale&&!item.deliveryTime.trim()){alert(`预售商品 ${item.name} 请填写配送/取货时间段`);return;}
            }
            let allOk=true;
            for(const item of cart.value){
                try{
                    const r=await axios.post(`/api/canteen/order`,{
                        userId:user.value.id,
                        dishId:item.dishId,
                        quantity:item.qty,
                        deliveryType:item.deliveryType,
                        deliveryTime:item.deliveryTime||null
                    });
                    if(!r.data.success){alert(r.data.message||`下单失败`);allOk=false;break;}
                }catch(e){allOk=false;break;}
            }
            if(allOk){cart.value=[];loadMenu();alert(`下单成功！`);}
        };

        const loadOrders=async()=>{
            if(!user.value.id)return;
            try{const r=await axios.get(`/api/canteen/my-orders?userId=`+user.value.id);if(r.data.success)orders.value=r.data.data;}
            catch(e){}
        };

        const cancelOrd=async o=>{
            if(!confirm(`确认取消该订单？`))return;
            try{
                const r=await axios.post(`/api/canteen/order/cancel?orderId=`+o.id+`&userId=`+user.value.id);
                if(r.data.success)loadOrders();else alert(r.data.message||`取消失败`);
            }catch(e){}
        };

        const saveAddr=async()=>{
            if(!user.value.id)return;
            try{
                const r=await axios.post(`/api/canteen/user/update`,user.value);
                if(r.data.success){
                    if(r.data.data)user.value={...user.value,...r.data.data};
                    localStorage.setItem(`user`,JSON.stringify(user.value));
                    alert(`保存成功`);
                }else alert(r.data.message||`保存失败`);
            }catch(e){}
        };

        const switchMine=async()=>{
            if(!user.value.id){window.location.href=`login.html`;return;}
            page.value=`mine`;curP.value=1;
            try{
                const r=await axios.get(`/api/canteen/user/profile?userId=`+user.value.id);
                if(r.data.success&&r.data.data){
                    user.value={...user.value,...r.data.data};
                    localStorage.setItem(`user`,JSON.stringify(user.value));
                }
            }catch(e){}
            loadOrders();
        };

        const sTxt=s=>({'1':'制作中','2':'已完成','3':'已取消'})[s]||s;
        const sCls=s=>({'1':'stp','2':'std','3':'stc'})[s]||'stc';
        const goLogin=()=>window.location.href=`login.html`;
        const logout=()=>{localStorage.clear();window.location.href=`login.html`;};
        onMounted(loadMenu);
        return{user,page,mTab,menu,curList,cart,totalQty,totalAmt,orders,pageOrders,curP,totalPages,
            addCart,chgQty,placeOrder,cancelOrd,saveAddr,switchMine,sTxt,sCls,goLogin,logout};
    }
}).mount(`#app`);
