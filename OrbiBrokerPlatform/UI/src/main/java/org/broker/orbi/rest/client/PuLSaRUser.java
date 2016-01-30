package org.broker.orbi.rest.client;

/**
 *
 * @author Christos Paraskeva <ch.paraskeva at gmail dot com>
 */
public class PuLSaRUser {

    String username;
    String password;
    String roles;

    public PuLSaRUser(String username, String password, String roles) {
        this.username = username;
        this.password = password;
        this.roles = roles;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRoles() {
        return roles;
    }

    public void setRoles(String roles) {
        this.roles = roles;
    }

    @Override
    public String toString() {
        return "username=".concat(this.username).concat("&password=").concat(password).concat("&roles=").concat(roles);
    }

}
