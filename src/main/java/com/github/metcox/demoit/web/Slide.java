package com.github.metcox.demoit.web;

public class Slide {

    private String content = "";
    private String prevUrl = "";
    private String nextUrl = "";
    private int currentSlide;
    private int slideCount;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getPrevUrl() {
        return prevUrl;
    }

    public void setPrevUrl(String prevUrl) {
        this.prevUrl = prevUrl;
    }

    public String getNextUrl() {
        return nextUrl;
    }

    public void setNextUrl(String nextUrl) {
        this.nextUrl = nextUrl;
    }

    public int getCurrentSlide() {
        return currentSlide;
    }

    public void setCurrentSlide(int currentSlide) {
        this.currentSlide = currentSlide;
    }

    public int getSlideCount() {
        return slideCount;
    }

    public void setSlideCount(int slideCount) {
        this.slideCount = slideCount;
    }
}
