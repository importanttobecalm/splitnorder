<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<jsp:include page="/WEB-INF/views/layout/head.jsp">
  <jsp:param name="titleKey" value="home.title" />
</jsp:include>
<c:set var="navActive" value="home" scope="request" />
<jsp:include page="/WEB-INF/views/layout/nav.jsp" />

<main class="pt-12 pb-24 px-margin_mobile md:px-margin_desktop max-w-[1440px] mx-auto">

  <%-- ===== Hero ===== --%>
  <section class="flex flex-col items-center text-center max-w-4xl mx-auto mb-32">
    <div class="inline-flex items-center gap-2 bg-surface-container-lowest px-4 py-2 rounded-full border border-outline-variant/50 soft-shadow mb-8 mt-8">
      <span class="text-xl">🎵</span>
      <span class="font-mono-label text-mono-label text-on-surface-variant uppercase tracking-wider"><fmt:message key="home.eyebrow" /></span>
    </div>

    <h1 class="font-display-lg text-display-lg text-on-surface mb-6 max-w-3xl leading-tight">
      <fmt:message key="home.hero.title.part1" />
      <span class="text-primary relative inline-block">
        <fmt:message key="home.hero.title.part2" />
        <svg class="absolute w-full h-3 -bottom-1 left-0 text-primary-fixed-dim" preserveAspectRatio="none" viewBox="0 0 100 10">
          <path d="M0 5 Q 50 10 100 5" fill="none" stroke="currentColor" stroke-width="4"></path>
        </svg>
      </span>
    </h1>

    <p class="font-body-lg text-body-lg text-on-surface-variant mb-10 max-w-2xl mx-auto leading-relaxed">
      <fmt:message key="home.hero.subtitle" />
    </p>

    <div class="flex flex-col sm:flex-row items-center justify-center gap-4">
      <a href="${ctx}/upload" class="bg-primary hover:bg-primary-container text-on-primary px-8 py-4 rounded-xl font-body-md font-medium flex items-center justify-center gap-2 transition-all shadow-md hover:shadow-lg transform hover:-translate-y-0.5">
        <span class="material-symbols-outlined text-[20px]" style="font-variation-settings: 'FILL' 1;">upload_file</span>
        <fmt:message key="home.cta.upload" />
      </a>
      <a href="${ctx}/history" class="bg-transparent hover:bg-primary-fixed/30 text-primary px-8 py-4 rounded-xl font-body-md font-medium flex items-center justify-center gap-2 border border-primary transition-all">
        <span class="material-symbols-outlined text-[20px]">history</span>
        <fmt:message key="home.cta.history" />
      </a>
    </div>
  </section>

  <%-- ===== Stem kartları ===== --%>
  <section class="relative w-full max-w-5xl mx-auto h-[400px] mb-32 hidden md:block">
    <div class="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 z-20 bg-surface-container-lowest p-6 rounded-2xl soft-shadow border border-outline-variant/20 flex flex-col items-center w-48 h-48">
      <div class="w-16 h-16 rounded-full bg-primary-container flex items-center justify-center text-on-primary mb-3">
        <span class="material-symbols-outlined text-[32px]">graphic_eq</span>
      </div>
      <span class="font-mono-label text-mono-label text-on-surface-variant">FULL_TRACK.WAV</span>
    </div>
    <div class="absolute top-[40px] left-[100px] z-30 bg-surface-container-lowest px-3 py-3 rounded-xl soft-shadow flex items-center gap-3 w-48">
      <div class="w-1.5 h-10 bg-[#E53935] rounded-full"></div>
      <span class="font-mono-label text-mono-label text-on-surface-variant">VOCALS</span>
    </div>
    <div class="absolute top-[40px] right-[100px] z-30 bg-surface-container-lowest px-3 py-3 rounded-xl soft-shadow flex items-center gap-3 w-48">
      <div class="w-1.5 h-10 bg-[#FF9800] rounded-full"></div>
      <span class="font-mono-label text-mono-label text-on-surface-variant">DRUMS</span>
    </div>
    <div class="absolute bottom-[40px] left-[100px] z-30 bg-surface-container-lowest px-3 py-3 rounded-xl soft-shadow flex items-center gap-3 w-48">
      <div class="w-1.5 h-10 bg-[#9C27B0] rounded-full"></div>
      <span class="font-mono-label text-mono-label text-on-surface-variant">BASS</span>
    </div>
    <div class="absolute bottom-[40px] right-[100px] z-30 bg-surface-container-lowest px-3 py-3 rounded-xl soft-shadow flex items-center gap-3 w-48">
      <div class="w-1.5 h-10 bg-[#009688] rounded-full"></div>
      <span class="font-mono-label text-mono-label text-on-surface-variant">OTHER</span>
    </div>
  </section>

  <%-- ===== Özellikler ===== --%>
  <section class="grid grid-cols-1 md:grid-cols-3 gap-6 mb-32">
    <div class="bg-surface-container-lowest p-8 rounded-2xl soft-shadow border border-outline-variant/20">
      <div class="w-12 h-12 rounded-full bg-primary-fixed/50 flex items-center justify-center text-primary mb-6"><span class="material-symbols-outlined">bolt</span></div>
      <h3 class="font-headline-sm text-headline-sm text-on-surface mb-3"><fmt:message key="home.feature1.title" /></h3>
      <p class="font-body-sm text-body-sm text-on-surface-variant leading-relaxed"><fmt:message key="home.feature1.desc" /></p>
    </div>
    <div class="bg-surface-container-lowest p-8 rounded-2xl soft-shadow border border-outline-variant/20">
      <div class="w-12 h-12 rounded-full bg-tertiary-fixed/50 flex items-center justify-center text-tertiary mb-6"><span class="material-symbols-outlined">tune</span></div>
      <h3 class="font-headline-sm text-headline-sm text-on-surface mb-3"><fmt:message key="home.feature2.title" /></h3>
      <p class="font-body-sm text-body-sm text-on-surface-variant leading-relaxed"><fmt:message key="home.feature2.desc" /></p>
    </div>
    <div class="bg-surface-container-lowest p-8 rounded-2xl soft-shadow border border-outline-variant/20">
      <div class="w-12 h-12 rounded-full bg-secondary-fixed/50 flex items-center justify-center text-secondary mb-6"><span class="material-symbols-outlined">history</span></div>
      <h3 class="font-headline-sm text-headline-sm text-on-surface mb-3"><fmt:message key="home.feature3.title" /></h3>
      <p class="font-body-sm text-body-sm text-on-surface-variant leading-relaxed"><fmt:message key="home.feature3.desc" /></p>
    </div>
  </section>

  <%-- ===== Nasıl çalışır ===== --%>
  <section class="mb-16">
    <h2 class="font-headline-md text-headline-md text-center text-on-surface mb-16"><fmt:message key="home.howto.title" /></h2>
    <div class="relative flex flex-col md:flex-row justify-between gap-8 max-w-5xl mx-auto px-4">
      <div class="hidden md:block absolute top-1/2 left-[10%] right-[10%] h-[2px] border-t-2 border-dashed border-outline-variant/40 -z-10 -translate-y-1/2"></div>
      <c:forEach var="i" begin="1" end="3">
        <div class="flex-1 flex flex-col items-center text-center bg-surface-container-lowest p-8 rounded-2xl soft-shadow border border-outline-variant/10 z-10">
          <div class="w-16 h-16 rounded-full ${i == 2 ? 'bg-primary-fixed text-primary' : 'bg-surface-container text-outline'} flex items-center justify-center font-mono-numeric text-xl font-bold mb-6">0${i}</div>
          <h4 class="font-body-lg text-body-lg font-semibold text-on-surface"><fmt:message key="home.howto.step${i}" /></h4>
        </div>
      </c:forEach>
    </div>
  </section>
</main>

<jsp:include page="/WEB-INF/views/layout/site-footer.jsp" />
<jsp:include page="/WEB-INF/views/layout/footer.jsp" />
