# BM470 İLERİ JAVA PROGRAMLAMA DERSİ
# DERS NOTLARI 7-8-9 ANALİZİ
## Spring Service & DAO + Hibernate Criteria Query API + Spring Log ile Loglama

> **AMAÇ:** Bu belge, **ilk3pdf.md**'nin devamıdır. İlk3pdf.md'de 1-3. ders notları analiz edilmişti (Java EE Giriş, Geliştirme Ortamı, Maven). Bu belgede **7., 8. ve 9. ders notları** analiz edilmiştir. **4, 5, 6. ders notları henüz analiz edilmedi** — bu ders notları Controller/Interceptor/i18n konularını kapsıyor olabilir ve ayrı bir oturumda ele alınmalıdır.
>
> Ders notlarına **TAM OLARAK** sadık kalınmıştır. Modern best-practice'lere kaymak yok; derste ne gösterildiyse, o kullanılacak.

---

## 🟥 ÇÖZÜLMESİ GEREKEN VERSİYON ÇELİŞKİLERİ

**BU KONU PROJE BAŞLAMADAN KARARA BAĞLANMALIDIR.** Çünkü hoca farklı ders notlarında farklı bağımlılık versiyonları göstermiş:

| Bileşen | ilk3pdf.md (3. ders notu pom.xml) | Yeni Ders Notu (7/9) | Çakışma |
|---------|----------------------------------|----------------------|---------|
| `slf4j` | `1.7.25` | `slf4j-api 2.0.17` + `slf4j-reload4j 2.0.17` (PDF 9) | ✓ |
| `log4j` | `log4j 1.2.14` (log4j 1.x serisi) | `log4j 2.25.3` (log4j 2.x serisi!) | ✓ |
| `mysql-connector` | `mysql-connector-java 8.0.28` | `mysql-connector-j 9.1.0` (artifactId dahi değişmiş) | ✓ |

**Karar alırken dikkat edilmesi gereken noktalar:**
1. Log4j 1.x ile 2.x API'leri farklı. Eğer `log4j 2.x` kullanılırsa PDF 9'daki `org.apache.log4j.ConsoleAppender` / `org.apache.log4j.RollingFileAppender` / `org.apache.log4j.PatternLayout` sınıfları aynı paket adıyla mevcut olmayabilir (log4j 2.x'te paketler `org.apache.logging.log4j.*` altında, properties dosyası da `log4j2.xml/.properties`).
2. PDF 9'daki `log4j.properties` örneği hâlâ `org.apache.log4j.ConsoleAppender` gibi **log4j 1.x API sınıflarını** kullanıyor ama bağımlılık olarak `log4j 2.25.3` vermiş. Bu iki şey **birlikte çalışmaz**. Bu ya hocanın bir hatası, ya da `slf4j-reload4j` üzerinden Reload4j (log4j 1.x fork) kullanıldığı için `org.apache.log4j.*` API'si hâlâ yaşıyor olduğu anlamına geliyor. En tutarlı yorum: **Reload4j kullanılacak**; çünkü `slf4j-reload4j` dependency'si Reload4j'i içerir ve `org.apache.log4j.*` package'ini sağlar. Bu durumda log4j `2.25.3` numarası aslında **log4j 2** değil, `log4j:log4j:2.25.3` diye bir şey yok — muhtemelen PDF'te tipografik hata var. En sağlıklısı `reload4j` kullanmak.

**Tavsiye edilen yaklaşım (Yusuf'un karar vermesi gereken):**
- **Seçenek A — Eski versiyonlara sadık kal:** ilk3pdf.md'deki pom.xml'i değiştirme. `log4j 1.2.14` + `slf4j 1.7.25` + `mysql-connector-java 8.0.28` — bu durumda PDF 9'daki properties örnekleri aynen çalışır.
- **Seçenek B — Yeni versiyonlara geç:** `mysql-connector-j 9.1.0` + `slf4j-api 2.0.17` + `slf4j-reload4j 2.0.17`, log4j bağımlılığını `ch.qos.reload4j:reload4j:1.2.26` olarak yaz. Bu durumda `org.apache.log4j.*` API'si çalışmaya devam eder.

**Bu md boyunca her iki versiyonu da belirteceğim ve kod örneklerini hocanın verdiği hâliyle aynen aktaracağım.**

---

# 📘 DERS NOTU 7: Spring Service ve DAO Katmanları

> **Kapak:** _Spring Service ve DAO Katmanları — Doç. Dr. Talha KABAKUŞ_
> **54 slayt.**

## 7.1. 3 Katmanlı Mimarinin Hatırlatılması

Derste verilen akış görseli:

```
    Controller  ──►  Service  ──►  DAO  ──►  Veritabanı
       ▲
   HTTP İsteği
  (Uygulama Sunucusu içinde)
```

`ilk3pdf.md`'deki katman şemasının detaylandırılmış hâli:
- **Sunum Katmanı** → Controller'lar
- **İş Katmanı (orta katman)** → Service'ler ← **Bu ders notunun ilk konusu**
- **Veri Erişim Katmanı** → DAO'lar ← **Bu ders notunun ikinci konusu**

---

## 7.2. SERVICE SINIFLARI

### 7.2.1. Temel Tanım

- Üç katmanlı mimarinin **orta katmanını** oluştururlar.
- `@Service` annotation'ı ile tanımlanırlar.
- Controller tarafından karşılanan HTTP isteğiyle ilgili **iş süreçlerinin (business procedure)** karşılanmasını sağlar.
- Model sınıfına ihtiyaç duyulması durumunda veritabanına erişim için **DAO (Data Access Object) sınıflarıyla iletişim kurar**.

### 7.2.2. Transaction Yönetimi (Spring)

- `@EnableTransactionManagement` annotation'ı, Spring konfigürasyonunda transaction işlemlerinin annotation'larla yapılacağını belirtir. Bu, **uygulama konfigürasyon sınıfına (AppConfig.java)** tanımlanır.
- **Transaction**: Veritabanı ile yapılan ve **bütünlük ifade eden işlemler seti** anlamına gelir.
- Veritabanı ile yapılan **bütün etkileşimler** bir transaction altında yapılmalıdır.
- Bir transaction'da yapılan işlemler ya **`commit`** edilip uygulanır ya da **`rollback`** edilip geri alınır.
- İlgili servis sınıfının transaction'larının nasıl yapılacağını **`TransactionManager`** belirler.
- `TransactionManager` bean'i, Spring uygulama konfigürasyon sınıfında tanımlanmalıdır.

### 7.2.3. AppConfig.java — İlk Versiyonu (Sadece Transaction & SessionFactory)

**Paket:** `tr.edu.duzce.mf.bm.bm470.config`

```java
@EnableTransactionManagement
@Configuration
@ComponentScan(basePackages = {"tr.edu.duzce"})   // veya aşağıdaki @ComponentScans
@ComponentScans(value = {
    @ComponentScan("tr.edu.duzce.mf.bm.bm470.service"),
    @ComponentScan("tr.edu.duzce.mf.bm.bm470.dao")
})
public class AppConfig {

    @Bean
    public LocalSessionFactoryBean getSessionFactory() {
        LocalSessionFactoryBean factoryBean = new LocalSessionFactoryBean();
        // session factory konfigurasyonu komutlari
        return factoryBean;
    }

    @Bean
    public HibernateTransactionManager getTransactionManager() {
        HibernateTransactionManager transactionManager = new HibernateTransactionManager();
        transactionManager.setSessionFactory(getSessionFactory().getObject());
        return transactionManager;
    }
}
```

**Kilit noktalar:**
- **İkili `@ComponentScan` seçeneği var:** Ya tek `@ComponentScan(basePackages = {"tr.edu.duzce"})`, ya da birden çok `@ComponentScans`.
- `LocalSessionFactoryBean` (Hibernate SessionFactory bean tipi) `@Bean` metoduyla döndürülüyor.
- `HibernateTransactionManager` bean'i, SessionFactory'ye bağlanıyor.

### 7.2.4. `@EnableTransactionManagement` Davranışı

- Bu annotation **esnek** bir yaklaşım sunar:
  - Uygulamada tanımlı olan her tipteki `TransactionManager` nesnelerini **otomatik arar**.
  - Birden fazla olması durumunda `@Bean` annotation'ının `name` özelliği ile isimlendirme yapılabilir.
  - Böylelikle aynı uygulama altında **farklı türlerdeki TransactionManager nesnelerinin** beraber kullanımı mümkün olur.

### 7.2.5. `@Transactional` Annotation'ının Tüm Parametreleri

#### (I) `value`
- İlgili Transaction Manager tanımı.
- Birden fazla transaction manager tanımlanıyorsa **unique (tekil)** olmalıdır.
- Tanımlanmaması durumunda uygulamada tanımlanan transaction manager otomatik olarak tespit edilir.

#### (II) `propagation` (Transaction Yayılımı)
Transaction içinden yeni bir transaction oluşma/yayılma durumunu belirler. Tüm değerler ve anlamları:

| Değer | Anlam |
|-------|-------|
| `Propagation.REQUIRED` | **Varsayılan**. Varsa mevcut transaction ile işlem devam eder. Yoksa yeni bir transaction oluşturulur. |
| `Propagation.REQUIRES_NEW` | İşlem öncesinde **her seferinde yeni bir transaction** oluşturulmasını sağlar. Bir önceki transaction commit edilir (işlenir). |
| `Propagation.MANDATORY` | İşlem öncesinde **mevcut bir transaction'ının olmasını gerektirir**. |
| `Propagation.SUPPORTS` | İşlem öncesinde mevcut bir transaction'ının olması şart değildir; ancak olması durumunda da yayılım gerçekleştirilir. |
| `Propagation.NOT_SUPPORTED` | İşlem bir transaction içerisinde gerçekleştirilemez. Mevcut bir transaction varsa **işlem süresince duraklatılır**. |
| `Propagation.NEVER` | İşlem bir transaction içerisinde gerçekleştirilemez. `NOT_SUPPORTED`'dan farklı olarak transaction varsa **Exception fırlatılır**. |

#### (III) `readOnly` (boolean)
- `true`: Sadece okuma.
- `false`: **Varsayılan değer**. Hem okuma hem yazma sağlar.

#### (IV) `rollbackFor`
- Tipi: `Class<? extends Throwable>[] rollbackFor()`
- İlgili transaction'ın hangi exception'ların oluşması durumunda geri alınacağı belirtilir.

#### (V) `noRollbackFor`
- Tipi: `Class<? extends Throwable>[] noRollbackFor()`
- İlgili transaction'ın hangi exception'ların oluşması durumunda geri alınmayacağı belirtilir.

### 7.2.6. Örnek Service Sınıfı (Dersten Aynen)

**Paket:** `tr.edu.duzce.mf.bm.bm470.service`

```java
@Service
@Transactional(
    propagation = Propagation.REQUIRED,
    readOnly = true,
    rollbackFor = RuntimeException.class
    // veya çok elemanlı:
    // rollbackFor = {NullPointerException.class, ArithmeticException.class}
)
public class OgrenciService {

    @Autowired
    private OgrenciDAO ogrenciDAO;

    public List<Ogrenci> ogrencileriYukle() {
        List<Ogrenci> ogrenciler = ogrenciDAO.ogrencileriYukle();
        return ogrenciler;
    }

    @Transactional(readOnly = false)
    public Boolean saveOrUpdateOgrenci(String isim, String soyisim) {
        Ogrenci ogrenci = new Ogrenci();
        ogrenci.setIsim(isim);
        ogrenci.setSoyisim(soyisim);
        Boolean result = ogrenciDAO.saveOrUpdate(ogrenci);
        return result;
    }
}
```

**Dikkat çeken noktalar:**
- **Sınıf düzeyinde** `@Transactional` default olarak `readOnly = true` verilmiş.
- **Metot düzeyinde** override edilerek `saveOrUpdateOgrenci` için `readOnly = false` yapılmış — yazma işlemi bu sayede mümkün.
- `@Autowired` ile `OgrenciDAO` dependency injection edilmiş.

---

## 7.3. DAO KATMANI

### 7.3.1. Temel Tanım

- **DAO = Data Access Object**
- 3 katmanlı mimarinin **son katmanını** temsil eder.
- `@Repository` annotation'ı ile tanımlanırlar.
- **Sağlayıcı (vendor) bağımsız** bir şekilde veritabanı ile iletişim kurulması ve veritabanı üzerinde işlemler yapılmasını sağlar.

### 7.3.2. CRUD İşlemleri

- **C**reate
- **R**ead
- **U**pdate
- **D**elete

### 7.3.3. SQL Alt Dilleri ve Komutları

Derste verilen infografikten:

| Alt Dil | Açılımı | Komutlar |
|---------|---------|----------|
| **DDL** | Data Definition Language | Create, Alter, Drop, Rename, Insert, Update, Delete, Merge, Truncate |
| **DML** | Data Manipulation Language | Insert, Update, Delete, Merge |
| **DCL** | Data Control Language | Grant, Revoke |
| **TCL** | Transaction Control Language | Commit, Rollback, Savepoint |
| **DQL** | Data Query Language | Select |

### 7.3.4. Hibernate (ORM Framework)

- Bir **ORM (Object-Relational Mapping)** uygulama çatısı (framework).
- Bir **JPA (Java Persistence API) sağlayıcısı**.
- **JPA**, çözüm (vendor) bağımsız ilişkisel veritabanı tablolarının basit Java sınıfları (**POJO — Plain Old Java Object**) ile eşleştirilmesini sağlar.
- **JSR 220**, 2006.
- Hibernate, ilişkisel veritabanı yönetim sisteminden geliştiriciyi **soyutlamayı (abstraction)** sağlar.
- Bu sayede aynı kod ile çeşitli veritabanı yönetim sistemi ürünlerinin (Oracle, MySQL, PostgreSQL, MS SQL, gibi) kullanımı sağlanır.
- Sağladığı `SessionFactory` sınıfı ile veritabanı ile iletişim için kullanılacak olan **`session`** (oturum)'ların tanımı ve yönetimi yapılır.

---

## 7.4. VERİTABANI KURULUMU — MySQL

> Derste, veritabanı olarak **MySQL** seçilmiş. Dünyanın en popüler veritabanı yazılımı (Stack Overflow 2022 anketi kaynak gösterilmiş).

### 7.4.1. Linux Üzerinde Kurulum

```bash
$ sudo apt update
$ sudo apt install mysql-server
$ mysql_secure_installation

# Servis Durum Görüntüleme
$ systemctl status mysql.service

# Servis Başlatma
$ systemctl start mysql.service

# Servis Durdurma
$ systemctl stop mysql.service
```

Linux sanal sunucu kurulum rehberi (dersten referans):
`https://speakerdeck.com/talhakabakus/how-to-install-ubuntu-server-on-virtualbox`

### 7.4.2. Windows Üzerinde Kurulum

- `https://dev.mysql.com/downloads/installer/` adresinden setup dosyası indirilir.
- Kurulum esnasında geliştirici araçlarının da yüklenmesi için **"Developer Default"** profili ayarlanır.
- Servis olarak MySQL'in kurulumu:
  - MySQL servisi durdurulur:
    ```
    C:\> "C:\Program Files\MySQL\MySQL Server 5.5\bin\mysqladmin" -u root -p sifre shutdown
    ```
  - Servis olarak tanıtılır:
    ```
    C:\> "C:\Program Files\MySQL\MySQL Server 5.5\bin\mysqld" /-install
    ```
- Servis işletim sistemiyle beraber ayağa kalkar.
- Servisi manuel olarak durdurmak için `NET STOP MySQL`
- Servisi manuel olarak başlatmak için `NET START MySQL`

### 7.4.3. Docker Üzerinde Kurulum

Kaynak: `https://hub.docker.com/_/mysql`

> (Ders notunda kod ayrıntısı verilmemiş, sadece referans var.)

### 7.4.4. MySQL Hızlı Başlangıç Komutları

Ders notunda quickref.me cheatsheet'i kullanılmış. Önemli komutlar:

**Bağlantı (Connect MySQL):**
```bash
mysql -u <user> -p
mysql [db_name]
mysql -h <host> -P <port> -u <user> -p [db_name]
mysql -h <host> -u <user> -p [db_name]
```

**Database işlemleri:**
```sql
CREATE DATABASE db;           -- Create database
SHOW DATABASES;               -- List databases
USE db;                       -- Switch to db
CONNECT db;                   -- Switch to db
DROP DATABASE db;             -- Delete db
```

**Tablo işlemleri:**
```sql
SHOW TABLES;                  -- List tables for current db
SHOW FIELDS FROM t;           -- List fields for a table
DESC t;                       -- Show table structure
SHOW CREATE TABLE t;          -- Show create table sql
TRUNCATE TABLE t;             -- Remove all data in a table
DROP TABLE t;                 -- Delete table
```

**Process:**
```sql
SHOW PROCESSLIST;             -- List processes
KILL pid;                     -- Kill process
```

**Backup:**
```bash
# Backup oluştur
mysqldump -u user -p db_name > db.sql

# Şema olmadan export
mysqldump -u user -p db_name --no-data=true --add-drop-table=false > db.sql

# Backup geri yükle
mysql -u user -p db_name < db.sql
```

**Diğer:**
```
exit veya \q                  -- MySQL session'dan çık
```

### 7.4.5. Veritabanı Yönetim Araçları

Derste 3 farklı araç gösterilmiş:

#### (a) MySQL Workbench
- MySQL veritabanlarını yönetmek ve sorgulamak için geliştirilmiş, **Oracle'ın kendi aracıdır**.
- Ücretsiz, platform bağımsız (Windows, Linux ve macOS).

#### (b) IntelliJ IDEA Dahili "Database" Arayüzü
- IntelliJ'in içine gömülü, birçok veritabanını destekleyen araç.
- **Bu araçla E-R diyagramı da üretilebiliyor** (ders notu 7/34).
- Rapor gereksinimlerindeki E-R diyagramı bu araçla oluşturulabilir.

#### (c) DataGrip
- JetBrains ürün ailesinin **platform-bağımsız bir ürünü**.
- Çoklu veritabanı yönetim desteği sunuyor.
- Özellikler:
  - AI Destekli Sorgu Üretme
  - Çeşitli formatlarda export
  - Sorgu geçmişi ve parametreli SQL sorguları
- **DataGrip ticari olmayan kullanımlar için ücretsiz** olarak sunuluyor (Ekim 2025 itibarıyla).

### 7.4.6. MySQL Maven Bağımlılık Tanımı (Dersten)

> **⚠️ İlk3pdf.md'deki pom.xml'de `mysql-connector-java 8.0.28` vardı. PDF 7 slayt 32'de ise aşağıdaki (farklı artifactId) kullanılmış:**

```xml
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <version>9.1.0</version>
</dependency>
```

(PDF'te `//groupId>` gibi yazım vardı ama bu sembol eksikliğinden kaynaklanıyor, gerçek XML yukarıdaki.)

---

## 7.5. c3p0 — JDBC Bağlantı Havuzu

### 7.5.1. Genel Bilgi

- **Açık kaynak kodlu JDBC bağlantı havuzu yönetim kütüphanesi**.
- Sorgu **cache'leme desteği** sunmakta.
- Hibernate bağlantı havuz yönetimi için c3p0 entegrasyon desteği sunmakta.

### 7.5.2. c3p0 Konfigürasyon Parametreleri (Tamamı)

| Parametre | Açıklama |
|-----------|----------|
| `hibernate.c3p0.min_size` | Bağlantı havuzunda tutulacak **minimum** bağlantı sayısı |
| `hibernate.c3p0.max_size` | Bağlantı havuzunda tutulacak **maksimum** bağlantı sayısı |
| `hibernate.c3p0.acquire_increment` | Bağlantı havuzu dolduğu zaman yapılacak bağlantı artırım miktarı |
| `hibernate.c3p0.timeout` | Bağlantının havuzdan çıkartılma zaman aşımı süresi (saniye) |
| `hibernate.c3p0.max_statements` | Cache'lenen maksimum sorgu sayısı |
| `hibernate.c3p0.initialPoolSize` | Bağlantı havuzu **başlangıç** bağlantı sayısı |
| `hibernate.c3p0.idle_test_period` | Boş bağlantı test süresi (saniye) |
| `hibernate.c3p0.acquireRetryAttempts` | c3p0'nun veritabanından bağlantı elde etme tekrar girişim sayısı |
| `hibernate.c3p0.acquireRetryDelay` | c3p0'nun veritabanından bağlantı elde etme tekrar girişimleri arası bekleme süresi (milisaniye) |

---

## 7.6. `hibernate.properties` Dosyası

Classpath'te (**`src/main/resources/`**) konumlandırılan `hibernate.properties` dosyası ile Hibernate konfigürasyonu gerçekleştirilir.

### 7.6.1. Dosya İçeriği (Dersten Aynen)

```properties
# MySQL ayarları
mysql.driver=com.mysql.cj.jdbc.Driver
#mysql.driver=com.mysql.jdbc.Driver
mysql.url=jdbc:mysql://server_adresi:3306/sema_adi?characterEncoding=utf-8
mysql.user=kullanici_adi
mysql.password=sifre

# Hibernate ayarları
hibernate.show_sql=true
hibernate.hbm2ddl.auto=create
hibernate.dialect=org.hibernate.dialect.MySQLDialect
hibernate.default_schema=sema_adi

# c3p0 ayarları
hibernate.c3p0.min_size=5
hibernate.c3p0.max_size=200
hibernate.c3p0.initialPoolSize=5
hibernate.c3p0.acquire_increment=1
hibernate.c3p0.timeout=1800            # bostaki baglantilar ne kadar tutulsun (sn)
hibernate.c3p0.max_statements=150      # cache'lenecek sorgu sayisi
hibernate.c3p0.idle_test_period=60     # baglanti kontrol (ping) suresi (sn)
hibernate.c3p0.acquireRetryAttempts=1
hibernate.c3p0.acquireRetryDelay=250   # ms
```

### 7.6.2. `hibernate.hbm2ddl.auto` Değerleri

| Değer | Davranış |
|-------|----------|
| `create` | Mevcut tablo kaldırılıp yeniden oluşturulur. |
| `validate` | Tablo ve sütunları doğrular. **(default)** |
| `update` | Eksik olan tablo ve sütunları tamamlar. |
| `create-drop` | Tabloyu oluşturur, ilgili işlemleri gerçekleştirir ve son olarak tabloyu kaldırır. |

### 7.6.3. Ek MySQL Parametresi

- Ek olarak MySQL SSL uyarısını bastırmak için **`useSSL=false`** parametresi de URL'ye eklenebilir.

### 7.6.4. Başlıca Hibernate Dialect Sınıfları

| Veritabanı | Dialect Sınıfı |
|-----------|----------------|
| **MySQL (Generic)** | `org.hibernate.dialect.MySQLDialect` |
| Oracle (Generic) | `org.hibernate.dialect.OracleDialect` |
| Oracle 9i | `org.hibernate.dialect.Oracle9iDialect` |
| Oracle 10g | `org.hibernate.dialect.Oracle10gDialect` |
| Microsoft SQL Server | `org.hibernate.dialect.SQLServerDialect` |
| DB2 | `org.hibernate.dialect.DB2Dialect` |
| PostgreSQL | `org.hibernate.dialect.PostgreSQLDialect` |
| Sybase | `org.hibernate.dialect.SybaseDialect` |

---

## 7.7. Tam `AppConfig.java` — Veritabanı Bağlantı Konfigürasyonu

**Paket:** `tr.edu.duzce.mf.bm.bm470.config`

Spring 4+ için annotation'lar kullanılarak veritabanı bağlantısı konfigüre edilebilir. Dersten tam hâli:

```java
@PropertySource(value = "classpath:hibernate.properties", encoding = "UTF-8")
@EnableTransactionManagement
@Configuration
@ComponentScan(basePackages = {"tr.edu.duzce"})   // veya:
@ComponentScans(value = {
    @ComponentScan("tr.edu.duzce.mf.bm.bm470.service"),
    @ComponentScan("tr.edu.duzce.mf.bm.bm470.dao")
})
public class AppConfig {

    @Autowired
    private Environment env;

    @Bean
    public LocalSessionFactoryBean getSessionFactory() {
        LocalSessionFactoryBean factoryBean = new LocalSessionFactoryBean();
        Properties props = new Properties();

        // JDBC ayarları
        props.put(DRIVER,          env.getProperty("mysql.driver"));
        props.put(URL,             env.getProperty("mysql.url"));
        props.put(USER,            env.getProperty("mysql.user"));
        props.put(PASS,            env.getProperty("mysql.password"));

        // Hibernate ayarları
        props.put(SHOW_SQL,        env.getProperty("hibernate.show_sql"));
        props.put(HBM2DDL_AUTO,    env.getProperty("hibernate.hbm2ddl.auto"));
        props.put(DIALECT,         env.getProperty("hibernate.dialect"));
        props.put(DEFAULT_SCHEMA,  env.getProperty("hibernate.default_schema"));

        // c3p0 ayarları
        props.put(C3P0_MIN_SIZE,           env.getProperty("hibernate.c3p0.min_size"));
        props.put(C3P0_MAX_SIZE,           env.getProperty("hibernate.c3p0.max_size"));
        props.put(C3P0_ACQUIRE_INCREMENT,  env.getProperty("hibernate.c3p0.acquire_increment"));
        props.put(C3P0_TIMEOUT,            env.getProperty("hibernate.c3p0.timeout"));
        props.put(C3P0_MAX_STATEMENTS,     env.getProperty("hibernate.c3p0.max_statements"));
        props.put(C3P0_CONFIG_PREFIX + ".initialPoolSize",
                                            env.getProperty("hibernate.c3p0.initialPoolSize"));

        factoryBean.setHibernateProperties(props);

        // Model sınıfları (varargs parametresi)
        factoryBean.setAnnotatedClasses(Ogrenci.class, Bolum.class, Fakulte.class, Danisman.class);

        return factoryBean;
    }

    @Bean
    public HibernateTransactionManager getTransactionManager() {
        HibernateTransactionManager transactionManager = new HibernateTransactionManager();
        transactionManager.setSessionFactory(getSessionFactory().getObject());
        return transactionManager;
    }
}
```

**Kritik detaylar:**
- `DRIVER`, `URL`, `USER`, `PASS`, `SHOW_SQL`, `HBM2DDL_AUTO`, `DIALECT`, `DEFAULT_SCHEMA`, `C3P0_*` sabitlerinin tamamı `org.hibernate.cfg.AvailableSettings` **arayüzünde tanımlanan sabit anahtarlar**'dır. Yani `import static org.hibernate.cfg.AvailableSettings.*;` ile geliyor.
- `setAnnotatedClasses(...)` metoduna **tüm entity sınıfları** varargs olarak verilir.
- `@PropertySource(encoding = "UTF-8")` → Türkçe karakter sorunu yaşanmaması için **şart**.

---

## 7.8. Spring MVC Proje Dizin Yapısı (Dersten Görsel — Çok Önemli)

```
src/
└── main/
    ├── java/
    │   └── tr/edu/duzce/mf/bm/bm470/
    │       ├── config/            ← AppConfig.java burada
    │       ├── dao/               ← DAO sınıfları burada
    │       ├── exception/         ← Exception sınıfları
    │       ├── interceptor/       ← Interceptor'lar (4-6. ders notlarında olabilir)
    │       ├── model/             ← Hibernate Entity sınıfları (Ogrenci, Bolum, ...)
    │       ├── service/           ← Service sınıfları
    │       └── web/               ← Controller sınıfları
    └── resources/
        ├── hibernate.properties   ← Veritabanı konfig
        └── Resource Bundle 'messages'
            ├── messages_en_US.properties   ← i18n: İngilizce
            └── messages_tr_TR.properties   ← i18n: Türkçe (zorunlu)

webapp/
├── app/
├── lib/
├── resources/
└── WEB-INF/
    └── view/                      ← JSP view'ları
```

**Proje dokümanındaki gereksinimlerle eşleşme:**
- ✅ `config` — annotation-based config için
- ✅ `interceptor` — her istekte loglama için (ilk3pdf.md gereksinim 4)
- ✅ `messages_tr_TR.properties` + `messages_en_US.properties` — i18n (gereksinim 6)

---

## 7.9. Hibernate Entity (POJO)

### 7.9.1. POJO Tanımı

- **POJO = Plain Old Java Object**.
- `Serializable` arayüzünü implemente eden basit (**başka bir sınıftan türememiş**) Java sınıfları.
- Veritabanı tablolarının Java sınıfları olarak denkliğidir.

### 7.9.2. Temel Entity Annotation'ları

#### `@Entity`
Spring'e bu sınıfın bir veritabanı tablosunu temsil ettiğini belirtir.

#### `@Table`
Sınıfın temsil ettiği veritabanı tablo ismi (ve varsa şema adı).
- `name` → tablo adı
- `schema` → şema adı (örneğin `@Table(name = "ogrenci", schema = "obs")`)

#### `@Column`
Sınıf alanının (field) temsil ettiği veritabanı tablosu sütun bilgileri.

| Özellik | Anlam | Default |
|---------|-------|---------|
| `name` | Sütun adı | İlgili sınıf alan (field) adı |
| `nullable` | Sütunun boş olup olamayacağı | `true` |
| `unique` | Sütunun unique değer olup olmadığı | `false` |
| `insertable` | Sütunun insert kapsamında veritabanına eklenip eklenmeyeceği | `true` |
| `updatable` | Sütunun update kapsamında veritabanına güncellenip güncellenmeyeceği | `true` |
| `length` | Sütunun değer uzunluğu (metinsel tipler için) | `255` |
| `precision` | Sütunun toplam basamak sayısı | - |
| `scale` | Sütunun ondalık kısım basamak sayısı | - |

#### `@Id`
Tanımlandığı field'ın veritabanı tablosunun **birincil anahtarını** temsil ettiğini belirtir.

#### `@GeneratedValue`
Tanımlandığı field değerinin Hibernate tarafından üretilen bir değer olduğunu belirtir.

**`strategy` parametresi değerleri:**

| Değer | Davranış |
|-------|----------|
| `GenerationType.AUTO` | **Varsayılan**. Hibernate'in veritabanına bağlı olarak otomatik olarak değer üretme stratejisinin seçilmesini sağlar. |
| `GenerationType.IDENTITY` | Birincil anahtar sütuna veritabanı tarafından değer atanması yöntemidir. |
| `GenerationType.SEQUENCE` | Tanımlanan Sequence Generator ile değer üretilmesi sağlanır. |
| `GenerationType.TABLE` | Tanımlanan Table Generator ile değerin veritabanında bir tablo aracılığıyla üretilmesi sağlanır. **Tavsiye edilmeyen, yaygın kullanılmayan bir yöntemdir.** |

#### `@Temporal`
Tarih-zaman nesnelerinin içerik türünü belirtir.

| Değer | Anlam |
|-------|-------|
| `TemporalType.DATE` | Field'ın **tarih** bilgisi içereceğini belirtir. |
| `TemporalType.TIME` | Field'ın **zaman** bilgisi içereceğini belirtir. |
| `TemporalType.TIMESTAMP` | Field'ın **hem tarih hem de zaman** bilgisi içereceğini belirtir. |

#### `@Transient`
Tanımlandığı field'ın bir sütunu temsil etmediğini, **sadece ilgili Java sınıfı tarafından kullanıldığını** belirtir. Yani veritabanına kaydedilmez.

### 7.9.3. Hibernate SQL ↔ Java Tip Eşleştirmeleri

| SQL Veri Tipi | Java Nesne Tipi |
|---------------|-----------------|
| BIT | Boolean |
| TINYINT | Byte |
| CHAR | Character |
| DOUBLE | Double |
| FLOAT | Float |
| INTEGER | Integer |
| BIGINT | Long |
| SMALLINT | Short |
| VARCHAR, TEXT | String |
| NUMERIC | BigDecimal |
| VARBINARY | Byte[] |

**Genel SQL veri tipleri kategorileri (dersten):**
- **Character/String:** char, varchar
- **Numeric:** bit, int, smallint, tinyint, decimal, numeric, float, real
- **Binary:** binary, varbinary, varbinary max, year
- **Date and Time:** date, time, datetime, timestamp, year
- **Unicode Character:** vchar, nvarchar, nvarchar max, ntext
- **Miscellaneous:** clob, blob, xml, json

### 7.9.4. Örnek Hibernate Entity (Lombok'suz)

`ogrenci` tablosu şeması:

| id | adi | soyadi | dogum_tarihi | sinifi |
|----|-----|--------|--------------|--------|
| 1 | Ali | Veli | 02.02.1995 | 2 |

```java
@Entity
@Table(name = "ogrenci")
public class Ogrenci implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "adi", nullable = false)
    private String adi;

    @Column(name = "soyadi", nullable = false)
    private String soyadi;

    @Column(name = "dogum_tarihi", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date dogumTarihi;

    @Column(name = "sinifi", nullable = false)
    private Integer sinifi;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getAdi() { return adi; }
    public void setAdi(String adi) { this.adi = adi; }
    public String getSoyadi() { return soyadi; }
    public void setSoyadi(String soyadi) { this.soyadi = soyadi; }
    // diger getter-setter metotlar...
}
```

---

## 7.10. Project Lombok — Boilerplate'in Azaltılması

### 7.10.1. Lombok Nedir?

- **Standart kodları (boilerplate) annotation'lar ile otomatik oluşturmayı** sağlayan açık kaynak kodlu kütüphane.
- Repo: `https://github.com/projectlombok/lombok`

### 7.10.2. Lombok Annotation'ları (Derste Geçen)

| Annotation | Görevi |
|------------|--------|
| `@Getter` | İlgili alana yönelik **getter** metodunun oluşturulmasını sağlar. |
| `@Setter` | İlgili alana yönelik **setter** metodunun oluşturulmasını sağlar. |
| `@NoArgsConstructor` | Varsayılan (parametresiz) yapıcı metodun oluşturulmasını sağlar. |
| `@AllArgsConstructor` | Tüm alanları parametre alan yapıcı metodun oluşturulmasını sağlar. |
| `@RequiredArgsConstructor` | `final` ve `@NonNull` olarak tanımlanmış alanları içeren yapıcı metodun oluşturulmasını sağlar. |
| `@ToString` | `toString()` metodunu ezerek nesneye özel hale getirir. |
| `@Builder` | Sınıfa **Builder tasarım desenini** uygular. |

### 7.10.3. Lombok Maven Bağımlılığı

```xml
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <version>1.18.42</version>
    <scope>provided</scope>
</dependency>
```

**Not:** `scope = provided` — derleme ve geliştirme sırasında var, runtime'da değil.

### 7.10.4. Lombok'lu vs Lombok'suz Karşılaştırma

Aynı Ogrenci entity:
- **Lombok'suz:** 48 satır
- **Lombok'lu:** 23 satır
- **%52 azaltma**

---

## 7.11. Örneklerde Kullanılan Veritabanı E-R Diyagramı (Tüm PDF 7-8'de Tekrar Kullanılıyor)

Dersteki örneklerin tamamında kullanılan şema:

```
┌───────────────────┐         ┌────────────────────┐
│      fakulte      │         │      ogrenci       │
├───────────────────┤         ├────────────────────┤
│ 🔑 fakulte_id     │◄────┐   │ 🔑 ogrenci_id      │
│ fakulte_adi       │     │   │ adi                │
└───────────────────┘     │   │ soyadi             │
                          │   │ dogum_tarihi       │
┌───────────────────┐     │   │ gpa                │
│      bolum        │─────┘   │ ogrenci_no         │
├───────────────────┤         │ sinifi             │
│ 🔑 bolum_id       │◄────┐   │ 🔗 bolum_id        │────┘
│ bolum_adi         │     │   │ 🔗 danisman_id     │────┐
│ 🔗 fakulte_id     │     │   └────────────────────┘    │
└───────────────────┘     │                             │
                          │   ┌────────────────────┐    │
                          └───│      danisman      │◄───┘
                              ├────────────────────┤
                              │ 🔑 danisman_id     │
                              │ adi                │
                              │ soyadi             │
                              │ 🔗 bolum_id        │
                              └────────────────────┘
```

**Tablolar ve kolonlar:**
- `fakulte`: fakulte_id (bigint, PK), fakulte_adi (varchar 255)
- `bolum`: bolum_id (bigint, PK), bolum_adi (varchar 255), fakulte_id (bigint, FK)
- `ogrenci`: ogrenci_id (bigint, PK), adi (varchar 255), soyadi (varchar 255), dogum_tarihi (date), gpa (double), ogrenci_no (bigint), sinifi (int), bolum_id (FK), danisman_id (FK)
- `danisman`: danisman_id (bigint, PK), adi (varchar 255), soyadi (varchar 255), bolum_id (FK)

## 7.12. Lombok'lu Tam Entity Örnekleri

**Paket:** `tr.edu.duzce.mf.bm.bm470.model`

### 7.12.1. `Ogrenci.java`

```java
@Entity
@Table(name = "ogrenci")
@NoArgsConstructor
@AllArgsConstructor
public class Ogrenci implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ogrenci_id", nullable = false)
    @Getter @Setter
    private Long ogrenciId;

    @Column(name = "ogrenci_no", nullable = false, unique = true)
    @Getter @Setter
    private Long ogrenciNo;

    @Column(name = "adi", nullable = false)
    @Getter @Setter
    private String adi;

    @Column(name = "soyadi", nullable = false)
    @Getter @Setter
    private String soyadi;

    @Column(name = "dogum_tarihi", nullable = false)
    @Temporal(TemporalType.DATE)
    @Getter @Setter
    private Date dogumTarihi;

    @ManyToOne
    @JoinColumn(name = "bolum_id", referencedColumnName = "bolum_id", nullable = false)
    @Getter @Setter
    private Bolum bolum;

    @ManyToOne
    @JoinColumn(name = "danisman_id", referencedColumnName = "danisman_id", nullable = true)
    @Getter @Setter
    private Danisman danisman;

    @Column(name = "sinifi", nullable = false)
    @Getter @Setter
    private Integer sinifi;

    @Column(name = "gpa", nullable = false)
    @Getter @Setter
    private Double gpa;
}
```

### 7.12.2. `Bolum.java` (Entity'ler Arası İlişki I)

```java
@Entity
@Table(name = "bolum")
@NoArgsConstructor
public class Bolum implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bolum_id", nullable = false)
    @Getter @Setter
    private Long bolumId;

    @Column(name = "bolum_adi", nullable = false)
    @Getter @Setter
    private String bolumAdi;

    @ManyToOne
    @JoinColumn(name = "fakulte_id", referencedColumnName = "fakulte_id")
    @Getter @Setter
    private Fakulte fakulte;
}
```

**İlişki:** Her bölümün bir fakültesi var → **`@ManyToOne`** (bolum → fakulte).

### 7.12.3. `Fakulte.java` (Entity'ler Arası İlişki II)

```java
@Entity
@Table(name = "fakulte")
@NoArgsConstructor
public class Fakulte implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fakulte_id", nullable = false)
    @Getter @Setter
    private Long fakulteId;

    @Column(name = "fakulte_adi", nullable = false)
    @Getter @Setter
    private String fakulteAdi;

    @OneToMany(
        mappedBy = "fakulte",
        fetch = FetchType.LAZY,          // Varsayılan değer
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    @Getter @Setter
    private List<Bolum> bolumList;
}
```

**İlişki:** Fakültelerin bölümleri var → **`@OneToMany`** (fakulte → bolum listesi). `mappedBy = "fakulte"` ile `Bolum`'daki `fakulte` field'ına eşleniyor.

### 7.12.4. İlişki Annotation'larının Özeti

| Annotation | Açıklama | Örnek |
|-----------|----------|-------|
| `@ManyToOne` | Çoktan-bire ilişki | `Ogrenci`→`Bolum`, `Bolum`→`Fakulte` |
| `@OneToMany` | Birden-çoka ilişki | `Fakulte`→`List<Bolum>` |
| `@JoinColumn` | FK kolonunu tanımlar | `name`, `referencedColumnName`, `nullable` |
| `FetchType.LAZY` | Gerektiğinde yükle (varsayılan) | `@OneToMany` için |
| `FetchType.EAGER` | Anında yükle | - |
| `CascadeType.ALL` | Parent işlemleri (persist/merge/remove) çocuklara yansısın | - |
| `orphanRemoval = true` | Listeden çıkarılan child otomatik silinsin | - |

---

# 📘 DERS NOTU 8: Hibernate Criteria Query API

> **Kapak:** _Hibernate Criteria Query API — Doç. Dr. Talha KABAKUŞ_
> **32 slayt.** Tüm örnekler 7. ders notundaki E-R diyagramını (ogrenci, bolum, fakulte, danisman) kullanıyor.

## 8.1. CriteriaBuilder Nedir ve Neden Kullanılıyor?

- **SQL ya da HQL komutları yerine Java nesne ve metotlarının kullanılmasını sağlar.**
- `Session` arayüzünden **`getCriteriaBuilder()`** isimli metod ile elde edilen `CriteriaBuilder` nesnesi ile **SQL sorgularını nesne tabanlı olarak** tanımlanması sağlanır.
- Her bir SQL kısıtını temsil etmek için `CriteriaBuilder` nesnesinin sorgu metotlarından elde edilen **`Predicate`** nesneleri kullanılır. Bu nesneler `where` metoduyla beraber kullanılır.

### 8.1.1. Temel Kalıp (DAO Sınıfı Metod Gövdesi)

```java
Session session = sessionFactory.getCurrentSession();
CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
CriteriaQuery<Ogrenci> criteriaQuery = criteriaBuilder.createQuery(Ogrenci.class);
Root<Ogrenci> root = criteriaQuery.from(Ogrenci.class);

// (1) Predicate nesneleri

criteriaQuery.select(root).where(predicate);    // (2) select + where

Query<Ogrenci> query = session.createQuery(criteriaQuery);   // (3) Query oluştur

// (4) query üzerinden sonuçların elde edilmesi
List<Ogrenci> ogrenciListesi = query.getResultList();
```

**Adımlar:**
1. `CriteriaBuilder` elde et.
2. `CriteriaQuery<T>` oluştur.
3. `Root<T>` tanımla.
4. `Predicate` kur ve `where` ile uygula.
5. `Query<T>` elde et ve sonucu çek.

---

## 8.2. CriteriaBuilder Sorgu Metotları — Tam Tablo

### 8.2.1. Karşılaştırma ve Mantıksal Operatörler

| CriteriaBuilder Metodu | SQL Karşılığı |
|------------------------|---------------|
| `equal()` | `==` |
| `notEqual()` | `!=` |
| `greaterThanOrEqualTo()` | `>=` |
| `greaterThan()` | `>` |
| `lessThanOrEqualTo()` | `<=` |
| `lessThan()` | `<` |
| `isNull()` | `is null` |
| `isNotNull()` | `is not null` |
| `between()` | `between ? and ?` |
| `and()` | `and` |
| `or()` | `or` |
| `in()` | `in` |
| `not()` | `!` |

### 8.2.2. Predicate Kalıbı

```java
Predicate predicate = criteriaBuilder.ilgili_metod([parametreler]);
criteriaQuery.select(root).where(predicate);
```

---

## 8.3. CriteriaQuery Örnekleri (I-VIII)

### 8.3.1. Örnek I — `equal` (İsmi Ali olan öğrenciler)

```java
Session session = getCurrentSession();
CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
CriteriaQuery<Ogrenci> criteriaQuery = criteriaBuilder.createQuery(Ogrenci.class);
Root<Ogrenci> root = criteriaQuery.from(Ogrenci.class);

Predicate predicateAd = criteriaBuilder.equal(root.get("adi"), "Ali");
//                                             └─ property    └─ value
criteriaQuery.select(root).where(predicateAd);

Query<Ogrenci> query = session.createQuery(criteriaQuery);
List<Ogrenci> ogrenciListesi = query.getResultList();
```

### 8.3.2. DAO Metoduna Dönüştürme

```java
public List<Ogrenci> ogrencileriGetir(String ad) {
    Session session = getCurrentSession();
    CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
    CriteriaQuery<Ogrenci> criteriaQuery = criteriaBuilder.createQuery(Ogrenci.class);
    Root<Ogrenci> root = criteriaQuery.from(Ogrenci.class);
    Predicate predicateAd = criteriaBuilder.equal(root.get("adi"), ad);
    criteriaQuery.select(root).where(predicateAd);
    Query<Ogrenci> query = session.createQuery(criteriaQuery);
    return query.getResultList();
}
```

### 8.3.3. Uçtan Uca Controller-Service-DAO Örneği

**Controller:**
```java
@PostMapping(value = "/ogrencileriGetir.ajax")
public @ResponseBody String ogrencileriGetir(@RequestParam(name = "ad") String ad) {
    List<Ogrenci> ogrenciListesi = ogrenciService.ogrencileriGetir(ad);
    return JSONArray.fromObject(ogrenciListesi).toString();
}
```

**Service:**
```java
public List<Ogrenci> ogrencileriGetir(String ad) {
    return ogrenciDAO.ogrencileriGetir(ad);
}
```

**DAO:**
```java
public List<Ogrenci> ogrencileriGetir(String ad) {
    // Bir önceki slayttaki metot gövdesi
}
```

**Not:** Controller'da `@PostMapping`, `@ResponseBody`, `@RequestParam` kullanılıyor — bu annotation'lar muhtemelen 4-6. ders notlarında detaylı anlatılmıştır.

### 8.3.4. Örnek II — Tüm öğrencilerin isim listesi (where yok, property projection)

```java
CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
CriteriaQuery<String> criteriaQuery = criteriaBuilder.createQuery(String.class);
Root<Ogrenci> root = criteriaQuery.from(Ogrenci.class);
criteriaQuery.select(root.get("adi"));            // where şartı yok!
Query<String> query = session.createQuery(criteriaQuery);
List<String> isimListesi = query.getResultList();
```

**Dikkat:** `createQuery(String.class)` — çünkü sadece isim (String) geliyor.

### 8.3.5. Örnek III — Tek sonuç (`getSingleResult`)

```java
CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
CriteriaQuery<Ogrenci> criteriaQuery = criteriaBuilder.createQuery(Ogrenci.class);
Root<Ogrenci> root = criteriaQuery.from(Ogrenci.class);
Predicate predicateOgrNo = criteriaBuilder.equal(root.get("ogrenciNo"), 123);
criteriaQuery.select(root).where(predicateOgrNo);
Query<Ogrenci> query = session.createQuery(criteriaQuery);
Ogrenci ogrenci = query.getSingleResult();        // T döndürür
```

**⚠️ UYARI (dersten):** Bulunamaması veya birden çok sonuç bulunması durumunda **Exception fırlatır**. Bu sebeple bu durumlar ihtimal dahilinde ise **try-catch** kapsamında çağrılması gerekir.

### 8.3.6. Örnek IV — `or` (İsmi veya soyismi Yılmaz)

```java
CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
CriteriaQuery<Ogrenci> criteriaQuery = criteriaBuilder.createQuery(Ogrenci.class);
Root<Ogrenci> root = criteriaQuery.from(Ogrenci.class);

Predicate predicateAd = criteriaBuilder.equal(root.get("adi"), "Yılmaz");
Predicate predicateSoyad = criteriaBuilder.equal(root.get("soyadi"), "Yılmaz");

criteriaQuery.select(root).where(criteriaBuilder.or(predicateAd, predicateSoyad));

Query<Ogrenci> query = session.createQuery(criteriaQuery);
List<Ogrenci> ogrenciListesi = query.getResultList();
```

### 8.3.7. String Eşleşme Modları (MatchMode)

| Eşleşme Yeri | Arama Deseni | Sorgu Metodu |
|--------------|-------------|--------------|
| **Baş** | `deger + "%"` | `like` |
| **Son** | `"%" + deger` | `like` |
| **Birebir** | `deger` | `equal` |
| **Herhangi bir yer** | `"%" + value + "%"` | `like` |

> **Dersten:** String eşleşme modları ile kurulan sorgularda, `CriteriaBuilder`'ın `equal` metodu **değil**; **`like`** metodu kullanılmalıdır.

### 8.3.8. Örnek V — `like` (İsminde Ali geçen)

```java
CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
CriteriaQuery<Ogrenci> criteriaQuery = criteriaBuilder.createQuery(Ogrenci.class);
Root<Ogrenci> root = criteriaQuery.from(Ogrenci.class);
Predicate predicateAd = criteriaBuilder.like(root.get("adi"), "%Ali%");
criteriaQuery.select(root).where(predicateAd);
Query<Ogrenci> query = session.createQuery(criteriaQuery);
List<Ogrenci> ogrenciListesi = query.getResultList();
```

### 8.3.9. Örnek VI — Büyük-küçük harf duyarsız (case-insensitive) sorgu

"Ali" veya "ali" farketmiyorsa:

1. **String sınıfının** `toLowerCase(Locale)` ve `toUpperCase(Locale)` üye metotları kullanılarak ilgili değişken iki formda da elde edilir.
2. **Veritabanında ilgili alanın iki formdaki değerleri**, `CriteriaBuilder` arayüzünün `upper()` ve `lower()` metotlarıyla elde edilir.
3. İlgili kriterlerin **her biri büyük-küçük harf duyarsız** sorgulamayı gerçekleştirir.

```java
// Türkçe locale — veya Locale.ENGLISH, Locale.GERMAN gibi
Predicate predicateBuyuk = criteriaBuilder.equal(
    criteriaBuilder.upper(root.get("adi")),
    adi.toUpperCase(new Locale("tr", "TR"))
);

Predicate predicateKucuk = criteriaBuilder.equal(
    criteriaBuilder.lower(root.get("adi")),
    adi.toLowerCase(new Locale("tr", "TR"))
);

criteriaQuery.select(root).where(predicateBuyuk);   // veya predicateKucuk
```

Locale parametreleri referansı: `https://docs.oracle.com/javase/tutorial/i18n/locale/create.html`

### 8.3.10. Örnek VII — `isNotNull` (Danışmanı olan öğrenciler)

```java
CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
CriteriaQuery<Ogrenci> criteriaQuery = criteriaBuilder.createQuery(Ogrenci.class);
Root<Ogrenci> root = criteriaQuery.from(Ogrenci.class);
Predicate predicateDanisman = criteriaBuilder.isNotNull(root.get("danisman"));
criteriaQuery.select(root).where(predicateDanisman);
Query<Ogrenci> query = session.createQuery(criteriaQuery);
List<Ogrenci> ogrenciListesi = query.getResultList();
```

### 8.3.11. NULL ≠ "" — Boş Kontrolü Ayrımı

| Amaç | Metod |
|------|-------|
| NULL kontrolü | `criteriaBuilder.isNull()` veya `criteriaBuilder.isNotNull()` |
| Boş String ("") kontrolü | `criteriaBuilder.isEmpty()` veya `criteriaBuilder.isNotEmpty()` |

**Denklikler:**
- `criteriaBuilder.isNotNull()` ≡ `criteriaBuilder.not(criteriaBuilder.isNull())`
- `criteriaBuilder.isNotEmpty()` ≡ `criteriaBuilder.not(criteriaBuilder.isEmpty())`

### 8.3.12. `.not()` Kullanımının Denklikleri

| `.not()` Kullanımı | Doğrudan Metod |
|--------------------|----------------|
| `equal(...).not()` | `notEqual(...)` |
| `isNull(...).not()` | `isNotNull(...)` |
| `lessThan(...).not()` | `greaterThanOrEqualTo(...)` |
| `greaterThan(...).not()` | `lessThanOrEqualTo(...)` |
| `lessThanOrEqualTo(...).not()` | `greaterThan(...)` |

### 8.3.13. Örnek VIII — `in` (2. veya 3. sınıftaki öğrenciler)

```java
CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
CriteriaQuery<Ogrenci> criteriaQuery = criteriaBuilder.createQuery(Ogrenci.class);
Root<Ogrenci> root = criteriaQuery.from(Ogrenci.class);
Integer[] siniflar = {2, 3};
criteriaQuery.select(root).where(root.get("sinifi").in(siniflar));
Query<Ogrenci> query = session.createQuery(criteriaQuery);
List<Ogrenci> ogrenciListesi = query.getResultList();
```

---

## 8.4. CriteriaQuery `orderBy` (Sıralama)

- `CriteriaQuery` arayüzünün **`orderBy()`** metodu kullanılır.
- Metot, parametre olarak **`Order`** nesnesi alır.
- `Order` nesnesi, `criteriaBuilder` nesnesinin **`asc()`** ve **`desc()`** isimli metotlarıyla elde edilir.

```java
// Öğrencileri isim sırasına göre küçükten büyüğe (ASC)
criteriaQuery.orderBy(criteriaBuilder.asc(root.get("adi")));

// Öğrencileri isim sırasına göre büyükten küçüğe (DESC)
criteriaQuery.orderBy(criteriaBuilder.desc(root.get("adi")));

// Zincirleme kullanım:
Order order = criteriaBuilder.desc(root.get("adi"));
criteriaQuery.select(root).where(predicate).orderBy(order);
```

---

## 8.5. Sayfalama (Pagination / Paging)

### 8.5.1. Neden Sayfalama Gerekli?

Veritabanından çekilecek sorguların sayfalara bölünerek:
- **Daha hızlı** → HTTP cevabının mümkün olan **en kısa sürede** döndürülmesi önemli.
- **Daha az bellek tüketimiyle** verilerin çekilmesini sağlar.
- Veri boyutunun çok büyük olması durumunda **bellek taşması olmadan** verilerin çekilebilmesini mümkün kılar.

### 8.5.2. İki Parametre

1. **Sayfa boyutu** (Page size)
2. **Kalınan veri indeksi**

### 8.5.3. CriteriaQuery ile Sayfalama

SQL LIMIT karşılığı:

| Metod | Görevi |
|-------|--------|
| `query.setMaxResults(int n)` | Toplam `n` adet sonuç döndürür (SQL LIMIT). |
| `query.setFirstResult(int n)` | Sonuçları `n`. sonuçtan başlayarak döndürür. |

**Örnek:** 10 sonuçtan `[0, 1, 2, 3, 4, 5, 6, 7, 8, 9]` indeks 1'den başlayarak 4 tane → `[1, 2, 3, 4]` döner.

```java
Query<Ogrenci> query = session.createQuery(criteriaQuery);
query.setFirstResult(1);
query.setMaxResults(4);
```

> **Sıralama kritik:** `CriteriaQuery<T>` oluşturulduktan **sonra** `setFirstResult`/`setMaxResults` çağrılır. Kullanıcı arayüzünde ise tipik olarak "Sayfada 10/25/50/100 kayıt göster" gibi bir seçici olur.

---

## 8.6. Foreign Key Sorguları

### 8.6.1. Kural

- `String` hariç **primitive tipte olmayan (nesne tipteki)** foreign key alanlara yönelik sorgular, foreign key sınıflarına yönelik oluşturulan **`Root`** nesneleri ile yapılır.
- **İki farklı `Root` nesnesinin** ilgili alanlarına yönelik sorgular oluşturulur.
- Sonuçlarda **çift veri (duplikasyon) olmaması için** `CriteriaQuery` arayüzünün `distinct(true)` metodu çağrılır.
- İstenirse **alias'lar (kısa adlar)** tanımlanarak sorgular sadeleştirilebilir.

### 8.6.2. Many-To-One FK Yapısı (Hatırlatma)

```
ogrenci                       bolum
├── ogrenci_id (PK)           ├── bolum_id (PK)
├── adi                       ├── bolum_adi
├── soyadi                    └── fakulte_id
├── dogum_tarihi
├── sinifi
├── bolum_id (FK) ────────────┘
└── danisman_id (FK)

public class Ogrenci {                public class Bolum {
    ...                                   private Long bolumId;
    private Bolum bolum;                  ...
    ...                               }
}
```

### 8.6.3. Örnek — Danışmanı olan öğrenciler (Foreign Key Sorgusu)

```java
CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
CriteriaQuery<Ogrenci> criteriaQuery = criteriaBuilder.createQuery(Ogrenci.class);

Root<Ogrenci>  ogrenciRoot  = criteriaQuery.from(Ogrenci.class);
Root<Danisman> danismanRoot = criteriaQuery.from(Danisman.class);

Predicate predicateDanisman = criteriaBuilder.equal(
    ogrenciRoot.get("danisman").get("danismanId"),
    danismanRoot.get("danismanId")
);

criteriaQuery.select(ogrenciRoot).where(predicateDanisman);
criteriaQuery.distinct(true);   // tekil sonuç elde etmek için

Query<Ogrenci> query = session.createQuery(criteriaQuery);
List<Ogrenci> ogrenciListesi = query.getResultList();
return ogrenciListesi;
```

### 8.6.4. Alias'lı Çözüm

```java
ogrenciRoot.get("danisman").alias("danisman");
criteriaBuilder.equal(
    ogrenciRoot.get("danisman.danismanId"),
    danismanRoot.get("danismanId")
);
```

---

## 8.7. Projeksiyonlar (Aggregate + Property)

### 8.7.1. Projeksiyon Hiyerarşisi

Derste verilen ağaç yapısı:

```
Projection
├── EnhancedProjection
└── SimpleProjection
    ├── AggregateProjection
    │   └── CountProjection
    │       ├── count()
    │       ├── countDistinct()
    │       ├── min()
    │       ├── max()
    │       ├── avg()
    │       └── sum()
    └── PropertyProjection
        ├── select()
        ├── multiselect()
        └── groupBy()
```

### 8.7.2. Projeksiyon Metotları Tablosu

`CriteriaQuery` üzerinden veri çekme işleminde (`select`) uygulanır.

| Projection | Açıklama |
|-----------|----------|
| `count()` | Sonuç listesindeki alan sayısı |
| `countDistinct()` | Sonuç listesindeki tekil ilgili alan sayısı |
| `sum()` | Sonuç listesindeki ilgili alanın toplamı |
| `avg()` | Sonuç listesindeki ilgili alanın ortalaması |
| `min()` | Sonuç listesindeki ilgili alanın minimum değeri |
| `max()` | Sonuç listesindeki ilgili alanın maksimum değeri |
| `select()` | Sonuçlardaki çekilecek alan projeksiyonu |
| `multiselect()` | Sonuçlardaki çekilecek alanlar projeksiyonu |
| `groupBy()` | Sonuçları gruplama koşulu |

### 8.7.3. `count / countDistinct` Projeksiyonu

- Geri dönüş tipi: **`Long`**
- Metod: `criteriaBuilder.count(root)` / `criteriaBuilder.countDistinct(root)`

**Örnek — 1. sınıftaki öğrenci sayısı:**
```java
CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
Root<Ogrenci> root = criteriaQuery.from(Ogrenci.class);
Predicate predicateSinif = criteriaBuilder.equal(root.get("sinifi"), 1);
criteriaQuery.select(criteriaBuilder.count(root)).where(predicateSinif);
// veya:   criteriaBuilder.countDistinct(root)
Query<Long> query = session.createQuery(criteriaQuery);
Long adet = query.getSingleResult();
```

### 8.7.4. `sum` Projeksiyonu

- İlgili **sayısal alana** yönelik toplam değeri döndürür.
- Geri dönüş türü tanımlı numerik veri türüne göre belirlenir.

**Örnek — Öğrenci sınıflarının toplamı:**
```java
CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
CriteriaQuery<Integer> criteriaQuery = criteriaBuilder.createQuery(Integer.class);
Root<Ogrenci> root = criteriaQuery.from(Ogrenci.class);
criteriaQuery.select(criteriaBuilder.sum(root.get("sinifi")));
// Devamına where() sorgusu eklenebilir.
Query<Integer> query = session.createQuery(criteriaQuery);
Integer toplam = query.getSingleResult();
```

### 8.7.5. `avg` Projeksiyonu

- İlgili sayısal alana yönelik **ortalama** değeri döndürür.
- **Geri dönüş türü: `Double`**

```java
CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
CriteriaQuery<Double> criteriaQuery = criteriaBuilder.createQuery(Double.class);
Root<Ogrenci> root = criteriaQuery.from(Ogrenci.class);
criteriaQuery.select(criteriaBuilder.avg(root.get("sinifi")));
Query<Double> query = session.createQuery(criteriaQuery);
Double ortalama = query.getSingleResult();
```

### 8.7.6. `min` Projeksiyonu

```java
CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
CriteriaQuery<Integer> criteriaQuery = criteriaBuilder.createQuery(Integer.class);
Root<Ogrenci> root = criteriaQuery.from(Ogrenci.class);
criteriaQuery.select(criteriaBuilder.min(root.get("sinifi")));
Query<Integer> query = session.createQuery(criteriaQuery);
Integer min = query.getSingleResult();
```

### 8.7.7. `max` Projeksiyonu

```java
CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
CriteriaQuery<Integer> criteriaQuery = criteriaBuilder.createQuery(Integer.class);
Root<Ogrenci> root = criteriaQuery.from(Ogrenci.class);
criteriaQuery.select(criteriaBuilder.max(root.get("sinifi")));
Query<Integer> query = session.createQuery(criteriaQuery);
Integer max = query.getSingleResult();
```

### 8.7.8. Property Projection (Tek alan — `select` + `groupBy`)

- Nesnenin tamamı değil, **ilgili alanın** elde edilmesi.
- SQL karşılığı: `select(property)`.
- `criteriaQuery.groupBy(...)` metodu ile sonuçlar ilgili alan üzerinde **gruplandırılabilir**. SQL karşılığı: `GROUP BY`.

```java
CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
CriteriaQuery<String> criteriaQuery = criteriaBuilder.createQuery(String.class);
Root<Ogrenci> root = criteriaQuery.from(Ogrenci.class);
criteriaQuery.select(root.get("adi"));
// İsterseniz:   criteriaQuery.groupBy(root.get("sinifi"));
Query<String> query = session.createQuery(criteriaQuery);
List<String> isimListesi = query.getResultList();
return isimListesi;
```

### 8.7.9. Multiproperty Projection (Çok alan — `multiselect`)

- Nesnenin tamamı değil, **ilgili alanlarının** elde edilmesi.
- SQL karşılığı: `select(property1, property2, ...)`

**Örnek — Öğrencilerin isim ve soy isimleriyle elde edilmesi:**
```java
CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
CriteriaQuery<Ogrenci> criteriaQuery = criteriaBuilder.createQuery(Ogrenci.class);
Root<Ogrenci> root = criteriaQuery.from(Ogrenci.class);
criteriaQuery.multiselect(root.get("adi"), root.get("soyadi"));
Query<Ogrenci> query = session.createQuery(criteriaQuery);
List<Ogrenci> ogrenciListesi = query.getResultList();
return ogrenciListesi;
```

**⚠️ KRİTİK DETAY:** `multiselect` için **ilgili alanları içeren yapıcı metodun** entity'de sunulması gerekmektedir:

```java
public Ogrenci(String adi, String soyadi) {
    this.adi = adi;
    this.soyadi = soyadi;
}
```

Bu yüzden Lombok kullanırken `@AllArgsConstructor` yeterli olmayabilir, ayrıca **partial constructor** eklenmesi gerekebilir.

---

# 📘 DERS NOTU 9: Spring Log ile Loglama

> **Kapak:** _Spring Log ile Loglama — Doç. Dr. Talha KABAKUŞ_
> **19 slayt.** Proje gereksinimlerindeki **"log4j + slf4j ile konsol + dosya sistemine loglama"** zorunluluğunun teknik karşılığı.

## 9.1. Neden Loglama Önemlidir?

### 9.1.1. Yazılımların Monitor Edilmesi
- **Yazılım kaynak kullanımı:**
  - CPU, RAM, fiziksel disk kullanımı
  - JVM durum bilgisi, oturum bilgileri gibi

### 9.1.2. Yazılım Olaylarının Kayıt Altına Alınması
- Takibi
- Bilgilendirme mekanizması

### 9.1.3. Yasal Zorunluluklar — 5651 No'lu Kanun
> **"İNTERNET ORTAMINDA YAPILAN YAYINLARIN DÜZENLENMESİ VE BU YAYINLAR YOLUYLA İŞLENEN SUÇLARLA MÜCADELE EDİLMESİ HAKKINDA KANUN"**

Bu yüzden **dosya sistemine loglama Türkiye'de yasal bir gereklilik.** Proje dokümanındaki log gereksiniminin arkasında bu kanun var.

### 9.1.4. Log Mining & Analysis
- Örüntülerden sisteme yönelik **akıllı çıkartımlar**.
- Mimari: Birden çok uygulama (APP #1-4) → Log Veritabanı → Log Analiz Yazılımı / Log Mining.

---

## 9.2. slf4j + log4j Mimarisi

```
┌─────────────┐      ┌─────────┐      ┌────────┐      ┌───────────┐
│  Uygulama   │      │         │      │        │      │           │
│  Log        │ ───► │ slf4j   │ ───► │ Bridge │ ───► │ log4j API │
│  Çağrıları  │      │         │      │        │      │           │
└─────────────┘      └─────────┘      └────────┘      └───────────┘
```

**Mantık:** Uygulama `slf4j` API'sine yazar (facade/cephe). `slf4j`, bridge aracılığıyla altta çalışan gerçek log kütüphanesine (log4j) yönlendirir. Böylece log kütüphanesi değişse bile uygulama kodu değişmez.

---

## 9.3. Maven Bağımlılık Yapılandırması (Dersten Aynen)

```xml
<!-- Spring context — varsayılan commons-logging çakışma olmaması için exclude -->
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-context</artifactId>
    <version>${spring.framework.version}</version>
    <exclusions>
        <exclusion>
            <groupId>commons-logging</groupId>
            <artifactId>commons-logging</artifactId>
        </exclusion>
    </exclusions>
</dependency>

<!-- log4j -->
<dependency>
    <groupId>org.apache.logging.log4j</groupId>
    <artifactId>log4j</artifactId>
    <version>2.25.3</version>
</dependency>

<!-- slf4j-api -->
<dependency>
    <groupId>org.slf4j</groupId>
    <artifactId>slf4j-api</artifactId>
    <version>2.0.17</version>
</dependency>

<!-- slf4j ↔ reload4j bridge -->
<dependency>
    <groupId>org.slf4j</groupId>
    <artifactId>slf4j-reload4j</artifactId>
    <version>2.0.17</version>
</dependency>
```

**Önemli notlar:**
- `commons-logging` exclusion'ı → Spring varsayılan olarak commons-logging ile geliyor, **sınıflar arasında çakışma olmaması** için hariç bırakılıyor.
- `slf4j-reload4j` bridge'i → slf4j çağrılarını Reload4j'e (log4j 1.x fork) yönlendiriyor. Bu sayede `org.apache.log4j.*` API'si hâlâ kullanılabiliyor.

> **⚠️ ÇELİŞKİ NOTU (başta belirtildi):** `ilk3pdf.md`'de `log4j 1.2.14` + `slf4j 1.7.25` vardı. Bu ders notunda versiyonlar **2.x ailesine** güncellenmiş. Karar verilmeli.

---

## 9.4. Log Ayarlarının Spring ile Entegrasyonu

- Spring 4+ için Java tabanlı konfigürasyonda **varsayılan özellikler dosyası** olan **`log4j.properties`** dosyası **classpath'de (`resources/` klasörü altında)** tanımlanır.
- Bu dosyada şunlar tanımlanır:
  - Loglamanın hangi bileşenler ile yapılacağı
  - Logların nerede saklanacağı
  - Logların formatı
  - Loglara dahil edilecek **minimum öncelik/önem derecesi**

**Konum:** `src/main/resources/log4j.properties`

---

## 9.5. Log Desen Tasarımı — Tam Dönüşüm Karakteri Tablosu

### 9.5.1. Ana Karakterler

| Dönüşüm Karakteri | Anlamı |
|-------------------|--------|
| `c` | Kategori bilgisi (Loglayıcı bilgisi) |
| `C` | Tam tanımlanmış (fully-qualified) sınıf ismi |
| `d` | Tarih-saat bilgisi |
| `F` | Olay tetikleyici dosya ismi |
| `l` | Olay tetikleyicisi yer bilgisi (Class, metod ve satır no) |
| `L` | İlgili satır numarası |
| `m` | İlgili mesaj |
| `M` | İlgili metod adı |
| `n` | Yeni satır ayıracı |
| `p` | Öncelik bilgisi |
| `r` | Loglamanın başlangıcından itibaren geçen süre (ms) |
| `t` | İlgili thread adı |
| `x` | İlgili thread'in **NDC (Nested Diagnostic Context)** bilgisi |
| `X` | İlgili thread'in **MDC (Mapped Diagnostic Context)** bilgisi |
| `%` | İki defa yan yana yazılması durumunda **tekil `%` işareti** |

### 9.5.2. Kullanım Kuralları
- Desene **istenildiği yerde herhangi bir literal** istenildiği kadar eklenebilir.
- Her bir dönüşüm karakteri **`%` ile birlikte** belirtilir (örn. `%d`, `%p`, `%m`).

---

## 9.6. Log Seviye ve Format Ayarı

### 9.6.1. Temel Konfigürasyon (Console'a Çıktı)

```properties
log4j.rootLogger=DEBUG, stdout
log4j.appender.stdout=org.apache.log4j.ConsoleAppender
log4j.appender.stdout.Target=System.out
log4j.appender.stdout.layout=org.apache.log4j.PatternLayout
log4j.appender.stdout.layout.ConversionPattern=%d{yyyy-MM-dd HH:mm:ss} %-5p %c{1}:%L - %m%n
```

### 9.6.2. Desen Detay Analizi

Pattern: `%d{yyyy-MM-dd HH:mm:ss} %-5p %c{1}:%L - %m%n`

| Parça | Anlamı |
|-------|--------|
| `%d{yyyy-MM-dd HH:mm:ss}` | Tarih ve saat (desen: SimpleDateFormat) |
| `%-5p` | **5 karakter log seviyesi** (öncelik bilgisi), sola dayalı |
| `%c{1}` | İlgili sınıf (1 seviyede — sadece sınıf ismi, paket yok) |
| `%L` | İlgili satır numarası |
| ` - ` | Literal (araya tire) |
| `%m` | Mesaj içeriği |
| `%n` | Satır ayıracı |

### 9.6.3. Çıktı Ayarlarının Anlamı

| Ayar | Anlamı |
|------|--------|
| `log4j.rootLogger=DEBUG, stdout` | Çıktı seviye ve yöntem(ler)i |
| `log4j.appender.stdout=...ConsoleAppender` | Çıktı için kullanılacak **sınıf** |
| `log4j.appender.stdout.Target=System.out` | Çıktı hedefi |

---

## 9.7. `RollingFileAppender` — Dosyaya Loglama

- `RollingFileAppender` sınıfı ile loglar, **dönerli log dosyalarına** yazılabilir.
- Log dosyalarının yolu (`File`) tanımlanabilir.
- Dosyalar **tanımlı boyutu (`MaxFileSize`) aştığında arşivlenir** ve yeni bir log dosyası oluşturulur.
- Arşivlenecek log dosya sayısı (`MaxBackupIndex`) tanımlanabilir.
- Log'lar kaydedilirken belirtilen düzen sınıfı (`layout`) ve desen (`ConversionPattern`) kullanılarak biçimlendirilir.

```properties
log4j.appender.file=org.apache.log4j.RollingFileAppender
log4j.appender.file.File=../logs/bm470.log
log4j.appender.file.MaxFileSize=10MB
log4j.appender.file.MaxBackupIndex=10
log4j.appender.file.layout=org.apache.log4j.PatternLayout
log4j.appender.file.layout.ConversionPattern=%d{yyyy-MM-dd HH:mm:ss} %-5p %c{1}:%L - %m%n
```

**Parametre özeti:**
- `File` → Çıktı dosya yolu (`../logs/bm470.log` → Tomcat klasöründe `logs/` altına)
- `MaxFileSize=10MB` → Her dosyanın maksimum boyutu
- `MaxBackupIndex=10` → Arşivlenecek maksimum dosya sayısı (bm470.log.1, bm470.log.2, ..., bm470.log.10)

---

## 9.8. Log Öncelik Seviyeleri (slf4j API)

| Metod | Kullanım |
|-------|----------|
| `debug(String message)` | Uygulama **hata ayıklama** logları |
| `info(String message)` | **Bilgi amaçlı** loglar |
| `warn(String message)` | **Uyarı** seviyesinde loglar |
| `error(String message)` | **Hatalar** |

**Artan önem sırası:** DEBUG → INFO → WARN → ERROR.

`log4j.rootLogger=DEBUG, ...` ayarı DEBUG ve üzerini loglar. `INFO` denirse DEBUG loglanmaz.

### 9.8.1. Örnek Kullanım

```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HesapMakinesi {

    private static Logger logger = LoggerFactory.getLogger(HesapMakinesi.class);
    //                                                      └── Class nesnesi

    public int topla(int sayi1, int sayi2) {
        logger.debug(sayi1 + " + " + sayi2 + " = " + (sayi1 + sayi2));
        return sayi1 + sayi2;
    }

    public double bol(int sayi1, int sayi2) {
        if (sayi2 == 0)
            logger.error("Bölen 0 olamaz!");
        return (double) sayi1 / sayi2;
    }
}
```

**Kalıp:**
- `Logger` ve `LoggerFactory` importları **`org.slf4j.*`** paketinden.
- `private static Logger logger = LoggerFactory.getLogger(SınıfAdı.class);` — her sınıfta standart.

---

## 9.9. Tam Örnek `log4j.properties` Dosyası (Konsol + Dosya)

```properties
# Temel log ayarları
log4j.rootLogger=DEBUG, stdout, file

# Log mesajlarını console'a yazdırma
log4j.appender.stdout=org.apache.log4j.ConsoleAppender
log4j.appender.stdout.Target=System.out
log4j.appender.stdout.layout=org.apache.log4j.PatternLayout
log4j.appender.stdout.layout.ConversionPattern=%d{yyyy-MM-dd HH:mm:ss} %-5p %c{1}:%L - %m%n

# Log mesajlarını dosyaya kaydetme
log4j.appender.file=org.apache.log4j.RollingFileAppender
log4j.appender.file.File=../logs/bm470.log
log4j.appender.file.MaxFileSize=10MB
log4j.appender.file.MaxBackupIndex=10
log4j.appender.file.layout=org.apache.log4j.PatternLayout
log4j.appender.file.layout.ConversionPattern=%d{yyyy-MM-dd HH:mm:ss} %-5p %c{1}:%L - %m%n
```

**Bu dosya, proje dokümanındaki "konsola VE dosya sistemine yazılacak" gereksinimini birebir karşılıyor.**

---

## 9.10. Log Dosyasını İzleme

### 9.10.1. Unix / Linux / macOS
```bash
tail -30f bm470.log
```

### 9.10.2. Windows PowerShell
```powershell
Get-Content bm470.log -Wait -Tail 30
```

### 9.10.3. Örnek Çıktı (Dersten)

Tipik bir Tomcat başlangıç logu şuna benzer:

```
2018-01-26 10:01:22 INFO  HibernateTransactionManager:360 - Using DataSource [com.mchange.v2.c3p0.ComboPooledDataSource ...]
2018-01-26 10:01:22 INFO  ContextLoader:345 - Root WebApplicationContext: initialization completed in 2716 ms
2018-01-26 10:01:22 INFO  DispatcherServlet:489 - FrameworkServlet 'springApp': initialization started
2018-01-26 10:01:22 INFO  XmlBeanDefinitionReader:317 - Loading XML bean definitions from class path resource [app-config.xml]
2018-01-26 10:01:22 INFO  Dialect:122 - HHH000400: Using dialect: org.hibernate.dialect.MySQL5InnoDBDialect
2018-01-26 10:01:22 INFO  SchemaUpdate:182 - HHH000228: Running hbm2ddl schema update
2018-01-26 10:01:22 INFO  TableMetadata:65 - HHH000261: Table found: duzceuni.bolum
2018-01-26 10:01:22 INFO  TableMetadata:66 - HHH000037: Columns: [bolum_id, bolum_adi, fakulte_id]
2018-01-26 10:01:22 INFO  RequestMappingHandlerMapping:537 - Mapped "{[/index]}" onto public org.springframework...
2018-01-26 10:01:22 INFO  RequestMappingHandlerMapping:537 - Mapped "{[/student/loadStudents.ajax]}" onto public java.lang.String tr.edu.duzce.mf.bm.springApp.controller.OgrenciController.loadOgrenci(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)
```

Bu çıktı **rapor gereksinimlerindeki "dosya sistemine kaydedilen log örnekleri"** kısmına doğrudan konulabilir.

---

## 9.11. Sektörel Çözümler (Bilgi Amaçlı — Projede Kullanılmıyor)

### 9.11.1. ELK Stack
- **Elasticsearch** → İndeksleme ve depolama
- **Logstash** → Log ayrıştırma ve sunma
- **Kibana** → Dashboard ve görselleştirme
- Gerçek zamanlı.

**Kibana Kullanım Alanları (dersten):**
- Standart dashboard arayüzleri
- APM (Application Performance Management)
- Uptime (Ayakta Kalma)
- SIEM (Security Information and Event Management)

### 9.11.2. Graylog
- Elasticsearch ve MongoDB temelli
- Merkezileştirilmiş log yönetimi

> **Not:** Bu araçlar endüstride kullanılan araçlar. Projemizde sadece log4j + slf4j yeterli; ELK/Graylog zorunlu değil.

---

# 🧩 HEPSİ BİR ARADA — PROJE GERÇEKLEŞTİRME AKIŞI

> Bu bölüm, 7-8-9. ders notlarının **birlikte nasıl çalıştığını** özetliyor. Proje yazarken bu akışı zihinde tut.

## 10.1. Katmanlar Arası Tam Akış

```
┌─────────────────────────────────────────────────────────────────┐
│                    İSTEMCİ (Web Tarayıcısı)                     │
│        Ör: "Öğrencileri getir" butonuna tıklama                 │
└────────────────────────────┬────────────────────────────────────┘
                             │ HTTP POST
                             │ /ogrencileriGetir.ajax?ad=Ali
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                   CONTROLLER (tr.edu.duzce....web)              │
│   @Controller @PostMapping  @RequestParam  @ResponseBody        │
│   logger.info("İstek geldi, ad: {}", ad);                       │
│   (Loglama ders notu 9 — her istekte parametreleri logla)       │
└────────────────────────────┬────────────────────────────────────┘
                             │ ogrenciService.ogrencileriGetir(ad)
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                   SERVICE (tr.edu.duzce....service)             │
│   @Service @Transactional(readOnly=true)                        │
│   logger.debug("Service çağrıldı, parametre: {}", ad);          │
│   (İş mantığı — ders notu 7)                                    │
└────────────────────────────┬────────────────────────────────────┘
                             │ ogrenciDAO.ogrencileriGetir(ad)
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                   DAO (tr.edu.duzce....dao)                     │
│   @Repository                                                   │
│   Session → CriteriaBuilder → CriteriaQuery → Root → Predicate  │
│   (Hibernate Criteria Query — ders notu 8)                      │
│   session.createQuery(cq).getResultList()                       │
└────────────────────────────┬────────────────────────────────────┘
                             │ Hibernate → JDBC → c3p0 bağlantı havuzu
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                  VERİTABANI (MySQL — ayrı makina)               │
│   SELECT * FROM ogrenci WHERE adi = 'Ali';                      │
│   (hibernate.show_sql=true ile bu SQL loglanır — ders notu 9)   │
└────────────────────────────┬────────────────────────────────────┘
                             │ ResultSet → Ogrenci POJO (ders notu 7)
                             ▼
                  ... yukarı doğru geri dönüş ...

Her seviyede paralel: log4j → ConsoleAppender (stdout) + RollingFileAppender (../logs/bm470.log)
```

## 10.2. Tam Paket Yapısı

```
tr.edu.duzce.mf.bm.bm470/
├── config/
│   └── AppConfig.java              ← 7. ders
│                                     @Configuration, @EnableTransactionManagement
│                                     @PropertySource, @ComponentScan
│                                     LocalSessionFactoryBean, HibernateTransactionManager
├── model/                          ← 7. ders
│   ├── Ogrenci.java                  @Entity, @Table, @Id, @Column, @Temporal
│   ├── Bolum.java                    @ManyToOne, @JoinColumn
│   ├── Fakulte.java                  @OneToMany, FetchType, CascadeType
│   └── Danisman.java                 Lombok: @Getter, @Setter, @NoArgsConstructor
├── dao/                            ← 7. + 8. ders
│   └── OgrenciDAO.java               @Repository
│                                     CriteriaBuilder, CriteriaQuery, Root, Predicate
│                                     count/sum/avg/min/max projections
├── service/                        ← 7. ders
│   └── OgrenciService.java           @Service, @Transactional (propagation, readOnly)
│                                     @Autowired (DAO inject)
├── web/                            ← 4-6. ders notları (henüz analiz edilmedi)
│   └── OgrenciController.java        @Controller, @PostMapping, @RequestParam, @ResponseBody
├── exception/                      ← 4-6. ders notları (henüz analiz edilmedi)
└── interceptor/                    ← 4-6. ders notları (henüz analiz edilmedi)
                                     Her istekte parametre ve dönüş loglaması

src/main/resources/
├── hibernate.properties            ← 7. ders
│                                     mysql.driver, mysql.url, ...
│                                     hibernate.show_sql, hibernate.hbm2ddl.auto
│                                     c3p0 parametreleri
├── log4j.properties                ← 9. ders
│                                     rootLogger, ConsoleAppender, RollingFileAppender
├── messages_tr_TR.properties       ← i18n (ilk3pdf.md gereksinim 6)
└── messages_en_US.properties       ← i18n

logs/ (runtime — Tomcat'a göre göreli ../logs/)
└── bm470.log                       ← 9. ders (RollingFileAppender çıktısı)
    bm470.log.1, .2, ..., .10 (arşiv)
```

## 10.3. Tipik İstek Yaşam Döngüsü (Uçtan Uca Örnek)

**Senaryo:** Kullanıcı arama kutusuna "Ali" yazıp ara'ya bastı.

1. **Tarayıcı** → `POST /ogrencileriGetir.ajax` (ad=Ali)
2. **DispatcherServlet** → Controller'a yönlendirir.
3. **Controller** (`OgrenciController.ogrencileriGetir`):
   - Parametre loglanır (ders notu 9 kuralı: her isteğin parametreleri loglanacak)
   - `ogrenciService.ogrencileriGetir("Ali")` çağrılır
4. **Service** (`OgrenciService.ogrencileriGetir`):
   - `@Transactional(readOnly = true)` başlar
   - DAO çağrılır
5. **DAO** (`OgrenciDAO.ogrencileriGetir`):
   ```java
   Session session = getCurrentSession();
   CriteriaBuilder cb = session.getCriteriaBuilder();
   CriteriaQuery<Ogrenci> cq = cb.createQuery(Ogrenci.class);
   Root<Ogrenci> root = cq.from(Ogrenci.class);
   Predicate p = cb.equal(root.get("adi"), "Ali");
   cq.select(root).where(p);
   return session.createQuery(cq).getResultList();
   ```
6. **Hibernate** → SQL oluşturur: `SELECT * FROM ogrenci WHERE adi = 'Ali'`
7. **c3p0** → Havuzdan bir bağlantı alır.
8. **MySQL** → Sorgu çalışır, ResultSet döner.
9. **Hibernate** → ResultSet'i `Ogrenci` POJO'larına çevirir. `hibernate.show_sql=true` nedeniyle SQL logu düşer.
10. **DAO → Service → Controller** → Sonuç geri döner.
11. **Controller** → `JSONArray.fromObject(ogrenciListesi).toString()` ile JSON'a çevirir, dönüş loglanır.
12. **Tarayıcı** → JSON yanıtı alır.

**Her basamakta `log4j.properties`'teki iki appender devrede:**
- Konsol (`stdout` → Tomcat log konsoluna)
- Dosya (`../logs/bm470.log` → rapor gereksinimlerindeki "dosya sistemine kaydedilen log örnekleri" için)

---

## 10.4. Güncellenmiş Teknoloji Yığını Tablosu

İlk3pdf.md'deki tabloya bu 3 ders notunun eklediği / değiştirdiği satırlar:

| Bileşen | ilk3pdf.md | 7-8-9 ders notları | Değişim |
|---------|-----------|--------------------|---------|
| MySQL Driver | `mysql-connector-java 8.0.28` | `mysql-connector-j 9.1.0` (artifactId dahi değişti) | 🔴 Çelişki |
| Hibernate | `5.3.20.Final` | Değişiklik yok, ama `LocalSessionFactoryBean` kullanımı netleşti | ➡️ Aynı |
| c3p0 | `0.9.5.2` | 9 parametre detaylı gösterildi | ➡️ Aynı |
| slf4j | `1.7.25` | `slf4j-api 2.0.17` + `slf4j-reload4j 2.0.17` | 🔴 Çelişki |
| log4j | `log4j 1.2.14` | `log4j 2.25.3` (ama API'si hâlâ `org.apache.log4j.*`) | 🔴 Çelişki |
| Lombok | — | `1.18.42` (scope: provided) | 🆕 Eklendi |

## 10.5. Rapor Gereksinimleri ile Eşleştirme

İlk3pdf.md'deki rapor gereksinimlerinin hangileri 7-8-9 ders notlarında karşılandı:

| Gereksinim | 7-8-9 ile Karşılandı mı? | Nasıl |
|-----------|------------------------|-------|
| Veritabanı tabloları + E-R diyagramı | ✅ | Fakulte-Bolum-Danisman-Ogrenci E-R'i örnek olarak verildi; IntelliJ Database aracıyla veya DataGrip ile çizilebilir |
| Configuration, Service, DAO vb. açıklamalı | ✅ | Bu belgede her biri tam kodla mevcut |
| Tüm istekler tablo halinde (URI, metot, parametre, dönüş) | ⚠️ | Controller örnekleri var (`@PostMapping /ogrencileriGetir.ajax`) ama tam tablo format Controller ders notunda olacak |
| Dosya sistemine kaydedilen log örnekleri | ✅ | Ders notu 9'da `bm470.log` örneği verildi |
| Açıklamalı birim testler (JUnit) | ❌ | Henüz analiz edilmedi — 4-6'da olabilir |
| Controller bileşeni | ❌ | 4-6'da olacak |
| Interceptor bileşeni | ❌ | 4-6'da olacak |
| i18n (Türkçe + başka dil) | ⚠️ | Sadece dizin yapısında `messages_tr_TR.properties` / `messages_en_US.properties` görüldü, kullanım 4-6'da |

---

## 🚧 HENÜZ ANALİZ EDİLMEYEN DERS NOTLARI

| Ders Notu | Durum | Muhtemel İçerik |
|-----------|-------|-----------------|
| 4_unlocked.pdf | ⛔ Analiz edilmedi | Spring MVC temelleri / Controller / DispatcherServlet |
| 5_unlocked.pdf | ⛔ Analiz edilmedi | View / JSP / i18n / Interceptor |
| 6_unlocked.pdf | ⛔ Analiz edilmedi | JUnit / Birim test / AOP (AspectJ zaten pom.xml'de var) |

Bu notlar analiz edildiğinde `ders4-5-6.md` dosyasında ayrıca birleştirilecek. Gerekli olduğu bilinen konular:
- `@Controller`, `@GetMapping`, `@PostMapping`, `@PathVariable`, `@RequestParam`, `@ResponseBody`
- Model, ModelAndView, JSP view resolver
- Interceptor (her istekte parametre/dönüş loglama)
- MessageSource ile i18n
- JUnit test yazma (hem Service hem DAO için)

---

## 🎯 KISACASI (Yusuf için özet)

Bu 3 ders notu (7-8-9) senin projendeki şu kısımları eksiksiz kapsıyor:

1. **Veritabanı bağlantısı ve konfigürasyonu** → `config/AppConfig.java`, `hibernate.properties`
2. **Model (Entity) tasarımı** → `model/*` (Lombok'lu)
3. **DAO katmanı** → `dao/*` — CriteriaBuilder ile sorgular (where, or, like, in, order by, pagination, foreign key, projections)
4. **Service katmanı** → `service/*` — Transaction yönetimi
5. **Loglama altyapısı** → `log4j.properties` — konsol + dosya (5651'e uyum)

**Kalan parçalar (4-6'dan gelecek):**
- Controller, View (JSP), i18n, Interceptor, JUnit

**Önce çözülmesi gereken:** 🔴 **Versiyon çelişkisi** — eski (ilk3pdf.md'deki) mi yeni (7-9'daki) mi kullanılacak? Karar olmadan pom.xml yazmak zor.
