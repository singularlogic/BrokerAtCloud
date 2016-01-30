package org.broker.orbi.models;

/**
 *
 * @author ermis
 */
public class Offer {

    int id;
    String name;
    String date_created;
    String policy_name;
    String service_description;
    String flavor;
    String imageTemplate;
    String iaasProvider;
    String username;
    int policy_id;
    int service_description_id;
    int image_template_id;
    int flavor_id;
    int iaas_id;

    public String getFlavor() {
        return flavor;
    }

    public void setFlavor(String flavor) {
        this.flavor = flavor;
    }

    public String getImageTemplate() {
        return imageTemplate;
    }

    public void setImageTemplate(String imageTemplate) {
        this.imageTemplate = imageTemplate;
    }

    public String getIaasProvider() {
        return iaasProvider;
    }

    public void setIaasProvider(String iaasProvider) {
        this.iaasProvider = iaasProvider;
    }

    public int getService_description_id() {
        return service_description_id;
    }

    public void setService_description_id(int service_description_id) {
        this.service_description_id = service_description_id;
    }

    public int getPolicy_id() {
        return policy_id;
    }

    public void setPolicy_id(int policy_id) {
        this.policy_id = policy_id;
    }

    public int getIaas_id() {
        return iaas_id;
    }

    public void setIaas_id(int iaas_id) {
        this.iaas_id = iaas_id;
    }

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

    public String getService_description() {
        return service_description;
    }

    public void setService_description(String service_description) {
        this.service_description = service_description;
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

    public Offer(int id, String name, String date_created, String policy_name, String profile_name, String username, int image_template_id, int flavor_id, int iaaas_id) {
        this.id = id;
        this.name = name;
        this.date_created = date_created;
        this.policy_name = policy_name;
        this.service_description = profile_name;
        this.username = username;
        this.image_template_id = image_template_id;
        this.flavor_id = flavor_id;
    }

    public Offer(int id, String name, String date_created, String policy_name, String profile_name, String username) {
        this.id = id;
        this.name = name;
        this.date_created = date_created;
        this.policy_name = policy_name;
        this.service_description = profile_name;
        this.username = username;
    }

    public Offer() {
        this.id = 0;
    }

}
