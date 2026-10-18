package com.geofence.checkin.core.checkin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.geofence.checkin.common.exception.BusinessException;
import com.geofence.checkin.core.checkin.dto.RecordVO;
import com.geofence.checkin.core.checkin.entity.CheckinRecord;
import com.geofence.checkin.core.checkin.mapper.CheckinRecordMapper;
import com.geofence.checkin.core.geo.entity.GeoFence;
import com.geofence.checkin.core.geo.mapper.GeoFenceMapper;
import com.geofence.checkin.core.system.service.DataScopeService;
import com.geofence.checkin.core.system.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 打卡记录查询：我的今日/数据范围过滤的列表/单条详情（IDOR 校验）。
 */
@Service
@RequiredArgsConstructor
public class RecordQueryService {

    private final CheckinRecordMapper recordMapper;
    private final GeoFenceMapper fenceMapper;
    private final UserService userService;
    private final DataScopeService dataScopeService;

    public List<RecordVO> myToday(Long userId) {
        return toVO(recordMapper.selectList(new LambdaQueryWrapper<CheckinRecord>()
                .eq(CheckinRecord::getUserId, userId)
                .eq(CheckinRecord::getCheckinDate, LocalDate.now())
                .orderByAsc(CheckinRecord::getCheckinTime)));
    }

    public Page<RecordVO> page(long page, long size, LocalDate date, Long userId) {
        Set<Long> visible = dataScopeService.visibleUserIds();
        LambdaQueryWrapper<CheckinRecord> wrapper = new LambdaQueryWrapper<CheckinRecord>()
                .eq(date != null, CheckinRecord::getCheckinDate, date)
                .eq(userId != null, CheckinRecord::getUserId, userId)
                .in(visible != null, CheckinRecord::getUserId, visible)
                .orderByDesc(CheckinRecord::getCheckinTime);
        Page<CheckinRecord> result = recordMapper.selectPage(Page.of(page, size), wrapper);
        Page<RecordVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(toVO(result.getRecords()));
        return voPage;
    }

    public RecordVO getDetail(Long id) {
        CheckinRecord record = recordMapper.selectById(id);
        if (record == null) {
            throw new BusinessException("记录不存在");
        }
        dataScopeService.checkAccess(record.getUserId());
        return toVO(List.of(record)).get(0);
    }

    public List<CheckinRecord> listByUserAndDate(Long userId, LocalDate date) {
        return recordMapper.selectList(new LambdaQueryWrapper<CheckinRecord>()
                .eq(CheckinRecord::getUserId, userId)
                .eq(CheckinRecord::getCheckinDate, date)
                .orderByAsc(CheckinRecord::getCheckinTime));
    }

    private List<RecordVO> toVO(List<CheckinRecord> records) {
        if (records.isEmpty()) {
            return List.of();
        }
        Set<Long> userIds = new HashSet<>();
        Set<Long> fenceIds = new HashSet<>();
        records.forEach(r -> {
            userIds.add(r.getUserId());
            if (r.getManualBy() != null) {
                userIds.add(r.getManualBy());
            }
            fenceIds.add(r.getFenceId());
        });
        Map<Long, String> userNames = userService.nameMap(new ArrayList<>(userIds));
        Map<Long, String> fenceNames = new HashMap<>();
        for (GeoFence f : fenceMapper.selectBatchIds(fenceIds)) {
            fenceNames.put(f.getId(), f.getName());
        }

        return records.stream().map(r -> {
            RecordVO vo = new RecordVO();
            vo.setId(r.getId());
            vo.setUserId(r.getUserId());
            vo.setUserName(userNames.get(r.getUserId()));
            vo.setFenceId(r.getFenceId());
            vo.setFenceName(fenceNames.getOrDefault(r.getFenceId(), "已删除围栏#" + r.getFenceId()));
            vo.setCheckinDate(r.getCheckinDate());
            vo.setCheckinTime(r.getCheckinTime());
            vo.setClientTime(r.getClientTime());
            vo.setDistanceM(r.getDistanceM());
            vo.setAccuracyM(r.getAccuracyM());
            vo.setLng(r.getLng());
            vo.setLat(r.getLat());
            vo.setSource(r.getSource());
            vo.setSuspicious(r.getSuspicious());
            vo.setRemark(r.getRemark());
            vo.setManualBy(r.getManualBy());
            vo.setManualByName(r.getManualBy() != null ? userNames.get(r.getManualBy()) : null);
            return vo;
        }).toList();
    }
}
