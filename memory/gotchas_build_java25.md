---
name: Lokal build için JAVA_HOME 21 şart
description: Homebrew default Java 25 + Lombok 1.18.42 → annotation processing fail, User/Job getter-setter cannot find symbol
type: feedback
---

`mvn clean package` direkt çalıştırılırsa Homebrew default Java 25 kullanır, Lombok 1.18.42 ile annotation processing patlar → "cannot find symbol: method setEmail / getId" gibi yanıltıcı hatalar verir (aslında Lombok generated metodlar).

**Why:** Lombok 1.18.42 Java 25'i tam desteklemiyor, annotation processor sessizce devre dışı kalıyor.

**How to apply:** Build komutunu hep şöyle çalıştır:
```bash
JAVA_HOME=$(/usr/libexec/java_home -v 21) mvn clean package -DskipTests
```
Sunucu zaten Java 17/21 ile çalıştığı için runtime'da sorun yok — sadece lokal build.
