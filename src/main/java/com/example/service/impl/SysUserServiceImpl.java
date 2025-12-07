package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.entity.SysUser;
import com.example.mapper.SysUserMapper;
import com.example.service.SysMenuService;
import com.example.service.SysRoleService;
import com.example.service.SysUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author teacher
 * @since 2025-11-20
 */
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService
{
    @Autowired
    SysRoleService sysRoleService;

    @Autowired
    SysUserMapper sysUserMapper;

    @Autowired
    SysMenuService sysMenuService;

    @Autowired
    RedisUtil redisUtil;

    @Override
    public SysUser getByUserName(String userName) {
        return getOne(new QueryWrapper<SysUser>().eq("username", userName));
    }
}
