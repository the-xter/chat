package com.thex.chat.messaging.config;

import com.thex.chat.messaging.chat.JwtHandshakePolicy;
import com.thex.chat.messaging.chat.VisitorService;
import org.cometd.bayeux.server.BayeuxServer;
import org.cometd.server.BayeuxServerImpl;
import org.cometd.server.http.jakarta.CometDServlet;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.websocket.server.config.JettyWebSocketServletContainerInitializer;
import org.eclipse.jetty.server.Handler;
import org.springframework.boot.web.embedded.jetty.JettyServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CometDConfig {

    @Bean
    public WebServerFactoryCustomizer<JettyServletWebServerFactory> jettyWebSocketCustomizer() {
        return factory -> factory.addServerCustomizers(server -> {
            Handler handler = server.getHandler();
            while (handler instanceof Handler.Wrapper wrapper) {
                if (wrapper instanceof ServletContextHandler sch) {
                    JettyWebSocketServletContainerInitializer.configure(sch, null);
                    return;
                }
                handler = wrapper.getHandler();
            }
        });
    }

    @Bean(destroyMethod = "stop")
    public BayeuxServerImpl bayeuxServer(JwtHandshakePolicy jwtHandshakePolicy) {
        BayeuxServerImpl bayeuxServer = new BayeuxServerImpl();
        bayeuxServer.setOption(BayeuxServerImpl.TRANSPORTS_OPTION,
                "org.cometd.server.websocket.jetty.JettyWebSocketTransport");
        bayeuxServer.setOption("ws.cometdURLMapping", "/cometd/*");
        bayeuxServer.setSecurityPolicy(jwtHandshakePolicy);
        return bayeuxServer;
    }

    @Bean
    public ServletContextInitializer bayeuxInitializer(BayeuxServerImpl bayeuxServer,
                                                       VisitorService visitorService) {
        return servletContext -> {
            bayeuxServer.setOption(
                    jakarta.servlet.ServletContext.class.getName(), servletContext);
            var handler = ServletContextHandler.getServletContextHandler(servletContext);
            bayeuxServer.setOption(
                    org.eclipse.jetty.server.Context.class.getName(), handler.getContext());
            servletContext.setAttribute(BayeuxServer.ATTRIBUTE, bayeuxServer);
            try {
                bayeuxServer.start();
            } catch (Exception e) {
                throw new jakarta.servlet.ServletException(
                        "Failed to start BayeuxServer", e);
            }
            visitorService.start();
        };
    }

    @Bean
    public ServletRegistrationBean<CometDServlet> cometdServlet() {
        ServletRegistrationBean<CometDServlet> registration =
                new ServletRegistrationBean<>(new CometDServlet(), "/cometd/*");
        registration.setAsyncSupported(true);
        registration.setLoadOnStartup(2);
        registration.addInitParameter("ws.cometdURLMapping", "/cometd/*");
        return registration;
    }
}
