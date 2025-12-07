package com.example.common.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

//在这个类中选择有哪些后端字段需要传递给前端的
@Data
public class SysMenuDto implements Serializable
{
    private Long id;

    private String name;

    private String title;

    private String icon;

    private String path;

    private String component;

    private List<SysMenuDto> children = new ArrayList<>();

}
