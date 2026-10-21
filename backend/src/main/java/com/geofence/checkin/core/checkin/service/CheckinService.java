package com.geofence.checkin.core.checkin.service;

import com.geofence.checkin.core.checkin.dto.CheckinReq;
import com.geofence.checkin.core.checkin.dto.CheckinResultVO;

/**
 * 打卡服务接口：core 对外契约，判定与落账一律在服务端。
 */
public interface CheckinService {

    CheckinResultVO doCheckin(Long userId, CheckinReq req);
}
