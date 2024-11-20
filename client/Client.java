package client;

import interfaces.FileService;

import java.rmi.Naming;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) {
        try {
            FileService service = (FileService) Naming.lookup("rmi://localhost/MetadataServer");

            Scanner scanner = new Scanner(System.in);

            // Upload file
            System.out.println("Enter file name to upload:");
            String fileName = scanner.nextLine();
            System.out.println("Enter file content:");
            String content = scanner.nextLine();
            boolean created = service.createFile(fileName, "Mostafa");
            if (created) {
                service.storeFile(fileName, content.getBytes());
            }

            // Read file
            System.out.println("Enter file name to read:");
            fileName = scanner.nextLine();
            byte[] fileContent = service.readFile(fileName);
            System.out.println("File content: " + (fileContent != null ? new String(fileContent) : "Not found"));

            // Delete file
            System.out.println("Enter file name to delete:");
            fileName = scanner.nextLine();
            boolean deleted = service.deleteFile(fileName);
            System.out.println("File deletion status: " + deleted);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}