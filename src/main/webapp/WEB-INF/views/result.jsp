<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<jsp:include page="/WEB-INF/views/layout/head.jsp">
  <jsp:param name="titleKey" value="studio.title" />
</jsp:include>
<jsp:include page="/WEB-INF/views/layout/nav.jsp" />

<style>
  .stem-card { background: rgb(var(--card-rgb, 255 255 255) / 0.95); border-radius: 16px; box-shadow: 0 4px 24px rgba(30,50,90,0.06); border: 1px solid rgba(193,199,211,0.3); }
  .waveform-bar { width: 4px; min-height: 2px; border-radius: 2px; }
</style>

<main class="max-w-[1240px] mx-auto px-margin_mobile md:px-margin_desktop pt-8 pb-32">

  <%-- Üst başlık --%>
  <div class="flex flex-col md:flex-row justify-between items-start md:items-center mb-8 gap-4">
    <div class="flex items-center gap-4">
      <div class="w-16 h-16 rounded-xl bg-gradient-to-br from-primary to-inverse-primary shadow-sm flex items-center justify-center text-on-primary">
        <span class="material-symbols-outlined text-[28px]">graphic_eq</span>
      </div>
      <div>
        <h1 class="font-headline-md text-headline-md text-on-surface mb-1">${job.originalFilename}</h1>
        <p class="font-mono-label text-mono-label text-on-surface-variant uppercase">${job.modelUsed} · ${fn:replace(fn:substring(job.createdAt, 0, 16), "T", " ")}</p>
      </div>
    </div>
    <a href="${ctx}/job/${job.publicId}/download-all" class="bg-primary text-on-primary px-6 py-3 rounded-xl font-body-md font-medium hover:bg-primary-container transition-colors inline-flex items-center gap-2 shadow-sm">
      <span class="material-symbols-outlined">download</span>
      <fmt:message key="studio.downloadAll" />
    </a>
  </div>

  <%-- 4 stem kart (grid layout — Stitch'in absolute konumuna karşılık) --%>
  <c:set var="stemList" value="vocals,drums,bass,other" />
  <c:set var="stemColors" value="#E53935,#FB8C00,#5E35B1,#00897B" />
  <c:set var="stemIcons" value="mic,radio,graphic_eq,music_note" />

  <div class="grid grid-cols-1 md:grid-cols-2 gap-gutter mb-12">
    <c:forEach var="stem" items="${stemList}" varStatus="loop">
      <c:set var="color" value="${stemColors.split(',')[loop.index]}" />
      <c:set var="icon" value="${stemIcons.split(',')[loop.index]}" />

      <div class="stem-card p-6 flex flex-col gap-4" style="border-left: 4px solid ${color};" data-stem="${stem}">
        <div class="flex justify-between items-start">
          <div class="flex items-center gap-3">
            <input type="checkbox" data-role="mix-select" value="${stem}"
                   class="w-5 h-5 rounded cursor-pointer accent-current"
                   style="color: ${color};"
                   title="<fmt:message key='mix.select' />">
            <span class="material-symbols-outlined" style="color: ${color};">${icon}</span>
            <h3 class="font-headline-sm text-[20px] text-on-surface uppercase font-bold"><fmt:message key="stem.${stem}" /></h3>
          </div>
          <div class="flex gap-2">
            <button type="button" data-action="solo" title="Solo" class="stem-btn w-8 h-8 rounded bg-surface-variant text-on-surface-variant font-mono-label hover:bg-surface-dim transition-colors">S</button>
            <button type="button" data-action="mute" title="Mute" class="stem-btn w-8 h-8 rounded bg-surface-variant text-on-surface-variant font-mono-label hover:bg-surface-dim transition-colors">M</button>
            <a href="${ctx}/job/${job.publicId}/download/${stem}" class="w-8 h-8 rounded bg-surface-variant text-on-surface-variant hover:bg-surface-dim transition-colors flex items-center justify-center">
              <span class="material-symbols-outlined text-[18px]">download</span>
            </a>
          </div>
        </div>

        <div class="h-16 flex items-end justify-between w-full gap-[1px]">
          <c:forEach var="h" items="${[20,40,30,60,80,50,70,90,100,60,40,30,50,20,10,30,20,10,40,60,80,50,70,90,100,60,40,30,50,20,10,30]}">
            <div class="waveform-bar" style="height: ${h}%; background-color: ${color};"></div>
          </c:forEach>
        </div>

        <div class="flex items-center gap-2">
          <span class="material-symbols-outlined text-outline text-[16px]">volume_up</span>
          <input type="range" min="0" max="100" value="80" data-role="volume" class="w-full h-1 bg-surface-variant rounded-full appearance-none" style="accent-color: ${color};">
        </div>

        <audio controls data-role="audio" class="w-full mt-2">
          <source src="${ctx}/job/${job.publicId}/stream/${stem}" type="audio/wav">
        </audio>
      </div>
    </c:forEach>
  </div>

  <%-- ===== Karma Mix Paneli ===== --%>
  <div id="mixPanel" class="bg-surface-container-lowest rounded-xl soft-shadow p-6 mb-12">
    <div class="flex flex-col md:flex-row md:items-center md:justify-between gap-4 mb-4">
      <div>
        <h2 class="font-headline-sm text-[18px] text-on-surface mb-1 flex items-center gap-2">
          <span class="material-symbols-outlined text-primary">tune</span>
          <fmt:message key="mix.title" />
        </h2>
        <p class="font-body-sm text-on-surface-variant"><fmt:message key="mix.subtitle" /></p>
      </div>
      <div class="flex items-center gap-2">
        <span id="mixSelectionCount" class="font-mono-label text-mono-label text-on-surface-variant"></span>
        <div class="flex rounded-lg bg-surface-container-low border border-outline-variant/40 overflow-hidden">
          <button type="button" data-fmt="mp3" class="mix-fmt-btn px-3 py-2 font-mono-label text-mono-label bg-primary text-on-primary">MP3</button>
          <button type="button" data-fmt="wav" class="mix-fmt-btn px-3 py-2 font-mono-label text-mono-label text-on-surface-variant">WAV</button>
        </div>
        <button id="mixBuildBtn" type="button" disabled
                class="bg-primary text-on-primary px-5 py-2.5 rounded-xl font-body-md font-medium hover:bg-primary-container transition-colors inline-flex items-center gap-2 disabled:opacity-50 disabled:cursor-not-allowed">
          <span class="material-symbols-outlined text-[20px]" id="mixBuildIcon">graphic_eq</span>
          <span><fmt:message key="mix.build" /></span>
        </button>
      </div>
    </div>

    <div id="mixError" class="hidden p-3 mb-3 bg-error-container text-on-error-container rounded-lg font-body-sm"></div>

    <%-- Üretilen mix'lerin listesi --%>
    <div id="mixList" class="space-y-3"></div>
    <p id="mixEmpty" class="hidden text-center py-6 text-on-surface-variant font-body-sm">
      <fmt:message key="mix.empty" />
    </p>
  </div>

  <%-- Orijinal master --%>
  <div class="bg-surface-container-lowest rounded-xl soft-shadow p-6 mb-12">
    <div class="flex items-center gap-4">
      <div class="w-16 h-16 rounded-lg bg-gradient-to-br from-primary-container to-primary flex items-center justify-center text-on-primary">
        <span class="material-symbols-outlined text-[28px]">album</span>
      </div>
      <div class="flex-1">
        <h2 class="font-headline-sm text-[18px] text-on-surface mb-1"><fmt:message key="studio.master" /></h2>
        <p class="font-mono-numeric text-on-surface-variant">${job.originalFilename}</p>
      </div>
    </div>
    <audio controls class="w-full mt-4">
      <source src="${ctx}/job/${job.publicId}/stream/original" type="audio/mpeg">
    </audio>
  </div>

  <%-- Tekrar ayrıştır / Geçmişe dön --%>
  <div class="flex flex-col sm:flex-row gap-4 justify-center">
    <a href="${ctx}/upload" class="bg-surface-container-lowest border border-primary text-primary px-6 py-3 rounded-xl font-body-md font-medium hover:bg-primary-fixed/20 transition-colors inline-flex items-center justify-center gap-2">
      <span class="material-symbols-outlined">upload_file</span>
      <fmt:message key="studio.uploadNew" />
    </a>
    <a href="${ctx}/history" class="bg-surface-container-lowest border border-outline-variant text-on-surface-variant px-6 py-3 rounded-xl font-body-md font-medium hover:bg-surface-container-low transition-colors inline-flex items-center justify-center gap-2">
      <span class="material-symbols-outlined">history</span>
      <fmt:message key="nav.history" />
    </a>
  </div>
</main>

<script>
// ===== Karma Mix Paneli =====
(function(){
  var ctx = '${ctx}';
  var publicId = '${job.publicId}';
  var i18n = {
    confirmDelete: '<fmt:message key="storage.delete.confirm" />',
    minTwo: '<fmt:message key="mix.error.minTwo" />',
    loading: '<fmt:message key="mix.loading" />',
    download: '<fmt:message key="storage.delete" />',
    quotaExceeded: '<fmt:message key="storage.error.quotaExceeded" />',
    inferenceFailed: '<fmt:message key="job.error.INFERENCE_FAILED" />',
    deleteError: '<fmt:message key="storage.delete.error" />'
  };

  var selectedFmt = 'mp3';
  var fmtBtns = document.querySelectorAll('.mix-fmt-btn');
  fmtBtns.forEach(function(btn){
    btn.addEventListener('click', function(){
      selectedFmt = btn.dataset.fmt;
      fmtBtns.forEach(function(b){
        var active = (b === btn);
        b.classList.toggle('bg-primary', active);
        b.classList.toggle('text-on-primary', active);
        b.classList.toggle('text-on-surface-variant', !active);
      });
    });
  });

  var selects = document.querySelectorAll('input[data-role="mix-select"]');
  var buildBtn = document.getElementById('mixBuildBtn');
  var countEl = document.getElementById('mixSelectionCount');
  var errEl = document.getElementById('mixError');

  function getSelected(){
    return Array.from(selects).filter(function(c){ return c.checked; }).map(function(c){ return c.value; });
  }
  function refreshSelection(){
    var sel = getSelected();
    countEl.textContent = sel.length === 0 ? '' : (sel.length + ' / 4');
    buildBtn.disabled = sel.length < 2;
  }
  selects.forEach(function(c){ c.addEventListener('change', refreshSelection); });

  function showError(msg){
    errEl.textContent = msg;
    errEl.classList.remove('hidden');
    setTimeout(function(){ errEl.classList.add('hidden'); }, 6000);
  }

  function renderMix(m){
    var li = document.createElement('div');
    li.className = 'flex items-center justify-between p-4 bg-surface-container-low rounded-lg border border-outline-variant/30';
    li.dataset.mixId = m.mixId;
    var sizeMb = (m.fileSize / 1048576).toFixed(1);
    li.innerHTML =
      '<div class="flex items-center gap-3 min-w-0">' +
        '<span class="material-symbols-outlined text-primary">queue_music</span>' +
        '<div class="min-w-0">' +
          '<div class="font-body-md font-medium text-on-surface truncate">' + escapeHtml(m.name) + '</div>' +
          '<div class="font-mono-label text-mono-label text-on-surface-variant uppercase">' + m.format + ' · ' + sizeMb + ' MB</div>' +
        '</div>' +
      '</div>' +
      '<div class="flex items-center gap-2 shrink-0">' +
        '<audio controls preload="none" class="h-9 max-w-[260px]"><source src="' + ctx + m.downloadUrl + '"></audio>' +
        '<a href="' + ctx + m.downloadUrl + '" download class="w-9 h-9 rounded-lg bg-surface-container text-on-surface-variant hover:bg-primary hover:text-on-primary flex items-center justify-center transition-colors" title="Download"><span class="material-symbols-outlined text-[18px]">download</span></a>' +
        '<button type="button" data-act="del" class="w-9 h-9 rounded-lg bg-surface-container text-on-surface-variant hover:bg-error hover:text-on-error flex items-center justify-center transition-colors" title="Delete"><span class="material-symbols-outlined text-[18px]">delete</span></button>' +
      '</div>';
    li.querySelector('button[data-act="del"]').addEventListener('click', function(){
      if (!confirm(i18n.confirmDelete)) return;
      fetch(ctx + '/job/' + publicId + '/mix/' + m.mixId + '/delete', { method: 'POST' })
        .then(function(r){ return r.json(); })
        .then(function(j){
          if (j.ok) { li.remove(); checkEmpty(); }
          else { showError(i18n.deleteError); }
        })
        .catch(function(){ showError(i18n.deleteError); });
    });
    return li;
  }

  function escapeHtml(s){
    return String(s).replace(/[&<>"']/g, function(c){
      return { '&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;' }[c];
    });
  }

  var listEl = document.getElementById('mixList');
  var emptyEl = document.getElementById('mixEmpty');
  function checkEmpty(){
    emptyEl.classList.toggle('hidden', listEl.children.length > 0);
  }

  function loadExisting(){
    fetch(ctx + '/job/' + publicId + '/mix')
      .then(function(r){ return r.json(); })
      .then(function(arr){
        listEl.innerHTML = '';
        (arr || []).forEach(function(m){ listEl.appendChild(renderMix(m)); });
        checkEmpty();
      })
      .catch(function(){ checkEmpty(); });
  }

  buildBtn.addEventListener('click', function(){
    var sel = getSelected();
    if (sel.length < 2) { showError(i18n.minTwo); return; }
    buildBtn.disabled = true;
    var orig = buildBtn.querySelector('span:last-child').textContent;
    buildBtn.querySelector('span:last-child').textContent = i18n.loading;

    var body = new URLSearchParams();
    body.set('stems', sel.join(','));
    body.set('fmt', selectedFmt);
    fetch(ctx + '/job/' + publicId + '/mix', {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      body: body.toString()
    })
      .then(function(r){ return r.json(); })
      .then(function(j){
        if (j.error) {
          if (j.error === 'storage.error.quotaExceeded') showError(i18n.quotaExceeded);
          else showError(i18n.inferenceFailed);
        } else {
          listEl.prepend(renderMix(j));
          checkEmpty();
          // seçimleri temizle
          selects.forEach(function(c){ c.checked = false; });
          refreshSelection();
        }
      })
      .catch(function(){ showError(i18n.inferenceFailed); })
      .finally(function(){
        buildBtn.querySelector('span:last-child').textContent = orig;
        refreshSelection();
      });
  });

  loadExisting();
  refreshSelection();
})();
</script>

<script>
(function(){
  var cards = document.querySelectorAll('.stem-card');
  var states = {};
  cards.forEach(function(card){
    var key = card.dataset.stem;
    states[key] = { mute: false, solo: false };
    var audio = card.querySelector('audio[data-role="audio"]');
    var vol = card.querySelector('input[data-role="volume"]');
    if (audio && vol) {
      audio.volume = vol.value / 100;
      vol.addEventListener('input', function(){ audio.volume = vol.value / 100; });
    }
  });
  function apply(){
    var soloActive = Object.keys(states).some(function(k){ return states[k].solo; });
    cards.forEach(function(card){
      var key = card.dataset.stem;
      var audio = card.querySelector('audio[data-role="audio"]');
      if (!audio) return;
      var shouldMute = states[key].mute || (soloActive && !states[key].solo);
      audio.muted = shouldMute;
      card.style.opacity = shouldMute ? '0.55' : '1';
    });
  }
  cards.forEach(function(card){
    var key = card.dataset.stem;
    card.querySelectorAll('.stem-btn').forEach(function(btn){
      btn.addEventListener('click', function(){
        var action = btn.dataset.action;
        states[key][action] = !states[key][action];
        if (action === 'solo' && states[key].solo) states[key].mute = false;
        if (action === 'mute' && states[key].mute) states[key].solo = false;
        card.querySelectorAll('.stem-btn').forEach(function(b){
          var on = states[key][b.dataset.action];
          b.classList.toggle('bg-primary', on);
          b.classList.toggle('text-on-primary', on);
          b.classList.toggle('bg-surface-variant', !on);
          b.classList.toggle('text-on-surface-variant', !on);
        });
        apply();
      });
    });
  });
})();
</script>

<jsp:include page="/WEB-INF/views/layout/site-footer.jsp" />
<jsp:include page="/WEB-INF/views/layout/footer.jsp" />
