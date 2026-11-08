package com.datashop.service;

import com.datashop.config.AppConfig;
import com.datashop.db.Jpa;
import com.datashop.domain.Product;
import com.datashop.domain.PromoCode;
import com.datashop.domain.User;
import com.datashop.domain.UserRole;
import com.datashop.repository.ProductRepository;
import com.datashop.repository.PromoRepository;
import com.datashop.repository.UserRepository;
import com.datashop.security.PasswordHasher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class SeedService {

    private final Jpa jpa;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final PromoRepository promoRepository;
    private final PasswordHasher passwordHasher;
    private final MediaStorage mediaStorage;
    private final AppConfig config;

    public void seed() {
        if (!config.isSeedEnabled()) {
            log.info("Seeding disabled");
            return;
        }
        String adminHash = passwordHasher.hash(config.getSeedAdminPassword());
        int[] created = jpa.write(entityManager -> {
            int products = 0;

            if (userRepository.findByEmail(entityManager, config.getSeedAdminEmail()).isEmpty()) {
                User admin = new User();
                admin.setEmail(config.getSeedAdminEmail());
                admin.setPasswordHash(adminHash);
                admin.setRole(UserRole.ADMIN);
                userRepository.save(entityManager, admin);
                log.info("Seeded admin account {}", config.getSeedAdminEmail());
            }

            if (promoRepository.findByCode(entityManager, "SAVE10").isEmpty()) {
                PromoCode promo = new PromoCode();
                promo.setCode("SAVE10");
                promo.setDiscountPercentage(10);
                promo.setActive(true);
                promoRepository.save(entityManager, promo);
                log.info("Seeded promo code SAVE10");
            }

            Long existing = entityManager.createQuery("select count(p) from Product p", Long.class)
                    .getSingleResult();
            if (existing == null || existing == 0) {
                for (int index = 0; index < DEMO_PRODUCTS.size(); index++) {
                    DemoProduct demo = DEMO_PRODUCTS.get(index);
                    Product product = new Product();
                    product.setTitle(demo.title());
                    product.setDescription(demo.description());
                    product.setPrice(BigDecimal.valueOf(demo.price()));
                    product.setRating(BigDecimal.valueOf(demo.rating()));
                    product.setStockQuantity(demo.stockQuantity());
                    product.setImages(List.of(
                            datasetImage(demo.label(), demo.color(), null),
                            datasetImage(demo.label(), demo.color(), "PREVIEW 1"),
                            datasetImage(demo.label(), demo.color(), "PREVIEW 2")
                    ));
                    product.setFilePublicId(storeDemoDataset(demo, index + 1));
                    productRepository.save(entityManager, product);
                    products++;
                }
                log.info("Seeded {} demo products", products);
            }

            return new int[]{products};
        });
        if (created[0] == 0) {
            log.info("Seed data already present");
        }
    }

    private String storeDemoDataset(DemoProduct demo, int index) {
        String csv = "title,price,rating,stock_quantity\n\""
                + demo.title().replace("\"", "\"\"") + "\","
                + demo.price() + ","
                + demo.rating() + ","
                + demo.stockQuantity() + "\n";
        try {
            MediaStorage.StoredMedia stored = mediaStorage.store(
                    new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8)),
                    "demo-" + index + ".csv",
                    "text/csv",
                    "datasets"
            );
            return stored.publicId();
        } catch (RuntimeException exception) {
            throw new IllegalStateException("Cannot seed dataset file for " + demo.title(), exception);
        }
    }

    private String datasetImage(String label, String color, String variant) {
        String text = variant == null ? label : label + "\n" + variant;
        return "https://placehold.co/800x600/" + color + "/ffffff/png?text="
                + URLEncoder.encode(text, StandardCharsets.UTF_8)
                + "&font=montserrat";
    }

    private record DemoProduct(
            String title,
            String description,
            double price,
            double rating,
            int stockQuantity,
            String label,
            String color
    ) {
    }

    private static final List<DemoProduct> DEMO_PRODUCTS = List.of(
            new DemoProduct("Акции Tesla 2019–2024",
                    "Дневные котировки TSLA с объёмами и дивидендами.", 1499.0, 4.8, 25, "TSLA", "1e293b"),
            new DemoProduct("Крипто-пары BTC/ETH 1 минута",
                    "Минутные свечи биржи за три года, 12 инструментов.", 2499.0, 4.9, 15, "BTC / ETH", "6d28d9"),
            new DemoProduct("Рынок недвижимости США",
                    "Цены, ставки и объёмы сделок по округам за 10 лет.", 3990.0, 4.5, 10, "REAL ESTATE", "0f766e"),
            new DemoProduct("Логи веб-сервера Nginx",
                    "Размеченные access-логи за месяц, 5 млн строк.", 899.0, 4.2, 40, "NGINX LOGS", "9a3412"),
            new DemoProduct("Отзывы Amazon (электроника)",
                    "Обезличенные отзывы и оценки, 500k записей.", 1290.0, 4.4, 30, "AMAZON REVIEWS", "b45309"),
            new DemoProduct("Погода по 100 городам",
                    "Почасовые наблюдения: температура, давление, ветер.", 690.0, 4.1, 50, "WEATHER", "0369a1"),
            new DemoProduct("Авиарейсы и задержки",
                    "Расписание и фактические времена вылета за 2024.", 2190.0, 4.6, 12, "FLIGHTS", "1d4ed8"),
            new DemoProduct("Вакансии IT (HeadHunter)",
                    "Названия, грейды и вилки зарплат, 300k вакансий.", 1990.0, 4.7, 18, "IT VACANCIES", "4338ca"),
            new DemoProduct("Медицинские снимки (демо)",
                    "Анонимизированный набор рентген-снимков с разметкой.", 4990.0, 4.9, 6, "MEDICAL IMAGING", "9f1239"),
            new DemoProduct("Транзакции ритейла",
                    "Чеки супермаркета: товары, суммы, время покупки.", 1590.0, 4.3, 22, "RETAIL", "15803d"),
            new DemoProduct("Курсы валют ЦБ",
                    "Ежедневные курсы 30 валют за 15 лет.", 490.0, 4.0, 60, "FX RATES", "7c2d12"),
            new DemoProduct("Соцсеть: граф друзей",
                    "Обезличенный граф связей, 1 млн рёбер.", 2790.0, 4.5, 9, "SOCIAL GRAPH", "5b21b6"),
            new DemoProduct("Музыкальные треки и жанры",
                    "Аудио-фичи и жанровая разметка 200k треков.", 1890.0, 4.6, 14, "MUSIC", "a21caf"),
            new DemoProduct("Дорожный трафик (датчики)",
                    "Интенсивность потока по 500 датчикам за год.", 3290.0, 4.4, 8, "TRAFFIC", "b91c1c"),
            new DemoProduct("Футбольная статистика",
                    "Матчи, голы и xG по 20 лигам за 10 сезонов.", 2590.0, 4.8, 11, "FOOTBALL", "166534"),
            new DemoProduct("Энергопотребление домохозяйств",
                    "Почасовые показания счётчиков, 200 домов.", 1190.0, 4.2, 20, "ENERGY", "ca3500"),
            new DemoProduct("Опросы общественного мнения",
                    "Анонимизированные ответы 50k респондентов.", 990.0, 4.1, 35, "SURVEYS", "0e7490"),
            new DemoProduct("Логи приложений (APM)",
                    "Трассировки и ошибки распределённой системы.", 3490.0, 4.7, 7, "APM LOGS", "334155")
    );
}
