package com.example.security;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.example.common.exception.CaptchaException;
import com.example.common.lang.Const;
import com.example.util.RedisUtil;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

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
                return; // 验证码验证失败时，直接返回，不继续执行后续过滤器
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

        // 先检查Redis中是否存在这个Hash key
        boolean hashExists = redisUtil.hasKey(Const.CAPTCHA_KEY);
        System.out.println("Redis中是否存在Hash Key '" + Const.CAPTCHA_KEY + "': " + hashExists);
        
        Object redis_code_obj = redisUtil.hget(Const.CAPTCHA_KEY, key);
        System.out.println("在Redis中提取出来的对象类型为：" + (redis_code_obj != null ? redis_code_obj.getClass().getName() : "null"));
        System.out.println("在Redis中提取出来的值为：" + redis_code_obj);
        
        if (redis_code_obj == null) {
            throw new CaptchaException("验证码已过期或不存在，请重新获取验证码");
        }
        
        String redis_code = redis_code_obj.toString();
        if (!code.equals(redis_code)){
            throw new CaptchaException("验证码错误");
        }
        redisUtil.hdel(Const.CAPTCHA_KEY,key);
    }
}
