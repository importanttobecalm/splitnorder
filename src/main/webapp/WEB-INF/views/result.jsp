<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<jsp:include page="/WEB-INF/views/layout/head.jsp">
  <jsp:param name="titleKey" value="studio.title" />
</jsp:include>
<jsp:include page="/WEB-INF/views/layout/nav.jsp" />

<style>
  .stem-card { background: rgb(var(--card-rgb, 255 255 255) / 0.95); border-radius: 16px; box-shadow: 0 4px 24px rgba(30,50,90,0.06); border: 1px solid rgba(193,199,211,0.3); }
  .waveform-bar { width: 4px; min-height: 2px; border-radius: 2px; }
</style>

<main class="max-w-[1240px] mx-auto px-margin_mobile md:px-margin_desktop pt-8 pb-32">

  <%-- Üst başlık --%>
  <div class="flex flex-col md:flex-row justify-between items-start md:items-center mb-8 gap-4">
    <div class="flex items-center gap-4">
      <div class="w-16 h-16 rounded-xl bg-gradient-to-br from-primary to-inverse-primary shadow-sm flex items-center justify-center text-on-primary">
        <span class="material-symbols-outlined text-[28px]">graphic_eq</span>
      </div>
      <div>
        <h1 class="font-headline-md text-headline-md text-on-surface mb-1">${job.originalFilename}</h1>
        <p class="font-mono-label text-mono-label text-on-surface-variant uppercase">${job.modelUsed} · ${fn:replace(fn:substring(job.createdAt, 0, 16), "T", " ")}</p>
      </div>
    </div>
    <a href="${ctx}/job/${job.id}/download-all" class="bg-primary text-on-primary px-6 py-3 rounded-xl font-body-md font-medium hover:bg-primary-container transition-colors inline-flex items-center gap-2 shadow-sm">
      <span class="material-symbols-outlined">download</span>
      <fmt:message key="studio.downloadAll" />
    </a>
  </div>

  <%-- 4 stem kart (grid layout — Stitch'in absolute konumuna karşılık) --%>
  <c:set var="stemList" value="vocals,drums,bass,other" />
  <c:set var="stemColors" value="#E53935,#FB8C00,#5E35B1,#00897B" />
  <c:set var="stemIcons" value="mic,radio,graphic_eq,music_note" />

  <div class="grid grid-cols-1 md:grid-cols-2 gap-gutter mb-12">
    <c:forEach var="stem" items="${stemList}" varStatus="loop">
      <c:set var="color" value="${stemColors.split(',')[loop.index]}" />
      <c:set var="icon" value="${stemIcons.split(',')[loop.index]}" />

      <div class="stem-card p-6 flex flex-col gap-4" style="border-left: 4px solid ${color};">
        <div class="flex justify-between items-start">
          <div class="flex items-center gap-2">
            <span class="material-symbols-outlined" style="color: ${color};">${icon}</span>
            <h3 class="font-headline-sm text-[20px] text-on-surface uppercase font-bold"><fmt:message key="stem.${stem}" /></h3>
          </div>
          <div class="flex gap-2">
            <button title="Solo" class="w-8 h-8 rounded bg-surface-variant text-on-surface-variant font-mono-label hover:bg-surface-dim transition-colors">S</button>
            <button title="Mute" class="w-8 h-8 rounded bg-surface-variant text-on-surface-variant font-mono-label hover:bg-surface-dim transition-colors">M</button>
            <a href="${ctx}/job/${job.id}/download/${stem}" class="w-8 h-8 rounded bg-surface-variant text-on-surface-variant hover:bg-surface-dim transition-colors flex items-center justify-center">
              <span class="material-symbols-outlined text-[18px]">download</span>
            </a>
          </div>
        </div>

        <%-- Waveform mock (gerçekte ${stem}.peaks.json'dan render edilir) --%>
        <div class="h-16 flex items-end justify-between w-full gap-[1px]">
          <c:forEach var="h" items="${[20,40,30,60,80,50,70,90,100,60,40,30,50,20,10,30,20,10,40,60,80,50,70,90,100,60,40,30,50,20,10,30]}">
            <div class="waveform-bar" style="height: ${h}%; background-color: ${color};"></div>
          </c:forEach>
        </div>

        <%-- Volume slider --%>
        <div class="flex items-center gap-2">
          <span class="material-symbols-outlined text-outline text-[16px]">volume_up</span>
          <input type="range" min="0" max="100" value="80" class="w-full h-1 bg-surface-variant rounded-full appearance-none" style="accent-color: ${color};">
        </div>

        <%-- HTML5 audio (gerçek dosya bağlı) --%>
        <audio controls class="w-full mt-2">
          <source src="${ctx}/job/${job.id}/stream/${stem}" type="audio/wav">
        </audio>
      </div>
    </c:forEach>
  </div>

  <%-- Orijinal master --%>
  <div class="bg-surface-container-lowest rounded-xl soft-shadow p-6 mb-12">
    <div class="flex items-center gap-4">
      <div class="w-16 h-16 rounded-lg bg-gradient-to-br from-primary-container to-primary flex items-center justify-center text-on-primary">
        <span class="material-symbols-outlined text-[28px]">album</span>
      </div>
      <div class="flex-1">
        <h2 class="font-headline-sm text-[18px] text-on-surface mb-1"><fmt:message key="studio.master" /></h2>
        <p class="font-mono-numeric text-on-surface-variant">${job.originalFilename}</p>
      </div>
    </div>
    <audio controls class="w-full mt-4">
      <source src="${ctx}/job/${job.id}/stream/original" type="audio/mpeg">
    </audio>
  </div>

  <%-- Tekrar ayrıştır / Geçmişe dön --%>
  <div class="flex flex-col sm:flex-row gap-4 justify-center">
    <a href="${ctx}/upload" class="bg-surface-container-lowest border border-primary text-primary px-6 py-3 rounded-xl font-body-md font-medium hover:bg-primary-fixed/20 transition-colors inline-flex items-center justify-center gap-2">
      <span class="material-symbols-outlined">upload_file</span>
      <fmt:message key="studio.uploadNew" />
    </a>
    <a href="${ctx}/history" class="bg-surface-container-lowest border border-outline-variant text-on-surface-variant px-6 py-3 rounded-xl font-body-md font-medium hover:bg-surface-container-low transition-colors inline-flex items-center justify-center gap-2">
      <span class="material-symbols-outlined">history</span>
      <fmt:message key="nav.history" />
    </a>
  </div>
</main>

<jsp:include page="/WEB-INF/views/layout/site-footer.jsp" />
<jsp:include page="/WEB-INF/views/layout/footer.jsp" />
