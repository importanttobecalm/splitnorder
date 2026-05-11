<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="${pageContext.response.locale.language}">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><spring:message code="nav.profile" text="Profil"/> | AI StemSep</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.1/font/bootstrap-icons.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Outfit:wght@300;400;600;700&display=swap" rel="stylesheet">
    <style>
        :root {
            --primary: #8A2BE2;
            --primary-dark: #7B21D0;
            --bg-dark: #0F0F13;
            --card-bg: #1A1A24;
            --text-main: #FFFFFF;
            --text-dim: #A0A0B0;
            --accent: #00F2FE;
        }

        body {
            background-color: var(--bg-dark);
            color: var(--text-main);
            font-family: 'Outfit', sans-serif;
            min-height: 100vh;
            display: flex;
            flex-direction: column;
        }

        /* Navbar Style */
        .navbar {
            background: rgba(15, 15, 19, 0.8) !important;
            backdrop-filter: blur(10px);
            border-bottom: 1px solid rgba(255, 255, 255, 0.1);
            padding: 1rem 0;
        }
        .navbar-brand {
            font-weight: 700;
            color: var(--text-main) !important;
            font-size: 1.5rem;
        }
        .nav-link {
            color: var(--text-dim) !important;
            font-weight: 500;
            margin: 0 10px;
            transition: 0.3s;
        }
        .nav-link:hover, .nav-link.active {
            color: var(--accent) !important;
        }

        /* Profile Card */
        .profile-container {
            flex: 1;
            display: flex;
            align-items: center;
            justify-content: center;
            padding: 40px 20px;
        }
        .profile-card {
            background: var(--card-bg);
            border-radius: 24px;
            padding: 40px;
            width: 100%;
            max-width: 500px;
            border: 1px solid rgba(255, 255, 255, 0.05);
            box-shadow: 0 20px 40px rgba(0,0,0,0.4);
            text-align: center;
            position: relative;
            overflow: hidden;
        }
        .profile-card::before {
            content: '';
            position: absolute;
            top: 0; left: 0; width: 100%; height: 4px;
            background: linear-gradient(90deg, var(--primary), var(--accent));
        }

        .profile-avatar {
            width: 120px;
            height: 120px;
            border-radius: 50%;
            background: linear-gradient(135deg, var(--primary), var(--accent));
            margin: 0 auto 24px;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 3rem;
            color: white;
            border: 4px solid rgba(255,255,255,0.1);
            object-fit: cover;
        }

        .profile-name {
            font-size: 1.8rem;
            font-weight: 700;
            margin-bottom: 8px;
        }
        .profile-email {
            color: var(--text-dim);
            font-size: 1.1rem;
            margin-bottom: 30px;
        }

        .info-grid {
            text-align: left;
            background: rgba(0,0,0,0.2);
            border-radius: 16px;
            padding: 20px;
            margin-bottom: 30px;
        }
        .info-item {
            display: flex;
            justify-content: space-between;
            padding: 10px 0;
            border-bottom: 1px solid rgba(255,255,255,0.05);
        }
        .info-item:last-child { border-bottom: none; }
        .info-label { color: var(--text-dim); font-size: 0.9rem; }
        .info-value { font-weight: 600; color: var(--accent); }

        .btn-logout {
            background: rgba(255, 50, 50, 0.1);
            color: #FF4D4D;
            border: 1px solid rgba(255, 50, 50, 0.2);
            padding: 12px 30px;
            border-radius: 12px;
            font-weight: 600;
            transition: 0.3s;
            width: 100%;
            text-decoration: none;
            display: inline-block;
        }
        .btn-logout:hover {
            background: #FF4D4D;
            color: white;
            transform: translateY(-2px);
        }

        footer {
            padding: 20px;
            text-align: center;
            color: var(--text-dim);
            font-size: 0.9rem;
            border-top: 1px solid rgba(255, 255, 255, 0.05);
        }
    </style>
</head>
<body>

<!-- Navbar -->
<nav class="navbar navbar-expand-lg sticky-top">
    <div class="container">
        <a class="navbar-brand" href="<c:url value='/' />">
            <i class="bi bi-soundwave"></i> AI StemSep
        </a>
        <div class="collapse navbar-collapse" id="navbarNav">
            <ul class="navbar-nav me-auto">
                <li class="nav-item"><a class="nav-link" href="<c:url value='/' />"><spring:message code="nav.home"/></a></li>
                <li class="nav-item"><a class="nav-link" href="<c:url value='/upload' />"><spring:message code="nav.upload"/></a></li>
                <li class="nav-item"><a class="nav-link" href="<c:url value='/history' />"><spring:message code="nav.history"/></a></li>
            </ul>
            <ul class="navbar-nav ms-auto">
                <li class="nav-item"><a class="nav-link active" href="<c:url value='/api/auth/profile' />"><i class="bi bi-person-circle"></i> <spring:message code="nav.profile" text="Profil"/></a></li>
            </ul>
        </div>
    </div>
</nav>

<div class="profile-container">
    <div class="profile-card">
        <!-- Avatar -->
        <c:choose>
            <c:when test="${not empty user.profilePictureUrl}">
                <img src="${user.profilePictureUrl}" alt="Avatar" class="profile-avatar">
            </c:when>
            <c:otherwise>
                <div class="profile-avatar">
                    ${user.username.substring(0, 1).toUpperCase()}
                </div>
            </c:otherwise>
        </c:choose>

        <h2 class="profile-name">${user.username}</h2>
        <p class="profile-email">${user.email}</p>

        <div class="info-grid">
            <div class="info-item">
                <span class="info-label"><spring:message code="profile.provider" text="Giriş Yöntemi"/></span>
                <span class="info-value">
                    <c:choose>
                        <c:when test="${user.authProvider == 'LOCAL'}">E-posta</c:when>
                        <c:when test="${user.authProvider == 'GOOGLE'}">Google</c:when>
                        <c:otherwise>${user.authProvider}</c:otherwise>
                    </c:choose>
                </span>
            </div>
            <div class="info-item">
                <span class="info-label"><spring:message code="profile.joined" text="Katılım Tarihi"/></span>
                <span class="info-value">
                    <fmt:parseDate value="${user.createdAt}" pattern="yyyy-MM-dd'T'HH:mm" var="parsedDate" type="both" />
                    <fmt:formatDate value="${parsedDate}" pattern="dd.MM.yyyy" />
                </span>
            </div>
        </div>

        <a href="<c:url value='/api/auth/logout' />" class="btn-logout">
            <i class="bi bi-box-arrow-right"></i> <spring:message code="profile.logout" text="Çıkış Yap"/>
        </a>
    </div>
</div>

<footer>
    <spring:message code="footer.text"/>
</footer>

</body>
</html>
