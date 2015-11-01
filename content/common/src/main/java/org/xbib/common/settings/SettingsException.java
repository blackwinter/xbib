
package org.xbib.common.settings;

/**
 * A generic failure to handle settings.
 */
public class SettingsException extends RuntimeException {

    public SettingsException(String message) {
        super(message);
    }

    public SettingsException(String message, Throwable cause) {
        super(message, cause);
    }
}
