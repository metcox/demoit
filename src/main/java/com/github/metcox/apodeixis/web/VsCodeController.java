package com.github.metcox.apodeixis.web;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.PullImageCmd;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.PullResponseItem;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.atomic.AtomicBoolean;

@Controller
public class VsCodeController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ShellController.class);
    private static AtomicBoolean init = new AtomicBoolean();

    RestTemplate restTemplate;


    public VsCodeController(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    @GetMapping({"/vscode/{folder}"})
    public ResponseEntity<String> shell(@PathVariable String folder) throws Exception {

        if (!init.compareAndExchange(false, true)) {
            startServer();
        }

        // Wait for vscode to start as much as we can.
        // Don't error out if it can't be started.
        for (int i = 0; i < 10; i++) {
            try {
                restTemplate.execute("http://localhost:18080/", HttpMethod.HEAD, null, ClientHttpResponse::getRawStatusCode);
                break;
            } catch (HttpStatusCodeException e) {
                // well, we receive a response so no need to wait more
                break;
            } catch (Exception e) {
                // not good
            }
            Thread.sleep(500);
        }

        UriComponentsBuilder uriBuilder = UriComponentsBuilder.newInstance();
        URI uri = uriBuilder
                .scheme("http")
                .host("localhost")
                .port(18080)
                .queryParam("folder", "{path}")
                .build("/app/" + folder);

        return ResponseEntity.status(HttpStatus.SEE_OTHER).location(uri).build();
    }

    private void startServer() {
        //        vscode.Start()
        DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder().build();
        DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
                .dockerHost(config.getDockerHost())
                .sslConfig(config.getSSLConfig())
                .maxConnections(100)
                .build();
        DockerClient dockerClient = DockerClientImpl.getInstance(config, httpClient);

        try {
            dockerClient
                    .removeContainerCmd("demoit-vscode")
                    .withForce(true)
                    .exec();
        } catch (Exception ignore) {
            //
        }

        PullImageCmd pullImageCmd = dockerClient.pullImageCmd("codercom/code-server:3.4.1");
        ResultCallback.Adapter<PullResponseItem> pullResponseCallback = pullImageCmd.start();
        try {
            pullResponseCallback.awaitCompletion();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        CreateContainerCmd createContainerCmd = dockerClient
                .createContainerCmd("codercom/code-server:3.4.1")
                .withName("demoit-vscode")
                .withCmd("--auth=none", "--disable-telemetry")
                .withExposedPorts(ExposedPort.tcp(8080));
        createContainerCmd
                .withUser("coder")
                .getHostConfig()
                .withPortBindings(PortBinding.parse("18080:8080"))
//                .withBinds(Bind.parse("demoit-vscode-vol:/app"))
        ;
        CreateContainerResponse container = createContainerCmd.exec();

        dockerClient.startContainerCmd(container.getId()).exec();

        Path outputPath = null;
        try {
            Path inputPath = Paths.get("sample");
            outputPath = Files.createTempFile("docker-java", ".tar.gz");
            createTarGzipFolder(inputPath, outputPath);
            FileInputStream fileInputStream = FileUtils.openInputStream(outputPath.toFile());
            dockerClient.copyArchiveToContainerCmd(container.getId()).withTarInputStream(fileInputStream).withRemotePath("/app").exec();
        } catch (IOException createFileIOException) {
            //
        } finally {
            if (outputPath != null) {
                outputPath.toFile().delete();
            }
        }
    }


    public static void createTarGzipFolder(Path inputPath, Path outputPath) throws IOException {

        try (OutputStream fOut = Files.newOutputStream(outputPath);
             BufferedOutputStream buffOut = new BufferedOutputStream(fOut);
             GzipCompressorOutputStream gzOut = new GzipCompressorOutputStream(buffOut);
             TarArchiveOutputStream tOut = new TarArchiveOutputStream(gzOut)) {

            Files.walkFileTree(inputPath, new SimpleFileVisitor<>() {

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {

                    Path targetFile = inputPath.relativize(dir);

                    TarArchiveEntry tarEntry = new TarArchiveEntry(
                            dir.toFile(), targetFile.toString());
                    tarEntry.setUserId(1000);
                    tarEntry.setGroupId(1000);

                    tOut.putArchiveEntry(tarEntry);

                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) throws IOException {

                    // only copy files, no symbolic links
                    if (attributes.isSymbolicLink()) {
                        return FileVisitResult.CONTINUE;
                    }

                    // get filename
                    Path targetFile = inputPath.relativize(file);

                    TarArchiveEntry tarEntry = new TarArchiveEntry(
                            file.toFile(), targetFile.toString());
                    tarEntry.setUserId(1000);
                    tarEntry.setGroupId(1000);

                    tOut.putArchiveEntry(tarEntry);

                    Files.copy(file, tOut);

                    tOut.closeArchiveEntry();


                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) {
                    return FileVisitResult.CONTINUE;
                }

            });

            tOut.finish();
        }

    }


}
