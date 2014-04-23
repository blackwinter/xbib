/**
 * Copyright (c) 2012, Ben Fortuna
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  o Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 *  o Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 *  o Neither the name of Ben Fortuna nor the names of any other contributors
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.fortuna.ical4j.model;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import net.fortuna.ical4j.util.CompatibilityHints;
import net.fortuna.ical4j.util.Dates;
import net.fortuna.ical4j.util.TimeZones;


/**
 * $Id$
 *
 * Created on 26/06/2005
 *
 * Base class for all representations of time values in RFC2445.
 *
 * <pre>
 * 4.3.4 Date
 * 
 *    Value Name: DATE
 * 
 *    Purpose: This value type is used to identify values that contain a
 *    calendar date.
 * 
 *    Formal Definition: The value type is defined by the following
 *    notation:
 * 
 *      date               = date-value
 * 
 *      date-value         = date-fullyear date-month date-mday
 *      date-fullyear      = 4DIGIT
 *      date-month         = 2DIGIT        ;01-12
 *      date-mday          = 2DIGIT        ;01-28, 01-29, 01-30, 01-31
 *                                         ;based on month/year
 * 
 *    Description: If the property permits, multiple "date" values are
 *    specified as a COMMA character (US-ASCII decimal 44) separated list
 *    of values. The format for the value type is expressed as the [ISO
 *    8601] complete representation, basic format for a calendar date. The
 *    textual format specifies a four-digit year, two-digit month, and
 *    two-digit day of the month. There are no separator characters between
 *    the year, month and day component text.
 * 
 *    No additional content value encoding (i.e., BACKSLASH character
 *    encoding) is defined for this value type.
 * 
 *    Example: The following represents July 14, 1997:
 * 
 *      19970714
 * 
 * </pre>
 * 
 * @author Ben Fortuna
 */
public class Date extends Iso8601 {

    private static final long serialVersionUID = 7136072363141363141L;

    private static final String DEFAULT_PATTERN = "yyyyMMdd";
    
    private static final String VCARD_PATTERN = "yyyy'-'MM'-'dd";

    /**
     * Default constructor.
     */
    public Date() {
        super(DEFAULT_PATTERN, Dates.PRECISION_DAY, TimeZones.getDateTimeZone());
    }
    
    /**
     * Creates a new date instance with the specified precision. This
     * constructor is only intended for use by sub-classes.
     * @param precision the date precision
     * @param tz the timezone
     * @see net.fortuna.ical4j.util.Dates#PRECISION_DAY
     * @see net.fortuna.ical4j.util.Dates#PRECISION_SECOND
     */
    protected Date(final int precision, TimeZone tz) {
        super(DEFAULT_PATTERN, precision, tz);
    }

    /**
     * @param time a date value in milliseconds
     */
    public Date(final long time) {
        super(time, DEFAULT_PATTERN, Dates.PRECISION_DAY, TimeZones.getDateTimeZone());
    }
    
    /**
     * Creates a new date instance with the specified precision. This
     * constructor is only intended for use by sub-classes.
     * @param time a date value in milliseconds
     * @param precision the date precision
     * @param tz the timezone
     * @see net.fortuna.ical4j.util.Dates#PRECISION_DAY
     * @see net.fortuna.ical4j.util.Dates#PRECISION_SECOND
     */
    protected Date(final long time, final int precision, TimeZone tz) {
        super(time, DEFAULT_PATTERN, precision, tz);
    }

    /**
     * @param date a date value
     */
    public Date(final java.util.Date date) {
//        this();
        this(date.getTime(), Dates.PRECISION_DAY, TimeZones.getDateTimeZone());
//        setTime(date.getTime());
    }

    /**
     * @param value a string representation of a date
     * @throws java.text.ParseException where the specified string is not a valid date
     */
    public Date(final String value) throws ParseException {
        this();
        try {
        	setTime(getFormat().parse(value).getTime());
        } catch (ParseException pe) {
        	if (CompatibilityHints.isHintEnabled(CompatibilityHints.KEY_VCARD_COMPATIBILITY)) {
                final DateFormat parseFormat = new SimpleDateFormat(VCARD_PATTERN);
                parseFormat.setTimeZone(TimeZones.getDateTimeZone());
                setTime(parseFormat.parse(value).getTime());
        	}
        	else {
        		throw pe;
        	}
        }
    }
    
    /**
     * @param value a string representation of a date
     * @param pattern a date pattern to apply when parsing
     * @throws java.text.ParseException where the specified string is not a valid date
     */
    public Date(String value, String pattern) throws ParseException {
        super(DEFAULT_PATTERN, Dates.PRECISION_DAY, TimeZones.getDateTimeZone());
        final DateFormat parseFormat = new SimpleDateFormat(pattern);
        parseFormat.setTimeZone(TimeZones.getDateTimeZone());
        setTime(parseFormat.parse(value).getTime());
    }
}
