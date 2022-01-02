package com.github.metcox.apodeixis.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.stream.IntStream;

@Controller
public class GridController {

    @GetMapping({"/grid"})
    public String grid(Map<String, Object> model) throws Exception {
        String content = Files.readString(Paths.get("sample/demoit.html"));  // TODO use application args instead of 'sample'
        String[] split = content.split("---");

        model.put("indexes", IntStream.range(0, split.length).toArray());
        return "grid";
    }
}
