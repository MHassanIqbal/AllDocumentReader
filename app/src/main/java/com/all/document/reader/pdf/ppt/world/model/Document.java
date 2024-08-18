package com.all.document.reader.pdf.ppt.world.model;

import java.io.File;

public class Document {

    private File file;
    private boolean isImportant;

    public Document(File file, boolean isImportant) {
        this.file = file;
        this.isImportant = isImportant;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public boolean isImportant() {
        return isImportant;
    }

    public void setImportant(boolean important) {
        isImportant = important;
    }
}
