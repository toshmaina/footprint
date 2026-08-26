package ke.co.skyworld.internship;

import com.github.lalyos.jfiglet.FigletFont;
import ke.co.skyworld.internship.config.Constants;
import ke.co.skyworld.internship.util.db.ConnectionPool;
import ke.co.skyworld.internship.util.http.Dispatcher;
import ke.co.skyworld.internship.util.http.FallBack;
import ke.co.skyworld.internship.util.http.InvalidMethod;
import ke.co.skyworld.internship.util.http.handlers.advanceshippingnotice.CreateAdvanceShippingNoticeHandler;
import ke.co.skyworld.internship.util.http.handlers.advanceshippingnotice.GetAdvanceShippingNoticeHandler;
import ke.co.skyworld.internship.util.http.handlers.advanceshippingnotice.ListAdvanceShippingNoticesHandler;
import ke.co.skyworld.internship.util.http.handlers.auth.LoginHandler;
import ke.co.skyworld.internship.util.http.handlers.auth.LogoutHandler;
import ke.co.skyworld.internship.util.http.handlers.auth.RefreshHandler;
import ke.co.skyworld.internship.util.http.handlers.auth.RegisterHandler;
import ke.co.skyworld.internship.util.http.handlers.products.*;
import ke.co.skyworld.internship.util.http.handlers.purchaseorder.CancelPurchaseOrderHandler;
import ke.co.skyworld.internship.util.http.handlers.purchaseorder.CreatePurchaseOrderHandler;
import ke.co.skyworld.internship.util.http.handlers.purchaseorder.GetPurchaseOrderHandler;
import ke.co.skyworld.internship.util.http.handlers.purchaseorder.ListPurchaseOrdersHandler;
import ke.co.skyworld.internship.util.http.handlers.suppliers.*;
import ke.co.skyworld.internship.util.http.handlers.warehouses.*;
import ke.co.skyworld.internship.util.http.middleware.AuthMiddleware;
import ke.co.skyworld.internship.util.infra.SkyCoreScheduler;
import ke.co.skyworld.internship.util.logging.Log;
import com.zaxxer.hikari.HikariDataSource;
import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.RoutingHandler;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;


import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static ke.co.skyworld.internship.config.Constants.*;
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
        RoutingHandler router = Handlers.routing()
                .setFallbackHandler(new Dispatcher(new FallBack()))
                .setInvalidMethodHandler(new Dispatcher(new InvalidMethod()));

        router.post("/auth/register",  new Dispatcher(new AuthMiddleware(new RegisterHandler(), "identity.users.create")));
        router.post("/auth/login", new Dispatcher(new LoginHandler()));
        router.post("/auth/refresh", new Dispatcher(new RefreshHandler()));
        router.post("/auth/logout", new Dispatcher(new AuthMiddleware(new LogoutHandler())));

        // Products/Warehouses/Suppliers CRUD routes will register here next,
        // each wrapped in AuthMiddleware where a permission check is needed:
        //   router.get("/products", new Dispatcher(new AuthMiddleware(new ListProductsHandler())));

        router.get("/products", new Dispatcher(new AuthMiddleware(new ListProductsHandler())));
        router.get("/products/{id}", new Dispatcher(new AuthMiddleware(new GetProductHandler())));
        router.post("/products", new Dispatcher(new AuthMiddleware(new CreateProductHandler(), "inventory.products.write")));
        router.put("/products/{id}", new Dispatcher(new AuthMiddleware(new UpdateProductHandler(), "inventory.products.write")));
        router.delete("/products/{id}", new Dispatcher(new AuthMiddleware(new DeactivateProductHandler(), "inventory.products.write")));

        // Warehouses
        router.get("/warehouses", new Dispatcher(new AuthMiddleware(new ListWarehousesHandler())));
        router.get("/warehouses/{id}", new Dispatcher(new AuthMiddleware(new GetWarehouseHandler())));
        router.post("/warehouses", new Dispatcher(new AuthMiddleware(new CreateWarehouseHandler(), "inventory.warehouses.write")));
        router.put("/warehouses/{id}", new Dispatcher(new AuthMiddleware(new UpdateWarehouseHandler(), "inventory.warehouses.write")));
        router.delete("/warehouses/{id}", new Dispatcher(new AuthMiddleware(new DeactivateWarehouseHandler(), "inventory.warehouses.write")));

        // Suppliers
        router.get("/suppliers", new Dispatcher(new AuthMiddleware(new ListSuppliersHandler())));
        router.get("/suppliers/{id}", new Dispatcher(new AuthMiddleware(new GetSupplierHandler())));
        router.post("/suppliers", new Dispatcher(new AuthMiddleware(new CreateSupplierHandler(), "inventory.suppliers.write")));
        router.put("/suppliers/{id}", new Dispatcher(new AuthMiddleware(new UpdateSupplierHandler(), "inventory.suppliers.write")));
        router.delete("/suppliers/{id}", new Dispatcher(new AuthMiddleware(new DeactivateSupplierHandler(), "inventory.suppliers.write")));

        // Purchase Orders
        router.get("/purchase-orders", new Dispatcher(new AuthMiddleware(new ListPurchaseOrdersHandler())));
        router.get("/purchase-orders/{id}", new Dispatcher(new AuthMiddleware(new GetPurchaseOrderHandler())));
        router.post("/purchase-orders", new Dispatcher(new AuthMiddleware(new CreatePurchaseOrderHandler(), "inventory.purchase_orders.write")));
        router.delete("/purchase-orders/{id}", new Dispatcher(new AuthMiddleware(new CancelPurchaseOrderHandler(),"inventory.purchase_orders.write")));

        // Advance Shipping Notice
        router.get("/advance-shipping-notice", new Dispatcher(new AuthMiddleware(new ListAdvanceShippingNoticesHandler())));
        router.get("/advance-shipping-notice/{id}", new Dispatcher(new AuthMiddleware(new GetAdvanceShippingNoticeHandler())));
        router.post("/advance-shipping-notice", new Dispatcher(new AuthMiddleware(new CreateAdvanceShippingNoticeHandler(),"inventory.asns.write")));

        return router;
    }


    private static String orUnknown(String value) {
        return (value != null && !value.isBlank()) ? value : "?";
    }

}