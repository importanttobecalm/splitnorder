<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%-- Dil değiştirici: ?lang=tr / ?lang=en — LocaleChangeInterceptor parametresi --%>
<c:set var="curUri" value="${requestScope['jakarta.servlet.forward.request_uri'] != null ? requestScope['jakarta.servlet.forward.request_uri'] : pageContext.request.requestURI}" />
<div class="flex items-center gap-1 text-body-sm">
  <a href="${curUri}?lang=tr" class="px-2 py-1 rounded font-mono-label hover:text-primary ${pageContext.response.locale.language == 'tr' ? 'text-primary font-bold' : 'text-outline'}">TR</a>
  <span class="text-outline-variant">|</span>
  <a href="${curUri}?lang=en" class="px-2 py-1 rounded font-mono-label hover:text-primary ${pageContext.response.locale.language == 'en' ? 'text-primary font-bold' : 'text-outline'}">EN</a>
</div>
