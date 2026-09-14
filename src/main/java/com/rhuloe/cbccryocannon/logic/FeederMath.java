package com.rhuloe.cbccryocannon.logic;

public final class FeederMath {
    private FeederMath() {}

    public static int reloadTicks(int configuredTicks, int rpm, int minimumRpm, boolean rpmAdjustsTime) {
        if (rpm < minimumRpm) return Integer.MAX_VALUE;
        if (!rpmAdjustsTime) return configuredTicks;
        return Math.max(1, (int) Math.ceil(configuredTicks * (double) minimumRpm / rpm));
    }

    public static boolean canLoad(int fuel, int perShot) {
        return fuel >= perShot;
    }
}