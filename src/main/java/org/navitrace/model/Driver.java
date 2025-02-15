
package org.navitrace.model;

import org.navitrace.storage.StorageName;

@StorageName("tc_drivers")
public class Driver extends ExtendedModel {

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private String uniqueId;

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId.trim();
    }

}
