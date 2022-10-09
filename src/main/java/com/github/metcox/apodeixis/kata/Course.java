package com.github.metcox.apodeixis.kata;

import java.util.List;

public class Course {

    private String title;
    private List<HtmlStep> steps;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<HtmlStep> getSteps() {
        return steps;
    }

    public void setSteps(List<HtmlStep> steps) {
        this.steps = steps;
    }
}
