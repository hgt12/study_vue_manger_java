package com.example.service.impl;

import com.example.entity.SysUser;
import com.example.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

//安全认证层，桥接业务数据与Spring Security，提供用户认证所需的标准化安全数据。
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
    public List<GrantedAuthority> getUserAuthority(Long userId)
    {
        //将逗号分隔的权限字符串（如 "ROLE_admin,sys:user:list"）转换为 Spring Security 的标准权限对象列表，用于后续的权限校验。
        //例如"ROLE_admin,sys:user:list“  [ROLE_admin, sys:user:list]
        String authority = sysUserService.getUserAuthorityInfo(userId);
        return AuthorityUtils.commaSeparatedStringToAuthorityList(authority);
    }
}
