package com.rhuloe.cbccryocannon.logic;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CannonMathTest {
    @Test void chamberScaleIsCapped() {
        assertEquals(1.0D, CannonMath.velocityMultiplier(2));
        assertEquals(1.2D, CannonMath.velocityMultiplier(3));
        assertEquals(1.4D, CannonMath.velocityMultiplier(99));
    }

    @Test void barrelSpreadIsCapped() {
        assertEquals(3.0D, CannonMath.spreadDegrees(2));
        assertEquals(0.5D, CannonMath.spreadDegrees(99));
    }
}