package com.skyworld;

import com.github.lalyos.jfiglet.FigletFont;
import com.skyworld.config.Constants;
import com.skyworld.util.db.ConnectionPool;
import com.skyworld.util.infra.SkyCoreScheduler;
import com.skyworld.util.logging.Log;
import com.zaxxer.hikari.HikariDataSource;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;


import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static com.skyworld.config.Constants.*;
import static org.fusesource.jansi.Ansi.ansi;



public class Main {

    private static final String VERSION = "BETA";
    private static SkyCoreScheduler sscheduler;

    public static void main(String[] args) {
        try {
            bootstrap(args);
            Undertow server = buildServer();
            startServer(server);
            registerShutdownHook();
        } catch (Exception e) {
            System.err.println("Fatal error starting API: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void bootstrap(String[] args) throws Exception {
        System.setProperty(
                "java.util.logging.manager",
                "org.apache.logging.log4j.jul.LogManager"
        ); // route JUL logging to Log4J (OpenHTMLToPDF)

        Configurator.setRootLevel(
                org.apache.logging.log4j.Level.valueOf(
                        Constants.getLogLevel()
                )
        );

        if (Constants.getLogLevel().equalsIgnoreCase("DEBUG")) {
            Configurator.setLevel("service", Level.DEBUG);
            Configurator.setLevel("repository", Level.DEBUG);
            Configurator.setLevel("controller", Level.DEBUG);
            Configurator.setLevel("util", Level.DEBUG);
        }

        AnsiConsole.systemInstall();


        printBanner();


        if (dumpRequest()) {
            Log.warning(Main.class, "bootstrap", "Request dumping is ENABLED - disable in production");
        }

        ConnectionPool.initialize();


    }



    private static Undertow buildServer() {
        return Undertow.builder()
                .setIoThreads(getIoThreadPool())
                .setWorkerThreads(getWorkerThreadPool())
                .addHttpListener(getApiContextPort(), getApiContextHost())
                .setHandler(buildRouteHandler())
                .build();
    }

    private static void printBanner() throws IOException {
        String undertowVersion = Undertow.class.getPackage().getImplementationVersion();
        String hikariVersion = HikariDataSource.class.getPackage().getImplementationVersion();
        String javaVersion = System.getProperty("java.version");

        String stack = "Undertow " + orUnknown(undertowVersion)
                + " · HikariCP " + orUnknown(hikariVersion)
                + " · Java " + orUnknown(javaVersion);

        String ascii = FigletFont.convertOneLine("FootPrint");
        System.out.println();
        System.out.println(ansi().fg(Ansi.Color.GREEN).a(ascii).reset());
        System.out.println(ansi().render(
                "  @|faint ======================================================================================|@"
        ));
        System.out.println(ansi().render(
                "  @|bold,green FootPrint API|@  @|cyan " + VERSION + "|@   @|faint " + stack + "|@"
        ));
        System.out.println(ansi().render(
                "  @|faint ======================================================================================|@"
        ));
        System.out.println();
    }

    private static void startServer(Undertow server) {
        try {
            server.start();
            printStartupSummary();
        } catch (Exception e) {
            server.stop();
            throw e;
        }
    }

    private static void printStartupSummary() {
        String host = getApiContextHost();
        int port = getApiContextPort();
        String base = getApiContextPath();
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        var mx = ConnectionPool.getDataSource().getHikariPoolMXBean();
        int total = mx.getTotalConnections();
        int idle = mx.getIdleConnections();
        int active = mx.getActiveConnections();
        int pending = mx.getThreadsAwaitingConnection();
        String db = getDbHost() + ":" + getDbPort() + "/" + getDbName();
        String pool = "min=" + getDbPoolMin() + " max=" + getDbPoolMax()
                + "  @|faint active=" + active + " idle=" + idle
                + " total=" + total + " pending=" + pending + "|@";

        String displayHost = host.equals("0.0.0.0") ? "localhost" : host;
        System.out.println(ansi().render("  @|green -|@ @|bold Database|@  " + db + "  " + pool));
        System.out.println(ansi().render("  @|faint Ready · " + time + "|@"));
        System.out.println();
    }

    private static void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(Main::cleanup));
    }

    private static void cleanup() {
        Log.info(Main.class, "cleanup", "Shutting down API");
        ConnectionPool.shutdown();

        try {
            if (sscheduler != null) {
                sscheduler.shutdown();
                Log.info(Main.class, "cleanup",
                        "Scheduler shutdown gracefully");
            }
        } catch (Exception e) {
            Log.error(Main.class, "cleanup",
                    "Error shutting down scheduler", e);
        }
    }

    private static HttpHandler buildRouteHandler() {
        return null;
    }

    private static String orUnknown(String value) {
        return (value != null && !value.isBlank()) ? value : "?";
    }

}