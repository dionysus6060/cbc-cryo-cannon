package com.rhuloe.cbccryocannon.logic;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FeederMathTest {
    @Test void fixedRateRemainsFixed() {
        assertEquals(120, FeederMath.reloadTicks(120, 64, 32, false));
        assertEquals(Integer.MAX_VALUE, FeederMath.reloadTicks(120, 31, 32, false));
        assertTrue(FeederMath.canLoad(1000, 1000));
    }
}