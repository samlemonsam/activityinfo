package org.activityinfo.legacy.shared.command;

import com.extjs.gxt.ui.client.data.RpcMap;

import java.util.ArrayList;
import java.util.List;

/**
 * Creates or updates one or more resources
 */
public class UpdateResource {
    
    public static class Update {
        private String id;
        private Boolean deleted;
        private RpcMap properties;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public Boolean getDeleted() {
            return deleted;
        }

        public void setDeleted(Boolean deleted) {
            this.deleted = deleted;
        }

        public RpcMap getProperties() {
            return properties;
        }

        public void setProperties(RpcMap properties) {
            this.properties = properties;
        }
    }
    
    private final List<Update> updates = new ArrayList<>();

    public List<Update> getUpdates() {
        return updates;
    }
}
