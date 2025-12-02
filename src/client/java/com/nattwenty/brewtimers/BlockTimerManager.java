package com.nattwenty.brewtimers;

import net.minecraft.util.math.BlockPos;

import java.time.Instant;
import java.util.HashMap;

public class BlockTimerManager {
    private static BlockTimerManager instance;
    private static HashMap<BlockPos, BlockTimer> brewtimers_timers;

    private BlockTimerManager() {
        brewtimers_timers = new HashMap<>();
    }

    static {
        instance = new BlockTimerManager();
    }

    public static BlockTimerManager getInstance() {
        return instance;
    }

    public HashMap<BlockPos, BlockTimer> checkTimers() {
        HashMap<BlockPos, BlockTimer> hm = new HashMap<>();
        brewtimers_timers.forEach((pos,timer) -> {
            if (timer.getTimerUTC() <= Instant.now().getEpochSecond()) {
                hm.put(pos, timer);
            }
        });

        return hm;
    }

    public void addTimer(BlockPos pos, BlockTimer timer) {
        brewtimers_timers.put(pos, timer);
    }

    public void removeTimer(BlockPos pos) {
        brewtimers_timers.remove(pos);
    }

    public boolean containsKey(BlockPos pos) {
        return brewtimers_timers.containsKey(pos);
    }

    public void clear() {
        brewtimers_timers.clear();
    }

}
