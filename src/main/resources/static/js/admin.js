const { createApp, ref, onMounted, nextTick, watch, computed } = Vue;
createApp({
    setup() {
        const currentTab = ref('stats');
        const adminName = ref('管理员');
        const range = ref('week');
        const dishes = ref([]);
        const allOrders = ref([]);
        const latestOrders = ref([]);
        const showModal = ref(false);
        const showCancelModal = ref(false);
        const cancelTarget = ref({});
        const cancelReason = ref('');
        const lowStockCount = ref(0);
        const stats = ref({ revenue: 0, profit: 0, orderCount: 0 });
        const newDish = ref({ name: '', category: '荤菜', price: 0, cost: 0, stock: 10, imageUrl: '', status: 1, saleType: 0 });

        const checkAuth = () => {
            const user = JSON.parse(localStorage.getItem('user') || '{}');
            if (user.role !== 'ADMIN') { alert('权限不足'); window.location.href = 'login.html'; }
            adminName.value = user.username;
        };
        const logout = () => { localStorage.clear(); window.location.href = 'login.html'; };

        const fetchDashboard = async () => {
            try {
                const res = await axios.get('/api/canteen/admin/dashboard');
                if (res.data.success) {
                    stats.value = res.data.data.overview;
                    latestOrders.value = res.data.data.latestOrders || [];
                    renderChart(res.data.data.chart);
                }
            } catch (e) { console.error('大屏数据加载失败'); }
        };

        const renderChart = (chartData) => {
            nextTick(() => {
                const dom = document.getElementById('trendChart');
                if (!dom) return;
                const existing = echarts.getInstanceByDom(dom);
                if (existing) existing.dispose();
                const myChart = echarts.init(dom);
                myChart.setOption({
                    color: ['#ff6b01'],
                    tooltip: { trigger: 'axis' },
                    xAxis: { type: 'category', data: chartData.map(i => i.date) },
                    yAxis: { type: 'value' },
                    series: [{ name: '营业额', type: 'line', smooth: true, areaStyle: { color: 'rgba(255,107,1,0.3)' }, data: chartData.map(i => i.value) }]
                });
            });
        };

        const updateRange = async (newRange) => {
            range.value = newRange;
            const res = await axios.get(`/api/canteen/admin/dashboard?range=${newRange}`);
            if (res.data.success) { stats.value = res.data.data.overview; renderChart(res.data.data.chart); }
        };

        const loadDishes = async () => {
            const res = await axios.get('/api/canteen/admin/dish/all');
            if (res.data.success) { dishes.value = res.data.data; lowStockCount.value = dishes.value.filter(d => d.stock < 5).length; }
        };
        const openAddModal = () => { newDish.value = { name: '', category: '荤菜', price: 0, cost: 0, stock: 10, imageUrl: '', status: 1, saleType: 0 }; showModal.value = true; };
        const handleEdit = (dish) => { newDish.value = { ...dish }; showModal.value = true; };
        const handleDelete = async (id) => { if (confirm('确定删除吗？')) { await axios.delete(`/api/canteen/admin/dish/${id}`); loadDishes(); } };
        const toggleStatus = async (dish) => {
            const res = await axios.put(`/api/canteen/admin/dish/status/${dish.id}/${dish.status===1?0:1}`);
            if (res.data.success) loadDishes();
        };
        const handleFileUpload = async (event) => {
            const file = event.target.files[0];
            const formData = new FormData();
            formData.append('file', file);
            try { const res = await axios.post('/api/canteen/common/upload', formData); if (res.data.success) newDish.value.imageUrl = res.data.data; }
            catch (e) { alert('图片上传失败'); }
        };
        const submitSave = async () => {
            const url = newDish.value.id ? '/api/canteen/admin/dish/update' : '/api/canteen/admin/dish/add';
            const method = newDish.value.id ? 'put' : 'post';
            const res = await axios[method](url, newDish.value);
            if (res.data.success) { showModal.value = false; loadDishes(); }
        };

        const loadOrders = async () => {
            const res = await axios.get('/api/canteen/admin/orders');
            if (res.data.success) allOrders.value = res.data.data;
        };
        const updateStatus = async (id, status) => {
            await axios.put(`/api/canteen/admin/order/status/${id}/${status}`);
            loadOrders(); fetchDashboard();
        };
        const openCancelModal = (order) => {
            cancelTarget.value = order;
            cancelReason.value = '';
            showCancelModal.value = true;
        };
        const submitCancel = async () => {
            if (!cancelReason.value.trim()) { alert('请填写取消理由'); return; }
            try {
                const res = await axios.post(`/api/canteen/admin/order/cancel?orderId=${cancelTarget.value.id}&reason=${encodeURIComponent(cancelReason.value)}`);
                if (res.data.success) { showCancelModal.value = false; loadOrders(); fetchDashboard(); }
                else alert(res.data.message || '取消失败');
            } catch (e) { alert('请求失败，请重试'); }
        };

        const pendingOrdersCount = computed(() => allOrders.value.filter(o => o.status == 1).length);
        const completedOrdersCount = computed(() => allOrders.value.filter(o => o.status == 2).length);
        const shippingOrdersCount = ref(0);

        watch(currentTab, (newTab) => {
            if (newTab === 'orders') loadOrders();
            if (newTab === 'stats') fetchDashboard();
            if (newTab === 'menu') loadDishes();
        });
        onMounted(() => { checkAuth(); loadDishes(); fetchDashboard(); loadOrders(); });

        return {
            currentTab, adminName, range, dishes, allOrders, latestOrders,
            showModal, showCancelModal, cancelTarget, cancelReason,
            lowStockCount, stats, newDish,
            pendingOrdersCount, completedOrdersCount, shippingOrdersCount,
            logout, updateRange, openAddModal, handleEdit, submitSave,
            toggleStatus, updateStatus, loadOrders, handleDelete, handleFileUpload,
            openCancelModal, submitCancel
        };
    }
}).mount('#app');
