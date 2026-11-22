package com.datashop;

import com.datashop.config.AppConfig;
import com.datashop.db.DatabaseMigrator;
import com.datashop.db.Jpa;
import com.datashop.db.LiquibaseMigrator;
import com.datashop.repository.CartRepository;
import com.datashop.repository.MediaAssetRepository;
import com.datashop.repository.OrderRepository;
import com.datashop.repository.ProductRepository;
import com.datashop.repository.PromoRepository;
import com.datashop.repository.UserRepository;
import com.datashop.repository.impl.JpaCartRepository;
import com.datashop.repository.impl.JpaMediaAssetRepository;
import com.datashop.repository.impl.JpaOrderRepository;
import com.datashop.repository.impl.JpaProductRepository;
import com.datashop.repository.impl.JpaPromoRepository;
import com.datashop.repository.impl.JpaUserRepository;
import com.datashop.security.AuthCookieService;
import com.datashop.security.CsrfService;
import com.datashop.security.PasswordEncoder;
import com.datashop.security.RateLimitService;
import com.datashop.security.RequestAuthenticator;
import com.datashop.security.TokenService;
import com.datashop.security.impl.AuthCookies;
import com.datashop.security.impl.CsrfTokens;
import com.datashop.security.impl.JwtTokenService;
import com.datashop.security.impl.PasswordHasher;
import com.datashop.security.impl.RateLimiter;
import com.datashop.security.impl.RequestAuth;
import com.datashop.security.impl.SecurityHeaders;
import com.datashop.service.auth.AuthService;
import com.datashop.service.auth.AuthSessionService;
import com.datashop.service.auth.impl.AuthServiceImpl;
import com.datashop.service.auth.impl.AuthSessionServiceImpl;
import com.datashop.service.cart.CartService;
import com.datashop.service.cart.CartServiceImpl;
import com.datashop.service.catalog.CatalogCache;
import com.datashop.service.catalog.ProductService;
import com.datashop.service.catalog.impl.InMemoryCatalogCache;
import com.datashop.service.catalog.impl.ProductServiceImpl;
import com.datashop.service.catalog.impl.RedisCatalogCache;
import com.datashop.service.catalog.impl.ResilientCatalogCache;
import com.datashop.service.download.DownloadService;
import com.datashop.service.download.DownloadServiceImpl;
import com.datashop.service.health.HealthService;
import com.datashop.service.health.HealthServiceImpl;
import com.datashop.service.media.MediaService;
import com.datashop.service.media.MediaStorage;
import com.datashop.service.media.ProductPreviewService;
import com.datashop.service.media.impl.DatabaseProductPreviewService;
import com.datashop.service.media.impl.MediaServiceImpl;
import com.datashop.service.media.impl.SupabaseMediaStorage;
import com.datashop.service.order.OrderService;
import com.datashop.service.order.OrderServiceImpl;
import com.datashop.service.seed.SeedService;
import com.datashop.service.seed.SeedServiceImpl;
import com.datashop.util.Constants;
import com.datashop.web.ErrorHandler;
import com.datashop.web.api.auth.AuthApiController;
import com.datashop.web.api.cart.CartApiController;
import com.datashop.web.api.download.DownloadApiController;
import com.datashop.web.api.health.HealthApiController;
import com.datashop.web.api.media.AdminMediaApiController;
import com.datashop.web.api.media.MediaAssetApiController;
import com.datashop.web.api.order.OrderApiController;
import com.datashop.web.api.product.ProductApiController;
import com.datashop.web.page.about.AboutPageController;
import com.datashop.web.page.admin.AdminPageController;
import com.datashop.web.page.auth.AuthPageController;
import com.datashop.web.page.cart.CartPageController;
import com.datashop.web.page.catalog.CatalogPageController;
import com.datashop.web.page.order.OrdersPageController;
import com.datashop.web.page.product.ProductPageController;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.javalin.Javalin;
import io.javalin.config.SizeUnit;
import io.javalin.http.staticfiles.Location;
import io.javalin.json.JavalinJackson;
import io.javalin.openapi.plugin.OpenApiPlugin;
import io.javalin.openapi.plugin.OpenApiPluginConfiguration;
import io.javalin.openapi.plugin.swagger.SwaggerPlugin;
import io.javalin.rendering.template.JavalinThymeleaf;
import lombok.extern.slf4j.Slf4j;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.util.function.Consumer;

@Slf4j
public class Application {

    public static void main(String[] args) {
        AppConfig config = AppConfig.load();
        DatabaseMigrator migrator = new LiquibaseMigrator();
        migrator.migrate(config.getDatabase(), config.isSeedEnabled());
        Jpa jpa = new Jpa(config.getDatabase());
        CatalogCache cache = createCache(config);
        try {
            Services services = wire(config, jpa, cache);
            services.seedService().seed();
            services.productPreviewService().ensurePreviews();

            Javalin app = createApp(config, services);
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                log.info("Stopping DataShop");
                app.stop();
                cache.close();
                jpa.close();
            }, Constants.App.SHUTDOWN_HOOK_NAME));

            app.start(Constants.App.HOST, config.getPort());
            log.info("DataShop is listening on http://{}:{}", Constants.App.HOST, config.getPort());
        } catch (RuntimeException exception) {
            cache.close();
            jpa.close();
            log.error("Cannot start DataShop", exception);
            throw exception;
        }
    }

    public static Javalin createApp(
            AppConfig config,
            Services services
    ) {
        ObjectMapper mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        TemplateEngine templateEngine = templateEngine();
        Javalin app = Javalin.create(javalinConfig -> {
            javalinConfig.showJavalinBanner = false;
            javalinConfig.http.defaultContentType = Constants.App.DEFAULT_CONTENT_TYPE;
            javalinConfig.http.maxRequestSize = config.getDatasetMaxBytes() + Constants.App.MULTIPART_EXTRA_BYTES;
            javalinConfig.jetty.multipartConfig.maxFileSize(
                    config.getDatasetMaxBytes() + Constants.App.MULTIPART_FILE_EXTRA_BYTES, SizeUnit.BYTES);
            javalinConfig.jetty.multipartConfig.maxTotalRequestSize(
                    config.getDatasetMaxBytes() + Constants.App.MULTIPART_EXTRA_BYTES, SizeUnit.BYTES);
            javalinConfig.jetty.multipartConfig.maxInMemoryFileSize(
                    Constants.App.MULTIPART_MAX_IN_MEMORY_MB, SizeUnit.MB);

            javalinConfig.fileRenderer(new JavalinThymeleaf(templateEngine));
            javalinConfig.jsonMapper(new JavalinJackson(mapper, false));
            javalinConfig.staticFiles.add(staticFiles -> {
                staticFiles.hostedPath = Constants.App.STATIC_HOSTED_PATH;
                staticFiles.directory = Constants.App.STATIC_DIRECTORY;
                staticFiles.location = Location.CLASSPATH;
            });
            javalinConfig.registerPlugin(openApiPlugin(Constants.Routes.OPENAPI));
            javalinConfig.registerPlugin(openApiPlugin(Constants.Routes.DOCS_JSON));
            javalinConfig.registerPlugin(new SwaggerPlugin(swagger -> {
                swagger.setDocumentationPath(Constants.Routes.OPENAPI);
                swagger.setUiPath(Constants.Routes.DOCS);
                swagger.setTitle(Constants.Swagger.UI_TITLE);
                swagger.setValidatorUrl(null);
            }));

            if (!config.getCorsOrigins().isEmpty()) {
                javalinConfig.bundledPlugins.enableCors(cors -> cors.addRule(rule -> {
                    rule.allowCredentials = true;
                    config.getCorsOrigins().forEach(rule::allowHost);
                    rule.exposeHeader(Constants.Http.CACHE_HEADER);
                }));
            }
        });

        app.after(new SecurityHeaders(config)::apply);
        ErrorHandler.register(app);

        new HealthApiController(services.healthService()).register(app);
        new AuthApiController(
                services.authService(),
                services.authSessionService(),
                services.tokenService(),
                services.requestAuth(),
                services.rateLimiter()
        ).register(app);
        new ProductApiController(services.productService(), services.requestAuth()).register(app);
        new CartApiController(services.cartService(), services.requestAuth()).register(app);
        new OrderApiController(services.orderService(), services.requestAuth(), services.rateLimiter())
                .register(app);
        new DownloadApiController(services.downloadService(), services.requestAuth()).register(app);
        new MediaAssetApiController(services.mediaService()).register(app);
        new AdminMediaApiController(services.mediaService(), services.requestAuth()).register(app);

        new CatalogPageController(
                services.productService(), services.cartService(), services.requestAuth(),
                services.csrfService()
        ).register(app);
        new ProductPageController(
                services.productService(), services.cartService(), services.orderService(),
                services.downloadService(), services.requestAuth(), services.csrfService()
        ).register(app);
        new CartPageController(
                services.cartService(), services.orderService(), services.requestAuth(),
                services.csrfService()
        ).register(app);
        new OrdersPageController(
                services.orderService(), services.cartService(), services.requestAuth(),
                services.csrfService()
        ).register(app);
        new AuthPageController(
                services.authService(), services.tokenService(), services.authCookies(),
                services.requestAuth(), services.cartService(), services.csrfService(),
                services.rateLimiter()
        ).register(app);
        new AdminPageController(
                services.productService(), services.mediaService(), services.cartService(),
                services.requestAuth(), services.csrfService()
        ).register(app);
        new AboutPageController(services.cartService(), services.requestAuth(), services.csrfService())
                .register(app);

        return app;
    }

    public static Services wire(AppConfig config, Jpa jpa, CatalogCache cache) {
        UserRepository userRepository = new JpaUserRepository();
        ProductRepository productRepository = new JpaProductRepository();
        MediaAssetRepository mediaAssetRepository = new JpaMediaAssetRepository();
        PromoRepository promoRepository = new JpaPromoRepository();
        CartRepository cartRepository = new JpaCartRepository();
        OrderRepository orderRepository = new JpaOrderRepository();

        PasswordEncoder passwordEncoder = new PasswordHasher();
        TokenService tokenService = new JwtTokenService(
                config.getJwtSecret(), config.getJwtTtl());
        AuthCookieService authCookies = new AuthCookies(config, config.getJwtTtl());
        RequestAuthenticator requestAuth = new RequestAuth(tokenService);
        CsrfService csrfService = new CsrfTokens(config);
        RateLimitService rateLimiter = new RateLimiter();

        String supabaseUrl = config.getSupabaseUrl();
        String publishableKey = config.getSupabasePublishableKey();
        String serviceRoleKey = config.getSupabaseServiceRoleKey();
        String storageEmail = config.getSupabaseStorageEmail();
        String storagePassword = config.getSupabaseStoragePassword();
        if (!isConfigured(supabaseUrl)) {
            throw new IllegalStateException(Constants.Messages.SUPABASE_REQUIRED);
        }
        boolean serviceRoleConfigured = isConfigured(serviceRoleKey);
        boolean storageUserConfigured = isConfigured(publishableKey)
                && isConfigured(storageEmail)
                && isConfigured(storagePassword);
        if (!serviceRoleConfigured && !storageUserConfigured) {
            throw new IllegalStateException(Constants.Messages.SUPABASE_STORAGE_AUTH_REQUIRED);
        }
        MediaStorage mediaStorage = new SupabaseMediaStorage(
                supabaseUrl, publishableKey, serviceRoleKey, storageEmail, storagePassword);
        AuthService authService = new AuthServiceImpl(jpa, userRepository, cartRepository, passwordEncoder);
        AuthSessionService authSessionService = new AuthSessionServiceImpl(authCookies);
        ProductPreviewService productPreviewService = new DatabaseProductPreviewService(
                jpa, productRepository, mediaAssetRepository, cache);
        ProductService productService = new ProductServiceImpl(
                jpa, productRepository, mediaAssetRepository, productPreviewService, cache, config);
        CartService cartService = new CartServiceImpl(
                jpa, cartRepository, productRepository, promoRepository, userRepository);
        OrderService orderService = new OrderServiceImpl(
                jpa, cartRepository, orderRepository, productRepository, cache);
        DownloadService downloadService = new DownloadServiceImpl(
                productService, orderService, mediaStorage);
        MediaService mediaService = new MediaServiceImpl(
                mediaStorage, productService, config, jpa, mediaAssetRepository);
        HealthService healthService = new HealthServiceImpl(jpa);
        SeedService seedService = new SeedServiceImpl(
                jpa, userRepository, passwordEncoder, config);

        return new Services(
                authService, authSessionService, productService, cartService, orderService,
                downloadService, mediaService, productPreviewService, healthService, seedService,
                tokenService, authCookies, requestAuth, csrfService, rateLimiter
        );
    }

    private static OpenApiPlugin openApiPlugin(String documentationPath) {
        return new RepeatableOpenApiPlugin(openApi -> openApi
                .withDocumentationPath(documentationPath)
                .withPrettyOutput()
                .withDefinitionConfiguration((version, definition) -> definition
                        .withInfo(info -> info
                                .title(Constants.Swagger.TITLE)
                                .description(Constants.Swagger.DESCRIPTION)
                                .version(Constants.Swagger.VERSION))
                        .withServer(server -> server
                                .url(Constants.Swagger.SERVER_URL)
                                .description(Constants.Swagger.SERVER_DESCRIPTION))
                        .withSecurity(security -> security.withBearerAuth(Constants.Swagger.BEARER_AUTH))
                        .withDefinitionProcessor(Application::decorateOpenApi)));
    }

    private static String decorateOpenApi(ObjectNode content) {
        ObjectNode bearerAuth = content
                .with("components")
                .with("securitySchemes")
                .with(Constants.Swagger.BEARER_AUTH);
        bearerAuth.put("bearerFormat", Constants.Swagger.BEARER_FORMAT);
        bearerAuth.put("description", Constants.Swagger.BEARER_AUTH_DESCRIPTION);

        ArrayNode tags = content.putArray("tags");
        addTag(tags, Constants.Swagger.TAG_AUTH, Constants.Swagger.TAG_AUTH_DESCRIPTION);
        addTag(tags, Constants.Swagger.TAG_PRODUCTS, Constants.Swagger.TAG_PRODUCTS_DESCRIPTION);
        addTag(tags, Constants.Swagger.TAG_CART, Constants.Swagger.TAG_CART_DESCRIPTION);
        addTag(tags, Constants.Swagger.TAG_ORDERS, Constants.Swagger.TAG_ORDERS_DESCRIPTION);
        addTag(tags, Constants.Swagger.TAG_MEDIA, Constants.Swagger.TAG_MEDIA_DESCRIPTION);
        addTag(tags, Constants.Swagger.TAG_DOWNLOADS, Constants.Swagger.TAG_DOWNLOADS_DESCRIPTION);
        addTag(tags, Constants.Swagger.TAG_HEALTH, Constants.Swagger.TAG_HEALTH_DESCRIPTION);
        return content.toPrettyString();
    }

    private static void addTag(ArrayNode tags, String name, String description) {
        tags.addObject()
                .put("name", name)
                .put("description", description);
    }

    private static final class RepeatableOpenApiPlugin extends OpenApiPlugin {

        private RepeatableOpenApiPlugin(Consumer<OpenApiPluginConfiguration> config) {
            super(config);
        }

        @Override
        public boolean repeatable() {
            return true;
        }
    }

    private static CatalogCache createCache(AppConfig config) {
        if (config.getRedisUrl() != null && !config.getRedisUrl().isBlank()) {
            ObjectMapper mapper = new ObjectMapper()
                    .registerModule(new JavaTimeModule())
                    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            try {
                CatalogCache cache = new RedisCatalogCache(config.getRedisUrl(), mapper);
                log.info("Using Redis catalog cache");
                return new ResilientCatalogCache(cache);
            } catch (RuntimeException exception) {
                log.error("Redis is unavailable, using in-memory catalog cache: {}", exception.getMessage());
            }
        }
        return new ResilientCatalogCache(new InMemoryCatalogCache());
    }

    private static boolean isConfigured(String value) {
        return value != null
                && !value.isBlank()
                && !"placeholder".equalsIgnoreCase(value.trim());
    }

    private static TemplateEngine templateEngine() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix(Constants.App.TEMPLATES_PREFIX);
        resolver.setSuffix(Constants.App.TEMPLATE_SUFFIX);
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCharacterEncoding(Constants.App.TEMPLATE_ENCODING);
        resolver.setCacheable(true);

        TemplateEngine engine = new TemplateEngine();
        engine.setTemplateResolver(resolver);
        return engine;
    }

    public record Services(
            AuthService authService,
            AuthSessionService authSessionService,
            ProductService productService,
            CartService cartService,
            OrderService orderService,
            DownloadService downloadService,
            MediaService mediaService,
            ProductPreviewService productPreviewService,
            HealthService healthService,
            SeedService seedService,
            TokenService tokenService,
            AuthCookieService authCookies,
            RequestAuthenticator requestAuth,
            CsrfService csrfService,
            RateLimitService rateLimiter
    ) {
    }
}
