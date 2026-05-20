package com.stemsep.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Kullanıcının seçtiği stem alt kümesinden üretilen karma (mix) parça.
 *
 * <p>Örnek: {@code stemTypes="vocals,drums"} → yalnız vokal + davul içeren
 * yeni bir ses dosyası. Kaggle Flask {@code /api/mix} endpoint'i tarafından
 * üretilir, sunucuda {@code stems/<jobPublicId>/mixes/<publicId>.<format>}
 * yolunda saklanır.</p>
 *
 * <p>Bu kayıtlar kullanıcının 5 GB kotasına {@link #fileSize} kadar
 * eklenir (bkz. ADR-13).</p>
 */
@Entity
@Table(name = "mixed_tracks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MixedTrack {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "public_id", nullable = false, unique = true, updatable = false, length = 36)
    private String publicId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    /** Kullanıcı tarafından verilen ya da auto-generated isim (örn. "Vocals + Drums"). */
    @Column(nullable = false, length = 200)
    private String name;

    /** Kaynak stem türleri CSV — "vocals,drums". 4 stem'in alt kümesi. */
    @Column(name = "stem_types", nullable = false, length = 100)
    private String stemTypes;

    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    /** "mp3" veya "wav" — düşük seviye `audio/*` content-type türetimi için. */
    @Column(nullable = false, length = 10)
    private String format;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.publicId == null) {
            this.publicId = UUID.randomUUID().toString();
        }
    }
}
