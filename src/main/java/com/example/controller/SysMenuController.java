package com.example.controller;


import cn.hutool.core.map.MapUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.common.dto.SysMenuDto;
import com.example.common.lang.Result;
import com.example.entity.SysMenu;
import com.example.entity.SysRoleMenu;
import com.example.entity.SysUser;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author teacher
 * @since 2025-11-20
 */
@RestController
@RequestMapping("/sys/menu")
public class SysMenuController extends BaseController {
    @GetMapping("nav")
    public Result nav(Principal principal)
    {
        SysUser sysUser = sysUserService.getByUserName(principal.getName());
        String authorityInfo = sysUserService.getUserAuthorityInfo(sysUser.getId());
        String [] authorityInfoArray = StringUtils.tokenizeToStringArray(authorityInfo, ",");
        List<SysMenuDto> navs = sysMenuService.getCurrentUserNav();
        return Result.succ(MapUtil.builder()
                .put("authoritys", authorityInfoArray)
                .put("navs", navs)
                .map());
    }

    //根据id获取某条菜单数据
    @GetMapping("/info/{id}")
    @PreAuthorize("hasAuthority('sys:menu:list')")
    public Result info( @PathVariable (name = "id") Long id)
    {
        return Result.succ(sysMenuService.getById(id));
    }

    //查询所有菜单数据
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('sys:menu:list')")
    public Result list()
    {
        List<SysMenu> menus = sysMenuService.tree();
        return Result.succ(menus);
    }

    //新增菜单
    @PostMapping("/save")
    @PreAuthorize("hasAuthority('sys:menu:save')")
    public Result save(@Validated @RequestBody SysMenu sysMenu)
    {
        sysMenu.setCreated(LocalDateTime.now());
        sysMenuService.save(sysMenu);
        return Result.succ(sysMenu);
    }

    //菜单更新
    @PostMapping("/update")
    @PreAuthorize("hasAuthority('sys:menu:update')")
    public Result update(@Validated @RequestBody SysMenu sysMenu)
    {
        sysMenu.setUpdated(LocalDateTime.now());
        sysMenuService.updateById(sysMenu);
        //清空用户所有与菜单相关的权限缓存
        sysUserService.clearUserAuthorityByMenuId(sysMenu.getId());
        return Result.succ(sysMenu);
    }

    //删除菜单(根据id)
    @PostMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('sys:menu:delete')")
    public Result delete(@PathVariable("id") Long id)
    {
        int count = sysMenuService.count(new QueryWrapper<SysMenu>().eq("parent_id", id));
        if (count > 0){
            return Result.fail("请先删除子菜单");
        }
        //清除所有与菜单相关的权限缓存
        sysUserService.clearUserAuthorityByMenuId(id);
        sysMenuService.removeById(id);
        //同步删除中间关联表
        sysRoleMenuService.remove(new QueryWrapper<SysRoleMenu>().eq("menu_id", id));
        return Result.succ("");
    }
}
