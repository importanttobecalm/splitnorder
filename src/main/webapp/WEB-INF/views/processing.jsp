<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="${pageContext.response.locale}">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><spring:message code="processing.title"/> - <spring:message code="app.title"/></title>
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
        }

        .nav-link:hover { color: var(--primary-light) !important; }

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

        /* Processing content */
        .processing-wrapper {
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            min-height: calc(100vh - 200px);
            padding: 2rem;
            text-align: center;
        }

        .processing-card {
            background: var(--bg-card);
            border: 1px solid rgba(232, 216, 176,0.15);
            border-radius: 24px;
            padding: 3rem;
            max-width: 500px;
            width: 100%;
        }

        /* Spinner */
        .spinner-ring {
            width: 100px;
            height: 100px;
            margin: 0 auto 2rem;
            position: relative;
        }

        .spinner-ring::before,
        .spinner-ring::after {
            content: '';
            position: absolute;
            border-radius: 50%;
        }

        .spinner-ring::before {
            top: 0; left: 0; right: 0; bottom: 0;
            border: 4px solid rgba(232, 216, 176,0.15);
        }

        .spinner-ring::after {
            top: 0; left: 0; right: 0; bottom: 0;
            border: 4px solid transparent;
            border-top-color: var(--primary);
            border-right-color: var(--accent);
            animation: spin 1.2s linear infinite;
        }

        @keyframes spin {
            to { transform: rotate(360deg); }
        }

        .spinner-icon {
            position: absolute;
            top: 50%;
            left: 50%;
            transform: translate(-50%, -50%);
            font-size: 2rem;
            background: var(--gradient-1);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
        }

        .processing-card h2 {
            font-size: 1.8rem;
            font-weight: 700;
            margin-bottom: 0.5rem;
        }

        .processing-card .subtitle {
            color: var(--text-secondary);
            margin-bottom: 2rem;
        }

        /* Status info */
        .status-info {
            background: rgba(232, 216, 176,0.08);
            border-radius: 12px;
            padding: 1.25rem;
            margin-bottom: 1.5rem;
        }

        .status-row {
            display: flex;
            justify-content: space-between;
            padding: 0.4rem 0;
        }

        .status-row .label {
            color: var(--text-secondary);
            font-size: 0.9rem;
        }

        .status-row .value {
            font-weight: 600;
            font-size: 0.9rem;
        }

        .status-badge {
            display: inline-flex;
            align-items: center;
            gap: 0.4rem;
            padding: 0.2rem 0.75rem;
            border-radius: 20px;
            font-size: 0.8rem;
            font-weight: 600;
        }

        .status-badge.pending {
            background: rgba(234,179,8,0.15);
            color: #facc15;
        }

        .status-badge.processing {
            background: rgba(232, 216, 176,0.15);
            color: var(--primary-light);
        }

        .status-badge .dot {
            width: 8px;
            height: 8px;
            border-radius: 50%;
            animation: pulse 1.5s ease-in-out infinite;
        }

        .status-badge.pending .dot { background: #facc15; }
        .status-badge.processing .dot { background: var(--primary-light); }

        @keyframes pulse {
            0%, 100% { opacity: 1; }
            50% { opacity: 0.3; }
        }

        .wait-text {
            color: var(--text-secondary);
            font-size: 0.9rem;
        }

        /* Waveform */
        .mini-wave {
            display: flex;
            justify-content: center;
            align-items: flex-end;
            gap: 3px;
            height: 30px;
            margin: 1.5rem auto 0;
        }

        .mini-wave .bar {
            width: 3px;
            background: var(--gradient-1);
            border-radius: 3px;
            animation: wave 1.2s ease-in-out infinite;
        }

        @keyframes wave {
            0%, 100% { height: 6px; }
            50% { height: 26px; }
        }

        .footer {
            text-align: center;
            padding: 2rem;
            color: var(--text-secondary);
            font-size: 0.85rem;
            border-top: 1px solid rgba(232, 216, 176,0.1);
            margin-top: 4rem;
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
                <li class="nav-item"><a class="nav-link" href="/history"><spring:message code="nav.history"/></a></li>
            </ul>
            <div class="lang-switch">
                <a href="?lang=tr" class="lang-btn active">TR</a>
                <a href="?lang=en" class="lang-btn">EN</a>
            </div>
        </div>
    </div>
</nav>

<!-- Processing -->
<div class="processing-wrapper">
    <div class="processing-card">
        <div class="spinner-ring">
            <i class="bi bi-music-note-beamed spinner-icon"></i>
        </div>

        <h2><spring:message code="processing.title"/></h2>
        <p class="subtitle"><spring:message code="processing.subtitle"/></p>

        <div class="status-info">
            <div class="status-row">
                <span class="label"><i class="bi bi-file-earmark-music"></i> ${job.originalFilename}</span>
                <span class="value">${job.modelUsed}</span>
            </div>
            <div class="status-row">
                <span class="label">Status</span>
                <span id="statusBadge" class="status-badge processing">
                    <span class="dot"></span>
                    <span id="statusText"><spring:message code="processing.status.processing"/></span>
                </span>
            </div>
        </div>

        <p class="wait-text"><spring:message code="processing.wait"/></p>

        <div class="mini-wave">
            <c:forEach begin="1" end="15" var="i">
                <div class="bar" style="animation-delay: ${i * 0.08}s;"></div>
            </c:forEach>
        </div>
    </div>
</div>

<!-- Footer -->
<footer class="footer">
    <spring:message code="footer.text"/>
</footer>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
<script>
    const jobId = ${job.id};

    function pollStatus() {
        fetch('/job/' + jobId + '/status')
            .then(r => r.json())
            .then(data => {
                if (data.status === 'COMPLETED' || data.status === 'FAILED') {
                    window.location.href = '/job/' + jobId;
                }
            })
            .catch(() => {});
    }

    setInterval(pollStatus, 3000);
</script>
</body>
</html>
