package com.stemsep.config;

import jakarta.servlet.MultipartConfigElement;
import jakarta.servlet.ServletRegistration;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

public class WebAppInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {

    @Override
    protected Class<?>[] getRootConfigClasses() {
        return new Class<?>[]{ AppConfig.class, HibernateConfig.class };
    }

    @Override
    protected Class<?>[] getServletConfigClasses() {
        return new Class<?>[]{ WebConfig.class };
    }

    @Override
    protected String[] getServletMappings() {
        return new String[]{ "/" };
    }

    @Override
    protected void customizeRegistration(ServletRegistration.Dynamic registration) {
        registration.setMultipartConfig(
            new MultipartConfigElement(
                System.getProperty("java.io.tmpdir"),
                50 * 1024 * 1024,   // maxFileSize: 50MB
                50 * 1024 * 1024,   // maxRequestSize: 50MB
                0                    // fileSizeThreshold
            )
        );
    }
}
