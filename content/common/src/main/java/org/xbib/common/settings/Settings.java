/*
 * Licensed to Jörg Prante and xbib under one or more contributor
 * license agreements. See the NOTICE.txt file distributed with this work
 * for additional information regarding copyright ownership.
 *
 * Copyright (C) 2012 Jörg Prante and xbib
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses
 * or write to the Free Software Foundation, Inc., 51 Franklin Street,
 * Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * The interactive user interfaces in modified source and object code
 * versions of this program must display Appropriate Legal Notices,
 * as required under Section 5 of the GNU Affero General Public License.
 *
 * In accordance with Section 7(b) of the GNU Affero General Public
 * License, these Appropriate Legal Notices must retain the display of the
 * "Powered by xbib" logo. If the display of the logo is not reasonably
 * feasible for technical reasons, the Appropriate Legal Notices must display
 * the words "Powered by xbib".
 */
package org.xbib.common.settings;

import com.google.common.collect.ImmutableMap;
import org.xbib.common.unit.ByteSizeValue;
import org.xbib.common.unit.TimeValue;

import java.io.StringReader;
import java.text.ParseException;
import java.util.Map;

/**
 * Immutable settings allowing to control the configuration.
 *
 * Using {@link ImmutableSettings#settingsBuilder()} in order to create a builder
 * which in turn can create an immutable implementation of settings.
 *
 * @see ImmutableSettings
 */
public interface Settings {

    /**
     * Component settings for a specific component. Returns all the settings for the given class, where the
     * FQN of the class is used, without provided prefix.
     */
    Settings getComponentSettings(String prefix, Class component);

    /**
     * A settings that are filtered (and key is removed) with the specified prefix.
     */
    Settings getByPrefix(String prefix);

    /**
     * Returns the settings mapped to the given setting name.
     */
    Settings getAsSettings(String setting);

    /**
     * The settings as a {@link com.google.common.collect.ImmutableMap}.
     */
    ImmutableMap<String, String> getAsMap();

    StringReader getAsReader();

    boolean containsSetting(String setting);

    /**
     * Returns the setting value associated with the setting key.
     *
     * @param setting The setting key
     * @return The setting value, <tt>null</tt> if it does not exists.
     */
    String get(String setting);

    /**
     * Returns the setting value associated with the setting key. If it does not exists,
     * returns the default value provided.
     *
     * @param setting      The setting key
     * @param defaultValue The value to return if no value is associated with the setting
     * @return The setting value, or the default value if no value exists
     */
    String get(String setting, String defaultValue);

    /**
     * Returns group settings for the given setting prefix.
     */
    Map<String, Settings> getGroups(String settingPrefix) throws SettingsException;

    /**
     * Returns the setting value (as float) associated with the setting key. If it does not exists,
     * returns the default value provided.
     *
     * @param setting      The setting key
     * @param defaultValue The value to return if no value is associated with the setting
     * @return The (float) value, or the default value if no value exists.
     * @throws SettingsException Failure to parse the setting
     */
    Float getAsFloat(String setting, Float defaultValue) throws SettingsException;

    /**
     * Returns the setting value (as double) associated with the setting key. If it does not exists,
     * returns the default value provided.
     *
     * @param setting      The setting key
     * @param defaultValue The value to return if no value is associated with the setting
     * @return The (double) value, or the default value if no value exists.
     * @throws SettingsException Failure to parse the setting
     */
    Double getAsDouble(String setting, Double defaultValue) throws SettingsException;

    /**
     * Returns the setting value (as int) associated with the setting key. If it does not exists,
     * returns the default value provided.
     *
     * @param setting      The setting key
     * @param defaultValue The value to return if no value is associated with the setting
     * @return The (int) value, or the default value if no value exists.
     * @throws SettingsException Failure to parse the setting
     */
    Integer getAsInt(String setting, Integer defaultValue) throws SettingsException;

    /**
     * Returns the setting value (as long) associated with the setting key. If it does not exists,
     * returns the default value provided.
     *
     * @param setting      The setting key
     * @param defaultValue The value to return if no value is associated with the setting
     * @return The (long) value, or the default value if no value exists.
     * @throws SettingsException Failure to parse the setting
     */
    Long getAsLong(String setting, Long defaultValue) throws SettingsException;

    /**
     * Returns the setting value (as boolean) associated with the setting key. If it does not exists,
     * returns the default value provided.
     *
     * @param setting      The setting key
     * @param defaultValue The value to return if no value is associated with the setting
     * @return The (boolean) value, or the default value if no value exists.
     * @throws SettingsException Failure to parse the setting
     */
    Boolean getAsBoolean(String setting, Boolean defaultValue) throws SettingsException;

    /**
     * Returns the setting value (as time) associated with the setting key. If it does not exists,
     * returns the default value provided.
     */
    TimeValue getAsTime(String setting, TimeValue defaultValue);

    /**
     * Returns the value as byte size associated with the setting key.
     * @param setting The setting key
     * @param defaultValue the default value
     * @return the byte size value
     * @throws SettingsException
     */
    ByteSizeValue getAsBytesSize(String setting, ByteSizeValue defaultValue) throws SettingsException;

    /**
     * The values associated with a setting prefix as an array. The settings array is in the format of:
     * <tt>settingPrefix.[index]</tt>.
     * <p/>
     * <p>It will also automatically load a comma separated list under the settingPrefix and merge with
     * the numbered format.
     *
     * @param settingPrefix The setting prefix to load the array by
     * @return The setting array values
     * @throws SettingsException
     */
    String[] getAsArray(String settingPrefix, String[] defaultArray) throws SettingsException;

    /**
     * The values associated with a setting prefix as an array. The settings array is in the format of:
     * <tt>settingPrefix.[index]</tt>.
     * <p/>
     * <p>It will also automatically load a comma separated list under the settingPrefix and merge with
     * the numbered format.
     *
     * @param settingPrefix The setting prefix to load the array by
     * @return The setting array values
     * @throws SettingsException
     */
    String[] getAsArray(String settingPrefix) throws SettingsException;

    /**
     * A settings builder interface.
     */
    interface Builder {

        /**
         * Builds the settings.
         */
        Settings build();
    }
}
