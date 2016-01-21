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
package org.xbib.util;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * A finder for traversing PathFiles
 */
public class Finder {

    private final EnumSet<FileVisitOption> opts;

    private List<PathFile> input = new LinkedList<>();

    private FileTime modifiedSince;

    private Comparator<PathFile> comparator;

    public Finder() {
        this(EnumSet.of(FileVisitOption.FOLLOW_LINKS));
    }

    public Finder(EnumSet<FileVisitOption> opts) {
        this.opts = opts;
    }

    public Finder modifiedSince(long modifiedSince, TimeUnit tu) {
        this.modifiedSince = FileTime.from(modifiedSince, tu);
        return this;
    }

    public Finder find(String path, String pattern) throws IOException {
        return find(null, null, Paths.get(path), pattern);
    }

    public Finder find(String base, String basePattern, String path, String pattern) throws IOException {
        return find(Strings.isNullOrEmpty(base) ? null : Paths.get(base), basePattern,
                Strings.isNullOrEmpty(path) ? null : Paths.get(path), pattern);
    }


    /**
     * Find the most recent version of a file/archive.
     *
     * @param base the path of the base directory
     * @param basePattern a pattern to match directory entries in the base directory or null to match '*'
     * @param path the path of the file/archive if no recent path can be found in the base directory
     * @param pattern  th file name pattern to match
     * @return this Finder
     * @throws IOException
     */
    public Finder find(Path base, String basePattern, Path path, String pattern) throws IOException {
        if (base != null) {
            final PathMatcher baseMatcher = base.getFileSystem().getPathMatcher("glob:" + (basePattern != null ? basePattern : "*"));
            Set<Path> recent = new TreeSet<>((p1, p2) -> p2.toString().compareTo(p1.toString()));
            List<Path> dir = Files.find(base, 1,
                    (p,a) -> p.toFile().isDirectory() && baseMatcher.matches(p.getFileName()),
                    FileVisitOption.FOLLOW_LINKS)
                    .collect(Collectors.toList());
            recent.addAll(dir);
            if (recent.isEmpty()) {
                return this;
            }
            path = recent.iterator().next();
        }
        PathMatcher pathMatcher = path.getFileSystem().getPathMatcher("glob:" + (pattern != null ? pattern : "*"));
        Files.walkFileTree(path, opts, Integer.MAX_VALUE, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) {
                if (pathMatcher.matches(path.getFileName())) {
                    if (modifiedSince == null || attrs.lastModifiedTime().toMillis() > modifiedSince.toMillis()) {
                        input.add(new PathFile(path, attrs));
                    }
                }
                return FileVisitResult.CONTINUE;
            }
        });
        return this;
    }

    public Finder sortBy(String mode) {
        if ("lastmodified".equals(mode)) {
            this.comparator = (p1, p2) -> p1.getAttributes().lastModifiedTime().compareTo(p2.getAttributes().lastModifiedTime());
        } else if ("name".equals(mode)) {
            this.comparator = (p1, p2) -> p1.getPath().toString().compareTo(p2.getPath().toString());
        }
        return this;
    }

    public Finder order(String mode) {
        if ("desc".equals(mode)) {
            this.comparator = Collections.reverseOrder(comparator);
        }
        return this;
    }

    public Queue<PathFile> getPathFiles() {
        if (comparator != null) {
            Collections.sort(input, comparator);
        }
        return new ConcurrentLinkedQueue<>(input);
    }

    public Queue<URI> getURIs() {
        return getPathFiles()
                .stream()
                .map(p -> p.getPath().toAbsolutePath().toUri())
                .collect(Collectors.toCollection(ConcurrentLinkedQueue::new));
    }

}