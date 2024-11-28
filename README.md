# Distributed Storage System

Welcome to the **Distributed Storage System** project repository. This project implements a client-server architecture to manage distributed storage, allowing multiple clients and servers to communicate seamlessly. The system leverages **Java RMI (Remote Method Invocation)** to ensure efficient communication between components.

---

## **Features**

- **Client-Server Architecture**: Supports multiple clients and servers in a distributed setup.
- **Metadata Management**: Centralized metadata management to track files and their respective servers.
- **Dynamic DataServer Initialization**: Automatically starts all existing DataServers based on metadata during system initialization.
- **File Operations**: Upload, download, delete, and refresh files seamlessly across servers.
- **Dynamic File Access**: Clients can query metadata to access files from other DataServers using RMI.
- **Scalability**: Add or remove DataServers dynamically without disrupting operations.

---

## **System Architecture**

The architecture comprises the following key components:

### **1. Client**
- **Responsibility**: Provides a user interface for interacting with the distributed system.
- **Features**:
  - Login and authentication via `MetadataServer`.
  - File upload, download, and deletion.
  - Dynamically connects to DataServers for file operations.
  - Displays all files across DataServers by querying `MetadataServer`.

---

### **2. DataServer**
- **Responsibility**: Manages storage operations and serves files on request.
- **Features**:
  - Handles local file storage for uploaded files.
  - Registers itself with the `MetadataServer` during initialization.
  - Provides file fetching, storage, and deletion capabilities to clients via RMI.
  - Supports dynamic refresh to update metadata with newly added or removed files.

---

### **3. MetadataServer**
- **Responsibility**: Acts as a central registry for all DataServers and files in the system.
- **Features**:
  - Tracks metadata for all files, including ownership, permissions, and associated DataServers.
  - Provides querying capabilities for clients to locate files.
  - Ensures system integrity by synchronizing metadata across all DataServers.
  - Stores and manages user credentials for authentication.

---

## **System Workflow**

1. **User Login**:
   - Clients authenticate with the `MetadataServer`.
   - On successful login, the main UI displays all files in the system.

2. **File Upload**:
   - The client connects to a DataServer.
   - Files are uploaded to the DataServer and registered in the metadata.

3. **File Download**:
   - The client queries the `MetadataServer` to locate the file.
   - The file is fetched directly from the respective DataServer using RMI.

4. **Dynamic Server Interaction**:
   - Clients can dynamically connect to different DataServers for operations.
   - The system supports adding new DataServers without downtime.

5. **DataServer Initialization**:
   - On system startup, all DataServers listed in the metadata are initialized automatically.

---
## **How to Run**

1. Clone the repository:
   ```bash
   git clone https://github.com/Mostafa-Abedi/Distributed-Storage-System.git
   cd Distributed-Storage-System
   ./run
   ```
---

## **Pros and Cons**

### **Pros**
- **Scalability**: Easily add or remove DataServers.
- **Cost-Effective**: Minimal infrastructure required.
- **Dynamic Access**: Clients can access files across servers using metadata queries.
- **Simplicity**: Clear and modular architecture using Java RMI.

### **Cons**
- **Metadata Single Point of Failure**: The `MetadataServer` is critical for operations.
- **No Replication**: Files are not replicated across servers, reducing fault tolerance.
- **Limited Security**: Basic permissions management without encryption.
- **Performance Overhead**: RMI introduces latency compared to modern protocols.

---

## **Future Improvements**

1. **Fault Tolerance**: Implement file replication across DataServers.
2. **Distributed Metadata**: Replace the centralized `MetadataServer` with a distributed metadata store.
3. **Enhanced Security**: Add encryption for file transfers and more granular access control.
4. **Modern Protocols**: Upgrade communication to gRPC for improved performance.
5. **Advanced Features**: Add versioning, snapshots, and audit logs.

---

## **Demo Presentation**

### **Slide Topics**:
1. **Introduction**: Overview of the system.
2. **System Architecture**: Breakdown of components and responsibilities.
3. **Workflow**: Detailed explanation of client and server interactions.
4. **Key Features**: Highlight what makes this system unique.
5. **Demo Walkthrough**: Show live examples of file operations.
6. **Comparison**: Compare with existing systems (e.g., HDFS, S3).
7. **Evaluation**: Discuss performance, scalability, and security.
8. **Future Work**: Outline potential improvements.
9. **Q&A Session**: Open the floor for questions.

---

## **Contributors**
- **Mostafa Abedi**
- **Calvin Reveredo**
- **Naftanan Mamo**
- **Rodney Stanislaus**

---

## **License**
This project is licensed under the MIT License. See [LICENSE](LICENSE) for details.
