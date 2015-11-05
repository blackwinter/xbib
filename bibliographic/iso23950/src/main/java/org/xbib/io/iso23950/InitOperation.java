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

import asn1.ASN1BitString;
import asn1.ASN1GeneralString;
import asn1.ASN1Integer;
import java.io.IOException;

import z3950.v3.IdAuthentication;
import z3950.v3.IdAuthentication_idPass;
import z3950.v3.InitializeRequest;
import z3950.v3.InitializeResponse;
import z3950.v3.InternationalString;
import z3950.v3.Options;
import z3950.v3.PDU;
import z3950.v3.ProtocolVersion;

/**
 *  A Z39.50 Init operation
 *
 */
public class InitOperation {

    private String targetInfo;
    private int targetVersion;
    private String user;
    private String password;
    private String group;
    private boolean rejected;

    public InitOperation(String user, String password, String group) {
        this.user = user;
        this.password = password;
        this.group = group;
    }

    public void execute(ZSession session) throws IOException {
        if (session == null) {
            throw new IOException("no session");
        }
        if (session.getConnection() == null) {
            throw new IOException("no connection");
        }
        InitializeRequest init = new InitializeRequest();
        boolean version[] = new boolean[3];
        version[0] = true; // any version, should alwasy be true
        version[1] = true; // Z39.50 version 2
        version[2] = true; // Z39.50 version 3
        init.s_protocolVersion = new ProtocolVersion();
        init.s_protocolVersion.value = new ASN1BitString(version);
        boolean options[] = new boolean[15];
        options[0] = true; // search
        options[1] = true; // present
        options[2] = true;  // delete set
        options[3] = false; // resource-report
        options[4] = false; // trigger resource control
        options[5] = false;  // resource control
        options[6] = false; // access control
        options[7] = true; // scan
        options[8] = false; // sort
        options[9] = false; // (unused)
        options[10] = false; // extended-services  
        options[11] = false; // level 1 segmentation
        options[12] = false; // level 2 segmentation
        options[13] = false; // concurrent operations
        options[14] = true;// named result sets
        init.s_options = new Options();
        init.s_options.value = new ASN1BitString(options);
        init.s_preferredMessageSize = new ASN1Integer(128 * 1024);
        init.s_exceptionalRecordSize = new ASN1Integer(256 * 1024);
        init.s_implementationId = new InternationalString();
        init.s_implementationId.value = new ASN1GeneralString("1");
        init.s_implementationName = new InternationalString();
        init.s_implementationName.value = new ASN1GeneralString("JAFER ZClient");
        init.s_implementationVersion = new InternationalString();
        init.s_implementationVersion.value = new ASN1GeneralString("1.00");
        if (user != null) {
            init.s_idAuthentication = new IdAuthentication();
            init.s_idAuthentication.c_idPass = new IdAuthentication_idPass();
            init.s_idAuthentication.c_idPass.s_userId = new InternationalString();
            init.s_idAuthentication.c_idPass.s_userId.value = new ASN1GeneralString(user);

            if (password != null) {
                init.s_idAuthentication.c_idPass.s_password = new InternationalString();
                init.s_idAuthentication.c_idPass.s_password.value = new ASN1GeneralString(password);
            }
            if (group != null) {
                init.s_idAuthentication.c_idPass.s_groupId = new InternationalString();
                init.s_idAuthentication.c_idPass.s_groupId.value = new ASN1GeneralString(group);
            }
        }
        PDU pduOut = new PDU();
        pduOut.c_initRequest = init;
        session.getConnection().writePDU(pduOut);
        PDU pduIn = session.getConnection().readPDU();
        InitializeResponse initResp = pduIn.c_initResponse;
        if (initResp.s_implementationName != null) {
            targetInfo = initResp.s_implementationName.toString();
            if (initResp.s_implementationVersion != null) {
                targetInfo += " - " + initResp.s_implementationVersion.toString();
            }
        } else {
            targetInfo = "server";
        }
        if (initResp.s_protocolVersion != null) {
            for (int n = 0; n < initResp.s_protocolVersion.value.get().length; n++) {
                if (initResp.s_protocolVersion.value.get()[n]) {
                    targetVersion = n + 1;
                }
            }
            targetInfo += " (Version " + targetVersion + ")";
        } else {
            targetInfo += " (Version unknown)";
        }
        if (initResp.s_userInformationField != null) {
            if (initResp.s_userInformationField.c_singleASN1type != null) {
                targetInfo += "\n" + initResp.s_userInformationField.c_singleASN1type.toString();
            }
        }
        if (initResp.s_otherInfo != null) {
            targetInfo += "\n" + initResp.s_otherInfo.toString();
        }
        targetInfo = targetInfo.replaceAll("\"", "");
        this.rejected =  !initResp.s_result.get();
        if (rejected) {
            throw new IOException("Connection rejected by " + targetInfo);
        }
    }
    
    public boolean rejected() {
        return rejected;
    }

    public String getTargetInfo() {
        return targetInfo;
    }

    public int getTargetVersion() {
        return targetVersion;
    }
}
