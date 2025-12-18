package com.example.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.service.*;
import com.example.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.ServletRequestUtils;

import javax.servlet.http.HttpServletRequest;

public class BaseController
{
    @Autowired
    HttpServletRequest request;

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    SysUserService sysUserService;

    @Autowired
    SysRoleService sysRoleService;

    @Autowired
    SysMenuService sysMenuService;

    @Autowired
    SysUserRoleService sysUserRoleService;

    @Autowired
    SysRoleMenuService sysRoleMenuService;

    public Page getPage()
    {
        int current = ServletRequestUtils.getIntParameter(request, "current", 1);
        int pageSize = ServletRequestUtils.getIntParameter(request, "pageSize", 10);
        return new Page(current, pageSize);
    }
}
