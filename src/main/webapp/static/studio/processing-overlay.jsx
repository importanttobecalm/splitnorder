/* global React */
// Splitnorder Processing Overlay — job PENDING/PROCESSING iken studio üstünde
// görünür. setInterval ile /job/{id}/status'a polling yapar; COMPLETED gelince
// sayfayı yeniden yükler (yeni state'te audio engine bind olur).
// FAILED gelirse hata kartı + "Tekrar dene" yönlendirmesi.

function ProcessingOverlay(props) {
  const ctx = props.ctx || "";
  const jobId = props.jobId;
  const filename = props.filename || "—";

  const [status, setStatus] = React.useState(props.initialStatus || "PROCESSING");
  const [elapsed, setElapsed] = React.useState(0);
  const [error, setError] = React.useState(null);

  // Polling: her 2 sn /job/{id}/status
  React.useEffect(() => {
    if (!jobId) return;
    let cancelled = false;

    const poll = () => {
      fetch(ctx + "/job/" + jobId + "/status", { credentials: "same-origin" })
        .then(r => r.json())
        .then(data => {
          if (cancelled) return;
          if (data.error) {
            setError(data.error);
            return;
          }
          setStatus(data.status);
          if (data.status === "COMPLETED") {
            // Studio sayfayı yenileyince HomeController yeni job durumunu
            // sunucu tarafında render edip audio engine'i devreye sokar.
            window.location.href = ctx + "/?jobId=" + jobId;
          }
        })
        .catch(err => { if (!cancelled) setError(err.message); });
    };

    poll(); // ilk anlık check
    const id = setInterval(poll, 2000);
    return () => { cancelled = true; clearInterval(id); };
  }, [ctx, jobId]);

  // Geçen süre sayacı
  React.useEffect(() => {
    const id = setInterval(() => setElapsed(e => e + 1), 1000);
    return () => clearInterval(id);
  }, []);

  const isFailed = status === "FAILED" || error;

  return (
    <div className="upmodal-backdrop">
      <div className="upmodal-card" role="dialog" style={{ textAlign: "center" }}>

        {!isFailed && (
          <React.Fragment>
            <div className="proc-spinner" aria-hidden="true">
              <svg viewBox="0 0 48 48" width="64" height="64">
                <circle cx="24" cy="24" r="20" stroke="#EAF2FB" strokeWidth="4" fill="none" />
                <circle cx="24" cy="24" r="20" stroke="#4A90E2" strokeWidth="4" fill="none"
                        strokeLinecap="round" strokeDasharray="40 200"
                        style={{ transformOrigin: "center", animation: "procSpin 1.2s linear infinite" }} />
              </svg>
            </div>
            <h2 className="upmodal-title" style={{ marginTop: 18 }}>Şarkın ayrılıyor...</h2>
            <p className="upmodal-subtitle">{filename}</p>
            <p className="upmodal-subtitle" style={{ marginTop: 12 }}>
              Vokali, davulu, bası ve diğer enstrümanları çıkarıyoruz.
              Bu işlem ortalama 10-30 saniye sürer.
            </p>
            <div style={{ marginTop: 18, fontSize: 13, color: "#A4B0C0" }}>
              Geçen süre: <strong style={{ color: "#1E3A5F" }}>{elapsed} sn</strong> · Durum: <strong style={{ color: "#1E3A5F" }}>{status}</strong>
            </div>
          </React.Fragment>
        )}

        {isFailed && (
          <React.Fragment>
            <div style={{ fontSize: 48, marginBottom: 12 }}>⚠️</div>
            <h2 className="upmodal-title">İşlem başarısız</h2>
            <p className="upmodal-subtitle">{error || "Şarkın ayrılırken bir hata oluştu."}</p>
            <button className="upmodal-submit" style={{ marginTop: 24 }}
                    onClick={() => window.location.href = ctx + "/"}>
              Tekrar dene
            </button>
          </React.Fragment>
        )}
      </div>
    </div>
  );
}

window.ProcessingOverlay = ProcessingOverlay;
