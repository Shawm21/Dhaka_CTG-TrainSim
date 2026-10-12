package com.train.game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RouteData {
    public static final double PX_PER_KM = 120.0;

    // 5 Stations on the Dhaka -> Dhaka Airport -> Gazipur -> Comilla -> Chittagong Line
    public static final List<Station> STATIONS = new ArrayList<>();

    public static class ScheduleEntry {
        public final double arr;
        public final double dep;

        public ScheduleEntry(double arr, double dep) {
            this.arr = arr;
            this.dep = dep;
        }
    }

    public static final List<ScheduleEntry> SCHEDULE = new ArrayList<>();
    public static double HOURS_PER_S = 0.005;

    // Bridges along the route (Turag River bridge at Gazipur, Meghna & Gomti Bridges)
    public static final double TURAG_BRIDGE_START = 32 * PX_PER_KM;
    public static final double TURAG_BRIDGE_END = 35 * PX_PER_KM;

    public static final double MEGHNA_BRIDGE_START = 90 * PX_PER_KM;
    public static final double MEGHNA_BRIDGE_END = 94 * PX_PER_KM;

    static {
        STATIONS.add(new Station(
                0, "KAMALAPUR", "কমলাপুর", "DHK", 0, 0 * PX_PER_KM,
                -330, 650, "umbrella", "red",
                18, 0, 14, Arrays.asList("tea_stall", "fruit_vendor")
        ));
        STATIONS.add(new Station(
                1, "DHAKA AIRPORT", "ঢাকা বিমানবন্দর", "DAA", 21, 21 * PX_PER_KM,
                -260, 480, "canopy", "blue",
                14, 5, 10, Arrays.asList("luggage_porter")
        ));
        STATIONS.add(new Station(
                2, "GAZIPUR", "গাজীপুর", "GZP", 36, 36 * PX_PER_KM,
                -220, 420, "shelter", "green",
                10, 6, 8, Arrays.asList("tea_stall")
        ));
        STATIONS.add(new Station(
                3, "COMILLA", "কুমিল্লা", "CO", 173, 173 * PX_PER_KM,
                -240, 450, "shelter", "red",
                12, 8, 7, Arrays.asList("chests", "tea_stall")
        ));
        STATIONS.add(new Station(
                4, "CHITTAGONG", "চট্টগ্রাম", "CTG", 321, 321 * PX_PER_KM,
                -300, 500, "canopy", "blue",
                0, 20, 5, Arrays.asList("chests")
        ));

        buildSchedule();
    }

    private static void buildSchedule() {
        SCHEDULE.clear();
        double firstDep = 32.0;
        double dwell = 16.0;
        double slack = 6.0;
        double schedSpeed = 84.0;

        SCHEDULE.add(new ScheduleEntry(0.0, firstDep));

        for (int i = 1; i < STATIONS.size(); i++) {
            double distLeg = STATIONS.get(i).distPx - STATIONS.get(i - 1).distPx;
            double runTime = distLeg / schedSpeed + slack;
            double arr = SCHEDULE.get(i - 1).dep + runTime;
            double dep = arr + dwell;
            SCHEDULE.add(new ScheduleEntry(arr, dep));
        }

        double totalTripTime = SCHEDULE.get(STATIONS.size() - 1).arr;
        double hourStart = 5.6;  // 5:36 AM departure from Kamalapur
        double hourEnd = 13.5;   // 1:30 PM arrival at Chittagong
        HOURS_PER_S = (hourEnd - hourStart) / Math.max(1.0, totalTripTime);
    }

    public static double getSpeedLimitAt(double distPx) {
        double limit = 100.0;

        // Bridge slow zones
        if ((distPx > TURAG_BRIDGE_START - 200 && distPx < TURAG_BRIDGE_END + 200) ||
            (distPx > MEGHNA_BRIDGE_START - 200 && distPx < MEGHNA_BRIDGE_END + 200)) {
            limit = Math.min(limit, 60.0);
        }

        // Chittagong hill approach
        if (distPx > 240 * PX_PER_KM) {
            limit = Math.min(limit, 80.0);
        }

        // Station approach slow zones
        for (Station st : STATIONS) {
            if (distPx > st.distPx - 280 && distPx < st.distPx + 160) {
                limit = Math.min(limit, 60.0);
                break;
            }
        }
        return limit;
    }
}
