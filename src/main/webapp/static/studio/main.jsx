/* global React, ReactDOM, AppScreen, UploadModal, ProcessingOverlay, AudioEngine */
// Splitnorder studio root — login sonrası ana ekran state machine.
//
//   EMPTY              → UploadModal otomatik açık, arkada AppScreen mock
//   PENDING/PROCESSING → ProcessingOverlay polling, arkada AppScreen mock
//   COMPLETED          → AudioEngine AppScreen'i CONTROLLED render eder
//                        (orijinal tasarım, gerçek 4 audio bağlı)
//   FAILED             → ProcessingOverlay hata kartı, arkada mock

function useAppScale(active) {
  const ref = React.useRef(null);
  React.useLayoutEffect(() => {
    if (!active) return;
    const apply = () => {
      const el = ref.current;
      if (!el) return;
      const w = el.clientWidth || window.innerWidth;
      const h = el.clientHeight || window.innerHeight;
      const s = Math.min(w / 1440, h / 900);
      el.style.setProperty("--app-scale", s.toFixed(4));
    };
    apply();
    requestAnimationFrame(apply);
    window.addEventListener("resize", apply);
    return () => window.removeEventListener("resize", apply);
  }, [active]);
  return ref;
}

const TWEAK_DEFAULTS = { trackName: "Midnight Bloom", artist: "Otherwave" };

function StudioRoot(props) {
  const fitRef = useAppScale(true);
  const { jobStatus, jobId, jobFilename, ctx } = props;

  // URL'de ?upload=1 → kullanıcı navbar'daki "Yeni Ayır" butonuna bastı,
  // jobStatus ne olursa olsun modal'ı zorla aç. Kapatma butonu kapatınca
  // URL'den param'ı temizle ki sayfa yenilense modal yeniden açılmasın.
  const [forceUpload, setForceUpload] = React.useState(() => {
    try {
      return new URLSearchParams(window.location.search).get("upload") === "1";
    } catch (e) { return false; }
  });

  const closeForcedUpload = React.useCallback(() => {
    setForceUpload(false);
    try {
      const u = new URL(window.location.href);
      u.searchParams.delete("upload");
      window.history.replaceState({}, "", u.toString());
    } catch (e) { /* sessizce yut */ }
  }, []);

  const isEmpty         = jobStatus === "EMPTY" || !jobStatus;
  const showUploadModal = isEmpty || forceUpload;
  const showProcessing  = !forceUpload && (jobStatus === "PENDING" || jobStatus === "PROCESSING" || jobStatus === "FAILED");
  const showAudio       = jobStatus === "COMPLETED" && jobId;

  // Modal'ın kapatılabilirliği: forceUpload moduyla açıldıysa kullanıcı X'leyebilir;
  // EMPTY state'inde job yok zaten — kapatınca arkada görecek bir şey olmaz, gizleme
  const modalCloseHandler = forceUpload ? closeForcedUpload : undefined;

  const tweaks = showAudio && jobFilename
    ? { trackName: jobFilename.replace(/\.[^/.]+$/, ""), artist: "Splitnorder" }
    : TWEAK_DEFAULTS;

  return (
    <React.Fragment>
      <div className="app-fit" ref={fitRef}>
        <div className="app-stage">
          {showAudio
            ? <AudioEngine ctx={ctx} jobId={jobId} tweaks={tweaks} />
            : <AppScreen tweaks={tweaks} /> }
        </div>
      </div>

      {showUploadModal && <UploadModal ctx={ctx} onClose={modalCloseHandler} />}

      {showProcessing && jobId && (
        <ProcessingOverlay
          ctx={ctx}
          jobId={jobId}
          filename={jobFilename}
          initialStatus={jobStatus} />
      )}
    </React.Fragment>
  );
}

const mount = document.getElementById("splitnorder-studio-root");
const ds = mount.dataset || {};
ReactDOM.createRoot(mount).render(
  <StudioRoot
    jobStatus={ds.jobStatus || "EMPTY"}
    jobId={ds.jobId || null}
    jobFilename={ds.jobFilename || null}
    ctx={ds.ctx || ""} />
);
