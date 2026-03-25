const { createApp, ref, onMounted } = Vue;

createApp({
    setup() {
        const isLogin = ref(true);
        const loading = ref(false);
        const rememberMe = ref(false);
        const form = ref({
            username: '',
            password: ''
        });
        const confirmPassword = ref('');

        onMounted(() => {
            const savedUser = localStorage.getItem('rememberedUser');
            if (savedUser) {
                form.value.username = savedUser;
                rememberMe.value = true;
            }
        });

        const handleSubmit = async () => {
            if (!form.value.username || !form.value.password) {
                alert("⚠️ 请完整填写用户名和密码");
                return;
            }

            loading.value = true;

            const payload = {
                username: form.value.username,
                password: md5(form.value.password)
            };

            try {
                if (isLogin.value) {
                    const res = await axios.post('/api/canteen/login', payload);

                    if (res.data.success) {
                        const userData = res.data.data;

                        if (rememberMe.value) {
                            localStorage.setItem('rememberedUser', form.value.username);
                        } else {
                            localStorage.removeItem('rememberedUser');
                        }

                        localStorage.setItem('user', JSON.stringify({
                            username: userData.username,
                            role: userData.role,
                            id: userData.id
                        }));

                        if (userData.role === 'ADMIN') {
                            window.location.href = 'admin.html';
                        } else {
                            window.location.href = 'index.html';
                        }
                    } else {
                        alert("❌ 登录失败：" + (res.data.message || "用户名或密码错误"));
                    }
                } else {
                    if (form.value.password !== confirmPassword.value) {
                        alert("⚠️ 两次输入的密码不一致");
                        loading.value = false;
                        return;
                    }

                    const res = await axios.post('/api/canteen/register', payload);

                    if (res.data.success) {
                        alert("🎉 注册成功！请使用新账号登录");
                        isLogin.value = true;
                        form.value.password = '';
                        confirmPassword.value = '';
                    } else {
                        alert("❌ 注册失败：" + (res.data.message || "用户名已被占用"));
                    }
                }
            } catch (error) {
                console.error("网络或服务器异常:", error);
                alert("🚨 无法连接到服务器，请稍后再试");
            } finally {
                loading.value = false;
            }
        };

        return { isLogin, form, confirmPassword, handleSubmit, loading, rememberMe };
    }
}).mount('#app');
