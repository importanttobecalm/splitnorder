<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<jsp:include page="/WEB-INF/views/layout/head.jsp">
  <jsp:param name="titleKey" value="home.title" />
</jsp:include>
<c:set var="navActive" value="home" scope="request" />
<jsp:include page="/WEB-INF/views/layout/nav.jsp" />

<%--
  Studio ekranı — Claude Design tasarımı birebir korunur.
  data-* attribute'leri server-side job durumunu JSX tarafına geçirir;
  main.jsx bunları okuyup EMPTY/PENDING/PROCESSING/COMPLETED state'lerine
  göre upload modal veya audio bind eder.
--%>
<link rel="stylesheet" href="${pageContext.request.contextPath}/static/studio/styles.css">
<link rel="preconnect" href="https://fonts.googleapis.com">
<link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
<link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">

<div id="splitnorder-studio-root"
     data-job-status="${jobStatus}"
     data-job-id="${jobId}"
     data-job-filename="${jobFilename}"
     data-ctx="${pageContext.request.contextPath}"></div>

<script crossorigin src="https://unpkg.com/react@18.3.1/umd/react.production.min.js"></script>
<script crossorigin src="https://unpkg.com/react-dom@18.3.1/umd/react-dom.production.min.js"></script>
<script src="https://unpkg.com/@babel/standalone@7.29.0/babel.min.js"></script>
<%-- JSZip — tarayıcıda ZIP açma için, 96 KB. Tek ZIP fetch + 4 blob URL ile
     audio'lar RAM'den anında çalsın (play/seek 0ms latency). --%>
<script src="https://unpkg.com/jszip@3.10.1/dist/jszip.min.js"></script>

<script type="text/babel" data-presets="react" src="${pageContext.request.contextPath}/static/studio/app.jsx"></script>
<script type="text/babel" data-presets="react" src="${pageContext.request.contextPath}/static/studio/upload-modal.jsx"></script>
<script type="text/babel" data-presets="react" src="${pageContext.request.contextPath}/static/studio/processing-overlay.jsx"></script>
<script type="text/babel" data-presets="react" src="${pageContext.request.contextPath}/static/studio/audio-engine.jsx"></script>
<script type="text/babel" data-presets="react" src="${pageContext.request.contextPath}/static/studio/main.jsx"></script>

<jsp:include page="/WEB-INF/views/layout/footer.jsp" />
