package com.github.metcox.demoit.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;

@Controller
public class SlideController {

    @GetMapping({"/", "/{slideId:[0-9]+}"})
    public String slide(@PathVariable Optional<Integer> slideId, Map<String, Object> model) throws Exception {
        String content = Files.readString(Paths.get("sample/demoit.html"));  // TODO use application args instead of 'sample'
        String[] split = content.split("---");
        Integer id = slideId.orElse(0);
        Slide slide = new Slide();
        slide.setCurrentSlide(id);
        slide.setContent(split[id]);
        slide.setSlideCount(split.length - 1);
        if (id > 1) {
            slide.setPrevUrl("/" + (id - 1));
        } else if (id > 0) {
            slide.setPrevUrl("/");
        }
        if (id < split.length - 1) {
            slide.setNextUrl("/" + (id + 1));
        }

        model.put("slide", slide);
        return "index";

    }

}
