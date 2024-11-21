package server.metadata;

import java.io.Serializable;

public class FileMetadata implements Serializable {
    private final String dataServer;
    private final String owner;
    private final String permissions;

    public FileMetadata(String dataServer, String owner, String permissions) {
        this.dataServer = dataServer;
        this.owner = owner;
        this.permissions = permissions;
    }

    public String getDataServer() {
        return dataServer;
    }

    public String getOwner() {
        return owner;
    }

    public String getPermissions() {
        return permissions;
    }
}
