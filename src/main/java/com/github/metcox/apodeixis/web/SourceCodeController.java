package com.github.metcox.apodeixis.web;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.python.util.PythonInterpreter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static org.apache.commons.lang.StringUtils.EMPTY;
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

        // remove leading slash, see org.springframework.web.util.pattern.PathPattern for {*filePath} syntax usage.
        filePath = filePath.substring(1);

        LOGGER.info("retrieving file {} for hash {} with {} color style and selected lines from {} to {}", filePath, hash, style, startLine, endLine);

        // TODO use application args instead of 'sample'
        Path ceilingDirectory = Paths.get("sample").toAbsolutePath();
        Path sourceFile = ceilingDirectory.resolve(filePath);
        String code = retrieveFileContent(hash, ceilingDirectory, sourceFile);

        String html = generatedHtmlWithCssStyle(style, startLine, endLine, sourceFile, code);
        return ok(html);

    }

    private String retrieveFileContent(String hash, Path ceilingDirectory, Path sourceFile) {
        try {
            if (isNotBlank(hash)) {
                return readContentFromGitRepository(ceilingDirectory, sourceFile, hash);
            }
            return Files.readString(sourceFile);
        } catch (Exception e) {
            LOGGER.warn("unable to read content of file {}", sourceFile);
        }
        return EMPTY;
    }

    private String readContentFromGitRepository(Path ceilingDirectory, Path sourceFile, String hash) throws IOException {
        // retrieve the git repository
        try (Repository repo = new FileRepositoryBuilder()
                .setMustExist(true)
                // CeilingDirectory must be set before calling findGitDir
                .addCeilingDirectory(ceilingDirectory.toFile())
                .findGitDir(sourceFile.toFile())
                .build()) {

            // relative path against git repo
            String path = repo.getWorkTree().toPath().relativize(sourceFile).toString();
            // use '/' to delimit directories, see org.eclipse.jgit.treewalk.filter.PathFilter#create
            path = FilenameUtils.separatorsToUnix(path);

            // retrieve the commit for the given hash in the repo
            ObjectId commitId = repo.resolve(hash);
            if (commitId == null) {
                throw new IllegalArgumentException("hash not found.");
            }
            RevCommit commit = repo.parseCommit(commitId);

            // retrieve the file content for the commit
            try (TreeWalk walk = TreeWalk.forPath(repo, path, commit.getTree())) {
                if (walk == null) {
                    throw new IllegalArgumentException("path not found.");
                }
                byte[] bytes = repo.open(walk.getObjectId(0)).getBytes();
                return new String(bytes, StandardCharsets.UTF_8);
            }
        }
    }

    private String generatedHtmlWithCssStyle(String style, String startLine, String endLine, Path sourceFile, String code) {

        PythonInterpreter interpreter = new PythonInterpreter();
        interpreter.set("code", code);

        List<Integer> lines = getLinesToSelect(startLine, endLine);

        interpreter.exec("from pygments import highlight\n"
                + "from pygments.lexers import get_lexer_for_filename\n"
                + "from pygments.lexers import TextLexer\n"
                + "from pygments.formatters import HtmlFormatter\n"
                + "try:\n"
                + "    lexer = get_lexer_for_filename(\"" + sourceFile.toFile().getName() + "\")\n"
                + "except:\n"
                + "    lexer = TextLexer()\n"
                + "formatter = HtmlFormatter(style=\"" + style + "\", linenos=\"inline\", hl_lines=" + lines + ")\n"
                + "html = highlight(code, lexer, formatter)\n"
                + "css = formatter.get_style_defs('.highlight')");

        // Get results that has been set in variables
        String html = interpreter.get("html", String.class);
        String css = interpreter.get("css", String.class);

        return "<style>\n" + css + "\n</style>\n" + html;
    }

    private List<Integer> getLinesToSelect(String startLine, String endLine) {
        List<Integer> lines = new ArrayList<>();
        if (isNotBlank(startLine) && isNotBlank(endLine)) {
            String[] starts = StringUtils.split(startLine, ",");
            String[] ends = StringUtils.split(endLine, ",");
            if (starts.length == ends.length) {
                for (int i = 0; i < starts.length; i++) {
                    IntStream.rangeClosed(Integer.parseInt(starts[i]), Integer.parseInt(ends[i])).forEach((lines::add));
                }
            }
        }
        return lines;
    }
}
