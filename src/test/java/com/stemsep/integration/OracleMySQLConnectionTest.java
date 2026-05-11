package com.stemsep.integration;

import com.stemsep.config.HibernateConfig;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Spring kontekst + Hibernate SessionFactory + Oracle Cloud MySQL canlı bağlantı testi.
 *
 * <p>Bu test sınıfı, Spring uygulama bağlamının (ApplicationContext) tamamen
 * yüklenip, Hibernate {@link SessionFactory} bean'inin Spring konteynerinden
 * alınabildiğini ve gerçek MySQL veritabanına bağlanılıp basit yazma/okuma
 * round-trip'inin başarılı olduğunu doğrular.</p>
 *
 * <p><b>Çalıştırma önkoşulu:</b> bastion SSH tunnel açık olmalı
 * (localhost:3306 → Oracle Cloud MySQL 10.0.1.212:3306). Bu yüzden varsayılan
 * olarak {@link Disabled} ile devre dışı bırakılmıştır; CI/CD pipeline'ında veya
 * canlı veritabanı erişimi olduğunda elle açılır.</p>
 *
 * <p><b>Slayt referansı:</b> JUnit 5 + Spring entegrasyonu (BM470 Servlet & JSP
 * ders sunumları, "Birim Testleri" bölümü).</p>
 */
@Disabled("Bastion SSH tunnel + canlı Oracle Cloud MySQL gerektiriyor")
public class OracleMySQLConnectionTest {

    /**
     * Persistence katmanını izole olarak başlatan iç konfigürasyon.
     * {@code WebConfig} dahil edilmez (ServletContext beklemesin diye).
     */
    @Configuration
    @Import(HibernateConfig.class)
    @PropertySource(value = "classpath:hibernate.properties", encoding = "UTF-8")
    @EnableTransactionManagement
    static class PersistenceOnlyConfig {
    }

    private static AnnotationConfigApplicationContext ctx;

    /** Tüm test sınıfı için tek bir Spring kontekst başlatılır (pahalı işlem). */
    @BeforeAll
    public static void setUp() {
        ctx = new AnnotationConfigApplicationContext(PersistenceOnlyConfig.class);
    }

    /** Sınıf testleri bittiğinde kontekst temizce kapatılır. */
    @AfterAll
    public static void tearDown() {
        if (ctx != null) {
            ctx.close();
        }
    }

    /**
     * Test 1: Spring bağlamının başlamasını ve SessionFactory bean'inin
     * konteynerden alınabildiğini doğrular. Bu, Hibernate konfigürasyonunun
     * (c3p0, dialect, mapping) hatasız çözüldüğünün ilk göstergesidir.
     */
    @Test
    public void springContextStartsAndSessionFactoryIsAvailable() {
        SessionFactory sf = ctx.getBean(SessionFactory.class);
        assertNotNull(sf, "SessionFactory bean Spring container'dan alınamadı");
        assertTrue(!sf.isClosed(), "SessionFactory beklenmedik şekilde kapalı");
    }

    /**
     * Test 2: SessionFactory üzerinden gerçek bir oturum açıp MySQL sunucusunun
     * sürüm bilgisini sorgular. Bu test, c3p0 bağlantı havuzunun JDBC üzerinden
     * gerçek bir DB ile el sıkıştığını ve sürümün beklenen 8.x/9.x aralığında
     * olduğunu doğrular.
     */
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

    /**
     * Test 3: Transaction içinde tablo oluşturma, kayıt ekleme/güncelleme,
     * okuma ve tablo silme akışını tam bir round-trip ile doğrular. Bu test,
     * Hibernate transaction yönetimi + JDBC DDL desteğinin uçtan uca
     * çalıştığını gösterir.
     */
    @Test
    public void canCreateAndDropTestTable() {
        SessionFactory sf = ctx.getBean(SessionFactory.class);
        try (Session session = sf.openSession()) {
            session.beginTransaction();
            String msg = session.doReturningWork(conn -> {
                try (var stmt = conn.createStatement()) {
                    stmt.executeUpdate(
                            "CREATE TABLE IF NOT EXISTS _spring_smoketest (id INT PRIMARY KEY, msg VARCHAR(64))");
                    stmt.executeUpdate(
                            "INSERT INTO _spring_smoketest (id, msg) VALUES (1, 'ok') ON DUPLICATE KEY UPDATE msg='ok'");
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
