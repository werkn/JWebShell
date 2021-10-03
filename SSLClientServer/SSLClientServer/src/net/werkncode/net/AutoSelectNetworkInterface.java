package net.werkncode.net;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * AutoSelectNetworkInterface will automatically select a network interface based on
 * a remote host that needs resolution.  If it can't resolve the remote host after
 * exhausting all options it will return null.
 * 
 * Underlying impl. currently using ICMP, so if host is blocking ICMP this will fail or
 * any hops along the way filters ICMP.
 * @author werkn
 */
public class AutoSelectNetworkInterface {

	/**
	 * 
	 * @param ip Remote host to test connectivity too.
	 * @return If an InetAddress is found returns that address to bind, otherwise returns null
	 */
	public static InetAddress getInterfaceAddressWithAccessToRemote(String ip) {
		try {
			Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
			
			while (networkInterfaces.hasMoreElements()) {
				NetworkInterface networkInterface = networkInterfaces.nextElement();
				Enumeration<InetAddress> interfaceAddresses = networkInterface.getInetAddresses();
				while (interfaceAddresses.hasMoreElements()) {
					InetAddress interfaceAddress = interfaceAddresses.nextElement();
					//we only wan't IPv4 addresses
					if (interfaceAddress instanceof Inet4Address) {
						//System.out.println(interfaceAddress.getHostAddress());
						
						boolean remoteIsReachable = false;
						//the following is unrealable if remote is outside the local
						//subnet, TODO: at some point if we recycle this implement as
						//a system call to shell for 'ping'
						try {
							//System.out.println("Testing " + interfaceAddress);
							
							//using the currently selected network interface see if remote
							//is reachable if it is exit immed.
							remoteIsReachable = InetAddress
									.getByName(ip)
									//check interface, allow up to 30 hops (ttl), and timeout in 3 seconds
									.isReachable(networkInterface, 30, 3000);  
						} catch (IOException e) {
							//System.out.println("Host not reachable!  Trying next interface.");
							remoteIsReachable = false;
						}
						
						if (remoteIsReachable) {
							//System.out.println(
									//String.format("Host reachable from interface (%s)!  Exiting.", interfaceAddress));
							return interfaceAddress;
						}
					}
				}	    
			}
			
		} catch (SocketException e) {
			e.printStackTrace();
			return null;
		}
		
		//none of the interfaces can reach the host
		return null;
	}
	
	/**
	public static void main(String[] args) {
		
		//test 8.8.8.8 dns is resolved
		AutoSelectNetworkInterface.getInterfaceAddressWithAccessToRemote("8.8.8.8");
		
		//test local resolver
		AutoSelectNetworkInterface.getInterfaceAddressWithAccessToRemote("localhost");
	}
	*/

}
