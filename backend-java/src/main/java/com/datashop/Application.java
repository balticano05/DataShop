package com.datashop;

import com.datashop.config.AppConfig;
import com.datashop.db.Jpa;
import com.datashop.db.SchemaInitializer;
import com.datashop.repository.CartRepository;
import com.datashop.repository.JpaCartRepository;
import com.datashop.repository.JpaOrderRepository;
import com.datashop.repository.JpaProductRepository;
import com.datashop.repository.JpaPromoRepository;
import com.datashop.repository.JpaUserRepository;
import com.datashop.repository.OrderRepository;
import com.datashop.repository.ProductRepository;
import com.datashop.repository.PromoRepository;
import com.datashop.repository.UserRepository;
import com.datashop.security.AuthCookies;
import com.datashop.security.CsrfTokens;
import com.datashop.security.DownloadSigner;
import com.datashop.security.JwtTokenService;
import com.datashop.security.PasswordHasher;
import com.datashop.security.RateLimiter;
import com.datashop.security.RequestAuth;
import com.datashop.security.SecurityHeaders;
import com.datashop.service.AuthService;
import com.datashop.service.CartService;
import com.datashop.service.CatalogCache;
import com.datashop.service.CloudinaryMediaStorage;
import com.datashop.service.DownloadService;
import com.datashop.service.InMemoryCatalogCache;
import com.datashop.service.LocalMediaStorage;
import com.datashop.service.MediaService;
import com.datashop.service.MediaStorage;
import com.datashop.service.OrderService;
import com.datashop.service.ProductService;
import com.datashop.service.RedisCatalogCache;
import com.datashop.service.ResilientCatalogCache;
import com.datashop.service.SeedService;
import com.datashop.web.ErrorHandler;
import com.datashop.web.MediaController;
import com.datashop.web.api.AdminMediaApiController;
import com.datashop.web.api.AuthApiController;
import com.datashop.web.api.CartApiController;
import com.datashop.web.api.DownloadApiController;
import com.datashop.web.api.HealthApiController;
import com.datashop.web.api.OrderApiController;
import com.datashop.web.api.ProductApiController;
import com.datashop.web.api.SwaggerApiController;
import com.datashop.web.page.AboutPageController;
import com.datashop.web.page.AdminPageController;
import com.datashop.web.page.AuthPageController;
import com.datashop.web.page.CartPageController;
import com.datashop.web.page.CatalogPageController;
import com.datashop.web.page.OrdersPageController;
import com.datashop.web.page.ProductPageController;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.javalin.Javalin;
import io.javalin.config.SizeUnit;
import io.javalin.http.staticfiles.Location;
import io.javalin.json.JavalinJackson;
import io.javalin.rendering.template.JavalinThymeleaf;
import lombok.extern.slf4j.Slf4j;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

@Slf4j
public class Application {

    public static void main(String[] args) {
        AppConfig config = AppConfig.load();
        Jpa jpa = new Jpa(config.getDatabase());
        CatalogCache cache = createCache(config);
        try {
            new SchemaInitializer(jpa).initialize();
            Services services = wire(config, jpa, cache);
            services.seedService().seed();

            Javalin app = createApp(config, jpa, cache, services);
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                log.info("Stopping DataShop");
                app.stop();
                cache.close();
                jpa.close();
            }, "datashop-shutdown"));

            app.start("0.0.0.0", config.getPort());
            log.info("DataShop is listening on http://0.0.0.0:{}", config.getPort());
        } catch (RuntimeException exception) {
            cache.close();
            jpa.close();
            log.error("Cannot start DataShop", exception);
            throw exception;
        }
    }

    public static Javalin createApp(
            AppConfig config,
            Jpa jpa,
            CatalogCache cache,
            Services services
    ) {
        ObjectMapper mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        TemplateEngine templateEngine = templateEngine();
        Javalin app = Javalin.create(javalinConfig -> {
            javalinConfig.showJavalinBanner = false;
            javalinConfig.http.defaultContentType = "text/html; charset=utf-8";
            javalinConfig.http.maxRequestSize = config.getDatasetMaxBytes() + 2L * 1024 * 1024;
            javalinConfig.jetty.multipartConfig.cacheDirectory(
                    java.nio.file.Path.of(config.getUploadsDirectory(), ".tmp").toString());
            javalinConfig.jetty.multipartConfig.maxFileSize(
                    config.getDatasetMaxBytes() + 1024 * 1024, SizeUnit.BYTES);
            javalinConfig.jetty.multipartConfig.maxTotalRequestSize(
                    config.getDatasetMaxBytes() + 2L * 1024 * 1024, SizeUnit.BYTES);
            javalinConfig.jetty.multipartConfig.maxInMemoryFileSize(2, SizeUnit.MB);

            javalinConfig.fileRenderer(new JavalinThymeleaf(templateEngine));
            javalinConfig.jsonMapper(new JavalinJackson(mapper, false));
            javalinConfig.staticFiles.add(staticFiles -> {
                staticFiles.hostedPath = "/static";
                staticFiles.directory = "/static";
                staticFiles.location = Location.CLASSPATH;
            });
            javalinConfig.staticFiles.add(staticFiles -> {
                staticFiles.hostedPath = "/webjars";
                staticFiles.directory = "/META-INF/resources/webjars";
                staticFiles.location = Location.CLASSPATH;
            });

            if (!config.getCorsOrigins().isEmpty()) {
                javalinConfig.bundledPlugins.enableCors(cors -> cors.addRule(rule -> {
                    rule.allowCredentials = true;
                    config.getCorsOrigins().forEach(origin -> rule.allowHost(origin));
                    rule.exposeHeader("X-Cache");
                }));
            }
        });

        app.after(new SecurityHeaders(config)::apply);
        ErrorHandler.register(app);

        new HealthApiController(jpa).register(app);
        new SwaggerApiController().register(app);
        new AuthApiController(
                services.authService(),
                services.jwtTokenService(),
                services.authCookies(),
                services.requestAuth(),
                services.rateLimiter()
        ).register(app);
        new ProductApiController(services.productService(), services.requestAuth()).register(app);
        new CartApiController(services.cartService(), services.requestAuth()).register(app);
        new OrderApiController(services.orderService(), services.requestAuth(), services.rateLimiter())
                .register(app);
        new DownloadApiController(services.downloadService(), services.requestAuth()).register(app);
        new AdminMediaApiController(services.mediaService(), services.requestAuth()).register(app);
        new MediaController(services.mediaService()).register(app);

        new CatalogPageController(
                services.productService(), services.cartService(), services.requestAuth(),
                services.csrfTokens()
        ).register(app);
        new ProductPageController(
                services.productService(), services.cartService(), services.orderService(),
                services.downloadService(), services.requestAuth(), services.csrfTokens()
        ).register(app);
        new CartPageController(
                services.cartService(), services.orderService(), services.requestAuth(),
                services.csrfTokens()
        ).register(app);
        new OrdersPageController(
                services.orderService(), services.cartService(), services.requestAuth(),
                services.csrfTokens()
        ).register(app);
        new AuthPageController(
                services.authService(), services.jwtTokenService(), services.authCookies(),
                services.requestAuth(), services.cartService(), services.csrfTokens(),
                services.rateLimiter()
        ).register(app);
        new AdminPageController(
                services.productService(), services.mediaService(), services.cartService(),
                services.requestAuth(), services.csrfTokens()
        ).register(app);
        new AboutPageController(services.cartService(), services.requestAuth(), services.csrfTokens())
                .register(app);

        return app;
    }

    public static Services wire(AppConfig config, Jpa jpa, CatalogCache cache) {
        UserRepository userRepository = new JpaUserRepository();
        ProductRepository productRepository = new JpaProductRepository();
        PromoRepository promoRepository = new JpaPromoRepository();
        CartRepository cartRepository = new JpaCartRepository();
        OrderRepository orderRepository = new JpaOrderRepository();

        PasswordHasher passwordHasher = new PasswordHasher();
        JwtTokenService jwtTokenService = new JwtTokenService(
                config.getJwtSecret(), config.getJwtTtl());
        AuthCookies authCookies = new AuthCookies(config, config.getJwtTtl());
        RequestAuth requestAuth = new RequestAuth(jwtTokenService, authCookies);
        CsrfTokens csrfTokens = new CsrfTokens(config);
        RateLimiter rateLimiter = new RateLimiter();

        MediaStorage mediaStorage = config.getCloudinaryUrl().isBlank()
                ? new LocalMediaStorage(config)
                : new CloudinaryMediaStorage(config.getCloudinaryUrl());
        AuthService authService = new AuthService(jpa, userRepository, cartRepository, passwordHasher);
        ProductService productService = new ProductService(jpa, productRepository, cache, config);
        CartService cartService = new CartService(
                jpa, cartRepository, productRepository, promoRepository, userRepository);
        OrderService orderService = new OrderService(
                jpa, cartRepository, orderRepository, productRepository, cache);
        DownloadService downloadService = new DownloadService(
                productService, orderService, mediaStorage,
                new DownloadSigner(config.getJwtSecret()), config);
        MediaService mediaService = new MediaService(mediaStorage, productService, config);
        SeedService seedService = new SeedService(
                jpa, userRepository, productRepository, promoRepository,
                passwordHasher, mediaStorage, config);

        return new Services(
                authService, productService, cartService, orderService, downloadService,
                mediaService, seedService, jwtTokenService, authCookies, requestAuth, csrfTokens,
                rateLimiter
        );
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
                log.warn("Redis is unavailable, using in-memory catalog cache: {}", exception.getMessage());
            }
        }
        return new ResilientCatalogCache(new InMemoryCatalogCache());
    }

    private static TemplateEngine templateEngine() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCharacterEncoding("UTF-8");
        resolver.setCacheable(true);

        TemplateEngine engine = new TemplateEngine();
        engine.setTemplateResolver(resolver);
        return engine;
    }

    public record Services(
            AuthService authService,
            ProductService productService,
            CartService cartService,
            OrderService orderService,
            DownloadService downloadService,
            MediaService mediaService,
            SeedService seedService,
            JwtTokenService jwtTokenService,
            AuthCookies authCookies,
            RequestAuth requestAuth,
            CsrfTokens csrfTokens,
            RateLimiter rateLimiter
    ) {
    }
}
