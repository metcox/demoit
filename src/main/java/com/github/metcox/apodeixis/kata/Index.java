package com.github.metcox.apodeixis.kata;

import java.util.ArrayList;
import java.util.List;

public class Index {

    private String title;

    private String description;

    private Details details = new Details();

    private Backend backend = new Backend();

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Details getDetails() {
        return details;
    }

    public void setDetails(Details details) {
        this.details = details;
    }

    public Backend getBackend() {
        return backend;
    }

    public void setBackend(Backend backend) {
        this.backend = backend;
    }

    public static class Details {

        private List<Step> steps = new ArrayList<>();

        public List<Step> getSteps() {
            return steps;
        }

        public void setSteps(List<Step> steps) {
            this.steps = steps;
        }
    }

    public static class Step {

        private String title;

        private String text;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }
    }

    public static class Backend {

        private String imageid;

        public String getImageid() {
            return imageid;
        }

        public void setImageid(String imageid) {
            this.imageid = imageid;
        }
    }

}
