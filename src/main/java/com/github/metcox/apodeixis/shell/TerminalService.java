package com.github.metcox.apodeixis.shell;

import com.pty4j.PtyProcess;
import com.pty4j.PtyProcessBuilder;
import com.pty4j.WinSize;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.LinkedBlockingQueue;

public class TerminalService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TerminalService.class);

    private boolean isReady;
    private PtyProcess process;
    private Integer columns = 10;
    private Integer rows = 10;
    private BufferedReader inputReader;
    private BufferedReader errorReader;
    private BufferedWriter outputWriter;
    private final WebSocketSession webSocketSession;

    private final LinkedBlockingQueue<String> commandQueue = new LinkedBlockingQueue<>();

    public TerminalService(WebSocketSession webSocketSession) {
        this.webSocketSession = webSocketSession;
    }

    public void onTerminalInit() {
        LOGGER.info("onTerminalInit");
    }

    public void onTerminalReady() throws Exception {
        initializeProcess();
    }

    private void initializeProcess() throws Exception {
        if (isReady) {
            return;
        }

        String userHome = retrieveUserHome(webSocketSession);
        Map<String, String> environment = buildEnvironment(userHome);
        String[] termCommand = buildCommand(userHome, environment);

        this.process = new PtyProcessBuilder(termCommand)
                .setEnvironment(environment)
                .setDirectory(userHome)
                .start();

        process.setWinSize(new WinSize(columns, rows));
        this.inputReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        this.errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        this.outputWriter = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));

        TerminalService.startThread(() -> printReader(inputReader));

        TerminalService.startThread(() -> printReader(errorReader));
        this.isReady = true;

    }

    private String retrieveUserHome(WebSocketSession webSocketSession) {
        // TODO use application args instead of relying on the current dir
        String rootFolder = "sample";
        String subFolder = StringUtils.substringAfterLast(webSocketSession.getUri().getPath(), "/");
        subFolder = StringUtils.defaultIfBlank(subFolder, "");
        return Paths.get(rootFolder, subFolder).toString();
    }

    private Map<String, String> buildEnvironment(String userHome) {
        HashMap<String, String> env = new HashMap<>(System.getenv());
        env.put("TERM", "xterm");
        // TODO use application args instead of relying on the current dir
        String rootFolder = "sample";
        Path historyFile = Paths.get(rootFolder, ".apodeixis", ".bash_history");
        if (Files.exists(historyFile)) {
            // Bash history needs to be copied because it's going to be
            // modified by the shell.
            try {
                Path tempFile = Files.createTempFile(null, null);
                FileUtils.copyFile(historyFile.toFile(), tempFile.toFile());
                env.put("HISTFILE", tempFile.toString());
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        return env;
    }

    private String[] buildCommand(String userHome, Map<String, String> environment) {

        // TODO use application args instead of relying on the current dir
        String rootFolder = "sample";
        Path sourceFile = Paths.get(rootFolder, ".apodeixis", ".bashrc");
        if (Files.exists(sourceFile)) {
            return new String[]{"/bin/bash", "--rcfile", sourceFile.toFile().getAbsolutePath()};
        }
        return new String[]{"/bin/bash"};
    }


    private void printReader(BufferedReader bufferedReader) {
        try {
            int nRead;
            char[] data = new char[10 * 1024];

            while ((nRead = bufferedReader.read(data, 0, data.length)) != -1) {
                print(String.valueOf(data, 0, nRead));
            }

        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    private void print(String text) throws IOException {
        webSocketSession.sendMessage(new TextMessage(text));
    }

    public void onCommand(String command) {
        if (null == command || StringUtils.isEmpty(command)) {
            return;
        }

        try {
            commandQueue.put(command);
        } catch (InterruptedException e) {
            LOGGER.error(e.getMessage(), e);
        }

        TerminalService.startThread(() -> {
            try {
                outputWriter.write(commandQueue.poll());
                outputWriter.flush();
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
            }
        });

    }

    public void onTerminalResize(String columns, String rows) {
        if (Objects.nonNull(columns) && Objects.nonNull(rows)) {
            this.columns = Integer.valueOf(columns);
            this.rows = Integer.valueOf(rows);

            if (Objects.nonNull(process)) {
                process.setWinSize(new WinSize(this.columns, this.rows));
            }

        }
    }

    public void onTerminalClose() {
        LOGGER.info("onTerminalClose");
        if (null != process && process.isAlive()) {
            process.destroy();
            closeQuietly(outputWriter);
            closeQuietly(inputReader);
            closeQuietly(errorReader);
        }
    }

    private static void closeQuietly(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (final IOException ioe) {
            // ignore
        }
    }

    private static void startThread(Runnable runnable) {
        Thread thread = new Thread(runnable);
        thread.start();
    }

}