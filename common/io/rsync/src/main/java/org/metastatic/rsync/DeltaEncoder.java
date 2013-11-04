/* DeltaEncoder -- encodes Delta objects to external representations.
   Copyright (C) 2003, 2007  Casey Marshall <rsdio@metastatic.org>

This file is a part of Jarsync.

Jarsync is free software; you can redistribute it and/or modify it
under the terms of the GNU General Public License as published by the
Free Software Foundation; either version 2 of the License, or (at your
option) any later version.

Jarsync is distributed in the hope that it will be useful, but WITHOUT
ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
for more details.

You should have received a copy of the GNU General Public License
along with Jarsync; if not, write to the Free Software Foundation,
Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.

Linking Jarsync statically or dynamically with other modules is making
a combined work based on Jarsync.  Thus, the terms and conditions of
the GNU General Public License cover the whole combination.

As a special exception, the copyright holders of Jarsync give you
permission to link Jarsync with independent modules to produce an
executable, regardless of the license terms of these independent
modules, and to copy and distribute the resulting executable under
terms of your choice, provided that you also meet, for each linked
independent module, the terms and conditions of the license of that
module.  An independent module is a module which is not derived from
or based on Jarsync.  If you modify Jarsync, you may extend this
exception to your version of it, but you are not obligated to do so.
If you do not wish to do so, delete this exception statement from your
version.

ALTERNATIVELY, Jarsync may be licensed under the Apache License,
Version 2.0 (the "License"); you may not use this file except in
compliance with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
implied.  See the License for the specific language governing
permissions and limitations under the License.

If you modify Jarsync, you may extend this same choice of license for
your library, but you are not obligated to do so. If you do not offer
the same license terms, delete the license terms that your library is
NOT licensed under.  */


package org.metastatic.rsync;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * The superclass of objects that encode sets of deltas to external
 * representations, such as the over-the-wire format of rsync or the
 * rdiff file format.
 * <p/>
 * <p>Subclasses MAY define themselves to be accessable through the
 * {@link #getInstance(String, Configuration, java.io.OutputStream)} method
 * by providing a one-argument constructor that accepts an {@link
 * java.io.OutputStream} and defining the system property
 * "jarsync.deltaEncoder.<i>encoding-name</i>".
 */
public abstract class DeltaEncoder {

    public static final String PROPERTY = "jarsync.deltaEncoder.";

    /**
     * The configuration.
     */
    protected Configuration config;

    /**
     * The output stream.
     */
    protected OutputStream out;

    /**
     * Creates a new delta encoder.
     *
     * @param config The configuration.
     * @param out    The output stream to write the data to.
     */
    public DeltaEncoder(Configuration config, OutputStream out) {
        this.config = config;
        this.out = out;
    }

    /**
     * Returns a new instance of the specified encoder.
     *
     * @throws IllegalArgumentException If there is no appropriate
     *                                  encoder available.
     */
    public static final DeltaEncoder getInstance(String encoding,
                                                 Configuration config,
                                                 OutputStream out) {
        if (encoding == null || config == null || out == null) {
            throw new NullPointerException();
        }
        if (encoding.length() == 0) {
            throw new IllegalArgumentException();
        }
        try {
            Class clazz = Class.forName(System.getProperty(PROPERTY + encoding));
            if (!DeltaEncoder.class.isAssignableFrom(clazz)) {
                throw new IllegalArgumentException(clazz.getName() +
                        ": not a subclass of " +
                        DeltaEncoder.class.getName());
            }
            Constructor c = clazz.getConstructor(new Class[]{Configuration.class,
                    OutputStream.class});
            return (DeltaEncoder) c.newInstance(new Object[]{config, out});
        } catch (ClassNotFoundException cnfe) {
            throw new IllegalArgumentException("class not found: " +
                    cnfe.getMessage());
        } catch (NoSuchMethodException nsme) {
            throw new IllegalArgumentException("subclass has no constructor");
        } catch (InvocationTargetException ite) {
            throw new IllegalArgumentException(ite.getMessage());
        } catch (InstantiationException ie) {
            throw new IllegalArgumentException(ie.getMessage());
        } catch (IllegalAccessException iae) {
            throw new IllegalArgumentException(iae.getMessage());
        }
    }

    /**
     * Write (encode) a list of deltas to the output stream. This method does
     * <b>not</b> call {@link #doFinal()}.
     * <p/>
     * <p>This method checks every element of the supplied list to ensure that
     * all are either non-null or implement the {@link org.metastatic.rsync.Delta} interface, before
     * writing any data.
     *
     * @param deltas The list of deltas to write.
     * @throws java.io.IOException      If an I/O error occurs.
     * @throws IllegalArgumentException If any element of the list is not
     *                                  a {@link org.metastatic.rsync.Delta}.
     * @throws NullPointerException     If any element is null.
     */
    public void write(List<Delta> deltas) throws IOException {
        for (Delta delta : deltas) {
            write(delta);
        }
    }

    /**
     * Write (encode) a single delta to the output stream.
     *
     * @param d The delta to write.
     * @throws java.io.IOException If an I/O error occurs.
     */
    public abstract void write(Delta d) throws IOException;

    /**
     * Finish encoding the deltas (at least, this set of deltas) and write any
     * encoding-specific end-of-deltas entity.
     *
     * @throws java.io.IOException If an I/O error occurs.
     */
    public abstract void doFinal() throws IOException;


}
