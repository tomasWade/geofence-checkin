package com.geofence.checkin.core.system.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 用户编辑请求体（密码留空则不修改，只写不读）。
 */
@Data
public class UserUpdateReq {
    @NotNull
    private Long id;
    @Size(max = 50)
    private String realName;
    @Size(max = 20)
    private String mobile;
    private Integer status;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;
}
