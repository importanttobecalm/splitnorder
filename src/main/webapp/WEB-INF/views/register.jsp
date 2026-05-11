<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="${pageContext.request.locale}">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><spring:message code="auth.register.title"/> | AI StemSep</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800&display=swap" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.1/font/bootstrap-icons.css" rel="stylesheet">
    <style>
        :root {
            --primary: #e8d8b0;
            --primary-light: #f5e9c7;
            --accent: #c4a875;
            --bg-dark: #0a0908;
            --bg-card: #14120f;
            --bg-card-hover: #1c1916;
            --text-primary: #f0ebe0;
            --text-secondary: #8a8378;
            --gradient-1: linear-gradient(135deg, #e8d8b0, #c4a875);
            --glow: 0 0 40px rgba(232, 216, 176, 0.18);
            --error: #e57373;
            --info: #c4a875;
        }
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body {
            font-family: 'Inter', sans-serif;
            background: var(--bg-dark);
            color: var(--text-primary);
            min-height: 100vh;
            display: flex; align-items: center; justify-content: center;
            padding: 2rem 1rem;
        }
        body::before {
            content: '';
            position: fixed; top: 0; left: 0; right: 0; bottom: 0;
            background:
                radial-gradient(circle at 20% 50%, rgba(232,216,176,0.10) 0%, transparent 50%),
                radial-gradient(circle at 80% 20%, rgba(196,168,117,0.08) 0%, transparent 50%);
            z-index: -1;
        }
        .auth-card {
            width: 100%; max-width: 460px;
            background: var(--bg-card);
            border: 1px solid rgba(232,216,176,0.15);
            border-radius: 20px; padding: 2.5rem;
            box-shadow: var(--glow);
        }
        .brand { text-align: center; margin-bottom: 1.5rem; }
        .brand a {
            font-weight: 800; font-size: 1.5rem;
            background: var(--gradient-1);
            -webkit-background-clip: text; -webkit-text-fill-color: transparent;
            text-decoration: none;
        }
        h1 { font-size: 1.75rem; font-weight: 700; text-align: center; margin-bottom: 0.5rem; }
        .subtitle { text-align: center; color: var(--text-secondary); margin-bottom: 2rem; font-size: 0.95rem; }
        .form-label { color: var(--text-primary); font-weight: 500; margin-bottom: 0.4rem; }
        .form-control {
            background: var(--bg-card-hover);
            border: 1px solid rgba(232,216,176,0.15);
            color: var(--text-primary);
            padding: 0.75rem 1rem; border-radius: 10px;
        }
        .form-control:focus {
            background: var(--bg-card-hover);
            color: var(--text-primary);
            border-color: var(--primary);
            box-shadow: 0 0 0 3px rgba(232,216,176,0.15);
        }
        .form-text { color: var(--text-secondary); font-size: 0.8rem; }
        .btn-primary-glow {
            display: block; width: 100%; padding: 0.85rem;
            background: var(--gradient-1); border: none; border-radius: 10px;
            color: #1a1611; font-weight: 700; font-size: 1rem;
            cursor: pointer; transition: all 0.3s;
        }
        .btn-primary-glow:hover {
            transform: translateY(-1px);
            box-shadow: 0 0 30px rgba(232,216,176,0.4);
        }
        .alert {
            padding: 0.75rem 1rem; border-radius: 8px;
            margin-bottom: 1.25rem; font-size: 0.9rem;
            border: 1px solid transparent;
        }
        .alert-error {
            background: rgba(229,115,115,0.1);
            border-color: rgba(229,115,115,0.3);
            color: var(--error);
        }
        .footer-link {
            text-align: center; margin-top: 1.5rem;
            color: var(--text-secondary); font-size: 0.9rem;
        }
        .footer-link a { color: var(--primary); text-decoration: none; font-weight: 600; }
    </style>
</head>
<body>

<div class="auth-card">
    <div class="brand">
        <a href="<c:url value='/' />"><i class="bi bi-soundwave"></i> AI StemSep</a>
    </div>
    <h1><spring:message code="auth.register.title"/></h1>
    <p class="subtitle"><spring:message code="auth.register.subtitle"/></p>

    <c:if test="${not empty error}">
        <div class="alert alert-error"><spring:message code="auth.error.${error}" text="${error}"/></div>
    </c:if>

    <form method="post" action="<c:url value='/auth/register'/>" novalidate>
        <input type="hidden" name="lang" value="${pageContext.request.locale.language}">
        <div class="mb-3">
            <label class="form-label" for="username"><spring:message code="auth.username"/></label>
            <input type="text" id="username" name="username" class="form-control" value="${username}" required autofocus>
        </div>
        <div class="mb-3">
            <label class="form-label" for="email"><spring:message code="auth.email"/></label>
            <input type="email" id="email" name="email" class="form-control" value="${email}" required>
        </div>
        <div class="mb-3">
            <label class="form-label" for="password"><spring:message code="auth.password"/></label>
            <input type="password" id="password" name="password" class="form-control" required minlength="8">
            <div class="form-text"><spring:message code="auth.password.hint"/></div>
        </div>
        <button type="submit" class="btn-primary-glow"><spring:message code="auth.register.submit"/></button>
    </form>

    <div class="footer-link">
        <spring:message code="auth.register.haveAccount"/>
        <a href="<c:url value='/auth/login'/>"><spring:message code="auth.login.title"/></a>
    </div>
</div>

</body>
</html>
