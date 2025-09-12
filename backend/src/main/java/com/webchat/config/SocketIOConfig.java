package com.webchat.config;

import com.corundumstudio.socketio.SocketIOServer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SocketIOConfig {

    @Value("${webchat.socketio.host:0.0.0.0}")
    private String host;

    @Value("${webchat.socketio.port:9092}")
    private int port;

    @Bean(destroyMethod = "stop")
    public SocketIOServer socketIOServer() {
        com.corundumstudio.socketio.Configuration socketConfig = new com.corundumstudio.socketio.Configuration();
        socketConfig.setHostname(host);
        socketConfig.setPort(port);
        socketConfig.setOrigin("*");
        return new SocketIOServer(socketConfig);
    }
}
