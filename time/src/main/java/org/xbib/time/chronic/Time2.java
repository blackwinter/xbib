package org.xbib.time.chronic;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.MonthDay;
import java.time.Year;
import java.time.ZonedDateTime;
import java.time.chrono.IsoChronology;

public class Time2 {

    public ZonedDateTime construct(int year, int month) {
        LocalDate localDate = IsoChronology.INSTANCE.date(year, month, 1);
        return ZonedDateTime.from(localDate);
    }

    public ZonedDateTime construct(int year, int month, int day) {
        LocalDate localDate = IsoChronology.INSTANCE.date(year, month, day);
        return ZonedDateTime.from(localDate);
    }

    public ZonedDateTime construct(int year, int month, int day, int hour) {
        LocalDateTime localDateTime = LocalDateTime.of(year, month, day, hour, 0);
        return ZonedDateTime.from(localDateTime);
    }

    public ZonedDateTime construct(int year, int month, int day, int hour, int minute) {
        LocalDateTime localDateTime = LocalDateTime.of(year, month, day, hour, minute);
        return ZonedDateTime.from(localDateTime);
    }

    public ZonedDateTime construct(int year, int month, int day, int hour, int minute, int second) {
        LocalDateTime localDateTime = LocalDateTime.of(year, month, day, hour, minute, second);
        return ZonedDateTime.from(localDateTime);
    }

    public ZonedDateTime construct(int year, int month, int day, int hour, int minute, int second, int millisecond) {
        LocalDateTime localDateTime = LocalDateTime.of(year, month, day, hour, minute, second, millisecond);
        return ZonedDateTime.from(localDateTime);
    }

    public ZonedDateTime y(LocalDateTime basis) {
        Year year = Year.from(basis);
        return ZonedDateTime.from(year);
    }

    public ZonedDateTime yJan1(LocalDateTime basis) {
        Year year = Year.from(basis);
        return ZonedDateTime.from(year);
    }

    public ZonedDateTime y(LocalDateTime basis, int month) {
        Year year = Year.from(basis);
        LocalDate localDate = LocalDate.of(year.getValue(), month, 1);
        return ZonedDateTime.from(localDate);
    }

    public ZonedDateTime y(LocalDateTime basis, int month, int day) {
        Year year = Year.from(basis);
        LocalDate localDate = LocalDate.of(year.getValue(), month, day);
        return ZonedDateTime.from(localDate);
    }

    public ZonedDateTime ym(LocalDateTime basis) {
        Year year = Year.from(basis);
        Month month = Month.from(basis);
        LocalDate localDate = LocalDate.of(year.getValue(), month, 1);
        return ZonedDateTime.from(localDate);
    }

    public ZonedDateTime ymd(LocalDateTime basis) {
        Year year = Year.from(basis);
        Month month = Month.from(basis);
        MonthDay day = MonthDay.from(basis);
        LocalDate localDate = LocalDate.of(year.getValue(), month.getValue(), day.getDayOfMonth());
        return ZonedDateTime.from(localDate);
    }

}
