package ke.co.skyworld.internship.controllers;

import io.undertow.Handlers;
import io.undertow.server.HttpHandler;
import io.undertow.server.RoutingHandler;
import io.undertow.server.handlers.PathHandler;
import io.undertow.util.Methods;
import ke.co.skyworld.internship.controllers.handlers.advanceshippingnotice.CreateAdvanceShippingNoticeHandler;
import ke.co.skyworld.internship.controllers.handlers.advanceshippingnotice.GetAdvanceShippingNoticeHandler;
import ke.co.skyworld.internship.controllers.handlers.advanceshippingnotice.ListAdvanceShippingNoticesHandler;
import ke.co.skyworld.internship.controllers.handlers.auth.LoginHandler;
import ke.co.skyworld.internship.controllers.handlers.auth.LogoutHandler;
import ke.co.skyworld.internship.controllers.handlers.auth.RefreshHandler;
import ke.co.skyworld.internship.controllers.handlers.auth.RegisterHandler;
import ke.co.skyworld.internship.controllers.handlers.goodsreceipts.CreateGoodsReceiptHandler;
import ke.co.skyworld.internship.controllers.handlers.goodsreceipts.GetGoodsReceiptHandler;
import ke.co.skyworld.internship.controllers.handlers.goodsreceipts.ListGoodsReceiptsHandler;
import ke.co.skyworld.internship.controllers.handlers.licenseplate.CreateLicensePlatesHandler;
import ke.co.skyworld.internship.controllers.handlers.licenseplate.GetLicensePlateHandler;
import ke.co.skyworld.internship.controllers.handlers.products.*;
import ke.co.skyworld.internship.controllers.handlers.purchaseorder.CancelPurchaseOrderHandler;
import ke.co.skyworld.internship.controllers.handlers.purchaseorder.CreatePurchaseOrderHandler;
import ke.co.skyworld.internship.controllers.handlers.purchaseorder.GetPurchaseOrderHandler;
import ke.co.skyworld.internship.controllers.handlers.purchaseorder.ListPurchaseOrdersHandler;
import ke.co.skyworld.internship.controllers.handlers.putaway.ConfirmPutawayHandler;
import ke.co.skyworld.internship.controllers.handlers.putaway.CreatePutawayTaskHandler;
import ke.co.skyworld.internship.controllers.handlers.qa.RecordQaInspectionHandler;
import ke.co.skyworld.internship.controllers.handlers.suppliers.*;
import ke.co.skyworld.internship.controllers.handlers.warehouses.*;
import ke.co.skyworld.internship.util.http.CorsHandler;
import ke.co.skyworld.internship.util.http.Dispatcher;
import ke.co.skyworld.internship.util.http.FallBack;
import ke.co.skyworld.internship.util.http.InvalidMethod;
import ke.co.skyworld.internship.util.http.middleware.AuthMiddleware;

public class Routes {

    private static HttpHandler authed(HttpHandler handler) {
        return new AuthMiddleware(handler);
    }

    private static HttpHandler authed(HttpHandler handler, String... requiredPermissions) {
        return new AuthMiddleware(handler, requiredPermissions);
    }

    private static RoutingHandler auth() {
        return Handlers.routing()
                .add(Methods.POST, "/register", authed(new RegisterHandler(), "identity.users.create"))
                .add(Methods.POST, "/login", new LoginHandler())
                .add(Methods.POST, "/refresh", new RefreshHandler())
                .add(Methods.POST, "/logout", authed(new LogoutHandler()))
                .setFallbackHandler(new FallBack())
                .setInvalidMethodHandler(new InvalidMethod())
                .add(Methods.OPTIONS, "/*", new CorsHandler(exchange -> {}));
    }

    private static RoutingHandler products() {
        return Handlers.routing()
                .add(Methods.GET, "/", authed(new ListProductsHandler()))
                .add(Methods.GET, "/{id}", authed(new GetProductHandler()))
                .add(Methods.POST, "/", authed(new CreateProductHandler(), "inventory.products.write"))
                .add(Methods.PUT, "/{id}", authed(new UpdateProductHandler(), "inventory.products.write"))
                .add(Methods.DELETE, "/{id}", authed(new DeactivateProductHandler(), "inventory.products.write"))
                .setFallbackHandler(new FallBack())
                .setInvalidMethodHandler(new InvalidMethod())
                .add(Methods.OPTIONS, "/*", new CorsHandler(exchange -> {}));
    }

    private static RoutingHandler warehouses() {
        return Handlers.routing()
                .add(Methods.GET, "/", authed(new ListWarehousesHandler()))
                .add(Methods.GET, "/{id}", authed(new GetWarehouseHandler()))
                .add(Methods.POST, "/", authed(new CreateWarehouseHandler(), "inventory.warehouses.write"))
                .add(Methods.PUT, "/{id}", authed(new UpdateWarehouseHandler(), "inventory.warehouses.write"))
                .add(Methods.DELETE, "/{id}", authed(new DeactivateWarehouseHandler(), "inventory.warehouses.write"))
                .setFallbackHandler(new FallBack())
                .setInvalidMethodHandler(new InvalidMethod())
                .add(Methods.OPTIONS, "/*", new CorsHandler(exchange -> {}));
    }

    private static RoutingHandler suppliers() {
        return Handlers.routing()
                .add(Methods.GET, "/", authed(new ListSuppliersHandler()))
                .add(Methods.GET, "/{id}", authed(new GetSupplierHandler()))
                .add(Methods.POST, "/", authed(new CreateSupplierHandler(), "inventory.suppliers.write"))
                .add(Methods.PUT, "/{id}", authed(new UpdateSupplierHandler(), "inventory.suppliers.write"))
                .add(Methods.DELETE, "/{id}", authed(new DeactivateSupplierHandler(), "inventory.suppliers.write"))
                .setFallbackHandler(new FallBack())
                .setInvalidMethodHandler(new InvalidMethod())
                .add(Methods.OPTIONS, "/*", new CorsHandler(exchange -> {}));
    }

    private static RoutingHandler purchaseOrders() {
        return Handlers.routing()
                .add(Methods.GET, "/", authed(new ListPurchaseOrdersHandler()))
                .add(Methods.GET, "/{id}", authed(new GetPurchaseOrderHandler()))
                .add(Methods.POST, "/", authed(new CreatePurchaseOrderHandler(), "inventory.purchase_orders.write"))
                .add(Methods.POST, "/{id}/cancel", authed(new CancelPurchaseOrderHandler(), "inventory.purchase_orders.write"))
                .setFallbackHandler(new FallBack())
                .setInvalidMethodHandler(new InvalidMethod())
                .add(Methods.OPTIONS, "/*", new CorsHandler(exchange -> {}));
    }

    private static RoutingHandler advanceShippingNotices() {
        return Handlers.routing()
                .add(Methods.GET, "/", authed(new ListAdvanceShippingNoticesHandler()))
                .add(Methods.GET, "/{id}", authed(new GetAdvanceShippingNoticeHandler()))
                .add(Methods.POST, "/", authed(new CreateAdvanceShippingNoticeHandler(), "inventory.asns.write"))
                .setFallbackHandler(new FallBack())
                .setInvalidMethodHandler(new InvalidMethod())
                .add(Methods.OPTIONS, "/*", new CorsHandler(exchange -> {}));
    }

    private static RoutingHandler goodsReceipts() {
        return Handlers.routing()
                .add(Methods.GET, "/", authed(new ListGoodsReceiptsHandler()))
                .add(Methods.GET, "/{id}", authed(new GetGoodsReceiptHandler()))
                .add(Methods.POST, "/", authed(new CreateGoodsReceiptHandler(), "inventory.goods_receipts.write"))
                .setFallbackHandler(new FallBack())
                .setInvalidMethodHandler(new InvalidMethod())
                .add(Methods.OPTIONS, "/*", new CorsHandler(exchange -> {}));
    }

    /**
     * Separate prefix from goodsReceipts() - license plates are created
     * against a specific *line* id, not the receipt header, so this needs
     * its own "/goods-receipt-lines" prefix rather than nesting under
     * "/goods-receipts/{id}/lines/{lineId}/license-plates".
     */
    private static RoutingHandler goodsReceiptLines() {
        return Handlers.routing()
                .add(Methods.POST, "/{id}/license-plates", authed(new CreateLicensePlatesHandler(), "inventory.license_plates.write"))
                .setFallbackHandler(new FallBack())
                .setInvalidMethodHandler(new InvalidMethod())
                .add(Methods.OPTIONS, "/*", new CorsHandler(exchange -> {}));
    }

    private static RoutingHandler licensePlates() {
        return Handlers.routing()
                .add(Methods.GET, "/{id}", authed(new GetLicensePlateHandler()))
                // Single call records both the QA inspection and its
                // disposition (pass/fail/partial) - see RecordQaInspectionHandler.
                .add(Methods.POST, "/{id}/qa-inspections", authed(new RecordQaInspectionHandler(), "inventory.qa.write"))
                .add(Methods.POST, "/{id}/putaway-tasks", authed(new CreatePutawayTaskHandler(), "inventory.putaway.write"))
                .setFallbackHandler(new FallBack())
                .setInvalidMethodHandler(new InvalidMethod())
                .add(Methods.OPTIONS, "/*", new CorsHandler(exchange -> {}));
    }

    /**
     * /putaway-tasks/{id}/confirm is the concurrency-critical endpoint -
     * row-locked plus backstopped by a UNIQUE(putaway_task_id) constraint
     * on putaway_confirmations, so two concurrent confirmations of the same
     * task can't both succeed.
     */
    private static RoutingHandler putawayTasks() {
        return Handlers.routing()
                .add(Methods.POST, "/{id}/confirm", authed(new ConfirmPutawayHandler(), "inventory.putaway.write"))
                .setFallbackHandler(new FallBack())
                .setInvalidMethodHandler(new InvalidMethod())
                .add(Methods.OPTIONS, "/*", new CorsHandler(exchange -> {}));
    }

    public static HttpHandler buildRouteHandler() {
        PathHandler path = Handlers.path();

        path.addPrefixPath("/auth", new Dispatcher(auth()));
        path.addPrefixPath("/products", new Dispatcher(products()));
        path.addPrefixPath("/warehouses", new Dispatcher(warehouses()));
        path.addPrefixPath("/suppliers", new Dispatcher(suppliers()));
        path.addPrefixPath("/purchase-orders", new Dispatcher(purchaseOrders()));
        path.addPrefixPath("/advance-shipping-notices", new Dispatcher(advanceShippingNotices()));
        path.addPrefixPath("/goods-receipts", new Dispatcher(goodsReceipts()));
        path.addPrefixPath("/goods-receipt-lines", new Dispatcher(goodsReceiptLines()));
        path.addPrefixPath("/license-plates", new Dispatcher(licensePlates()));
        path.addPrefixPath("/putaway-tasks", new Dispatcher(putawayTasks()));

        return path;
    }
}