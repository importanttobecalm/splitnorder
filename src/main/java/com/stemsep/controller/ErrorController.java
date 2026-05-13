package com.stemsep.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * web.xml &lt;error-page&gt; mapping'lerinin hedefi. Tomcat'in default
 * hata sayfasını gösterme yolunu kesip her hata kodunu uygun JSP'ye
 * yönlendirir. Status kodu korunur (response.setStatus).
 */
@Controller
@RequestMapping("/error")
public class ErrorController {

    private static final Logger logger = LoggerFactory.getLogger(ErrorController.class);

    @GetMapping({"/show/{code}", "/show/{code}/"})
    public String show(@PathVariable("code") int code,
                       HttpServletRequest req,
                       HttpServletResponse res,
                       Model model) {

        Object origUri  = req.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
        Object origMsg  = req.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        Object origEx   = req.getAttribute(RequestDispatcher.ERROR_EXCEPTION);

        if (code >= 500) {
            logger.error("Container error {} → URI={} msg={} ex={}",
                    code, origUri, origMsg, origEx);
        } else {
            logger.warn("Container error {} → URI={} msg={}",
                    code, origUri, origMsg);
        }

        res.setStatus(code);
        model.addAttribute("status", code);

        // 404 dışında her şey 500 görselini kullanır (kodu farklı yansıtılır)
        return (code == 404) ? "error/404" : "error/500";
    }
}
