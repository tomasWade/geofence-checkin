package com.geofence.checkin.core.geo.calc;

/**
 * Haversine 球面距离纯函数（地球半径取平均半径 6371008.8 米）。
 */
public final class Haversine {

    private static final double EARTH_RADIUS_M = 6_371_008.8;

    private Haversine() {
    }

    public static double distanceMeters(double lng1, double lat1, double lng2, double lat2) {
        double radLat1 = Math.toRadians(lat1);
        double radLat2 = Math.toRadians(lat2);
        double dLat = radLat2 - radLat1;
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(radLat1) * Math.cos(radLat2)
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_M * c;
    }
}
