package org.broker.orbi.models;

/**
 *
 * @author Christos Paraskeva <ch.paraskeva at gmail dot com>
 */
public class ProviderDashboardInfo {

    String numOfServicedesc;
    String numOfImages;
    String numOfFlavors;
    String numOfOfferings;

    public ProviderDashboardInfo() {

    }

    public String getNumOfServicedesc() {
        return numOfServicedesc;
    }

    public void setNumOfServicedesc(String numOfServicedesc) {
        this.numOfServicedesc = numOfServicedesc;
    }

    public String getNumOfImages() {
        return numOfImages;
    }

    public void setNumOfImages(String numOfImages) {
        this.numOfImages = numOfImages;
    }

    public String getNumOfFlavors() {
        return numOfFlavors;
    }

    public void setNumOfFlavors(String numOfFlavors) {
        this.numOfFlavors = numOfFlavors;
    }

    public String getNumOfOfferings() {
        return numOfOfferings;
    }

    public void setNumOfOfferings(String numOfOfferings) {
        this.numOfOfferings = numOfOfferings;
    }

    public ProviderDashboardInfo(String numOfServicedesc,  String numOfFlavors ,String numOfImages, String numOfOfferings) {
        this.numOfServicedesc = numOfServicedesc;
        this.numOfImages = numOfImages;
        this.numOfFlavors = numOfFlavors;
        this.numOfOfferings = numOfOfferings;
    }

}
