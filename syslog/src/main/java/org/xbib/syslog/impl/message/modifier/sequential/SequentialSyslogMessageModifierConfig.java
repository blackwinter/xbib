package org.xbib.syslog.impl.message.modifier.sequential;

import org.xbib.syslog.impl.message.modifier.AbstractSyslogMessageModifierConfig;

/**
 * SequentialSyslogMessageModifierConfig is an implementation of AbstractSyslogMessageModifierConfig
 * that provides configuration for SequentialSyslogMessageModifier.
 */
public class SequentialSyslogMessageModifierConfig extends AbstractSyslogMessageModifierConfig {

    protected long firstNumber = SYSLOG_SEQUENTIAL_MESSAGE_MODIFIER_FIRST_NUMBER_DEFAULT;

    protected long lastNumber = SYSLOG_SEQUENTIAL_MESSAGE_MODIFIER_LAST_NUMBER_DEFAULT;

    protected char padChar = SYSLOG_SEQUENTIAL_MESSAGE_MODIFIER_PAD_CHAR_DEFAULT;

    protected boolean usePadding = SYSLOG_SEQUENTIAL_MESSAGE_MODIFIER_USE_PADDING_DEFAULT;

    public static final SequentialSyslogMessageModifierConfig createDefault() {
        SequentialSyslogMessageModifierConfig modifierConfig = new SequentialSyslogMessageModifierConfig();

        return modifierConfig;
    }

    public SequentialSyslogMessageModifierConfig() {
        setPrefix(SYSLOG_SEQUENTIAL_MESSAGE_MODIFIER_PREFIX_DEFAULT);
        setSuffix(SYSLOG_SEQUENTIAL_MESSAGE_MODIFIER_SUFFIX_DEFAULT);
    }

    public long getLastNumberDigits() {
        return Long.toString(this.lastNumber).length();
    }

    public long getFirstNumber() {
        return this.firstNumber;
    }

    public void setFirstNumber(long firstNumber) {
        if (firstNumber < this.lastNumber) {
            this.firstNumber = firstNumber;
        }
    }

    public long getLastNumber() {
        return this.lastNumber;
    }

    public void setLastNumber(long lastNumber) {
        if (lastNumber > this.firstNumber) {
            this.lastNumber = lastNumber;
        }
    }

    public boolean isUsePadding() {
        return this.usePadding;
    }

    public void setUsePadding(boolean usePadding) {
        this.usePadding = usePadding;
    }

    public char getPadChar() {
        return this.padChar;
    }

    public void setPadChar(char padChar) {
        this.padChar = padChar;
    }
}
