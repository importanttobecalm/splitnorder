<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="${pageContext.request.locale}">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><spring:message code="auth.verify.title"/> | AI StemSep</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800&display=swap" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.1/font/bootstrap-icons.css" rel="stylesheet">
    <style>
        :root {
            --primary: #e8d8b0;
            --bg-dark: #0a0908;
            --bg-card: #14120f;
            --bg-card-hover: #1c1916;
            --text-primary: #f0ebe0;
            --text-secondary: #8a8378;
            --gradient-1: linear-gradient(135deg, #e8d8b0, #c4a875);
            --error: #e57373;
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
        .card-box {
            width: 100%; max-width: 480px;
            background: var(--bg-card);
            border: 1px solid rgba(232,216,176,0.15);
            border-radius: 20px; padding: 2.5rem;
            text-align: center;
        }
        .icon {
            font-size: 3.5rem;
            margin-bottom: 1rem;
        }
        .icon.error { color: var(--error); }
        .icon.success { color: var(--primary); }
        h1 { font-size: 1.5rem; font-weight: 700; margin-bottom: 0.75rem; }
        p { color: var(--text-secondary); margin-bottom: 1.5rem; }
        .btn-primary-glow {
            display: inline-block; padding: 0.75rem 2rem;
            background: var(--gradient-1); border-radius: 10px;
            color: #1a1611; font-weight: 700; text-decoration: none;
        }
        form { display: flex; gap: 0.5rem; margin-top: 1rem; }
        .form-control {
            flex: 1;
            background: var(--bg-card-hover);
            border: 1px solid rgba(232,216,176,0.15);
            color: var(--text-primary);
            padding: 0.65rem 0.9rem; border-radius: 8px;
        }
        .btn-resend {
            padding: 0.65rem 1.25rem;
            background: transparent;
            border: 1px solid rgba(232,216,176,0.3);
            color: var(--primary);
            border-radius: 8px;
            font-weight: 600;
            cursor: pointer;
        }
    </style>
</head>
<body>

<div class="card-box">
    <c:choose>
        <c:when test="${empty error}">
            <div class="icon success"><i class="bi bi-check-circle-fill"></i></div>
            <h1><spring:message code="auth.verify.success.title"/></h1>
            <p><spring:message code="auth.verify.success.body"/></p>
            <a href="<c:url value='/auth/login'/>" class="btn-primary-glow">
                <spring:message code="auth.login.title"/>
            </a>
        </c:when>
        <c:otherwise>
            <div class="icon error"><i class="bi bi-x-circle-fill"></i></div>
            <h1><spring:message code="auth.verify.error.title"/></h1>
            <p><spring:message code="auth.error.${error}" text="${error}"/></p>
            <form method="post" action="<c:url value='/auth/resend-verification'/>">
                <input type="hidden" name="lang" value="${pageContext.request.locale.language}">
                <input type="email" class="form-control" name="email"
                       placeholder="<spring:message code='auth.email'/>" required>
                <button type="submit" class="btn-resend">
                    <spring:message code="auth.verify.resend"/>
                </button>
            </form>
        </c:otherwise>
    </c:choose>
</div>

</body>
</html>
