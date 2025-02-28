// Purpose: This file is used to create my own git version named Neo.

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Date;

public class Neo {
    private String objectPath;
    private String headPath;
    private String indexPath;

    private static final String RESET = "\u001B[0m";
    private static final String RED = "\u001B[31m"; // Removed lines (-) in Red
    private static final String GREEN = "\u001B[32m"; // Added lines (+) in Green
    private static final String BLUE = "\u001B[34m"; // Unchanged lines in Blue

    public Neo(String repoPath) {
        this.objectPath = repoPath + "/objects";
        this.headPath = repoPath + "/HEAD";
        this.indexPath = repoPath + "/index";
    }

    public void init() {
        System.out.println("Neo initialized.");

        // Create directory if not exists
        File objectDir = new File(this.objectPath);

        if (!objectDir.exists()) {
            boolean created = objectDir.mkdirs();
            if (created) {
                System.out.println("Object directory created.");
            } else {
                System.out.println("Failed to create object directory.");
            }
        }

        try {
            createFileIfNotExists(this.headPath, "");
            createFileIfNotExists(this.indexPath, "");

            System.out.println("Neo repository created.");
        } catch (IOException e) {
            System.out.println("Failed to create file.");
        }
    }

    public void add(String fileToBeAdded) throws IOException, NoSuchAlgorithmException {
        // Read file content
        String fileData = new String(Files.readAllBytes(Paths.get(fileToBeAdded)), StandardCharsets.UTF_8);

        // Generate file hash
        String fileHash = generateSHA1(fileData);
        System.out.println("File hash: " + fileHash);

        // Write file to object directory
        Path newFilePath = Paths.get(objectPath, fileHash);
        Files.write(newFilePath, fileData.getBytes(StandardCharsets.UTF_8));
        System.out.println("Added " + fileToBeAdded + " to the index");

        // Update the staging area (index file)
        updateStagingArea(fileToBeAdded, fileHash);
    }

    public void commit(String message) throws IOException, NoSuchAlgorithmException {
        System.out.println("Committing changes...");

        // Read index file
        List<String> indexLines = Files.readAllLines(Paths.get(this.indexPath), StandardCharsets.UTF_8);
        if (indexLines.isEmpty()) {
            System.out.println("No files to commit.");
            return;
        }

        // Parse index into a list of file-path and hash mappings
        List<Map<String, String>> files = new ArrayList<>();
        for (String line : indexLines) {
            String[] parts = line.split(" ");
            if (parts.length == 2) {
                Map<String, String> fileEntry = new HashMap<>();
                fileEntry.put("path", parts[0]);
                fileEntry.put("hash", parts[1]);
                files.add(fileEntry);
            }
        }

        // Get parent commit (from HEAD file)
        String parentCommit = "";
        if (Files.exists(Paths.get(this.headPath))) {
            parentCommit = new String(Files.readAllBytes(Paths.get(this.headPath)), StandardCharsets.UTF_8).trim();
        }
        System.out.println("Parent Commit: " + parentCommit);

        // Create commit data
        String commitData = "timeStamp: " + new Date().toInstant() + "\n"
                + "message: " + message + "\n"
                + "files: " + files + "\n"
                + "parent: " + (parentCommit.isEmpty() ? "null" : parentCommit) + "\n";

        // Generate commit hash
        String commitHash = generateSHA1(commitData);
        System.out.println("Commit Hash: " + commitHash);

        // Save commit object in objects directory
        Path commitPath = Paths.get(objectPath, commitHash);
        Files.write(commitPath, commitData.getBytes(StandardCharsets.UTF_8));

        // Update HEAD file with new commit hash
        Files.write(Paths.get(this.headPath), commitHash.getBytes(StandardCharsets.UTF_8));

        // Clear the index file
        Files.write(Paths.get(this.indexPath), new byte[0]);

        System.out.println("Commit successfully created: " + commitHash);
    }

    public void log() throws IOException {
        System.out.println("Fetching commit logs...");

        // Read current commit hash from HEAD file
        Path headPath = Paths.get(this.headPath);
        if (!Files.exists(headPath)) {
            System.out.println("No commits found.");
            return;
        }

        String currentCommitHash = new String(Files.readAllBytes(headPath), StandardCharsets.UTF_8).trim();

        if (currentCommitHash.isEmpty()) {
            System.out.println("No commits found.");
            return;
        }

        while (currentCommitHash != null && !currentCommitHash.isEmpty()) {
            System.out.println("Reading commit: " + currentCommitHash);

            Path commitPath = Paths.get(objectPath, currentCommitHash);
            if (!Files.exists(commitPath)) {
                System.out.println("Commit " + currentCommitHash + " not found.");
                break;
            }

            // Read commit data
            List<String> commitLines = Files.readAllLines(commitPath, StandardCharsets.UTF_8);
            StringBuilder commitContent = new StringBuilder();
            for (String line : commitLines) {
                commitContent.append(line).append("\n");
            }

            // Parse commit data
            String commitData = commitContent.toString();
            System.out.println("Commit: " + currentCommitHash);

            // Extract fields
            String timestamp = extractField(commitData, "timeStamp");
            String message = extractField(commitData, "message");
            String parentCommit = extractField(commitData, "parent");

            System.out.println("Date: " + timestamp);
            System.out.println("Message: " + message);
            System.out.println("Files:");

            // Extract files
            String filesData = extractField(commitData, "files");
            if (filesData != null) {
                String[] files = filesData.replace("[", "").replace("]", "").split(",");
                for (String file : files) {
                    System.out.println("\t" + file.trim());
                }
            }

            System.out.println();

            currentCommitHash = parentCommit.equals("null") ? null : parentCommit;
        }
    }

    public void showCommitDiff(String commitHash) throws IOException {
        // Retrieve commit data
        String commitDataString = getCommitData(commitHash);
        if (commitDataString == null) {
            System.out.println("Commit not found.");
            return;
        }

        Map<String, Object> commitData = parseCommitData(commitDataString);
        System.out.println("Changes in the commit are:");

        @SuppressWarnings("unchecked")
        List<Map<String, String>> files = (List<Map<String, String>>) commitData.get("files");

        for (Map<String, String> file : files) {
            String filePath = file.get("path");
            String fileHash = file.get("hash");

            String fileContent = getFileContent(fileHash);

            if (commitData.containsKey("parent") && commitData.get("parent") != null) {
                String parentCommitHash = (String) commitData.get("parent");
                String parentCommitDataString = getCommitData(parentCommitHash);

                if (parentCommitDataString != null) {
                    Map<String, Object> parentCommitData = parseCommitData(parentCommitDataString);
                    String parentFileContent = getParentFileContent(parentCommitData, filePath);

                    if (parentFileContent != null) {
                        printDiff(parentFileContent, fileContent);
                    } else {
                        System.out.println("New file committed.");
                    }
                } else {
                    System.out.println("Parent commit not found.");
                }
            } else {
                System.out.println("First commit.");
            }
        }
    }

    private void createFileIfNotExists(String filePath, String content) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(content);
            }
        } else {
            System.out.println("Repository already exists.");
        }
    }

    private String getCommitData(String commitHash) throws IOException {
        Path commitPath = Paths.get(objectPath, commitHash);
        if (!Files.exists(commitPath)) {
            return null; // Handle missing commit data properly
        }
        return Files.readString(commitPath, StandardCharsets.UTF_8);
    }

    private Map<String, Object> parseCommitData(String commitDataString) {
        Map<String, Object> commitData = new HashMap<>();
        List<Map<String, String>> files = new ArrayList<>();
        commitData.put("files", files); // Ensure the files list is initialized

        String[] lines = commitDataString.split("\n");

        for (String line : lines) {
            if (line.startsWith("timeStamp:") || line.startsWith("message:") || line.startsWith("parent:")) {
                String[] parts = line.split(":", 2);
                commitData.put(parts[0].trim(), parts[1].trim());
            } else if (line.startsWith("files:")) {
                String fileData = line.substring(6).trim(); // Extract the content after "files:"
                if (fileData.startsWith("[") && fileData.endsWith("]")) {
                    fileData = fileData.substring(1, fileData.length() - 1).trim(); // Remove the square brackets
                }

                // Match multiple file entries of format {path=..., hash=...}
                Pattern pattern = Pattern.compile("\\{path=([^,]+), hash=([^}]+)}");
                Matcher matcher = pattern.matcher(fileData);

                while (matcher.find()) {
                    Map<String, String> fileEntry = new HashMap<>();
                    fileEntry.put("path", matcher.group(1).trim());
                    fileEntry.put("hash", matcher.group(2).trim());
                    files.add(fileEntry);
                }
            }
        }
        return commitData;
    }

    // Method to retrieve the content of a file using its hash
    private String getFileContent(String fileHash) throws IOException {
        Path filePath = Paths.get(objectPath, fileHash);
        return Files.exists(filePath) ? Files.readString(filePath, StandardCharsets.UTF_8) : "";
    }

    // Method to retrieve parent commit's file content
    private String getParentFileContent(Map<String, Object> parentCommitData, String filePath) throws IOException {
        @SuppressWarnings("unchecked")
        List<Map<String, String>> parentFiles = (List<Map<String, String>>) parentCommitData.get("files");

        for (Map<String, String> file : parentFiles) {
            if (file.get("path").equals(filePath)) {
                return getFileContent(file.get("hash"));
            }
        }
        return null;
    }

    private void printDiff(String oldContent, String newContent) {
        String[] oldLines = oldContent.split("\n");
        String[] newLines = newContent.split("\n");

        int oldIndex = 0, newIndex = 0;

        while (oldIndex < oldLines.length || newIndex < newLines.length) {
            if (oldIndex < oldLines.length && newIndex < newLines.length) {
                if (oldLines[oldIndex].equals(newLines[newIndex])) {
                    System.out.println(BLUE + "  " + oldLines[oldIndex] + RESET); // No change
                    oldIndex++;
                    newIndex++;
                } else {
                    System.out.println(RED + "- " + oldLines[oldIndex] + RESET); // Removed line
                    System.out.println(GREEN + "+ " + newLines[newIndex] + RESET); // Added line
                    oldIndex++;
                    newIndex++;
                }
            } else if (oldIndex < oldLines.length) {
                System.out.println(RED + "- " + oldLines[oldIndex] + RESET); // Removed line
                oldIndex++;
            } else if (newIndex < newLines.length) {
                System.out.println(GREEN + "+ " + newLines[newIndex] + RESET); // Added line
                newIndex++;
            }
        }
    }

    // Helper method to extract fields from commit data
    private String extractField(String data, String fieldName) {
        String[] lines = data.split("\n");
        for (String line : lines) {
            if (line.startsWith(fieldName + ":")) {
                return line.substring(fieldName.length() + 1).trim();
            }
        }
        return null;
    }

    private String generateSHA1(String input) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] hashBytes = md.digest(input.getBytes(StandardCharsets.UTF_8));

        // Convert bytes to hex
        StringBuilder hexString = new StringBuilder();
        for (byte b : hashBytes) {
            hexString.append(String.format("%02x", b));
        }
        return hexString.toString();
    }

    private void updateStagingArea(String filePath, String fileHash) throws IOException {
        String entry = filePath + " " + fileHash + System.lineSeparator();
        Files.write(Paths.get(this.indexPath), entry.getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
    }
}
