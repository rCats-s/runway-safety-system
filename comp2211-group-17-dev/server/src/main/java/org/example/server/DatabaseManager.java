package org.example.server;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.field.DatabaseFieldConfig;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.support.DatabaseConnection;
import com.j256.ormlite.table.DatabaseTableConfig;
import com.j256.ormlite.table.TableUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import org.example.*;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class DatabaseManager {
    private static DatabaseManager instance;
    private static final String DB_URL = "jdbc:sqlite:database.db";
    private ConnectionSource connSrc;

    private Dao<User, Integer> userDao;
    private Dao<Calculation, Integer> calculationDao;
    private Dao<Obstacle, Integer> obstacleDao;
    private Dao<Airport, Integer> airportDao;
    private Dao<Runway, Integer> runwayDao;

    public DatabaseManager() throws SQLException {
        initDb();
    }

    public static DatabaseManager getInstance() throws SQLException {
        if (instance == null) instance = new DatabaseManager();
        return instance;
    }

    public static void setInstance(DatabaseManager manager) {
        instance = manager;
    }

    public ConnectionSource getConnSrc() {
        return connSrc;
    }

    public void initDb() throws SQLException {
        connSrc = new JdbcConnectionSource(DB_URL);
        
        // Enable SQLite foreign key constraints
        try (Connection conn = java.sql.DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON");
        }
        
        createTables();
        ensureDefaultAdminUserExists();
        ensureDefaultAirportsExist();
        System.out.println("tables created");
    }

    public void close() throws Exception {
        if (connSrc != null) connSrc.close();
    }

    private void createTables() throws SQLException {
        {
            var id = new DatabaseFieldConfig("id");
            id.setGeneratedId(true);
            var username = new DatabaseFieldConfig("username");
            username.setCanBeNull(false);
            username.setUnique(true);
            var password = new DatabaseFieldConfig("password");
            password.setCanBeNull(false);
            var role = new DatabaseFieldConfig("role");
            DatabaseTableConfig<User> userConfig = new DatabaseTableConfig<>(
                    User.class,
                    "users",
                    Arrays.asList(
                            id, username, password, role
                    )
            );

            TableUtils.createTableIfNotExists(connSrc, userConfig);
            userDao = DaoManager.createDao(connSrc, userConfig);
        }

        {
            var id = new DatabaseFieldConfig("id");
            id.setGeneratedId(true);
            var name = new DatabaseFieldConfig("name");
            name.setCanBeNull(false);
            var calculationTimestamp = new DatabaseFieldConfig("calculationTimestamp");
            calculationTimestamp.setDefaultValue(String.valueOf(new Date().getTime()));
            var newTORA = new DatabaseFieldConfig("newTORA");
            newTORA.setCanBeNull(false);
            newTORA.setDefaultValue("0");
            var newTODA = new DatabaseFieldConfig("newTODA");
            newTODA.setCanBeNull(false);
            newTODA.setDefaultValue("0");
            var newASDA = new DatabaseFieldConfig("newASDA");
            newASDA.setCanBeNull(false);
            newASDA.setDefaultValue("0");
            var newLDA = new DatabaseFieldConfig("newLDA");
            newLDA.setCanBeNull(false);
            newLDA.setDefaultValue("0");
            var details = new DatabaseFieldConfig("details");
            details.setCanBeNull(false);
            details.setDefaultValue("");

            DatabaseTableConfig<Calculation> userConfig = new DatabaseTableConfig<>(
                    Calculation.class,
                    "calculations",
                    Arrays.asList(
                            id, name, calculationTimestamp, newTORA, newTODA, newASDA, newLDA, details
                    )
            );

            TableUtils.createTableIfNotExists(connSrc, userConfig);
            calculationDao = DaoManager.createDao(connSrc, userConfig);
        }

        {
            var id = new DatabaseFieldConfig("id");
            id.setGeneratedId(true);
            var name = new DatabaseFieldConfig("name");
            var height = new DatabaseFieldConfig("height");
            height.setCanBeNull(false);
            var distanceFromThreshold = new DatabaseFieldConfig("distanceFromThreshold");
            distanceFromThreshold.setCanBeNull(false);
            var offsetFromCenterLine = new DatabaseFieldConfig("offsetFromCenterLine");
            offsetFromCenterLine.setCanBeNull(false);
            var description = new DatabaseFieldConfig("description");

            DatabaseTableConfig<Obstacle> userConfig = new DatabaseTableConfig<>(
                    Obstacle.class,
                    "obstacle",
                    Arrays.asList(
                            id, name, height, distanceFromThreshold, offsetFromCenterLine, description
                    )
            );

            TableUtils.createTableIfNotExists(connSrc, userConfig);
            obstacleDao = DaoManager.createDao(connSrc, userConfig);
        }

        {
            User adminUser = new User("admin", "password", "Admin");
            boolean notExists = userDao.queryBuilder()
                    .where()
                    .eq("username", "admin")
                    .query()
                    .isEmpty();
            if (notExists) {
                userDao.create(adminUser);
            }
        }

        {
            var id = new DatabaseFieldConfig("id");
            id.setGeneratedId(true);
            var name = new DatabaseFieldConfig("name");
            name.setCanBeNull(false);
            var code = new DatabaseFieldConfig("code");
            code.setCanBeNull(false);
            code.setUnique(true);
            var location = new DatabaseFieldConfig("location");
            location.setCanBeNull(false);

            DatabaseTableConfig<Airport> airportConfig = new DatabaseTableConfig<>(
                    Airport.class,
                    "airports",
                    Arrays.asList(
                            id, name, code, location
                    )
            );

            TableUtils.createTableIfNotExists(connSrc, airportConfig);
            airportDao = DaoManager.createDao(connSrc, airportConfig);
        }

        {
            var id = new DatabaseFieldConfig("id");
            id.setGeneratedId(true);
            var designation = new DatabaseFieldConfig("designation");
            designation.setCanBeNull(false);
            var airportId = new DatabaseFieldConfig("airportId");
            airportId.setCanBeNull(false);
            var originalTORA = new DatabaseFieldConfig("originalTORA");
            originalTORA.setCanBeNull(false);
            var originalTODA = new DatabaseFieldConfig("originalTODA");
            originalTODA.setCanBeNull(false);
            var originalASDA = new DatabaseFieldConfig("originalASDA");
            originalASDA.setCanBeNull(false);
            var originalLDA = new DatabaseFieldConfig("originalLDA");
            originalLDA.setCanBeNull(false);
            var displacedThreshold = new DatabaseFieldConfig("displacedThreshold");
            displacedThreshold.setCanBeNull(false);
            displacedThreshold.setDefaultValue("0");
            var clearway = new DatabaseFieldConfig("clearway");
            clearway.setCanBeNull(false);
            clearway.setDefaultValue("0");
            var stopway = new DatabaseFieldConfig("stopway");
            stopway.setCanBeNull(false);
            stopway.setDefaultValue("0");

            DatabaseTableConfig<Runway> runwayConfig = new DatabaseTableConfig<>(
                    Runway.class,
                    "runways",
                    Arrays.asList(
                            id, designation, airportId, originalTORA, originalTODA, originalASDA, originalLDA, 
                            displacedThreshold, clearway, stopway
                    )
            );

            TableUtils.createTableIfNotExists(connSrc, runwayConfig);
            runwayDao = DaoManager.createDao(connSrc, runwayConfig);
            
            // Under the hood for auto deleting runways when an airport is deleted
            try (Connection conn = java.sql.DriverManager.getConnection(DB_URL);
                 Statement stmt = conn.createStatement()) {
                // Check if we need to add the constraint
                ResultSet rs = stmt.executeQuery(
                    "SELECT COUNT(*) FROM sqlite_master WHERE type='table' AND name='runways' AND " +
                    "sql LIKE '%FOREIGN KEY%REFERENCES%airports%'");
                
                if (rs.next() && rs.getInt(1) == 0) {
                    // Recreate the table with proper constraints if needed
                    stmt.execute(
                        "CREATE TABLE IF NOT EXISTS runways_new (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "designation VARCHAR NOT NULL, " +
                        "airportId INTEGER NOT NULL, " +
                        "originalTORA INTEGER NOT NULL, " +
                        "originalTODA INTEGER NOT NULL, " +
                        "originalASDA INTEGER NOT NULL, " +
                        "originalLDA INTEGER NOT NULL, " +
                        "displacedThreshold INTEGER NOT NULL DEFAULT 0, " +
                        "clearway INTEGER NOT NULL DEFAULT 0, " +
                        "stopway INTEGER NOT NULL DEFAULT 0, " +
                        "FOREIGN KEY (airportId) REFERENCES airports(id) ON DELETE CASCADE)");
                    
                    // Copy data if there's any
                    stmt.execute("INSERT OR IGNORE INTO runways_new SELECT * FROM runways");
                    stmt.execute("DROP TABLE runways");
                    stmt.execute("ALTER TABLE runways_new RENAME TO runways");
                }
            }
        }

        // example usage
//        User newUser = new User("test", "123");
//        userDao.create(newUser);

    }

    private void ensureDefaultAdminUserExists() throws SQLException {
        // Check if admin user exists
        var queryBuilder = userDao.queryBuilder();
        var admins = queryBuilder
                .where()
                .eq("username", "admin")
                .query();
                
        // If admin doesn't exist, create one
        if (admins.isEmpty()) {
            User adminUser = new User("admin", "admin", "admin");
            userDao.create(adminUser);
            System.out.println("Default admin user created (username: admin, password: admin)");
        }
    }
    
    private void ensureDefaultAirportsExist() throws SQLException {
        // Check if any airports exist
        List<Airport> airports = airportDao.queryForAll();
        
        if (airports.isEmpty()) {
            // Populate with some default airports
            Airport[] defaultAirports = {
                new Airport("London Heathrow", "LHR", "London, UK"),
                new Airport("Southampton Airport", "SOU", "Southampton, UK")
            };
            
            for (Airport airport : defaultAirports) {
                airportDao.create(airport);
            }
            
            System.out.println("Populated database with default airports");
        }
    }

    public Dao<User, Integer> getUserDao() {
        return userDao;
    }

    public Dao<Calculation, Integer> getCalculationDao() {
        return calculationDao;
    }

    public Dao<Obstacle, Integer> getObstacleDao() {
        return obstacleDao;
    }

    public Dao<Airport, Integer> getAirportDao() {
        return airportDao;
    }

    public Dao<Runway, Integer> getRunwayDao() {
        return runwayDao;
    }
}
