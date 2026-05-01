# 01 — Paket Kökü: `com.stemsep`

**Statü:** ✅ Kabul (migrasyon iptal)

## Karar
Mevcut paket kökü `com.stemsep` korunacak. `tr.edu.duzce.mf.bm.bm470`'a migrasyon yapılmayacak.

## Neden
NotebookLM sorgusu (`bm470`, "paket adlandırma kuralı"):

> *"Proje teslim dokümanında paket adlandırması ile ilgili herhangi bir kural veya format zorunluluğu yer almamaktadır."*

Slaytlarda hem `tr.edu.duzce.mf.bm` hem `com.talhakabakus` örnekleri **birlikte** kullanılmış — biri zorunlu değil.

## Uygulama
- Yeni sınıflar `com.stemsep.*` altında.
- Önceki "paket migrasyon borcu" notu (`gotchas.md`, `INTEGRATION_GUIDELINES.md`) **iptal**.

## Kaynak
NotebookLM citations [7], [8], [9], [10] — `Düzce Üniversitesi - Proje Gereksinimleri` + Maven slaytları.
