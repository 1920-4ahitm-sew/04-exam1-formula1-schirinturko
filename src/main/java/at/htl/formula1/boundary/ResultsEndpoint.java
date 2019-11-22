package at.htl.formula1.boundary;

import at.htl.formula1.entity.Driver;
import at.htl.formula1.entity.Race;
import at.htl.formula1.entity.Team;

import javax.json.Json;
import javax.json.JsonObject;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("results")
public class ResultsEndpoint {

    @PersistenceContext
    EntityManager em;

    /**
     * @param name als QueryParam einzulesen
     * @return JsonObject
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject getPointsSumOfDriver(@QueryParam("name") String name) {
        //Ermitteln der Id des Fahrers
        Driver driver = em
                .createNamedQuery("Driver.findByName", Driver.class)
                .setParameter("NAME", name)
                .getSingleResult();

        Long points = em
                .createNamedQuery("Result.sumPointsForDriver", Long.class)
                .setParameter("NAME", name)
                .getSingleResult();

        JsonObject jsonObj = Json.createObjectBuilder()
                .add("driver", driver.getName())
                .add("points", points)
                .build();

        return jsonObj;
    }

    /**
     * @param country des Rennens
     * @return
     */
    @GET
    @Path("winner/{country}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response findWinnerOfRace(@PathParam("country") String country) {
        Race race = em
                .createNamedQuery("Race.findByCountry", Race.class)
                .setParameter("COUNTRY" , country)
                .getSingleResult();

        Driver winnerDriver = em
                .createNamedQuery("Driver.findWinnerOfRace", Driver.class)
                .setParameter("RACE", race)
                .getSingleResult();

        return Response.ok(winnerDriver).build();
    }

    // Ergänzen Sie Ihre eigenen Methoden ...

    /**
     *
     * @param team
     * @return
     */
    @GET
    @Path("raceswon")
    public Response findRacesWonByTeam(@QueryParam("team") String team){
        Team team1 = em
                .createNamedQuery("Team.findByName", Team.class)
                .setParameter("NAME", team)
                .getSingleResult();

        List<Race> racesWon = em
                .createNamedQuery("Race.findRacesWonByTeam", Race.class)
                .setParameter("TEAM", team)
                .getResultList();

        return Response.ok(racesWon).build();
    }

    /**
     *
     * @return
     */
    @GET
    @Path("all")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPointsSumOfAllDrivers(){
        List<Object[]> points = em
                .createNamedQuery("Result.sumPointsForAllDrivers", Object[].class)
                .getResultList();

        return Response.ok(points).build();
    }

}
