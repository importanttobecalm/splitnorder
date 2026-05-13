package com.stemsep.model;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link Job} entity'sinin davranışsal birim testleri.
 *
 * <p>Job artık iki kimliğe sahiptir: <b>numeric {@code id}</b> (internal PK,
 * FK ilişkilerinde kullanılır) ve <b>UUID tabanlı {@code publicId}</b>
 * (URL'lerde gözüken kimlik). Sıralı Long ID'nin URL'de görünmesi enumeration
 * saldırısına ve UX rahatsızlığına yol açtığı için her job persist edilirken
 * {@code @PrePersist} callback'i otomatik UUID üretir.</p>
 *
 * <p>Bu testler GERÇEK DB'ye ihtiyaç duymaz — sadece JPA lifecycle
 * callback'inin pure-Java davranışını doğrular.</p>
 */
public class JobTest {

    /**
     * {@code @PrePersist} çağrıldığında {@code publicId} boşsa otomatik UUID
     * üretilmeli ve {@code createdAt} set edilmeli. Üretilen değer geçerli
     * UUID formatında olmalı (parse edilebilir).
     */
    @Test
    public void prePersist_setsPublicIdAndCreatedAt() {
        Job job = new Job();
        assertNull(job.getPublicId());
        assertNull(job.getCreatedAt());

        job.prePersist();

        assertNotNull(job.getPublicId());
        assertNotNull(job.getCreatedAt());
        // Geçerli UUID formatı (throws IllegalArgumentException if invalid)
        UUID parsed = UUID.fromString(job.getPublicId());
        assertEquals(36, job.getPublicId().length());
        assertNotNull(parsed);
    }

    /**
     * {@code publicId} önceden set edilmişse (örn. test fixture, dataloader
     * script) {@code @PrePersist} override etmemeli. Aksi takdirde deterministik
     * seed verilerinin ID'leri her yeniden başlatmada değişirdi.
     */
    @Test
    public void prePersist_preservesExistingPublicId() {
        Job job = new Job();
        String fixed = "11111111-2222-3333-4444-555555555555";
        job.setPublicId(fixed);

        job.prePersist();

        assertEquals(fixed, job.getPublicId());
    }

    /**
     * Aynı Job nesnesinde art arda iki kez {@code prePersist} çağrılsa bile
     * publicId değişmemeli (idempotency). Hibernate normalde yalnızca bir kez
     * tetikler ama defansif kontrol — refactor regression koruması.
     */
    @Test
    public void prePersist_isIdempotentForPublicId() {
        Job job = new Job();
        job.prePersist();
        String first = job.getPublicId();

        job.prePersist();

        assertEquals(first, job.getPublicId());
    }

    /**
     * İki ayrı Job nesnesi için üretilen publicId'ler farklı olmalı
     * (UUID.randomUUID() çakışmazlığı). Bu, history'de iki kayıt aynı URL'e
     * gitmemesini garanti eder.
     */
    @Test
    public void prePersist_generatesUniquePublicIdsAcrossInstances() {
        Job a = new Job();
        Job b = new Job();
        a.prePersist();
        b.prePersist();

        assertNotEquals(a.getPublicId(), b.getPublicId());
    }
}
