package com.example.service.impl;

import com.example.entity.SysUser;
import com.example.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@Service
public class UserDetailServiceImpl implements UserDetailsService
{
    @Autowired
    SysUserService sysUserService;

    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException
    {
        SysUser sysUser = sysUserService.getByUserName(s);
        if (sysUser == null) {
            throw new UsernameNotFoundException("用户名或密码不正确");
        }

        return new AccountUser(sysUser.getId(),sysUser.getUsername(),sysUser.getPassword(),getUserAuthority(sysUser.getId()));
    }

    //获取授权信息，包括角色，菜单权限
    private List<GrantedAuthority> getUserAuthority(Long userId)
    {
        return null;
    }
}
