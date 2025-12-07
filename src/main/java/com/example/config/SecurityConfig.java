package com.example.config;

import com.example.security.*;
import com.example.util.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

//该类的作用
//启用 Spring Security。
//定义哪些接口可以匿名访问（白名单）。
//其他接口必须认证。
//禁用 Session，适合前后端分离 + Token 认证场景。
//预留了异常处理和自定义过滤器的位置。
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter
{
    @Autowired
    LoginSuccessHandler loginSuccessHandler;

    @Autowired
    LoginFailureHandler loginFailureHandler;

    @Autowired
    CaptchaFilter captchaFilter;

    @Autowired
    JwtAccessDeniedHandler jwtAccessDeniedHandler;

    @Autowired
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Autowired
    private JwtUtils jwtUtils;

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() throws Exception {
        return new JwtAuthenticationFilter(authenticationManager(), jwtUtils);
    }

    @Bean
    BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Autowired
    UserDetailsService userDetailsService;

    @Autowired
    JwtLogoutSuccessHandler jwtLogoutSuccessHandler;

    //白名单,这里定义了无需认证即可访问的接口路径（白名单）。比如验证码接口、网站图标等。
    //注意：/login 不需要放在白名单中，因为 formLogin() 会自动处理 /login 请求
    private static final String[] AUTH_WHITELIST = {
            "/captcha",
            "/favicon.ico",
    };

    protected void configure(HttpSecurity http) throws Exception
    {
        http.cors()
                .and()
                .csrf().disable()
                //登录配置
                .formLogin()
                .loginProcessingUrl("/login")  // 明确指定登录处理URL
                .successHandler(loginSuccessHandler)
                .failureHandler(loginFailureHandler)

                //退出配置
                .and()
                .logout()
                .logoutSuccessHandler(jwtLogoutSuccessHandler)

                //禁用session
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)

                //配置拦截规则
                .and()
                .authorizeRequests()
                .antMatchers(AUTH_WHITELIST).permitAll()
                .anyRequest().authenticated()

                //配置自定义的过滤器

                //异常处理 - 配置认证异常和权限异常的处理
                .and()
                .exceptionHandling()
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                .accessDeniedHandler(jwtAccessDeniedHandler)

                .and()
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(captchaFilter, UsernamePasswordAuthenticationFilter.class)
        ;

    }

    //测试
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception
    {
//        auth.inMemoryAuthentication()
//                .withUser("root")
//                .password("{noop}admin")
//                .roles("ADMIN");
//                //{noop}表示明文密码

        auth.userDetailsService(userDetailsService)
                .passwordEncoder(bCryptPasswordEncoder());
    }
}
