package com.github.metcox.demoit.web;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import static org.springframework.http.ResponseEntity.status;

@Controller
public class PingController {

    RestTemplate restTemplate;

    public PingController(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    @RequestMapping(path = "/ping", method = {RequestMethod.HEAD, RequestMethod.GET})
    public ResponseEntity<String> ping(@RequestParam("url") String url) {
        try {
            int status = restTemplate.execute(url, HttpMethod.HEAD, null, ClientHttpResponse::getRawStatusCode);
            return status(status).build();
        } catch (RestClientException e) {
            return status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unable to connect");
        }
    }

}
