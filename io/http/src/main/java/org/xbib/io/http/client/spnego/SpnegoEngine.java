package org.xbib.io.http.client.spnego;

import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSManager;
import org.ietf.jgss.GSSName;
import org.ietf.jgss.Oid;
import org.xbib.io.http.client.util.Base64;

import java.io.IOException;

/**
 * SPNEGO (Simple and Protected GSSAPI Negotiation Mechanism) authentication scheme.
 */
public class SpnegoEngine {

    private static final String SPNEGO_OID = "1.3.6.1.5.5.2";
    private static final String KERBEROS_OID = "1.2.840.113554.1.2.2";
    private static SpnegoEngine instance;
    private final SpnegoTokenGenerator spnegoGenerator;

    public SpnegoEngine(final SpnegoTokenGenerator spnegoGenerator) {
        this.spnegoGenerator = spnegoGenerator;
    }

    public SpnegoEngine() {
        this(null);
    }

    public static SpnegoEngine instance() {
        if (instance == null) {
            instance = new SpnegoEngine();
        }
        return instance;
    }

    public String generateToken(String server) throws SpnegoEngineException {
        GSSContext gssContext = null;
        byte[] token = null; // base64 decoded challenge
        Oid negotiationOid = null;

        try {
            /*
             * Using the SPNEGO OID is the correct method. Kerberos v5 works for IIS but not JBoss. Unwrapping the initial token when using SPNEGO OID looks like what is described
             * here...
             * 
             * http://msdn.microsoft.com/en-us/library/ms995330.aspx
             * 
             * Another helpful URL...
             * 
             * http://publib.boulder.ibm.com/infocenter/wasinfo/v7r0/index.jsp?topic=/com.ibm.websphere.express.doc/info/exp/ae/tsec_SPNEGO_token.html
             * 
             * Unfortunately SPNEGO is JRE >=1.6.
             */

            /** Try SPNEGO by default, fall back to Kerberos later if error */
            negotiationOid = new Oid(SPNEGO_OID);

            boolean tryKerberos = false;
            try {
                GSSManager manager = GSSManager.getInstance();
                GSSName serverName = manager.createName("HTTP@" + server, GSSName.NT_HOSTBASED_SERVICE);
                gssContext = manager.createContext(serverName.canonicalize(negotiationOid), negotiationOid, null,
                        GSSContext.DEFAULT_LIFETIME);
                gssContext.requestMutualAuth(true);
                gssContext.requestCredDeleg(true);
            } catch (GSSException ex) {
                // BAD MECH means we are likely to be using 1.5, fall back to Kerberos MECH.
                // Rethrow any other exception.
                if (ex.getMajor() == GSSException.BAD_MECH) {
                    tryKerberos = true;
                } else {
                    throw ex;
                }

            }
            if (tryKerberos) {
                /* Kerberos v5 GSS-API mechanism defined in RFC 1964. */
                negotiationOid = new Oid(KERBEROS_OID);
                GSSManager manager = GSSManager.getInstance();
                GSSName serverName = manager.createName("HTTP@" + server, GSSName.NT_HOSTBASED_SERVICE);
                gssContext = manager.createContext(serverName.canonicalize(negotiationOid), negotiationOid, null,
                        GSSContext.DEFAULT_LIFETIME);
                gssContext.requestMutualAuth(true);
                gssContext.requestCredDeleg(true);
            }
            token = new byte[0];
            token = gssContext.initSecContext(token, 0, token.length);
            if (token == null) {
                throw new SpnegoEngineException("GSS security context initialization failed");
            }

            /*
             * IIS accepts Kerberos and SPNEGO tokens. Some other servers Jboss, Glassfish? seem to only accept SPNEGO. Below wraps Kerberos into SPNEGO token.
             */
            if (spnegoGenerator != null && negotiationOid.toString().equals(KERBEROS_OID)) {
                token = spnegoGenerator.generateSpnegoDERObject(token);
            }

            gssContext.dispose();

            return Base64.encode(token);
        } catch (GSSException gsse) {
            if (gsse.getMajor() == GSSException.DEFECTIVE_CREDENTIAL || gsse.getMajor() == GSSException.CREDENTIALS_EXPIRED) {
                throw new SpnegoEngineException(gsse.getMessage(), gsse);
            }
            if (gsse.getMajor() == GSSException.NO_CRED) {
                throw new SpnegoEngineException(gsse.getMessage(), gsse);
            }
            if (gsse.getMajor() == GSSException.DEFECTIVE_TOKEN || gsse.getMajor() == GSSException.DUPLICATE_TOKEN
                    || gsse.getMajor() == GSSException.OLD_TOKEN) {
                throw new SpnegoEngineException(gsse.getMessage(), gsse);
            }
            // other error
            throw new SpnegoEngineException(gsse.getMessage());
        } catch (IOException ex) {
            throw new SpnegoEngineException(ex.getMessage());
        }
    }
}
