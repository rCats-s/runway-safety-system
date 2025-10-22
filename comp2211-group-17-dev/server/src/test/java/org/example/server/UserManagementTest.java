package org.example.server;

import com.j256.ormlite.dao.Dao;
import org.example.Data;
import org.example.Protocols;
import org.example.User;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserManagementTest extends BaseConnectionTest {
    
    @Test
    @Order(1)
    @DisplayName("Test user creation with valid data")
    void testUserCreation_Success() throws IOException, ClassNotFoundException, SQLException {
        // Generate a unique username for this test run to avoid conflicts
        String uniqueUsername = "testuser_" + UUID.randomUUID().toString().substring(0, 8);
        
        // Make sure this user doesn't already exist
        User existingUser = findUserInDatabase(uniqueUsername);
        if (existingUser != null) {
            DatabaseManager.getInstance().getUserDao().delete(existingUser);
        }
        
        // Create a User object instead of a String array
        User newUser = new User(uniqueUsername, TEST_PASSWORD, TEST_ROLE);
        Data createUserRequest = new Data(Protocols.CREATE_USER, newUser);
        
        Data response = processMessageAndGetResponse(createUserRequest);
        
        // Verify the response is USER_CREATED
        assertEquals(Protocols.USER_CREATED, response.getMessage(), 
            "Should respond with USER_CREATED for successful user creation");
        
        verify(serverView).log(contains("New user registered"));
    
        User user = findUserInDatabase(uniqueUsername);
        assertNotNull(user, "User should be created in database");
        assertEquals(uniqueUsername, user.getUsername());
        assertEquals(TEST_ROLE, user.getRole());
    }
    
    @Test
    @Order(2)
    @DisplayName("Test user creation with duplicate username")
    void testUserCreation_DuplicateUsername() throws IOException, ClassNotFoundException, SQLException {
        // Make sure test user exists first
        createTestUserIfNotExists();
        
        // Create a User object instead of a String array
        User newUser = new User(TEST_USERNAME, "differentpassword", "user");
        Data createUserRequest = new Data(Protocols.CREATE_USER, newUser);
        
        Data response = processMessageAndGetResponse(createUserRequest);
        
        assertEquals(Protocols.USER_CREATION_FAILED, response.getMessage());
        assertTrue(response.getValue().toString().contains("Username already exists"));
        verify(serverView, never()).log(contains("New user registered: " + TEST_USERNAME));
    }
    
    // @Test
    // @Order(3)
    // @DisplayName("Test password change")
    // void testPasswordChange_Success() throws IOException, ClassNotFoundException, SQLException {
    //     // Make sure test user exists first
    //     createTestUserIfNotExists();
        
    //     // Prepare password change request
    //     String newPassword = "newpassword123";
    //     String[] passwordData = {TEST_USERNAME, newPassword};
    //     Data changePasswordRequest = new Data(Protocols.CHANGE_PASSWORD, passwordData);
        
    //     processMessageAndGetResponse(changePasswordRequest);
        
    //     verify(serverView).log(contains("Password changed for user: " + TEST_USERNAME));
        
    //     // Verify new password works for login
    //     resetOutputStream();
    //     reset(serverView);
        
    //     String[] credentials = {TEST_USERNAME, newPassword};
    //     Data loginRequest = new Data(Protocols.USER_LOGIN, credentials);
        
    //     Data response = processMessageAndGetResponse(loginRequest);
        
    //     assertEquals(Protocols.LOGGED_IN, response.getMessage(), 
    //         "Should respond with LOGGED_IN message after password change");
    //     assertNotNull(response.getValue(), "Should return user data with login response");
    // }
    
    // @Test
    // @Order(4)
    // @DisplayName("Test password change for non-existent user")
    // void testPasswordChange_NonExistentUser() throws IOException, ClassNotFoundException {
    //     String[] passwordData = {"nonexistentuser", "newpassword"};
    //     Data changePasswordRequest = new Data(Protocols.CHANGE_PASSWORD, passwordData);
        
    //     Data response = processMessageAndGetResponse(changePasswordRequest);
        
    //     assertEquals(Protocols.PASSWORD_CHANGE_FAILED, response.getMessage());
    //     assertTrue(response.getValue().toString().contains("User not found"));
    //     verify(serverView, never()).log(contains("Password changed for user: nonexistentuser"));
    // }
    
    @Test
    @Order(5)
    @DisplayName("Test retrieving all users")
    void testGetUsers_Success() throws IOException, ClassNotFoundException, SQLException {
        // Make sure test user exists first
        createTestUserIfNotExists();
        
        Data getUsersRequest = new Data(Protocols.GET_USERS, null);
        Data response = processMessageAndGetResponse(getUsersRequest);
        
        assertEquals(Protocols.USERS_RETRIEVED, response.getMessage(), "Should receive USERS_RETRIEVED message");
        assertNotNull(response.getValue(), "Response value should not be null");
        
        // Check that we received a list of users
        List<User> users = (List<User>) response.getValue();
        assertFalse(users.isEmpty(), "User list should not be empty");
        
        // Verify the test user exists in the list
        boolean foundTestUser = false;
        for (User user : users) {
            if (TEST_USERNAME.equals(user.getUsername())) {
                foundTestUser = true;
                assertEquals(TEST_ROLE, user.getRole(), "User role should match");
                assertEquals("", user.getPassword(), "Password should be empty string for security");
                break;
            }
        }
        assertTrue(foundTestUser, "The test user should be in the returned list");
        
        // Verify logging
        verify(serverView).log(contains("User list retrieved"));
    }

    @Test
    @Order(6)
    @DisplayName("Test user retrieval with database error")
    void testGetUsers_DatabaseError() throws IOException, ClassNotFoundException, SQLException, NoSuchFieldException, IllegalAccessException {
        Dao<User, Integer> userDao = DatabaseManager.getInstance().getUserDao();
        Dao<User, Integer> spyUserDao = spy(userDao);
        
        doThrow(new SQLException("Simulated database error")).when(spyUserDao).queryForAll();
        
        // Replace the real DAO with our spy using reflection
        Field userDaoField = DatabaseManager.class.getDeclaredField("userDao");
        userDaoField.setAccessible(true);
        userDaoField.set(DatabaseManager.getInstance(), spyUserDao);
        
        try {
            Data getUsersRequest = new Data(Protocols.GET_USERS, null);
            Data response = processMessageAndGetResponse(getUsersRequest);
            
            assertEquals(Protocols.USERS_RETRIEVAL_FAILED, response.getMessage(), "Should receive USERS_RETRIEVAL_FAILED message");
            assertTrue(response.getValue().toString().contains("Database error"), "Error message should mention database error");
            
            verify(serverView).logErr(contains("Failed to retrieve users"));
        } finally {
            userDaoField.set(DatabaseManager.getInstance(), userDao);
        }
    }
    
    @Test
    @Order(7)
    @DisplayName("Test user deletion")
    void testUserDeletion_Success() throws IOException, ClassNotFoundException, SQLException {
        // Make sure test user exists first
        createTestUserIfNotExists();
        
        // Create a User object with the username to delete
        User userToDelete = new User(TEST_USERNAME, "", "");
        
        Data deleteUserRequest = new Data(Protocols.DELETE_USER, userToDelete);
        
        processMessageAndGetResponse(deleteUserRequest);
        
        verify(serverView).log(contains("User deleted: " + TEST_USERNAME));
        
        User user = findUserInDatabase(TEST_USERNAME);
        assertNull(user, "User should be deleted from database");
    }

    @Test
    @Order(8)
    @DisplayName("Test user deletion for non-existent user")
    void testUserDeletion_NonExistentUser() throws IOException, ClassNotFoundException {
        // Create a User object with a non-existent username
        User nonExistentUser = new User("nonexistentuser", "", "");
        
        Data deleteUserRequest = new Data(Protocols.DELETE_USER, nonExistentUser);
        
        Data response = processMessageAndGetResponse(deleteUserRequest);
        
        assertEquals(Protocols.USER_DELETION_FAILED, response.getMessage());
        assertTrue(response.getValue().toString().contains("User not found"));
        verify(serverView, never()).log(contains("User deleted: nonexistentuser"));
    }
}
