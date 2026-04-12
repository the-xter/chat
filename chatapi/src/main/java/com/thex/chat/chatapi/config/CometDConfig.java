package com.thex.chat.chatapi.config;

import com.thex.chat.chatapi.chat.JwtHandshakePolicy;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.cometd.annotation.server.ServerAnnotationProcessor;
import org.cometd.bayeux.server.BayeuxServer;
import org.cometd.server.BayeuxServerImpl;
import org.cometd.server.http.jakarta.CometDServlet;
import org.cometd.server.websocket.jakarta.WebSocketTransport;
import org.eclipse.jetty.ee11.servlet.ServletContextHandler;
import org.eclipse.jetty.ee11.websocket.jakarta.server.config.JakartaWebSocketServletContainerInitializer;
import org.springframework.beans.factory.config.DestructionAwareBeanPostProcessor;
import org.springframework.boot.jetty.servlet.JettyServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class CometDConfig {

    @Bean
    public ServletRegistrationBean<@NonNull CometDServlet> cometDServlet() {
        var registration = new ServletRegistrationBean<>(new CometDServlet(), "/cometd/*");
        registration.setLoadOnStartup(1);
        registration.addInitParameter("transports", WebSocketTransport.class.getName());
        registration.addInitParameter("ws.cometdURLMapping", "/cometd/*");
        registration.setAsyncSupported(true);
        return registration;
    }

    @Bean
    public ServletContextInitializer bayeuxInitializer(BayeuxServer bayeuxServer) {
        return servletContext -> servletContext.setAttribute(BayeuxServer.ATTRIBUTE, bayeuxServer);
    }

    @Bean
    public BayeuxServer bayeuxServer(JwtHandshakePolicy jwtHandshakePolicy) {
        var bayeux = new BayeuxServerImpl();
        bayeux.setOption("ws.cometdURLMapping", "/cometd/*");
        bayeux.setSecurityPolicy(jwtHandshakePolicy);
        return bayeux;
    }

    @Bean
    public DestructionAwareBeanPostProcessor cometdAnnotationProcessor(BayeuxServer bayeuxServer) {
        var processor = new ServerAnnotationProcessor(bayeuxServer);
        return new DestructionAwareBeanPostProcessor() {
            @Override
            public Object postProcessBeforeInitialization(@NonNull Object bean, @NonNull String name) {
                processor.processDependencies(bean);
                processor.processConfigurations(bean);
                processor.processCallbacks(bean);
                return bean;
            }

            @Override
            public Object postProcessAfterInitialization(@NonNull Object bean, @NonNull String name) {
                return bean;
            }

            @Override
            public void postProcessBeforeDestruction(@NonNull Object bean, @NonNull String name) {
                processor.deprocess(bean);
            }
        };
    }

    @Bean
    public WebServerFactoryCustomizer<@NonNull JettyServletWebServerFactory> jettyCustomizer() {
        return factory -> factory.addServerCustomizers(server -> {
            var handler = server.getHandler();
            if (handler instanceof ServletContextHandler sch) {
                JakartaWebSocketServletContainerInitializer.configure(sch, null);
                log.info("Jakarta WebSocket support initialized");
            }
        });
    }
}