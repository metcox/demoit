package com.github.metcox.demoit.web;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.springframework.http.ResponseEntity.ok;

@Controller
public class CodeController {


    @GetMapping({"/sourceCode/{*filePath}"})
    public ResponseEntity<String> shell(@PathVariable String filePath) throws IOException {
        Path source = Paths.get("sample", filePath).toAbsolutePath();
        return ok(Files.readString(source));
    }
}
