package server.metadata;

import java.io.Serializable;

public class FileMetadata implements Serializable {
    private final String dataServer;
    private final String owner;

    public FileMetadata(String dataServer, String owner) {
        this.dataServer = dataServer;
        this.owner = owner;
    }

    public String getDataServer() {
        return dataServer;
    }

    public String getOwner() {
        return owner;
    }
}
