<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%-- Üst navigasyon — kimlik doğrulanmış sayfalar için ortak header. --%>
<c:set var="navActive" value="${empty navActive ? param.active : navActive}" />

<header class="fixed top-0 left-0 right-0 z-40 bg-surface-container-lowest/80 backdrop-blur-md border-b border-outline-variant/30">
  <nav class="flex items-center justify-between px-margin_mobile md:px-margin_desktop py-4 max-w-[1440px] mx-auto">

    <%-- Marka --%>
    <a href="${ctx}/" class="flex items-center gap-2">
      <img src="${ctx}/static/img/logo.jpg" alt="Splitnorder" class="w-8 h-8 rounded-md object-cover">
      <span class="text-headline-sm font-headline-sm font-bold text-primary tracking-tight">Splitnorder</span>
    </a>

    <%-- Linkler --%>
    <div class="hidden md:flex items-center gap-8">
      <a href="${ctx}/" class="${navActive == 'home' ? 'text-primary font-bold border-b-2 border-primary pb-1' : 'text-on-surface-variant hover:text-primary'} font-body-sm text-body-sm transition-all"><fmt:message key="nav.home" /></a>
      <a href="${ctx}/upload" class="${navActive == 'upload' ? 'text-primary font-bold border-b-2 border-primary pb-1' : 'text-on-surface-variant hover:text-primary'} font-body-sm text-body-sm transition-all"><fmt:message key="nav.upload" /></a>
      <a href="${ctx}/history" class="${navActive == 'history' ? 'text-primary font-bold border-b-2 border-primary pb-1' : 'text-on-surface-variant hover:text-primary'} font-body-sm text-body-sm transition-all"><fmt:message key="nav.history" /></a>
    </div>

    <%-- Aksiyonlar (dil + profil/giriş) --%>
    <div class="flex items-center gap-4">
      <jsp:include page="/WEB-INF/views/layout/lang-switcher.jsp" />

      <c:choose>
        <c:when test="${not empty sessionScope.user}">
          <a href="${ctx}/profile" class="flex items-center gap-2 hover:bg-surface-container-low px-3 py-1.5 rounded-full transition-colors">
            <c:choose>
              <c:when test="${not empty sessionScope.user.profilePictureUrl}">
                <img src="${sessionScope.user.profilePictureUrl}" alt="" class="w-8 h-8 rounded-full object-cover border border-outline-variant/30">
              </c:when>
              <c:otherwise>
                <div class="w-8 h-8 rounded-full bg-primary-fixed flex items-center justify-center text-primary font-bold text-body-sm">
                  ${fn:toUpperCase(fn:substring(sessionScope.user.username, 0, 1))}
                </div>
              </c:otherwise>
            </c:choose>
            <span class="hidden sm:inline font-body-sm text-body-sm text-on-surface">${sessionScope.user.username}</span>
          </a>
          <a href="${ctx}/api/auth/logout" class="hidden sm:inline-flex items-center gap-1 text-on-surface-variant hover:text-error text-body-sm transition-colors">
            <span class="material-symbols-outlined text-[18px]">logout</span>
          </a>
        </c:when>
        <c:otherwise>
          <a href="${ctx}/auth/login" class="hidden md:inline-block px-4 py-2 text-primary font-body-sm font-medium hover:bg-surface-container-low rounded-lg transition-colors"><fmt:message key="nav.login" /></a>
          <a href="${ctx}/auth/register" class="px-5 py-2.5 bg-primary text-on-primary rounded-xl font-body-sm font-medium shadow-sm hover:bg-primary-container transition-all"><fmt:message key="nav.register" /></a>
        </c:otherwise>
      </c:choose>
    </div>
  </nav>
</header>
<div class="h-[72px]"></div>
