package org.example.server;

import com.j256.ormlite.dao.Dao;
import org.example.Airport;
import org.example.Data;
import org.example.Protocols;
import org.example.Runway;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;




@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RunwayManagementTest extends BaseConnectionTest {
    
    private static final String TEST_RUNWAY_DESIGNATION = "09L";
    private static final int TEST_RUNWAY_TORA = 3000;
    private static final int TEST_RUNWAY_TODA = 3300;
    private static final int TEST_RUNWAY_ASDA = 3200;
    private static final int TEST_RUNWAY_LDA = 2700;
    private static final int TEST_RUNWAY_THRESHOLD = 100;
    private static final int TEST_RUNWAY_CLEARWAY = 300;
    private static final int TEST_RUNWAY_STOPWAY = 200;
    
    private static Airport testAirport;
    
    @BeforeAll
    static void setupTestAirport() throws SQLException {
        // Create a test airport if it doesn't exist
        testAirport = new Airport("Test Runway Airport", "TRA", "Test Location");
        try {
            DatabaseManager.getInstance().getAirportDao().create(testAirport);
        } catch (SQLException e) {
            // If creation fails, try to find existing test airport
            List<Airport> airports = DatabaseManager.getInstance().getAirportDao().queryBuilder()
                .where().eq("code", "TRA").query();
            if (!airports.isEmpty()) {
                testAirport = airports.get(0);
            } else {
                throw e; // re-throw if we couldnt find or create
            }
        }
    }
    
    @BeforeEach
    void setupEach() throws SQLException {
        clearRunwayIfExists(TEST_RUNWAY_DESIGNATION, testAirport.getId());
    }
    
    @Test
    @Order(1)
    @DisplayName("Test runway creation with valid data")
    void testRunwayCreation_Success() throws IOException, ClassNotFoundException {
        Object[] runwayData = {
            TEST_RUNWAY_DESIGNATION, 
            testAirport.getId(), 
            TEST_RUNWAY_TORA, 
            TEST_RUNWAY_TODA, 
            TEST_RUNWAY_ASDA, 
            TEST_RUNWAY_LDA,
            TEST_RUNWAY_THRESHOLD, 
            TEST_RUNWAY_CLEARWAY, 
            TEST_RUNWAY_STOPWAY
        };
        
        Data createRunwayRequest = new Data(Protocols.CREATE_RUNWAY, runwayData);
        Data response = processMessageAndGetResponse(createRunwayRequest);
        
        assertEquals(Protocols.RUNWAY_CREATED, response.getMessage(), 
            "Should respond with RUNWAY_CREATED for successful runway creation");
        
        assertTrue(response.getValue() instanceof Runway, "Response value should be a Runway object");
        Runway createdRunway = (Runway) response.getValue();
        assertEquals(TEST_RUNWAY_DESIGNATION, createdRunway.getDesignation());
        assertEquals(testAirport.getId(), createdRunway.getAirportId());
        assertEquals(TEST_RUNWAY_TORA, createdRunway.getOriginalTORA());
        
        verify(serverView).log(contains("New runway added"));
    }
    
    @Test
    @Order(2)
    @DisplayName("Test runway creation with non-existent airport")
    void testRunwayCreation_NonExistentAirport() throws IOException, ClassNotFoundException {
        int nonExistentAirportId = 99999;
        
        Object[] runwayData = {
            TEST_RUNWAY_DESIGNATION, 
            nonExistentAirportId, 
            TEST_RUNWAY_TORA, 
            TEST_RUNWAY_TODA, 
            TEST_RUNWAY_ASDA, 
            TEST_RUNWAY_LDA,
            TEST_RUNWAY_THRESHOLD, 
            TEST_RUNWAY_CLEARWAY, 
            TEST_RUNWAY_STOPWAY
        };
        
        Data createRunwayRequest = new Data(Protocols.CREATE_RUNWAY, runwayData);
        
        Data response = processMessageAndGetResponse(createRunwayRequest);
        
        assertEquals(Protocols.RUNWAY_CREATION_FAILED, response.getMessage(), "Should respond with RUNWAY_CREATION_FAILED when airport doesn't exist");
        assertEquals("Airport not found", response.getValue(), 
            "Error message should indicate airport not found");
        
        verify(serverView, never()).log(contains("New runway added"));
    }
    
    @Test
    @Order(3)
    @DisplayName("Test runway creation with duplicate designation")
    void testRunwayCreation_DuplicateDesignation() throws IOException, ClassNotFoundException, SQLException {
        clearRunwayIfExists(TEST_RUNWAY_DESIGNATION, testAirport.getId());
        
        // Create a runway directly in the database to ensure it exists
        Runway runway = new Runway(TEST_RUNWAY_DESIGNATION, testAirport.getId(), 
                TEST_RUNWAY_TORA, TEST_RUNWAY_TODA, TEST_RUNWAY_ASDA, TEST_RUNWAY_LDA,
                TEST_RUNWAY_THRESHOLD, TEST_RUNWAY_CLEARWAY, TEST_RUNWAY_STOPWAY);
        DatabaseManager.getInstance().getRunwayDao().create(runway);
        
        // Verify the runway exists in the database
        Runway foundRunway = findRunwayInDatabase(TEST_RUNWAY_DESIGNATION, testAirport.getId());
        assertNotNull(foundRunway, "Test runway should exist in the database");
        
        // Now try to create a runway with the same designation
        Object[] runwayData = {
            TEST_RUNWAY_DESIGNATION, 
            testAirport.getId(), 
            TEST_RUNWAY_TORA + 100, // Different values but same designation
            TEST_RUNWAY_TODA + 100, 
            TEST_RUNWAY_ASDA + 100, 
            TEST_RUNWAY_LDA + 100,
            TEST_RUNWAY_THRESHOLD, 
            TEST_RUNWAY_CLEARWAY, 
            TEST_RUNWAY_STOPWAY
        };
        
        Data createRunwayRequest = new Data(Protocols.CREATE_RUNWAY, runwayData);
        Data response = processMessageAndGetResponse(createRunwayRequest);
        
        assertEquals(Protocols.RUNWAY_CREATION_FAILED, response.getMessage(), "Should respond with RUNWAY_CREATION_FAILED for duplicate designation");
        assertTrue(response.getValue().toString().contains("already exists"), 
            "Error message should indicate duplicate designation");
        
        verify(serverView).logWarning(contains("Runway creation failed: Designation already exists"));
    }
    
    @Test
    @Order(4)
    @DisplayName("Test retrieving runways for a specific airport")
    void testGetRunways_ByAirport() throws IOException, ClassNotFoundException, SQLException {

        createTestRunway();
        
        // Request runways for the test airport
        Data getRunwaysRequest = new Data(Protocols.GET_RUNWAYS, testAirport.getId());
        
        Data response = processMessageAndGetResponse(getRunwaysRequest);
        
        assertEquals(Protocols.RUNWAYS_RETRIEVED, response.getMessage(),
            "Should respond with RUNWAYS_RETRIEVED message");
        assertNotNull(response.getValue(), "Response value should not be null");
        
        List<Runway> runways = (List<Runway>) response.getValue();
        assertFalse(runways.isEmpty(), "Runway list should not be empty");
        
        boolean foundTestRunway = false;
        for (Runway runway : runways) {
            if (TEST_RUNWAY_DESIGNATION.equals(runway.getDesignation()) && 
                runway.getAirportId() == testAirport.getId()) {
                foundTestRunway = true;
                assertEquals(TEST_RUNWAY_TORA, runway.getOriginalTORA(), "TORA should match");
                assertEquals(TEST_RUNWAY_TODA, runway.getOriginalTODA(), "TODA should match");
                assertEquals(TEST_RUNWAY_ASDA, runway.getOriginalASDA(), "ASDA should match");
                assertEquals(TEST_RUNWAY_LDA, runway.getOriginalLDA(), "LDA should match");
                assertEquals(TEST_RUNWAY_THRESHOLD, runway.getDisplacedThreshold(), "Threshold should match");
                assertEquals(TEST_RUNWAY_CLEARWAY, runway.getClearway(), "Clearway should match");
                assertEquals(TEST_RUNWAY_STOPWAY, runway.getStopway(), "Stopway should match");
                break;
            }
        }
        assertTrue(foundTestRunway, "The test runway should be in the returned list");
        
        verify(serverView).log(contains("Retrieved runways for airport"));
    }
    
    @Test
    @Order(5)
    @DisplayName("Test retrieving all runways")
    void testGetAllRunways_Success() throws IOException, ClassNotFoundException, SQLException {
        createTestRunway();
        
        // Request all runways
        Data getRunwaysRequest = new Data(Protocols.GET_RUNWAYS, null);
        
        Data response = processMessageAndGetResponse(getRunwaysRequest);
        
        assertEquals(Protocols.RUNWAYS_RETRIEVED, response.getMessage(),
            "Should respond with RUNWAYS_RETRIEVED message");
        assertNotNull(response.getValue(), "Response value should not be null");
        
        List<Runway> runways = (List<Runway>) response.getValue();
        assertFalse(runways.isEmpty(), "Runway list should not be empty");
        
        boolean foundTestRunway = false;
        for (Runway runway : runways) {
            if (TEST_RUNWAY_DESIGNATION.equals(runway.getDesignation()) && 
                runway.getAirportId() == testAirport.getId()) {
                foundTestRunway = true;
                break;
            }
        }
        assertTrue(foundTestRunway, "The test runway should be in the returned list");
        
        verify(serverView).log(contains("Retrieved all runways"));
    }
    
    @Test
    @Order(6)
    @DisplayName("Test runway retrieval with database error")
    void testGetRunways_DatabaseError() throws IOException, ClassNotFoundException, SQLException, NoSuchFieldException, IllegalAccessException {
        Dao<Runway, Integer> runwayDao = DatabaseManager.getInstance().getRunwayDao();
        Dao<Runway, Integer> spyRunwayDao = spy(runwayDao);
        
        // Only mock queryForAll() with SQLException - remove the queryBuilder() mock
        doThrow(new SQLException("Simulated database error")).when(spyRunwayDao).queryForAll();
        
        // Replace the real DAO with our spy using reflection
        Field runwayDaoField = DatabaseManager.class.getDeclaredField("runwayDao");
        runwayDaoField.setAccessible(true);
        runwayDaoField.set(DatabaseManager.getInstance(), spyRunwayDao);
        
        try {
            Data getRunwaysRequest = new Data(Protocols.GET_RUNWAYS, null);
            Data response = processMessageAndGetResponse(getRunwaysRequest);
            
            assertEquals(Protocols.RUNWAYS_RETRIEVAL_FAILED, response.getMessage(), 
                "Should receive RUNWAYS_RETRIEVAL_FAILED message");
            assertTrue(response.getValue().toString().contains("Database error"), 
                "Error message should mention database error");
            
            verify(serverView).logErr(contains("Failed to retrieve runways"));
        } finally {
            runwayDaoField.set(DatabaseManager.getInstance(), runwayDao);
        }
    }
    
    @Test
    @Order(7)
    @DisplayName("Test runway deletion")
    void testRunwayDeletion_Success() throws IOException, ClassNotFoundException, SQLException {
        Runway runway = createTestRunway();
        
        // Delete the runway
        Data deleteRunwayRequest = new Data(Protocols.DELETE_RUNWAY, runway.getId());
        
        Data response = processMessageAndGetResponse(deleteRunwayRequest);
        
        assertEquals(Protocols.RUNWAY_DELETED, response.getMessage(), 
            "Should respond with RUNWAY_DELETED message");
        assertEquals(runway.getId(), response.getValue(), 
            "Response value should be the deleted runway ID");
        
        Runway deletedRunway = findRunwayInDatabase(TEST_RUNWAY_DESIGNATION, testAirport.getId());
        assertNull(deletedRunway, "Runway should be deleted from database");
        
        verify(serverView).log(contains("Runway deleted"));
    }
    
    @Test
    @Order(8)
    @DisplayName("Test runway deletion for non-existent runway")
    void testRunwayDeletion_NonExistentRunway() throws IOException, ClassNotFoundException {
        int nonExistentRunwayId = 99999;
        
        Data deleteRunwayRequest = new Data(Protocols.DELETE_RUNWAY, nonExistentRunwayId);
        
        Data response = processMessageAndGetResponse(deleteRunwayRequest);
        
        assertEquals(Protocols.RUNWAY_DELETION_FAILED, response.getMessage(), 
            "Should respond with RUNWAY_DELETION_FAILED message");
        assertEquals("Runway not found", response.getValue(), 
            "Error message should indicate runway not found");
        
        verify(serverView, never()).log(contains("Runway deleted"));
    }
    

    
    private Runway createTestRunway() throws SQLException {
        Runway runway = findRunwayInDatabase(TEST_RUNWAY_DESIGNATION, testAirport.getId());
        if (runway == null) {
            runway = new Runway(TEST_RUNWAY_DESIGNATION, testAirport.getId(), 
                    TEST_RUNWAY_TORA, TEST_RUNWAY_TODA, TEST_RUNWAY_ASDA, TEST_RUNWAY_LDA,
                    TEST_RUNWAY_THRESHOLD, TEST_RUNWAY_CLEARWAY, TEST_RUNWAY_STOPWAY);
            DatabaseManager.getInstance().getRunwayDao().create(runway);
        }
        return runway;
    }
    
    private void clearRunwayIfExists(String designation, int airportId) throws SQLException {
        Runway runway = findRunwayInDatabase(designation, airportId);
        if (runway != null) {
            DatabaseManager.getInstance().getRunwayDao().delete(runway);
        }
    }
    
    private Runway findRunwayInDatabase(String designation, int airportId) throws SQLException {
        List<Runway> runways = DatabaseManager.getInstance().getRunwayDao().queryBuilder()
            .where()
            .eq("designation", designation)
            .and()
            .eq("airportId", airportId)
            .query();
        
        return runways.isEmpty() ? null : runways.get(0);
    }
}
