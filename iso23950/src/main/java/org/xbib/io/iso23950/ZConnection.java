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
package org.xbib.io.iso23950;

import asn1.ASN1Exception;
import asn1.ASN1Integer;
import asn1.BEREncoding;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xbib.io.Connection;
import z3950.v3.Close;
import z3950.v3.CloseReason;
import z3950.v3.PDU;

/**
 * A Z39.50 connection
 */
public class ZConnection extends URLConnection implements Connection<ZSession> {

    private static final Logger logger = LogManager.getLogger(ZConnection.class.getName());

    private ZSession session;

    private URL url;

    private Socket socket;

    private BufferedInputStream src;

    private BufferedOutputStream dest;

    private long timeout;

    /**
     * Constructs a URL connection to the specified URL. A connection to
     * the object referenced by the URL is not created.
     *
     * @param url the specified URL.
     */
    public ZConnection(URL url) {
        super(url);
        this.url = url;
        this.timeout = 30000L;
    }

    @Override
    public void connect() throws IOException {
        if (isConnected()) {
            return;
        }
        this.socket = new Socket();
        socket.connect(new InetSocketAddress(url.getHost(), url.getPort()), (int) timeout);
        socket.setSoTimeout((int) timeout);
        // initialize bi-directional communication channel
        this.src = new BufferedInputStream(socket.getInputStream());
        this.dest = new BufferedOutputStream(socket.getOutputStream());
        this.session = createSession();
    }

    public void setTimeout(long millis) {
        this.timeout = millis;
        if (socket != null) {
            try {
                socket.setSoTimeout((int) timeout);
            } catch (SocketException ex) {
                logger.warn(ex.getMessage(), ex);
            }
        }
    }

    public long getTimeout() {
        return timeout;
    }

    @Override
    public ZSession createSession() throws IOException {
        return new ZSession(this);
    }

    @Override
    public void close() throws IOException {
        session.close();
        if (isConnected()) {
            try {
                initClose(0);
            } catch (IOException e) {
                logger.warn("while attempting to initiate connection close: {}", e.getMessage());
            }
            try {
                if (src != null) {
                    src.close();
                    src = null;
                }
                if (dest != null) {
                    dest.close();
                    dest = null;
                }
                if (socket != null) {
                    socket.close();
                    socket = null;
                }
            } catch (IOException e) {
                logger.warn("error attempting to close connection: {}", e.getMessage());
            }
        }
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected();
    }

    public void writePDU(PDU pdu) throws IOException {
        if (dest == null) {
            throw new IOException("no output stream");
        }
        try {
            pdu.ber_encode().output(dest);
            dest.flush();
        } catch (ASN1Exception ex) {
            throw new IOException(ex.getMessage(), ex);
        }
    }

    public PDU readPDU() throws IOException {
        if (src == null) {
            throw new IOException("no input stream");
        }
        try {
            BEREncoding ber = BEREncoding.input(src);
            if (ber == null) {
                throw new IOException("connection read PDU error");
            }
            return new PDU(ber, true);
        } catch (ASN1Exception ex) {
            throw new IOException(ex.getMessage(), ex);
        } catch (NullPointerException ex) {
            throw new IOException("connection read PDU error", ex);
        }
    }

    /**
     * Initiate a close request. Reason codes are:
     *
     * 0=finished 1=shutdown 2=system problem 3=cost limits
     * 4=resources 5=security violation 6=protocol error 7=lack of activity
     * 8=peer abort 9=unspecified

     * @param reason reason
     * @throws IOException
     */
    public void initClose(int reason) throws IOException {
        PDU pdu = new PDU();
        pdu.c_close = new Close();
        pdu.c_close.s_closeReason = new CloseReason();
        pdu.c_close.s_closeReason.value = new ASN1Integer(reason);
        pdu.c_close.s_referenceId = null;
        writePDU(pdu);
        //waitClosePDU();
    }
}
