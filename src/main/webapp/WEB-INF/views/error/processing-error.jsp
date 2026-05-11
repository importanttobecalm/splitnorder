<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<jsp:include page="/WEB-INF/views/layout/head.jsp">
  <jsp:param name="titleKey" value="error.processing.title" />
</jsp:include>
<jsp:include page="/WEB-INF/views/layout/nav.jsp" />

<main class="flex-grow flex items-center justify-center pt-12 pb-12 px-margin_mobile md:px-margin_desktop">
  <div class="max-w-2xl w-full text-center flex flex-col items-center">

    <%-- Kırık stem kart illüstrasyonu --%>
    <div class="relative w-48 h-64 bg-surface-container-lowest rounded-2xl soft-shadow border border-outline-variant/30 flex items-center justify-center mb-10">
      <div class="absolute left-0 top-0 bottom-0 w-2 bg-error rounded-l-2xl"></div>
      <div class="flex items-center gap-[2px] opacity-30">
        <div class="w-[2px] h-4 bg-on-surface-variant"></div>
        <div class="w-[2px] h-8 bg-on-surface-variant"></div>
        <div class="w-[2px] h-12 bg-on-surface-variant"></div>
        <div class="w-[4px] h-16 bg-error skew-x-12 mx-1"></div>
        <div class="w-[2px] h-10 bg-on-surface-variant"></div>
        <div class="w-[2px] h-14 bg-on-surface-variant"></div>
        <div class="w-[2px] h-6 bg-on-surface-variant"></div>
      </div>
      <div class="absolute -top-4 -right-4 w-10 h-10 bg-surface-container-lowest rounded-full shadow-sm border border-outline-variant/20 flex items-center justify-center">
        <span class="material-symbols-outlined text-error" style="font-variation-settings: 'FILL' 1;">warning</span>
      </div>
    </div>

    <h1 class="font-display-lg text-display-lg text-on-surface mb-4"><fmt:message key="error.processing.title" /></h1>
    <p class="font-body-lg text-body-lg text-on-surface-variant max-w-lg mb-10"><fmt:message key="error.processing.message" /></p>

    <div class="flex flex-col sm:flex-row gap-4 mb-12 w-full justify-center">
      <a href="${ctx}/upload" class="bg-primary text-on-primary font-body-sm px-6 py-3 rounded-xl flex items-center justify-center gap-2 hover:bg-primary-container transition-colors shadow-sm">
        <span class="material-symbols-outlined" style="font-size: 18px;">upload</span>
        <fmt:message key="error.processing.retry" />
      </a>
      <a href="${ctx}/history" class="bg-transparent text-primary border border-primary font-body-sm px-6 py-3 rounded-xl flex items-center justify-center hover:bg-primary-fixed/20 transition-colors">
        <fmt:message key="nav.history" />
      </a>
    </div>

    <%-- Teknik ayrıntılar (collapsible) --%>
    <c:if test="${not empty job.errorMessage}">
      <div class="w-full max-w-md text-left">
        <details class="group bg-surface-container-lowest/50 rounded-lg border border-outline-variant/30 overflow-hidden">
          <summary class="font-body-sm text-on-surface-variant px-4 py-3 cursor-pointer list-none flex justify-between items-center hover:bg-surface-container-low transition-colors">
            <fmt:message key="error.processing.technical" />
            <span class="material-symbols-outlined text-on-surface-variant transition-transform group-open:rotate-180">arrow_drop_down</span>
          </summary>
          <div class="px-4 py-3 bg-surface-container-highest/20 border-t border-outline-variant/20">
            <pre class="font-mono-label text-mono-label text-on-surface-variant whitespace-pre-wrap break-all">${job.errorMessage}</pre>
          </div>
        </details>
      </div>
    </c:if>
  </div>
</main>

<jsp:include page="/WEB-INF/views/layout/site-footer.jsp" />
<jsp:include page="/WEB-INF/views/layout/footer.jsp" />
