<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<fmt:setBundle basename="messages" />
<!DOCTYPE html>
<html lang="${pageContext.response.locale}">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title><fmt:message key="${param.titleKey != null ? param.titleKey : 'app.title'}" /></title>
<link rel="icon" type="image/jpeg" href="${pageContext.request.contextPath}/static/img/logo.jpg">
<script src="https://cdn.tailwindcss.com?plugins=forms,container-queries"></script>
<link href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@700&family=Inter:wght@400;500&family=JetBrains+Mono:wght@500&display=swap" rel="stylesheet">
<link href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:wght,FILL@100..700,0..1&display=swap" rel="stylesheet">
<script>
tailwind.config = {
  darkMode: "class",
  theme: {
    extend: {
      colors: {
        "tertiary": "#7f5300",
        "surface-container-highest": "#e1e2e9",
        "on-secondary-container": "#fffbff",
        "inverse-on-surface": "#eff0f8",
        "tertiary-fixed-dim": "#ffb953",
        "surface-bright": "#f8f9ff",
        "outline-variant": "#c1c7d3",
        "error": "#ba1a1a",
        "surface": "#f8f9ff",
        "inverse-primary": "#a4c9ff",
        "on-surface-variant": "#414751",
        "surface-container-low": "#f2f3fb",
        "on-background": "#191c21",
        "on-tertiary-container": "#fffbff",
        "on-primary-fixed-variant": "#004883",
        "secondary-fixed-dim": "#ffb4ac",
        "primary": "#005da7",
        "tertiary-container": "#a06900",
        "on-primary-container": "#fdfcff",
        "secondary-fixed": "#ffdad6",
        "surface-variant": "#e1e2e9",
        "on-surface": "#191c21",
        "primary-container": "#2976c7",
        "outline": "#717783",
        "primary-fixed": "#d4e3ff",
        "error-container": "#ffdad6",
        "on-error": "#ffffff",
        "secondary-container": "#db322f",
        "on-primary": "#ffffff",
        "on-tertiary-fixed": "#291800",
        "on-tertiary": "#ffffff",
        "surface-container-high": "#e6e8ef",
        "secondary": "#b7131a",
        "surface-container": "#ecedf5",
        "on-secondary-fixed": "#410002",
        "on-primary-fixed": "#001c39",
        "surface-container-lowest": "#ffffff",
        "on-secondary-fixed-variant": "#93000d",
        "background": "#f8f9ff",
        "primary-fixed-dim": "#a4c9ff",
        "on-tertiary-fixed-variant": "#633f00",
        "tertiary-fixed": "#ffddb4",
        "surface-tint": "#0060ac",
        "surface-dim": "#d8dae1",
        "on-error-container": "#93000a",
        "inverse-surface": "#2e3036",
        "on-secondary": "#ffffff"
      },
      borderRadius: { DEFAULT: "0.25rem", lg: "0.5rem", xl: "0.75rem", full: "9999px" },
      spacing: { margin_desktop: "48px", base: "8px", gutter: "24px", grid_dot: "16px", margin_mobile: "16px" },
      fontFamily: {
        "mono-numeric": ["JetBrains Mono"], "mono-label": ["JetBrains Mono"],
        "body-md": ["Inter"], "body-sm": ["Inter"], "body-lg": ["Inter"],
        "display-lg": ["Plus Jakarta Sans"], "headline-sm": ["Plus Jakarta Sans"], "headline-md": ["Plus Jakarta Sans"]
      },
      fontSize: {
        "mono-numeric": ["14px", { lineHeight: "1.0", fontWeight: "500" }],
        "mono-label":   ["12px", { lineHeight: "1.0", letterSpacing: "0.05em", fontWeight: "500" }],
        "body-md":      ["16px", { lineHeight: "1.5", fontWeight: "400" }],
        "body-sm":      ["14px", { lineHeight: "1.4", fontWeight: "500" }],
        "body-lg":      ["18px", { lineHeight: "1.6", fontWeight: "400" }],
        "display-lg":   ["48px", { lineHeight: "1.1", letterSpacing: "-0.02em", fontWeight: "700" }],
        "headline-sm":  ["24px", { lineHeight: "1.3", fontWeight: "700" }],
        "headline-md":  ["32px", { lineHeight: "1.2", letterSpacing: "-0.01em", fontWeight: "700" }]
      }
    }
  }
}
</script>
<style>
  .dotted-grid { background-image: radial-gradient(#c1c7d3 1px, transparent 1px); background-size: 16px 16px; }
  .soft-shadow { box-shadow: 0 4px 24px rgba(30, 50, 90, 0.06); }
  .waveform-card-red    { background: linear-gradient(135deg, rgba(219,50,47,0.1),  rgba(219,50,47,0.05)); }
  .waveform-card-orange { background: linear-gradient(135deg, rgba(255,185,83,0.1), rgba(255,185,83,0.05)); }
  .waveform-card-purple { background: linear-gradient(135deg, rgba(160,105,0,0.1),  rgba(160,105,0,0.05)); }
  .waveform-card-teal   { background: linear-gradient(135deg, rgba(0,93,167,0.1),   rgba(0,93,167,0.05)); }
</style>
</head>
<body class="bg-background min-h-screen text-on-surface">
<c:set var="ctx" value="${pageContext.request.contextPath}" scope="request" />
