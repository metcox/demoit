package com.github.metcox.demoit.web;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
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

import java.net.URI;
import java.nio.file.Paths;
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
                // well we received a response so we're fine
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

        String source = Paths.get("sample").toFile().getAbsolutePath();

        CreateContainerCmd createContainerCmd = dockerClient
                .createContainerCmd("codercom/code-server:3.4.1")
                .withName("demoit-vscode")
                .withCmd("--auth=none", "--disable-telemetry")
                .withExposedPorts(ExposedPort.tcp(8080));
        createContainerCmd
                .getHostConfig()
                .withPortBindings(PortBinding.parse("18080:8080"))
                .withBinds(Bind.parse(source + ":/app"));
        CreateContainerResponse container = createContainerCmd.exec();

        dockerClient
                .startContainerCmd(container.getId())
                .exec();
    }


}
