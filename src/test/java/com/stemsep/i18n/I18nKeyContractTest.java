package com.stemsep.i18n;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.*;

/**
 * i18n Sözleşme (Contract) Smoke Test'i.
 *
 * <p>Amaç: Controller'da {@code model.addAttribute("error", "XXX")} ile
 * kullanılan key'lerin {@code messages_*.properties} dosyalarında
 * <b>kesin</b> mevcut olmasını ve TR/EN dosyalarının aynı key kümesine sahip
 * olmasını garanti etmek. Üretimde gördüğümüz
 * {@code ???auth.error.INVALID_CREDENTIALS???} bug'ı, bu sözleşme yokken
 * doğdu; bu test onu bir daha doğmasını engeller.</p>
 *
 * <p><b>Slayt referansı doğrudan yok</b> — proje gereksinim dokümanındaki
 * "açıklamalı birim testler" beklentisinin uygulamalı bir genişlemesidir.
 * Saf JUnit 5 + JDK Properties API; ek bağımlılık yok.</p>
 *
 * @author splitnorder team — BM470
 */
public class I18nKeyContractTest {

    private static Properties tr;
    private static Properties en;

    /** Controller'ın Model'e koyduğu hata kodlarının tam listesi (UPPER_SNAKE_CASE). */
    private static final List<String> AUTH_ERROR_CODES = List.of(
            "INVALID_CREDENTIALS",
            "EMAIL_NOT_VERIFIED",
            "TOKEN_EXPIRED",
            "INVALID_TOKEN",
            "USERNAME_EXISTS",
            "EMAIL_EXISTS",
            "INTERNAL_ERROR"
    );

    @BeforeAll
    public static void loadBundles() throws Exception {
        tr = load("messages_tr_TR.properties");
        en = load("messages_en_US.properties");
    }

    private static Properties load(String resource) throws Exception {
        Properties p = new Properties();
        try (InputStream in = I18nKeyContractTest.class.getClassLoader().getResourceAsStream(resource)) {
            assertNotNull(in, "Resource not on classpath: " + resource);
            p.load(new java.io.InputStreamReader(in, StandardCharsets.UTF_8));
        }
        return p;
    }

    /**
     * TR ve EN bundle'ları aynı key set'ine sahip olmalı — aksi takdirde
     * dil değişiminde bazı mesajlar bir dilde çevrilmemiş kalır (asymmetric
     * translation bug).
     */
    @Test
    public void trAndEnBundles_haveIdenticalKeySets() {
        Set<String> trKeys = new TreeSet<>(tr.stringPropertyNames());
        Set<String> enKeys = new TreeSet<>(en.stringPropertyNames());

        Set<String> onlyInTr = new HashSet<>(trKeys); onlyInTr.removeAll(enKeys);
        Set<String> onlyInEn = new HashSet<>(enKeys); onlyInEn.removeAll(trKeys);

        assertTrue(onlyInTr.isEmpty() && onlyInEn.isEmpty(),
                "Bundle key set'leri farklı.\n  Sadece TR'de: " + onlyInTr +
                "\n  Sadece EN'de: " + onlyInEn);
    }

    /**
     * Controller'ın koyduğu HER auth.error.XXX key'i TR ve EN dosyalarında
     * tanımlı VE boş değil olmalı. Bug regression koruması.
     */
    @Test
    public void authErrorCodes_existInBothBundles_withNonEmptyValues() {
        for (String code : AUTH_ERROR_CODES) {
            String key = "auth.error." + code;
            assertContainsNonEmpty(tr, key, "messages_tr_TR.properties");
            assertContainsNonEmpty(en, key, "messages_en_US.properties");
        }
    }

    /**
     * Hiçbir mesaj değeri Spring'in "key bulunamadı" işareti olan
     * {@code ???} prefix'ini içermemeli — bu durum properties dosyasına
     * yanlışlıkla yansıyan bir lookup placeholder anlamına gelir.
     */
    @Test
    public void noValue_containsTripleQuestionMarks() {
        for (String key : tr.stringPropertyNames()) {
            assertFalse(tr.getProperty(key).contains("???"),
                    "TR bundle değeri ??? içeriyor: " + key);
        }
        for (String key : en.stringPropertyNames()) {
            assertFalse(en.getProperty(key).contains("???"),
                    "EN bundle değeri ??? içeriyor: " + key);
        }
    }

    private void assertContainsNonEmpty(Properties p, String key, String fileLabel) {
        String v = p.getProperty(key);
        assertNotNull(v, "Key eksik [" + fileLabel + "]: " + key);
        assertFalse(v.isBlank(), "Key boş değer [" + fileLabel + "]: " + key);
    }
}
