package com.stemsep.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * {@link HomeController} için MockMvc tabanlı birim testleri.
 *
 * <p>Spring slayt'ında ({@code Controller Testi} bölümü) gösterilen {@link MockMvc}
 * kalıbını kullanır: gerçek bir uygulama sunucusu (Tomcat) ayağa kaldırmadan,
 * Controller'a HTTP istekleri simüle edilir ve dönen view adı/HTTP durum kodu
 * doğrulanır. Bu, Controller-View binding'inin doğru çalıştığını hızlı bir
 * şekilde garantiler.</p>
 *
 * <p><b>Slayt referansı:</b> "Spring-JUnit ile Controller Testi" — gerçek
 * sunucu olmadan HTTP simülasyonu yapılır, Test edilecek metotlar
 * {@code public void} olmak zorundadır.</p>
 */
public class HomeControllerTest {

    private MockMvc mockMvc;

    /**
     * Her testten önce yalın bir Controller örneği ve {@link MockMvc} kurulur.
     * {@code standaloneSetup} ile Spring kontekst yüklenmez — bu test sadece
     * route + view name döndürmesini doğrular, gerçek bean wiring'i değil.
     */
    @BeforeEach
    public void setUp() {
        HomeController controller = new HomeController();
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    /**
     * Test 1: {@code GET /} isteğinin {@code home} view adıyla yanıtlanması.
     * Bu, ViewResolver'ın {@code /WEB-INF/views/home.jsp} dosyasını çözeceği
     * mantıksal view'ın Controller'dan döndüğünü doğrular.
     */
    @Test
    public void testHomePageReturnsHomeView() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"));
    }

    /**
     * Test 2: {@code GET /} isteğinin HTTP 2xx başarı durum kodu ile yanıtlanması.
     * Ana sayfa ziyaretçinin ilk karşılaştığı endpoint olduğu için her zaman
     * erişilebilir olmalıdır.
     */
    @Test
    public void testHomePageReturnsHttp200() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is2xxSuccessful());
    }
}
