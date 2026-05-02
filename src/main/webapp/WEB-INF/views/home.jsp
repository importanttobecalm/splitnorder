<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="${pageContext.response.locale}">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><spring:message code="app.title"/></title>
    <meta name="description" content="<spring:message code='app.description'/>">
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
            --gradient-2: linear-gradient(135deg, #14120f, #1c1916);
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

        /* Animated background */
        body::before {
            content: '';
            position: fixed;
            top: 0; left: 0; right: 0; bottom: 0;
            background:
                radial-gradient(circle at 20% 50%, rgba(232, 216, 176,0.1) 0%, transparent 50%),
                radial-gradient(circle at 80% 20%, rgba(196, 168, 117,0.08) 0%, transparent 50%),
                radial-gradient(circle at 40% 80%, rgba(232, 216, 176,0.05) 0%, transparent 50%);
            z-index: -1;
            animation: bgPulse 8s ease-in-out infinite alternate;
        }

        @keyframes bgPulse {
            0% { opacity: 0.7; }
            100% { opacity: 1; }
        }

        /* Navbar */
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

        /* Hero Section */
        .hero {
            text-align: center;
            padding: 6rem 2rem 4rem;
            position: relative;
        }

        .hero h1 {
            font-size: 3.5rem;
            font-weight: 800;
            line-height: 1.1;
            margin-bottom: 1.5rem;
            background: var(--gradient-1);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
            animation: fadeInUp 0.8s ease-out;
        }

        .hero p {
            font-size: 1.25rem;
            color: var(--text-secondary);
            max-width: 600px;
            margin: 0 auto 2.5rem;
            animation: fadeInUp 0.8s ease-out 0.2s both;
        }

        .btn-glow {
            display: inline-flex;
            align-items: center;
            gap: 0.5rem;
            padding: 1rem 2.5rem;
            background: var(--gradient-1);
            border: none;
            border-radius: 12px;
            color: white;
            font-size: 1.1rem;
            font-weight: 600;
            cursor: pointer;
            transition: all 0.3s;
            text-decoration: none;
            box-shadow: var(--glow);
            animation: fadeInUp 0.8s ease-out 0.4s both;
        }

        .btn-glow:hover {
            transform: translateY(-2px);
            box-shadow: 0 0 60px rgba(232, 216, 176,0.5);
            color: white;
        }

        /* Waveform animation */
        .waveform {
            display: flex;
            justify-content: center;
            align-items: flex-end;
            gap: 4px;
            height: 60px;
            margin: 3rem auto;
            animation: fadeInUp 0.8s ease-out 0.6s both;
        }

        .waveform .bar {
            width: 4px;
            background: var(--gradient-1);
            border-radius: 4px;
            animation: wave 1.2s ease-in-out infinite;
        }

        @keyframes wave {
            0%, 100% { height: 10px; }
            50% { height: 50px; }
        }

        /* Features */
        .features {
            padding: 4rem 0;
        }

        .feature-card {
            background: var(--bg-card);
            border: 1px solid rgba(232, 216, 176,0.15);
            border-radius: 16px;
            padding: 2rem;
            text-align: center;
            transition: all 0.4s;
            height: 100%;
        }

        .feature-card:hover {
            background: var(--bg-card-hover);
            border-color: rgba(232, 216, 176,0.4);
            transform: translateY(-8px);
            box-shadow: var(--glow);
        }

        .feature-icon {
            width: 64px;
            height: 64px;
            border-radius: 16px;
            background: var(--gradient-1);
            display: flex;
            align-items: center;
            justify-content: center;
            margin: 0 auto 1.5rem;
            font-size: 1.8rem;
        }

        .feature-card h3 {
            font-size: 1.2rem;
            font-weight: 700;
            margin-bottom: 0.75rem;
        }

        .feature-card p {
            color: var(--text-secondary);
            font-size: 0.95rem;
        }

        /* How it works */
        .howto {
            padding: 4rem 0;
        }

        .howto h2 {
            text-align: center;
            font-size: 2rem;
            font-weight: 700;
            margin-bottom: 3rem;
        }

        .step {
            display: flex;
            align-items: center;
            gap: 1.5rem;
            padding: 1.5rem;
            background: var(--bg-card);
            border-radius: 12px;
            margin-bottom: 1rem;
            border: 1px solid rgba(232, 216, 176,0.1);
            transition: all 0.3s;
        }

        .step:hover {
            border-color: rgba(232, 216, 176,0.3);
            background: var(--bg-card-hover);
        }

        .step-number {
            width: 48px;
            height: 48px;
            border-radius: 50%;
            background: var(--gradient-1);
            display: flex;
            align-items: center;
            justify-content: center;
            font-weight: 800;
            font-size: 1.2rem;
            flex-shrink: 0;
        }

        /* Footer */
        .footer {
            text-align: center;
            padding: 2rem;
            color: var(--text-secondary);
            font-size: 0.85rem;
            border-top: 1px solid rgba(232, 216, 176,0.1);
            margin-top: 4rem;
        }

        @keyframes fadeInUp {
            from { opacity: 0; transform: translateY(30px); }
            to { opacity: 1; transform: translateY(0); }
        }

        @media (max-width: 768px) {
            .hero h1 { font-size: 2.2rem; }
            .hero p { font-size: 1rem; }
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
                <li class="nav-item">
                    <a class="nav-link active" href="/"><spring:message code="nav.home"/></a>
                </li>
                <li class="nav-item">
                    <a class="nav-link" href="/upload"><spring:message code="nav.upload"/></a>
                </li>
                <li class="nav-item">
                    <a class="nav-link" href="/history"><spring:message code="nav.history"/></a>
                </li>
            </ul>
            <div class="lang-switch">
                <a href="?lang=tr" class="lang-btn active">TR</a>
                <a href="?lang=en" class="lang-btn">EN</a>
            </div>
        </div>
    </div>
</nav>

<!-- Hero Section -->
<section class="hero">
    <h1><spring:message code="home.hero.title"/></h1>
    <p><spring:message code="home.hero.subtitle"/></p>
    <a href="/upload" class="btn-glow">
        <i class="bi bi-upload"></i> <spring:message code="home.hero.button"/>
    </a>

    <div class="waveform">
        <c:forEach begin="1" end="30" var="i">
            <div class="bar" style="animation-delay: ${i * 0.05}s; height: ${10 + (i % 7) * 8}px;"></div>
        </c:forEach>
    </div>
</section>

<!-- Features -->
<section class="features">
    <div class="container">
        <div class="row g-4">
            <div class="col-md-4">
                <div class="feature-card">
                    <div class="feature-icon"><i class="bi bi-music-note-list"></i></div>
                    <h3><spring:message code="home.feature1.title"/></h3>
                    <p><spring:message code="home.feature1.desc"/></p>
                </div>
            </div>
            <div class="col-md-4">
                <div class="feature-card">
                    <div class="feature-icon"><i class="bi bi-cpu"></i></div>
                    <h3><spring:message code="home.feature2.title"/></h3>
                    <p><spring:message code="home.feature2.desc"/></p>
                </div>
            </div>
            <div class="col-md-4">
                <div class="feature-card">
                    <div class="feature-icon"><i class="bi bi-lightning-charge"></i></div>
                    <h3><spring:message code="home.feature3.title"/></h3>
                    <p><spring:message code="home.feature3.desc"/></p>
                </div>
            </div>
        </div>
    </div>
</section>

<!-- How it works -->
<section class="howto">
    <div class="container">
        <h2><spring:message code="home.howto.title"/></h2>
        <div class="row justify-content-center">
            <div class="col-lg-8">
                <div class="step">
                    <div class="step-number">1</div>
                    <span><spring:message code="home.howto.step1"/></span>
                </div>
                <div class="step">
                    <div class="step-number">2</div>
                    <span><spring:message code="home.howto.step2"/></span>
                </div>
                <div class="step">
                    <div class="step-number">3</div>
                    <span><spring:message code="home.howto.step3"/></span>
                </div>
            </div>
        </div>
    </div>
</section>

<!-- Footer -->
<footer class="footer">
    <spring:message code="footer.text"/>
</footer>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
