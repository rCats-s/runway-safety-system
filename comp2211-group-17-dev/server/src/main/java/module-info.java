module server {
    requires shared;
    requires org.xerial.sqlitejdbc;
    requires ormlite.jdbc;

    exports org.example.server;
}