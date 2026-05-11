<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<jsp:include page="/WEB-INF/views/layout/head.jsp">
  <jsp:param name="titleKey" value="auth.login.title" />
</jsp:include>

<div class="flex w-full h-screen flex-col md:flex-row">

  <%-- ====== Sol panel (marka) ====== --%>
  <div class="hidden md:flex md:w-[60%] bg-surface-container-low dotted-grid relative items-center justify-center overflow-hidden">
    <div class="absolute top-[20%] left-[10%]    w-64 h-32 waveform-card-red    rounded-xl blur-sm opacity-60 transform -rotate-6"></div>
    <div class="absolute bottom-[20%] right-[15%] w-72 h-40 waveform-card-teal   rounded-xl blur-sm opacity-60 transform rotate-12"></div>
    <div class="absolute top-[10%] right-[20%]   w-48 h-24 waveform-card-orange rounded-xl blur-sm opacity-60 transform rotate-3"></div>
    <div class="absolute bottom-[30%] left-[15%] w-56 h-32 waveform-card-purple rounded-xl blur-sm opacity-60 transform -rotate-12"></div>

    <div class="z-10 flex flex-col items-center text-center px-gutter max-w-lg">
      <img alt="Splitnorder" class="w-32 h-32 mb-8 object-contain drop-shadow-xl" src="${ctx}/static/img/logo.png" onerror="this.style.display='none'">
      <h1 class="font-display-lg text-display-lg text-on-primary-fixed mb-4 tracking-tight">Splitnorder</h1>
      <p class="font-body-lg text-body-lg text-on-surface-variant font-medium">
        <fmt:message key="brand.tagline" />
      </p>
    </div>
  </div>

  <%-- ====== Sağ panel (form) ====== --%>
  <div class="w-full md:w-[40%] flex items-center justify-center p-margin_mobile md:p-margin_desktop bg-surface-container-lowest">
    <div class="w-full max-w-[400px] bg-surface-container-lowest p-10 rounded-[16px] soft-shadow">

      <%-- dil değiştirici sağ üst --%>
      <div class="flex justify-end mb-4">
        <jsp:include page="/WEB-INF/views/layout/lang-switcher.jsp" />
      </div>

      <div class="mb-8 text-center md:text-left">
        <h2 class="font-headline-md text-headline-md text-on-primary-fixed mb-2">
          <fmt:message key="auth.login.title" />
        </h2>
        <p class="font-body-md text-body-md text-on-surface-variant">
          <fmt:message key="auth.login.subtitle" />
        </p>
      </div>

      <%-- Hata mesajı (Controller model.addAttribute("error", "...") koyarsa) --%>
      <c:if test="${not empty error}">
        <div class="mb-4 p-3 rounded-[10px] bg-error-container text-on-error-container font-body-sm text-body-sm">
          <fmt:message key="auth.error.${error}" />
        </div>
      </c:if>

      <%-- Başarı mesajı (örn. doğrulama maili gönderildi) --%>
      <c:if test="${not empty message}">
        <div class="mb-4 p-3 rounded-[10px] bg-primary-fixed text-on-primary-fixed font-body-sm text-body-sm">
          <fmt:message key="auth.info.${message}" />
        </div>
      </c:if>

      <form class="space-y-6" method="post" action="${ctx}/auth/login">
        <div>
          <label class="block font-body-sm text-body-sm text-on-surface mb-2 font-medium" for="email">
            <fmt:message key="auth.field.email" />
          </label>
          <div class="relative">
            <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-outline">
              <span class="material-symbols-outlined text-[20px]">mail</span>
            </div>
            <input id="email" name="email" type="email" required value="${param.email}"
                   placeholder="ornek@mail.com"
                   class="block w-full pl-10 pr-3 py-3 border border-surface-dim rounded-[10px] bg-surface-container-lowest text-on-surface placeholder-outline-variant font-body-md text-body-md focus:outline-none focus:ring-2 focus:ring-primary focus:ring-offset-2 transition-shadow">
          </div>
        </div>

        <div>
          <label class="block font-body-sm text-body-sm text-on-surface mb-2 font-medium" for="password">
            <fmt:message key="auth.field.password" />
          </label>
          <div class="relative">
            <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-outline">
              <span class="material-symbols-outlined text-[20px]">lock</span>
            </div>
            <input id="password" name="password" type="password" required placeholder="••••••••"
                   class="block w-full pl-10 pr-10 py-3 border border-surface-dim rounded-[10px] bg-surface-container-lowest text-on-surface placeholder-outline-variant font-body-md text-body-md focus:outline-none focus:ring-2 focus:ring-primary focus:ring-offset-2 transition-shadow">
            <div class="absolute inset-y-0 right-0 pr-3 flex items-center cursor-pointer text-outline hover:text-on-surface transition-colors"
                 onclick="var p=document.getElementById('password'); p.type=p.type==='password'?'text':'password';">
              <span class="material-symbols-outlined text-[20px]">visibility</span>
            </div>
          </div>
        </div>

        <div class="flex items-center justify-between">
          <div class="flex items-center">
            <input id="remember-me" name="remember-me" type="checkbox"
                   class="h-4 w-4 text-primary focus:ring-primary border-outline-variant rounded-[4px]">
            <label for="remember-me" class="ml-2 block font-body-sm text-body-sm text-on-surface-variant">
              <fmt:message key="auth.login.remember" />
            </label>
          </div>
          <div class="font-body-sm text-body-sm">
            <a class="font-medium text-primary hover:text-primary-container transition-colors" href="${ctx}/auth/forgot-password">
              <fmt:message key="auth.login.forgot" />
            </a>
          </div>
        </div>

        <div>
          <button type="submit"
                  class="w-full flex justify-center py-3 px-4 border border-transparent rounded-[12px] shadow-sm font-body-md text-body-md font-medium text-on-primary bg-primary hover:bg-primary-container focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary transition-colors">
            <fmt:message key="auth.login.submit" />
          </button>
        </div>

        <div class="mt-6 relative">
          <div class="absolute inset-0 flex items-center"><div class="w-full border-t border-surface-variant"></div></div>
          <div class="relative flex justify-center font-body-sm text-body-sm">
            <span class="px-2 bg-surface-container-lowest text-outline"><fmt:message key="auth.or" /></span>
          </div>
        </div>

        <div>
          <a href="${ctx}/api/auth/google/login"
             class="w-full flex justify-center items-center py-3 px-4 border border-surface-variant rounded-[12px] bg-surface-container-lowest font-body-md text-body-md font-medium text-on-surface hover:bg-surface-container-low focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary transition-colors">
            <svg class="h-5 w-5 mr-2" viewBox="0 0 24 24"><path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"/><path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/><path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"/><path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"/></svg>
            <fmt:message key="auth.login.google" />
          </a>
        </div>
      </form>

      <div class="mt-8 text-center">
        <p class="font-body-sm text-body-sm text-on-surface-variant">
          <fmt:message key="auth.login.no_account" />
          <a href="${ctx}/auth/register" class="font-medium text-primary hover:text-primary-container transition-colors">
            <fmt:message key="auth.login.register_link" />
          </a>
        </p>
      </div>
    </div>
  </div>
</div>

<jsp:include page="/WEB-INF/views/layout/footer.jsp" />
