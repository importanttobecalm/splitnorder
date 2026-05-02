<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="${pageContext.response.locale}">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><spring:message code="result.title"/> - <spring:message code="app.title"/></title>
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

        .page-header p {
            color: var(--text-secondary);
            font-size: 1.1rem;
        }

        /* Result card */
        .result-container {
            max-width: 800px;
            margin: 0 auto;
            padding: 0 1rem;
        }

        /* Download all */
        .download-all {
            display: flex;
            justify-content: center;
            margin-bottom: 2rem;
        }

        .btn-download-all {
            display: inline-flex;
            align-items: center;
            gap: 0.5rem;
            padding: 0.85rem 2rem;
            background: var(--gradient-1);
            border: none;
            border-radius: 12px;
            color: white;
            font-size: 1rem;
            font-weight: 600;
            cursor: pointer;
            transition: all 0.3s;
            text-decoration: none;
            box-shadow: var(--glow);
        }

        .btn-download-all:hover {
            transform: translateY(-2px);
            box-shadow: 0 0 60px rgba(232, 216, 176,0.5);
            color: white;
        }

        /* Stem cards */
        .stem-card {
            background: var(--bg-card);
            border: 1px solid rgba(232, 216, 176,0.15);
            border-radius: 16px;
            padding: 1.5rem;
            margin-bottom: 1rem;
            display: flex;
            align-items: center;
            gap: 1.25rem;
            transition: all 0.3s;
        }

        .stem-card:hover {
            border-color: rgba(232, 216, 176,0.4);
            background: var(--bg-card-hover);
        }

        .stem-icon {
            width: 56px;
            height: 56px;
            border-radius: 14px;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 1.5rem;
            flex-shrink: 0;
            background: linear-gradient(135deg, rgba(232,216,176,0.12), rgba(196,168,117,0.08));
            border: 1px solid rgba(232,216,176,0.18);
            color: var(--primary-light);
        }

        /* Aynı luxe paletinde her stem için hafif tonlama farkı (overuse edilmiş gradient'lere son) */
        .stem-icon.vocals { color: #f5e9c7; }
        .stem-icon.drums  { color: #e8d8b0; }
        .stem-icon.bass   { color: #d4c19a; }
        .stem-icon.other  { color: #c4b289; }

        .stem-info { flex: 1; min-width: 0; }
        .stem-name {
            font-weight: 700;
            font-size: 1.15rem;
            letter-spacing: 0.04em;
            text-transform: uppercase;
            color: var(--primary-light);
            margin-bottom: 0.5rem;
            line-height: 1.2;
        }

        /* Audio player */
        .stem-player {
            width: 100%;
            height: 36px;
            border-radius: 8px;
            outline: none;
        }

        .stem-player::-webkit-media-controls-panel {
            background: var(--bg-card-hover);
        }

        .btn-stem-download {
            display: flex;
            align-items: center;
            justify-content: center;
            width: 44px;
            height: 44px;
            border-radius: 12px;
            border: 1px solid rgba(232, 216, 176,0.3);
            background: transparent;
            color: var(--primary-light);
            font-size: 1.2rem;
            cursor: pointer;
            transition: all 0.3s;
            text-decoration: none;
            flex-shrink: 0;
        }

        .btn-stem-download:hover {
            background: var(--primary);
            color: white;
            border-color: var(--primary);
        }

        /* Error state */
        .error-card {
            background: var(--bg-card);
            border: 1px solid rgba(239,68,68,0.3);
            border-radius: 20px;
            padding: 3rem;
            text-align: center;
            max-width: 500px;
            margin: 0 auto;
        }

        .error-card .error-icon {
            width: 80px;
            height: 80px;
            border-radius: 50%;
            background: rgba(220,90,80,0.15);
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 2.5rem;
            color: #f87171;
            margin: 0 auto 1.5rem;
        }

        .error-card h2 {
            color: #f87171;
            margin-bottom: 0.5rem;
        }

        .error-card p {
            color: var(--text-secondary);
            margin-bottom: 1.5rem;
        }

        .btn-retry {
            display: inline-flex;
            align-items: center;
            gap: 0.5rem;
            padding: 0.75rem 1.75rem;
            background: transparent;
            border: 2px solid var(--primary);
            border-radius: 12px;
            color: var(--primary-light);
            font-weight: 600;
            text-decoration: none;
            transition: all 0.3s;
        }

        .btn-retry:hover {
            background: var(--primary);
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
            .stem-card { flex-wrap: wrap; }
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
                <li class="nav-item"><a class="nav-link" href="/history"><spring:message code="nav.history"/></a></li>
            </ul>
            <div class="lang-switch">
                <a href="?lang=tr" class="lang-btn active">TR</a>
                <a href="?lang=en" class="lang-btn">EN</a>
            </div>
        </div>
    </div>
</nav>

<c:choose>
    <c:when test="${job.status == 'FAILED'}">
        <!-- Error State -->
        <section class="page-header">
            <h1><spring:message code="result.failed"/></h1>
        </section>
        <div class="container">
            <div class="error-card">
                <div class="error-icon"><i class="bi bi-x-lg"></i></div>
                <h2><spring:message code="result.error"/></h2>
                <p>${error}</p>
                <a href="/upload" class="btn-retry">
                    <i class="bi bi-arrow-repeat"></i> <spring:message code="home.hero.button"/>
                </a>
            </div>
        </div>
    </c:when>
    <c:otherwise>
        <!-- Success State -->
        <section class="page-header">
            <h1><spring:message code="result.title"/></h1>
            <p><spring:message code="result.subtitle"/> - ${job.originalFilename}</p>
        </section>

        <div class="result-container">
            <!-- Download All -->
            <div class="download-all">
                <a href="/job/${job.id}/download-all" class="btn-download-all">
                    <i class="bi bi-file-earmark-zip"></i> <spring:message code="result.download.all"/>
                </a>
            </div>

            <!-- Vocals -->
            <div class="stem-card">
                <div class="stem-icon vocals"><i class="bi bi-mic"></i></div>
                <div class="stem-info">
                    <div class="stem-name"><spring:message code="result.stem.vocals"/></div>
                    <audio class="stem-player" controls preload="none">
                        <source src="${stemUrls.vocals}" type="audio/wav">
                    </audio>
                </div>
                <a href="${stemUrls.vocals}" class="btn-stem-download" title="<spring:message code='result.download'/>">
                    <i class="bi bi-download"></i>
                </a>
            </div>

            <!-- Drums -->
            <div class="stem-card">
                <div class="stem-icon drums"><i class="bi bi-disc"></i></div>
                <div class="stem-info">
                    <div class="stem-name"><spring:message code="result.stem.drums"/></div>
                    <audio class="stem-player" controls preload="none">
                        <source src="${stemUrls.drums}" type="audio/wav">
                    </audio>
                </div>
                <a href="${stemUrls.drums}" class="btn-stem-download" title="<spring:message code='result.download'/>">
                    <i class="bi bi-download"></i>
                </a>
            </div>

            <!-- Bass -->
            <div class="stem-card">
                <div class="stem-icon bass"><i class="bi bi-badge-8k"></i></div>
                <div class="stem-info">
                    <div class="stem-name"><spring:message code="result.stem.bass"/></div>
                    <audio class="stem-player" controls preload="none">
                        <source src="${stemUrls.bass}" type="audio/wav">
                    </audio>
                </div>
                <a href="${stemUrls.bass}" class="btn-stem-download" title="<spring:message code='result.download'/>">
                    <i class="bi bi-download"></i>
                </a>
            </div>

            <!-- Other -->
            <div class="stem-card">
                <div class="stem-icon other"><i class="bi bi-music-note-list"></i></div>
                <div class="stem-info">
                    <div class="stem-name"><spring:message code="result.stem.other"/></div>
                    <audio class="stem-player" controls preload="none">
                        <source src="${stemUrls.other}" type="audio/wav">
                    </audio>
                </div>
                <a href="${stemUrls.other}" class="btn-stem-download" title="<spring:message code='result.download'/>">
                    <i class="bi bi-download"></i>
                </a>
            </div>
        </div>
    </c:otherwise>
</c:choose>

<!-- Footer -->
<footer class="footer">
    <spring:message code="footer.text"/>
</footer>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
