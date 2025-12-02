package com.example.controller;

import com.example.common.lang.Result;
import com.example.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

// 测试类在http://localhost:8081/test中查看数据库返回的数据
@RestController
public class TestController
{
    @Autowired
    SysUserService sysUserService;

    @GetMapping("/test")
    public Result test()
    {
        return Result.succ(sysUserService.list());
    }
}
