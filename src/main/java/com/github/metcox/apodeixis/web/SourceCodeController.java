package com.github.metcox.apodeixis.web;

import org.apache.commons.lang.StringUtils;
import org.python.util.PythonInterpreter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.springframework.http.ResponseEntity.ok;

@Controller
public class SourceCodeController {

    private static final Logger LOGGER = LoggerFactory.getLogger(SourceCodeController.class);

    @GetMapping({"/sourceCode/{*filePath}"})
    public ResponseEntity<String> shell(
            @PathVariable String filePath,
            @RequestParam(required = false) String hash,
            @RequestParam(defaultValue = "vs") String style,
            @RequestParam(required = false) String startLine,
            @RequestParam(required = false) String endLine
    ) throws IOException {

        LOGGER.info("{} {} {} {} {}", filePath, hash, style, startLine, endLine);

        // TODO use application args instead of 'sample'
        Path sourceFile = Paths.get("sample", filePath).toAbsolutePath();
        String code = Files.readString(sourceFile);
        PythonInterpreter interpreter = new PythonInterpreter();

        interpreter.set("code", code);

        List<Integer> lines = new ArrayList<>();
        if (isNotBlank(startLine) && isNotBlank(endLine)) {
            String[] starts = StringUtils.split(startLine, ",");
            String[] ends = StringUtils.split(endLine, ",");
            if (starts.length == ends.length) {
                for (int i = 0; i < starts.length; i++) {
                    IntStream.rangeClosed(Integer.valueOf(starts[i]), Integer.valueOf(ends[i])).forEach((lines::add));
                }
            }
        }

        interpreter.exec("from pygments import highlight\n"
                + "from pygments.lexers import get_lexer_for_filename\n"
                + "from pygments.formatters import HtmlFormatter\n"
                + "lexer = get_lexer_for_filename(\"" + sourceFile.getName(sourceFile.getNameCount() - 1) + "\")\n"
                + "formatter = HtmlFormatter(style=\"" + style + "\", linenos=\"inline\", hl_lines=" + lines + ")\n"
                + "formatted = highlight(code, lexer, formatter)\n"
                + "css = formatter.get_style_defs('.highlight')");

// Get the result that has been set in a variable
        String formatted = interpreter.get("formatted", String.class);
        String css = interpreter.get("css", String.class);

        return ok("<style>\n" + css + "</style>\n" + formatted);
    }
}
