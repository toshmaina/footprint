package com.skyworld.util.db;

import com.skyworld.config.Constants;
import com.skyworld.util.logging.Log;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;


import java.sql.Connection;
import java.sql.SQLException;




public final class ConnectionPool {

    private static volatile HikariDataSource dataSource;

    private ConnectionPool() {
    }

    public static void initialize() {
        if (dataSource == null) {
            synchronized (ConnectionPool.class) {
                if (dataSource == null) {
                    String host = Constants.getDbHost();
                    int port = Constants.getDbPort();
                    String dbName = Constants.getDbName();
                    String user = Constants.getDbUser();
                    String pass = Constants.getDbPass();
                    int minIdle = Constants.getDbPoolMin();
                    int maxSize = Constants.getDbPoolMax();
                    int timeout = Constants.getDbCheckoutTimeoutSeconds();

                    HikariConfig config = new HikariConfig();
                    config.setJdbcUrl("jdbc:postgresql://" + host + ":" + port + "/" + dbName);
                    config.setUsername(user);

//                    System.out.println(pass);
                    config.setPassword(pass);
                    config.setDriverClassName("org.postgresql.Driver");

                    config.setMinimumIdle(minIdle);
                    config.setMaximumPoolSize(maxSize);

                    config.setConnectionTimeout((long) timeout * 1_000);
                    config.setIdleTimeout(600_000);
                    config.setMaxLifetime(1_800_000);

                    config.setConnectionTestQuery("SELECT 1");
                    config.setKeepaliveTime(60_000);

                    config.setPoolName("Sky-Inventory Pool");

                    dataSource = new HikariDataSource(config);

                    Log.info(ConnectionPool.class, "init",
                            "HikariCP pool ready – min=" + minIdle +
                                    " max=" + maxSize + " url=" + config.getJdbcUrl());
                }
            }
        }
    }

    public static ConnectionPool getInstance() {

        if (dataSource == null)
            throw new IllegalStateException(
                    "ConnectionPool has not been initialised – call ConnectionPool.initialize() at startup");
        return new ConnectionPool();
    }

    public static void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            Log.info(ConnectionPool.class, "shutdown", "Closing HikariCP pool…");
            dataSource.close();
            dataSource = null;
        }
    }

    public static HikariDataSource getDataSource() {
        if (dataSource == null)
            throw new IllegalStateException("ConnectionPool has not been initialised");
        return dataSource;
    }

    public Connection borrow() throws SQLException {
        return dataSource.getConnection();
    }

    public void release(Connection connection) {
        if (connection == null) return;
        try {
            connection.close();
        } catch (SQLException ignore) {
        }
    }

    public int availableConnections() {
        return dataSource.getHikariPoolMXBean().getIdleConnections();
    }

    public int totalConnections() {
        return dataSource.getHikariPoolMXBean().getTotalConnections();
    }

    public int pendingThreads() {
        return dataSource.getHikariPoolMXBean().getThreadsAwaitingConnection();
    }
}