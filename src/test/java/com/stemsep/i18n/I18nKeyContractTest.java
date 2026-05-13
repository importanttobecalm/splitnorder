package com.stemsep.i18n;

import com.stemsep.exception.ErrorCode;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
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
 * <p><b>Enum-driven kontrat:</b> Önceden manuel string listesi vardı; yeni
 * exception eklerken o listeyi güncellemeyi unutmak regression doğuruyordu.
 * Artık {@link ErrorCode} enum'u tek kaynak — reflection ile gezilir,
 * her sabit için {@code getMessageKey()} TR+EN'de mevcut mu otomatik
 * doğrulanır. Yeni hata eklemenin maliyeti: enum'a 1 satır + properties'e
 * 2 satır.</p>
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
     * <b>ErrorCode enum sözleşmesi:</b> Reflection ile {@link ErrorCode}'in
     * her sabitini gezer, {@code getMessageKey()} TR ve EN'de tanımlı ve
     * boş değil olmalı. Yeni bir {@code ErrorCode} eklenince properties'e
     * key eklemeyi unutursak test patlar — production'da
     * {@code ???auth.error.XXX???} sızıntısı yaşanmadan önce CI yakalar.
     */
    @Test
    public void everyErrorCodeMessageKey_existsInBothBundles_withNonEmptyValues() {
        for (ErrorCode code : ErrorCode.values()) {
            String key = code.getMessageKey();
            assertContainsNonEmpty(tr, key,
                    "messages_tr_TR.properties  (ErrorCode." + code.name() + ")");
            assertContainsNonEmpty(en, key,
                    "messages_en_US.properties  (ErrorCode." + code.name() + ")");
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

    /**
     * Hiçbir {@link ErrorCode} sabiti aynı {@code messageKey}'i paylaşmamalı
     * — duplicate key, iki farklı hatanın aynı mesajı göstermesine yol açar
     * ve kullanıcı için debug zorlaşır. Bu test enum'un disiplinini korur.
     */
    @Test
    public void errorCode_messageKeysAreUnique() {
        Set<String> seen = new HashSet<>();
        for (ErrorCode code : ErrorCode.values()) {
            String key = code.getMessageKey();
            assertTrue(seen.add(key),
                    "Duplicate messageKey: " + key + " (ErrorCode." + code.name() + ")");
        }
    }

    private void assertContainsNonEmpty(Properties p, String key, String fileLabel) {
        String v = p.getProperty(key);
        assertNotNull(v, "Key eksik [" + fileLabel + "]: " + key);
        assertFalse(v.isBlank(), "Key boş değer [" + fileLabel + "]: " + key);
    }
}
