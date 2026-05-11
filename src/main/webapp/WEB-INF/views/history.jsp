<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<jsp:include page="/WEB-INF/views/layout/head.jsp">
  <jsp:param name="titleKey" value="history.title" />
</jsp:include>
<c:set var="navActive" value="history" scope="request" />
<jsp:include page="/WEB-INF/views/layout/nav.jsp" />

<c:set var="view" value="${empty param.view ? 'grid' : param.view}" />

<main class="max-w-[1240px] mx-auto px-margin_mobile md:px-margin_desktop pt-8 pb-24">

  <%-- Üst satır: başlık + view toggle + arama --%>
  <div class="flex flex-col md:flex-row justify-between items-start md:items-center gap-4 mb-8">
    <div>
      <h1 class="font-headline-md text-headline-md text-on-surface mb-1"><fmt:message key="history.title" /></h1>
      <p class="font-body-sm text-body-sm text-on-surface-variant"><fmt:message key="history.subtitle" />: ${fn:length(jobs)}</p>
    </div>

    <div class="flex items-center gap-3">
      <%-- Arama --%>
      <form method="get" action="${ctx}/history" class="relative">
        <input type="hidden" name="view" value="${view}">
        <input type="text" name="q" value="${param.q}" placeholder="<fmt:message key='history.search' />"
               class="pl-10 pr-4 py-2 bg-surface-container-lowest rounded-lg border border-outline-variant/50 font-body-sm focus:border-primary outline-none">
        <span class="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-outline text-[18px]">search</span>
      </form>

      <%-- View toggle --%>
      <div class="flex bg-surface-container-lowest rounded-lg border border-outline-variant/30 overflow-hidden">
        <a href="${ctx}/history?view=grid" class="px-3 py-2 ${view == 'grid' ? 'bg-primary text-on-primary' : 'text-on-surface-variant'} transition-colors">
          <span class="material-symbols-outlined text-[20px]">grid_view</span>
        </a>
        <a href="${ctx}/history?view=list" class="px-3 py-2 ${view == 'list' ? 'bg-primary text-on-primary' : 'text-on-surface-variant'} transition-colors">
          <span class="material-symbols-outlined text-[20px]">view_list</span>
        </a>
      </div>
    </div>
  </div>

  <c:choose>
    <%-- ===== STATE: Boş ===== --%>
    <c:when test="${empty jobs}">
      <div class="flex flex-col items-center justify-center py-24">
        <div class="w-32 h-32 rounded-full bg-surface-container-low flex items-center justify-center mb-6">
          <span class="material-symbols-outlined text-[64px] text-outline-variant">folder_open</span>
        </div>
        <h2 class="font-headline-sm text-headline-sm text-on-surface mb-2"><fmt:message key="history.empty.title" /></h2>
        <p class="font-body-md text-body-md text-on-surface-variant mb-8 text-center max-w-md"><fmt:message key="history.empty.message" /></p>
        <a href="${ctx}/upload" class="bg-primary text-on-primary px-8 py-3 rounded-xl font-body-md font-medium hover:bg-primary-container transition-colors inline-flex items-center gap-2">
          <span class="material-symbols-outlined">upload_file</span>
          <fmt:message key="home.cta.upload" />
        </a>
      </div>
    </c:when>

    <%-- ===== STATE: Grid view ===== --%>
    <c:when test="${view == 'grid'}">
      <div class="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-gutter">
        <c:forEach var="job" items="${jobs}">
          <a href="${ctx}/job/${job.id}/result" class="bg-surface-container-lowest rounded-xl overflow-hidden soft-shadow hover:shadow-lg transition-all group">
            <div class="h-[160px] relative bg-gradient-to-br from-primary-fixed via-tertiary-fixed to-secondary-fixed">
              <div class="absolute top-3 left-3 bg-surface-container-lowest/90 backdrop-blur-sm px-2 py-1 rounded-lg flex items-center gap-1">
                <c:choose>
                  <c:when test="${job.status == 'COMPLETED'}">
                    <span class="material-symbols-outlined text-[14px] text-green-600" style="font-variation-settings: 'FILL' 1;">check_circle</span>
                    <span class="font-body-sm text-[11px] font-bold text-green-800"><fmt:message key="status.completed" /></span>
                  </c:when>
                  <c:when test="${job.status == 'PROCESSING'}">
                    <span class="material-symbols-outlined text-[14px] text-primary animate-spin">sync</span>
                    <span class="font-body-sm text-[11px] font-bold text-primary"><fmt:message key="status.processing" /></span>
                  </c:when>
                  <c:when test="${job.status == 'FAILED'}">
                    <span class="material-symbols-outlined text-[14px] text-error" style="font-variation-settings: 'FILL' 1;">error</span>
                    <span class="font-body-sm text-[11px] font-bold text-error"><fmt:message key="status.failed" /></span>
                  </c:when>
                  <c:otherwise>
                    <span class="material-symbols-outlined text-[14px] text-outline">schedule</span>
                    <span class="font-body-sm text-[11px] font-bold text-outline"><fmt:message key="status.pending" /></span>
                  </c:otherwise>
                </c:choose>
              </div>
              <c:if test="${not empty job.durationLabel}">
                <div class="absolute bottom-3 right-3 bg-inverse-surface/80 backdrop-blur-sm px-2 py-1 rounded-lg text-on-primary font-mono-numeric text-[12px]">${job.durationLabel}</div>
              </c:if>
            </div>
            <div class="p-4">
              <h3 class="font-body-sm text-body-sm font-bold text-on-surface truncate">${job.originalFilename}</h3>
              <p class="font-mono-label text-mono-label text-on-surface-variant truncate mt-1">${job.modelUsed}</p>
              <div class="flex items-center gap-1.5 mt-3">
                <div class="w-2.5 h-2.5 rounded-full bg-[#E53935]"></div>
                <div class="w-2.5 h-2.5 rounded-full bg-[#FB8C00]"></div>
                <div class="w-2.5 h-2.5 rounded-full bg-[#8E24AA]"></div>
                <div class="w-2.5 h-2.5 rounded-full bg-[#00897B]"></div>
              </div>
              <div class="mt-3 pt-3 border-t border-outline-variant/30 flex items-center justify-between">
                <span class="font-body-sm text-[12px] text-on-surface-variant">
                  <fmt:formatDate value="${job.createdAtDate}" type="both" dateStyle="short" timeStyle="short" />
                </span>
                <span class="material-symbols-outlined text-[18px] text-outline group-hover:text-primary transition-colors">arrow_forward</span>
              </div>
            </div>
          </a>
        </c:forEach>
      </div>
    </c:when>

    <%-- ===== STATE: List view ===== --%>
    <c:otherwise>
      <div class="bg-surface-container-lowest rounded-xl soft-shadow overflow-hidden">
        <table class="w-full text-left">
          <thead class="bg-surface-container-low border-b border-outline-variant/30">
            <tr class="font-mono-label text-mono-label text-outline uppercase tracking-wider">
              <th class="px-6 py-3"><fmt:message key="history.col.file" /></th>
              <th class="px-6 py-3"><fmt:message key="history.col.model" /></th>
              <th class="px-6 py-3"><fmt:message key="history.col.status" /></th>
              <th class="px-6 py-3"><fmt:message key="history.col.date" /></th>
              <th class="px-6 py-3"></th>
            </tr>
          </thead>
          <tbody>
            <c:forEach var="job" items="${jobs}">
              <tr class="border-b border-outline-variant/20 hover:bg-surface-container-low transition-colors">
                <td class="px-6 py-4">
                  <a href="${ctx}/job/${job.id}/result" class="flex items-center gap-3">
                    <div class="w-10 h-10 rounded-lg bg-gradient-to-br from-primary to-inverse-primary flex items-center justify-center text-on-primary">
                      <span class="material-symbols-outlined text-[20px]">music_note</span>
                    </div>
                    <div>
                      <div class="font-body-md text-body-md font-medium text-on-surface truncate max-w-xs">${job.originalFilename}</div>
                    </div>
                  </a>
                </td>
                <td class="px-6 py-4 font-mono-label text-on-surface-variant">${job.modelUsed}</td>
                <td class="px-6 py-4">
                  <c:choose>
                    <c:when test="${job.status == 'COMPLETED'}"><span class="px-3 py-1 bg-green-100 text-green-800 rounded-full text-[11px] font-bold"><fmt:message key="status.completed" /></span></c:when>
                    <c:when test="${job.status == 'PROCESSING'}"><span class="px-3 py-1 bg-primary-fixed text-primary rounded-full text-[11px] font-bold"><fmt:message key="status.processing" /></span></c:when>
                    <c:when test="${job.status == 'FAILED'}"><span class="px-3 py-1 bg-error-container text-on-error-container rounded-full text-[11px] font-bold"><fmt:message key="status.failed" /></span></c:when>
                    <c:otherwise><span class="px-3 py-1 bg-surface-container text-outline rounded-full text-[11px] font-bold"><fmt:message key="status.pending" /></span></c:otherwise>
                  </c:choose>
                </td>
                <td class="px-6 py-4 font-body-sm text-on-surface-variant">
                  <fmt:formatDate value="${job.createdAtDate}" type="both" dateStyle="short" timeStyle="short" />
                </td>
                <td class="px-6 py-4 text-right">
                  <a href="${ctx}/job/${job.id}/result" class="text-primary hover:text-primary-container">
                    <span class="material-symbols-outlined">arrow_forward</span>
                  </a>
                </td>
              </tr>
            </c:forEach>
          </tbody>
        </table>
      </div>
    </c:otherwise>
  </c:choose>
</main>

<jsp:include page="/WEB-INF/views/layout/site-footer.jsp" />
<jsp:include page="/WEB-INF/views/layout/footer.jsp" />
