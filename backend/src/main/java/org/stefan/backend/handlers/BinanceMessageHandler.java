package org.stefan.backend.handlers;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;

public class BinanceMessageHandler extends TextWebSocketHandler {

    private final CopyOnWriteArrayList<WebSocketSession> sessions = new CopyOnWriteArrayList<>();

    @Override
    public void handleTextMessage(WebSocketSession session , TextMessage message) throws IOException {
        for(WebSocketSession frontend: sessions) {
            frontend.sendMessage(new TextMessage(message.getPayload()));
        }
    }
}
