//module shared {
//    exports org.example;
//
//    opens org.example to ormlite.jdbc;
//}
module shared {
    exports org.example; // Export the necessary packages from shared
    opens org.example to ormlite.jdbc;
}