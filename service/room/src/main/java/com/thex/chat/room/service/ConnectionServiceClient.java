package com.thex.chat.room.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Slf4j
@Component
public class ConnectionServiceClient {

    private final RestClient restClient;

    public ConnectionServiceClient(@Value("${services.connection.url}") String connectionServiceUrl) {
        this.restClient = RestClient.builder().baseUrl(connectionServiceUrl).build();
    }

    public boolean isConnectionAlive(String connectionId) {
        try {
            restClient.get()
                .uri("/connections/{connectionId}", connectionId)
                .retrieve()
                .toBodilessEntity();
            return true;
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                return false;
            }
            throw e;
        } catch (RestClientException e) {
            log.warn(
                "Could not verify connection liveness for {} ({}). Allowing join.",
                connectionId, e.getClass().getSimpleName()
            );
            return true;
        }
    }
}
