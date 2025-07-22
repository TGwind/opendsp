package io.github.javagossip.opendsp.model;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import java.io.Serializable;
import java.time.LocalDateTime;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 系统权限表（支持菜单/按钮/API） 实体类。
 *
 * @author weiping wang
 * @since 2025-04-09
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("sys_permission")
public class SysPermission implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 权限唯一标识
     */
    @Id(keyType = KeyType.Auto)
    private Integer id;

    /**
     * 权限名称（如：用户管理）
     */
    private String name;

    /**
     * 权限类型（1=菜单 2=按钮 3=API）
     */
    private Integer type;

    /**
     * 权限描述
     */
    private String remark;

    /**
     * 权限标识（如 user:add, GET:/api/users）
     */
    private String key;

    /**
     * 状态（0=禁用 1=启用）
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 最后更新时间
     */
    private LocalDateTime updateTime;

}
