/*
 * #%L
 * Preference-based cLoud Service Recommender (PuLSaR) - Broker@Cloud optimisation engine
 * %%
 * Copyright (C) 2014 - 2016 Information Management Unit, Institute of Communication and Computer Systems, National Technical University of Athens
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
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