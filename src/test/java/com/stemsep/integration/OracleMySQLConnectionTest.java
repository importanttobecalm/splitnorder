package com.stemsep.integration;

import com.stemsep.config.HibernateConfig;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Spring context + Hibernate SessionFactory + Oracle Cloud MySQL canlı bağlantı testi.
 * Çalıştırmadan önce: bastion SSH tunnel açık olmalı (localhost:3306 → 10.0.1.212:3306).
 */
public class OracleMySQLConnectionTest {

    @Configuration
    @Import(HibernateConfig.class)
    @PropertySource(value = "classpath:hibernate.properties", encoding = "UTF-8")
    @EnableTransactionManagement
    static class PersistenceOnlyConfig {
    }

    private static AnnotationConfigApplicationContext ctx;

    @BeforeAll
    public static void setUp() {
        ctx = new AnnotationConfigApplicationContext(PersistenceOnlyConfig.class);
    }

    @AfterAll
    public static void tearDown() {
        if (ctx != null) ctx.close();
    }

    @Test
    public void springContextStartsAndSessionFactoryIsAvailable() {
        SessionFactory sf = ctx.getBean(SessionFactory.class);
        assertNotNull(sf, "SessionFactory bean Spring container'dan alınamadı");
        assertTrue(!sf.isClosed(), "SessionFactory beklenmedik şekilde kapalı");
    }

    @Test
    public void canOpenSessionAndQueryMySQLVersion() {
        SessionFactory sf = ctx.getBean(SessionFactory.class);
        try (Session session = sf.openSession()) {
            String version = session.doReturningWork(conn -> {
                try (var stmt = conn.createStatement();
                     var rs = stmt.executeQuery("SELECT VERSION()")) {
                    rs.next();
                    return rs.getString(1);
                }
            });
            assertNotNull(version, "MySQL VERSION() null döndü");
            System.out.println(">>> Bağlanılan MySQL versiyonu: " + version);
            assertTrue(version.startsWith("8") || version.startsWith("9"),
                    "Beklenen MySQL 8/9 versiyonu değil: " + version);
        }
    }

    @Test
    public void canCreateAndDropTestTable() {
        SessionFactory sf = ctx.getBean(SessionFactory.class);
        try (Session session = sf.openSession()) {
            session.beginTransaction();
            String msg = session.doReturningWork(conn -> {
                try (var stmt = conn.createStatement()) {
                    stmt.executeUpdate("CREATE TABLE IF NOT EXISTS _spring_smoketest (id INT PRIMARY KEY, msg VARCHAR(64))");
                    stmt.executeUpdate("INSERT INTO _spring_smoketest (id, msg) VALUES (1, 'ok') ON DUPLICATE KEY UPDATE msg='ok'");
                    try (var rs = stmt.executeQuery("SELECT msg FROM _spring_smoketest WHERE id=1")) {
                        rs.next();
                        String result = rs.getString(1);
                        stmt.executeUpdate("DROP TABLE _spring_smoketest");
                        return result;
                    }
                }
            });
            session.getTransaction().commit();
            assertTrue("ok".equals(msg), "Yazma/okuma round-trip başarısız");
            System.out.println(">>> Yazma/okuma/silme round-trip OK");
        }
    }
}
