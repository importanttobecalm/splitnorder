<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<jsp:include page="/WEB-INF/views/layout/head.jsp">
  <jsp:param name="titleKey" value="auth.reset.title" />
</jsp:include>

<div class="flex w-full h-screen flex-col md:flex-row">

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

  <div class="w-full md:w-[40%] flex items-center justify-center p-margin_mobile md:p-margin_desktop bg-surface-container-lowest">
    <div class="w-full max-w-[400px] bg-surface-container-lowest p-10 rounded-[16px] soft-shadow">

      <div class="flex justify-end mb-4">
        <jsp:include page="/WEB-INF/views/layout/lang-switcher.jsp" />
      </div>

      <div class="mb-8 text-center md:text-left">
        <h2 class="font-headline-md text-headline-md text-on-primary-fixed mb-2">
          <fmt:message key="auth.reset.title" />
        </h2>
        <p class="font-body-md text-body-md text-on-surface-variant">
          <fmt:message key="auth.reset.subtitle" />
        </p>
      </div>

      <c:if test="${not empty error}">
        <div class="mb-4 p-3 rounded-[10px] bg-error-container text-on-error-container font-body-sm text-body-sm">
          <fmt:message key="auth.error.${error}" />
        </div>
      </c:if>

      <form class="space-y-6" method="post" action="${ctx}/auth/reset-password">
        <input type="hidden" name="token" value="${token}">

        <div>
          <label class="block font-body-sm text-body-sm text-on-surface mb-2 font-medium" for="password">
            <fmt:message key="auth.reset.new_password" />
          </label>
          <div class="relative">
            <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-outline">
              <span class="material-symbols-outlined text-[20px]">lock</span>
            </div>
            <input id="password" name="password" type="password" required minlength="8" placeholder="••••••••"
                   class="block w-full pl-10 pr-3 py-3 border border-surface-dim rounded-[10px] bg-surface-container-lowest text-on-surface placeholder-outline-variant font-body-md text-body-md focus:outline-none focus:ring-2 focus:ring-primary focus:ring-offset-2 transition-shadow">
          </div>
        </div>

        <div>
          <label class="block font-body-sm text-body-sm text-on-surface mb-2 font-medium" for="password_confirm">
            <fmt:message key="auth.field.password_confirm" />
          </label>
          <div class="relative">
            <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-outline">
              <span class="material-symbols-outlined text-[20px]">lock</span>
            </div>
            <input id="password_confirm" name="password_confirm" type="password" required minlength="8" placeholder="••••••••"
                   class="block w-full pl-10 pr-3 py-3 border border-surface-dim rounded-[10px] bg-surface-container-lowest text-on-surface placeholder-outline-variant font-body-md text-body-md focus:outline-none focus:ring-2 focus:ring-primary focus:ring-offset-2 transition-shadow">
          </div>
        </div>

        <div>
          <button type="submit"
                  class="w-full flex justify-center py-3 px-4 border border-transparent rounded-[12px] shadow-sm font-body-md text-body-md font-medium text-on-primary bg-primary hover:bg-primary-container focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary transition-colors">
            <fmt:message key="auth.reset.submit" />
          </button>
        </div>
      </form>

      <div class="mt-8 text-center">
        <p class="font-body-sm text-body-sm text-on-surface-variant">
          <a href="${ctx}/auth/login" class="font-medium text-primary hover:text-primary-container transition-colors">
            <fmt:message key="auth.forgot.back_to_login" />
          </a>
        </p>
      </div>
    </div>
  </div>
</div>

<jsp:include page="/WEB-INF/views/layout/footer.jsp" />
