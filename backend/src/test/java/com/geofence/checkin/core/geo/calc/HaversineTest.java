package com.geofence.checkin.core.geo.calc;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HaversineTest {

    @Test
    void samePointIsZero() {
        assertEquals(0.0, Haversine.distanceMeters(104.06, 30.66, 104.06, 30.66), 1e-9);
    }

    @Test
    void knownDistanceTianfuSquareToChunxiRoad() {
        double d = Haversine.distanceMeters(104.065735, 30.657008, 104.081702, 30.659823);
        assertTrue(d > 1400 && d < 1600, "actual=" + d);
    }

    @Test
    void oneDegreeLatIsAbout111Km() {
        double d = Haversine.distanceMeters(104.0, 30.0, 104.0, 31.0);
        assertEquals(111_195, d, 1000);
    }

    @Test
    void smallDistanceSanity() {
        double d = Haversine.distanceMeters(104.065735, 30.657008, 104.066842, 30.657350);
        assertTrue(d > 90 && d < 120, "actual=" + d);
    }
}
