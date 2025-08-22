package org.stefan.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.stefan.backend.handlers.BinanceWebSocketHandler;

@Configuration(proxyBeanMethods = false)
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

        private final BinanceWebSocketHandler handler;

        private WebSocketConfig(BinanceWebSocketHandler handler) {
            this.handler = handler;
        }


        @Override
        public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
            registry.addHandler(handler, "/ws/bitcoin").setAllowedOrigins("*");
        }
}
