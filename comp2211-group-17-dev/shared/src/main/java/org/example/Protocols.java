package org.example;

public class Protocols {
    // User
    public final static String CREATE_USER = "CREATE_USER";
    public final static String USER_CREATED = "USER_CREATED";
    public static final String USER_CREATION_FAILED = "USER_CREATION_FAILED";

    public static final String DELETE_USER = "DELETE_USER";
    public static final String USER_DELETED = "USER_DELETED";
    public static final String USER_DELETION_FAILED = "USER_DELETION_FAILED";

    public static final String UPDATE_USER = "UPDATE_USER";
    public static final String USER_UPDATED = "USER_UPDATED";
    public static final String USER_UPDATE_FAILED = "USER_UPDATE_FAILED";
    
    public static final String GET_USERS = "GET_USERS";
    public static final String USERS_RETRIEVED = "USERS_RETRIEVED";
    public static final String USERS_RETRIEVAL_FAILED = "USERS_RETRIEVAL_FAILED";

    // Login
    public static final String USER_LOGIN = "USER_LOGIN";
    public static final String LOGGED_IN = "LOGGED_IN";
    public static final String FAILED_LOGIN = "FAILED_LOGIN";

    
    // Airports
    public final static String UPDATE_AIRPORT = "UPDATE_AIRPORT";
    public final static String AIRPORT_UPDATED = "AIRPORT_UPDATED";
    public final static String CREATE_AIRPORT = "CREATE_AIRPORT";
    public final static String AIRPORT_CREATED = "AIRPORT_CREATED";
    public final static String AIRPORT_CREATION_FAILED = "AIRPORT_CREATION_FAILED";

    public final static String DELETE_AIRPORT = "DELETE_AIRPORT";
    public final static String AIRPORT_DELETED = "AIRPORT_DELETED";
    public final static String AIRPORT_DELETION_FAILED = "AIRPORT_DELETION_FAILED";

    public final static String GET_AIRPORTS = "GET_AIRPORTS";
    public final static String AIRPORTS_RETRIEVED = "AIRPORTS_RETRIEVED";
    public final static String AIRPORTS_RETRIEVAL_FAILED = "AIRPORTS_RETRIEVAL_FAILED";
    
    // Runways
    public final static String CREATE_RUNWAY = "CREATE_RUNWAY";
    public final static String RUNWAY_CREATED = "RUNWAY_CREATED";
    public final static String RUNWAY_CREATION_FAILED = "RUNWAY_CREATION_FAILED";

    public final static String DELETE_RUNWAY = "DELETE_RUNWAY";
    public final static String RUNWAY_DELETED = "RUNWAY_DELETED";
    public final static String RUNWAY_DELETION_FAILED = "RUNWAY_DELETION_FAILED";

    public final static String GET_RUNWAYS = "GET_RUNWAYS";
    public final static String RUNWAYS_RETRIEVED = "RUNWAYS_RETRIEVED";
    public final static String RUNWAYS_RETRIEVAL_FAILED = "RUNWAYS_RETRIEVAL_FAILED";

    public static final String RUNWAY_UPDATED = "RUNWAY_UPDATED";
    public static final String UPDATE_RUNWAY = "UPDATE_RUNWAY";


    // Calculations + room
    public final static String LOAD_CALCS = "LOAD_CALCS";
    public final static String LOADED_CALCS = "LOADED_CALCS";
    public final static String CREATE_CALC = "CREATE_CALC";
    public final static String CALC_CREATED = "CALC_CREATED";

    public final static String CALC_UPDATED = "CALC_UPDATED";
    public final static String JOIN_CALC_ROOM = "JOIN_CALC_ROOM";
    public final static String LEAVE_CALC_ROOM = "LEAVE_CALC_ROOM";
    public final static String LOAD_USERS = "LOAD_USERS";
    public final static String LOADED_USERS = "LOADED_USERS";
}
