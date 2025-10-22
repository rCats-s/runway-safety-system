package org.example.server;

import org.example.Airport;
import org.example.Data;
import org.example.Protocols;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Tests for airport management operations
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AirportManagementTest extends BaseConnectionTest {
    
    private static final String TEST_AIRPORT_NAME = "Test Airport";
    private static final String TEST_AIRPORT_CODE = "TST";
    private static final String TEST_AIRPORT_LOCATION = "Test Location";
    
    @BeforeEach
    void setupEach() throws SQLException {
        clearAirportIfExists(TEST_AIRPORT_CODE);
    }
    
    @Test
    @Order(1)
    @DisplayName("Test airport creation with valid data")
    void testAirportCreation_Success() throws IOException, ClassNotFoundException {
        String[] airportData = {TEST_AIRPORT_NAME, TEST_AIRPORT_CODE, TEST_AIRPORT_LOCATION};
      
        Data createAirportRequest = new Data(Protocols.CREATE_AIRPORT, airportData);
      
        Data response = processMessageAndGetResponse(createAirportRequest);
        assertEquals(Protocols.AIRPORT_CREATED, response.getMessage());
        assertTrue(response.getValue() instanceof Airport);
        Airport createdAirport = (Airport) response.getValue();
        assertEquals(TEST_AIRPORT_NAME, createdAirport.getName());
        assertEquals(TEST_AIRPORT_CODE, createdAirport.getCode());
        assertEquals(TEST_AIRPORT_LOCATION, createdAirport.getLocation());
        verify(serverView).log(contains("New airport added"));
    }
    
    @Test
    @Order(2)
    @DisplayName("Test airport creation with duplicate code")
    void testAirportCreation_DuplicateCode() throws IOException, ClassNotFoundException, SQLException {
        
        createTestAirportIfNotExists();
        
        String[] airportData = {"Different Airport", TEST_AIRPORT_CODE, "Different Location"};
        Data createAirportRequest = new Data(Protocols.CREATE_AIRPORT, airportData);
        
        Data response = processMessageAndGetResponse(createAirportRequest);
        
        assertEquals(Protocols.AIRPORT_CREATION_FAILED, response.getMessage(), "Should respond with AIRPORT_CREATION_FAILED for duplicate airport code");
        assertEquals("Airport code already exists", response.getValue(), "Error message should indicate duplicate airport code");
        
        verify(serverView, never()).log(contains("New airport added"));
    }
    
    @Test
    @Order(3)
    @DisplayName("Test retrieving all airports")
    void testGetAirports_Success() throws IOException, ClassNotFoundException, SQLException {
        
        createTestAirportIfNotExists();
        
        Data getAirportsRequest = new Data(Protocols.GET_AIRPORTS, null);
        
        Data response = processMessageAndGetResponse(getAirportsRequest);
        
        assertEquals(Protocols.AIRPORTS_RETRIEVED, response.getMessage(), "Should respond with AIRPORTS_RETRIEVED message");
        assertNotNull(response.getValue(), "Response value should not be null");
        
        List<Airport> airports = (List<Airport>) response.getValue();
        assertFalse(airports.isEmpty(), "Airport list should not be empty");
        
        boolean foundTestAirport = false;
        for (Airport airport : airports) {
            if (TEST_AIRPORT_CODE.equals(airport.getCode())) {
                foundTestAirport = true;
                assertEquals(TEST_AIRPORT_NAME, airport.getName(), "Airport name should match");
                assertEquals(TEST_AIRPORT_LOCATION, airport.getLocation(), "Airport location should match");
                break;
            }
        }
        assertTrue(foundTestAirport, "The test airport should be in the returned list");
        
        verify(serverView).log(contains("Airport list retrieved"));
    }
    
    @Test
    @Order(4)
    @DisplayName("Test airport retrieval with database error")
    void testGetAirports_DatabaseError() throws IOException, ClassNotFoundException, SQLException, NoSuchFieldException, IllegalAccessException {
        com.j256.ormlite.dao.Dao<Airport, Integer> originalAirportDao = DatabaseManager.getInstance().getAirportDao();
        
        try {
            com.j256.ormlite.dao.Dao<Airport, Integer> spyAirportDao = spy(originalAirportDao);
            
            doThrow(new SQLException("Simulated database error for testing")).when(spyAirportDao).queryForAll();
            
            Field airportDaoField = DatabaseManager.class.getDeclaredField("airportDao");
            airportDaoField.setAccessible(true);
            airportDaoField.set(DatabaseManager.getInstance(), spyAirportDao);
            
            Data getAirportsRequest = new Data(Protocols.GET_AIRPORTS, null);
            
            Data response = processMessageAndGetResponse(getAirportsRequest);
            
            assertEquals(Protocols.AIRPORTS_RETRIEVAL_FAILED, response.getMessage(), 
                "Should respond with AIRPORTS_RETRIEVAL_FAILED message");
            assertTrue(response.getValue().toString().contains("Database error"), 
                "Error message should mention database error");
            
            verify(serverView).logErr(contains("Failed to retrieve airports"));
        } finally {
            Field airportDaoField = DatabaseManager.class.getDeclaredField("airportDao");
            airportDaoField.setAccessible(true);
            airportDaoField.set(DatabaseManager.getInstance(), originalAirportDao);
        }
    }
    
    @Test
    @Order(5)
    @DisplayName("Test airport deletion")
    void testAirportDeletion_Success() throws IOException, ClassNotFoundException, SQLException {
        createTestAirportIfNotExists();
        
        Data deleteAirportRequest = new Data(Protocols.DELETE_AIRPORT, TEST_AIRPORT_CODE);
        
        Data response = processMessageAndGetResponse(deleteAirportRequest);
        
        assertEquals(Protocols.AIRPORT_DELETED, response.getMessage(), 
            "Should respond with AIRPORT_DELETED message");
        assertEquals(TEST_AIRPORT_CODE, response.getValue(), 
            "Response value should be the deleted airport code");
        
        Airport airport = findAirportInDatabase(TEST_AIRPORT_CODE);
        assertNull(airport, "Airport should be deleted from database");
        
        verify(serverView).log(contains("Airport deleted"));
    }
    
    @Test
    @Order(6)
    @DisplayName("Test airport deletion for non-existent airport")
    void testAirportDeletion_NonExistentAirport() throws IOException, ClassNotFoundException, SQLException {
        String nonExistentCode = "XXX";
        
        clearAirportIfExists(nonExistentCode);
        
        Data deleteAirportRequest = new Data(Protocols.DELETE_AIRPORT, nonExistentCode);
        
        Data response = processMessageAndGetResponse(deleteAirportRequest);
        
        assertEquals(Protocols.AIRPORT_DELETION_FAILED, response.getMessage(), 
            "Should respond with AIRPORT_DELETION_FAILED message");
        assertEquals("Airport not found", response.getValue(), 
            "Error message should indicate airport not found");
        
        verify(serverView, never()).log(contains("Airport deleted"));
    }
    
    // Helper methods
    
    private void createTestAirportIfNotExists() throws SQLException {
        Airport airport = findAirportInDatabase(TEST_AIRPORT_CODE);
        if (airport == null) {
            airport = new Airport(TEST_AIRPORT_NAME, TEST_AIRPORT_CODE, TEST_AIRPORT_LOCATION);
            DatabaseManager.getInstance().getAirportDao().create(airport);
        }
    }
    
    private void clearAirportIfExists(String code) throws SQLException {
        Airport airport = findAirportInDatabase(code);
        if (airport != null) {
            DatabaseManager.getInstance().getAirportDao().delete(airport);
        }
    }
    
    private Airport findAirportInDatabase(String code) throws SQLException {
        List<Airport> airports = DatabaseManager.getInstance().getAirportDao().queryBuilder()
            .where().eq("code", code).query();
        
        return airports.isEmpty() ? null : airports.get(0);
    }
}
