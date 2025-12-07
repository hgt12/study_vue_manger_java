package com.example.security;

import cn.hutool.core.util.StrUtil;
import com.example.entity.SysUser;
import com.example.service.SysUserService;
import com.example.service.impl.UserDetailServiceImpl;
import com.example.util.JwtUtils;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

//Jwt过滤器
public class JwtAuthenticationFilter extends BasicAuthenticationFilter
{
    private JwtUtils jwtUtils;

    @Autowired
    UserDetailServiceImpl userDetailsService;

    @Autowired
    SysUserService sysUserService;

    public JwtAuthenticationFilter(AuthenticationManager authenticationManager, JwtUtils jwtUtils) {
        super(authenticationManager);
        this.jwtUtils = jwtUtils;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException
    {
        // 优先从Header中获取token，如果没有则从URL参数中获取
        String jwt = request.getHeader(jwtUtils.getHeader());
        
        // 如果Header中没有，尝试从URL参数中获取
        if (StrUtil.isBlankOrUndefined(jwt)) {
            jwt = request.getParameter(jwtUtils.getHeader());
        }

        // 如果仍然没有token，直接放行（让后续的认证机制处理）
        if (StrUtil.isBlankOrUndefined(jwt)){
            chain.doFilter(request, response);
            return;
        }

        // 解析token
        Claims claims = jwtUtils.getClaimByToken(jwt);
        if (claims == null){
            chain.doFilter(request, response);
            return;
        }

        // 检查token是否过期
        if (JwtUtils.isTokenExpired(claims)) {
            chain.doFilter(request, response);
            return;
        }

        // 将JWT里的用户名 转换成 Spring Security能识别的通行证
        String username = claims.getSubject();

        SysUser sysUser = sysUserService.getByUserName(username);

        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(username, null, userDetailsService.getUserAuthority(sysUser.getId()));
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        chain.doFilter(request, response);
    }
}
