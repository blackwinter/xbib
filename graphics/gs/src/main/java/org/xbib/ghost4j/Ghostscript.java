package org.ghost4j;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import org.apache.logging.log4j.Level;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class Ghostscript {

    public static final String PROPERTY_NAME_ENCODING = "ghost4j.encoding";
    private static GhostscriptLibrary.gs_main_instance.ByReference nativeInstanceByRef;
    private static Ghostscript instance;
    private static InputStream stdIn;
    private static OutputStream stdOut;
    private static OutputStream stdErr;
    private static DisplayCallback displayCallback;
    private static DisplayData displayData;
    private static GhostscriptLibrary.display_callback_s nativeDisplayCallback;

    private Ghostscript() {
    }

    public static synchronized Ghostscript getInstance() {
        if (instance == null) {
            instance = new Ghostscript();
        }
        return instance;
    }

    /**
     * Gets Ghostscript revision data.
     *
     * @return Revision data.
     */
    public static GhostscriptRevision getRevision() {
        GhostscriptLibrary.gsapi_revision_s revision = new GhostscriptLibrary.gsapi_revision_s();
        GhostscriptLibrary.instance.gsapi_revision(revision, revision.size());
        GhostscriptRevision result = new GhostscriptRevision();
        result.setProduct(revision.product);
        result.setCopyright(revision.copyright);
        result.setNumber(Float.toString(revision.revision.floatValue() / 100));
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            result.setRevisionDate(sdf.parse(revision.revisiondate.toString()));
        } catch (ParseException e) {
            result.setRevisionDate(null);
        }
        return result;

    }

    /**
     * Deletes the singleton instance of the Ghostscript object. This ensures
     * that the native Ghostscript interpreter instance is deleted. This method
     * must be called if Ghostscript is not used anymore.
     *
     * @throws org.ghost4j.GhostscriptException
     */
    public static synchronized void deleteInstance() throws GhostscriptException {
        if (instance != null) {
            instance = null;
        }
        if (nativeInstanceByRef != null) {
            GhostscriptLibrary.instance.gsapi_delete_instance(nativeInstanceByRef.getValue());
            nativeInstanceByRef = null;
        }
    }

    /**
     * Gets the display callback set on the Ghostscript interpreter (may be null
     * if not set).
     *
     * @return The DisplayCallback or null
     */
    public synchronized DisplayCallback getDisplayCallback() {
        return displayCallback;
    }

    /**
     * Sets a display callback for the Ghostscript interpreter.
     *
     * @param displayCallback DisplayCallback object
     */
    public synchronized void setDisplayCallback(DisplayCallback displayCallback) {
        this.displayCallback = displayCallback;
    }

    /**
     * Gets the error output stream of the Ghostscript interpreter (may be null
     * if not set).
     *
     * @return The OutputStream or null
     */
    public synchronized OutputStream getStdErr() {
        return stdErr;
    }

    /**
     * Sets the error output stream of the Ghostscript interpreter.
     *
     * @param stdErr OutputStream object
     */
    public synchronized void setStdErr(OutputStream stdErr) {
        this.stdErr = stdErr;
    }

    /**
     * Gets the standard output stream of the Ghostscript interpreter (may be
     * null if not set).
     *
     * @return The OutputStream or null
     */
    public synchronized OutputStream getStdOut() {
        return stdOut;
    }

    /**
     * Sets the standard output stream of the Ghostscript interpreter.
     *
     * @param stdOut OutputStream object
     */
    public synchronized void setStdOut(OutputStream stdOut) {
        this.stdOut = stdOut;
    }

    /**
     * Gets the standard input stream of the Ghostscript interpreter (may be
     * null if not set).
     *
     * @return The InputStream or null
     */
    public synchronized InputStream getStdIn() {
        return stdIn;
    }

    /**
     * Sets the standard input stream of the Ghostscript interpreter.
     *
     * @param stdIn InputStream object
     */
    public synchronized void setStdIn(InputStream stdIn) {
        this.stdIn = stdIn;
    }

    /**
     * Singleton factory method for getting a Ghostscript,interpreter instance.
     * Only called from class itself.
     *
     * @return Ghostscript instance.
     * @throws org.ghost4j.GhostscriptException
     */
    private synchronized GhostscriptLibrary.gs_main_instance.ByReference getNativeInstanceByRef()
            throws GhostscriptException {
        if (nativeInstanceByRef == null) {
            nativeInstanceByRef = new GhostscriptLibrary.gs_main_instance.ByReference();
            int result = GhostscriptLibrary.instance.gsapi_new_instance(
                    nativeInstanceByRef.getPointer(), null);
            if (result != 0) {
                nativeInstanceByRef = null;
                throw new GhostscriptException(
                        "Cannot get Ghostscript interpreter instance. Error code is "
                                + result);
            }
        }
        return nativeInstanceByRef;
    }

    private synchronized DisplayData getDisplayData() {
        if (displayData == null) {
            displayData = new DisplayData();
        }
        return displayData;
    }

    /**
     * Initializes Ghostscript interpreter.
     *
     * @param args Interpreter parameters. Use the same as Ghostscript command
     *             line arguments.
     * @throws org.ghost4j.GhostscriptException
     */
    public void initialize(String[] args) throws GhostscriptException {
        int result = 0;
        GhostscriptLibrary.stdin_fn stdinCallback = null;
        if (getStdIn() != null) {
            stdinCallback = new GhostscriptLibrary.stdin_fn() {
                public int callback(Pointer caller_handle, Pointer buf, int len) {
                    String encoding = System.getProperty(PROPERTY_NAME_ENCODING, System.getProperty("file.encoding"));
                    try {
                        byte[] buffer = new byte[1000];
                        int read = getStdIn().read(buffer);
                        if (read != -1) {
                            buf.setString(0, new String(buffer, 0, read,
                                    encoding));
                            return read;
                        }
                    } catch (Exception e) {
                        //
                    }
                    return 0;
                }
            };
        }
        GhostscriptLibrary.stdout_fn stdoutCallback = null;
        if (getStdOut() == null) {
            setStdOut(new GhostscriptLoggerOutputStream(Level.INFO));
        }
        stdoutCallback = new GhostscriptLibrary.stdout_fn() {
            public int callback(Pointer caller_handle, String str, int len) {
                try {
                    getStdOut().write(str.getBytes(), 0, len);
                } catch (IOException ex) {
                    //
                }
                return len;
            }
        };
        GhostscriptLibrary.stderr_fn stderrCallback = null;
        if (getStdErr() == null) {
            setStdErr(new GhostscriptLoggerOutputStream(Level.ERROR));
        }
        stderrCallback = new GhostscriptLibrary.stderr_fn() {
            public int callback(Pointer caller_handle, String str, int len) {
                try {
                    getStdErr().write(str.getBytes(), 0, len);
                } catch (IOException ex) {
                    //
                }
                return len;
            }
        };
        result = GhostscriptLibrary.instance.gsapi_set_stdio(getNativeInstanceByRef().getValue(), stdinCallback,
                stdoutCallback, stderrCallback);
        if (result != 0) {
            throw new GhostscriptException(
                    "Cannot set IO on Ghostscript interpreter. Error code is "
                            + result);
        }
        if (getDisplayCallback() != null) {
            result = GhostscriptLibrary.instance.gsapi_set_display_callback(
                    getNativeInstanceByRef().getValue(),
                    buildNativeDisplayCallback(getDisplayCallback()));
            if (result != 0) {
                throw new GhostscriptException(
                        "Cannot set display callback on Ghostscript interpreter. Error code is "
                                + result);
            }
        }
        if (args != null) {
            result = GhostscriptLibrary.instance.gsapi_init_with_args(
                    getNativeInstanceByRef().getValue(), args.length, args);
        } else {
            result = GhostscriptLibrary.instance.gsapi_init_with_args(
                    getNativeInstanceByRef().getValue(), 0, null);
        }
        if (result == -101) {
            exit();
            result = 0;
        }
        if (result != 0) {
            throw new GhostscriptException("Cannot initialize Ghostscript interpreter. Error code is "
                    + result);
        }
    }

    /**
     * Builds a native display callback from a DisplayCallback object.
     *
     * @param displayCallback DisplayCallback to use.
     * @return The created native display callback.
     */
    private synchronized GhostscriptLibrary.display_callback_s buildNativeDisplayCallback(
            DisplayCallback displayCallback) throws GhostscriptException {
        nativeDisplayCallback = new GhostscriptLibrary.display_callback_s();
        float version = Float.parseFloat(getRevision().getNumber());
        // some versions report version 8.15 as 815.05
        if (version < 8.50 || version > 100) {
            nativeDisplayCallback.version_major = 1;
        } else {
            nativeDisplayCallback.version_major = 2;
        }
        nativeDisplayCallback.version_minor = 0;
        nativeDisplayCallback.display_open = new GhostscriptLibrary.display_callback_s.display_open() {
            public int callback(Pointer handle, Pointer device) {
                try {
                    getDisplayCallback().displayOpen();
                } catch (GhostscriptException e) {
                    return 1;
                }
                return 0;
            }
        };
        nativeDisplayCallback.display_preclose = new GhostscriptLibrary.display_callback_s.display_preclose() {
            public int callback(Pointer handle, Pointer device) {
                try {
                    getDisplayCallback().displayPreClose();
                } catch (GhostscriptException e) {
                    return 1;
                }
                return 0;
            }
        };
        nativeDisplayCallback.display_close = new GhostscriptLibrary.display_callback_s.display_close() {
            public int callback(Pointer handle, Pointer device) {
                try {
                    getDisplayCallback().displayClose();
                } catch (GhostscriptException e) {
                    return 1;
                }
                return 0;
            }
        };
        nativeDisplayCallback.display_presize = new GhostscriptLibrary.display_callback_s.display_presize() {
            public int callback(Pointer handle, Pointer device, int width,
                                int height, int raster, int format) {
                try {
                    getDisplayCallback().displayPreSize(width, height, raster,
                            format);
                } catch (GhostscriptException e) {
                    return 1;
                }

                return 0;
            }
        };
        nativeDisplayCallback.display_size = new GhostscriptLibrary.display_callback_s.display_size() {

            public int callback(Pointer handle, Pointer device, int width,
                                int height, int raster, int format, Pointer pimage) {
                getDisplayData().setWidth(width);
                getDisplayData().setHeight(height);
                getDisplayData().setRaster(raster);
                getDisplayData().setFormat(format);
                getDisplayData().setPimage(pimage);
                try {
                    getDisplayCallback().displaySize(width, height, raster, format);
                } catch (GhostscriptException e) {
                    return 1;
                }

                return 0;
            }
        };
        nativeDisplayCallback.display_sync = new GhostscriptLibrary.display_callback_s.display_sync() {

            public int callback(Pointer handle, Pointer device) {
                try {
                    getDisplayCallback().displaySync();
                } catch (GhostscriptException e) {
                    return 1;
                }
                return 0;
            }
        };
        nativeDisplayCallback.display_page = new GhostscriptLibrary.display_callback_s.display_page() {

            public int callback(Pointer handle, Pointer device, int copies, int flush) {
                byte[] data = getDisplayData().getPimage().getByteArray(
                        0, getDisplayData().getRaster() * getDisplayData().getHeight());
                try {
                    getDisplayCallback().displayPage(
                            getDisplayData().getWidth(),
                            getDisplayData().getHeight(),
                            getDisplayData().getRaster(),
                            getDisplayData().getFormat(), copies, flush, data);
                } catch (GhostscriptException e) {
                    return 1;
                }
                return 0;
            }
        };
        nativeDisplayCallback.display_update = new GhostscriptLibrary.display_callback_s.display_update() {
            public int callback(Pointer handle, Pointer device, int x, int y, int w, int h) {
                try {
                    getDisplayCallback().displayUpdate(x, y, w, h);
                } catch (GhostscriptException e) {
                    return 1;
                }

                return 0;
            }
        };

        nativeDisplayCallback.display_memalloc = null;
        nativeDisplayCallback.display_memfree = null;

        switch (nativeDisplayCallback.version_major) {
            case 1:
                nativeDisplayCallback.size = nativeDisplayCallback.size()
                        - Pointer.SIZE;
                break;
            default:
                nativeDisplayCallback.size = nativeDisplayCallback.size();
                break;
        }

        nativeDisplayCallback.display_separation = null;

        return nativeDisplayCallback;
    }

    /**
     * Exits Ghostscript interpreter. Must be called after initialize.
     *
     * @throws org.ghost4j.GhostscriptException
     */
    public void exit() throws GhostscriptException {
        if (nativeInstanceByRef != null) {
            int result = GhostscriptLibrary.instance.gsapi_exit(getNativeInstanceByRef().getValue());
            if (result != 0) {
                throw new GhostscriptException("Cannot exit Ghostscript interpreter. Error code is "
                        + result);
            }
        }
    }

    /**
     * Sends command string to Ghostscript interpreter. Must be called after
     * initialize method.
     *
     * @param string Command string
     * @throws org.ghost4j.GhostscriptException
     */
    public void runString(String string) throws GhostscriptException {
        IntByReference exitCode = new IntByReference();
        GhostscriptLibrary.instance.gsapi_run_string_begin(getNativeInstanceByRef().getValue(), 0, exitCode);
        if (exitCode.getValue() != 0) {
            throw new GhostscriptException("Cannot run command on Ghostscript interpreter. gsapi_run_string_begin failed with error code "
                    + exitCode.getValue());
        }
        String[] slices = string.split("\n");
        for (String slice1 : slices) {
            String slice = slice1 + "\n";
            GhostscriptLibrary.instance.gsapi_run_string_continue(
                    getNativeInstanceByRef().getValue(), slice, slice.length(),
                    0, exitCode);
            if (exitCode.getValue() != 0) {
                throw new GhostscriptException("Cannot run command on Ghostscript interpreter. gsapi_run_string_continue failed with error code "
                        + exitCode.getValue());
            }
        }
        GhostscriptLibrary.instance.gsapi_run_string_end(getNativeInstanceByRef().getValue(), 0, exitCode);
        if (exitCode.getValue() != 0) {
            throw new GhostscriptException("Cannot run command on Ghostscript interpreter. gsapi_run_string_end failed with error code "
                    + exitCode.getValue());
        }
    }

    /**
     * Sends file Ghostscript interpreter. Must be called after initialize
     * method.
     *
     * @param fileName File name
     * @throws org.ghost4j.GhostscriptException
     */
    public void runFile(String fileName) throws GhostscriptException {
        IntByReference exitCode = new IntByReference();
        GhostscriptLibrary.instance.gsapi_run_file(getNativeInstanceByRef().getValue(), fileName, 0, exitCode);
        if (exitCode.getValue() != 0) {
            throw new GhostscriptException(
                    "Cannot run file on Ghostscript interpreter. Error code "
                            + exitCode.getValue());
        }

    }
}
