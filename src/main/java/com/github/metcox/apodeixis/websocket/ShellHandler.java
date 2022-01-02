package com.github.metcox.apodeixis.websocket;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.metcox.apodeixis.shell.TerminalService;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ShellHandler extends TextWebSocketHandler {

    private TerminalService terminalService;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        terminalService = new TerminalService(session);
        super.afterConnectionEstablished(session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Map<String, String> messageMap = getMessageMap(message);

        if (messageMap.containsKey("type")) {
            String type = messageMap.get("type");

            switch (type) {
                case "TERMINAL_INIT":
                    terminalService.onTerminalInit();
                    break;
                case "TERMINAL_READY":
                    terminalService.onTerminalReady();
                    break;
                case "TERMINAL_COMMAND":
                    terminalService.onCommand(messageMap.get("command"));
                    break;
                case "TERMINAL_RESIZE":
                    terminalService.onTerminalResize(messageMap.get("cols"), messageMap.get("rows"));
                    break;
                default:
                    throw new RuntimeException("Unrecognized action");
            }
        }
    }

    private Map<String, String> getMessageMap(TextMessage message) {
        try {
            return new ObjectMapper().readValue(message.getPayload(), new TypeReference<>() {
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new HashMap<>();
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        try {
            session.close();
        } finally {
            terminalService.onTerminalClose();
        }
    }

}