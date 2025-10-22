package org.example.server;

import java.io.Serializable;

import com.j256.ormlite.stmt.QueryBuilder;
import org.example.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Consumer;

public class ConnectionManager implements Runnable {
    private final Socket socket;
    private final ServerController server;
    private final ServerView view;
    private ObjectOutputStream output;
    private final Map<String, Consumer<Data>> messageHandlers = new HashMap<>();

    private final Set<String> joinedRooms = new HashSet<>();

    public ConnectionManager(Socket socket, ServerController server) {
        this.socket = socket;
        this.server = server;
        this.view = server.getView();
        initializeMessageHandlers();
    }

    // easier to add handlers this way instead of switching
    private void initializeMessageHandlers() {
        messageHandlers.put(Protocols.CREATE_USER, this::handleCreateUser);
        messageHandlers.put(Protocols.USER_LOGIN, this::handleUserLogin);
        messageHandlers.put(Protocols.DELETE_USER, this::handleDeleteUser);
        messageHandlers.put(Protocols.UPDATE_USER, this::handleUserUpdate);
        messageHandlers.put(Protocols.GET_USERS, this::handleGetUsers);
        
        // Add handlers for calculation-related operations
        messageHandlers.put(Protocols.LOAD_CALCS, this::handleLoadCalcs);
        messageHandlers.put(Protocols.CREATE_CALC, this::handleCreateCalc);
        messageHandlers.put(Protocols.CALC_UPDATED, this::handleCalcUpdated);
        messageHandlers.put(Protocols.JOIN_CALC_ROOM, this::handleJoinCalcRoom);
        messageHandlers.put(Protocols.LEAVE_CALC_ROOM, this::handleLeaveCalcRoom);
        messageHandlers.put(Protocols.LOAD_USERS, this::handleLoadUsers);
        
        // Add handlers for airport-related operations
        messageHandlers.put(Protocols.CREATE_AIRPORT, this::handleCreateAirport);
        messageHandlers.put(Protocols.DELETE_AIRPORT, this::handleDeleteAirport);
        messageHandlers.put(Protocols.GET_AIRPORTS, this::handleGetAirports);
        messageHandlers.put(Protocols.UPDATE_AIRPORT, this::handleUpdateAirport);
        
        // Add handlers for runway-related operations
        messageHandlers.put(Protocols.CREATE_RUNWAY, this::handleCreateRunway);
        messageHandlers.put(Protocols.GET_RUNWAYS, this::handleGetRunways);
        messageHandlers.put(Protocols.DELETE_RUNWAY, this::handleDeleteRunway);
        messageHandlers.put(Protocols.UPDATE_RUNWAY, this::handleUpdateRunway);
    }

    private void handleUpdateRunway(Data message) {
        try {
            Runway updated = (Runway) message.getValue();
            view.logDebug("Updating runway ID " + updated.getId());

            DatabaseManager.getInstance().getRunwayDao().update(updated);

            view.log("Runway updated: " + updated.getDesignation() + " (ID: " + updated.getId() + ")");
            send(new Data(Protocols.RUNWAY_UPDATED, updated));
        } catch (SQLException e) {
            view.logErr(view.formatDatabaseError("update", "airport", e));
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        try {
            output = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream input = new ObjectInputStream(socket.getInputStream());

            server.onConnected(this);

            while (true) {
                Data message = (Data) input.readObject();
                view.logDebug("Received message: " + message.getMessage());
                handleMessage(message);
            }
        } catch (IOException | ClassNotFoundException e) {
            view.logErr("Client disconnected: " + e.getMessage());
        } finally {
            server.onDisconnected(this);
            try {
                socket.close();
            } catch (IOException e) {
                view.logErr("Could not close socket connection: " + e.getMessage());
                throw new RuntimeException(e);
            }
        }
    }

    private void joinRoom(String room) {
        joinedRooms.add(room);
        server.joinRoom(room, this);
    }

    private void leaveRoom(String room) {
        joinedRooms.remove(room);
        server.leaveRoom(room, this);
    }

    public void send(Serializable msg) {
        view.logDebug("Sending message: " + ((msg instanceof Data) ? ((Data)msg).getMessage() : "Raw message"));
        try {
            output.writeObject(msg);
            output.flush();
        } catch (IOException e) {
            view.logErr("Failed to send message to client: " + e.getMessage());
        }
    }

    void handleMessage(Data message) { // had to change this from private to allow testing calls
        view.logDebug("Handling message: " + message.getMessage());
        // map instead of switch
        Consumer<Data> handler = messageHandlers.get(message.getMessage());
        if (handler != null) {
            handler.accept(message);
        } else {
            view.logWarning("Unsupported message received: " + message.getMessage());
        }
    }

    public Set<String> getJoinedRooms() {
        return joinedRooms;
    }
    
    private void handleCreateUser(Data message) {
        try {
            // Expecting [username, password, role]
            User newUser = (User) message.getValue();
            
            // Check if username already exists
            var queryBuilder = DatabaseManager.getInstance().getUserDao().queryBuilder();
            List<User> existingUsers = queryBuilder
                    .where()
                    .eq("username", newUser.getUsername())
                    .query();
                    
            if (!existingUsers.isEmpty()) {
                send(new Data(Protocols.USER_CREATION_FAILED, "Username already exists"));
                return;
            }
            
            // Create new user
            DatabaseManager.getInstance().getUserDao().create(newUser);
            
            send(new Data(Protocols.USER_CREATED, newUser));
            view.log("New user registered: " + newUser.getUsername() + " with role: " + newUser.getRole());
            
        } catch (SQLException e) {
            view.logErr("Failed to register user: " + e.getMessage());
            send(new Data(Protocols.USER_CREATION_FAILED, "Database error: " + e.getMessage()));
        } catch (Exception e) {
            view.logErr("Error registering user: " + e.getMessage());
            send(new Data(Protocols.USER_CREATION_FAILED, "Error: " + e.getMessage()));
        }
    }
    
    private void handleUserLogin(Data message) {
        view.logDebug("Processing login request");
        // u can send any serializable object to and from server like this makes it easier.
        String[] cred = (String[])message.getValue();
        String username = cred[0];
        String password = cred[1];
        
        try {
            view.logDebug("Querying database for user credentials");
            // pls use the query builder for queries. it does make it easier lol.
            var queryBuilder = DatabaseManager.getInstance().getUserDao().queryBuilder();
            List<User> res = queryBuilder.selectColumns("role")
                    .where()
                    .eq("username", username)
                    .and()
                    .eq("password", password)
                    .query();
            if (!res.isEmpty()) {
                // we need the user on the client side anyways
                view.log("User login successful: " + username);
                send(new Data(Protocols.LOGGED_IN, res.get(0)));
            } else {
                view.logWarning("Login failed for user: " + username);
                send(new Data(Protocols.FAILED_LOGIN, null));
            }
        } catch (SQLException e) {
            view.logErr(view.formatDatabaseError("authentication", "user", e));
            send(new Data(Protocols.FAILED_LOGIN, "Database error during login"));
        } catch (Exception e) { // well that's for any other case
            view.logErr("Error during login: " + e.getMessage());
            send(new Data(Protocols.FAILED_LOGIN, "Error during login"));
        }
    }
    
    private void handleDeleteUser(Data message) {
        try {
            // Expecting user to delete
            User user = (User) message.getValue();
            
            // use username to find and delete user
            var queryBuilder = DatabaseManager.getInstance().getUserDao().queryBuilder();
            List<User> users = queryBuilder
                    .where()
                    .eq("username", user.getUsername())
                    .query();
                    
            if (users.isEmpty()) {
                send(new Data(Protocols.USER_DELETION_FAILED, "User not found"));
                return;
            }
            
            User userToDelete = users.get(0);
            DatabaseManager.getInstance().getUserDao().delete(userToDelete);
            
            send(new Data(Protocols.USER_DELETED, user));
            view.log("User deleted: " + user.getUsername());
            
        } catch (SQLException e) {
            view.logErr("Failed to delete user: " + e.getMessage());
            send(new Data(Protocols.USER_DELETION_FAILED, "Database error: " + e.getMessage()));
        } catch (Exception e) {
            view.logErr("Error deleting user: " + e.getMessage());
            send(new Data(Protocols.USER_DELETION_FAILED, "Error: " + e.getMessage()));
        }
    }
    
    private void handleUserUpdate(Data message) {
        try {
            // Expecting [username, newPassword]
            User user = (User) message.getValue();

            var updatedUser = DatabaseManager.getInstance().getUserDao().update(user);
            
            send(new Data(Protocols.USER_UPDATED, user));
            view.log("User updated: " + user);
            
        } catch (SQLException e) {
            view.logErr("Failed to update user: " + e.getMessage());
            send(new Data(Protocols.USER_UPDATE_FAILED, "Database error: " + e.getMessage()));
        } catch (Exception e) {
            view.logErr("Error updating user: " + e.getMessage());
            send(new Data(Protocols.USER_UPDATE_FAILED, "Error: " + e.getMessage()));
        }
    }
    
    private void handleLoadCalcs(Data message) {
        try {
            view.logDebug("Fetching all calculations from database");
            QueryBuilder<Calculation, Integer> query = DatabaseManager.getInstance().getCalculationDao().queryBuilder();
            List<Calculation> calculations = query.query();
            
            view.log("Retrieved " + calculations.size() + " calculations for client");
            send(new Data(Protocols.LOADED_CALCS, new ArrayList<>(calculations)));
        } catch (SQLException e) { // I really wanna add error responses here....
            view.logErr("Failed to retrieve calculations: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
    
    private void handleCreateCalc(Data message) {
        try {
            Calculation calculation = (Calculation) message.getValue();
            view.logDebug("Creating new calculation: " + calculation.getName());
            
            DatabaseManager.getInstance().getCalculationDao().create(calculation);
            
            view.log("New calculation created: " + calculation.getName() + " (ID: " + calculation.getId() + ")");
            server.broadcast(new Data(Protocols.CALC_CREATED, calculation));
        } catch (SQLException e) { // another one of those cases where we need to handle errors
            view.logErr(view.formatDatabaseError("creation", "calculation", e));
            throw new RuntimeException(e);
        }
    }
    
    private void handleCalcUpdated(Data message) {
        try {
            Calculation calc = (Calculation) message.getValue();
            view.logDebug("Updating calculation ID " + calc.getId());
            
            DatabaseManager.getInstance().getCalculationDao().update(calc);
            
            view.log("Calculation updated: " + calc.getName() + " (ID: " + calc.getId() + ")");
            server.broadcastToRoomExcl("calcRoom"+calc.getId(), new Data(Protocols.CALC_UPDATED, calc), this);
        } catch (SQLException e) {
                        view.logErr(view.formatDatabaseError("update", "calculation", e));
            throw new RuntimeException(e);
        }
    }
    
    private void handleJoinCalcRoom(Data message) {
        Calculation calc = (Calculation)message.getValue();
        String roomName = "calcRoom" + calc.getId();
        view.logDebug("User joining calculation room: " + roomName);
        
        joinRoom(roomName);
        
        try {
            Calculation updatedCalc = DatabaseManager.getInstance().getCalculationDao().queryForId(calc.getId());
            view.log("User joined calculation room: " + calc.getName() + " (ID: " + calc.getId() + ")");
            send(new Data(Protocols.CALC_UPDATED, updatedCalc));
        } catch (SQLException e) {
            view.logErr("Failed to retrieve calculation: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
    
    private void handleLeaveCalcRoom(Data message) {
        Calculation calc = (Calculation)message.getValue();
        String roomName = "calcRoom" + calc.getId();
        view.logDebug("User leaving calculation room: " + roomName);
        
        leaveRoom(roomName);
        view.log("User left calculation room: " + calc.getName() + " (ID: " + calc.getId() + ")");
    }

    private void handleGetUsers(Data message) {
        try {
            List<User> allUsers = DatabaseManager.getInstance().getUserDao().queryForAll();
            
            List<User> sanitizedUsers = new ArrayList<>();
            
            // Create sanitized versions of each user (without passwords)
            for (User user : allUsers) {
                User sanitizedUser = new User(user.getUsername(), "", user.getRole());
                sanitizedUsers.add(sanitizedUser);
            }
            
            send(new Data(Protocols.USERS_RETRIEVED, (Serializable) sanitizedUsers));
            view.log("User list retrieved by client");
            
        } catch (SQLException e) {
            view.logErr("Failed to retrieve users: " + e.getMessage());
            send(new Data(Protocols.USERS_RETRIEVAL_FAILED, "Database error: " + e.getMessage()));
        } catch (Exception e) {
            view.logErr("Error retrieving users: " + e.getMessage());
            send(new Data(Protocols.USERS_RETRIEVAL_FAILED, "Error: " + e.getMessage()));
        }
    }

    private void handleLoadUsers(Data message) {
        try {
            send(new Data(Protocols.LOADED_USERS, new ArrayList<>(DatabaseManager.getInstance().getUserDao().queryBuilder().query())));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void handleCreateAirport(Data message) {
        try {
            var airportData = (Airport) message.getValue();
            
            var queryBuilder = DatabaseManager.getInstance().getAirportDao().queryBuilder();
            List<Airport> existingAirports = queryBuilder
                    .where()
                    .eq("code", airportData.getCode())
                    .query();
                    
            if (!existingAirports.isEmpty()) {
                view.logWarning("Airport creation failed: Code already exists (" + airportData.getCode() + ")");
                send(new Data(Protocols.AIRPORT_CREATION_FAILED, "Airport code already exists"));
                return;
            }

            DatabaseManager.getInstance().getAirportDao().create(airportData);
            
            send(new Data(Protocols.AIRPORT_CREATED, airportData));
            view.log("New airport added: " + airportData.getName() + " (" + airportData.getCode() + ") in " + airportData.getLocation());
            
        } catch (SQLException e) {
            view.logErr(view.formatDatabaseError("creation", "airport", e));
            send(new Data(Protocols.AIRPORT_CREATION_FAILED, "Database error: " + e.getMessage()));
        } catch (Exception e) {
            view.logErr("Error creating airport: " + e.getMessage());
            send(new Data(Protocols.AIRPORT_CREATION_FAILED, "Error: " + e.getMessage()));
        }
    }

    private void handleUpdateAirport(Data message) {
        try {
            Airport updatedAirport = (Airport) message.getValue();
            view.logDebug("Updating airport ID " + updatedAirport.getId());

            DatabaseManager.getInstance().getAirportDao().update(updatedAirport);

            view.log("Airport updated: " + updatedAirport.getName() + " (ID: " + updatedAirport.getId() + ")");
            send(new Data(Protocols.AIRPORT_UPDATED, updatedAirport));
        } catch (SQLException e) {
            view.logErr(view.formatDatabaseError("update", "airport", e));
            throw new RuntimeException(e);
        }
    }
    
    // TODO: ! When links are done make sure to not allow deleting airports that have runways linked to them
    private void handleDeleteAirport(Data message) {
        try {
            // Expecting airport code to delete
            String airportCode = ((Airport) message.getValue()).getCode();
            
            var queryBuilder = DatabaseManager.getInstance().getAirportDao().queryBuilder();
            List<Airport> airports = queryBuilder
                    .where()
                    .eq("code", airportCode)
                    .query();
                    
            if (airports.isEmpty()) {
                view.logWarning("Airport deletion failed: Airport not found with code " + airportCode);
                send(new Data(Protocols.AIRPORT_DELETION_FAILED, "Airport not found"));
                return;
            }
            
            Airport airportToDelete = airports.get(0);
            
            // Check if the airport has associated runways
            List<Runway> relatedRunways = DatabaseManager.getInstance().getRunwayDao().queryBuilder()
                .where()
                .eq("airportId", airportToDelete.getId())
                .query();
            
            if (!relatedRunways.isEmpty()) {
                view.log("Airport " + airportCode + " has " + relatedRunways.size() + 
                        " associated runways that will also be deleted");
            }
            
            DatabaseManager.getInstance().getAirportDao().delete(airportToDelete);
            
            send(new Data(Protocols.AIRPORT_DELETED, airportCode));
            view.log("Airport deleted: " + airportToDelete.getName() + " (" + airportCode + ")");
            
        } catch (SQLException e) {
            view.logErr(view.formatDatabaseError("deletion", "airport", e));
            send(new Data(Protocols.AIRPORT_DELETION_FAILED, "Database error: " + e.getMessage()));
        } catch (Exception e) {
            view.logErr("Error deleting airport: " + e.getMessage());
            send(new Data(Protocols.AIRPORT_DELETION_FAILED, "Error: " + e.getMessage()));
        }
    }
    
    private void handleGetAirports(Data message) {
        try {
            List<Airport> allAirports = DatabaseManager.getInstance().getAirportDao().queryForAll();
            
            send(new Data(Protocols.AIRPORTS_RETRIEVED, (Serializable) allAirports));
            view.log("Airport list retrieved by client");
            
        } catch (SQLException e) {
            view.logErr("Failed to retrieve airports: " + e.getMessage());
            view.logErr(view.formatDatabaseError("retrieval", "airports", e));
            send(new Data(Protocols.AIRPORTS_RETRIEVAL_FAILED, "Database error: " + e.getMessage()));
        } catch (Exception e) {
            view.logErr("Error retrieving airports: " + e.getMessage());
            send(new Data(Protocols.AIRPORTS_RETRIEVAL_FAILED, "Error: " + e.getMessage()));
        }
    }

    private void handleCreateRunway(Data message) {
        try {
            Runway newRunway = (Runway) message.getValue();

            // Check if airport exists
            Airport airport = DatabaseManager.getInstance().getAirportDao().queryForId(newRunway.getAirportId());
            if (airport == null) {
                view.logWarning("Runway creation failed: Airport ID " + newRunway.getAirportId() + " doesn't exist");
                send(new Data(Protocols.RUNWAY_CREATION_FAILED, "Airport not found"));
                return;
            }
            
            var queryBuilder = DatabaseManager.getInstance().getRunwayDao().queryBuilder();
            List<Runway> existingRunways = queryBuilder
                    .where()
                    .eq("airportId", newRunway.getAirportId())
                    .and()
                    .eq("designation", newRunway.getDesignation())
                    .query();
                    
            if (!existingRunways.isEmpty()) {
                view.logWarning("Runway creation failed: Designation already exists for this airport (" + newRunway.getDesignation() + ")");
                send(new Data(Protocols.RUNWAY_CREATION_FAILED, "Runway designation already exists for this airport"));
                return;
            }

            DatabaseManager.getInstance().getRunwayDao().create(newRunway);
            
            send(new Data(Protocols.RUNWAY_CREATED, newRunway));
            view.log("New runway added: " + newRunway.getDesignation() + " for airport " + airport.getCode());
            
        } catch (SQLException e) {
            view.logErr(view.formatDatabaseError("creation", "runway", e));
            send(new Data(Protocols.RUNWAY_CREATION_FAILED, "Database error: " + e.getMessage()));
        } catch (Exception e) {
            view.logErr("Error creating runway: " + e.getMessage());
            send(new Data(Protocols.RUNWAY_CREATION_FAILED, "Error: " + e.getMessage()));
        }
    }
    
    private void handleGetRunways(Data message) {
        try {
            // The message value could be an airport ID to filter runways by airport or none to get all
            Object value = message.getValue();
            List<Runway> runways;
            
            if (value instanceof Integer) {
                int airportId = (Integer) value;
                Airport airport = DatabaseManager.getInstance().getAirportDao().queryForId(airportId);
                if (airport == null) {
                    send(new Data(Protocols.RUNWAYS_RETRIEVAL_FAILED, "Airport not found"));
                    return;
                }
                
                // Get runways for specific airport
                var queryBuilder = DatabaseManager.getInstance().getRunwayDao().queryBuilder();
                runways = queryBuilder
                        .where()
                        .eq("airportId", airportId)
                        .query();
                
                view.log("Retrieved runways for airport: " + airport.getCode());
            } else if (value instanceof String airportCode) {
                Airport airport = DatabaseManager.getInstance().getAirportDao().queryBuilder()
                        .where()
                        .eq("code", airportCode)
                        .queryForFirst();

                if (airport == null) {
                    send(new Data(Protocols.RUNWAYS_RETRIEVAL_FAILED, "Airport not found"));
                    return;
                }

                // Get runways for specific airport
                var queryBuilder = DatabaseManager.getInstance().getRunwayDao().queryBuilder();
                runways = queryBuilder
                        .where()
                        .eq("airportId", airport.getId())
                        .query();

                view.log("Retrieved runways for airport: " + airport.getCode());
            } else {
                // Get all runways
                runways = DatabaseManager.getInstance().getRunwayDao().queryForAll();
                view.log("Retrieved all runways");
            }
            
            send(new Data(Protocols.RUNWAYS_RETRIEVED, (Serializable) runways));
            
        } catch (SQLException e) {
            view.logErr("Failed to retrieve runways: " + e.getMessage());
            send(new Data(Protocols.RUNWAYS_RETRIEVAL_FAILED, "Database error: " + e.getMessage()));
        } catch (Exception e) {
            view.logErr("Error retrieving runways: " + e.getMessage());
            send(new Data(Protocols.RUNWAYS_RETRIEVAL_FAILED, "Error: " + e.getMessage()));
        }
    }
    
    private void handleDeleteRunway(Data message) {
        try {
            int runwayId = ((Runway) message.getValue()).getId();
            Runway runway = DatabaseManager.getInstance().getRunwayDao().queryForId(runwayId);
            if (runway == null) {
                view.logWarning("Runway deletion failed: Runway ID " + runwayId + " not found");
                send(new Data(Protocols.RUNWAY_DELETION_FAILED, "Runway not found"));
                return;
            }
            
            DatabaseManager.getInstance().getRunwayDao().delete(runway);
            
            send(new Data(Protocols.RUNWAY_DELETED, runwayId));
            view.log("Runway deleted: " + runway.getDesignation());
            
        } catch (SQLException e) {
            view.logErr(view.formatDatabaseError("deletion", "runway", e));
            send(new Data(Protocols.RUNWAY_DELETION_FAILED, "Database error: " + e.getMessage()));
        } catch (Exception e) {
            view.logErr("Error deleting runway: " + e.getMessage());
            send(new Data(Protocols.RUNWAY_DELETION_FAILED, "Error: " + e.getMessage()));
        }
    }

    // Initialize output stream for testing purposes. This method should only be called in test environments.
    void initializeForTesting(ObjectOutputStream outputStream) {
        this.output = outputStream;
    }
}
