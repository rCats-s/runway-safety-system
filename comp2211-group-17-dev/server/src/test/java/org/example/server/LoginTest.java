package org.example.server;

import org.example.Data;
import org.example.Protocols;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for login functionality
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class LoginTest extends BaseConnectionTest {
    
    @Test
    @Order(1)
    @DisplayName("Test user login with valid credentials")
    void testUserLogin_Success() throws IOException, ClassNotFoundException, SQLException {
        // Make sure test user exists first
        createTestUserIfNotExists();
        
        // Make sure output stream is clean before sending login request
        resetOutputStream();
        
        String[] credentials = {TEST_USERNAME, TEST_PASSWORD};
        Data loginRequest = new Data(Protocols.USER_LOGIN, credentials);
        Data response = processMessageAndGetResponse(loginRequest);
        
        assertEquals(Protocols.LOGGED_IN, response.getMessage(), 
            "Should respond with LOGGED_IN message for valid credentials");
        assertNotNull(response.getValue(), "Should return user data with login response");
    }
    
    @Test
    @Order(2)
    @DisplayName("Test user login with invalid credentials")
    void testUserLogin_InvalidCredentials() throws IOException, ClassNotFoundException, SQLException {
        // Make sure test user exists first
        createTestUserIfNotExists();
        
        String[] credentials = {TEST_USERNAME, "wrongpassword"};
        Data loginRequest = new Data(Protocols.USER_LOGIN, credentials);
        
        Data response = processMessageAndGetResponse(loginRequest);
        
        assertEquals(Protocols.FAILED_LOGIN, response.getMessage(), 
            "Should respond with FAILED_LOGIN message for invalid credentials");
    }
    
    @Test
    @Order(3)
    @DisplayName("Test user login with non-existent username")
    void testUserLogin_NonExistentUser() throws IOException, ClassNotFoundException {
        String[] credentials = {"nonexistentuser", "anypassword"};
        Data loginRequest = new Data(Protocols.USER_LOGIN, credentials);
        
        Data response = processMessageAndGetResponse(loginRequest);
        
        assertEquals(Protocols.FAILED_LOGIN, response.getMessage(), 
            "Should respond with FAILED_LOGIN message for non-existent user");
    }
}
