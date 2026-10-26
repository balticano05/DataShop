package com.datashop.db;

import com.datashop.config.DatabaseSettings;
import com.datashop.domain.Cart;
import com.datashop.domain.CartItem;
import com.datashop.domain.Order;
import com.datashop.domain.OrderItem;
import com.datashop.domain.Product;
import com.datashop.domain.PromoCode;
import com.datashop.domain.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.util.function.Function;

@Slf4j
public class Jpa implements AutoCloseable {

    private final SessionFactory sessionFactory;

    public Jpa(DatabaseSettings settings) {
        Configuration configuration = new Configuration();
        configuration.addAnnotatedClass(User.class);
        configuration.addAnnotatedClass(Product.class);
        configuration.addAnnotatedClass(PromoCode.class);
        configuration.addAnnotatedClass(Cart.class);
        configuration.addAnnotatedClass(CartItem.class);
        configuration.addAnnotatedClass(Order.class);
        configuration.addAnnotatedClass(OrderItem.class);

        configuration.setProperty("hibernate.connection.provider_class",
                "org.hibernate.hikaricp.internal.HikariCPConnectionProvider");
        configuration.setProperty("hibernate.hikari.dataSourceClassName",
                "org.postgresql.ds.PGSimpleDataSource");
        configuration.setProperty("hibernate.hikari.dataSource.url", settings.getJdbcUrl());
        configuration.setProperty("hibernate.hikari.dataSource.user", settings.getUsername());
        configuration.setProperty("hibernate.hikari.dataSource.password", settings.getPassword());
        configuration.setProperty("hibernate.hikari.maximumPoolSize", String.valueOf(settings.getMaxPoolSize()));
        configuration.setProperty("hibernate.hikari.minimumIdle", "2");
        configuration.setProperty("hibernate.hikari.connectionTimeout", "10000");
        configuration.setProperty("hibernate.hikari.poolName", "datashop-pool");
        configuration.setProperty("hibernate.hikari.autoCommit", "false");

        configuration.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        configuration.setProperty("hibernate.hbm2ddl.auto", "none");
        configuration.setProperty("hibernate.jdbc.time_zone", "UTC");
        configuration.setProperty("hibernate.show_sql", "false");
        configuration.setProperty("hibernate.format_sql", "false");
        configuration.setProperty("hibernate.jdbc.batch_size", "25");
        configuration.setProperty("hibernate.order_inserts", "true");
        configuration.setProperty("hibernate.order_updates", "true");
        configuration.setProperty("hibernate.connection.autocommit", "false");

        this.sessionFactory = configuration.buildSessionFactory();
        log.info("Hibernate session factory initialised for {}", safeUrl(settings.getJdbcUrl()));
    }

    public <T> T read(Function<EntityManager, T> work) {
        try (EntityManager entityManager = sessionFactory.createEntityManager()) {
            return work.apply(entityManager);
        }
    }

    public <T> T write(Function<EntityManager, T> work) {
        try (EntityManager entityManager = sessionFactory.createEntityManager()) {
            EntityTransaction transaction = entityManager.getTransaction();
            transaction.begin();
            try {
                T result = work.apply(entityManager);
                transaction.commit();
                return result;
            } catch (RuntimeException exception) {
                if (transaction.isActive()) {
                    transaction.rollback();
                }
                throw exception;
            }
        }
    }

    public boolean healthCheck() {
        return read(entityManager -> {
            Object value = entityManager.createNativeQuery("select 1").getSingleResult();
            return value != null;
        });
    }

    @Override
    public void close() {
        sessionFactory.close();
    }

    private static String safeUrl(String url) {
        int credentials = url.indexOf("//");
        int at = url.indexOf('@');
        if (credentials > 0 && at > credentials) {
            return url.substring(0, credentials + 2) + "***" + url.substring(at);
        }
        return url;
    }
}
