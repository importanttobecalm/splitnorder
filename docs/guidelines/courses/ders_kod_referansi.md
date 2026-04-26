# BM470 İLERİ JAVA PROGRAMLAMA — DERS KOD REFERANSI

> **AMAÇ:** Bu dosya ders notlarında (4, 5, 6 numaralı PDF'ler) geçen TÜM kod yapılarını tek bir yerde toplar. Kod yazarken SADECE bu dosyadaki kalıplar, annotation'lar, sınıflar ve yapılandırmalar kullanılacaktır. Modern Spring Boot, Lombok, JPA repositories, JUnit 5, log4j2, @RestController gibi dersin DIŞINA çıkan hiçbir yapı kullanılmayacaktır.

---

## 0. KULLANILACAK TEKNOLOJİ VE VERSİYONLAR

| Bileşen | Versiyon |
|---------|----------|
| JDK | 18 |
| Spring Framework | 6.0.4 |
| Spring LDAP | 2.3.2.RELEASE |
| Hibernate | 5.3.20.Final |
| c3p0 | 0.9.5.2 |
| MySQL Driver | 8.0.28 |
| SLF4J | 1.7.25 |
| log4j | 1.2.14 (log4j 1.x) |
| JUnit | 4.13.1 |
| AspectJ | 1.8.13 |
| Jakarta Servlet API | 6.1.0 |
| JSTL API | 3.0.0 |
| Packaging | war |

**Paket kökü:** `tr.edu.duzce.mf.bm.bm470`

**Paket yapısı:**
```
tr.edu.duzce.mf.bm.bm470.config       → Configuration sınıfları (WebConfig, WebAppInitializer)
tr.edu.duzce.mf.bm.bm470.web          → Controller sınıfları
tr.edu.duzce.mf.bm.bm470.service      → Service sınıfları (@Service)
tr.edu.duzce.mf.bm.bm470.dao          → DAO sınıfları (@Repository)
tr.edu.duzce.mf.bm.bm470.interceptor  → Interceptor sınıfları
tr.edu.duzce.mf.bm.bm470.exception    → Exception sınıfları
tr.edu.duzce.mf.bm.bm470.model        → Entity/DTO sınıfları
```

---

## 1. pom.xml

### Proje Koordinatları
```xml
<project>
    <modelVersion>4.0.0</modelVersion>
    <groupId>tr.edu.duzce.mf.bm</groupId>
    <artifactId>projeadi</artifactId>
    <version>1.0-SNAPSHOT</version>
    <packaging>war</packaging>
    <name>projeadi</name>
    
    <organization>
        <name>Duzce University</name>
        <url>https://duzce.edu.tr</url>
    </organization>
    
    <developers>
        <developer>
            <id>...</id>
            <name>...</name>
            <email>...</email>
            <roles><role>...</role></roles>
        </developer>
    </developers>
</project>
```

### Properties (versiyon bilgileri)
```xml
<properties>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    <maven.test.skip>false</maven.test.skip>
    <spring.framework.version>6.0.4</spring.framework.version>
    <spring.ldap.version>2.3.2.RELEASE</spring.ldap.version>
    <aspectj.version>1.8.13</aspectj.version>
    <mysql.driver.version>8.0.28</mysql.driver.version>
    <hibernate.version>5.3.20.Final</hibernate.version>
    <c3p0.version>0.9.5.2</c3p0.version>
    <slf4j.version>1.7.25</slf4j.version>
    <log4j.version>1.2.14</log4j.version>
    <junit.version>4.13.1</junit.version>
</properties>
```

### Dependency Tanım Formatı (Dersten)
```xml
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-webmvc</artifactId>
    <version>6.0.4</version>
</dependency>
```

### Servlet API (provided scope)
```xml
<dependency>
    <groupId>jakarta.servlet</groupId>
    <artifactId>jakarta.servlet-api</artifactId>
    <version>6.1.0</version>
    <scope>provided</scope>
</dependency>
```

### JSTL API
```xml
<dependency>
    <groupId>jakarta.servlet.jsp.jstl</groupId>
    <artifactId>jakarta.servlet.jsp.jstl-api</artifactId>
    <version>3.0.0</version>
</dependency>
```

### JUnit (test scope)
```xml
<dependency>
    <groupId>junit</groupId>
    <artifactId>junit</artifactId>
    <version>${junit.version}</version>
    <scope>test</scope>
</dependency>
```

### Build Yapılandırması
```xml
<build>
    <finalName>projeadi</finalName>
    <pluginManagement>
        <plugins>
            <plugin>
                <artifactId>maven-clean-plugin</artifactId>
                <version>3.2.0</version>
            </plugin>
            <plugin>
                <artifactId>maven-resources-plugin</artifactId>
                <version>3.3.0</version>
            </plugin>
            <plugin>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.11.0</version>
                <configuration>
                    <release>18</release>
                </configuration>
            </plugin>
            <plugin>
                <artifactId>maven-surefire-plugin</artifactId>
                <version>2.22.1</version>
            </plugin>
            <plugin>
                <artifactId>maven-war-plugin</artifactId>
                <version>3.3.2</version>
                <configuration>
                    <failOnMissingWebXml>false</failOnMissingWebXml>
                </configuration>
            </plugin>
            <plugin>
                <artifactId>maven-install-plugin</artifactId>
                <version>3.1.0</version>
            </plugin>
            <plugin>
                <artifactId>maven-deploy-plugin</artifactId>
                <version>3.1.0</version>
            </plugin>
        </plugins>
    </pluginManagement>
</build>
```

### JavaDoc Üretim Plugin
```xml
<plugin>
    <artifactId>maven-site-plugin</artifactId>
    <version>3.12.1</version>
</plugin>
<plugin>
    <artifactId>maven-javadoc-plugin</artifactId>
    <version>3.5.0</version>
    <configuration>
        <show>private</show>
        <nohelp>true</nohelp>
    </configuration>
</plugin>
```

---

## 2. WEBAPPINITIALIZER — Dispatcher Servlet ve Filter Tanımı

**Paket:** `tr.edu.duzce.mf.bm.bm470.config`

```java
public class WebAppInitializer implements WebApplicationInitializer {
    @Override
    public void onStartup(ServletContext servletContext) {
        AnnotationConfigWebApplicationContext context = getContext();
        servletContext.addListener(new ContextLoaderListener(context));
        ServletRegistration.Dynamic dispatcherServlet =
            servletContext.addServlet("DispatcherServlet", new DispatcherServlet(context));
        dispatcherServlet.setLoadOnStartup(1);
        dispatcherServlet.addMapping("/");

        CharacterEncodingFilter characterEncodingFilter = new CharacterEncodingFilter();
        characterEncodingFilter.setEncoding("UTF-8");
        characterEncodingFilter.setForceEncoding(true);
        characterEncodingFilter.setForceRequestEncoding(true);
        characterEncodingFilter.setForceResponseEncoding(true);
        servletContext.addFilter("characterEncodingFilter",
            characterEncodingFilter).addMappingForUrlPatterns(null, false, "/*");
    }

    private AnnotationConfigWebApplicationContext getContext() {
        AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
        context.setConfigLocation("tr.edu.duzce.mf.bm.bm470.config");
        return context;
    }
}
```

---

## 3. WEBCONFIG — Spring MVC Konfigürasyon Sınıfı

**Paket:** `tr.edu.duzce.mf.bm.bm470.config`

### Temel İskelet
```java
@Configuration
@EnableWebMvc
@ComponentScan(basePackages = {"tr.edu.duzce.mf.bm"})
public class WebConfig implements WebMvcConfigurer {
}
```

### ViewResolver Tanımı (JSP için)
```java
@Bean
public InternalResourceViewResolver jspViewResolver() {
    InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
    viewResolver.setViewClass(JstlView.class);
    viewResolver.setPrefix("/WEB-INF/view/");
    viewResolver.setSuffix(".jsp");
    viewResolver.setContentType("text/html;charset=UTF-8");
    return viewResolver;
}
```
> `return "anasayfa"` → `/WEB-INF/view/anasayfa.jsp` dosyasını çözümler.

### Static Kaynakların Servlet Container'a Devri
```java
@Override
public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
    configurer.enable();
}
```

### Response Karakter Kodlaması (MessageConverter)
```java
@Override
public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
    StringHttpMessageConverter stringConverter = new StringHttpMessageConverter();
    List<MediaType> mediaTypeList = new ArrayList<>();
    mediaTypeList.add(new MediaType("text", "plain", Charset.forName("UTF-8")));
    mediaTypeList.add(new MediaType("text", "html", Charset.forName("UTF-8")));
    mediaTypeList.add(new MediaType("application", "json", Charset.forName("UTF-8")));
    mediaTypeList.add(new MediaType("text", "javascript", Charset.forName("UTF-8")));
    
    stringConverter.setSupportedMediaTypes(mediaTypeList);
    converters.add(stringConverter);
}
```

### Interceptor Kaydı
```java
@Override
public void addInterceptors(InterceptorRegistry registry) {
    // Tek path'li tanım:
    registry.addInterceptor(new RequestInterceptor()).addPathPatterns("/*");

    // Birden çok path'li tanım:
    registry.addInterceptor(new RequestInterceptor())
            .addPathPatterns(new String[] { "/ogrenci/*", "/personel/*" });
}
```

---

## 4. CONTROLLER — Tüm Kalıplar

**Paket:** `tr.edu.duzce.mf.bm.bm470.web`

### Kural: Controller Sınıfı = `@Controller`. `@RestController` KULLANILMAZ.

### 4.1. Temel Controller İskeleti
```java
@Controller
@RequestMapping(value = "/*")
public class MainController {
    @Autowired
    private OgrenciService ogrenciService;
    @Autowired
    private MessageSource messageSource;
    @Autowired
    private MailService mailService;

    @GetMapping(value = "/anasayfa")
    public String index(Model model) {
        //.
        return "anasayfa";
    }
}
```

### 4.2. Sınıf Üst Yolu + Metot Yolu
```java
@Controller
@RequestMapping(value = "/ogrenci/*")
public class OgrenciController {
    @Autowired
    private OgrenciService ogrenciService;

    @GetMapping(value = "/ogrenciYukle")
    public @ResponseBody String ogrenciYukle(@RequestParam Integer donem) {
        //.
        return "veriler";
    }
}
// Tam URI: /ogrenci/ogrenciYukle
```

### 4.3. @RequestMapping — Uzun Yazım (Denklikler)
```java
@RequestMapping(value = "/kaydet", method = RequestMethod.POST)   // == @PostMapping("/kaydet")
@RequestMapping(value = "/upload", method = RequestMethod.PUT)    // == @PutMapping("/upload")
@RequestMapping(value = "/index",  method = RequestMethod.GET)    // == @GetMapping("/index")
```

### 4.4. @RequestMapping — value Özellikleri
```java
// Tek yol
@RequestMapping(value = "/giris", method = RequestMethod.GET)

// Birden fazla yol
@RequestMapping(value = {"/giris", "/login", "/anasayfa", "/home"}, method = RequestMethod.GET)
```

### 4.5. @RequestMapping — headers / produces / consumes
```java
// headers
@RequestMapping(value = "/giris", method = RequestMethod.GET, headers = "donem=2018")
@RequestMapping(value = "/giris", method = RequestMethod.GET, headers = {"donem=2018", "sinav=1"})

// produces / consumes
@RequestMapping(value = "/giris",
                produces = {"application/json", "application/xml"},
                consumes = "text/html")
```

### 4.6. @RequestParam — İstek Parametresi
```java
// Temel kullanım
@GetMapping(value = "/giris")
public String giris(@RequestParam(name = "sinifi") Integer sinifi,
                    HttpServletRequest request, HttpServletResponse response) {
    //.
    return "giris";
}

// required = false (parametre zorunlu değil)
@GetMapping(value = "/giris")
public String giris(@RequestParam(name = "sinifi", required = false) Integer sinifi,
                    HttpServletRequest request, HttpServletResponse response) {
    //.
    return "giris";
}
```

### 4.7. @PathVariable — İstek Yolu Değişkeni
```java
@Controller
@RequestMapping(value = "/personel/*")
public class PersonelController {
    @GetMapping(value = "/{email}")
    public String personel(Model model,
                           @PathVariable("email") String eposta,
                           HttpServletRequest request, HttpServletResponse response) {
        model.addAttribute("email", eposta);
        return "personel";
    }
}
```

### 4.8. @ResponseBody — Gövdeye Veri Doğrudan Yazma
```java
@GetMapping(value = "/ogrenciYukle")
public @ResponseBody String ogrenciYukle(HttpServletRequest request, HttpServletResponse response) {
    // ilgili islemler
    return "veri";
}
```

### 4.9. Response produces ile Karakter Kodlaması
```java
@GetMapping(value = "/ogrenciYukle", produces = "application/json; charset=utf-8")
public @ResponseBody String ogrenciYukle() {
    //.
    return "veriler";
}
```

### 4.10. Model Sınıfı Kullanımı
```java
// Tek özellik
model.addAttribute("email", eposta);

// Toplu özellik
model.addAllAttributes(attributesMap);  // attributes: Map<String, ?>

// Sorgulama
boolean varmi = model.containsAttribute("email");
```

### 4.11. @ResponseStatus — Metot Seviyesinde Durum Kodu
```java
@ResponseStatus(value = HttpStatus.NOT_FOUND)
@GetMapping(value = "/ogrenciYukle")
public String ogrenci(HttpServletRequest request, HttpServletResponse response) {
    return "ogrenci";
}

// reason ile
@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Kaynak bulunamadı")
@GetMapping(value = "/ogrenciYukle")
public String ogrenci(HttpServletRequest request, HttpServletResponse response) {
    return "ogrenci";
}
```

### 4.12. Varolan View'ı 404 Olarak Döndürme
```java
@Controller
public class MainController {
    @GetMapping(value = "/personel/{email}")
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public String personel(Model model,
                           @PathVariable("email") String eposta,
                           HttpServletRequest request, HttpServletResponse response) {
        model.addAttribute("email", eposta);
        return "personel";
    }
}
// NOT: Cevap kodu 404 olmasına rağmen sayfa görüntülenir.
```

### 4.13. RuntimeException ile Durum Ayarı (Daha Temiz Çözüm)
```java
// Paket: tr.edu.duzce.mf.bm.bm470.exception
@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException() {
        super("Kaynak bulunamadi!");
    }
}

// Paket: tr.edu.duzce.mf.bm.bm470.web
@Controller
public class MainController {
    @GetMapping(value = "/personel/{email}")
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public void personel(Model model, @PathVariable("email") String eposta,
                         HttpServletRequest request, HttpServletResponse response) {
        model.addAttribute("email", eposta);
        throw new ResourceNotFoundException();
    }
}
```

### 4.14. response.setStatus ile Durum Kodu Ayarlama
```java
response.setStatus(HttpStatus.BAD_REQUEST.value());   // 400
```

### HttpStatus Enum Sabitleri (Derste Gösterilenler)
```
HttpStatus.OK                           -- 200
HttpStatus.MOVED_PERMANENTLY            -- 301
HttpStatus.BAD_REQUEST                  -- 400
HttpStatus.UNAUTHORIZED                 -- 401
HttpStatus.FORBIDDEN                    -- 403
HttpStatus.NOT_FOUND                    -- 404
HttpStatus.INTERNAL_SERVER_ERROR        -- 500
HttpStatus.SERVICE_UNAVAILABLE          -- 503
HttpStatus.HTTP_VERSION_NOT_SUPPORTED   -- 505
```

---

## 5. INTERCEPTOR

**Paket:** `tr.edu.duzce.mf.bm.bm470.interceptor`

### 5.1. Temel Interceptor — Metot Girişi/Çıkışı Logla
```java
@Component
public class RequestInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        HandlerMethod hm = (HandlerMethod) handler;
        Method method = hm.getMethod();
        System.out.println(method.getName() + " isimli metodun yurutumunden once");
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response,
                           Object handler, ModelAndView modelAndView) throws Exception {
        HandlerMethod hm = (HandlerMethod) handler;
        Method method = hm.getMethod();
        System.out.println(method.getName() + " isimli metodun yurutumunden sonra");
    }

    // afterCompletion metodu (gövdelenmesi zorunludur, boş bile olsa)
}
```

### 5.2. Session Tabanlı Interceptor (Yetkilendirme)
```java
@Component
public class SessionInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        HttpSession session = request.getSession();
        Object username = session.getAttribute("username");
        return username != null;
    }

    // postHandle metodu
    // afterCompletion metodu
}
```

### 5.3. Süre Ölçen Interceptor — Üç Metot Kullanımı
```java
@Component
public class ControllerInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        long baslangic = System.currentTimeMillis();
        request.setAttribute("baslangic", baslangic);
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response,
                           Object handler, ModelAndView modelAndView) throws Exception {
        long bitis = System.currentTimeMillis();
        request.setAttribute("bitis", bitis);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) throws Exception {
        HandlerMethod hm = (HandlerMethod) handler;
        Method method = hm.getMethod();
        if (request.getAttribute("endTime") != null) {
            long baslangic = Long.parseLong(request.getAttribute("baslangic").toString());
            long bitis = Long.parseLong(request.getAttribute("bitis").toString());
            long sure = bitis - baslangic;
            System.out.println(method.getName() + " isimli metodun yurutumu " + sure + " ms. surdu");
        } else {
            System.err.println(method.getName() + " isimli metodun yurutumu tamamlanamadi!");
        }
    }
}
```

### HandlerInterceptor Metot İmzaları (Zorunlu)
```
boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
    → true: HTTP isteğinin yürütümü devam eder
    → false: HTTP isteği sonlandırılır

void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView)
    → HTTP isteği controller tarafından tamamlanınca çağrılır

void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
    → HTTP isteği tamamen bitip istemciye dönüş yapıldıktan sonra çağrılır
```

### İstek-Cevap Yürütüm Sırası
```
Filter → DispatcherServlet → Interceptor → Controller   (request)
Filter ← DispatcherServlet ← Interceptor ← Controller   (response)
```

---

## 6. SERVLET ve JSP (Referans için — Önceki Ders Notları)

### 6.1. Servlet İskeleti (HttpServlet)
```java
public class AnasayfaServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = null;
        try {
            out = response.getWriter();
            out.println("<html><body>");
            HttpSession session = request.getSession();
            out.println("<p>Bağlantı Adresi: " + request.getRemoteAddr() + "</p>");
            out.println("<p>İstek URI: " + request.getRequestURI() + "</p>");
            out.println("<p>Protokol: " + request.getProtocol() + "</p>");
            out.println("<p>İstek Metodu: " + request.getMethod() + "</p>");
            out.println("<p>Session Id: " + session.getId() + "</p>");
            out.println("</body></html>");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (out != null) out.close();
        }
    }
}
```

### 6.2. doPost Servlet
```java
public class KaydetServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = null;
        try {
            out = response.getWriter();
            request.setCharacterEncoding("UTF-8");
            String isim = request.getParameter("isim");
            isim = isim.trim();
            String soyisim = request.getParameter("soyisim");
            soyisim = soyisim.trim();
            // ...
            out.println("<body><h1>Girilen Bilgiler</h1>");
            out.println("<p>İsim: " + isim + "</p>");
            out.println("<p>Soyisim: " + soyisim + "</p>");
            out.println("</body></html>");
        } catch (IOException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } finally {
            if (out != null) out.close();
        }
    }
}
```

### 6.3. web.xml Servlet Kaydı
```xml
<servlet>
    <servlet-name>AnasayfaServlet</servlet-name>
    <servlet-class>tr.edu.duzce.mf.bm.servletapp.AnasayfaServlet</servlet-class>
</servlet>
<!-- servlet tanimlari, servlet-mapping tanimlarindan önce yazilmalidir -->
<servlet-mapping>
    <servlet-name>AnasayfaServlet</servlet-name>
    <url-pattern>/anasayfa</url-pattern>
</servlet-mapping>
```

### 6.4. init-param ile ServletConfig Kullanımı
```xml
<servlet>
    <servlet-name>ServletName</servlet-name>
    <servlet-class>a.b.c.ServletClass</servlet-class>
    <init-param>
        <param-name>initParam1</param-name>
        <param-value>initParam1Value</param-value>
    </init-param>
</servlet>
```

```java
public void init(ServletConfig servletConfig) throws ServletException {
    String initParam1Value = servletConfig.getInitParameter("initParam1");
}

public void destroy() {
    //.
}
```

---

## 7. JSP

### 7.1. Yönergeler
```jsp
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.Date" %>
<%@ include file="header.jsp" %>
<%@ taglib prefix="c" uri="jakarta/tags/core" %>
```

### 7.2. Betik Elemanları
```jsp
<%-- Yorum satırı --%>
<%! int sayi = 5; %>                        <%-- Tanımlama --%>
<%= request.getParameter("isim") %>         <%-- İfade --%>
<% String isim = request.getParameter("isim"); %>   <%-- Komut (Scriptlet) --%>
```

### 7.3. Scriptlet + HTML Karışım Örnek
```jsp
<% String isim = request.getParameter("isim");
   if(isim == null || isim.equals("")) { %>
    <h2> Lütfen bir isim giriniz! </h2>
<% } else { %>
    <h2> Merhaba <%= isim %> </h2>
<% } %>
```

### 7.4. Dahili (Kapalı) Nesneler
```
request    (jakarta.servlet.http.HttpServletRequest)
response   (jakarta.servlet.http.HttpServletResponse)
out        (PrintWriter)
session    (HttpSession)
application (ServletContext)
config     (ServletConfig)
pageContext (JspWriters)
page       (this)
Exception  (Exception)
```

### 7.5. JSP Bean İşlemleri
```jsp
<jsp:useBean id="ogrenciAli" class="tr.edu.duzce.mf.bm.Ogrenci" />
<jsp:setProperty name="ogrenciAli" property="ad" value="Ali" />
<jsp:getProperty name="ogrenciAli" property="ad" />
```

### 7.6. Kayıt Formu Örneği (kayit.jsp)
```jsp
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<html>
<head>
    <meta http-equiv='Content-Type' content='text/html; charset=UTF-8'>
    <title>Kayıt Sayfası</title>
</head>
<body>
    <h2>Kayıt Formu</h2>
    <form method="post" action="kaydet">
        <fieldset>
            <legend>Kullanıcı Kayıt Formu</legend>
            İsim: <input type="text" name="isim" /><br />
            Soyisim: <input type="text" name="soyisim" /><br />
            Cinsiyet: 
            <input type="radio" name="cinsiyet" value="1">Erkek</input>
            <input type="radio" name="cinsiyet" value="2">Kadın</input><br />
            Sınıf: 
            <select name="sinif">
                <option value="1">1. Sınıf</option>
                <option value="2">2. Sınıf</option>
                <option value="3">3. Sınıf</option>
                <option value="4">4. Sınıf</option>
            </select>
        </fieldset>
        <input type="submit" value="Kaydet" />
        <input type="reset" value="Temizle" />
    </form>
</body>
</html>
```

---

## 8. JSTL (Jakarta Taglibs)

### 8.1. Temel Etiketler (core — `c` öneki)
```jsp
<%@ taglib prefix="c" uri="jakarta/tags/core" %>

<c:out value="..." />
<c:set var="..." value="..." />
<c:remove var="..." />

<c:catch var="...">
    Komutlar
</c:catch>

<c:if test="..." >
    ...
</c:if>

<c:choose>
    <c:when test="...">
        İşlem
    </c:when>
    <c:otherwise>
        İşlem
    </c:otherwise>
</c:choose>

<c:import var="..." url="..." />

<c:forEach items="..." var="...">
    İşlem
</c:forEach>

<c:forTokens items="..." var="..." delims="...">
    İşlem
</c:forTokens>
```

### 8.2. Formatlama Etiketleri (fmt)
```jsp
<%@ taglib prefix="fmt" uri="jakarta/tags/fmt" %>

<fmt:formatNumber>
<fmt:parseNumber>
<fmt:formatDate>
<fmt:parseDate>
<fmt:bundle>
<fmt:setLocale>
<fmt:setBundle>
<fmt:timeZone>
<fmt:setTimeZone>
<fmt:requestEncoding>
```

### 8.3. JSTL Fonksiyonları (fn)
```jsp
<%@ taglib prefix="fn" uri="jakarta/tags/functions" %>

${fn:contains(str, 'test')}
${fn:containsIgnoreCase(str, 'test')}
${fn:endsWith(str, '.jsp')}
${fn:escapeXml(str)}
${fn:indexOf(str, 'a')}
${fn:join(array, ',')}
${fn:length(str)}
${fn:replace(str, 'a', 'b')}
${fn:split(str, ',')}
${fn:startsWith(str, 'a')}
${fn:substring(str, 0, 3)}
${fn:substringAfter(str, 'a')}
${fn:substringBefore(str, 'a')}
${fn:toLowerCase(str)}
${fn:toUpperCase(str)}
${fn:trim(str)}
```

### 8.4. XML Etiketleri (x)
```jsp
<%@ taglib prefix="x" uri="jakarta/tags/xml" %>
<x:out> <x:parse> <x:set> <x:if> <x:forEach>
<x:choose> <x:when> <x:otherwise>
<x:transform> <x:param>
```

### 8.5. SQL Etiketleri (sql) — Dersten: "Güvenlik gereği servlet'lerden kullanılmalı"
```jsp
<%@ taglib prefix="sql" uri="jakarta/tags/sql" %>
<sql:setDataSource>
<sql:query>
<sql:update>
<sql:param>
<sql:dateParam>
<sql:transaction>
```

### 8.6. fmt:formatDate Örneği
```jsp
<%@ taglib prefix="c" uri="jakarta/tags/core" %>
<%@ taglib prefix="fmt" uri="jakarta/tags/fmt" %>
<html>
<body>
    <c:set var="currentDate" value="<%=new java.util.Date()%>" />
    <fmt:formatDate type="date" value="${currentDate}" /> <br/>
    <fmt:formatDate type="time" value="${currentDate}" /> <br/>
    <fmt:formatDate type="both" value="${currentDate}" /> <br/>
    <fmt:formatDate pattern="yyyy-MM-dd" value="${currentDate}" />
</body>
</html>
```

### 8.7. JSTL Fonksiyon Örneği
```jsp
<%@ taglib uri="jakarta/tags/core" prefix="c" %>
<%@ taglib uri="jakarta/tags/functions" prefix="fn" %>
<html>
<body>
    <c:set var="str" value="Bu bir test String'idir." />
    <c:if test="${fn:contains(str, 'test')}">
        <p>test bulundu.</p>
    </c:if>
    <c:if test="${fn:contains(str, 'TEST')}">
        <p>TEST bulundu.</p>
    </c:if>
</body>
</html>
```

---

## 9. HTTP REQUEST / RESPONSE API'LARI

### HttpServletRequest — Temel Metotlar
```java
String getParameter(String parameter);                   // Parametre değeri (yoksa null)
Map<String, String[]> getParameterMap();                 // Parametre Map
Enumeration<String> getParameterNames();                 // Parametre adları
String[] getParameterValues(String parameter);           // Parametre değerleri (array)
String getRemoteAddr();                                  // İstemci IP
String getRequestURI();                                  // İstek URI
String getProtocol();                                    // Protokol
String getMethod();                                      // HTTP metodu
HttpSession getSession();                                // Oturum nesnesi
void setCharacterEncoding(String encoding);              // Karakter kodlaması
void setAttribute(String name, Object value);            // Request scope attribute
Object getAttribute(String name);                        // Request scope attribute okuma
```

### HttpServletResponse — Temel Metotlar
```java
void setStatus(int code);                 // Durum kodu
void setContentType(String type);         // Content-Type
void sendRedirect(String location);       // Yönlendirme
void setLocale(Locale locale);            // Locale ayarı
PrintWriter getWriter();                  // Yazıcı
```

### HttpSession — Temel Metotlar
```java
String getId();                                          // Oturum Id
long getCreationTime();                                  // Oluşturulma zamanı
long getLastAccessedTime();                              // Son istek zamanı
void setMaxInactiveInterval(int interval);               // Max inaktif süre (saniye)
Object getAttribute(String name);                        // Oturumdan veri
void setAttribute(String name, Object value);            // Oturuma veri
void removeAttribute(String name);                       // Oturumdan sil
void invalidate();                                       // Oturumu sonlandır
boolean isNew();                                         // Yeni mi?
```

---

## 10. JavaDoc Annotation'ları (Dersten)

```java
@author       → Sınıf geliştirici bilgisi (sınıf)
@param        → Parametre açıklaması (metod)
@return       → Metot geri dönüş bilgisi (metod)
@throws       → Exception fırlattığını belirtir (metod)
@see          → Ek açıklama linki (metod)
@since        → Sunulduğu versiyon (metod)
@deprecated   → Metotun deprecated olduğunu belirtir (metod)
@Override     → Override belirtir (metod)
@link         → Açıklamada link belirtir (metod)
```

JavaDoc üretimi: `mvn javadoc:javadoc` → çıktı: `target/site/apidocs/`

---

## 11. SUNUCU JVM AYARLARI

Tomcat `bin/catalina.sh` veya `catalina.bat` içine:
```
JAVA_OPTS="-Xms1024m -Xmx4096m -DuriEncoding=UTF-8 -XX:+UseG1GC"
```

JVM karakter kodlaması parametreleri:
```
-DuriEncoding=UTF-8
-Dfile.encoding=UTF-8
```

---

## 12. YAPMA / YAPMAMA LİSTESİ (BU PROJE İÇİN)

### ✗ ASLA KULLANILMAYACAK
- `@RestController` (kullanılan: `@Controller` + `@ResponseBody`)
- Spring Boot (`spring-boot-starter-*`)
- JUnit 5 (`org.junit.jupiter.*`) — kullanılan: JUnit 4 (`org.junit.Test`)
- log4j2 — kullanılan: log4j 1.x (`org.apache.log4j`)
- Lombok (`@Data`, `@Getter`, `@Setter`)
- Spring Data JPA `JpaRepository` — kullanılan: Hibernate 5 + `SessionFactory`
- `@SpringBootApplication`, `main` metodu ile başlatma — kullanılan: `WebApplicationInitializer`
- Constructor injection zorunluluğu — kullanılan: field-level `@Autowired`
- Gradle — kullanılan: Maven
- `application.properties` / `application.yml` — kullanılan: Java sınıfı tabanlı `@Configuration`
- web.xml (servlet API olarak provided dependency hariç) — kullanılan: `WebAppInitializer`

### ✓ HER ZAMAN UYULACAK
- Sınıf üstünde `@Controller` + opsiyonel `@RequestMapping("/ustYol/*")`
- Metot parametrelerinde `HttpServletRequest request, HttpServletResponse response` eklenmesi
- `@Autowired` field seviyesinde
- Paket adları: `tr.edu.duzce.mf.bm.bm470.<katman>`
- ViewResolver prefix: `/WEB-INF/view/`, suffix: `.jsp`
- Tüm Controller metotlarında istek parametreleri ve geri dönüş log'lanır (log4j + slf4j)
- JSTL taglib URI formatı: `jakarta/tags/<name>` (örn: `jakarta/tags/core`)
- failOnMissingWebXml = false (annotation-based config)
- JDK release: 18
- Packaging: war
- UTF-8 her katmanda zorunlu
