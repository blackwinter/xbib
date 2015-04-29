package org.snmp4j.security;

import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;

/**
 * The <code>UsmUserEntry</code> class represents a user in the
 * Local Configuration Datastore (LCD).
 */
public class UsmUserEntry implements Comparable {

    private OctetString engineID;
    private OctetString userName;
    private UsmUser usmUser;
    private byte[] authenticationKey;
    private byte[] privacyKey;

    /**
     * Creates a new user entry with empty engine ID and empty user.
     */
    public UsmUserEntry() {
        engineID = new OctetString();
        userName = new OctetString();
        usmUser = new UsmUser(new OctetString(), null, null, null, null);
    }

    /**
     * Creates a user with user name and associated {@link org.snmp4j.security.UsmUser}.
     *
     * @param userName the user name of the new entry.
     * @param user     the <code>UsmUser</code> representing the security information of the
     *                 user.
     */
    public UsmUserEntry(OctetString userName, UsmUser user) {
        this.userName = userName;
        this.usmUser = user;
        if (user.isLocalized()) {
            this.engineID = user.getLocalizationEngineID();
            if ((user.getAuthenticationProtocol() != null) &&
                    (user.getAuthenticationPassphrase() != null)) {
                authenticationKey = user.getAuthenticationPassphrase().getValue();
                if ((user.getPrivacyProtocol() != null) &&
                        (user.getPrivacyPassphrase() != null)) {
                    privacyKey = user.getPrivacyPassphrase().getValue();
                }
            }
        }
    }

    /**
     * Creates a user with user name and associated {@link org.snmp4j.security.UsmUser}.
     *
     * @param userName the user name of the new entry.
     * @param engineID the authoritative engine ID associated with the user.
     * @param user     the <code>UsmUser</code> representing the security information of the
     *                 user.
     */
    public UsmUserEntry(OctetString userName,
                        OctetString engineID,
                        UsmUser user) {
        this(userName, user);
        this.engineID = engineID;
    }

    /**
     * Creates a localized user entry.
     *
     * @param engineID     the engine ID for which the users has bee localized.
     * @param securityName the user and security name of the new entry.
     * @param authProtocol the authentication protocol ID.
     * @param authKey      the authentication key.
     * @param privProtocol the privacy protocol ID.
     * @param privKey      the privacy key.
     */
    public UsmUserEntry(byte[] engineID, OctetString securityName,
                        OID authProtocol, byte[] authKey,
                        OID privProtocol, byte[] privKey) {
        this.engineID = (engineID == null) ? null : new OctetString(engineID);
        this.userName = securityName;
        this.authenticationKey = authKey;
        this.privacyKey = privKey;
        this.usmUser =
                new UsmUser(userName, authProtocol,
                        ((authenticationKey != null) ?
                                new OctetString(authenticationKey) : null),
                        privProtocol,
                        ((privacyKey != null) ?
                                new OctetString(privacyKey) : null), this.engineID);
    }

    public OctetString getEngineID() {
        return engineID;
    }

    public void setEngineID(OctetString engineID) {
        this.engineID = engineID;
    }

    public OctetString getUserName() {
        return userName;
    }

    public void setUserName(OctetString userName) {
        this.userName = userName;
    }

    public UsmUser getUsmUser() {
        return usmUser;
    }

    public void setUsmUser(UsmUser usmUser) {
        this.usmUser = usmUser;
    }

    public byte[] getAuthenticationKey() {
        return authenticationKey;
    }

    public void setAuthenticationKey(byte[] authenticationKey) {
        this.authenticationKey = authenticationKey;
    }

    public byte[] getPrivacyKey() {
        return privacyKey;
    }

    public void setPrivacyKey(byte[] privacyKey) {
        this.privacyKey = privacyKey;
    }

    /**
     * Compares this user entry with another one by engine ID then by their user
     * names.
     *
     * @param o a <code>UsmUserEntry</code> instance.
     * @return a negative integer, zero, or a positive integer as this object is
     * less than, equal to, or greater than the specified object.
     */
    public int compareTo(Object o) {
        UsmUserEntry other = (UsmUserEntry) o;
        int result = 0;

        if (engineID != null)  {
            if (other.engineID != null) {
                result = engineID.compareTo(other.engineID);
            } else {
                result = 1;
            }
        } else if (other.engineID != null) {
            result = -1;
        }
        if (result == 0) {
            result = userName.compareTo(other.userName);
            if (result == 0) {
                result = usmUser.compareTo(other.usmUser);
            }
        }
        return result;
    }

    public String toString() {
        return "UsmUserEntry[userName=" + userName + ",usmUser=" + usmUser + "]";
    }

}
