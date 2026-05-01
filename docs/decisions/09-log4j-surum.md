# 09 — log4j 2.25.3 + slf4j-reload4j 2.0.17

**Statü:** ✅ Kabul (2026-05-01 — slayta hizalandı)

## Karar
Slayttaki birebir loglama yığını kullanılır:
- `org.apache.logging.log4j:log4j` **2.25.3**
- `org.slf4j:slf4j-api` **2.0.17**
- `org.slf4j:slf4j-reload4j` **2.0.17**
- `spring-context`'ten `commons-logging` exclude

## Neden
NotebookLM citation [1] (loglama slaytları): yukarıdaki bağımlılıklar birebir slaytta gösterilmiş.
Citation [2]: *"Spring ile varsayılan olarak gelen loglama kütüphanesi (`commons-logging`), sınıflar arasında çakışma olmaması için ... hariç bırakılıyor."*

reload4j köprüsü, log4j 1.x stili `log4j.properties` config'i çalıştırmayı mümkün kılar — slaytta config bu stilde gösterilmiştir.

## Uygulama
**`log4j.properties`** (slayt birebir, citation [3], [4]):
```properties
log4j.rootLogger=DEBUG, stdout, file

log4j.appender.stdout=org.apache.log4j.ConsoleAppender
log4j.appender.stdout.Target=System.out
log4j.appender.stdout.layout=org.apache.log4j.PatternLayout
log4j.appender.stdout.layout.ConversionPattern=%d{yyyy-MM-dd HH:mm:ss} %-5p %c{1}:%L - %m%n

log4j.appender.file=org.apache.log4j.RollingFileAppender
log4j.appender.file.File=../logs/bm470.log
log4j.appender.file.MaxFileSize=10MB
log4j.appender.file.MaxBackupIndex=10
log4j.appender.file.layout=org.apache.log4j.PatternLayout
log4j.appender.file.layout.ConversionPattern=%d{yyyy-MM-dd HH:mm:ss} %-5p %c{1}:%L - %m%n
```

**Java sınıflarında Logger** (slayt citation [5]):
```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HesapMakinesi {
    private static Logger logger = LoggerFactory.getLogger(HesapMakinesi.class);
}
```

## Eski → Yeni Bağımlılık Karşılığı
| Eski | Yeni |
|---|---|
| `slf4j-api 1.7.25` | `slf4j-api 2.0.17` |
| `slf4j-log4j12 1.7.25` | `slf4j-reload4j 2.0.17` |
| `log4j:log4j 1.2.14` | `org.apache.logging.log4j:log4j 2.25.3` |
| (yok) | `spring-context` `commons-logging` exclusion |

## Kaynak
NotebookLM citations [1], [2], [3], [4], [5].
