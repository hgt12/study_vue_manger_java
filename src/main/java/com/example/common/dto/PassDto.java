package com.example.common.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class PassDto implements Serializable {

    @NotNull(message = "新密码不能为空！")
    private String password;

    @NotNull(message = "旧密码不能为空！")
    private String currentPass;
}
