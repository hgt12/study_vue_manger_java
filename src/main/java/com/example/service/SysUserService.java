package com.example.service;

import com.example.entity.SysUser;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author teacher
 * @since 2025-11-20
 */
public interface SysUserService extends IService<SysUser> {

    SysUser getByUserName(String userName);

    String getUserAuthorityInfo(Long userId);//通过userId查询用户权限
}
