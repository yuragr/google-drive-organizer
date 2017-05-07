package com.yuri;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.yuri.model.RemoteFile;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.*;

/**
 * Created by yurig on 07-May-17.
 */
@Component
public class LocalOrganizer {
    public static final String DRIVE_FOLDER = "drive";

    private Gson gson;
    private Map<String, List<RemoteFile>> md5ToFile;

    @PostConstruct
    private void init() {
        gson = new GsonBuilder().create();
        md5ToFile = new HashMap<>();
    }

    public void generateDuplicatesReport() {
        File rootFolder = new File(DRIVE_FOLDER);
        if (!rootFolder.isDirectory()) {
            throw new RuntimeException("Could not find folder '" + DRIVE_FOLDER + "'");
        }
        readFiles(rootFolder);

        long sizeToBeSaved = 0;
        long duplicateFiles = 0;

        for (String md5 : md5ToFile.keySet()) {

            List<RemoteFile> remoteFiles = md5ToFile.get(md5);
            if (remoteFiles.size() > 1) {
                System.out.println(md5);
                remoteFiles.stream().map(RemoteFile::getPath).sorted().forEach(System.out::println);
                System.out.println();
                sizeToBeSaved += remoteFiles.get(0).getSize() * (remoteFiles.size() - 1);
                duplicateFiles += (remoteFiles.size() - 1);
            }
        }
        System.out.println("size to be saved in bytes: " + sizeToBeSaved);
        System.out.println("duplicate files: " + duplicateFiles);
    }

    private void readFiles(File folder) {
        Queue<File> queue = new ArrayDeque<>();
        queue.offer(folder);
        while (!queue.isEmpty()) {
            File file = queue.poll();
            if (file.isDirectory()) {
                Arrays.stream(file.listFiles()).forEach(queue::offer);
            } else {
                if (file.exists()) {
                    RemoteFile remoteFile = readJsonFile(file);
                    if (StringUtils.isNotBlank(remoteFile.getMd5())) {
                        md5ToFile.putIfAbsent(remoteFile.getMd5(), new ArrayList<>());
                        md5ToFile.get(remoteFile.getMd5()).add(remoteFile);
                    }
                }
            }
        }
    }

    public RemoteFile readJsonFile(File file) {
        try (Reader reader = new FileReader(file)) {

            // Convert JSON to Java Object
            RemoteFile remoteFile = gson.fromJson(reader, RemoteFile.class);
            remoteFile.setPath(file.getAbsolutePath().substring(0, file.getAbsolutePath().length() - 5)); // -5 in order to remove the JSON extension
            return remoteFile;
        } catch (IOException e) {
            throw new RuntimeException("Could not open file '" + file.getAbsolutePath() + "'", e);
        }
    }
}
