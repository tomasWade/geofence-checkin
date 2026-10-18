package com.geofence.checkin.core.system.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 用户新增请求体（密码只写不读）。
 */
@Data
public class UserCreateReq {
    @NotBlank
    @Size(max = 50)
    private String username;
    @NotBlank
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;
    @Size(max = 50)
    private String realName;
    @Size(max = 20)
    private String mobile;
    private Long roleId;
}
