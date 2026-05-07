<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="${pageContext.response.locale}">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><spring:message code="history.title"/> - <spring:message code="app.title"/></title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800&display=swap" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.1/font/bootstrap-icons.css" rel="stylesheet">
    <style>
        :root {
            --primary: #e8d8b0;
            --primary-light: #f5e9c7;
            --primary-dark: #c4b289;
            --accent: #c4a875;
            --bg-dark: #0a0908;
            --bg-card: #14120f;
            --bg-card-hover: #1c1916;
            --text-primary: #f0ebe0;
            --text-secondary: #8a8378;
            --gradient-1: linear-gradient(135deg, #e8d8b0, #c4a875);
            --glow: 0 0 40px rgba(232, 216, 176, 0.18);
        }

        * { margin: 0; padding: 0; box-sizing: border-box; }

        body {
            font-family: 'Inter', sans-serif;
            background: var(--bg-dark);
            color: var(--text-primary);
            min-height: 100vh;
            overflow-x: hidden;
        }

        body::before {
            content: '';
            position: fixed;
            top: 0; left: 0; right: 0; bottom: 0;
            background:
                radial-gradient(circle at 20% 50%, rgba(232, 216, 176,0.1) 0%, transparent 50%),
                radial-gradient(circle at 80% 20%, rgba(196, 168, 117,0.08) 0%, transparent 50%),
                radial-gradient(circle at 40% 80%, rgba(232, 216, 176,0.05) 0%, transparent 50%);
            z-index: -1;
        }

        .navbar {
            background: rgba(10, 9, 8, 0.8) !important;
            backdrop-filter: blur(20px);
            border-bottom: 1px solid rgba(232, 216, 176,0.2);
            padding: 1rem 0;
        }

        .navbar-brand {
            font-weight: 800;
            font-size: 1.5rem;
            background: var(--gradient-1);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
        }

        .nav-link {
            color: var(--text-secondary) !important;
            font-weight: 500;
            transition: all 0.3s;
            position: relative;
        }

        .nav-link:hover, .nav-link.active {
            color: var(--primary-light) !important;
        }

        .nav-link::after {
            content: '';
            position: absolute;
            bottom: -2px;
            left: 50%;
            width: 0;
            height: 2px;
            background: var(--gradient-1);
            transition: all 0.3s;
            transform: translateX(-50%);
        }

        .nav-link:hover::after { width: 80%; }

        .lang-switch {
            display: flex;
            gap: 0.25rem;
            background: var(--bg-card);
            border-radius: 8px;
            padding: 2px;
        }

        .lang-btn {
            padding: 4px 12px;
            border: none;
            background: transparent;
            color: var(--text-secondary);
            border-radius: 6px;
            font-size: 0.8rem;
            font-weight: 600;
            cursor: pointer;
            transition: all 0.3s;
            text-decoration: none;
        }

        .lang-btn.active, .lang-btn:hover {
            background: var(--primary);
            color: white;
        }

        .page-header {
            text-align: center;
            padding: 4rem 2rem 2rem;
        }

        .page-header h1 {
            font-size: 2.5rem;
            font-weight: 800;
            background: var(--gradient-1);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
            margin-bottom: 0.5rem;
        }

        /* Table */
        .history-container {
            max-width: 900px;
            margin: 0 auto;
            padding: 0 1rem;
        }

        .history-card {
            background: var(--bg-card);
            border: 1px solid rgba(232, 216, 176,0.15);
            border-radius: 20px;
            overflow: hidden;
        }

        .table-wrapper {
            overflow-x: auto;
        }

        .history-table {
            width: 100%;
            border-collapse: collapse;
        }

        .history-table thead th {
            padding: 1rem 1.25rem;
            font-size: 0.85rem;
            font-weight: 600;
            text-transform: uppercase;
            letter-spacing: 0.05em;
            color: var(--text-secondary);
            border-bottom: 1px solid rgba(232, 216, 176,0.15);
            text-align: left;
        }

        .history-table tbody tr {
            border-bottom: 1px solid rgba(232, 216, 176,0.08);
            transition: background 0.3s;
        }

        .history-table tbody tr:last-child { border-bottom: none; }

        .history-table tbody tr:hover {
            background: var(--bg-card-hover);
        }

        .history-table td {
            padding: 1rem 1.25rem;
            font-size: 0.95rem;
        }

        .file-cell {
            display: flex;
            align-items: center;
            gap: 0.75rem;
        }

        .file-cell .fc-icon {
            width: 36px;
            height: 36px;
            border-radius: 10px;
            background: var(--gradient-1);
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 1rem;
            flex-shrink: 0;
        }

        .model-badge {
            display: inline-block;
            padding: 0.2rem 0.7rem;
            border-radius: 6px;
            font-size: 0.8rem;
            font-weight: 600;
            background: rgba(232, 216, 176,0.15);
            color: var(--primary-light);
        }

        .status-badge {
            display: inline-flex;
            align-items: center;
            gap: 0.35rem;
            padding: 0.25rem 0.75rem;
            border-radius: 20px;
            font-size: 0.8rem;
            font-weight: 600;
        }

        .status-badge .dot {
            width: 7px;
            height: 7px;
            border-radius: 50%;
        }

        .status-badge.completed { background: rgba(16,185,129,0.15); color: #34d399; }
        .status-badge.completed .dot { background: #34d399; }

        .status-badge.processing { background: rgba(232, 216, 176,0.15); color: var(--primary-light); }
        .status-badge.processing .dot { background: var(--primary-light); animation: pulse 1.5s ease-in-out infinite; }

        .status-badge.pending { background: rgba(234,179,8,0.15); color: #facc15; }
        .status-badge.pending .dot { background: #facc15; }

        .status-badge.failed { background: rgba(220,90,80,0.15); color: #f87171; }
        .status-badge.failed .dot { background: #f87171; }

        @keyframes pulse {
            0%, 100% { opacity: 1; }
            50% { opacity: 0.3; }
        }

        .btn-view {
            display: inline-flex;
            align-items: center;
            gap: 0.4rem;
            padding: 0.4rem 1rem;
            border: 1px solid rgba(232, 216, 176,0.3);
            border-radius: 8px;
            background: transparent;
            color: var(--primary-light);
            font-size: 0.85rem;
            font-weight: 600;
            text-decoration: none;
            transition: all 0.3s;
        }

        .btn-view:hover {
            background: var(--primary);
            color: white;
            border-color: var(--primary);
        }

        /* Empty state */
        .empty-state {
            text-align: center;
            padding: 4rem 2rem;
        }

        .empty-state .empty-icon {
            width: 80px;
            height: 80px;
            border-radius: 50%;
            background: rgba(232, 216, 176,0.1);
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 2rem;
            margin: 0 auto 1.5rem;
            color: var(--text-secondary);
        }

        .empty-state p {
            color: var(--text-secondary);
            font-size: 1.1rem;
            margin-bottom: 1.5rem;
        }

        .btn-upload-link {
            display: inline-flex;
            align-items: center;
            gap: 0.5rem;
            padding: 0.75rem 1.75rem;
            background: var(--gradient-1);
            border: none;
            border-radius: 12px;
            color: white;
            font-weight: 600;
            text-decoration: none;
            transition: all 0.3s;
            box-shadow: var(--glow);
        }

        .btn-upload-link:hover {
            transform: translateY(-2px);
            color: white;
        }

        .footer {
            text-align: center;
            padding: 2rem;
            color: var(--text-secondary);
            font-size: 0.85rem;
            border-top: 1px solid rgba(232, 216, 176,0.1);
            margin-top: 4rem;
        }

        @media (max-width: 576px) {
            .page-header h1 { font-size: 1.8rem; }
        }
    </style>
</head>
<body>

<!-- Navbar -->
<nav class="navbar navbar-expand-lg sticky-top">
    <div class="container">
        <a class="navbar-brand" href="/">
            <i class="bi bi-soundwave"></i> AI StemSep
        </a>
        <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav">
            <span class="navbar-toggler-icon"></span>
        </button>
        <div class="collapse navbar-collapse" id="navbarNav">
            <ul class="navbar-nav me-auto">
                <li class="nav-item"><a class="nav-link" href="/"><spring:message code="nav.home"/></a></li>
                <li class="nav-item"><a class="nav-link" href="/upload"><spring:message code="nav.upload"/></a></li>
                <li class="nav-item"><a class="nav-link active" href="/history"><spring:message code="nav.history"/></a></li>
            </ul>
            <div class="lang-switch">
                <a href="?lang=tr" class="lang-btn active">TR</a>
                <a href="?lang=en" class="lang-btn">EN</a>
            </div>
        </div>
    </div>
</nav>

<!-- Header -->
<section class="page-header">
    <h1><spring:message code="history.title"/></h1>
</section>

<!-- Content -->
<div class="history-container">
    <c:choose>
        <c:when test="${empty jobs}">
            <div class="history-card">
                <div class="empty-state">
                    <div class="empty-icon"><i class="bi bi-clock-history"></i></div>
                    <p><spring:message code="history.empty"/></p>
                    <a href="/upload" class="btn-upload-link">
                        <i class="bi bi-upload"></i> <spring:message code="nav.upload"/>
                    </a>
                </div>
            </div>
        </c:when>
        <c:otherwise>
            <div class="history-card">
                <div class="table-wrapper">
                    <table class="history-table">
                        <thead>
                            <tr>
                                <th><spring:message code="history.table.filename"/></th>
                                <th><spring:message code="history.table.model"/></th>
                                <th><spring:message code="history.table.status"/></th>
                                <th><spring:message code="history.table.date"/></th>
                                <th><spring:message code="history.table.actions"/></th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="job" items="${jobs}">
                                <tr>
                                    <td>
                                        <div class="file-cell">
                                            <div class="fc-icon"><i class="bi bi-music-note-beamed"></i></div>
                                            <span>${job.originalFilename}</span>
                                        </div>
                                    </td>
                                    <td><span class="model-badge">${job.modelUsed}</span></td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${job.status == 'COMPLETED'}">
                                                <span class="status-badge completed"><span class="dot"></span> <spring:message code="status.COMPLETED"/></span>
                                            </c:when>
                                            <c:when test="${job.status == 'PROCESSING'}">
                                                <span class="status-badge processing"><span class="dot"></span> <spring:message code="status.PROCESSING"/></span>
                                            </c:when>
                                            <c:when test="${job.status == 'PENDING'}">
                                                <span class="status-badge pending"><span class="dot"></span> <spring:message code="status.PENDING"/></span>
                                            </c:when>
                                            <c:when test="${job.status == 'FAILED'}">
                                                <span class="status-badge failed"><span class="dot"></span> <spring:message code="status.FAILED"/></span>
                                            </c:when>
                                        </c:choose>
                                    </td>
                                    <td>${fn:substring(job.createdAt.toString(), 0, 16)}</td>
                                    <td>
                                        <a href="/job/${job.id}" class="btn-view">
                                            <i class="bi bi-eye"></i> <spring:message code="history.view"/>
                                        </a>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </div>
        </c:otherwise>
    </c:choose>
</div>

<!-- Footer -->
<footer class="footer">
    <spring:message code="footer.text"/>
</footer>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
