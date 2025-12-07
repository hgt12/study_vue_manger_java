package com.example.controller;

import com.example.common.lang.Result;
import com.example.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.DeprecatedConfigurationProperty;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

// 测试类在http://localhost:8081/test中查看数据库返回的数据
@RestController
public class TestController
{
    @Autowired
    SysUserService sysUserService;

    @Autowired
    BCryptPasswordEncoder bCryptPasswordEncoder;

    @GetMapping("/test")
    @PreAuthorize("hasRole('admin')")//添加访问此接口的所有权限
    public Result test()
    {
        return Result.succ(sysUserService.list());
    }

    @GetMapping("/test/pass")
    @PreAuthorize("hasAnyAuthority('sys:user:list')")
    public Result pass()
    {
        String password = bCryptPasswordEncoder.encode("123456");
        boolean isMatch = bCryptPasswordEncoder.matches("123456",password);
        System.out.println("匹配结果: " + isMatch);
        return Result.succ(password);
    }
}
