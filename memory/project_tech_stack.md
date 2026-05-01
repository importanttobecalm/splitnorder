---
name: Tech Stack
description: Kullanılan teknolojiler, mimari özet, dış servisler
type: project
updated: 2026-05-01
---

## Backend (Java)
- Spring 6.0.4 (saf, Boot yok) — `WebApplicationInitializer`
- Hibernate 6.1.7 (ders hedefi 5.3.20 — borç)
- MySQL (Oracle Cloud Free Tier)
- Tomcat (war deploy)
- pom.xml — Maven build
- Paket: `com.stemsep.*` (hedef: `tr.edu.duzce.mf.bm.bm470.*`)

## Audio Servisi
- `demucs-server/` — Python Flask API
- Demucs (Facebook AI) müzik kaynak ayrımı
- Java backend HTTP üzerinden çağırıyor

## Frontend
- JSP + JSTL
- i18n: messages_tr / messages_en (zorunlu)

## Deployment
- `deploy/` — Oracle Cloud Compute (Free Tier)
- `setenv.sh` ile JVM args
- Sade systemd unit
- GPU swap stratejisi (Faz 3)

## Test
- `test-local.sh` — local M3 8GB Mac test scripti
- TEST-README.md
- JUnit (hedef: 4.13.1)
