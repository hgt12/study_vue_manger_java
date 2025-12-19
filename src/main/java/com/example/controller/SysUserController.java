package com.example.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.dto.PassDto;
import com.example.common.lang.Const;
import com.example.common.lang.Result;
import com.example.entity.SysRole;
import com.example.entity.SysUser;
import com.example.entity.SysUserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.sound.sampled.Control;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
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
@RequestMapping("/sys/user")
public class SysUserController extends BaseController {

    @GetMapping("/info/{id}")
    @PreAuthorize("hasAnyAuthority('sys:user:list')")
    public Result info(@PathVariable("id") Long id)
    {
        SysUser sysUser = sysUserService.getById(id);
        Assert.notNull(sysUser, "找不到管理员！");
        List<SysRole> roles = sysRoleService.listRolesByUserId(id);
        sysUser.setSysRoles(roles);
        return Result.succ(sysUser);
    }

    @GetMapping("/list")
    @PreAuthorize("hasAnyAuthority('sys:user:list')")
    public Result list(String username)
    {
        Page<SysUser> pageData = sysUserService.page(getPage(),
                new QueryWrapper<SysUser>().like(StringUtils.isNotBlank(username), "username", username));

        pageData.getRecords().forEach(user -> {
            user.setSysRoles(sysRoleService.listRolesByUserId(user.getId()));
        });
        return Result.succ(pageData);
    }

    //添加用户功能
    @Autowired
    BCryptPasswordEncoder passwordEncoder;

    @PostMapping ("/save")
    @PreAuthorize("hasAnyAuthority('sys:user:save')")
    public Result save(@Validated @RequestBody SysUser sysUser)
    {
        sysUser.setCreated(LocalDateTime.now().now());
        if (sysUser.getStatu() == 1) {
            sysUser.setStatu(Const.STATUS_ON);
        }else {
            sysUser.setStatu(Const.STATUS_OFF);
        }

        // 如果密码为空，设置默认密码
        String rawPassword = StringUtils.isNotBlank(sysUser.getPassword()) ? sysUser.getPassword() : Const.DEFULT_PASSWORD;
        String password = passwordEncoder.encode(rawPassword);
        sysUser.setPassword(password);
        sysUser.setAvatar(Const.DEFULT_AVATAR);
        sysUserService.save(sysUser);
        return Result.succ(sysUser);
    }

    //编辑更新功能
    @PostMapping ("/update")
    @PreAuthorize("hasAnyAuthority('sys:user:update')")
    public Result update(@Validated @RequestBody  SysUser sysUser)
    {
        sysUser.setUpdated(LocalDateTime.now());
        sysUserService.updateById(sysUser);
        return Result.succ(sysUser);
    }

    //删除功能
    @PostMapping ("/delete")
    @PreAuthorize("hasAnyAuthority('sys:user:delete')")
    public Result delete(@RequestBody Long[] ids)
    {
        sysUserService.removeByIds(Arrays.asList(ids));
        //删除中间表
        sysUserRoleService.remove(new QueryWrapper<SysUserRole>().in("role_id",ids));
        return Result.succ("");
    }

    //分配角色功能
    @Transactional
    @PostMapping ("/role/{userId}")
    @PreAuthorize("hasAnyAuthority('sys:user:role')")
    public Result rolePerm(@PathVariable("userId") Long userId, @RequestBody Long[] roleIds)
    {
        List<SysUserRole> userRoles = new ArrayList<>();

        Arrays.stream(roleIds).forEach(roleId -> {
            SysUserRole sysUserRole = new SysUserRole();
            sysUserRole.setRoleId(roleId);
            sysUserRole.setUserId(userId);

            userRoles.add(sysUserRole);
        });

        sysUserRoleService.remove(new QueryWrapper<SysUserRole>().eq("user_id",userId));
        sysUserRoleService.saveBatch(userRoles);

        //删除缓存
        SysUser sysUser = sysUserService.getById(userId);
        sysUserService.clearUserAuthorityByMenuId(sysUser.getId());
        return Result.succ("");
    }

    //重置密码功能
    @PostMapping ("/repass")
    @PreAuthorize("hasAnyAuthority('sys:user:repass')")
    public Result repass(@RequestBody Long userId)
    {
        SysUser sysUser = sysUserService.getById(userId);

        sysUser.setPassword(passwordEncoder.encode(Const.DEFULT_PASSWORD));
        sysUser.setUpdated(LocalDateTime.now());

        sysUserService.updateById(sysUser);
        return Result.succ("");
    }

    //更新密码功能
    @PostMapping("/updatePass")
    public Result updatePass(@Validated @RequestBody PassDto passDto, Principal principal)
    {
        SysUser sysUser = sysUserService.getByUserName(principal.getName());

        boolean matches = passwordEncoder.matches(passDto.getCurrentPass(), sysUser.getPassword());
        if (!matches){
            return Result.fail("旧密码不正确");
        }

        sysUser.setPassword(passwordEncoder.encode(passDto.getPassword()));
        sysUser.setUpdated(LocalDateTime.now());

        sysUserService.updateById(sysUser);
        return Result.succ("");
    }

}
