package com.rhuloe.cbccryocannon.logic;

public final class CannonMath {
    private CannonMath() {}

    public static double velocityMultiplier(int chambers) {
        return switch (Math.clamp(chambers, 2, 4)) {
            case 3 -> 1.2D;
            case 4 -> 1.4D;
            default -> 1.0D;
        };
    }

    public static double spreadDegrees(int barrels) {
        return switch (Math.clamp(barrels, 2, 5)) {
            case 3 -> 2.1667D;
            case 4 -> 1.3333D;
            case 5 -> 0.5D;
            default -> 3.0D;
        };
    }
}