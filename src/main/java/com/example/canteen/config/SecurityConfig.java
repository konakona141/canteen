package com.example.canteen.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // 1. 必须禁用 CSRF，否则所有的 POST 请求（如注册、登录、下单）都会被 403 拒绝
        http.csrf().disable()
                .cors().and() // 允许跨域
                .authorizeRequests()
                // 2. 静态资源放行：确保能看到 HTML 和 JS 文件
                .antMatchers("/", "/index.html", "/login.html", "/admin.html").permitAll()
                .antMatchers("/js/**", "/css/**", "/img/**").permitAll()

                // 3. 用户端 API 放行：下单(/order)和菜单(/menu)必须允许访问
                .antMatchers("/api/canteen/login", "/api/canteen/register").permitAll()
                .antMatchers("/api/canteen/menu/**").permitAll()
                .antMatchers("/api/canteen/**").permitAll()
                .antMatchers("/api/canteen/order").permitAll() // 关键：解决下单 403

                // 4. 管理端 API 放行：开发阶段先放通，方便看大屏
                .antMatchers("/api/canteen/admin/**").permitAll()
                .antMatchers("/api/canteen/common/upload").permitAll()

                // 放通静态图片资源路径
                .antMatchers("/uploads/**").permitAll()
                .anyRequest().authenticated()
                .and()
                // 5. 禁用默认登录页，防止跳到 Spring 自带的 login 面板
                .formLogin().disable()
                .httpBasic().disable();
    }
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}