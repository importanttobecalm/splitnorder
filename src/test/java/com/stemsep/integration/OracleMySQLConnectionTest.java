package com.stemsep.integration;

import com.stemsep.config.HibernateConfig;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Spring context + Hibernate SessionFactory + Oracle Cloud MySQL canlı bağlantı testi.
 * Çalıştırmadan önce: bastion SSH tunnel açık olmalı (localhost:3306 → 10.0.1.212:3306).
 *
 * Sadece persistence katmanını bootstrap eder (WebConfig'i hariç tutar — ServletContext istemesin diye).
 */
public class OracleMySQLConnectionTest {

    @Configuration
    @Import(HibernateConfig.class)
    @PropertySource(value = "classpath:hibernate.properties", encoding = "UTF-8")
    @EnableTransactionManagement
    static class PersistenceOnlyConfig {
    }

    private static AnnotationConfigApplicationContext ctx;

    @BeforeClass
    public static void setUp() {
        ctx = new AnnotationConfigApplicationContext(PersistenceOnlyConfig.class);
    }

    @AfterClass
    public static void tearDown() {
        if (ctx != null) ctx.close();
    }

    @Test
    public void springContextStartsAndSessionFactoryIsAvailable() {
        SessionFactory sf = ctx.getBean(SessionFactory.class);
        assertNotNull("SessionFactory bean Spring container'dan alınamadı", sf);
        assertTrue("SessionFactory beklenmedik şekilde kapalı", !sf.isClosed());
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
            assertNotNull("MySQL VERSION() null döndü", version);
            System.out.println(">>> Bağlanılan MySQL versiyonu: " + version);
            assertTrue("Beklenen MySQL 8/9 versiyonu değil: " + version,
                    version.startsWith("8") || version.startsWith("9"));
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
            assertTrue("Yazma/okuma round-trip başarısız", "ok".equals(msg));
            System.out.println(">>> Yazma/okuma/silme round-trip OK");
        }
    }
}
