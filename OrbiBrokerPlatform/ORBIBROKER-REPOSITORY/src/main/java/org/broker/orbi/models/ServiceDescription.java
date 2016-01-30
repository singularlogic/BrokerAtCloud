package org.broker.orbi.models;

/**
 *
 * @author ermis
 */
public class ServiceDescription {

    int id;
    int policy_id;
    int pid;
    String name;
    String date_edited;
    String date_created;
    String policy_name;
    String content;
    String full_name;

    public String getFull_name() {
        return full_name;
    }

    public void setFull_name(String full_name) {
        this.full_name = full_name;
    }

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getPolicy_name() {
        return policy_name;
    }

    public void setPolicy_name(String policy_name) {
        this.policy_name = policy_name;
    }

    public int getPolicy_id() {
        return policy_id;
    }

    public void setPolicy_id(int policy_id) {
        this.policy_id = policy_id;
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

    public String getDate_edited() {
        return date_edited;
    }

    public void setDate_edited(String date_edited) {
        this.date_edited = date_edited;
    }

    public ServiceDescription(int id, int policy_id, int pid, String name, String full_name, String date_created, String date_edited, String policy_name) {
        this.id = id;
        this.policy_id = policy_id;
        this.pid = pid;
        this.name = name;
        this.date_created = date_created;
        this.date_edited = date_edited;
        this.policy_name = policy_name;
        this.full_name = full_name;
    }

    public ServiceDescription(int id, int policy_id, int pid, String name, String full_name, String date_created, String date_edited, String policy_name, String content) {
        this(id, policy_id, pid, name, full_name, date_created, date_edited, policy_name);
        this.content = content;

    }

}
