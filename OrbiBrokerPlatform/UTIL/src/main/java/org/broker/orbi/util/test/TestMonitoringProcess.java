package org.broker.orbi.util.test;

import org.broker.orbi.util.impl.ZabbixUtil;

/**
 *
 * @author smantzouratos
 */
public class TestMonitoringProcess {
    public static void main(String[] args) {
        ZabbixUtil.insertNEWZabbixHost("OrbiInstance192.168.3.11", "192.168.3.11");
//        ZabbixUtil.retrieveHistory(10121, 120);
    }
}
