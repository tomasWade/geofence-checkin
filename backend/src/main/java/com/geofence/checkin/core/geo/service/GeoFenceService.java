package com.geofence.checkin.core.geo.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.geofence.checkin.common.exception.BusinessException;
import com.geofence.checkin.core.auth.security.CurrentUser;
import com.geofence.checkin.core.geo.dto.FenceSaveReq;
import com.geofence.checkin.core.geo.entity.GeoFence;
import com.geofence.checkin.core.geo.mapper.GeoFenceMapper;
import com.geofence.checkin.core.system.audit.Audit;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 围栏管理：分页/启停/逻辑删；新增时防止客户端指定主键。
 */
@Service
@RequiredArgsConstructor
public class GeoFenceService {

    private final GeoFenceMapper fenceMapper;

    public Page<GeoFence> page(long page, long size, String keyword, Integer status) {
        return fenceMapper.selectPage(Page.of(page, size),
                new LambdaQueryWrapper<GeoFence>()
                        .and(keyword != null && !keyword.isBlank(),
                                w -> w.like(GeoFence::getName, keyword))
                        .eq(status != null, GeoFence::getStatus, status)
                        .orderByDesc(GeoFence::getId));
    }

    public List<GeoFence> listEnabled() {
        return fenceMapper.selectList(new LambdaQueryWrapper<GeoFence>()
                .eq(GeoFence::getStatus, 1));
    }

    public GeoFence get(Long id) {
        GeoFence fence = fenceMapper.selectById(id);
        if (fence == null) {
            throw new BusinessException("围栏不存在");
        }
        return fence;
    }

    @Audit(action = "fence:create")
    public Long create(FenceSaveReq req) {
        validateRadius(req.getRadiusM());
        GeoFence fence = new GeoFence();
        BeanUtils.copyProperties(req, fence);
        fence.setId(null);
        fence.setStatus(req.getStatus() == null ? 1 : req.getStatus());
        fence.setFenceType("circle");
        fence.setDeleted(0);
        fence.setCreatedBy(CurrentUser.userId());
        fenceMapper.insert(fence);
        return fence.getId();
    }

    @Audit(action = "fence:update")
    public void update(FenceSaveReq req) {
        GeoFence fence = fenceMapper.selectById(req.getId());
        if (fence == null) {
            throw new BusinessException("围栏不存在");
        }
        validateRadius(req.getRadiusM());
        fence.setName(req.getName());
        fence.setLng(req.getLng());
        fence.setLat(req.getLat());
        fence.setRadiusM(req.getRadiusM());
        if (req.getStatus() != null) {
            fence.setStatus(req.getStatus());
        }
        fenceMapper.updateById(fence);
    }

    @Audit(action = "fence:delete")
    public void delete(Long id) {
        fenceMapper.deleteById(id);
    }

    private void validateRadius(Integer radiusM) {
        if (radiusM != null && radiusM < 30) {
            throw new BusinessException("围栏半径过小（最小 30 米）");
        }
    }
}
