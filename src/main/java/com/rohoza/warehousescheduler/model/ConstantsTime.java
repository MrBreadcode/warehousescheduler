package com.rohoza.warehousescheduler.model;

import java.time.Duration;
import java.time.LocalTime;

public class ConstantsTime {
    public static final Duration TIME_ERROR = Duration.ofMinutes(15);
    public static final LocalTime DAY_SHIFT_START = LocalTime.of(8, 0);
    public static final LocalTime DAY_SHIFT_END = LocalTime.of(20, 0);
    public static final LocalTime NIGHT_SHIFT_START = LocalTime.of(20, 0);
    public static final LocalTime NIGHT_SHIFT_END = LocalTime.of(8, 0);
}
