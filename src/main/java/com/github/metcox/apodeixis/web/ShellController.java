package com.github.metcox.apodeixis.web;

import com.github.metcox.apodeixis.conf.AppProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Controller
public class ShellController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ShellController.class);

    private final AppProperties appProperties;

    public ShellController(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    @GetMapping({"/shell/{folder}"})
    public ResponseEntity<String> shell(@PathVariable String folder) {
        Path path = Paths.get("."); // TODO use application args instead of 'sample'
        if (!folder.equals(".")) {
            path = path.resolve(folder);
        }
        List<String> commands;
        try {
            commands = commands(path);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        UriComponentsBuilder uriBuilder = UriComponentsBuilder.newInstance();
        URI uri = uriBuilder
                .scheme("http")
                .host("localhost")
                .port(8080)
                .path("shellSocket")
                .queryParam("arg", "{commands}")
                .build(String.join(";", commands));

        return ResponseEntity.status(HttpStatus.SEE_OTHER).location(uri).build();

    }

    private List<String> commands(Path path) {
        List<String> commands = new ArrayList<>();
        commands.add("cd " + path);

        String shell = System.getenv("SHELL");
        if (shell == null) {
            shell = "bash";
        }

        LOGGER.info("Using shell {}", shell);

        // Source custom .bashrc
        // TODO use application args instead of 'sample'
        Path bashRc = Paths.get("sample", ".apodeixis", ".bashrc").toAbsolutePath();

        if (Files.exists(bashRc)) {
            LOGGER.info("Using bashrc file {}", bashRc);
            commands.add("source " + bashRc);
        }

        // Bash history needs to be copied because it's going to be modified by the shell.
        Path bashHistory = copyFile(".bash_history");
        if (bashHistory != null) {
            LOGGER.info("Using history {}", bashHistory);
            commands.add("HISTFILE=" + bashHistory + " exec " + shell);
        } else {
            commands.add("exec " + shell);
        }

        return commands;
    }

    private Path copyFile(String file) {

        String content;
        try {
            // TODO use application args instead of 'sample'
            content = Files.readString(Paths.get("sample", ".apodeixis", file));
        } catch (IOException e) {
            LOGGER.warn("Unable to read file {}", file, e);
            return null;
        }

        Path tmpFile;
        try {
            tmpFile = Files.createTempFile("demoit", "");
        } catch (IOException e) {
            LOGGER.warn("Unable to create temp file", e);
            return null;
        }

        try {
            Files.writeString(tmpFile, content);
        } catch (IOException e) {
            LOGGER.warn("Unable to write file", e);
            return null;
        }

        return tmpFile;
    }

}
