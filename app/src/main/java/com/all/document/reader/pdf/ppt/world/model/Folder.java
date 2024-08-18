package com.all.document.reader.pdf.ppt.world.model;

public class Folder {

    private String title;
    private int thumbnail;

    public Folder(String title, int thumbnail){
        this.title = title;
        this.thumbnail = thumbnail;
    }

    public int getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(int thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
