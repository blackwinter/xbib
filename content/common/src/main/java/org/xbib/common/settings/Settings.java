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
import com.google.common.collect.Maps;
import org.xbib.common.Strings;
import org.xbib.common.settings.loader.JsonSettingsLoader;
import org.xbib.common.settings.loader.SettingsLoader;
import org.xbib.common.settings.loader.SettingsLoaderFactory;
import org.xbib.common.unit.ByteSizeUnit;
import org.xbib.common.unit.ByteSizeValue;
import org.xbib.common.unit.MemorySizeValue;
import org.xbib.common.unit.RatioValue;
import org.xbib.common.unit.SizeValue;
import org.xbib.common.unit.TimeValue;
import org.xbib.common.xcontent.ToXContent;
import org.xbib.common.xcontent.XContentBuilder;
import org.xbib.io.stream.StreamInput;
import org.xbib.io.stream.StreamOutput;
import org.xbib.io.stream.Streams;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static org.xbib.common.unit.TimeValue.parseTimeValue;
import static org.xbib.common.xcontent.XContentFactory.jsonBuilder;

/**
 * An immutable settings implementation.
 */
public final class Settings implements ToXContent {

    public static final Settings EMPTY = new Builder().build();

    public static final String[] EMPTY_ARRAY = new String[0];

    private ImmutableMap<String, String> settings;

    private transient ClassLoader classLoader;

    Settings(Map<String, String> settings, ClassLoader classLoader) {
        this.settings = ImmutableMap.copyOf(settings);
        Map<String, String> forcedUnderscoreSettings = null;
        for (Map.Entry<String, String> entry : settings.entrySet()) {
            String toUnderscoreCase = Strings.toUnderscoreCase(entry.getKey());
            if (!toUnderscoreCase.equals(entry.getKey())) {
                if (forcedUnderscoreSettings == null) {
                    forcedUnderscoreSettings = new HashMap<>();
                }
                forcedUnderscoreSettings.put(toUnderscoreCase, entry.getValue());
            }
        }
        this.classLoader = classLoader;
    }

    public ClassLoader getClassLoader() {
        return this.classLoader == null ? ClassLoader.getSystemClassLoader() : classLoader;
    }

    public ImmutableMap<String, String> getAsMap() {
        return this.settings;
    }

    public Map<String, Object> getAsStructuredMap() {
        Map<String, Object> map = Maps.newHashMapWithExpectedSize(2);
        for (Map.Entry<String, String> entry : settings.entrySet()) {
            processSetting(map, "", entry.getKey(), entry.getValue());
        }
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (entry.getValue() instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> valMap = (Map<String, Object>) entry.getValue();
                entry.setValue(convertMapsToArrays(valMap));
            }
        }
        return map;
    }

    public Reader getAsReader() {
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

    public Settings getByPrefix(String prefix) {
        Builder builder = new Builder();
        for (Map.Entry<String, String> entry : getAsMap().entrySet()) {
            if (entry.getKey().startsWith(prefix)) {
                if (entry.getKey().length() < prefix.length()) {
                    // ignore this. one
                    continue;
                }
                builder.put(entry.getKey().substring(prefix.length()), entry.getValue());
            }
        }
        builder.classLoader(classLoader);
        return builder.build();
    }

    public Settings getAsSettings(String setting) {
        return getByPrefix(setting + ".");
    }

    public String get(String setting) {
        return settings.get(setting);
    }

    public String get(String[] settings) {
        for (String setting : settings) {
            String retVal = get(setting);
            if (retVal != null) {
                return retVal;
            }
        }
        return null;
    }

    public String get(String setting, String defaultValue) {
        String retVal = get(setting);
        return retVal == null ? defaultValue : retVal;
    }

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

    public Boolean getAsBoolean(String setting, Boolean defaultValue) {
        String value = get(setting);
        if (value == null) {
            return defaultValue;
        }
        return !(value.equals("false") || value.equals("0") || value.equals("off") || value.equals("no"));
    }

    public TimeValue getAsTime(String setting, TimeValue defaultValue) {
        return parseTimeValue(get(setting), defaultValue);
    }

    public ByteSizeValue getAsByteSize(String setting, String defaultValue) throws SettingsException {
        try {
            return MemorySizeValue.parseBytesSizeValueOrHeapRatio(get(setting, defaultValue));
        } catch (ParseException e) {
            throw new SettingsException("can not parse value", e);
        }
    }

    public ByteSizeValue getAsByteSize(String setting, ByteSizeValue defaultValue) throws SettingsException {
        return ByteSizeValue.parseBytesSizeValue(get(setting), defaultValue);
    }

    public RatioValue getAsRatio(String setting, String defaultValue) throws SettingsException {
        try {
            return RatioValue.parseRatioValue(get(setting, defaultValue));
        } catch (ParseException e) {
            throw new SettingsException("can not parse value", e);
        }
    }

    public SizeValue getAsSize(String setting, SizeValue defaultValue) throws SettingsException {
        try {
            return SizeValue.parseSizeValue(get(setting), defaultValue);
        } catch (ParseException e) {
            throw new SettingsException("can not parse value", e);
        }
    }

    public String[] getAsArray(String settingPrefix) throws SettingsException {
        return getAsArray(settingPrefix, EMPTY_ARRAY);
    }

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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Settings that = (Settings) o;

        if (classLoader != null ? !classLoader.equals(that.classLoader) : that.classLoader != null) return false;
        if (settings != null ? !settings.equals(that.settings) : that.settings != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = settings != null ? settings.hashCode() : 0;
        result = 31 * result + (classLoader != null ? classLoader.hashCode() : 0);
        return result;
    }

    public static Settings readFrom(StreamInput in) throws IOException {
        Builder builder = new Builder();
        int numberOfSettings = in.readVInt();
        for (int i = 0; i < numberOfSettings; i++) {
            builder.put(in.readString(), in.readString());
        }
        return builder.build();
    }

    public static void writeTo(Settings settings, StreamOutput out) throws IOException {
        out.writeVInt(settings.getAsMap().size());
        for (Map.Entry<String, String> entry : settings.getAsMap().entrySet()) {
            out.writeString(entry.getKey());
            out.writeString(entry.getValue());
        }
    }

    public static Settings readFrom(Map<String,Object> map) throws IOException {
        Builder builder = new Builder();
        for (String key : map.keySet()) {
            builder.put(key, map.get(key) != null ? map.get(key).toString() : null);
        }
        return builder.build();
    }

    public static void writeTo(Settings settings, Map<String,Object> map) throws IOException {
        for (String key : settings.getAsMap().keySet()) {
            map.put(key, settings.get(key));
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder settingsBuilder() {
        return new Builder();
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        if (!params.paramAsBoolean("flat_settings", false)) {
            for (Map.Entry<String, Object> entry : getAsStructuredMap().entrySet()) {
                builder.field(entry.getKey(), entry.getValue());
            }
        } else {
            for (Map.Entry<String, String> entry : getAsMap().entrySet()) {
                builder.field(entry.getKey(), entry.getValue(), XContentBuilder.FieldCaseConversion.NONE);
            }
        }
        return builder;
    }

    private void processSetting(Map<String, Object> map, String prefix, String setting, String value) {
        int prefixLength = setting.indexOf('.');
        if (prefixLength == -1) {
            @SuppressWarnings("unchecked")
            Map<String, Object> innerMap = (Map<String, Object>) map.get(prefix + setting);
            if (innerMap != null) {
                for (Map.Entry<String, Object> entry : innerMap.entrySet()) {
                    map.put(prefix + setting + "." + entry.getKey(), entry.getValue());
                }
            }
            map.put(prefix + setting, value);
        } else {
            String key = setting.substring(0, prefixLength);
            String rest = setting.substring(prefixLength + 1);
            Object existingValue = map.get(prefix + key);
            if (existingValue == null) {
                Map<String, Object> newMap = Maps.newHashMapWithExpectedSize(2);
                processSetting(newMap, "", rest, value);
                map.put(key, newMap);
            } else {
                if (existingValue instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> innerMap = (Map<String, Object>) existingValue;
                    processSetting(innerMap, "", rest, value);
                    map.put(key, innerMap);
                } else {
                    processSetting(map, prefix + key + ".", rest, value);
                }
            }
        }
    }

    private Object convertMapsToArrays(Map<String, Object> map) {
        if (map.isEmpty()) {
            return map;
        }
        boolean isArray = true;
        int maxIndex = -1;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (isArray) {
                try {
                    int index = Integer.parseInt(entry.getKey());
                    if (index >= 0) {
                        maxIndex = Math.max(maxIndex, index);
                    } else {
                        isArray = false;
                    }
                } catch (NumberFormatException ex) {
                    isArray = false;
                }
            }
            if (entry.getValue() instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> valMap = (Map<String, Object>) entry.getValue();
                entry.setValue(convertMapsToArrays(valMap));
            }
        }
        if (isArray && (maxIndex + 1) == map.size()) {
            ArrayList<Object> newValue = Lists.newArrayListWithExpectedSize(maxIndex + 1);
            for (int i = 0; i <= maxIndex; i++) {
                Object obj = map.get(Integer.toString(i));
                if (obj == null) {
                    return map;
                }
                newValue.add(obj);
            }
            return newValue;
        }
        return map;
    }

    private String[] splitStringByCommaToArray(final String s) {
        return splitStringToArray(s, ',');
    }

    private String[] splitStringToArray(final String s, final char c) {
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

    public static class Builder {

        private final Map<String, String> map = new LinkedHashMap<>();

        private ClassLoader classLoader;

        private Builder() {
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
            return map.get(key);
        }

        /**
         * Puts tuples of key value pairs of settings. Simplified version instead of repeating calling
         * put for each one.
         */
        public Builder put(Object... settings) {
            if (settings.length == 1) {
                // support cases where the actual type gets lost down the road...
                if (settings[0] instanceof Map) {
                    //noinspection unchecked
                    return put((Map) settings[0]);
                } else if (settings[0] instanceof Settings) {
                    return put((Settings) settings[0]);
                }
            }
            if ((settings.length % 2) != 0) {
                throw new IllegalArgumentException("array settings of key + value order doesn't hold correct number of arguments (" + settings.length + ")");
            }
            for (int i = 0; i < settings.length; i++) {
                put(settings[i++].toString(), settings[i].toString());
            }
            return this;
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
         * Sets the setting with the provided setting key and the time value.
         *
         * @param setting The setting key
         * @param value   The time value
         * @return The builder
         */
        public Builder put(String setting, long value, TimeUnit timeUnit) {
            put(setting, timeUnit.toMillis(value) + "ms");
            return this;
        }

        /**
         * Sets the setting with the provided setting key and the size value.
         *
         * @param setting The setting key
         * @param value   The size value
         * @return The builder
         */
        public Builder put(String setting, long value, ByteSizeUnit sizeUnit) {
            put(setting, sizeUnit.toBytes(value) + "b");
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
                put(setting + "." + i, values[i]);
            }
            return this;
        }

        /**
         * Sets the setting with the provided setting key and a list of values.
         *
         * @param setting The setting key
         * @param values  The values
         * @return The builder
         */
        public Builder putList(String setting, List<String> values) {
            remove(setting);
            int counter = 0;
            while (true) {
                String value = map.remove(setting + '.' + (counter++));
                if (value == null) {
                    break;
                }
            }
            for (int i = 0; i < values.size(); i++) {
                put(setting + '.' + i, values.get(i));
            }
            return this;
        }

        /**
         * Sets the setting group
         * @param settingPrefix setting prefix
         * @param groupName group name
         * @param settings settings
         * @param values values
         */
        public Builder put(String settingPrefix, String groupName, String[] settings, String[] values) throws SettingsException {
            if (settings.length != values.length) {
                throw new SettingsException("The settings length must match the value length");
            }
            for (int i = 0; i < settings.length; i++) {
                if (values[i] == null) {
                    continue;
                }
                put(settingPrefix + "." + groupName + "." + settings[i], values[i]);
            }
            return this;
        }

        /**
         * Sets all the provided settings
         * @param settings settings
         */
        public Builder put(Settings settings) {
            map.putAll(settings.getAsMap());
            classLoader = settings.getClassLoader();
            return this;
        }

        /**
         * Sets all the provided settings
         * @param settings settings
         */
        public Builder put(Map<String, String> settings) {
            map.putAll(settings);
            return this;
        }

        /**
         * Sets all the provided settings
         */
        public Builder put(Properties properties) {
            for (Map.Entry entry : properties.entrySet()) {
                map.put((String) entry.getKey(), (String) entry.getValue());
            }
            return this;
        }

        public Builder loadFromDelimitedString(String value, char delimiter) {
            String[] values = Strings.splitStringToArray(value, delimiter);
            for (String s : values) {
                int index = s.indexOf('=');
                if (index == -1) {
                    throw new IllegalArgumentException("value [" + s + "] for settings loaded with delimiter [" + delimiter + "] is malformed, missing =");
                }
                map.put(s.substring(0, index), s.substring(index + 1));
            }
            return this;
        }

        /**
         * Loads settings from the actual string content that represents them using the
         * {@link SettingsLoaderFactory#loaderFromString(String)}.
         */
        public Builder loadFrom(String source) {
            SettingsLoader settingsLoader = SettingsLoaderFactory.loaderFromString(source);
            try {
                Map<String, String> loadedSettings = settingsLoader.load(source);
                put(loadedSettings);
            } catch (Exception e) {
                throw new SettingsException("Failed to load settings from [" + source + "]", e);
            }
            return this;
        }

        public Builder loadFrom(Map<String,Object> map) {
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
        public Builder loadFrom(URL url) throws SettingsException {
            try {
                return loadFrom(url.toExternalForm(), url.openStream());
            } catch (IOException e) {
                throw new SettingsException("Failed to open stream for url [" + url.toExternalForm() + "]", e);
            }
        }

        /**
         * Loads settings from a path.
         */
        public Builder loadFrom(Path path) throws SettingsException {
            try {
                return loadFrom(path.getFileName().toString(), Files.newInputStream(path));
            } catch (IOException e) {
                throw new SettingsException("Failed to open stream for url [" + path + "]", e);
            }
        }

        public Builder loadFrom(String resourceName, InputStream in) throws SettingsException {
            SettingsLoader settingsLoader = SettingsLoaderFactory.loaderFromResource(resourceName);
            try {
                Map<String, String> loadedSettings = settingsLoader.load(Streams.copyToString(new InputStreamReader(in, "UTF-8")));
                put(loadedSettings);
            } catch (Exception e) {
                throw new SettingsException("Failed to load settings from [" + resourceName + "]", e);
            }
            return this;
        }

        public Builder loadFrom(InputStream in, String encoding) throws SettingsException {
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(in, encoding));
                SettingsLoader settingsLoader = SettingsLoaderFactory.loaderFromReader(br);
                put(settingsLoader.load(Streams.copyToString(br)));
                br.close();
            } catch (Exception e) {
                throw new SettingsException("Failed to load settings from inputstream", e);
            }
            return this;
        }

        public Builder loadFrom(Reader reader) throws SettingsException {
            try {
                BufferedReader br = new BufferedReader(reader);
                SettingsLoader settingsLoader = SettingsLoaderFactory.loaderFromReader(br);
                put(settingsLoader.load(Streams.copyToString(br)));
                br.close();
            } catch (Exception e) {
                throw new SettingsException("Failed to load settings from reader", e);
            }
            return this;
        }

        /**
         * Loads settings from classpath.
         */
        public Builder loadFromClasspath(String resourceName) throws SettingsException {
            InputStream in = classLoader.getResourceAsStream(resourceName);
            if (in == null) {
                return this;
            }
            return loadFrom(resourceName, in);
        }

        /**
         * Sets the class loader associated with the settings built.
         */
        public Builder classLoader(ClassLoader classLoader) {
            this.classLoader = classLoader;
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
         *
         * First, tries to resolve it against a System property ({@link System#getProperty(String)}), next,
         * tries and resolve it against an environment variable ({@link System#getenv(String)}), and last, tries
         * and replace it with another setting already set on this builder.
         */
        public Builder replacePropertyPlaceholders() {
            PropertyPlaceholder propertyPlaceholder = new PropertyPlaceholder("${", "}", false);
            PropertyPlaceholder.PlaceholderResolver placeholderResolver = new PropertyPlaceholder.PlaceholderResolver() {
                @Override
                public String resolvePlaceholder(String placeholderName) {
                    if (placeholderName.startsWith("env.")) {
                        // explicit env var prefix
                        return System.getenv(placeholderName.substring("env.".length()));
                    }
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

                @Override
                public boolean shouldIgnoreMissing(String placeholderName) {
                    return placeholderName.startsWith("env.") || placeholderName.startsWith("prompt.");
                }

                @Override
                public boolean shouldRemoveMissingPlaceholder(String placeholderName) {
                    return !placeholderName.startsWith("prompt.");
                }
            };
            for (Map.Entry<String, String> entry : Maps.newHashMap(map).entrySet()) {
                String value = propertyPlaceholder.replacePlaceholders(entry.getValue(), placeholderResolver);
                if (Strings.hasLength(value)) {
                    map.put(entry.getKey(), value);
                } else {
                    map.remove(entry.getKey());
                }
            }
            return this;
        }

        /**
         * Checks that all settings in the builder start with the specified prefix.
         *
         * If a setting doesn't start with the prefix, the builder appends the prefix to such setting.
         */
        public Builder normalizePrefix(String prefix) {
            Map<String, String> replacements = Maps.newHashMap();
            Iterator<Map.Entry<String, String>> iterator = map.entrySet().iterator();
            while(iterator.hasNext()) {
                Map.Entry<String, String> entry = iterator.next();
                if (!entry.getKey().startsWith(prefix)) {
                    replacements.put(prefix + entry.getKey(), entry.getValue());
                    iterator.remove();
                }
            }
            map.putAll(replacements);
            return this;
        }

        /**
         * Builds a {@link Settings} (underlying uses {@link Settings}) based on everything
         * set on this builder.
         */
        public Settings build() {
            return new Settings(Collections.unmodifiableMap(map), classLoader);
        }
    }
}
