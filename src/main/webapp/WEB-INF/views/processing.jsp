<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<jsp:include page="/WEB-INF/views/layout/head.jsp">
  <jsp:param name="titleKey" value="processing.title" />
</jsp:include>
<jsp:include page="/WEB-INF/views/layout/nav.jsp" />

<%-- Job durumu: PENDING | PROCESSING | COMPLETED | FAILED --%>
<%-- Job entity'de progressPercent yok; status'tan türetilir --%>
<c:choose>
  <c:when test="${job.status == 'COMPLETED'}"><c:set var="progress" value="100" /></c:when>
  <c:when test="${job.status == 'PROCESSING'}"><c:set var="progress" value="50" /></c:when>
  <c:when test="${job.status == 'FAILED'}"><c:set var="progress" value="0" /></c:when>
  <c:otherwise><c:set var="progress" value="10" /></c:otherwise>
</c:choose>

<main class="flex-grow flex items-center justify-center pt-12 pb-12 px-margin_mobile md:px-margin_desktop">
  <div class="w-full max-w-[760px] bg-surface-container-lowest rounded-[24px] soft-shadow p-8 md:p-[56px]">

    <%-- Üst: dosya bilgisi --%>
    <div class="flex items-center justify-between mb-10 pb-6 border-b border-outline-variant/30">
      <div class="flex items-center gap-4">
        <div class="w-14 h-14 rounded-xl bg-gradient-to-br from-primary to-inverse-primary shadow-sm flex items-center justify-center">
          <span class="material-symbols-outlined text-on-primary">album</span>
        </div>
        <div>
          <h3 class="font-body-md text-on-surface font-semibold truncate max-w-[200px] md:max-w-xs">${job.originalFilename}</h3>
          <p class="font-mono-label text-mono-label text-on-surface-variant mt-1">${job.modelUsed}</p>
        </div>
      </div>
      <c:if test="${job.status != 'COMPLETED' && job.status != 'FAILED'}">
        <div class="flex items-center gap-2 px-3 py-2 text-on-surface-variant font-body-sm">
          <span class="material-symbols-outlined text-[20px] animate-spin">sync</span>
          <span class="hidden sm:inline"><fmt:message key="status.processing" /></span>
        </div>
      </c:if>
    </div>

    <%-- Headline --%>
    <div class="text-center mb-12">
      <h2 class="font-headline-md text-headline-md text-on-surface mb-3">
        <c:choose>
          <c:when test="${job.status == 'COMPLETED'}"><fmt:message key="processing.done" /></c:when>
          <c:when test="${job.status == 'FAILED'}"><fmt:message key="processing.failed" /></c:when>
          <c:otherwise><fmt:message key="processing.inprogress" /></c:otherwise>
        </c:choose>
      </h2>
      <p class="font-body-lg text-body-lg text-on-surface-variant">
        <c:choose>
          <c:when test="${job.status == 'COMPLETED'}"><fmt:message key="processing.done.sub" /></c:when>
          <c:when test="${job.status == 'FAILED'}">${job.errorMessage}</c:when>
          <c:otherwise><fmt:message key="processing.inprogress.sub" /></c:otherwise>
        </c:choose>
      </p>
    </div>

    <%-- Progress ring + stem list --%>
    <div class="flex flex-col md:flex-row items-center gap-12 md:gap-16 mb-12">
      <%-- Progress ring --%>
      <div class="relative w-[240px] h-[240px] flex-shrink-0 flex items-center justify-center">
        <svg class="w-full h-full -rotate-90" viewBox="0 0 100 100">
          <defs>
            <linearGradient id="gradientStroke" x1="0%" y1="0%" x2="100%" y2="100%">
              <stop offset="0%" stop-color="#E53935"></stop>
              <stop offset="33%" stop-color="#FB8C00"></stop>
              <stop offset="66%" stop-color="#8E24AA"></stop>
              <stop offset="100%" stop-color="#00897B"></stop>
            </linearGradient>
          </defs>
          <circle cx="50" cy="50" r="45" fill="none" stroke="#E1E8F2" stroke-width="6"></circle>
          <circle cx="50" cy="50" r="45" fill="none" stroke="url(#gradientStroke)" stroke-width="6" stroke-linecap="round"
                  stroke-dasharray="282.7" stroke-dashoffset="${282.7 - (282.7 * progress / 100)}"></circle>
        </svg>
        <div class="absolute inset-0 flex flex-col items-center justify-center text-center">
          <span class="font-mono-numeric text-[48px] font-bold tracking-tight text-on-surface mb-1">${progress}%</span>
          <span class="font-body-sm text-on-surface-variant"><fmt:message key="processing.eta" /></span>
        </div>
      </div>

      <%-- Stem list --%>
      <div class="w-full flex-grow space-y-5">
        <c:set var="stems" value="vocals,drums,bass,other" />
        <c:set var="colors" value="#E53935,#FB8C00,#8E24AA,#00897B" />
        <c:forEach var="stem" items="${stems}" varStatus="loop">
          <c:set var="stemPct" value="${progress >= ((loop.index + 1) * 25) ? 100 : (progress > (loop.index * 25) ? ((progress - (loop.index * 25)) * 4) : 0)}" />
          <div class="flex flex-col gap-1.5 ${stemPct == 0 ? 'opacity-60' : ''}">
            <div class="flex justify-between items-end text-sm">
              <span class="font-body-sm font-medium text-on-surface"><fmt:message key="stem.${stem}" /></span>
              <c:choose>
                <c:when test="${stemPct >= 100}"><span class="material-symbols-outlined text-green-500 text-[18px]" style="font-variation-settings: 'FILL' 1;">check_circle</span></c:when>
                <c:when test="${stemPct > 0}"><span class="material-symbols-outlined text-[18px] animate-spin" style="color: ${colors.split(',')[loop.index]};">sync</span></c:when>
                <c:otherwise><div class="w-1.5 h-1.5 rounded-full bg-outline-variant mb-1"></div></c:otherwise>
              </c:choose>
            </div>
            <div class="w-full h-2 bg-surface-container-high rounded-full overflow-hidden">
              <div class="h-full rounded-full transition-all duration-500" style="width: ${stemPct}%; background-color: ${colors.split(',')[loop.index]};"></div>
            </div>
          </div>
        </c:forEach>
      </div>
    </div>

    <%-- Tamamlandı butonu --%>
    <c:if test="${job.status == 'COMPLETED'}">
      <div class="flex justify-center">
        <a href="${ctx}/job/${job.id}/result" class="bg-primary hover:bg-primary-container text-on-primary px-8 py-4 rounded-xl font-body-md font-medium flex items-center gap-2 transition-all shadow-md">
          <span class="material-symbols-outlined">graphic_eq</span>
          <fmt:message key="processing.viewResult" />
        </a>
      </div>
    </c:if>

    <%-- Footer ipucu --%>
    <div class="flex justify-center mt-8">
      <div class="inline-flex items-center gap-2 px-4 py-2 rounded-full bg-surface-container-low text-outline font-body-sm">
        <span>💡</span>
        <span><fmt:message key="processing.tip" /></span>
      </div>
    </div>
  </div>
</main>

<%-- Arka plan polling: kullanıcı flash görmez, sadece COMPLETED/FAILED olunca tek seferlik yenileme.
     Tam sayfa reload yerine /job/{id}/status JSON endpoint'i sessizce sorgulanır. --%>
<c:if test="${job.status == 'PROCESSING' || job.status == 'PENDING'}">
<script>
(function(){
  var jobId = '${job.id}';
  var statusUrl = '${ctx}/job/' + jobId + '/status';
  var pollMs = 3000;
  function poll(){
    fetch(statusUrl, { headers: { 'Accept': 'application/json' }, credentials: 'same-origin' })
      .then(function(r){ return r.ok ? r.json() : null; })
      .then(function(data){
        if (!data || !data.status) { setTimeout(poll, pollMs); return; }
        if (data.status === 'COMPLETED' || data.status === 'FAILED') {
          window.location.href = '${ctx}/job/' + jobId;
        } else {
          setTimeout(poll, pollMs);
        }
      })
      .catch(function(){ setTimeout(poll, pollMs); });
  }
  setTimeout(poll, pollMs);
})();
</script>
</c:if>

<jsp:include page="/WEB-INF/views/layout/site-footer.jsp" />
<jsp:include page="/WEB-INF/views/layout/footer.jsp" />
