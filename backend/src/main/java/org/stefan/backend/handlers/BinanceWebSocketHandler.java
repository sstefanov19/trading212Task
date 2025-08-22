package org.stefan.backend.handlers;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.WebSocketConnectionManager;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.stefan.backend.BinanceClient;


import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class BinanceWebSocketHandler extends TextWebSocketHandler {
    private final CopyOnWriteArrayList<WebSocketSession> sessions = new CopyOnWriteArrayList<>();

    public BinanceWebSocketHandler(BinanceClient binanceClient) {
        binanceClient.setMessageConsumer(this::broadcast);
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
    }

    private void broadcast(String message) {
        for (WebSocketSession s : sessions) {
            try {
                if (s.isOpen()) {
                    s.sendMessage(new TextMessage(message));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
