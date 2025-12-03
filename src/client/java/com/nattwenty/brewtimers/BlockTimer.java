package com.nattwenty.brewtimers;

import java.time.Instant;

public class BlockTimer {
    private final String timerName;
    private long startUTC;
    private Long timerUTC;
    private boolean elapsed;

    public BlockTimer(String timerName, Long timerUTC) {
        this.timerName = timerName;
        this.timerUTC = timerUTC;
        this.startUTC = Instant.now().getEpochSecond();
        this.elapsed = false;
    }

    public String getTimerName() {
        return timerName;
    }

    public long getStartUTC() {return startUTC;}

    public Long getTimerUTC() {
        return timerUTC;
    }

    public void setTimerUTC(Long timerUTC) {
        this.timerUTC = timerUTC;
        this.startUTC = Instant.now().getEpochSecond();
        this.elapsed = true;
    }

    public boolean isElapsed() {return elapsed;}
}
