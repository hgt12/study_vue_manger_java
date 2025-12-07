package com.example.service.impl;

import cn.hutool.json.JSONUtil;
import com.example.common.dto.SysMenuDto;
import com.example.entity.SysMenu;
import com.example.entity.SysUser;
import com.example.mapper.SysMenuMapper;
import com.example.mapper.SysUserMapper;
import com.example.service.SysMenuService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author teacher
 * @since 2025-11-20
 */
@Service
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements SysMenuService {

    @Autowired
    SysUserService sysUserService;

    @Autowired
    SysUserMapper sysUserMapper;

    @Override
    public List<SysMenuDto> getCurrentUserNav()
    {
        String username = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        SysUser sysUser = sysUserService.getByUserName(username);
        List<Long> menuIds = sysUserMapper.getNavMenuIds(sysUser.getId());
        List<SysMenu> menus = this.listByIds(menuIds);
        //转换成树状结构
        List<SysMenu> menuTree = buildTreeMenu(menus);
        //实体转换成DTO
        return convert(menuTree);
    }

    private List<SysMenu> buildTreeMenu(List<SysMenu> menus)
    {
        List<SysMenu> finalMenus = new ArrayList<>();

        for (SysMenu menu : menus){
            for (SysMenu child : menus){
                if (menu.getId().equals(child.getParentId())){
                    menu.getChildren().add(child);
                }
            }
            if (menu.getParentId() == 0L){
                finalMenus.add(menu);
            }
        }

        System.out.println("finalMenus: " + JSONUtil.toJsonStr(finalMenus));
        return finalMenus;
    }

    private List<SysMenuDto> convert(List<SysMenu> menuTree)
    {
        List<SysMenuDto> menuDtos = new ArrayList<>();
        menuTree.forEach(menu -> {
            SysMenuDto dto = new SysMenuDto();
            dto.setId(menu.getId());
            dto.setName(menu.getPerms());
            dto.setTitle(menu.getName());
            dto.setIcon(menu.getComponent());
            dto.setPath(menu.getPath());
            if (menu.getChildren().size() > 0){
                dto.setChildren(convert(menu.getChildren()));
            }
            menuDtos.add(dto);
        });
        return menuDtos;
    }

}
