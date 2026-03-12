package com.thex.chat.messaging.config;

import com.thex.chat.messaging.chat.JwtHandshakePolicy;
import com.thex.chat.messaging.chat.VisitorService;
import org.cometd.bayeux.server.BayeuxServer;
import org.cometd.server.BayeuxServerImpl;
import org.cometd.server.http.jakarta.CometDServlet;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CometDConfig {

    @Bean(destroyMethod = "stop")
    public BayeuxServerImpl bayeuxServer(JwtHandshakePolicy jwtHandshakePolicy) {
        BayeuxServerImpl bayeuxServer = new BayeuxServerImpl();
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
