package org.example.server;

import org.example.Data;
import org.example.Protocols;
import org.example.User;
import org.junit.jupiter.api.*;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.SQLException;
import java.util.UUID;

import static org.mockito.Mockito.*;

/**
 * Base test class for ConnectionManager tests with common setup and utility methods
 */
public abstract class BaseConnectionTest {

    @Mock
    protected Socket socket;
    
    @Mock
    protected ServerController serverController;
    
    @Mock
    protected ServerView serverView;
    
    protected ByteArrayOutputStream outputStream;
    protected ObjectOutputStream objectOutputStream;
    protected ConnectionManager connectionManager;
    protected static final String TEST_DB_URL = "jdbc:sqlite:test-database.db";
    protected static DatabaseManager databaseManager;
    
    // Test data
    protected static final String TEST_USERNAME = "testuser_" + UUID.randomUUID().toString().substring(0, 8);
    protected static final String TEST_PASSWORD = "password123";
    protected static final String TEST_ROLE = "user";
    protected static final String ADMIN_USERNAME = "admin_" + UUID.randomUUID().toString().substring(0, 8);
    
    @BeforeAll
    static void setupDatabase() throws SQLException {
        // Delete existing test database before starting tests
        java.io.File dbFile = new java.io.File("test-database.db");
        if (dbFile.exists()) {
            dbFile.delete();
        }
        
        System.setProperty("db.url", TEST_DB_URL);
        databaseManager = DatabaseManager.getInstance();
        
        // Create admin user
        try {
            User admin = new User(ADMIN_USERNAME, "adminpass", "admin");
            databaseManager.getUserDao().create(admin);
        } catch (SQLException e) {
            // If admin creation fails due to unique constraint, it's likely already created
            // Just log it and continue
            System.out.println("Note: Admin user creation failed, likely already exists: " + e.getMessage());
        }
    }
    
    @AfterAll
    static void cleanupDatabase() throws Exception {
        if (databaseManager != null) {
            databaseManager.close();
        }

        java.io.File dbFile = new java.io.File("test-database.db");
        if (dbFile.exists()) {
            dbFile.delete();
        }
    }
    
    @BeforeEach
    void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);
        
        when(serverController.getView()).thenReturn(serverView);
        
        // Set up mock Socket with streams
        outputStream = new ByteArrayOutputStream();
        objectOutputStream = new ObjectOutputStream(outputStream);
        when(socket.getOutputStream()).thenReturn(outputStream);
        connectionManager = new ConnectionManager(socket, serverController);
        connectionManager.initializeForTesting(objectOutputStream);
    }
    
    @AfterEach
    void tearDown() throws IOException {
        objectOutputStream.close();
        outputStream.close();
    }
    
    // Helper method to simulate receiving a message from client
    protected void simulateClientMessage(Data message) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(message);
        oos.flush();
        
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        when(socket.getInputStream()).thenReturn(bais);
    }
    
    protected Data getResponse() throws IOException, ClassNotFoundException {
        ByteArrayInputStream bais = new ByteArrayInputStream(outputStream.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        return (Data) ois.readObject();
    }
    
    protected Data processMessageAndGetResponse(Data request) throws IOException, ClassNotFoundException {
        simulateClientMessage(request);
        connectionManager.handleMessage(request);
        return getResponse();
    }
    
    protected User findUserInDatabase(String username) throws SQLException {
        var userDao = DatabaseManager.getInstance().getUserDao();
        var queryBuilder = userDao.queryBuilder();
        return queryBuilder.where().eq("username", username).queryForFirst();
    }
    
    protected void resetOutputStream() throws IOException {
        outputStream = new ByteArrayOutputStream();
        objectOutputStream = new ObjectOutputStream(outputStream);
        when(socket.getOutputStream()).thenReturn(outputStream);
        connectionManager.initializeForTesting(objectOutputStream);
    }
    
    // Helper method to ensure a test user exists
    protected void createTestUserIfNotExists() throws IOException, ClassNotFoundException, SQLException {
        if (findUserInDatabase(TEST_USERNAME) == null) {
            // Create a User object instead of a String array
            User newUser = new User(TEST_USERNAME, TEST_PASSWORD, TEST_ROLE);
            Data createUserRequest = new Data(Protocols.CREATE_USER, newUser);
            processMessageAndGetResponse(createUserRequest);
            
            // Reset the output stream and mock to clear previous responses
            resetOutputStream();
            reset(serverView); // Reset the mock to clear any verification state
        }
    }
}
