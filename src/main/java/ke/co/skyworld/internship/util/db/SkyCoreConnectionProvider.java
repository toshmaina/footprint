package ke.co.skyworld.internship.util.db;

import ke.co.skyworld.internship.util.logging.Log;
import org.quartz.utils.ConnectionProvider;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * sky-core (ke.co.skyworld.internship.skycore.util.db)
 * Created by: oloo
 * On: 8/14/26. 11:42 AM
 * Description: Adapts Quartz's ConnectionProvider SPI onto the application's
 * existing ConnectionPool (HikariCP)
 **/

public class SkyCoreConnectionProvider implements ConnectionProvider {

    @Override
    public Connection getConnection() throws SQLException {
        return ConnectionPool.getInstance().borrow();
    }

    @Override
    public void initialize() {
        if (ConnectionPool.getDataSource() == null) {
            throw new IllegalStateException(
                    "ConnectionPool.initialize() must run before the scheduler starts");
        }
        Log.info(SkyCoreConnectionProvider.class, "initialize",
                "Quartz will borrow connections from the shared ConnectionPool");
    }

    @Override
    public void shutdown() {
        // ConnectionPool owns the underlying HikariDataSource's lifecycle;
        // Main.cleanup() already calls ConnectionPool.shutdown(). Quartz must
        // not close a pool it doesn't own.
    }
}
