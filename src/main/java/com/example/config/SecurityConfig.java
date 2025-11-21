package com.example.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;

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

    //白名单,这里定义了无需认证即可访问的接口路径（白名单）。比如登录接口、验证码接口、网站图标等。
    private static final String[] AUTH_WHITELIST = {
            "/login",
            "/logout",
            "/captcha",
            "/favicon.ico",
    };

    protected void configure(HttpSecurity http) throws Exception
    {
        http.csrf().and().csrf().disable()
                //登录配置
                .formLogin()

                //禁用session
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)

                //配置拦截规则
                .and()
                .authorizeRequests()
                .antMatchers(AUTH_WHITELIST).permitAll()
                .anyRequest().authenticated()

                //异常处理


                //配置自定义的过滤器
        ;


    }
}
