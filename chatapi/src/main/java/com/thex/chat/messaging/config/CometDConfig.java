package com.thex.chat.messaging.config;

import com.thex.chat.messaging.chat.JwtHandshakePolicy;
import org.cometd.bayeux.server.BayeuxServer;
import org.cometd.server.BayeuxServerImpl;
import org.cometd.server.http.jakarta.CometDServlet;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CometDConfig {

    @Bean(initMethod = "start", destroyMethod = "stop")
    public BayeuxServerImpl bayeuxServer(JwtHandshakePolicy jwtHandshakePolicy) {
        BayeuxServerImpl bayeuxServer = new BayeuxServerImpl();
        bayeuxServer.setOption("ws.cometdURLMapping", "/cometd/*");
        bayeuxServer.setSecurityPolicy(jwtHandshakePolicy);
        return bayeuxServer;
    }

    @Bean
    public ServletContextInitializer bayeuxInitializer(BayeuxServer bayeuxServer) {
        return servletContext -> servletContext.setAttribute(
                BayeuxServer.ATTRIBUTE, bayeuxServer);
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
