package com.geofence.checkin.biz.rule.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 规则新增/编辑请求体（含绑定 ID 列表）。
 */
@Data
public class RuleSaveReq {
    private Long id;
    @NotBlank
    @Size(max = 100)
    private String name;
    @NotNull
    private Long fenceId;
    @NotNull
    @Min(0)
    @Max(86399)
    private Integer workStartSec;
    @NotNull
    @Min(1)
    @Max(86399)
    private Integer workEndSec;
    @Min(0)
    private Integer lateToleranceSec;
    @Min(1)
    @Max(127)
    private Integer effectiveDays;
    private String bindType;
    private Integer status;
    private List<Long> roleIds;
    private List<Long> groupIds;
    private List<Long> userIds;
}
