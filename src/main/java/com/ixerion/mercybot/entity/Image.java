package com.ixerion.mercybot.entity;

public class Image {

    private String href;
    private String name;

    public Image(String href, String name) {
        this.href = href;
        this.name = name;
    }

    public Image(String href) {
        this.href = href;
    }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
