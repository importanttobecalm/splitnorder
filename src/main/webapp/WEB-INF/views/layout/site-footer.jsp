<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<footer class="bg-surface-container-lowest border-t border-outline-variant/30 w-full py-base">
  <div class="flex flex-col md:flex-row justify-between items-center px-margin_desktop max-w-7xl mx-auto gap-gutter py-6">
    <span class="font-mono-label text-mono-label text-on-surface-variant"><fmt:message key="footer.copyright" /></span>
    <div class="flex items-center gap-6">
      <a href="${ctx}/" class="text-on-surface-variant hover:text-primary transition-colors font-body-sm text-body-sm"><fmt:message key="nav.home" /></a>
      <a href="https://github.com/importanttobecalm/splitnorder" target="_blank" rel="noopener" class="text-on-surface-variant hover:text-primary transition-colors font-body-sm text-body-sm">GitHub</a>
    </div>
  </div>
</footer>
