<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<jsp:include page="/WEB-INF/views/layout/head.jsp">
  <jsp:param name="titleKey" value="profile.title" />
</jsp:include>
<jsp:include page="/WEB-INF/views/layout/nav.jsp" />

<%-- Aktif sekme: ${param.tab} (account | security | preferences | language | notifications | data) --%>
<c:set var="tab" value="${empty param.tab ? 'account' : param.tab}" />

<main class="max-w-[1240px] mx-auto px-margin_mobile md:px-margin_desktop pt-8 pb-24 flex flex-col md:flex-row gap-8 items-start">

  <%-- Sol sidebar --%>
  <aside class="w-full md:w-[260px] flex-shrink-0 bg-surface-container-lowest rounded-xl soft-shadow overflow-hidden md:sticky md:top-[96px]">
    <div class="p-4">
      <h2 class="font-mono-label text-mono-label text-outline uppercase mb-4 px-3"><fmt:message key="profile.settings" /></h2>
      <nav class="flex flex-col gap-1">
        <c:forEach var="t" items="${['account','security','preferences','language','notifications','data']}">
          <a href="${ctx}/profile?tab=${t}" class="flex items-center gap-3 px-3 py-2 ${tab == t ? 'bg-primary-fixed text-primary border-l-4 border-primary rounded-r-lg' : 'text-on-surface-variant hover:bg-surface-container-low rounded-lg'} font-body-sm text-body-sm transition-colors">
            <span class="material-symbols-outlined text-xl">
              <c:choose>
                <c:when test="${t == 'account'}">person</c:when>
                <c:when test="${t == 'security'}">security</c:when>
                <c:when test="${t == 'preferences'}">tune</c:when>
                <c:when test="${t == 'language'}">language</c:when>
                <c:when test="${t == 'notifications'}">notifications</c:when>
                <c:otherwise>storage</c:otherwise>
              </c:choose>
            </span>
            <fmt:message key="profile.tab.${t}" />
          </a>
        </c:forEach>
      </nav>
    </div>
    <div class="p-4 border-t border-outline-variant/30">
      <form method="post" action="${ctx}/profile/delete" onsubmit="return confirm('<fmt:message key="profile.delete.confirm" />');">
        <button type="submit" class="flex items-center gap-3 px-3 py-2 text-error hover:bg-error-container/50 rounded-lg font-body-sm transition-colors w-full">
          <span class="material-symbols-outlined text-xl">delete</span>
          <fmt:message key="profile.delete.action" />
        </button>
      </form>
    </div>
  </aside>

  <%-- Sağ içerik --%>
  <div class="w-full flex-grow flex flex-col gap-8">
    <c:choose>

      <%-- ===== Hesap bilgileri ===== --%>
      <c:when test="${tab == 'account'}">
        <section class="bg-surface-container-lowest rounded-xl soft-shadow p-8">
          <h2 class="font-headline-sm text-headline-sm text-on-surface mb-6"><fmt:message key="profile.tab.account" /></h2>

          <c:if test="${param.saved == '1'}">
            <div class="mb-6 p-3 rounded-[10px] bg-primary-fixed text-on-primary-fixed font-body-sm"><fmt:message key="profile.saved" /></div>
          </c:if>
          <c:if test="${not empty param.error}">
            <div class="mb-6 p-3 rounded-[10px] bg-error-container text-on-error-container font-body-sm">
              <c:choose>
                <c:when test="${param.error == 'username_taken'}"><fmt:message key="auth.error.usernameExists" /></c:when>
                <c:otherwise><fmt:message key="profile.error.usernameInvalid" /></c:otherwise>
              </c:choose>
            </div>
          </c:if>

          <form method="post" action="${ctx}/profile/update" class="flex flex-col gap-6">
            <div class="flex flex-col md:flex-row gap-8 items-start">
              <div class="flex flex-col items-center gap-3">
                <c:choose>
                  <c:when test="${not empty sessionScope.user.profilePictureUrl}">
                    <img src="${sessionScope.user.profilePictureUrl}" alt="" class="w-24 h-24 rounded-full object-cover shadow-sm">
                  </c:when>
                  <c:otherwise>
                    <div class="w-24 h-24 rounded-full bg-gradient-to-br from-primary-container to-primary flex items-center justify-center text-on-primary font-headline-md text-headline-md">
                      ${fn:toUpperCase(fn:substring(sessionScope.user.username, 0, 2))}
                    </div>
                  </c:otherwise>
                </c:choose>
                <span class="font-mono-label text-mono-label text-on-surface-variant uppercase">${sessionScope.user.authProvider}</span>
              </div>

              <div class="grid grid-cols-1 md:grid-cols-2 gap-6 w-full">
                <div class="flex flex-col gap-1 md:col-span-2">
                  <label class="font-mono-label text-mono-label text-on-surface-variant uppercase"><fmt:message key="auth.field.username" /></label>
                  <input type="text" name="username" value="${sessionScope.user.username}" class="bg-surface-container-lowest border border-outline-variant/50 rounded-lg px-4 py-2 font-body-sm focus:border-primary focus:ring-4 focus:ring-primary/10 outline-none transition-all">
                </div>
                <div class="flex flex-col gap-1 md:col-span-2">
                  <label class="font-mono-label text-mono-label text-on-surface-variant uppercase flex justify-between">
                    <span><fmt:message key="auth.field.email" /></span>
                    <c:if test="${sessionScope.user.emailVerified}">
                      <span class="text-green-600 flex items-center gap-1">
                        <span class="material-symbols-outlined text-[12px]">check_circle</span>
                        <fmt:message key="profile.email.verified" />
                      </span>
                    </c:if>
                  </label>
                  <input type="email" value="${sessionScope.user.email}" disabled class="bg-surface-container-low border border-outline-variant/50 rounded-lg px-4 py-2 font-body-sm text-on-surface-variant cursor-not-allowed">
                </div>
              </div>
            </div>

            <div class="flex justify-end pt-4 border-t border-outline-variant/20">
              <button type="submit" class="bg-primary text-on-primary font-body-sm px-6 py-2 rounded-xl hover:bg-primary-container transition-colors">
                <fmt:message key="profile.save" />
              </button>
            </div>
          </form>
        </section>
      </c:when>

      <%-- ===== Güvenlik (LOCAL kullanıcı için şifre değiştirme) ===== --%>
      <c:when test="${tab == 'security'}">
        <section class="bg-surface-container-lowest rounded-xl soft-shadow p-8">
          <h2 class="font-headline-sm text-headline-sm text-on-surface mb-6"><fmt:message key="profile.tab.security" /></h2>
          <c:choose>
            <c:when test="${sessionScope.user.authProvider == 'LOCAL'}">
              <c:if test="${param.saved == '1'}">
                <div class="mb-6 p-3 rounded-[10px] bg-primary-fixed text-on-primary-fixed font-body-sm"><fmt:message key="profile.password.changed" /></div>
              </c:if>
              <c:if test="${not empty param.error}">
                <div class="mb-6 p-3 rounded-[10px] bg-error-container text-on-error-container font-body-sm">
                  <c:choose>
                    <c:when test="${param.error == 'invalid_current'}"><fmt:message key="profile.error.invalidCurrent" /></c:when>
                    <c:otherwise><fmt:message key="profile.error.passwordShort" /></c:otherwise>
                  </c:choose>
                </div>
              </c:if>
              <form method="post" action="${ctx}/profile/password" class="flex flex-col gap-6 max-w-md">
                <div class="flex flex-col gap-1">
                  <label class="font-mono-label text-mono-label text-on-surface-variant uppercase"><fmt:message key="profile.password.current" /></label>
                  <input type="password" name="currentPassword" required class="bg-surface-container-lowest border border-outline-variant/50 rounded-lg px-4 py-2 font-body-sm focus:border-primary outline-none">
                </div>
                <div class="flex flex-col gap-1">
                  <label class="font-mono-label text-mono-label text-on-surface-variant uppercase"><fmt:message key="profile.password.new" /></label>
                  <input type="password" name="newPassword" required minlength="8" class="bg-surface-container-lowest border border-outline-variant/50 rounded-lg px-4 py-2 font-body-sm focus:border-primary outline-none">
                </div>
                <button type="submit" class="bg-primary text-on-primary font-body-sm px-6 py-2 rounded-xl hover:bg-primary-container transition-colors self-end">
                  <fmt:message key="profile.password.change" />
                </button>
              </form>
            </c:when>
            <c:otherwise>
              <div class="flex items-center gap-4 p-6 bg-surface-container-low rounded-lg">
                <span class="material-symbols-outlined text-primary text-[32px]">verified_user</span>
                <p class="font-body-md text-body-md text-on-surface-variant"><fmt:message key="profile.security.googleAccount" /></p>
              </div>
            </c:otherwise>
          </c:choose>
        </section>
      </c:when>

      <%-- ===== Diğer sekmeler (placeholder) ===== --%>
      <c:otherwise>
        <section class="bg-surface-container-lowest rounded-xl soft-shadow p-8">
          <h2 class="font-headline-sm text-headline-sm text-on-surface mb-6"><fmt:message key="profile.tab.${tab}" /></h2>
          <p class="font-body-md text-body-md text-on-surface-variant"><fmt:message key="profile.coming_soon" /></p>
        </section>
      </c:otherwise>
    </c:choose>
  </div>
</main>

<jsp:include page="/WEB-INF/views/layout/site-footer.jsp" />
<jsp:include page="/WEB-INF/views/layout/footer.jsp" />
