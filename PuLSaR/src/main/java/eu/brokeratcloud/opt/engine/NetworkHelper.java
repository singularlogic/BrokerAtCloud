package eu.brokeratcloud.opt.engine;

import java.net.*;
import java.util.*;
import java.util.regex.Pattern;

public class NetworkHelper {
	
	public static void main(String[] args) {
		String filter = (args.length>0) ? args[0] : "";
		for (InetAddress addr : getInetAddresses(filter)) {
			System.out.println(addr);
		}
	}
	
	public static List<InetAddress> getInetAddresses(String filter) {
		if (filter==null || (filter=filter.trim()).isEmpty()) return getInetAddresses();
		
		Pattern pat = Pattern.compile( filter.trim() );
		List<InetAddress> addresses = new ArrayList<InetAddress>();
		for (InetAddress addr : getInetAddresses()) {
			if (pat.matcher( addr.toString().substring(1) ).matches()) {
				addresses.add(addr);
			}
		}
		return addresses;
	}
	
	public static List<InetAddress> getInetAddresses() {
		List<InetAddress> addresses = new ArrayList<InetAddress>();
		try {
			Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
			while (en.hasMoreElements()) {
				NetworkInterface intf = en.nextElement();
				try {
					if (!intf.isUp()) continue;
					if (intf.isLoopback()) continue;
				} catch (SocketException ex2) {
					continue;
				}
				
				Enumeration<InetAddress> en2 = intf.getInetAddresses();
				while (en2.hasMoreElements()) {
					InetAddress addr = en2.nextElement();
					if (!(addr instanceof Inet4Address)) continue;
					if (addr.isLoopbackAddress()) continue;
					addresses.add(addr);
				}
			}
			return addresses;
		} catch (SocketException ex) {
			return addresses;
		}
	}
}