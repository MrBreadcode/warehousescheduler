package com.rohoza.warehousescheduler.model;

import jakarta.persistence.*;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Embeddable
public class Preferences {

    public enum ShiftType {
        DAY,
        NIGHT,
        BOTH
    }

    private ShiftType shiftPreference;

    @ElementCollection
    private List<DayOfWeek> workingDays;
    @ElementCollection
    private List<DayOfWeek> possibleDays;

    @ManyToMany
    @JoinTable(
            name = "worker_working_locations",
            joinColumns = @JoinColumn(name = "worker_id"),
            inverseJoinColumns = @JoinColumn(name = "location_id")
    )
    private List<Location> workingLocations;
    @ManyToMany
    @JoinTable(
            name = "worker_possible_locations",
            joinColumns = @JoinColumn(name = "worker_id"),
            inverseJoinColumns = @JoinColumn(name = "location_id")
    )
    private List<Location> possibleLocations;

    private boolean vacation;

    public Preferences() {}

    public void setDefault() {
        shiftPreference = ShiftType.DAY;
        workingDays = new ArrayList<>();
        workingDays.add(DayOfWeek.MONDAY);
        workingDays.add(DayOfWeek.TUESDAY);
        workingDays.add(DayOfWeek.WEDNESDAY);
        workingDays.add(DayOfWeek.THURSDAY);
        workingDays.add(DayOfWeek.FRIDAY);
        possibleDays = new ArrayList<>();
        possibleDays.add(DayOfWeek.SATURDAY);
        possibleDays.add(DayOfWeek.SUNDAY);
        vacation = false;
    }

    public void correct() {
        for (DayOfWeek day : workingDays) {
            possibleDays.remove(day);
        }
        for (Location location : workingLocations) {
            possibleLocations.remove(location);
        }
    }

    public ShiftType getShiftPreference() {
        return shiftPreference;
    }

    public void setShiftPreference(ShiftType shiftPreference) {
        this.shiftPreference = shiftPreference;
    }

    public List<Location> getWorkingLocations() {
        return workingLocations;
    }

    public void setWorkingLocations(List<Location> workingLocations) {
        this.workingLocations = workingLocations;
    }

    public List<Location> getPossibleLocations() {
        return possibleLocations;
    }

    public void setPossibleLocations(List<Location> possibleLocations) {
        this.possibleLocations = possibleLocations;
    }

    public List<DayOfWeek> getWorkingDays() {
        return workingDays;
    }

    public void setWorkingDays(List<DayOfWeek> workingDays) {
        this.workingDays = workingDays;
    }

    public List<DayOfWeek> getPossibleDays() {
        return possibleDays;
    }

    public void setPossibleDays(List<DayOfWeek> possibleDays) {
        this.possibleDays = possibleDays;
    }

    public boolean isVacation() {
        return vacation;
    }

    public void setVacation(boolean vacation) {
        this.vacation = vacation;
    }

    public boolean canWorkAtTime(LocalDateTime dateTime) {
        DayOfWeek dayOfWeek = dateTime.getDayOfWeek();
        if (!workingDays.contains(dayOfWeek) && !possibleDays.contains(dayOfWeek)) {
            return false;
        }

        LocalTime time = dateTime.toLocalTime();
        if (shiftPreference == ShiftType.DAY) {
            return !time.isBefore(ConstantsTime.DAY_SHIFT_START) && !time.isAfter(ConstantsTime.DAY_SHIFT_END);
        } else if (shiftPreference == ShiftType.NIGHT) {
            return time.isAfter(ConstantsTime.NIGHT_SHIFT_START) || time.isBefore(ConstantsTime.NIGHT_SHIFT_END);
        } else { // BOTH
            return (time.isAfter(ConstantsTime.DAY_SHIFT_START) && time.isBefore(ConstantsTime.DAY_SHIFT_END)) || (time.isAfter(ConstantsTime.NIGHT_SHIFT_START) || time.isBefore(ConstantsTime.NIGHT_SHIFT_END));
        }
    }

    public boolean canWorkAtLocation(Location location) {
        return workingLocations.contains(location) || possibleLocations.contains(location);
    }

    public int getTimeDislike(LocalDateTime dateTime) {
        int dislike = 0;
        DayOfWeek dayOfWeek = dateTime.getDayOfWeek();
        if (workingDays.contains(dayOfWeek)) {
            dislike += 0;
        } else if (possibleDays.contains(dayOfWeek)) {
            dislike += ConstantsDecisions.DAY_WEIGHT;
        } else {
            dislike += 100 * ConstantsDecisions.DAY_WEIGHT;
        }

        LocalTime time = dateTime.toLocalTime();
        if (shiftPreference == ShiftType.DAY &&
                (!time.isBefore(ConstantsTime.DAY_SHIFT_START) &&
                        !time.isAfter(ConstantsTime.DAY_SHIFT_END))) {
            dislike += 0;
        } else if (shiftPreference == ShiftType.NIGHT &&
                (time.isAfter(ConstantsTime.NIGHT_SHIFT_START) ||
                        time.isBefore(ConstantsTime.NIGHT_SHIFT_END))) {
            dislike += 0;
        } else if (shiftPreference == ShiftType.BOTH) { // BOTH
            dislike += 0;
        } else {
            dislike += 100 * ConstantsDecisions.SHIFT_WEIGHT;
        }

        return dislike;
    }

    public int getStartTimeDislike(LocalDateTime startTime) {
        return getTimeDislike(startTime.plus(ConstantsTime.TIME_ERROR));
    }

    public int getFinishTimeDislike(LocalDateTime finishTime) {
        return getTimeDislike(finishTime.minus(ConstantsTime.TIME_ERROR));
    }

    public int getLocationDislike(Location location) {
        int dislike = 0;
        if (workingLocations.contains(location)) {
            dislike += 0;
        } else if (possibleLocations.contains(location)) {
            dislike += ConstantsDecisions.LOCATION_WEIGHT;
        } else {
            dislike += 100 * ConstantsDecisions.LOCATION_WEIGHT;
        }
        return dislike;
    }
}
