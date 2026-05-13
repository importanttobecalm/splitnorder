package com.stemsep.controller;

import com.stemsep.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * {@link HomeController} için MockMvc tabanlı birim testleri.
 *
 * <p>"/" rotası session'a göre iki davranış sergiler:
 * <ul>
 *   <li>Anonim ziyaretçi → {@code forward:/landing.html} (statik animasyonlu açılış)</li>
 *   <li>Login olmuş kullanıcı → {@code home} JSP (uygulama içi dashboard)</li>
 * </ul>
 * Landing'in JSP yerine static HTML olarak servis edilmesi bilinçli bir
 * tercihtir: GSAP / template-literal içeren JS kodları JSP EL ifadeleriyle
 * çakışmasın diye.</p>
 */
public class HomeControllerTest {

    private MockMvc mockMvc;

    @BeforeEach
    public void setUp() {
        HomeController controller = new HomeController();
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    /**
     * Anonim ziyaretçi → static landing'e forward.
     */
    @Test
    public void rootRequest_anonymousUser_forwardsToLandingHtml() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("/static/landing.html"));
    }

    /**
     * Login olmuş kullanıcı → home view'ı.
     */
    @Test
    public void rootRequest_authenticatedUser_returnsHomeView() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("user", new User());

        mockMvc.perform(get("/").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("home"));
    }
}
