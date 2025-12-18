package com.example.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.List;

/**
 * <p>
 * 
 * </p>
 *
 * @author teacher
 * @since 2025-11-20
 */
@Data
public class SysRoleMenu {

    @TableId(value = "id" , type =  IdType.AUTO)
    private Long id;

    private static final long serialVersionUID = 1L;

    private Long roleId;

    private Long menuId;


}
