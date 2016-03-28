package org.xbib.time.pretty;

import org.xbib.time.pretty.i18n.ResourcesTimeFormat;
import org.xbib.time.pretty.i18n.ResourcesTimeUnit;
import org.xbib.time.pretty.units.Century;
import org.xbib.time.pretty.units.Day;
import org.xbib.time.pretty.units.Decade;
import org.xbib.time.pretty.units.Hour;
import org.xbib.time.pretty.units.JustNow;
import org.xbib.time.pretty.units.Millennium;
import org.xbib.time.pretty.units.Millisecond;
import org.xbib.time.pretty.units.Minute;
import org.xbib.time.pretty.units.Month;
import org.xbib.time.pretty.units.Second;
import org.xbib.time.pretty.units.TimeUnitComparator;
import org.xbib.time.pretty.units.Week;
import org.xbib.time.pretty.units.Year;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * A utility for creating social-networking style timestamps. (e.g. "just now", "moments ago", "3 days ago",
 * "within 2 months")
 * <p>
 * <b>Usage:</b>
 * <p>
 * <code>
 * PrettyTime t = new PrettyTime();<br/>
 * String timestamp = t.format(new Date());<br/>
 * //result: moments from now
 * <p>
 * </code>
 */
public class PrettyTime {

    /**
     * The reference timestamp.
     * If the Date formatted is before the reference timestamp, the format command will produce a String that is in the
     * past tense. If the Date formatted is after the reference timestamp, the format command will produce a string
     * thatis in the future tense.
     */
    private LocalDateTime localDateTime;

    private Locale locale;

    private Map<TimeUnit, TimeFormat> units = new LinkedHashMap<>();

    /**
     * Default constructor
     */
    public PrettyTime() {
        setLocale(Locale.getDefault());
        initTimeUnits();
        this.localDateTime = LocalDateTime.now();
    }

    public PrettyTime(long l) {
        setLocale(Locale.getDefault());
        initTimeUnits();
        Instant instant = Instant.ofEpochMilli(l);
        ZoneId zoneId = ZoneId.systemDefault();
        this.localDateTime = LocalDateTime.ofInstant(instant, zoneId);
    }

    /**
     * Accept a {@link Date} timestamp to represent the point of reference for comparison. This may be changed by the
     * user, after construction.
     * <p>
     * See {@code PrettyTime.setReference(Date timestamp)}.
     *
     * @param localDateTime reference date time
     */
    public PrettyTime(LocalDateTime localDateTime) {
        setLocale(Locale.getDefault());
        initTimeUnits();
        this.localDateTime = localDateTime;
    }

    /**
     * Construct a new instance using the given {@link Locale} instead of the system default.
     */
    public PrettyTime(final Locale locale) {
        setLocale(locale);
        initTimeUnits();
        this.localDateTime = LocalDateTime.now();
    }

    /**
     * Accept a {@link Date} timestamp to represent the point of reference for comparison. This may be changed by the
     * user, after construction. Use the given {@link Locale} instead of the system default.
     * <p>
     * See {@code PrettyTime.setReference(Date timestamp)}.
     */
    public PrettyTime(final LocalDateTime localDateTime, final Locale locale) {
        setLocale(locale);
        initTimeUnits();
        this.localDateTime = localDateTime;
    }

    public PrettyTime(long l, final Locale locale) {
        setLocale(locale);
        initTimeUnits();
        Instant instant = Instant.ofEpochMilli(l);
        ZoneId zoneId = ZoneId.systemDefault();
        this.localDateTime = LocalDateTime.ofInstant(instant, zoneId);
    }

    /**
     * Calculate the approximate duration between the referenceDate and date
     */
    public Duration approximateDuration(final Date then) {
        if (then == null) {
            throw new IllegalArgumentException("Date to approximate must not be null.");
        }
        Date ref = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
        long difference = then.getTime() - ref.getTime();
        return calculateDuration(difference);
    }

    private void initTimeUnits() {
        addUnit(new JustNow());
        addUnit(new Millisecond());
        addUnit(new Second());
        addUnit(new Minute());
        addUnit(new Hour());
        addUnit(new Day());
        addUnit(new Week());
        addUnit(new Month());
        addUnit(new Year());
        addUnit(new Decade());
        addUnit(new Century());
        addUnit(new Millennium());
    }

    private void addUnit(ResourcesTimeUnit unit) {
        registerUnit(unit, new ResourcesTimeFormat(unit));
    }

    public Duration calculateDuration(final long difference) {
        long absoluteDifference = Math.abs(difference);
        List<TimeUnit> units = new ArrayList<>(getUnits().size());
        units.addAll(getUnits());
        Duration result = new Duration();
        for (int i = 0; i < units.size(); i++) {
            TimeUnit unit = units.get(i);
            long millisPerUnit = Math.abs(unit.getMillisPerUnit());
            long quantity = Math.abs(unit.getMaxQuantity());

            boolean isLastUnit = (i == units.size() - 1);

            if ((0 == quantity) && !isLastUnit) {
                quantity = units.get(i + 1).getMillisPerUnit() / unit.getMillisPerUnit();
            }

            // does our unit encompass the time duration?
            if ((millisPerUnit * quantity > absoluteDifference) || isLastUnit) {
                result.setUnit(unit);
                if (millisPerUnit > absoluteDifference) {
                    // we are rounding up: get 1 or -1 for past or future
                    result.setQuantity(getSign(difference));
                } else {
                    result.setQuantity(difference / millisPerUnit);
                }
                result.setDelta(difference - result.getQuantity() * millisPerUnit);
                break;
            }

        }
        return result;
    }

    private long getSign(final long difference) {
        if (0 > difference) {
            return -1;
        } else {
            return 1;
        }
    }

    /**
     * Calculate to the precision of the smallest provided {@link TimeUnit}, the exact duration represented by the
     * difference between the reference timestamp, and {@code then}
     * <p>
     * <b>Note</b>: Precision may be lost if no supplied {@link TimeUnit} is granular enough to represent one
     * millisecond
     *
     * @param then The date to be compared against the reference timestamp, or <i>now</i> if no reference timestamp was
     *             provided
     * @return A sorted {@link List} of {@link Duration} objects, from largest to smallest. Each element in the list
     * represents the approximate duration (number of times) that {@link TimeUnit} to fit into the previous
     * element's delta. The first element is the largest {@link TimeUnit} to fit within the total difference
     * between compared dates.
     */
    public List<Duration> calculatePreciseDuration(final Date then) {
        if (then == null) {
            throw new IllegalArgumentException("Date to calculate must not be null.");
        }
        Date ref = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
        List<Duration> result = new ArrayList<>();
        long difference = then.getTime() - ref.getTime();
        Duration duration = calculateDuration(difference);
        result.add(duration);
        while (0 != duration.getDelta()) {
            duration = calculateDuration(duration.getDelta());
            result.add(duration);
        }
        return result;
    }

    /**
     * Format the given {@link Date} object. This method applies the {@code PrettyTime.approximateDuration(date)}
     * method
     * to perform its calculation. If {@code then} is null, it will default to {@code new Date()}; also decorate for
     * past/future tense.
     *
     * @param then the {@link Date} to be formatted
     * @return A formatted string representing {@code then}
     */
    public String format(Date then) {
        if (then == null) {
            throw new IllegalArgumentException("Date to format must not be null.");
        }
        return format(approximateDuration(then));
    }

    /**
     * Format the given {@link Calendar} object. This method applies the {@code PrettyTime.approximateDuration(date)}
     * method
     * to perform its calculation. If {@code then} is null, it will default to {@code new Date()}; also decorate for
     * past/future tense.
     *
     * @param then the {@link Calendar} whose date is to be formatted
     * @return A formatted string representing {@code then}
     */
    public String format(Calendar then) {
        if (then == null) {
            throw new IllegalArgumentException("Provided Calendar must not be null.");
        }
        return format(then.getTime());
    }

    /**
     * Format the given {@link Date} object. This method applies the {@code PrettyTime.approximateDuration(date)}
     * method
     * to perform its calculation. If {@code then} is null, it will default to {@code new Date()}; also decorate for
     * past/future tense. Rounding rules are ignored.
     *
     * @param then the {@link Date} to be formatted
     * @return A formatted string representing {@code then}
     */
    public String formatUnrounded(final Date then) {
        Duration d = approximateDuration(then);
        return formatUnrounded(d);
    }

    /**
     * Format the given {@link Duration} object, using the {@link TimeFormat} specified by the {@link TimeUnit}
     * contained
     * within; also decorate for past/future tense.
     *
     * @param duration the {@link Duration} to be formatted
     * @return A formatted string representing {@code duration}
     */
    public String format(final Duration duration) {
        if (duration == null) {
            throw new IllegalArgumentException("Duration to format must not be null.");
        }
        TimeFormat format = getFormat(duration.getUnit());
        String time = format.format(duration);
        return format.decorate(duration, time);
    }

    /**
     * Format the given {@link Duration} object, using the {@link TimeFormat} specified by the {@link TimeUnit}
     * contained
     * within; also decorate for past/future tense. Rounding rules are ignored.
     *
     * @param duration the {@link Duration} to be formatted
     * @return A formatted string representing {@code duration}
     */
    public String formatUnrounded(final Duration duration) {
        if (duration == null) {
            throw new IllegalArgumentException("Duration to format must not be null.");
        }
        TimeFormat format = getFormat(duration.getUnit());
        String time = format.formatUnrounded(duration);
        return format.decorateUnrounded(duration, time);
    }

    /**
     * Format the given {@link Duration} objects, using the {@link TimeFormat} specified by the {@link TimeUnit}
     * contained within. Rounds only the last {@link Duration} object.
     *
     * @param durations the {@link Duration}s to be formatted
     * @return A list of formatted strings representing {@code durations}
     */
    public String format(final List<Duration> durations) {
        if (durations == null) {
            throw new IllegalArgumentException("Duration list must not be null.");
        }
        String result = null;
        StringBuilder builder = new StringBuilder();
        Duration duration = null;
        TimeFormat format = null;
        for (int i = 0; i < durations.size(); i++) {
            duration = durations.get(i);
            format = getFormat(duration.getUnit());
            boolean isLast = (i == durations.size() - 1);
            if (!isLast) {
                builder.append(format.formatUnrounded(duration));
                builder.append(" ");
            } else {
                builder.append(format.format(duration));
            }
        }
        if (format != null) {
            result = format.decorateUnrounded(duration, builder.toString());
        }
        return result;
    }

    /**
     * Given a date, returns a non-relative format string for the
     * approximate duration of the difference between the date and now.
     *
     * @param date the date to be formatted
     * @return A formatted string of the approximate duration
     */
    public String formatApproximateDuration(Date date) {
        Duration duration = approximateDuration(date);
        return formatDuration(duration);
    }

    /**
     * Given a duration, returns a non-relative format string.
     *
     * @param duration the duration to be formatted
     * @return A formatted string of the duration
     */
    public String formatDuration(Duration duration) {
        TimeFormat timeFormat = getFormat(duration.getUnit());
        return timeFormat.format(duration);
    }

    /**
     * Get the registered {@link TimeFormat} for the given {@link TimeUnit} or null if none exists.
     */
    public TimeFormat getFormat(TimeUnit unit) {
        if (unit == null) {
            throw new IllegalArgumentException("Time unit must not be null.");
        }

        if (units.get(unit) != null) {
            return units.get(unit);
        }
        return null;
    }

    public PrettyTime setReference(LocalDateTime localDateTime) {
        this.localDateTime = localDateTime;
        return this;
    }

    /**
     * Get a {@link List} of the current configured {@link TimeUnit} instances in calculations.
     *
     * @return list
     */
    public List<TimeUnit> getUnits() {
        List<TimeUnit> result = new ArrayList<>(units.keySet());
        Collections.sort(result, new TimeUnitComparator());
        return Collections.unmodifiableList(result);
    }

    /**
     * Get the registered {@link TimeUnit} for the given {@link TimeUnit} type or null if none exists.
     *
     * @return unit
     */
    @SuppressWarnings("unchecked")
    public <UNIT extends TimeUnit> UNIT getUnit(final Class<UNIT> unitType) {
        if (unitType == null) {
            throw new IllegalArgumentException("Unit type to get must not be null.");
        }
        for (TimeUnit unit : units.keySet()) {
            if (unitType.isAssignableFrom(unit.getClass())) {
                return (UNIT) unit;
            }
        }
        return null;
    }

    /**
     * Register the given {@link TimeUnit} and corresponding {@link TimeFormat} instance to be used in calculations. If
     * an entry already exists for the given {@link TimeUnit}, its format will be overwritten with the given
     * {@link TimeFormat}.
     */
    public PrettyTime registerUnit(final TimeUnit unit, TimeFormat format) {
        if (unit == null) {
            throw new IllegalArgumentException("Unit to register must not be null.");
        }
        if (format == null) {
            throw new IllegalArgumentException("Format to register must not be null.");
        }
        units.put(unit, format);
        if (unit instanceof LocaleAware) {
            ((LocaleAware<?>) unit).setLocale(locale);
        }
        if (format instanceof LocaleAware) {
            ((LocaleAware<?>) format).setLocale(locale);
        }
        return this;
    }

    /**
     * Removes the mapping for the given {@link TimeUnit} type. This effectively de-registers the unit so it will not
     * be
     * used in formatting. Returns the {@link TimeFormat} that was registered for the given {@link TimeUnit} type, or
     * null if no unit of the given type was registered.
     */
    public <UNIT extends TimeUnit> TimeFormat removeUnit(final Class<UNIT> unitType) {
        if (unitType == null) {
            throw new IllegalArgumentException("Unit type to remove must not be null.");
        }

        for (TimeUnit unit : units.keySet()) {
            if (unitType.isAssignableFrom(unit.getClass())) {
                return units.remove(unit);
            }
        }
        return null;
    }

    /**
     * Removes the mapping for the given {@link TimeUnit}. This effectively de-registers the unit so it will not be
     * used
     * in formatting. Returns the {@link TimeFormat} that was registered for the given {@link TimeUnit}, or null if no
     * such unit was registered.
     */
    public TimeFormat removeUnit(final TimeUnit unit) {
        if (unit == null) {
            throw new IllegalArgumentException("Unit to remove must not be null.");
        }

        return units.remove(unit);
    }

    /**
     * Get the currently configured {@link Locale} for this {@link PrettyTime} object.
     */
    public Locale getLocale() {
        return locale;
    }

    /**
     * Set the the {@link Locale} for this {@link PrettyTime} object. This may be an expensive operation, since this
     * operation calls {@link LocaleAware#setLocale(Locale)} for each {@link TimeUnit} in {@link #getUnits()}.
     */
    public PrettyTime setLocale(final Locale locale) {
        this.locale = locale;
        units.keySet().stream().filter(unit -> unit instanceof LocaleAware)
                .forEach(unit -> ((LocaleAware<?>) unit).setLocale(locale));
        units.values().stream().filter(format -> format instanceof LocaleAware)
                .forEach(format -> ((LocaleAware<?>) format).setLocale(locale));
        return this;
    }

    @Override
    public String toString() {
        return "PrettyTime [date=" + localDateTime + ", locale=" + locale + "]";
    }

    /**
     * Remove all registered {@link TimeUnit} instances.
     *
     * @return The removed {@link TimeUnit} instances.
     */
    public List<TimeUnit> clearUnits() {
        List<TimeUnit> result = getUnits();
        units.clear();
        return result;
    }

}
