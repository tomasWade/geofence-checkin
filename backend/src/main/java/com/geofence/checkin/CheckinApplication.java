package com.geofence.checkin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 围栏打卡系统启动类，扫描 MyBatis-Plus Mapper。
 */
@SpringBootApplication
@MapperScan("com.geofence.checkin.**.mapper")
public class CheckinApplication {
    public static void main(String[] args) {
        SpringApplication.run(CheckinApplication.class, args);
    }
}
