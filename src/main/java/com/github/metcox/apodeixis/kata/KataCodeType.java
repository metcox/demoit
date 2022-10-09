package com.github.metcox.apodeixis.kata;

public enum KataCodeType {

    copy("Copy To Clipboard"), execute("Run Command"), open("Open File");

    private String title;

    private KataCodeType(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
