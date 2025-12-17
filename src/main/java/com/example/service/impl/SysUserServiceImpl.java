package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.entity.SysMenu;
import com.example.entity.SysRole;
import com.example.entity.SysUser;
import com.example.mapper.SysUserMapper;
import com.example.service.SysMenuService;
import com.example.service.SysRoleService;
import com.example.service.SysUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author teacher
 * @since 2025-11-20
 */

//业务服务层,处理用户业务逻辑（如查询用户、角色、菜单权限等），与数据库直接交互。
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

    @Override
    public String getUserAuthorityInfo(Long userId)
    {
        SysUser sysUser = sysUserMapper.selectById(userId);

        String authority = "";

        // 临时禁用缓存进行调试
        System.out.println("开始获取用户 " + sysUser.getUsername() + " 的权限信息");
        if (false && redisUtil.hasKey("GranteAuthority:" + sysUser.getUsername())) {
            authority = (String) redisUtil.get("GranteAuthority:" + sysUser.getUsername());
            System.out.println("从Redis缓存中获取权限: " + authority);
        }else {
            System.out.println("从数据库重新查询权限");
            //获取角色信息
            List<SysRole> roles = sysRoleService.list(new QueryWrapper<SysRole>()
                    .inSql("id", "select role_id from sys_user_role where user_id = " + userId));

            if (roles != null && roles.size() > 0) {
                String roleCode = roles.stream().map(r -> "ROLE_" + r.getCode())
                        .collect(Collectors.joining(","));
                authority = roleCode.concat(",");
            }
            //获取菜单权限操作
            List<Long> menuId = sysUserMapper.getNavMenuIds(userId);
            System.out.println("用户ID " + userId + " 的菜单ID列表: " + menuId);
            System.out.println("菜单ID列表是否为空: " + (menuId == null || menuId.isEmpty()));
            
            if (menuId != null && !menuId.isEmpty()) {
                System.out.println("开始查询菜单，ID列表: " + menuId);
                List<SysMenu> menus = sysMenuService.listByIds(menuId);
                System.out.println("查询到的菜单数量: " + (menus != null ? menus.size() : 0));
                if (menus != null && !menus.isEmpty()) {
                    // 获取所有菜单的权限（perms字段），每个菜单的perms可能包含多个权限（逗号分隔）
                    String menuPersms = menus.stream()
                            .map(SysMenu::getPerms)
                            .filter(perms -> perms != null && !perms.trim().isEmpty())
                            .collect(Collectors.joining(","));
                    if (!menuPersms.isEmpty()) {
                        if (!authority.isEmpty() && !authority.endsWith(",")) {
                            authority = authority.concat(",");
                        }
                        authority = authority.concat(menuPersms);
                    }
                }
            }
            redisUtil.set("GranteAuthority:" + sysUser.getUsername(), authority,60*60);
        }

        return authority;
    }

    @Override
    public void clearUserAuthorityInfo(String username) {
        redisUtil.del("GranteAuthority:" + username);
    }

    @Override
    public void clearUserAuthorityByRoleId(Long roleId) {
        List<SysUser> sysUsers = this.list(new QueryWrapper<SysUser>()
                .inSql("id","select user_id from sys_user_role where role_id = " + roleId));

        sysUsers.forEach(user -> {
            this.clearUserAuthorityInfo(user.getUsername());
        });
    }

    @Override
    public void clearUserAuthorityByMenuId(Long menuId) {
        List<SysUser> sysUsers = sysUserMapper.listByMenuId(menuId);

        sysUsers.forEach(user -> {
            this.clearUserAuthorityInfo(user.getUsername());
        });
    }
}
