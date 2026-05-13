<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<jsp:include page="/WEB-INF/views/layout/head.jsp">
  <jsp:param name="titleKey" value="home.title" />
</jsp:include>
<c:set var="navActive" value="home" scope="request" />
<jsp:include page="/WEB-INF/views/layout/nav.jsp" />

<%--
  Studio ekranı — Claude Design'dan alınan tasarım birebir korunur.
  React 18 + JSX dosyaları CDN üzerinden Babel ile tarayıcıda transpile
  edilir; JSX kaynaklarına dokunulmaz, asset olarak servis edilir.
--%>
<link rel="stylesheet" href="${pageContext.request.contextPath}/static/studio/styles.css">
<link rel="preconnect" href="https://fonts.googleapis.com">
<link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
<link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">

<div id="splitnorder-studio-root"></div>

<script crossorigin src="https://unpkg.com/react@18.3.1/umd/react.production.min.js"></script>
<script crossorigin src="https://unpkg.com/react-dom@18.3.1/umd/react-dom.production.min.js"></script>
<script src="https://unpkg.com/@babel/standalone@7.29.0/babel.min.js"></script>

<script type="text/babel" data-presets="react" src="${pageContext.request.contextPath}/static/studio/app.jsx"></script>
<script type="text/babel" data-presets="react" src="${pageContext.request.contextPath}/static/studio/main.jsx"></script>

<jsp:include page="/WEB-INF/views/layout/footer.jsp" />
