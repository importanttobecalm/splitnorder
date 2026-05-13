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
      <img src="${ctx}/static/img/logo.png" alt="Splitnorder" class="w-8 h-8 rounded-md object-contain">
      <span class="text-headline-sm font-headline-sm font-bold text-primary tracking-tight">Splitnorder</span>
    </a>

    <%-- Linkler — "Yükle" sayfası kaldırıldı, yerine sağdaki "Yeni Ayır" CTA butonu kullanılır --%>
    <div class="hidden md:flex items-center gap-8">
      <a href="${ctx}/" class="${navActive == 'home' ? 'text-primary font-bold border-b-2 border-primary pb-1' : 'text-on-surface-variant hover:text-primary'} font-body-sm text-body-sm transition-all"><fmt:message key="nav.home" /></a>
      <a href="${ctx}/history" class="${navActive == 'history' ? 'text-primary font-bold border-b-2 border-primary pb-1' : 'text-on-surface-variant hover:text-primary'} font-body-sm text-body-sm transition-all"><fmt:message key="nav.history" /></a>
    </div>

    <%-- Aksiyonlar (yeni ayır + dil + profil/giriş) --%>
    <div class="flex items-center gap-3">
      <c:if test="${not empty sessionScope.user}">
        <%-- ZIP indir dropdown — sadece açık bir COMPLETED job varken görünür.
             MP3 hafif (~16MB) tarayıcı/casual için, WAV master (~160MB) stüdyo için. --%>
        <c:if test="${not empty jobId && jobStatus == 'COMPLETED'}">
          <div class="relative" data-zipdd>
            <button type="button" data-zipdd-toggle
                    class="inline-flex items-center gap-1.5 px-4 py-2 border border-outline-variant/50 text-on-surface rounded-xl font-body-sm font-medium hover:bg-surface-container-low transition-colors">
              <span class="material-symbols-outlined text-[18px]">download</span>
              <fmt:message key="nav.downloadZip" />
              <span class="material-symbols-outlined text-[16px]">expand_more</span>
            </button>
            <div data-zipdd-menu
                 class="hidden absolute right-0 mt-2 w-64 bg-surface-container-lowest border border-outline-variant/40 rounded-xl shadow-lg overflow-hidden z-50">
              <a href="${ctx}/job/${jobId}/download-all?format=mp3"
                 class="block px-4 py-3 hover:bg-surface-container-low transition-colors">
                <div class="font-body-md text-body-md font-medium text-on-surface"><fmt:message key="nav.zipMp3" /></div>
                <div class="font-body-sm text-body-sm text-on-surface-variant"><fmt:message key="nav.zipMp3.sub" /></div>
              </a>
              <a href="${ctx}/job/${jobId}/download-all?format=wav"
                 class="block px-4 py-3 hover:bg-surface-container-low transition-colors border-t border-outline-variant/30">
                <div class="font-body-md text-body-md font-medium text-on-surface"><fmt:message key="nav.zipWav" /></div>
                <div class="font-body-sm text-body-sm text-on-surface-variant"><fmt:message key="nav.zipWav.sub" /></div>
              </a>
            </div>
          </div>
          <script>
            (function () {
              var wrap = document.querySelector('[data-zipdd]');
              if (!wrap) return;
              var btn = wrap.querySelector('[data-zipdd-toggle]');
              var menu = wrap.querySelector('[data-zipdd-menu]');
              btn.addEventListener('click', function (e) {
                e.stopPropagation();
                menu.classList.toggle('hidden');
              });
              document.addEventListener('click', function (e) {
                if (!wrap.contains(e.target)) menu.classList.add('hidden');
              });
            })();
          </script>
        </c:if>

        <a href="${ctx}/?upload=1"
           class="inline-flex items-center gap-1.5 px-4 py-2 bg-primary text-on-primary rounded-xl font-body-sm font-medium shadow-sm hover:bg-primary-container transition-all">
          <span class="material-symbols-outlined text-[18px]">add</span>
          <fmt:message key="nav.newSplit" />
        </a>
      </c:if>

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
