/* global React */
// Splitnorder Upload Modal — studio'nun "boş durumu"nda otomatik açılır.
// Drag & drop + dosya seçici + model seçici + submit → POST /upload.ajax.
// Splitnorder design system: primary #4A90E2, dotted-grid, Plus Jakarta + Inter.
//
// NOT: app.jsx'de zaten `const { useState, useEffect, useRef } = React;`
// destructure ediliyor; aynı global scope'a girmemek için bu dosyada React.*
// prefix'i ile çağırıyoruz (main.jsx'teki Babel scope notu aynısı geçerli).

function UploadModal(props) {
  const ctx = props.ctx || "";
  const onClose = props.onClose;

  const [file, setFile] = React.useState(null);
  const [model, setModel] = React.useState("htdemucs");
  const [dragActive, setDragActive] = React.useState(false);
  const [uploading, setUploading] = React.useState(false);
  const [error, setError] = React.useState(null);
  const fileInputRef = React.useRef(null);

  const MAX_SIZE = 50 * 1024 * 1024;
  const ALLOWED = [".mp3", ".wav", ".flac"];

  function validateFile(f) {
    if (!f) return "Dosya seçilmedi";
    const name = f.name.toLowerCase();
    if (!ALLOWED.some(ext => name.endsWith(ext))) return "Geçersiz format. MP3, WAV veya FLAC kullan.";
    if (f.size > MAX_SIZE) return "Dosya 50 MB'tan büyük olamaz.";
    return null;
  }

  function handleFileSelect(f) {
    const err = validateFile(f);
    if (err) { setError(err); setFile(null); return; }
    setError(null);
    setFile(f);
  }

  function handleDrop(e) {
    e.preventDefault();
    setDragActive(false);
    const f = e.dataTransfer.files && e.dataTransfer.files[0];
    handleFileSelect(f);
  }

  function handleSubmit() {
    if (!file || uploading) return;
    setUploading(true);
    setError(null);

    const fd = new FormData();
    fd.append("file", file);
    fd.append("model", model);

    fetch(ctx + "/upload.ajax", { method: "POST", body: fd, credentials: "same-origin" })
      .then(r => r.json())
      .then(data => {
        if (data.error) {
          setError("Yükleme başarısız: " + data.error);
          setUploading(false);
        } else if (data.jobId) {
          window.location.href = ctx + "/?jobId=" + data.jobId;
        } else {
          setError("Beklenmedik cevap");
          setUploading(false);
        }
      })
      .catch(err => {
        setError("Ağ hatası: " + err.message);
        setUploading(false);
      });
  }

  function formatSize(bytes) {
    if (bytes < 1024) return bytes + " B";
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + " KB";
    return (bytes / (1024 * 1024)).toFixed(1) + " MB";
  }

  return (
    <div className="upmodal-backdrop">
      <div className="upmodal-card" role="dialog" aria-labelledby="upmodal-title">
        {onClose && (
          <button className="upmodal-close" onClick={onClose} aria-label="Kapat">×</button>
        )}

        <div className="upmodal-header">
          <h1 id="upmodal-title" className="upmodal-title">Şarkını yükle</h1>
          <p className="upmodal-subtitle">MP3, WAV veya FLAC · Maks. 50 MB · 10 dakikaya kadar</p>
        </div>

        <div
          className={"upmodal-dropzone" + (dragActive ? " active" : "") + (file ? " has-file" : "")}
          onDragOver={(e) => { e.preventDefault(); setDragActive(true); }}
          onDragLeave={() => setDragActive(false)}
          onDrop={handleDrop}
          onClick={() => !file && fileInputRef.current && fileInputRef.current.click()}>
          <input
            ref={fileInputRef}
            type="file"
            accept=".mp3,.wav,.flac,audio/*"
            style={{ display: "none" }}
            onChange={(e) => handleFileSelect(e.target.files[0])} />

          {!file && (
            <React.Fragment>
              <div className="upmodal-icon-circle">
                <svg viewBox="0 0 24 24" width="44" height="44" fill="none">
                  <path d="M12 4v12m0 0l-4-4m4 4l4-4M5 20h14" stroke="#4A90E2" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
                </svg>
              </div>
              <div className="upmodal-dz-title">Dosyanı buraya sürükle</div>
              <div className="upmodal-dz-or">veya</div>
              <button type="button" className="upmodal-btn-primary"
                      onClick={(e) => { e.stopPropagation(); fileInputRef.current.click(); }}>
                Dosya Seç
              </button>
              <div className="upmodal-finepr">Dosyaların 30 gün boyunca saklanır.</div>
            </React.Fragment>
          )}

          {file && (
            <div className="upmodal-file-info">
              <div className="upmodal-file-icon">♪</div>
              <div className="upmodal-file-meta">
                <div className="upmodal-file-name">{file.name}</div>
                <div className="upmodal-file-size">{formatSize(file.size)}</div>
              </div>
              <button type="button" className="upmodal-file-remove" onClick={(e) => { e.stopPropagation(); setFile(null); }} aria-label="Kaldır">×</button>
            </div>
          )}
        </div>

        {error && <div className="upmodal-error">{error}</div>}

        <div className="upmodal-models">
          <div className="upmodal-models-label">Model kalitesi</div>
          <div className="upmodal-models-grid">
            {[
              { id: "htdemucs", label: "Hızlı", sub: "htdemucs" },
              { id: "htdemucs_ft", label: "Standart", sub: "htdemucs_ft" }
            ].map(m => (
              <label key={m.id} className={"upmodal-model" + (model === m.id ? " selected" : "")}>
                <input type="radio" name="model" value={m.id}
                       checked={model === m.id}
                       onChange={() => setModel(m.id)} />
                <div className="upmodal-model-label">{m.label}</div>
                <div className="upmodal-model-sub">{m.sub}</div>
              </label>
            ))}
          </div>
        </div>

        <button
          className="upmodal-submit"
          disabled={!file || uploading}
          onClick={handleSubmit}>
          {uploading ? "Yükleniyor..." : "Ayırmaya Başla"}
        </button>
      </div>
    </div>
  );
}

window.UploadModal = UploadModal;
