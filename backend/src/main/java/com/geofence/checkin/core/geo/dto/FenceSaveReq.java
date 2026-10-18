package com.geofence.checkin.core.geo.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 围栏新增/编辑请求体（经纬度与半径范围校验）。
 */
@Data
public class FenceSaveReq {
    private Long id;
    @NotBlank
    @Size(max = 100)
    private String name;
    @NotNull
    @DecimalMin(value = "-180.0")
    @DecimalMax(value = "180.0")
    private BigDecimal lng;
    @NotNull
    @DecimalMin(value = "-90.0")
    @DecimalMax(value = "90.0")
    private BigDecimal lat;
    @NotNull
    @Min(30)
    @Max(100000)
    private Integer radiusM;
    private Integer status;
}
