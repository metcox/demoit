package com.github.metcox.demoit.conf;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("app")
public class AppProperties {

    // ShellPort is the local port for shell server
    private int shellPort = 9999;

    public int getShellPort() {
        return shellPort;
    }

    public void setShellPort(int shellPort) {
        this.shellPort = shellPort;
    }

}
