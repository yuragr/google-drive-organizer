package com.yuri.model;

import com.google.api.services.drive.model.File;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yurig on 06-May-17.
 */
public class RemoteFile implements Serializable {
    private String id;
    private String name;
    private List<String> parents;
    private long size;
    private String md5;
    private String path;

    private RemoteFile() {
        parents = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getParents() {
        return parents;
    }

    public void setParents(List<String> parents) {
        if (parents == null) {
            this.parents = new ArrayList<>();
        } else {
            this.parents = parents;
        }
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        RemoteFile that = (RemoteFile) o;

        return new org.apache.commons.lang3.builder.EqualsBuilder()
                .append(size, that.size)
                .append(id, that.id)
                .append(name, that.name)
                .append(parents, that.parents)
                .append(md5, that.md5)
                .isEquals();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", id)
                .append("name", name)
                .append("parents", parents)
                .append("size", size)
                .append("md5", md5)
                .append("path", path)
                .toString();
    }

    @Override
    public int hashCode() {
        return new org.apache.commons.lang3.builder.HashCodeBuilder(17, 37)
                .append(id)
                .append(md5)
                .toHashCode();
    }

    public static class RemoteFileBuilder {
        private RemoteFile driveFile = new RemoteFile();

        public RemoteFileBuilder fromGoogleFile(File file) {
            driveFile.setId(file.getId());
            driveFile.setName(file.getName());
            driveFile.setMd5(file.getMd5Checksum());
            driveFile.setParents(file.getParents());
            driveFile.setSize(file.getSize() != null ? file.getSize() : 0);
            return this;
        }

        public RemoteFileBuilder setId(String id) {
            driveFile.setId(id);
            return this;
        }

        public RemoteFileBuilder setName(String name) {
            driveFile.setName(name);
            return this;
        }

        public RemoteFileBuilder setParents(List<String> parents) {
            driveFile.setParents(parents);
            return this;
        }

        public RemoteFileBuilder setSize(long size) {
            driveFile.setSize(size);
            return this;
        }

        public RemoteFileBuilder setMd5(String md5) {
            driveFile.setMd5(md5);
            return this;
        }

        public RemoteFileBuilder setPath(String path) {
            driveFile.setPath(path);
            return this;
        }

        public RemoteFile build() {
            return driveFile;
        }
    }
}
