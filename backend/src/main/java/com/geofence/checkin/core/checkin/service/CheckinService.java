package com.geofence.checkin.core.checkin.service;

import com.geofence.checkin.core.checkin.dto.CheckinReq;
import com.geofence.checkin.core.checkin.dto.CheckinResultVO;

/**
 * 打卡服务接口：mock 与真实实现的双轨契约（接口签名不变切换）。
 */
public interface CheckinService {

    CheckinResultVO doCheckin(Long userId, CheckinReq req);
}
