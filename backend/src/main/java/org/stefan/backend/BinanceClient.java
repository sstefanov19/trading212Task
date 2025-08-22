package org.stefan.backend;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Setter;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.client.WebSocketConnectionManager;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import jakarta.annotation.PostConstruct;
import java.net.URI;
import java.util.function.Consumer;

@Setter
@Service
public class BinanceClient {

    private Consumer<String> messageConsumer;
    private volatile long lastMessageTime = 0;

    private static final ObjectMapper mapper = new ObjectMapper();

    @PostConstruct
    public void connect() {
        String url = "wss://stream.binance.com:9443/ws/ethusdt@trade";

        StandardWebSocketClient client = new StandardWebSocketClient();

        TextWebSocketHandler binanceHandler = new TextWebSocketHandler() {
            @Override
            public void handleTextMessage(org.springframework.web.socket.WebSocketSession session, TextMessage message) {
                long now = System.currentTimeMillis();
                if(now -  lastMessageTime >= 5000) {
                    lastMessageTime = now;

                try {
                    JsonNode node = mapper.readTree(message.getPayload());
                    String price = node.get("p").asText();
                    if (messageConsumer != null) {
                        messageConsumer.accept(price);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                }
            }
        };

        WebSocketConnectionManager manager =
                new WebSocketConnectionManager(client, binanceHandler, URI.create(url).toString());

        manager.setAutoStartup(true);
        manager.start();
    }
}
