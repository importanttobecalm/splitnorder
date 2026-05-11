<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<jsp:include page="/WEB-INF/views/layout/head.jsp">
  <jsp:param name="titleKey" value="error.404.title" />
</jsp:include>

<main class="min-h-screen flex flex-col items-center justify-center p-margin_mobile md:p-margin_desktop">
  <div class="bg-surface-container-lowest rounded-xl soft-shadow p-12 md:p-16 max-w-2xl w-full text-center relative overflow-hidden border border-outline-variant/20">

    <div class="mb-8 flex justify-center">
      <div class="relative w-48 h-48 flex items-center justify-center">
        <span class="material-symbols-outlined text-[120px] text-outline-variant opacity-30 absolute">music_note</span>
        <span class="material-symbols-outlined text-[80px] bg-clip-text text-transparent bg-gradient-to-r from-secondary to-tertiary-fixed-dim absolute transform rotate-12 drop-shadow-md">question_mark</span>
      </div>
    </div>

    <h1 class="font-display-lg text-display-lg bg-clip-text text-transparent bg-gradient-to-r from-secondary via-[#ff8a65] to-tertiary-fixed-dim mb-4">404</h1>
    <h2 class="font-headline-md text-headline-md text-on-surface mb-4"><fmt:message key="error.404.title" /></h2>
    <p class="font-body-lg text-body-lg text-on-surface-variant mb-10 max-w-md mx-auto"><fmt:message key="error.404.message" /></p>

    <div class="flex flex-col sm:flex-row items-center justify-center gap-4 mb-12">
      <a href="${ctx}/" class="bg-primary hover:bg-primary-container text-on-primary font-body-sm text-body-sm py-3 px-6 rounded-lg transition-colors flex items-center gap-2 w-full sm:w-auto justify-center shadow-sm">
        <span class="material-symbols-outlined text-[20px]">home</span>
        <fmt:message key="error.action.home" />
      </a>
      <button onclick="history.back()" class="bg-transparent border border-primary text-primary hover:bg-primary/5 font-body-sm text-body-sm py-3 px-6 rounded-lg transition-colors flex items-center gap-2 w-full sm:w-auto justify-center">
        <span class="material-symbols-outlined text-[20px]">arrow_back</span>
        <fmt:message key="error.action.back" />
      </button>
    </div>

    <div class="border-t border-outline-variant/30 pt-8">
      <p class="font-body-sm text-body-sm text-on-surface-variant mb-4"><fmt:message key="error.404.suggestions" /></p>
      <div class="flex flex-wrap justify-center gap-3">
        <a href="${ctx}/" class="bg-surface-container hover:bg-surface-container-high text-on-surface font-body-sm text-body-sm py-2 px-4 rounded-full transition-colors border border-outline-variant/20"><fmt:message key="nav.home" /></a>
        <a href="${ctx}/history" class="bg-surface-container hover:bg-surface-container-high text-on-surface font-body-sm text-body-sm py-2 px-4 rounded-full transition-colors border border-outline-variant/20"><fmt:message key="nav.history" /></a>
        <a href="${ctx}/upload" class="bg-surface-container hover:bg-surface-container-high text-on-surface font-body-sm text-body-sm py-2 px-4 rounded-full transition-colors border border-outline-variant/20"><fmt:message key="nav.upload" /></a>
      </div>
    </div>
  </div>
  <p class="mt-8 font-mono-label text-mono-label text-outline uppercase tracking-wider"><fmt:message key="error.code" />: #SP-2026-A47F</p>
</main>

<jsp:include page="/WEB-INF/views/layout/footer.jsp" />
