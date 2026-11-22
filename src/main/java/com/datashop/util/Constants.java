package com.datashop.util;

import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Set;

@UtilityClass
public final class Constants {

    public static final class Http {
        public static final String API_PREFIX = "/api/";
        public static final String DOWNLOADS_PREFIX = "/downloads/";
        public static final String AUTHORIZATION_HEADER = "Authorization";
        public static final String BEARER_PREFIX = "Bearer ";
        public static final String CACHE_HEADER = "X-Cache";
        public static final String CACHE_CONTROL = "Cache-Control";
        public static final String CACHE_HIT = "HIT";
        public static final String CACHE_MISS = "MISS";
        public static final String CONTENT_TYPE_HTML_UTF8 = "text/html; charset=utf-8";
        public static final String CONTENT_TYPE_TEXT_UTF8 = "text/plain; charset=utf-8";

        public static final class Status {
            public static final int OK = 200;
            public static final int CREATED = 201;
            public static final int NO_CONTENT = 204;
            public static final int BAD_REQUEST = 400;
            public static final int UNAUTHORIZED = 401;
            public static final int FORBIDDEN = 403;
            public static final int NOT_FOUND = 404;
            public static final int METHOD_NOT_ALLOWED = 405;
            public static final int CONFLICT = 409;
            public static final int PAYLOAD_TOO_LARGE = 413;
            public static final int TOO_MANY_REQUESTS = 429;
            public static final int INTERNAL_SERVER_ERROR = 500;
            public static final int SERVICE_UNAVAILABLE = 503;
        }
    }

    public static final class App {
        public static final String HOST = "0.0.0.0";
        public static final String SHUTDOWN_HOOK_NAME = "datashop-shutdown";
        public static final String DEFAULT_CONTENT_TYPE = Http.CONTENT_TYPE_HTML_UTF8;
        public static final String TEMPLATES_PREFIX = "templates/";
        public static final String TEMPLATE_SUFFIX = ".html";
        public static final String TEMPLATE_ENCODING = "UTF-8";
        public static final String STATIC_HOSTED_PATH = "/static";
        public static final String STATIC_DIRECTORY = "/static";
        public static final long BYTES_PER_MB = 1024L * 1024L;
        public static final long MULTIPART_EXTRA_BYTES = 2L * BYTES_PER_MB;
        public static final long MULTIPART_FILE_EXTRA_BYTES = 1L * BYTES_PER_MB;
        public static final int MULTIPART_MAX_IN_MEMORY_MB = 2;
    }

    public static final class Config {
        public static final int DEFAULT_PORT = 3000;
        public static final String DEFAULT_CORS_ORIGIN = "http://localhost:5173";
        public static final String CORS_ORIGINS_SEPARATOR = ",";
        public static final String DEFAULT_JWT_SECRET = "change-me-datashop-development-secret";
        public static final String DEFAULT_JWT_TTL = "1h";
        public static final Duration DEFAULT_JWT_TTL_DURATION = Duration.ofHours(1);
        public static final String DEFAULT_REDIS_URL = "redis://localhost:6379";
        public static final int DEFAULT_CACHE_TTL_SECONDS = 90;
        public static final long DEFAULT_PREVIEW_MAX_BYTES = 5L * App.BYTES_PER_MB;
        public static final long DEFAULT_DATASET_MAX_BYTES = 50L * App.BYTES_PER_MB;
        public static final String DEFAULT_SEED_ADMIN_EMAIL = "admin@datashop.local";
        public static final String DEFAULT_SEED_ADMIN_PASSWORD = "Admin12345!";
        public static final boolean DEFAULT_SEED_ENABLED = true;
        public static final String ENV_FILE = ".env";
        public static final String ENV_EXPORT_PREFIX = "export ";
        public static final String ENV_COMMENT_PREFIX = "#";
        public static final char ENV_ASSIGNMENT = '=';
        public static final String DURATION_ISO_PREFIX = "p";
        public static final String DURATION_SUFFIXES = "s, m, h or d";
    }

    public static final class EnvKeys {
        public static final String PORT = "PORT";
        public static final String RENDER_EXTERNAL_URL = "RENDER_EXTERNAL_URL";
        public static final String CORS_ORIGINS = "CORS_ORIGINS";
        public static final String JWT_SECRET = "JWT_SECRET";
        public static final String JWT_EXPIRES_IN = "JWT_EXPIRES_IN";
        public static final String COOKIE_SECURE = "COOKIE_SECURE";
        public static final String REDIS_URL = "REDIS_URL";
        public static final String SUPABASE_URL = "SUPABASE_URL";
        public static final String SUPABASE_PUBLISHABLE_KEY = "SUPABASE_PUBLISHABLE_KEY";
        public static final String SUPABASE_SERVICE_ROLE_KEY = "SUPABASE_SERVICE_ROLE_KEY";
        public static final String SUPABASE_STORAGE_EMAIL = "SUPABASE_STORAGE_EMAIL";
        public static final String SUPABASE_STORAGE_PASSWORD = "SUPABASE_STORAGE_PASSWORD";
        public static final String CACHE_TTL_SECONDS = "CACHE_TTL_SECONDS";
        public static final String PREVIEW_MAX_BYTES = "PREVIEW_MAX_BYTES";
        public static final String DATASET_MAX_BYTES = "DATASET_MAX_BYTES";
        public static final String SEED_ADMIN_EMAIL = "SEED_ADMIN_EMAIL";
        public static final String SEED_ADMIN_PASSWORD = "SEED_ADMIN_PASSWORD";
        public static final String SEED_ENABLED = "SEED_ENABLED";
        public static final String DATABASE_URL = "DATABASE_URL";
        public static final String DB_POOL_SIZE = "DB_POOL_SIZE";
    }

    public static final class Supabase {
        public static final String HOST_MARKER = ".supabase.";
    }

    public static final class Database {
        public static final int DEFAULT_POOL_SIZE = 10;
        public static final int DEFAULT_PORT_NUMBER = 5432;
        public static final String JDBC_URL_PREFIX = "jdbc:postgresql://";
        public static final String POSTGRES_SCHEME = "postgres";
        public static final String POSTGRESQL_SCHEME = "postgresql";
        public static final String USER_INFO_SEPARATOR = ":";
        public static final int USER_INFO_LIMIT = 2;
        public static final String POOL_NAME = "datashop-pool";
        public static final String TIME_ZONE = "UTC";
        public static final String HEALTH_QUERY = "select 1";
        public static final String SCHEME_ERROR =
                "DATABASE_URL must use the postgres:// or postgresql:// scheme";
        public static final String URL_REQUIRED =
                "DATABASE_URL is required: use the remote Supabase connection string";
        public static final String URL_INVALID =
                "DATABASE_URL must include a host, database path and credentials";
        public static final String SUPABASE_ONLY =
                "DATABASE_URL must point to Supabase; local databases are not supported";
        public static final String CREDENTIALS_REQUIRED =
                "DATABASE_URL must include the Supabase database user and password";
    }

    public static final class Security {
        public static final String AUTH_COOKIE = "datashop_token";
        public static final String CSRF_COOKIE = "datashop_csrf";
        public static final String CSRF_FORM_FIELD = "_csrf";
        public static final String CSRF_HEADER = "X-CSRF-Token";
        public static final String CSRF_ATTRIBUTE = "datashop.csrf";
        public static final String AUTH_ATTRIBUTE = "datashop.auth.user";
        public static final String COOKIE_PATH = "/";
        public static final int CSRF_TOKEN_BYTES = 32;
        public static final int COOKIE_MAX_AGE_ZERO = 0;
        public static final String JWT_EMAIL_CLAIM = "email";
        public static final String JWT_ROLE_CLAIM = "role";
        public static final int JWT_MIN_SECRET_LENGTH = 32;
        public static final String SHA_256 = "SHA-256";
        public static final String SHA_256_UNAVAILABLE = "SHA-256 is not available";
        public static final int ARGON2_ITERATIONS = 3;
        public static final int ARGON2_MEMORY_KB = 65536;
        public static final int ARGON2_PARALLELISM = 1;
        public static final int RATE_LIMIT_CLEANUP_INTERVAL = 512;

        public static final String HEADER_CONTENT_TYPE_OPTIONS = "X-Content-Type-Options";
        public static final String HEADER_FRAME_OPTIONS = "X-Frame-Options";
        public static final String HEADER_REFERRER_POLICY = "Referrer-Policy";
        public static final String HEADER_PERMISSIONS_POLICY = "Permissions-Policy";
        public static final String HEADER_CONTENT_SECURITY_POLICY = "Content-Security-Policy";
        public static final String HEADER_STRICT_TRANSPORT_SECURITY = "Strict-Transport-Security";
        public static final String HEADER_CACHE_CONTROL = "Cache-Control";
        public static final String VALUE_NOSNIFF = "nosniff";
        public static final String VALUE_DENY = "DENY";
        public static final String VALUE_STRICT_ORIGIN = "strict-origin-when-cross-origin";
        public static final String VALUE_PERMISSIONS_POLICY = "camera=(), microphone=(), geolocation=()";
        public static final String VALUE_HSTS = "max-age=31536000; includeSubDomains";
        public static final String VALUE_NO_STORE = "no-store";
        public static final String CSP = String.join("; ",
                "default-src 'self'",
                "img-src 'self' https: data:",
                "style-src 'self' 'unsafe-inline'",
                "script-src 'self'",
                "font-src 'self' data:",
                "connect-src 'self'",
                "frame-ancestors 'none'",
                "base-uri 'self'",
                "form-action 'self'"
        );
        // Swagger UI initialises itself with an inline script, so its page needs a relaxed policy.
        public static final String CSP_DOCS = CSP.replace(
                "script-src 'self'",
                "script-src 'self' 'unsafe-inline'"
        );
    }

    public static final class RateLimit {
        public static final Duration WINDOW = Duration.ofMinutes(1);
        public static final int API_LOGIN = 5;
        public static final int API_REGISTER = 5;
        public static final int PAGE_LOGIN = 20;
        public static final int PAGE_REGISTER = 10;
        public static final int CHECKOUT = 10;
        public static final String ACTION_LOGIN = "login";
        public static final String ACTION_REGISTER = "register";
        public static final String ACTION_PAGE_LOGIN = "page-login";
        public static final String ACTION_PAGE_REGISTER = "page-register";
        public static final String ACTION_CHECKOUT = "checkout";
    }

    public static final class Validation {
        public static final String EMAIL_PATTERN = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$";
        public static final int EMAIL_MIN_LENGTH = 3;
        public static final int EMAIL_MAX_LENGTH = 255;
        public static final int PASSWORD_MIN_LENGTH = 8;
        public static final int PASSWORD_MAX_LENGTH = 72;
        public static final int TITLE_MIN_LENGTH = 3;
        public static final int TITLE_MAX_LENGTH = 255;
        public static final int DESCRIPTION_MIN_LENGTH = 1;
        public static final int IMAGES_MIN_COUNT = 3;
        public static final String IMAGES_SEPARATOR = ",";
        public static final int PUBLIC_ID_MAX_LENGTH = 255;
        public static final int PUBLIC_ID_MIN_LENGTH = 1;
        public static final int SEARCH_MAX_LENGTH = 100;
        public static final BigDecimal MIN_PRICE = new BigDecimal("0.01");
        public static final BigDecimal MAX_PRICE = new BigDecimal("99999999.99");
        public static final BigDecimal MIN_RATING = BigDecimal.ZERO;
        public static final BigDecimal MAX_RATING = new BigDecimal("5.0");
        public static final int MIN_STOCK = 0;
        public static final int MAX_STOCK = Integer.MAX_VALUE;
        public static final int MIN_CART_QUANTITY = 1;
        public static final int MAX_CART_QUANTITY = 99;
        public static final int PROMO_CODE_MAX_LENGTH = 64;
        public static final int DEFAULT_PAGE = 1;
        public static final int MIN_PAGE = 1;
        public static final int MAX_PAGE = 100_000;
        public static final int DEFAULT_PAGE_LIMIT = 12;
        public static final int MIN_PAGE_LIMIT = 1;
        public static final int MAX_PAGE_LIMIT = 100;
        public static final int RELATED_PRODUCTS_LIMIT = 6;
        public static final int ADMIN_PRODUCTS_LIMIT = 100;
        public static final String HTTP_SCHEME = "http";
        public static final String HTTPS_SCHEME = "https";
    }

    public static final class Cart {
        public static final int CURRENCY_SCALE = 2;
        public static final int DIVISION_SCALE = 4;
        public static final int MIN_DISCOUNT_PERCENT = 0;
        public static final int MAX_DISCOUNT_PERCENT = 100;
        public static final int PERCENT_BASE = 100;
    }

    public static final class Cache {
        public static final String PREFIX = "catalog:";
        public static final String PRODUCTS_PREFIX = PREFIX + "products:";
        public static final String INVALIDATION_PATTERN = PREFIX + "*";
        public static final String HASH_ALGORITHM = "SHA-1";
        public static final String KEY_SEPARATOR = "|";
        public static final int MIN_TTL_SECONDS = 1;
        public static final String SHA1_UNAVAILABLE_MESSAGE = "SHA-1 is not available";
    }

    public static final class Media {
        public static final String DATASETS_FOLDER = "datasets";
        public static final String DATASETS_PREFIX = DATASETS_FOLDER + "/";
        public static final String SUPABASE_DATASETS_BUCKET = "datasets";
        public static final String DEFAULT_FILENAME = "file";
        public static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";
        public static final int MAX_FILENAME_LENGTH = 80;
        public static final Duration DOWNLOAD_LINK_TTL = Duration.ofSeconds(60);
        public static final int PREVIEWS_PER_PRODUCT = 3;
        public static final int PREVIEW_BOOTSTRAP_LIMIT = 100;
        public static final String SVG_CONTENT_TYPE = "image/svg+xml";
        public static final String ASSET_PATH_PREFIX = "/media/assets/";
        public static final String ASSET_PATTERN = "%" + ASSET_PATH_PREFIX + "%";
        public static final String ASSET_CACHE_CONTROL = "public, max-age=31536000, immutable";
        public static final Set<String> PREVIEW_TYPES = Set.of(
                "image/png",
                "image/jpeg",
                "image/webp"
        );
        public static final Set<String> DATASET_TYPES = Set.of(
                "application/zip",
                "application/x-zip-compressed",
                "application/json",
                "text/csv",
                "text/plain",
                "application/gzip",
                "application/x-gzip",
                "application/octet-stream"
        );
    }

    public static final class Routes {
        public static final String HEALTH = "/api/health";
        public static final String HEALTH_READY = "/api/health/ready";
        public static final String DOCS = "/api/docs";
        public static final String DOCS_JSON = "/api/docs-json";
        public static final String OPENAPI = "/api/openapi.json";

        public static final String API_AUTH_REGISTER = "/api/auth/register";
        public static final String API_AUTH_LOGIN = "/api/auth/login";
        public static final String API_AUTH_ME = "/api/auth/me";
        public static final String API_AUTH_ADMIN_CHECK = "/api/auth/admin-check";
        public static final String API_AUTH_LOGOUT = "/api/auth/logout";

        public static final String API_PRODUCTS = "/api/products";
        public static final String API_PRODUCT_BY_ID = "/api/products/{id}";

        public static final String API_CART = "/api/cart";
        public static final String API_CART_ITEMS = "/api/cart/items";
        public static final String API_CART_ITEM_BY_ID = "/api/cart/items/{id}";
        public static final String API_CART_PROMO = "/api/cart/apply-promo";

        public static final String API_ORDERS = "/api/orders";
        public static final String API_ORDER_BY_ID = "/api/orders/{id}";
        public static final String API_CHECKOUT = "/api/orders/checkout";

        public static final String API_DOWNLOAD_BY_PRODUCT_ID = "/api/downloads/{productId}";
        public static final String API_ADMIN_MEDIA_PREVIEW = "/api/admin/media/preview";
        public static final String API_ADMIN_MEDIA_DATASET = "/api/admin/media/dataset/{productId}";
        public static final String MEDIA_ASSET = "/media/assets/{id}";

        public static final String HOME = "/";
        public static final String ABOUT = "/about";
        public static final String ADMIN = "/admin";
        public static final String ADMIN_PRODUCTS = "/admin/products";
        public static final String ADMIN_PRODUCT_DELETE = "/admin/products/{id}/delete";
        public static final String ADMIN_MEDIA_PREVIEW = "/admin/media/preview";
        public static final String ADMIN_MEDIA_DATASET = "/admin/media/dataset/{productId}";
        public static final String ADMIN_MEDIA_DATASET_FORM = "/admin/media/dataset";
        public static final String LOGIN = "/login";
        public static final String REGISTER = "/register";
        public static final String LOGOUT = "/logout";
        public static final String CART = "/cart";
        public static final String CART_ITEMS = "/cart/items";
        public static final String CART_ITEM_UPDATE = "/cart/items/{id}/update";
        public static final String CART_ITEM_DELETE = "/cart/items/{id}/delete";
        public static final String CART_PROMO = "/cart/promo";
        public static final String CART_PROMO_REMOVE = "/cart/promo/remove";
        public static final String CHECKOUT = "/orders/checkout";
        public static final String ORDERS = "/orders";
        public static final String PRODUCT_BY_ID = "/products/{id}";
        public static final String DOWNLOAD_BY_PRODUCT_ID = "/downloads/{productId}";
    }

    public static final class Redirects {
        public static final String ADMIN_CREATED = "/admin?created=1";
        public static final String ADMIN_DELETED = "/admin?deleted=1";
        public static final String ADMIN_PREVIEW_UPLOADED = "/admin?uploaded=preview";
        public static final String ADMIN_DATASET_UPLOADED = "/admin?uploaded=dataset";
        public static final String ORDERS_CHECKOUT = "/orders?checkout=1";
        public static final String LOGIN_NEXT_PREFIX = "/login?next=";
    }

    public static final class Templates {
        public static final String ABOUT = "about";
        public static final String ADMIN = "admin";
        public static final String LOGIN = "login";
        public static final String REGISTER = "register";
        public static final String CART = "cart";
        public static final String CATALOG = "catalog";
        public static final String ORDERS = "orders";
        public static final String PRODUCT = "product";
        public static final String ERROR = "error";
    }

    public static final class Attributes {
        public static final String USER = "user";
        public static final String ACTIVE = "active";
        public static final String CART_COUNT = "cartCount";
        public static final String CSRF_TOKEN = "csrfToken";
        public static final String PRODUCTS = "products";
        public static final String META = "meta";
        public static final String QUERY = "query";
        public static final String ERROR = "error";
        public static final String CREATED = "created";
        public static final String DELETED = "deleted";
        public static final String UPLOADED = "uploaded";
        public static final String PREVIEW_URL = "previewUrl";
        public static final String NEXT = "next";
        public static final String EMAIL = "email";
        public static final String CART = "cart";
        public static final String ORDERS = "orders";
        public static final String CHECKOUT = "checkout";
        public static final String PRODUCT = "product";
        public static final String RELATED = "related";
        public static final String OWNED = "owned";
        public static final String IN_CART = "inCart";
        public static final String STATUS = "status";
        public static final String TITLE = "title";
        public static final String MESSAGE = "message";
    }

    public static final class Params {
        public static final String ID = "id";
        public static final String PRODUCT_ID = "productId";
        public static final String FILE = "file";
        public static final String QUANTITY = "quantity";
        public static final String NEXT = "next";
        public static final String CODE = "code";
        public static final String EMAIL = "email";
        public static final String PASSWORD = "password";
        public static final String SEARCH = "search";
        public static final String MIN_PRICE = "min_price";
        public static final String MAX_PRICE = "max_price";
        public static final String MIN_RATING = "min_rating";
        public static final String SORT_BY = "sort_by";
        public static final String ORDER = "order";
        public static final String PAGE = "page";
        public static final String LIMIT = "limit";
        public static final String TITLE = "title";
        public static final String DESCRIPTION = "description";
        public static final String PRICE = "price";
        public static final String RATING = "rating";
        public static final String IMAGES = "images";
        public static final String STOCK_QUANTITY = "stockQuantity";
        public static final String FILE_PUBLIC_ID = "filePublicId";
        public static final String CREATED = "created";
        public static final String DELETED = "deleted";
        public static final String UPLOADED = "uploaded";
        public static final String PREVIEW_URL = "previewUrl";
        public static final String CHECKOUT = "checkout";
        public static final String VALUE = "value";
    }

    public static final class Health {
        public static final String OK = "ok";
        public static final String READY = "ready";
        public static final String UNAVAILABLE = "unavailable";
        public static final String UP = "up";
        public static final String DOWN = "down";
        public static final long MILLIS_PER_SECOND = 1000;
    }

    public static final class Swagger {
        public static final String TITLE = "DataShop API";
        public static final String DESCRIPTION = "REST API маркетплейса датасетов: каталог, корзина, "
                + "промокоды, заказы, медиа в Supabase и защищённое скачивание.";
        public static final String VERSION = "1.0.0";
        public static final String UI_TITLE = "DataShop API — Swagger UI";
        public static final String SERVER_URL = "/";
        public static final String SERVER_DESCRIPTION = "Текущий хост";
        public static final String BEARER_AUTH = "bearerAuth";
        public static final String BEARER_FORMAT = "JWT";
        public static final String BEARER_AUTH_DESCRIPTION =
                "JWT из POST /api/auth/login или POST /api/auth/register.";

        public static final String TAG_AUTH = "Auth";
        public static final String TAG_PRODUCTS = "Products";
        public static final String TAG_CART = "Cart";
        public static final String TAG_ORDERS = "Orders";
        public static final String TAG_MEDIA = "Media";
        public static final String TAG_DOWNLOADS = "Downloads";
        public static final String TAG_HEALTH = "Health";

        public static final String TAG_AUTH_DESCRIPTION = "Регистрация, вход и профиль";
        public static final String TAG_PRODUCTS_DESCRIPTION = "Каталог и управление товарами";
        public static final String TAG_CART_DESCRIPTION = "Корзина и промокоды";
        public static final String TAG_ORDERS_DESCRIPTION = "Оформление и история заказов";
        public static final String TAG_MEDIA_DESCRIPTION =
                "Загрузка превью в Supabase PostgreSQL и датасетов в Supabase Storage";
        public static final String TAG_DOWNLOADS_DESCRIPTION = "Приватные ссылки Supabase Storage на скачивание";
        public static final String TAG_HEALTH_DESCRIPTION = "Проверки состояния";
    }

    public static final class Json {
        public static final String MESSAGE = "message";
        public static final String USER = "user";
    }

    public static final class Messages {
        public static final String AUTHENTICATION_REQUIRED = "Authentication required";
        public static final String INSUFFICIENT_PERMISSIONS = "Insufficient permissions";
        public static final String ADMIN_ACCESS_REQUIRED = "Admin access required";
        public static final String INVALID_CSRF = "Invalid CSRF token";
        public static final String TOO_MANY_REQUESTS = "Too many requests, try again later";
        public static final String ROUTE_NOT_FOUND = "Route not found";
        public static final String METHOD_NOT_ALLOWED = "Method not allowed";
        public static final String PAYLOAD_TOO_LARGE = "Payload too large";
        public static final String MALFORMED_REQUEST = "Malformed request";
        public static final String MALFORMED_REQUEST_BODY = "Malformed request body";
        public static final String INTERNAL_SERVER_ERROR = "Internal server error";
        public static final String REQUEST_BODY_REQUIRED = "Request body is required";
        public static final String PRODUCT_NOT_FOUND = "Product not found";
        public static final String IMAGE_NOT_FOUND = "Image not found";
        public static final String CART_ITEM_NOT_FOUND = "Cart item not found";
        public static final String CART_EMPTY = "Cart is empty";
        public static final String PROMO_NOT_FOUND = "Promo code not found";
        public static final String PROMO_INACTIVE = "Promo code is inactive";
        public static final String CODE_REQUIRED = "code is required";
        public static final String CODE_TOO_LONG = "code must be at most 64 characters";
        public static final String QUANTITY_REQUIRED = "quantity is required (1-99)";
        public static final String QUANTITY_RANGE = "quantity must be between 1 and 99";
        public static final String FILE_REQUIRED = "file is required";
        public static final String PREVIEW_TOO_LARGE = "Preview image is too large";
        public static final String DATASET_TOO_LARGE = "Dataset file is too large";
        public static final String UNSUPPORTED_IMAGE_TYPE = "Unsupported image type: ";
        public static final String UNSUPPORTED_DATASET_TYPE = "Unsupported dataset type: ";
        public static final String DATASET_NOT_AVAILABLE = "Dataset file is not available";
        public static final String PURCHASE_REQUIRED = "Purchase is required to download this dataset";
        public static final String ORDER_NOT_FOUND = "Order not found";
        public static final String INVALID_EMAIL_OR_PASSWORD = "Invalid email or password";
        public static final String USER_NOT_FOUND = "User no longer exists";
        public static final String EMAIL_ALREADY_REGISTERED = "Email is already registered";
        public static final String INSUFFICIENT_STOCK_PREFIX = "Insufficient stock for \"";
        public static final String SUPABASE_REQUIRED =
                "SUPABASE_URL is required: local media storage is disabled";
        public static final String SUPABASE_STORAGE_AUTH_REQUIRED =
                "Configure SUPABASE_SERVICE_ROLE_KEY or SUPABASE_PUBLISHABLE_KEY with "
                        + "SUPABASE_STORAGE_EMAIL and SUPABASE_STORAGE_PASSWORD";
        public static final String ADMIN_ACCESS_GRANTED = "admin access granted";
        public static final String IDENTIFIER_REQUIRED = "Identifier is required";
        public static final String INVALID_IDENTIFIER = "Invalid identifier: ";
        public static final String ADMIN_PANEL_ACTIVE = "admin";
        public static final String CART_ACTIVE = "cart";
        public static final String ORDERS_ACTIVE = "orders";
        public static final String LOGIN_ACTIVE = "login";
        public static final String CATALOG_ACTIVE = "catalog";
        public static final String ABOUT_ACTIVE = "about";
        public static final String TITLE_BAD_REQUEST = "Некорректный запрос";
        public static final String TITLE_UNAUTHORIZED = "Требуется вход";
        public static final String TITLE_FORBIDDEN = "Доступ запрещён";
        public static final String TITLE_NOT_FOUND = "Страница не найдена";
        public static final String TITLE_METHOD_NOT_ALLOWED = "Метод не поддерживается";
        public static final String TITLE_CONFLICT = "Конфликт данных";
        public static final String TITLE_PAYLOAD_TOO_LARGE = "Файл слишком большой";
        public static final String TITLE_TOO_MANY_REQUESTS = "Слишком много запросов";
        public static final String TITLE_INTERNAL_SERVER_ERROR = "Ошибка сервера";
        public static final String INVALID_EMAIL = "email must be a valid email address";
        public static final String PASSWORD_REQUIRED = "password is required";
        public static final String PASSWORD_REQUIRED_REGISTER = "password is required (8-72 characters)";
        public static final String PASSWORD_LENGTH = "password must be between "
                + Constants.Validation.PASSWORD_MIN_LENGTH + " and "
                + Constants.Validation.PASSWORD_MAX_LENGTH + " characters";
        public static final String IMAGES_MIN_COUNT = "images must contain at least "
                + Constants.Validation.IMAGES_MIN_COUNT + " URLs";
        public static final String IMAGES_BLANK_URL = "images must not contain blank URLs";
        public static final String IMAGES_INVALID_URL =
                "images must contain DataShop media asset URLs";
        public static final String IMAGES_NOT_FOUND =
                "images must reference existing DataShop media assets";
        public static final String FIELD_REQUIRED = "%s is required";
        public static final String FIELD_BETWEEN = "%s must be between %s and %s";
        public static final String FIELD_BETWEEN_CHARACTERS = "%s must be between %s and %s characters";
        public static final String FIELD_MIN = "%s must be at least %s";
        public static final String FIELD_MIN_CHARACTERS = "%s must be at least %s characters";
        public static final String FIELD_MAX = "%s must be at most %s";
        public static final String FIELD_MAX_CHARACTERS = "%s must be at most %s characters";
        public static final String FIELD_NUMBER = "%s must be a number";
        public static final String FIELD_INTEGER = "%s must be an integer";
        public static final String FIELD_NOT_EXCEED = "%s must not exceed %s";
        public static final String REASON_BAD_REQUEST = "Bad Request";
        public static final String REASON_UNAUTHORIZED = "Unauthorized";
        public static final String REASON_FORBIDDEN = "Forbidden";
        public static final String REASON_NOT_FOUND = "Not Found";
        public static final String REASON_METHOD_NOT_ALLOWED = "Method Not Allowed";
        public static final String REASON_CONFLICT = "Conflict";
        public static final String REASON_PAYLOAD_TOO_LARGE = "Payload Too Large";
        public static final String REASON_TOO_MANY_REQUESTS = "Too Many Requests";
        public static final String REASON_INTERNAL_SERVER_ERROR = "Internal Server Error";
    }
}
