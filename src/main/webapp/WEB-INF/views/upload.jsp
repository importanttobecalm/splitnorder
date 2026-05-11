<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<jsp:include page="/WEB-INF/views/layout/head.jsp">
  <jsp:param name="titleKey" value="upload.title" />
</jsp:include>
<c:set var="navActive" value="upload" scope="request" />
<jsp:include page="/WEB-INF/views/layout/nav.jsp" />

<%-- ViewState: empty | selected --%>
<c:set var="state" value="${empty selectedFile ? 'empty' : 'selected'}" />

<main class="flex-grow pt-8 pb-16 px-4 flex flex-col items-center">
  <div class="text-center mb-12 max-w-2xl mx-auto">
    <h1 class="text-[40px] font-headline-md font-bold text-on-surface mb-4"><fmt:message key="upload.title" /></h1>
    <p class="text-[16px] font-body-md text-on-surface-variant"><fmt:message key="upload.subtitle" /></p>
  </div>

  <c:if test="${not empty error}">
    <div class="w-full max-w-[920px] mb-6 p-4 rounded-xl bg-error-container text-on-error-container font-body-sm">
      <fmt:message key="upload.error.${error}" />
    </div>
  </c:if>

  <form id="uploadForm" method="post" action="${ctx}/upload" enctype="multipart/form-data" class="w-full max-w-[920px]">

    <c:choose>
      <%-- ===== STATE: Boş dropzone ===== --%>
      <c:when test="${state == 'empty'}">
        <div id="dropzone" class="bg-surface-container-lowest rounded-[24px] soft-shadow p-14 mb-8">
          <label for="fileInput" class="border-2 border-dashed border-primary border-opacity-50 rounded-xl flex flex-col items-center justify-center py-16 px-8 cursor-pointer hover:bg-surface-container-low transition-colors block">
            <div class="w-[140px] h-[140px] bg-primary-fixed rounded-full flex items-center justify-center mb-6">
              <span class="material-symbols-outlined text-[88px] text-primary">cloud_upload</span>
            </div>
            <h2 class="text-[28px] font-headline-md font-bold text-on-surface mb-2"><fmt:message key="upload.dropzone.title" /></h2>
            <span class="text-[14px] font-body-sm text-on-surface-variant mb-6"><fmt:message key="auth.or" /></span>
            <span class="bg-primary text-on-primary px-8 py-3 rounded-[12px] font-body-md font-medium hover:bg-primary-container transition-colors inline-flex items-center gap-2 mb-4">
              <span class="material-symbols-outlined text-[20px]">folder</span>
              <fmt:message key="upload.choose" />
            </span>
            <p class="text-[13px] font-body-sm text-outline-variant"><fmt:message key="upload.formats" /></p>
            <input id="fileInput" name="file" type="file" accept=".mp3,.wav,.flac" class="hidden" onchange="document.getElementById('uploadForm').submit()">
          </label>
        </div>
      </c:when>

      <%-- ===== STATE: Dosya seçili ===== --%>
      <c:otherwise>
        <div class="bg-surface-container-lowest rounded-xl p-gutter soft-shadow border border-surface-dim mb-6">
          <div class="flex justify-between items-center mb-6">
            <h2 class="font-headline-sm text-headline-sm text-on-surface"><fmt:message key="upload.selected" /></h2>
          </div>
          <div class="flex flex-col md:flex-row items-start md:items-center justify-between gap-6 bg-surface-container-low rounded-lg p-6 border border-outline-variant/30">
            <div class="flex items-center gap-6 flex-grow">
              <div class="w-16 h-16 rounded-lg bg-gradient-to-tr from-primary to-inverse-primary shadow-sm flex items-center justify-center text-on-primary">
                <span class="material-symbols-outlined text-3xl">music_note</span>
              </div>
              <div class="flex flex-col overflow-hidden">
                <span class="font-body-lg text-body-lg text-on-surface font-semibold truncate max-w-xs">${selectedFile.originalFilename}</span>
                <span class="font-mono-label text-mono-label text-outline uppercase mt-1">${selectedFile.formatLabel}</span>
              </div>
            </div>
            <div class="flex items-center gap-4">
              <a href="${ctx}/upload" class="font-body-sm text-on-surface-variant hover:text-error px-4 py-2 rounded-lg transition-colors border border-transparent hover:border-error/20">
                <fmt:message key="upload.remove" />
              </a>
              <button type="submit" name="action" value="start" class="h-14 px-8 rounded-xl font-body-lg text-on-primary font-semibold flex items-center gap-2 active:scale-95 shadow-md" style="background: linear-gradient(135deg, #E53935 0%, #FB8C00 100%);">
                <span class="material-symbols-outlined">auto_awesome</span>
                <fmt:message key="upload.start" />
              </button>
            </div>
          </div>
        </div>
      </c:otherwise>
    </c:choose>

    <%-- ===== Gelişmiş ayarlar (her durumda) ===== --%>
    <div class="bg-surface-container-lowest rounded-[16px] soft-shadow p-6 mb-12">
      <div class="flex items-center gap-2 mb-6 border-b border-outline-variant/30 pb-4">
        <span class="material-symbols-outlined text-primary">settings</span>
        <h3 class="font-body-md text-body-md font-medium text-on-surface"><fmt:message key="upload.advanced" /></h3>
      </div>

      <div>
        <label class="block font-mono-label text-mono-label text-outline mb-3 uppercase"><fmt:message key="upload.model.label" /></label>
        <div class="flex flex-wrap gap-2">
          <c:forEach var="model" items="${['htdemucs','htdemucs_ft','mdx_extra']}">
            <label class="cursor-pointer">
              <input type="radio" name="model" value="${model}" class="peer sr-only" ${model == 'htdemucs_ft' ? 'checked' : ''}>
              <div class="px-4 py-2 rounded-full border border-outline-variant text-on-surface-variant font-body-sm peer-checked:bg-primary peer-checked:text-on-primary peer-checked:border-primary transition-colors">
                <fmt:message key="upload.model.${model}" />
              </div>
            </label>
          </c:forEach>
        </div>
      </div>
    </div>

    <%-- ===== İpuçları ===== --%>
    <div class="w-full flex flex-wrap justify-center gap-4">
      <c:forEach var="i" begin="1" end="3">
        <div class="bg-surface-container-lowest/60 backdrop-blur-sm border border-outline-variant/30 px-4 py-2 rounded-full flex items-center gap-2">
          <span class="font-body-sm text-body-sm text-on-surface-variant"><fmt:message key="upload.tip${i}" /></span>
        </div>
      </c:forEach>
    </div>
  </form>
</main>

<jsp:include page="/WEB-INF/views/layout/site-footer.jsp" />
<jsp:include page="/WEB-INF/views/layout/footer.jsp" />
