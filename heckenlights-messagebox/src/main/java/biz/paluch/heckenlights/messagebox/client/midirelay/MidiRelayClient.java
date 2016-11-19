package biz.paluch.heckenlights.messagebox.client.midirelay;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.springframework.stereotype.Component;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 01.12.13 20:52
 */
@Component
@RequiredArgsConstructor
public class MidiRelayClient {

    @NonNull
    MidiRelayClientProxy clientProxy;

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

        return clientProxy.getState();
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
