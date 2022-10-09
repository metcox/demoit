package com.github.metcox.apodeixis.web;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.metcox.apodeixis.kata.Course;
import com.github.metcox.apodeixis.kata.HtmlStep;
import com.github.metcox.apodeixis.kata.Index;
import com.github.metcox.apodeixis.kata.KataCodeExtension;
import com.vladsch.flexmark.ext.youtube.embedded.YouTubeLinkExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.DataSet;
import com.vladsch.flexmark.util.data.MutableDataSet;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController
public class KataController {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @GetMapping({"/kata/{courseName}"})
    public Course kata(@PathVariable String courseName) throws IOException {

        Path courseDir = Paths.get("sample", courseName);
        Path indexFile = courseDir.resolve("index.json");

        Index index = OBJECT_MAPPER.readValue(indexFile.toFile(), Index.class);
//        startBackend(index);
        return buildCourse(courseDir, index);

    }

    @NotNull
    private Course buildCourse(Path courseDir, Index index) throws IOException {

        DataSet options = new MutableDataSet()
                .set(Parser.EXTENSIONS, Arrays.asList(KataCodeExtension.create(), YouTubeLinkExtension.create()))
                .toImmutable();

        // uncomment to set optional extensions
        //options.set(Parser.EXTENSIONS, Arrays.asList(TablesExtension.create(), StrikethroughExtension.create()));

        // uncomment to convert soft-breaks to hard breaks
        //options.set(HtmlRenderer.SOFT_BREAK, "<br />\n");

        Parser parser = Parser.builder(options).build();
        HtmlRenderer renderer = HtmlRenderer.builder(options).build();

        List<Index.Step> steps = index.getDetails().getSteps();

        List<HtmlStep> htmlSteps = new ArrayList<>();

        for (Index.Step step : steps) {
            Path stepFile = courseDir.resolve(step.getText());
            try (BufferedReader reader = Files.newBufferedReader(stepFile)) {
                Node document = parser.parseReader(reader);
                HtmlStep htmlStep = new HtmlStep();
                htmlStep.setTitle(step.getTitle());
                htmlStep.setHtml(renderer.render(document));
                htmlSteps.add(htmlStep);
            }
        }

        Course course = new Course();
        course.setTitle(index.getTitle());
        course.setSteps(htmlSteps);
        return course;
    }

}
