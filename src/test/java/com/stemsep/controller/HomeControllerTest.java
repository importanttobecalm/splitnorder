package com.stemsep.controller;

import com.stemsep.model.User;
import com.stemsep.service.JobService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * {@link HomeController} için MockMvc tabanlı birim testleri.
 *
 * <p>"/" rotası üç davranış sergiler:
 * <ul>
 *   <li>Anonim → {@code forward:/static/landing.html}</li>
 *   <li>Login + hiç tamamlanmış işi yok → {@code home} (model.jobStatus="EMPTY")</li>
 *   <li>Login + tamamlanmış iş var → {@code home} (model.jobId set)</li>
 * </ul>
 * </p>
 */
public class HomeControllerTest {

    @Mock
    private JobService jobService;

    @InjectMocks
    private HomeController controller;

    private MockMvc mockMvc;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    /** Anonim ziyaretçi → statik landing'e forward. */
    @Test
    public void rootRequest_anonymousUser_forwardsToLandingHtml() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("/static/landing.html"));
    }

    /** Login olmuş ama hiç işi yok → home view + jobStatus="EMPTY". */
    @Test
    public void rootRequest_authenticatedUserNoJobs_returnsHomeWithEmptyStatus() throws Exception {
        User user = new User();
        user.setId(42L);
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("user", user);
        when(jobService.getJobsByUser(42L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(model().attribute("jobStatus", "EMPTY"));
    }
}
