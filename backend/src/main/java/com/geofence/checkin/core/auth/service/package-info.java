/**
 * 认证服务：登录（失败双维度限速锁定）、refresh token 原子轮换（Redis getAndDelete 防重放）、登出吊销。
 */
package com.geofence.checkin.core.auth.service;
