package org.xbib.cluster.util;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

public class NetworkUtil {

    public static String getDefaultAddress() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            return InetAddress.getLoopbackAddress().getHostAddress();
        }
    }

    public static InetAddress getPublicAddress() {
        Enumeration<NetworkInterface> nets;
        try {
            nets = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            return null;
        }
        NetworkInterface netinf;
        while (nets.hasMoreElements()) {
            netinf = nets.nextElement();
            Enumeration<InetAddress> addresses = netinf.getInetAddresses();
            while (addresses.hasMoreElements()) {
                InetAddress address = addresses.nextElement();
                if (!address.isAnyLocalAddress() && !address.isMulticastAddress()
                        && !(address instanceof Inet6Address)) {
                    return address;
                }
            }
        }
        return null;
    }

    public static NetworkInterface getPublicInterface() {
        Enumeration<NetworkInterface> nets;
        try {
            nets = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            return null;
        }
        NetworkInterface netinf;
        while (nets.hasMoreElements()) {
            netinf = nets.nextElement();
            Enumeration<InetAddress> addresses = netinf.getInetAddresses();
            while (addresses.hasMoreElements()) {
                InetAddress address = addresses.nextElement();
                if (!address.isAnyLocalAddress() && !address.isMulticastAddress()
                        && !(address instanceof Inet6Address)) {
                    return netinf;
                }
            }
        }
        return null;
    }
}
