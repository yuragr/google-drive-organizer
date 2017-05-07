package com.yuri;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.FileList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.yuri.model.RemoteFile;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by yurig on 06-May-17.
 */
@Component
public class GoogleDriveOrganizer {

    public static final String DRIVE_FOLDER = "drive";
    @Autowired
    Drive googleDriveService;

    private Map<String, RemoteFile> files; // ID -> Children
    private Gson gson;

    @PostConstruct
    private void init() {
        gson = new GsonBuilder().create();
        files = new HashMap<>();
    }

    public void getFileDescriptors() throws IOException {
        FileList result;
        String pageToken = null;
        do {
            // Print the names and IDs for up to 10 files.
            result = googleDriveService.files().list()
                    .setPageSize(1000)
                    .setFields("nextPageToken, files(id, md5Checksum, name, parents, size)")
                    .setPageToken(pageToken)
                    .execute();
            List<com.google.api.services.drive.model.File> googleDriveFiles = result.getFiles();
            if (googleDriveFiles == null || googleDriveFiles.size() == 0) {
                System.out.println("No files found.");
            } else {
                for (com.google.api.services.drive.model.File file : googleDriveFiles) {
                    RemoteFile remoteFile = new RemoteFile.RemoteFileBuilder().fromGoogleFile(file).build();
//                    System.out.println(remoteFile);
                    files.put(remoteFile.getId(), remoteFile);
                }
            }
            pageToken = result.getNextPageToken();
        } while (StringUtils.isNotBlank(pageToken));

        // generate file structure
        File driveFolder = new File(DRIVE_FOLDER);
        if (!driveFolder.isDirectory()) {
            driveFolder.mkdir();
        }

        for (RemoteFile remoteFile : files.values()) {
            if (StringUtils.isNotBlank(remoteFile.getMd5()) && remoteFile.getSize() > 0) {
                for (String parentId : remoteFile.getParents()) {
                    String path = getFilePath(parentId);
                    File localFile = new File(path, remoteFile.getName() + ".json");
                    localFile.getParentFile().mkdirs();

                    try (FileWriter writer = new FileWriter(localFile)) {
                        gson.toJson(remoteFile, writer);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

    private String getFilePath(String parentId) {
        List<String> folders = new ArrayList<>();

        if (StringUtils.isNotBlank(parentId)) {
            Set<String> visitedParents = new HashSet<>();

            RemoteFile parent = files.get(parentId);
            while (parent != null) {
                folders.add(0, parent.getName());
                visitedParents.add(parent.getId());
                if (CollectionUtils.isEmpty(parent.getParents())) {
                    break;
                } else if (parent.getParents().size() > 1) {
                    throw new RuntimeException("Unsupported case, the file '" + parent + "' has more than one parent");
                } else {
                    parent = files.get(parent.getParents().iterator().next());
                    if (parent != null && visitedParents.contains(parent.getId())) {
                        throw new RuntimeException("loop!");
                    }
                }
            }
        }
        folders.add(0, DRIVE_FOLDER);
        return folders.stream().collect(Collectors.joining("\\"));
    }
}
