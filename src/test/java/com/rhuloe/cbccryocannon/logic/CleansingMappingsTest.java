package com.rhuloe.cbccryocannon.logic;

import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CleansingMappingsTest {
    @Test void acceptsValidPairsOnly() {
        var map = CleansingMappings.parse(List.of("spore:a|minecraft:stone", "bad", "|x"));
        assertEquals("minecraft:stone", map.get("spore:a"));
        assertEquals(1, map.size());
    }
}