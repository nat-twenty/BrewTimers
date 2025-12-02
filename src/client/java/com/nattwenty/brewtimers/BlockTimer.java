package com.nattwenty.brewtimers;

public class BlockTimer {
    private String timerName;
    private Long timerUTC;

    public BlockTimer(String timerName, Long timerUTC) {
        this.timerName = timerName;
        this.timerUTC = timerUTC;
    }

    public String getTimerName() {
        return timerName;
    }

    public void setTimerName(String timerName) {
        this.timerName = timerName;
    }

    public Long getTimerUTC() {
        return timerUTC;
    }

    public void setTimerUTC(Long timerUTC) {
        this.timerUTC = timerUTC;
    }
}
