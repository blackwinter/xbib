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
import com.google.common.collect.Lists;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.xbib.common.settings.loader.JsonSettingsLoader;
import org.xbib.common.unit.ByteSizeValue;
import org.xbib.common.unit.TimeValue;
import org.xbib.common.xcontent.XContentBuilder;
import org.xbib.io.stream.StreamInput;
import org.xbib.io.stream.StreamOutput;
import org.xbib.common.settings.loader.SettingsLoader;
import org.xbib.common.settings.loader.SettingsLoaderFactory;

import static org.xbib.common.unit.ByteSizeValue.parseBytesSizeValue;
import static org.xbib.common.unit.TimeValue.parseTimeValue;
import static org.xbib.common.xcontent.XContentFactory.jsonBuilder;

/**
 * An immutable implementation of {@link Settings}.
 */
public class ImmutableSettings implements Settings {

    private ImmutableMap<String, String> settings;

    private ImmutableSettings(Map<String, String> settings) {
        this.settings = ImmutableMap.copyOf(settings);
    }

    @Override
    public ImmutableMap<String, String> getAsMap() {
        return this.settings;
    }

    @Override
    public StringReader getAsReader() {
        try {
            XContentBuilder builder = jsonBuilder();
            builder.startObject();
            for (Map.Entry<String, String> entry : getAsMap().entrySet()) {
                builder.field(entry.getKey(), entry.getValue());
            }
            builder.endObject();
            return new StringReader(builder.string());
        } catch (IOException e) {
            //
        }
        return null;
    }

    @Override
    public Settings getComponentSettings(String prefix, Class component) {
        String type = component.getName();
        if (!type.startsWith(prefix)) {
            throw new SettingsException("Component [" + type + "] does not start with prefix [" + prefix + "]");
        }
        String settingPrefix = type.substring(prefix.length() + 1); // 1 for the '.'
        settingPrefix = settingPrefix.substring(0, settingPrefix.length() - component.getSimpleName().length()); // remove the simple class name (keep the dot)
        return getByPrefix(settingPrefix);
    }

    @Override
    public Settings getByPrefix(String prefix) {
        Builder builder = new Builder();
        for (Map.Entry<String, String> entry : settings.entrySet()) {
            if (entry.getKey().startsWith(prefix)) {
                if (entry.getKey().length() < prefix.length()) {
                    continue;
                }
                builder.put(entry.getKey().substring(prefix.length()), entry.getValue());
            }
        }
        return builder.build();
    }

    @Override
    public Settings getAsSettings(String setting) {
        return getByPrefix(setting + ".");
    }

    @Override
    public boolean containsSetting(String setting) {
        if (settings.containsKey(setting)) {
            return true;
        }
        for (Map.Entry<String, String> entry : settings.entrySet()) {
            if (entry.getKey().startsWith(setting)) {
                return true;
            }
        }
        return false;
    }


    @Override
    public String get(String setting) {
        String retVal = settings.get(setting);
        if (retVal != null) {
            return retVal;
        }
        return null;
    }

    @Override
    public String get(String setting, String defaultValue) {
        String retVal = settings.get(setting);
        return retVal == null ? defaultValue : retVal;
    }

    @Override
    public Float getAsFloat(String setting, Float defaultValue) {
        String sValue = get(setting);
        if (sValue == null) {
            return defaultValue;
        }
        try {
            return Float.parseFloat(sValue);
        } catch (NumberFormatException e) {
            throw new SettingsException("Failed to parse float setting [" + setting + "] with value [" + sValue + "]", e);
        }
    }

    @Override
    public Double getAsDouble(String setting, Double defaultValue) {
        String sValue = get(setting);
        if (sValue == null) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(sValue);
        } catch (NumberFormatException e) {
            throw new SettingsException("Failed to parse double setting [" + setting + "] with value [" + sValue + "]", e);
        }
    }

    @Override
    public Integer getAsInt(String setting, Integer defaultValue) {
        String sValue = get(setting);
        if (sValue == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(sValue);
        } catch (NumberFormatException e) {
            throw new SettingsException("Failed to parse int setting [" + setting + "] with value [" + sValue + "]", e);
        }
    }

    @Override
    public Long getAsLong(String setting, Long defaultValue) {
        String sValue = get(setting);
        if (sValue == null) {
            return defaultValue;
        }
        try {
            return Long.parseLong(sValue);
        } catch (NumberFormatException e) {
            throw new SettingsException("Failed to parse long setting [" + setting + "] with value [" + sValue + "]", e);
        }
    }

    @Override
    public Boolean getAsBoolean(String setting, Boolean defaultValue) {
        String value = get(setting);
        if (value == null) {
            return defaultValue;
        }
        return !(value.equals("false") || value.equals("0") || value.equals("off") || value.equals("no"));
    }

    @Override
    public TimeValue getAsTime(String setting, TimeValue defaultValue) {
        return parseTimeValue(get(setting), defaultValue);
    }

    @Override
    public ByteSizeValue getAsBytesSize(String setting, ByteSizeValue defaultValue) throws SettingsException {
        return parseBytesSizeValue(get(setting), defaultValue);
    }

    @Override
    public String[] getAsArray(String settingPrefix) throws SettingsException {
        return getAsArray(settingPrefix, EMPTY_ARRAY);
    }

    @Override
    public String[] getAsArray(String settingPrefix, String[] defaultArray) throws SettingsException {
        List<String> result = Lists.newArrayList();

        if (get(settingPrefix) != null) {
            String[] strings = splitStringByCommaToArray(get(settingPrefix));
            if (strings.length > 0) {
                for (String string : strings) {
                    result.add(string.trim());
                }
            }
        }

        int counter = 0;
        while (true) {
            String value = get(settingPrefix + '.' + (counter++));
            if (value == null) {
                break;
            }
            result.add(value.trim());
        }
        if (result.isEmpty()) {
            return defaultArray;
        }
        return result.toArray(new String[result.size()]);
    }

    @Override
    public Map<String, Settings> getGroups(String settingPrefix) throws SettingsException {
        if (settingPrefix.charAt(settingPrefix.length() - 1) != '.') {
            settingPrefix = settingPrefix + "";
        }
        // we don't really care that it might happen twice
        Map<String, Map<String, String>> map = new LinkedHashMap<String, Map<String, String>>();
        for (Object o : settings.keySet()) {
            String setting = (String) o;
            if (setting.startsWith(settingPrefix)) {
                String nameValue = setting.substring(settingPrefix.length());
                int dotIndex = nameValue.indexOf('.');
                if (dotIndex == -1) {
                    throw new SettingsException("Failed to get setting group for [" + settingPrefix + "] setting prefix and setting [" + setting + "] because of a missing '.'");
                }
                String name = nameValue.substring(0, dotIndex);
                String value = nameValue.substring(dotIndex + 1);
                Map<String, String> groupSettings = map.get(name);
                if (groupSettings == null) {
                    groupSettings = new LinkedHashMap<String, String>();
                    map.put(name, groupSettings);
                }
                groupSettings.put(value, get(setting));
            }
        }
        Map<String, Settings> retVal = new LinkedHashMap<String, Settings>();
        for (Map.Entry<String, Map<String, String>> entry : map.entrySet()) {
            retVal.put(entry.getKey(), new ImmutableSettings(Collections.unmodifiableMap(entry.getValue())));
        }
        return Collections.unmodifiableMap(retVal);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ImmutableSettings that = (ImmutableSettings) o;

        if (settings != null ? !settings.equals(that.settings) : that.settings != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return settings != null ? settings.hashCode() : 0;
    }

    public static Settings readSettingsFromStream(StreamInput in) throws IOException {
        Builder builder = new Builder();
        int numberOfSettings = in.readVInt();
        for (int i = 0; i < numberOfSettings; i++) {
            builder.put(in.readString(), in.readString());
        }
        return builder.build();
    }

    public static void writeSettingsToStream(Settings settings, StreamOutput out) throws IOException {
        out.writeVInt(settings.getAsMap().size());
        for (Map.Entry<String, String> entry : settings.getAsMap().entrySet()) {
            out.writeString(entry.getKey());
            out.writeString(entry.getValue());
        }
    }

    public static Settings readSettingsFromMap(Map<String,Object> map) throws IOException {
        Builder builder = new Builder();
        for (String key : map.keySet()) {
            builder.put(key, map.get(key) != null ? map.get(key).toString() : null);
        }
        return builder.build();
    }

    public static void writeSettingsToMap(Settings settings, Map<String,Object> map) throws IOException {
        for (String key : settings.getAsMap().keySet()) {
            map.put(key, settings.get(key));
        }
    }

    /**
     * Returns a builder to be used in order to build settings.
     */
    public static Builder settingsBuilder() {
        return new Builder();
    }

    /**
     * A builder allowing to put different settings and then {@link #build()} an immutable
     * settings implementation. Use {@link ImmutableSettings#settingsBuilder()} in order to
     * construct it.
     */
    public static class Builder implements Settings.Builder {

        public static final Settings EMPTY_SETTINGS = new Builder().build();

        private final Map<String, String> map = new LinkedHashMap<String, String>();

        private Builder() {

        }

        public Map<String, String> internalMap() {
            return this.map;
        }

        /**
         * Removes the provided setting from the internal map holding the current list of settings.
         */
        public String remove(String key) {
            return map.remove(key);
        }

        /**
         * Returns a setting value based on the setting key.
         */
        public String get(String key) {
            String retVal = map.get(key);
            if (retVal != null) {
                return retVal;
            }
            // try camel case version
            return null; //map.get(toCamelCase(key));
        }

        /**
         * Sets a setting with the provided setting key and value.
         *
         * @param key   The setting key
         * @param value The setting value
         * @return The builder
         */
        public Builder put(String key, String value) {
            map.put(key, value);
            return this;
        }

        /**
         * Sets a setting with the provided setting key and class as value.
         *
         * @param key   The setting key
         * @param clazz The setting class value
         * @return The builder
         */
        public Builder put(String key, Class clazz) {
            map.put(key, clazz.getName());
            return this;
        }

        /**
         * Sets the setting with the provided setting key and the boolean value.
         *
         * @param setting The setting key
         * @param value   The boolean value
         * @return The builder
         */
        public Builder put(String setting, boolean value) {
            put(setting, String.valueOf(value));
            return this;
        }

        /**
         * Sets the setting with the provided setting key and the int value.
         *
         * @param setting The setting key
         * @param value   The int value
         * @return The builder
         */
        public Builder put(String setting, int value) {
            put(setting, String.valueOf(value));
            return this;
        }

        /**
         * Sets the setting with the provided setting key and the long value.
         *
         * @param setting The setting key
         * @param value   The long value
         * @return The builder
         */
        public Builder put(String setting, long value) {
            put(setting, String.valueOf(value));
            return this;
        }

        /**
         * Sets the setting with the provided setting key and the float value.
         *
         * @param setting The setting key
         * @param value   The float value
         * @return The builder
         */
        public Builder put(String setting, float value) {
            put(setting, String.valueOf(value));
            return this;
        }

        /**
         * Sets the setting with the provided setting key and the double value.
         *
         * @param setting The setting key
         * @param value   The double value
         * @return The builder
         */
        public Builder put(String setting, double value) {
            put(setting, String.valueOf(value));
            return this;
        }

        /**
         * Sets the setting with the provided setting key and an array of values.
         *
         * @param setting The setting key
         * @param values  The values
         * @return The builder
         */
        public Builder putArray(String setting, String... values) {
            remove(setting);
            int counter = 0;
            while (true) {
                String value = map.remove(setting + '.' + (counter++));
                if (value == null) {
                    break;
                }
            }
            for (int i = 0; i < values.length; i++) {
                put(setting + '.' + i, values[i]);
            }
            return this;
        }

        /**
         * Sets the setting with the provided setting key and an array of values.
         *
         * @param setting The setting key
         * @param values  The values
         * @return The builder
         */
        public Builder putArray(String setting, List<String> values) {
            remove(setting);
            int counter = 0;
            while (true) {
                String value = map.remove(setting + '.' + (counter++));
                if (value == null) {
                    break;
                }
            }
            for (int i = 0; i < values.size(); i++) {
                put(setting + '.' + i, values.get(i).toString());
            }
            return this;
        }

        /**
         * Sets the setting group.
         */
        public Builder put(String settingPrefix, String groupName, String[] settings, String[] values) throws SettingsException {
            if (settings.length != values.length) {
                throw new SettingsException("The settings length must match the value length");
            }
            for (int i = 0; i < settings.length; i++) {
                if (values[i] == null) {
                    continue;
                }
                put(settingPrefix + "" + groupName + "." + settings[i], values[i]);
            }
            return this;
        }

        /**
         * Sets all the provided settings.
         */
        public Builder put(Settings settings) {
            map.putAll(settings.getAsMap());
            return this;
        }

        /**
         * Sets all the provided settings.
         */
        public Builder put(Map<String, String> settings) {
            map.putAll(settings);
            return this;
        }

        /**
         * Sets all the provided settings.
         */
        public Builder put(Properties properties) {
            for (Map.Entry entry : properties.entrySet()) {
                map.put((String) entry.getKey(), (String) entry.getValue());
            }
            return this;
        }

        /**
         * Loads settings from the actual string content that represents them using the
         * {@link SettingsLoaderFactory#loaderFromString(String)}.
         */
        public Builder loadFromString(String source) {
            SettingsLoader settingsLoader = SettingsLoaderFactory.loaderFromString(source);
            try {
                Map<String, String> loadedSettings = settingsLoader.load(source);
                put(loadedSettings);
            } catch (Exception e) {
                throw new SettingsException("Failed to load settings from [" + source + "]", e);
            }
            return this;
        }

        /**
         * Loads settings from a map
         */
        public Builder loadFromMap(Map<String,Object> map) {
            SettingsLoader settingsLoader = new JsonSettingsLoader();
            try {
                Map<String, String> loadedSettings = settingsLoader.load(jsonBuilder().map(map).string());
                put(loadedSettings);
            } catch (Exception e) {
                throw new SettingsException("Failed to load settings from [" + map + "]", e);
            }
            return this;
        }

        /**
         * Loads settings from a url that represents them using the
         * {@link SettingsLoaderFactory#loaderFromString(String)}.
         */
        public Builder loadFromUrl(URL url) throws SettingsException {
            try {
                return loadFromStream(url.toExternalForm(), url.openStream());
            } catch (IOException e) {
                throw new SettingsException("Failed to open stream for url [" + url.toExternalForm() + "]", e);
            }
        }

        /**
         * Loads settings from a stream that represents them using the
         * {@link SettingsLoaderFactory#loaderFromString(String)}.
         */
        public Builder loadFromStream(String resourceName, InputStream is) throws SettingsException {
            SettingsLoader settingsLoader = SettingsLoaderFactory.loaderFromResource(resourceName);
            try {
                Map<String, String> loadedSettings = settingsLoader.load(copyToString(new InputStreamReader(is, "UTF-8")));
                put(loadedSettings);
            } catch (Exception e) {
                throw new SettingsException("Failed to load settings from [" + resourceName + "]", e);
            }
            return this;
        }

        public Builder loadFromReader(Reader reader) throws SettingsException {
            try {
                BufferedReader br = new BufferedReader(reader);
                SettingsLoader settingsLoader = SettingsLoaderFactory.loaderFromReader(br);
                put(settingsLoader.load(copyToString(br)));
                br.close();
            } catch (Exception e) {
                throw new SettingsException("Failed to load settings from reader", e);
            }
            return this;
        }

        /**
         * Puts all the properties with keys starting with the provided <tt>prefix</tt>.
         *
         * @param prefix     The prefix to filter property key by
         * @param properties The properties to put
         * @return The builder
         */
        public Builder putProperties(String prefix, Properties properties) {
            for (Object key1 : properties.keySet()) {
                String key = (String) key1;
                String value = properties.getProperty(key);
                if (key.startsWith(prefix)) {
                    map.put(key.substring(prefix.length()), value);
                }
            }
            return this;
        }

        /**
         * Puts all the properties with keys starting with the provided <tt>prefix</tt>.
         *
         * @param prefix     The prefix to filter property key by
         * @param properties The properties to put
         * @return The builder
         */
        public Builder putProperties(String prefix, Properties properties, String[] ignorePrefixes) {
            for (Object key1 : properties.keySet()) {
                String key = (String) key1;
                String value = properties.getProperty(key);
                if (key.startsWith(prefix)) {
                    boolean ignore = false;
                    for (String ignorePrefix : ignorePrefixes) {
                        if (key.startsWith(ignorePrefix)) {
                            ignore = true;
                            break;
                        }
                    }
                    if (!ignore) {
                        map.put(key.substring(prefix.length()), value);
                    }
                }
            }
            return this;
        }

        /**
         * Runs across all the settings set on this builder and replaces <tt>${...}</tt> elements in the
         * each setting value according to the following logic:
         * <p/>
         * <p>First, tries to resolve it against a System property ({@link System#getProperty(String)}), next,
         * tries and resolve it against an environment variable ({@link System#getenv(String)}), and last, tries
         * and replace it with another setting already set on this builder.
         */
        public Builder replacePropertyPlaceholders() {
            PropertyPlaceholder propertyPlaceholder = new PropertyPlaceholder("${", "}", false);
            PropertyPlaceholder.PlaceholderResolver placeholderResolver = new PropertyPlaceholder.PlaceholderResolver() {
                @Override
                public String resolvePlaceholder(String placeholderName) {
                    String value = System.getProperty(placeholderName);
                    if (value != null) {
                        return value;
                    }
                    value = System.getenv(placeholderName);
                    if (value != null) {
                        return value;
                    }
                    return map.get(placeholderName);
                }
            };
            for (Map.Entry<String, String> entry : map.entrySet()) {
                map.put(entry.getKey(), propertyPlaceholder.replacePlaceholders(entry.getValue(), placeholderResolver));
            }
            return this;
        }

        /**
         * Builds a {@link Settings} (underlying uses {@link ImmutableSettings}) based on everything
         * set on this builder.
         */
        public Settings build() {
            return new ImmutableSettings(Collections.unmodifiableMap(map));
        }
    }
    
    public static final String[] EMPTY_ARRAY = new String[0];
    
    
    public static String[] splitStringByCommaToArray(final String s) {
        return splitStringToArray(s, ',');
    }
    
    public static String[] splitStringToArray(final String s, final char c) {
        if (s.length() == 0) {
            return EMPTY_ARRAY;
        }
        final char[] chars = s.toCharArray();
        int count = 1;
        for (final char x : chars) {
            if (x == c) {
                count++;
            }
        }
        final String[] result = new String[count];
        final int len = chars.length;
        int start = 0;  // starting index in chars of the current substring.
        int pos = 0;    // current index in chars.
        int i = 0;      // number of the current substring.
        for (; pos < len; pos++) {
            if (chars[pos] == c) {
                int size = pos - start;
                if (size > 0) {
                    result[i++] = new String(chars, start, size);
                }
                start = pos + 1;
            }
        }
        int size = pos - start;
        if (size > 0) {
            result[i++] = new String(chars, start, size);
        }
        if (i != count) {
            // we have empty strings, copy over to a new array
            String[] result1 = new String[i];
            System.arraycopy(result, 0, result1, 0, i);
            return result1;
        }
        return result;
    }    

    public static final int BUFFER_SIZE = 1024 * 8;
 
    public static String copyToString(Reader in) throws IOException {
        StringWriter out = new StringWriter();
        copy(in, out);
        return out.toString();
    }    
    
    public static int copy(Reader in, Writer out) throws IOException {
        try {
            int byteCount = 0;
            char[] buffer = new char[BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
                byteCount += bytesRead;
            }
            out.flush();
            return byteCount;
        } finally {
            try {
                in.close();
            } catch (IOException ex) {
                // do nothing
            }
            try {
                out.close();
            } catch (IOException ex) {
                // do nothing
            }
        }
    }

    private static class PropertyPlaceholder {

        private final String placeholderPrefix;

        private final String placeholderSuffix;

        private final boolean ignoreUnresolvablePlaceholders;

        /**
         * Creates a new <code>PropertyPlaceholderHelper</code> that uses the supplied prefix and suffix.
         *
         * @param placeholderPrefix              the prefix that denotes the start of a placeholder.
         * @param placeholderSuffix              the suffix that denotes the end of a placeholder.
         * @param ignoreUnresolvablePlaceholders indicates whether unresolvable placeholders should be ignored
         *                                       (<code>true</code>) or cause an exception (<code>false</code>).
         */
        public PropertyPlaceholder(String placeholderPrefix, String placeholderSuffix,
                                   boolean ignoreUnresolvablePlaceholders) {
            this.placeholderPrefix = placeholderPrefix;
            this.placeholderSuffix = placeholderSuffix;
            this.ignoreUnresolvablePlaceholders = ignoreUnresolvablePlaceholders;
        }


        /**
         * Replaces all placeholders of format <code>${name}</code> with the value returned from the supplied {@link
         * PlaceholderResolver}.
         *
         * @param value               the value containing the placeholders to be replaced.
         * @param placeholderResolver the <code>PlaceholderResolver</code> to use for replacement.
         * @return the supplied value with placeholders replaced inline.
         */
        public String replacePlaceholders(String value, PlaceholderResolver placeholderResolver) {
            return parseStringValue(value, placeholderResolver, new HashSet<String>());
        }

        protected String parseStringValue(String strVal, PlaceholderResolver placeholderResolver,
                                          Set<String> visitedPlaceholders) {
            StringBuilder buf = new StringBuilder(strVal);
            int startIndex = strVal.indexOf(this.placeholderPrefix);
            while (startIndex != -1) {
                int endIndex = findPlaceholderEndIndex(buf, startIndex);
                if (endIndex != -1) {
                    String placeholder = buf.substring(startIndex + this.placeholderPrefix.length(), endIndex);
                    if (!visitedPlaceholders.add(placeholder)) {
                        throw new IllegalArgumentException(
                                "Circular placeholder reference '" + placeholder + "' in property definitions");
                    }
                    // Recursive invocation, parsing placeholders contained in the placeholder key.
                    placeholder = parseStringValue(placeholder, placeholderResolver, visitedPlaceholders);

                    // Now obtain the value for the fully resolved key...
                    int defaultValueIdx = placeholder.indexOf(':');
                    String defaultValue = null;
                    if (defaultValueIdx != -1) {
                        defaultValue = placeholder.substring(defaultValueIdx + 1);
                        placeholder = placeholder.substring(0, defaultValueIdx);
                    }
                    String propVal = placeholderResolver.resolvePlaceholder(placeholder);
                    if (propVal == null) {
                        propVal = defaultValue;
                    }
                    if (propVal != null) {
                        // Recursive invocation, parsing placeholders contained in the
                        // previously resolved placeholder value.
                        propVal = parseStringValue(propVal, placeholderResolver, visitedPlaceholders);
                        buf.replace(startIndex, endIndex + this.placeholderSuffix.length(), propVal);
                        startIndex = buf.indexOf(this.placeholderPrefix, startIndex + propVal.length());
                    } else if (this.ignoreUnresolvablePlaceholders) {
                        // Proceed with unprocessed value.
                        startIndex = buf.indexOf(this.placeholderPrefix, endIndex + this.placeholderSuffix.length());
                    } else {
                        throw new IllegalArgumentException("Could not resolve placeholder '" + placeholder + "'");
                    }
                    visitedPlaceholders.remove(placeholder);
                } else {
                    startIndex = -1;
                }
            }
            return buf.toString();
        }

        private int findPlaceholderEndIndex(CharSequence buf, int startIndex) {
            int index = startIndex + this.placeholderPrefix.length();
            int withinNestedPlaceholder = 0;
            while (index < buf.length()) {
                if (substringMatch(buf, index, this.placeholderSuffix)) {
                    if (withinNestedPlaceholder > 0) {
                        withinNestedPlaceholder--;
                        index = index + this.placeholderPrefix.length() - 1;
                    } else {
                        return index;
                    }
                } else if (substringMatch(buf, index, this.placeholderPrefix)) {
                    withinNestedPlaceholder++;
                    index = index + this.placeholderPrefix.length();
                } else {
                    index++;
                }
            }
            return -1;
        }

        private boolean substringMatch(CharSequence str, int index, CharSequence substring) {
            for (int j = 0; j < substring.length(); j++) {
                int i = index + j;
                if (i >= str.length() || str.charAt(i) != substring.charAt(j)) {
                    return false;
                }
            }
            return true;
        }

        /**
         * Strategy interface used to resolve replacement values for placeholders contained in Strings.
         */
        public static interface PlaceholderResolver {

            /**
             * Resolves the supplied placeholder name into the replacement value.
             *
             * @param placeholderName the name of the placeholder to resolve.
             * @return the replacement value or <code>null</code> if no replacement is to be made.
             */
            String resolvePlaceholder(String placeholderName);
        }
    }
}
