package com.stemsep.controller;

import org.junit.Before;
import org.junit.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class HomeControllerTest {

    private MockMvc mockMvc;

    @Before
    public void setUp() {
        HomeController controller = new HomeController();
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    public void testHomePageReturnsHomeView() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"));
    }

    @Test
    public void testHomePageReturnsHttp200() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is2xxSuccessful());
    }
}
