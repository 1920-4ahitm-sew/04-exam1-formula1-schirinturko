package at.htl.formula1.control;

import at.htl.formula1.boundary.ResultsRestClient;
import at.htl.formula1.entity.Driver;
import at.htl.formula1.entity.Race;
import at.htl.formula1.entity.Team;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Stream;

@ApplicationScoped
public class InitBean {

    private static final String TEAM_FILE_NAME = "teams.csv";
    private static final String RACES_FILE_NAME = "races.csv";

    @PersistenceContext
    EntityManager em;

    @Inject
    ResultsRestClient client;
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    @Transactional
    public void init(@Observes @Initialized(ApplicationScoped.class) Object init) {

        readTeamsAndDriversFromFile(TEAM_FILE_NAME);
        readRacesFromFile(RACES_FILE_NAME);
        client.readResultsFromEndpoint();

    }

    /**
     * Einlesen der Datei "races.csv" und Speichern der Objekte in der Tabelle F1_RACE
     *
     * @param racesFileName
     */
    private void readRacesFromFile(String racesFileName) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy");

        URL url = Thread.currentThread().getContextClassLoader()
                .getResource(racesFileName);
        try (Stream<String> stream = Files.lines(Paths.get(url.getPath().substring(3)), StandardCharsets.UTF_8)) {
            stream
                    .skip(1)
                    .map((String s) -> s.split(";"))
                    .map(a -> new Race(Long.valueOf(a[0]), a[1], LocalDate.parse(a[2], dtf)))
                    .forEach(em::persist); //Daten in die DB einfügen
                    //.forEach(System.out::println);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Einlesen der Datei "teams.csv".
     * Das String-Array jeder einzelnen Zeile wird der Methode persistTeamAndDrivers(...)
     * übergeben
     *
     * @param teamFileName
     */
    private void readTeamsAndDriversFromFile(String teamFileName) {
        URL url = Thread.currentThread().getContextClassLoader()
                .getResource(teamFileName);
        try (Stream<String> stream = Files.lines(Paths.get(url.getPath().substring(3)), StandardCharsets.UTF_8)) {
            stream
                    .skip(1)
                    .map((String s) -> s.split(";"))
                    .forEach(this::persistTeamAndDrivers);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Es wird überprüft ob es das übergebene Team schon in der Tabelle F1_TEAM gibt.
     * Falls nicht, wird das Team in der Tabelle gespeichert.
     * Wenn es das Team schon gibt, dann liest man das Team aus der Tabelle und
     * erstellt ein Objekt (der Klasse Team).
     * Dieses Objekt wird verwendet, um die Fahrer mit Ihrem jeweiligen Team
     * in der Tabelle F!_DRIVER zu speichern.
     *
     * @param line String-Array mit den einzelnen Werten der csv-Datei
     */

    private void persistTeamAndDrivers(String[] line) {
        Team team = null; // team auf null setzen

        try {
            team = em
                        .createNamedQuery("team.findByName", Team.class)
                        .setParameter("NAME", line[0])
                        .getSingleResult();
        } catch (NoResultException ex){
            team = new Team(line[0]);
            em.persist(team);
        }

        Driver d1 = new Driver(line[1], team);
        Driver d2 = new Driver(line[2], team);
        em.persist(d1);
        em.persist(d2);
    }

}
