<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<jsp:include page="/WEB-INF/views/layout/head.jsp">
  <jsp:param name="titleKey" value="auth.register.title" />
</jsp:include>

<div class="flex w-full min-h-screen flex-col lg:flex-row">

  <%-- ====== Sol panel (marka) ====== --%>
  <div class="hidden lg:flex lg:w-[60%] bg-surface-container-low dotted-grid relative items-center justify-center overflow-hidden">
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
  <div class="w-full lg:w-[40%] flex items-center justify-center p-margin_mobile md:p-margin_desktop bg-surface-container-lowest">
    <div class="w-full max-w-[440px] bg-surface-container-lowest p-10 rounded-[16px] soft-shadow">

      <div class="flex justify-end mb-4">
        <jsp:include page="/WEB-INF/views/layout/lang-switcher.jsp" />
      </div>

      <div class="mb-8 text-center md:text-left">
        <h2 class="font-headline-md text-headline-md text-on-primary-fixed mb-2">
          <fmt:message key="auth.register.title" />
        </h2>
        <p class="font-body-md text-body-md text-on-surface-variant">
          <fmt:message key="auth.register.subtitle" />
        </p>
      </div>

      <c:if test="${not empty error}">
        <div class="mb-4 p-3 rounded-[10px] bg-error-container text-on-error-container font-body-sm text-body-sm">
          <fmt:message key="auth.error.${error}" />
        </div>
      </c:if>

      <form class="space-y-5" method="post" action="${ctx}/auth/register">

        <%-- Kullanıcı adı --%>
        <div class="flex flex-col gap-1.5">
          <label class="font-body-sm text-body-sm text-on-surface font-medium" for="username">
            <fmt:message key="auth.field.username" />
          </label>
          <div class="relative">
            <span class="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-outline">alternate_email</span>
            <input id="username" name="username" type="text" required minlength="3" maxlength="32" value="${param.username}"
                   placeholder="yusufb"
                   class="w-full h-12 pl-10 pr-4 bg-surface rounded-[10px] border border-surface-variant focus:border-primary focus:ring-4 focus:ring-primary/10 transition-all font-body-md text-on-surface placeholder:text-outline-variant outline-none">
          </div>
        </div>

        <%-- E-posta --%>
        <div class="flex flex-col gap-1.5">
          <label class="font-body-sm text-body-sm text-on-surface font-medium" for="email">
            <fmt:message key="auth.field.email" />
          </label>
          <div class="relative">
            <span class="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-outline">mail</span>
            <input id="email" name="email" type="email" required value="${param.email}"
                   placeholder="ornek@mail.com"
                   class="w-full h-12 pl-10 pr-4 bg-surface rounded-[10px] border border-surface-variant focus:border-primary focus:ring-4 focus:ring-primary/10 transition-all font-body-md text-on-surface placeholder:text-outline-variant outline-none">
          </div>
        </div>

        <%-- Parola --%>
        <div class="flex flex-col gap-1.5">
          <label class="font-body-sm text-body-sm text-on-surface font-medium" for="password">
            <fmt:message key="auth.field.password" />
          </label>
          <div class="relative">
            <span class="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-outline">lock</span>
            <input id="password" name="password" type="password" required minlength="8"
                   placeholder="En az 8 karakter"
                   class="w-full h-12 pl-10 pr-10 bg-surface rounded-[10px] border border-surface-variant focus:border-primary focus:ring-4 focus:ring-primary/10 transition-all font-body-md text-on-surface placeholder:text-outline-variant outline-none">
            <button type="button" class="absolute right-3 top-1/2 -translate-y-1/2 text-outline hover:text-on-surface transition-colors"
                    onclick="var p=document.getElementById('password'); p.type=p.type==='password'?'text':'password';">
              <span class="material-symbols-outlined">visibility</span>
            </button>
          </div>
        </div>

        <%-- Parola tekrar --%>
        <div class="flex flex-col gap-1.5">
          <label class="font-body-sm text-body-sm text-on-surface font-medium" for="password_confirm">
            <fmt:message key="auth.field.password_confirm" />
          </label>
          <div class="relative">
            <span class="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-outline">lock</span>
            <input id="password_confirm" name="password_confirm" type="password" required
                   class="w-full h-12 pl-10 pr-4 bg-surface rounded-[10px] border border-surface-variant focus:border-primary focus:ring-4 focus:ring-primary/10 transition-all font-body-md text-on-surface placeholder:text-outline-variant outline-none">
          </div>
        </div>

        <%-- Şartlar checkbox --%>
        <div class="flex items-start gap-3 mt-4">
          <input id="terms" name="terms" type="checkbox" required
                 class="peer h-5 w-5 mt-0.5 appearance-none rounded-[4px] border border-outline-variant checked:border-primary checked:bg-primary transition-all cursor-pointer">
          <label for="terms" class="font-body-sm text-body-sm text-on-surface-variant leading-tight cursor-pointer">
            <fmt:message key="auth.register.terms" />
          </label>
        </div>

        <button type="submit"
                class="w-full h-12 bg-primary text-on-primary font-body-md font-semibold rounded-xl hover:bg-primary-container transition-colors mt-4 flex items-center justify-center">
          <fmt:message key="auth.register.submit" />
        </button>

        <%-- Google ile devam et --%>
        <div class="mt-6 relative">
          <div class="absolute inset-0 flex items-center"><div class="w-full border-t border-surface-variant"></div></div>
          <div class="relative flex justify-center font-body-sm text-body-sm">
            <span class="px-2 bg-surface-container-lowest text-outline"><fmt:message key="auth.or" /></span>
          </div>
        </div>

        <a href="${ctx}/api/auth/google/login"
           class="w-full flex justify-center items-center py-3 px-4 border border-surface-variant rounded-[12px] bg-surface-container-lowest font-body-md text-body-md font-medium text-on-surface hover:bg-surface-container-low focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary transition-colors">
          <svg class="h-5 w-5 mr-2" viewBox="0 0 24 24"><path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"/><path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/><path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"/><path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"/></svg>
          <fmt:message key="auth.login.google" />
        </a>
      </form>

      <div class="mt-8 text-center border-t border-surface-variant pt-6">
        <p class="font-body-sm text-body-sm text-on-surface-variant">
          <fmt:message key="auth.register.have_account" />
          <a href="${ctx}/auth/login" class="text-primary font-semibold hover:underline">
            <fmt:message key="auth.register.login_link" />
          </a>
        </p>
      </div>
    </div>
  </div>
</div>

<jsp:include page="/WEB-INF/views/layout/footer.jsp" />
