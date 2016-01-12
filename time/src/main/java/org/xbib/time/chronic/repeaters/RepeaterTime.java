package org.xbib.time.chronic.repeaters;

import org.xbib.time.chronic.Options;
import org.xbib.time.chronic.Span;
import org.xbib.time.chronic.Tick;
import org.xbib.time.chronic.Time;
import org.xbib.time.chronic.Token;
import org.xbib.time.chronic.tags.Pointer.PointerType;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

public class RepeaterTime extends Repeater<Tick> {
    private static final Pattern TIME_PATTERN = Pattern.compile("^\\d{1,2}(:?\\d{2})?([\\.:]?\\d{2})?$");
    private Calendar currentTime;

    public RepeaterTime(String time) {
        super(null);
        String t = time.replaceAll(":", "");
        Tick type;
        int length = t.length();
        if (length <= 2) {
            int hours = Integer.parseInt(t);
            int hoursInSeconds = hours * 60 * 60;
            if (hours == 12) {
                type = new Tick(0, true);
            } else {
                type = new Tick(hoursInSeconds, true);
            }
        } else if (length == 3) {
            int hoursInSeconds = Integer.parseInt(t.substring(0, 1)) * 60 * 60;
            int minutesInSeconds = Integer.parseInt(t.substring(1)) * 60;
            type = new Tick(hoursInSeconds + minutesInSeconds, true);
        } else if (length == 4) {
            boolean ambiguous = (time.contains(":") && Integer.parseInt(t.substring(0, 1)) != 0 && Integer.parseInt(t.substring(0, 2)) <= 12);
            int hours = Integer.parseInt(t.substring(0, 2));
            int hoursInSeconds = hours * 60 * 60;
            int minutesInSeconds = Integer.parseInt(t.substring(2)) * 60;
            if (hours == 12) {
                type = new Tick(minutesInSeconds, ambiguous);
            } else {
                type = new Tick(hoursInSeconds + minutesInSeconds, ambiguous);
            }
        } else if (length == 5) {
            int hoursInSeconds = Integer.parseInt(t.substring(0, 1)) * 60 * 60;
            int minutesInSeconds = Integer.parseInt(t.substring(1, 3)) * 60;
            int seconds = Integer.parseInt(t.substring(3));
            type = new Tick(hoursInSeconds + minutesInSeconds + seconds, true);
        } else if (length == 6) {
            boolean ambiguous = (time.contains(":") && Integer.parseInt(t.substring(0, 1)) != 0 && Integer.parseInt(t.substring(0, 2)) <= 12);
            int hours = Integer.parseInt(t.substring(0, 2));
            int hoursInSeconds = hours * 60 * 60;
            int minutesInSeconds = Integer.parseInt(t.substring(2, 4)) * 60;
            int seconds = Integer.parseInt(t.substring(4, 6));
            //type = new Tick(hoursInSeconds + minutesInSeconds + seconds, ambiguous);
            if (hours == 12) {
                type = new Tick(minutesInSeconds + seconds, ambiguous);
            } else {
                type = new Tick(hoursInSeconds + minutesInSeconds + seconds, ambiguous);
            }
        } else {
            throw new IllegalArgumentException("Time cannot exceed six digits");
        }
        setType(type);
    }

    public static RepeaterTime scan(Token token, List<Token> tokens, Options options) {
        if (RepeaterTime.TIME_PATTERN.matcher(token.getWord()).matches()) {
            return new RepeaterTime(token.getWord());
        }
        Integer intStrValue = integerValue(token.getWord());
        if (intStrValue != null) {
            return new RepeaterTime(intStrValue.toString());
        }
        return null;
    }

    private static Integer integerValue(String str) {
        if (str != null) {
            String s = str.toLowerCase();
            if ("one".equals(s)) {
                return 1;
            } else if ("two".equals(s)) {
                return 2;
            } else if ("three".equals(s)) {
                return 3;
            } else if ("four".equals(s)) {
                return 4;
            } else if ("five".equals(s)) {
                return 5;
            } else if ("six".equals(s)) {
                return 6;
            } else if ("seven".equals(s)) {
                return 7;
            } else if ("eight".equals(s)) {
                return 8;
            } else if ("nine".equals(s)) {
                return 9;
            } else if ("ten".equals(s)) {
                return 10;
            } else if ("eleven".equals(s)) {
                return 11;
            } else if ("twelve".equals(s)) {
                return 12;
            }
        }
        return null;
    }

    @Override
    protected Span _nextSpan(PointerType pointer) {
        int halfDay = RepeaterDay.DAY_SECONDS / 2;
        int fullDay = RepeaterDay.DAY_SECONDS;

        Calendar now = getNow();
        Tick tick = getType();
        boolean first = false;
        if (currentTime == null) {
            first = true;
            Calendar midnight = Time.ymd(now);
            Calendar yesterdayMidnight = Time.cloneAndAdd(midnight, Calendar.SECOND, -fullDay);
            Calendar tomorrowMidnight = Time.cloneAndAdd(midnight, Calendar.SECOND, fullDay);
            boolean done = false;
            if (pointer == PointerType.FUTURE) {
                if (tick.isAmbiguous()) {
                    List<Calendar> futureDates = new LinkedList<Calendar>();
                    futureDates.add(Time.cloneAndAdd(midnight, Calendar.SECOND, tick.intValue()));
                    futureDates.add(Time.cloneAndAdd(midnight, Calendar.SECOND, halfDay + tick.intValue()));
                    futureDates.add(Time.cloneAndAdd(tomorrowMidnight, Calendar.SECOND, tick.intValue()));
                    for (Calendar futureDate : futureDates) {
                        if (futureDate.after(now) || futureDate.equals(now)) {
                            currentTime = futureDate;
                            done = true;
                            break;
                        }
                    }
                } else {
                    List<Calendar> futureDates = new LinkedList<Calendar>();
                    futureDates.add(Time.cloneAndAdd(midnight, Calendar.SECOND, tick.intValue()));
                    futureDates.add(Time.cloneAndAdd(tomorrowMidnight, Calendar.SECOND, tick.intValue()));
                    for (Calendar futureDate : futureDates) {
                        if (futureDate.after(now) || futureDate.equals(now)) {
                            currentTime = futureDate;
                            done = true;
                            break;
                        }
                    }
                }
            } else {
                if (tick.isAmbiguous()) {
                    List<Calendar> pastDates = new LinkedList<Calendar>();
                    pastDates.add(Time.cloneAndAdd(midnight, Calendar.SECOND, halfDay + tick.intValue()));
                    pastDates.add(Time.cloneAndAdd(midnight, Calendar.SECOND, tick.intValue()));
                    pastDates.add(Time.cloneAndAdd(yesterdayMidnight, Calendar.SECOND, tick.intValue() * 2));
                    for (Calendar pastDate : pastDates) {
                        if (pastDate.before(now) || pastDate.equals(now)) {
                            currentTime = pastDate;
                            done = true;
                            break;
                        }
                    }
                } else {
                    List<Calendar> pastDates = new LinkedList<Calendar>();
                    pastDates.add(Time.cloneAndAdd(midnight, Calendar.SECOND, tick.intValue()));
                    pastDates.add(Time.cloneAndAdd(yesterdayMidnight, Calendar.SECOND, tick.intValue()));
                    for (Calendar pastDate : pastDates) {
                        if (pastDate.before(now) || pastDate.equals(now)) {
                            currentTime = pastDate;
                            done = true;
                            break;
                        }
                    }
                }
            }

            if (!done && currentTime == null) {
                throw new IllegalStateException("Current time cannot be null at this point.");
            }
        }

        if (!first) {
            int increment = (tick.isAmbiguous()) ? halfDay : fullDay;
            int direction = (pointer == PointerType.FUTURE) ? 1 : -1;
            currentTime.add(Calendar.SECOND, direction * increment);
        }

        return new Span(currentTime, Time.cloneAndAdd(currentTime, Calendar.SECOND, getWidth()));
    }

    @Override
    protected Span _thisSpan(PointerType pointer) {
        if (pointer == PointerType.NONE) {
            pointer = PointerType.FUTURE;
        }
        return nextSpan(pointer);
    }

    @Override
    public Span getOffset(Span span, int amount, PointerType pointer) {
        throw new IllegalStateException("Not implemented.");
    }

    @Override
    public int getWidth() {
        return 1;
    }

    @Override
    public String toString() {
        return super.toString() + "-time-" + getType();
    }
}
