package org.broker.orbi.rest.client;

/**
 *
 * @author Christos Paraskeva <ch.paraskeva at gmail dot com>
 */
public class PuLSaRResponse {

    public enum StatusCode {

        USER_ADDED,
        USER_NOT_EXIST,
        USER_EXISTS,
        SERVICE_DOWN;
    }
}
