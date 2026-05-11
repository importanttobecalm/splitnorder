<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<jsp:include page="/WEB-INF/views/layout/head.jsp">
  <jsp:param name="titleKey" value="error.500.title" />
</jsp:include>

<main class="min-h-screen flex flex-col items-center justify-center p-margin_mobile md:p-margin_desktop relative overflow-hidden">

  <%-- dekoratif waveform çizgileri (Stitch tasarımı) --%>
  <style>
    .waveform-ambient { background-color: theme('colors.outline-variant'); border-radius: 9999px; }
  </style>
  <div class="absolute top-20 left-10 flex items-end gap-1 opacity-50 z-0">
    <div class="waveform-ambient w-2 h-16"></div>
    <div class="waveform-ambient w-2 h-24"></div>
    <div class="waveform-ambient w-2 h-12"></div>
    <div class="waveform-ambient w-2 h-32"></div>
    <div class="waveform-ambient w-2 h-20"></div>
  </div>
  <div class="absolute bottom-20 right-10 flex items-end gap-1 opacity-50 z-0">
    <div class="waveform-ambient w-2 h-20"></div>
    <div class="waveform-ambient w-2 h-32"></div>
    <div class="waveform-ambient w-2 h-16"></div>
    <div class="waveform-ambient w-2 h-24"></div>
    <div class="waveform-ambient w-2 h-12"></div>
  </div>

  <div class="bg-surface-container-lowest rounded-[24px] shadow-xl p-10 md:p-16 max-w-3xl w-full text-center relative z-10 border border-outline-variant/20 flex flex-col items-center">

    <div class="mb-8 w-48 h-48 relative flex items-center justify-center bg-surface-container-low rounded-full">
      <span class="material-symbols-outlined text-[120px] text-primary opacity-70">cloud_off</span>
    </div>

    <div class="font-display-lg text-[120px] leading-none font-bold bg-gradient-to-br from-primary to-inverse-primary text-transparent bg-clip-text tracking-tighter mb-4">500</div>

    <h1 class="font-display-lg text-display-lg text-on-surface mb-6"><fmt:message key="error.500.title" /></h1>
    <p class="font-body-lg text-body-lg text-on-surface-variant max-w-lg mb-10"><fmt:message key="error.500.message" /></p>

    <div class="flex flex-col sm:flex-row items-center gap-4 mb-12">
      <button onclick="location.reload()" class="bg-primary hover:bg-primary-container text-on-primary font-body-md text-body-md font-medium px-8 py-4 rounded-xl flex items-center gap-2 transition-colors shadow-md">
        <span class="material-symbols-outlined" style="font-variation-settings: 'FILL' 1;">refresh</span>
        <fmt:message key="error.action.retry" />
      </button>
      <a href="${ctx}/" class="border-2 border-primary text-primary hover:bg-primary-fixed font-body-md text-body-md font-medium px-8 py-4 rounded-xl transition-colors">
        <fmt:message key="error.action.home" />
      </a>
    </div>

    <div class="bg-surface-container py-2 px-4 rounded-full flex items-center gap-3 border border-outline-variant/50">
      <div class="w-2.5 h-2.5 rounded-full bg-primary animate-pulse"></div>
      <span class="font-mono-label text-mono-label text-on-surface-variant tracking-wide"><fmt:message key="error.500.status" /></span>
    </div>

    <div class="mt-8 font-mono-label text-mono-label text-outline uppercase tracking-widest">
      <fmt:message key="error.code" />: #SP-2026-B82C
    </div>
  </div>
</main>

<jsp:include page="/WEB-INF/views/layout/footer.jsp" />
