package com.example.security;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.example.commom.exception.CaptchaException;
import com.example.commom.lang.Const;
import com.example.util.RedisUtil;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.Console;
import java.io.IOException;

import static jdk.nashorn.internal.runtime.regexp.RegExpFactory.validate;

@Component
public class CaptchaFilter extends OncePerRequestFilter
{

    private final LoginFailureHandler loginFailureHandler;
    private final RedisUtil redisUtil;

    public CaptchaFilter(LoginFailureHandler loginFailureHandler, RedisUtil redisUtil) {
        this.loginFailureHandler = loginFailureHandler;
        this.redisUtil = redisUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest
            , HttpServletResponse httpServletResponse
            , FilterChain filterChain) throws ServletException, IOException {
        System.out.println("CaptchaFilter doFilterInternal.....");
        String url = httpServletRequest.getRequestURI();

        if ("/login".equals(url) && httpServletRequest.getMethod().equals("POST"))
        {
            try {
                validate(httpServletRequest);
            }catch (CaptchaException e) {
                loginFailureHandler.onAuthenticationFailure(httpServletRequest, httpServletResponse, e);
            }
        }
        filterChain.doFilter(httpServletRequest, httpServletResponse);
    }

    private void validate(HttpServletRequest httpServletRequest)
    {
        String code = httpServletRequest.getParameter("code");
        System.out.println("页面传递的验证码为： " + code);
        String key = httpServletRequest.getParameter("token");
        System.out.println("页面传递的key为： " + key);
        if (StringUtils.isBlank(code) || StringUtils.isBlank(key)){
            throw new CaptchaException("验证码错误");
        }

        if (!code.equals(redisUtil.hget(Const.CAPTCHA_KEY,key))){
            System.out.println("Redis中的key为：" + redisUtil.hget(Const.CAPTCHA_KEY,key));
            throw new CaptchaException("验证码错误");
        }
        redisUtil.hdel(Const.CAPTCHA_KEY,key);
    }
}
