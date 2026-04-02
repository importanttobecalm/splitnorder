<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="${pageContext.response.locale}">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><spring:message code="upload.title"/> - <spring:message code="app.title"/></title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800&display=swap" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.1/font/bootstrap-icons.css" rel="stylesheet">
    <style>
        :root {
            --primary: #7c3aed;
            --primary-light: #a78bfa;
            --primary-dark: #5b21b6;
            --accent: #06b6d4;
            --bg-dark: #0f0f23;
            --bg-card: #1a1a2e;
            --bg-card-hover: #25254a;
            --text-primary: #f1f5f9;
            --text-secondary: #94a3b8;
            --gradient-1: linear-gradient(135deg, #7c3aed, #06b6d4);
            --gradient-2: linear-gradient(135deg, #1a1a2e, #2d1b69);
            --glow: 0 0 40px rgba(124, 58, 237, 0.3);
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
                radial-gradient(circle at 20% 50%, rgba(124,58,237,0.1) 0%, transparent 50%),
                radial-gradient(circle at 80% 20%, rgba(6,182,212,0.08) 0%, transparent 50%),
                radial-gradient(circle at 40% 80%, rgba(124,58,237,0.05) 0%, transparent 50%);
            z-index: -1;
        }

        .navbar {
            background: rgba(15, 15, 35, 0.8) !important;
            backdrop-filter: blur(20px);
            border-bottom: 1px solid rgba(124,58,237,0.2);
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

        .page-header p {
            color: var(--text-secondary);
            font-size: 1.1rem;
        }

        /* Upload area */
        .upload-card {
            background: var(--bg-card);
            border: 1px solid rgba(124,58,237,0.15);
            border-radius: 20px;
            padding: 2.5rem;
            max-width: 640px;
            margin: 0 auto;
        }

        .dropzone {
            border: 2px dashed rgba(124,58,237,0.4);
            border-radius: 16px;
            padding: 3rem 2rem;
            text-align: center;
            cursor: pointer;
            transition: all 0.3s;
            background: rgba(124,58,237,0.05);
        }

        .dropzone:hover, .dropzone.drag-over {
            border-color: var(--primary);
            background: rgba(124,58,237,0.12);
            box-shadow: var(--glow);
        }

        .dropzone .icon {
            font-size: 3rem;
            background: var(--gradient-1);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
            margin-bottom: 1rem;
        }

        .dropzone p {
            color: var(--text-secondary);
            margin-bottom: 0.5rem;
        }

        .dropzone .formats {
            font-size: 0.85rem;
            color: var(--text-secondary);
            opacity: 0.7;
        }

        .file-info {
            display: none;
            align-items: center;
            gap: 1rem;
            padding: 1rem;
            background: rgba(124,58,237,0.1);
            border-radius: 12px;
            margin-top: 1rem;
        }

        .file-info.show { display: flex; }

        .file-info .fi-icon {
            width: 48px;
            height: 48px;
            border-radius: 12px;
            background: var(--gradient-1);
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 1.4rem;
            flex-shrink: 0;
        }

        .file-info .fi-details { flex: 1; }
        .file-info .fi-name { font-weight: 600; font-size: 0.95rem; }
        .file-info .fi-size { font-size: 0.8rem; color: var(--text-secondary); }

        .file-info .fi-remove {
            background: none;
            border: none;
            color: var(--text-secondary);
            font-size: 1.2rem;
            cursor: pointer;
            padding: 0.5rem;
            transition: color 0.3s;
        }

        .file-info .fi-remove:hover { color: #ef4444; }

        /* Model select */
        .model-section {
            margin-top: 2rem;
        }

        .model-section label {
            display: block;
            font-weight: 600;
            margin-bottom: 0.75rem;
        }

        .model-options {
            display: flex;
            gap: 1rem;
        }

        .model-option {
            flex: 1;
            padding: 1.25rem;
            border: 2px solid rgba(124,58,237,0.2);
            border-radius: 12px;
            cursor: pointer;
            transition: all 0.3s;
            text-align: center;
            background: transparent;
        }

        .model-option:hover {
            border-color: rgba(124,58,237,0.5);
            background: rgba(124,58,237,0.05);
        }

        .model-option.selected {
            border-color: var(--primary);
            background: rgba(124,58,237,0.15);
            box-shadow: 0 0 20px rgba(124,58,237,0.2);
        }

        .model-option input { display: none; }

        .model-option .mo-name {
            font-weight: 700;
            font-size: 1rem;
            margin-bottom: 0.25rem;
        }

        .model-option .mo-desc {
            font-size: 0.8rem;
            color: var(--text-secondary);
        }

        /* Submit */
        .btn-submit {
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 0.5rem;
            width: 100%;
            padding: 1rem;
            margin-top: 2rem;
            background: var(--gradient-1);
            border: none;
            border-radius: 12px;
            color: white;
            font-size: 1.1rem;
            font-weight: 600;
            cursor: pointer;
            transition: all 0.3s;
            box-shadow: var(--glow);
        }

        .btn-submit:hover {
            transform: translateY(-2px);
            box-shadow: 0 0 60px rgba(124,58,237,0.5);
        }

        .btn-submit:disabled {
            opacity: 0.5;
            cursor: not-allowed;
            transform: none;
            box-shadow: none;
        }

        /* Alert */
        .alert-error {
            background: rgba(239,68,68,0.15);
            border: 1px solid rgba(239,68,68,0.3);
            color: #fca5a5;
            border-radius: 12px;
            padding: 1rem 1.25rem;
            margin-bottom: 1.5rem;
            display: flex;
            align-items: center;
            gap: 0.75rem;
        }

        .footer {
            text-align: center;
            padding: 2rem;
            color: var(--text-secondary);
            font-size: 0.85rem;
            border-top: 1px solid rgba(124,58,237,0.1);
            margin-top: 4rem;
        }

        @media (max-width: 576px) {
            .model-options { flex-direction: column; }
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
                <li class="nav-item"><a class="nav-link active" href="/upload"><spring:message code="nav.upload"/></a></li>
                <li class="nav-item"><a class="nav-link" href="/history"><spring:message code="nav.history"/></a></li>
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
    <h1><spring:message code="upload.title"/></h1>
    <p><spring:message code="upload.subtitle"/></p>
</section>

<!-- Upload Form -->
<div class="container">
    <div class="upload-card">

        <c:if test="${not empty error}">
            <div class="alert-error">
                <i class="bi bi-exclamation-circle"></i>
                <span><spring:message code="${error}"/></span>
            </div>
        </c:if>

        <form id="uploadForm" action="/upload" method="post" enctype="multipart/form-data">

            <!-- Dropzone -->
            <div class="dropzone" id="dropzone">
                <div class="icon"><i class="bi bi-cloud-arrow-up"></i></div>
                <p><spring:message code="upload.dropzone"/></p>
                <span class="formats"><spring:message code="upload.formats"/></span>
                <input type="file" id="fileInput" name="file" accept=".mp3,.wav,.flac" hidden>
            </div>

            <!-- File info -->
            <div class="file-info" id="fileInfo">
                <div class="fi-icon"><i class="bi bi-music-note-beamed"></i></div>
                <div class="fi-details">
                    <div class="fi-name" id="fileName"></div>
                    <div class="fi-size" id="fileSize"></div>
                </div>
                <button type="button" class="fi-remove" id="removeFile">
                    <i class="bi bi-x-lg"></i>
                </button>
            </div>

            <!-- Model Selection -->
            <div class="model-section">
                <label><spring:message code="upload.model.label"/></label>
                <div class="model-options">
                    <div class="model-option selected" data-model="mdx_extra">
                        <input type="radio" name="model" value="mdx_extra" checked>
                        <div class="mo-name">MDX-Net</div>
                        <div class="mo-desc"><spring:message code="upload.model.mdx"/></div>
                    </div>
                    <div class="model-option" data-model="htdemucs_ft">
                        <input type="radio" name="model" value="htdemucs_ft">
                        <div class="mo-name">HTDemucs_ft</div>
                        <div class="mo-desc"><spring:message code="upload.model.htdemucs"/></div>
                    </div>
                </div>
            </div>

            <!-- Submit -->
            <button type="submit" class="btn-submit" id="submitBtn" disabled>
                <i class="bi bi-cpu"></i> <spring:message code="upload.button"/>
            </button>
        </form>
    </div>
</div>

<!-- Footer -->
<footer class="footer">
    <spring:message code="footer.text"/>
</footer>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
<script>
    const dropzone = document.getElementById('dropzone');
    const fileInput = document.getElementById('fileInput');
    const fileInfo = document.getElementById('fileInfo');
    const fileName = document.getElementById('fileName');
    const fileSize = document.getElementById('fileSize');
    const removeFile = document.getElementById('removeFile');
    const submitBtn = document.getElementById('submitBtn');

    // Dropzone click
    dropzone.addEventListener('click', () => fileInput.click());

    // Drag & drop
    dropzone.addEventListener('dragover', e => {
        e.preventDefault();
        dropzone.classList.add('drag-over');
    });
    dropzone.addEventListener('dragleave', () => dropzone.classList.remove('drag-over'));
    dropzone.addEventListener('drop', e => {
        e.preventDefault();
        dropzone.classList.remove('drag-over');
        if (e.dataTransfer.files.length > 0) {
            fileInput.files = e.dataTransfer.files;
            showFileInfo(e.dataTransfer.files[0]);
        }
    });

    // File select
    fileInput.addEventListener('change', () => {
        if (fileInput.files.length > 0) {
            showFileInfo(fileInput.files[0]);
        }
    });

    function showFileInfo(file) {
        fileName.textContent = file.name;
        const sizeMB = (file.size / (1024 * 1024)).toFixed(2);
        fileSize.textContent = sizeMB + ' MB';
        fileInfo.classList.add('show');
        dropzone.style.display = 'none';
        submitBtn.disabled = false;
    }

    // Remove file
    removeFile.addEventListener('click', () => {
        fileInput.value = '';
        fileInfo.classList.remove('show');
        dropzone.style.display = 'block';
        submitBtn.disabled = true;
    });

    // Model select
    document.querySelectorAll('.model-option').forEach(opt => {
        opt.addEventListener('click', () => {
            document.querySelectorAll('.model-option').forEach(o => o.classList.remove('selected'));
            opt.classList.add('selected');
            opt.querySelector('input').checked = true;
        });
    });
</script>
</body>
</html>
