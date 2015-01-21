/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.broker.orbi.ui;

/**
 *
 * @author ermis
 */
public class SPOffer {

    int id;
    String name;
    String date_created;
    String policy_name;
    String profile_name;
    String username;
    int image_template_id;
    int flavor_id;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDate_created() {
        return date_created;
    }

    public void setDate_created(String date_created) {
        this.date_created = date_created;
    }

    public String getPolicy_name() {
        return policy_name;
    }

    public void setPolicy_name(String policy_name) {
        this.policy_name = policy_name;
    }

    public String getProfile_name() {
        return profile_name;
    }

    public void setProfile_name(String profile_name) {
        this.profile_name = profile_name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getImage_template_id() {
        return image_template_id;
    }

    public void setImage_template_id(int image_template_id) {
        this.image_template_id = image_template_id;
    }

    public int getFlavor_id() {
        return flavor_id;
    }

    public void setFlavor_id(int flavor_id) {
        this.flavor_id = flavor_id;
    }

    public SPOffer(int id, String name, String date_created, String policy_name, String profile_name, String username, int image_template_id, int flavor_id) {
        this.id = id;
        this.name = name;
        this.date_created = date_created;
        this.policy_name = policy_name;
        this.profile_name = profile_name;
        this.username = username;
        this.image_template_id = image_template_id;
        this.flavor_id = flavor_id;
    }

        public SPOffer(int id, String name, String date_created, String policy_name, String profile_name, String username) {
        this.id = id;
        this.name = name;
        this.date_created = date_created;
        this.policy_name = policy_name;
        this.profile_name = profile_name;
        this.username = username;
    }

    
    
}
