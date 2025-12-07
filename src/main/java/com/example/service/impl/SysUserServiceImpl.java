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
    public String getUserAuthorityInfo(Long userId) {

        String authority = "";
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
        if (menuId != null && menuId.size() > 0) {
            List<SysMenu> menus = sysMenuService.listByIds(menuId);
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
        return authority;
    }
}
