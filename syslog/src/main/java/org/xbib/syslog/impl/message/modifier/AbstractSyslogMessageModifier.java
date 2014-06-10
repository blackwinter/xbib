package org.xbib.syslog.impl.message.modifier;

import org.xbib.syslog.SyslogMessageModifierConfigIF;
import org.xbib.syslog.SyslogMessageModifierIF;

public abstract class AbstractSyslogMessageModifier implements SyslogMessageModifierIF {

    protected SyslogMessageModifierConfigIF messageModifierConfig = null;

    public AbstractSyslogMessageModifier(SyslogMessageModifierConfigIF messageModifierConfig) {
        this.messageModifierConfig = messageModifierConfig;
    }

    public String[] parseInlineModifier(String message) {
        return parseInlineModifier(message, this.messageModifierConfig.getPrefix(), this.messageModifierConfig.getSuffix());
    }

    public static String[] parseInlineModifier(String message, String prefix, String suffix) {
        String[] messageAndModifier = null;

        if (message == null || "".equals(message.trim())) {
            return null;
        }

        if (prefix == null || "".equals(prefix)) {
            prefix = " ";
        }

        if (suffix == null || "".equals(suffix)) {
            int pi = message.lastIndexOf(prefix);

            if (pi > -1) {
                messageAndModifier = new String[]{message.substring(0, pi), message.substring(pi + prefix.length())};
            }

        } else {
            int si = message.lastIndexOf(suffix);

            if (si > -1) {
                int pi = message.lastIndexOf(prefix, si);

                if (pi > -1) {
                    messageAndModifier = new String[]{message.substring(0, pi), message.substring(pi + prefix.length(), si)};
                }
            }
        }

        return messageAndModifier;
    }

    protected abstract boolean verify(String message, String modifier);

    public boolean verify(String message) {
        String[] messageAndModifier = parseInlineModifier(message);

        if (messageAndModifier == null || messageAndModifier.length != 2) {
            return false;
        }

        return verify(messageAndModifier[0], messageAndModifier[1]);
    }
}
