package configservice;

import jakarta.ejb.EJB;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.Map;

@Path("/config")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ConfigServiceRest {

    @EJB
    private ConfigServiceRemote configService;

    @GET
    @Path("/game")
    public Map<String, String> getGameConfig() {
        return configService.getGameConfig();
    }

    @GET
    @Path("/piecehp")
    public Map<String, Integer> getPieceHP() {
        return configService.getPieceHP();
    }

    @POST
    @Path("/piecehp/{type}/{hp}")
    public void setPieceHP(@PathParam("type") String pieceType, @PathParam("hp") int hp) {
        configService.setPieceHP(pieceType, hp);
    }
}