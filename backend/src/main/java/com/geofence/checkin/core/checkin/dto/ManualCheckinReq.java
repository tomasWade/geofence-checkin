package com.geofence.checkin.core.checkin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 代打卡请求体（备注必填）。
 */
@Data
public class ManualCheckinReq {
    @NotNull
    private Long userId;
    @NotNull
    private Long fenceId;
    @NotNull
    private LocalDateTime checkinTime;
    @NotBlank
    @Size(max = 500)
    private String remark;
}
