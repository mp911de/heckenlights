package de.paluch.heckenlights.client;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import org.springframework.stereotype.Component;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 01.12.13 20:52
 */
@Component
public class MidiRelayClient {
    @Inject
    private MidiRelayClientProxy clientProxy;

    private Cache<Long, PlayerStateRepresentation> semaphore = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.MINUTES)
            .build();

    public String getCurrentPlayId() {

        PlayerStateRepresentation state = getState();
        if (state.isRunning() && state.getTrack() != null) {
            return state.getTrack().getId();
        }

        return null;
    }

    public int getRemainingTime() {

        PlayerStateRepresentation state = getState();
        if (state.isRunning()) {
            return state.getEstimatedSecondsToPlay();
        }

        return 0;
    }

    public PlayerStateRepresentation getState() {
        try {

            long timeWindow = (System.currentTimeMillis() / 1000) / 5;
            PlayerStateRepresentation result = semaphore.getIfPresent(timeWindow);
            if (result == null) {
                result = clientProxy.getState();
                semaphore.put(timeWindow, result);

            }
            return result;
        } catch (Exception e) {
            return null;
        }
    }

    public void play(String id, String fileName, byte[] body) {
        clientProxy.play(id, fileName, body);
    }

    public void switchOff() {
        clientProxy.switchOff();
    }

    public void switchOn() {
        clientProxy.switchOn();
    }

    @Path("player")
    public interface MidiRelayClientProxy {
        @GET
        @Produces(MediaType.APPLICATION_JSON)
        PlayerStateRepresentation getState();

        @GET
        @Path("port/ON")
        String switchOn();

        @GET
        @Path("port/OFF")
        String switchOff();

        @PUT
        @Path("play")
        @Produces(MediaType.TEXT_PLAIN)
        @Consumes(MediaType.APPLICATION_OCTET_STREAM)
        public String play(@HeaderParam("X-Request-Id") String id, @HeaderParam("X-Request-FileName") String fileName,
                byte[] body);

    }
}
