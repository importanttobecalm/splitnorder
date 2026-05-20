/* global React */
// Splitnorder main app screen — node graph + central input + bottom playback.

const { useState, useEffect, useRef, useMemo } = React;

// ─── inline svg icons (small, clean, original) ───────────────────────────────
const I = {
  note: (c = "#fff") =>
  <svg viewBox="0 0 24 24" width="16" height="16" fill="none">
      <path d="M9 18V6l11-2v12" stroke={c} strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
      <circle cx="6.5" cy="18" r="2.5" fill={c} />
      <circle cx="17.5" cy="16" r="2.5" fill={c} />
    </svg>,
  mic: (c = "#fff") =>
  <svg viewBox="0 0 24 24" width="16" height="16" fill="none">
      <rect x="9" y="3" width="6" height="11" rx="3" fill={c} />
      <path d="M6 11a6 6 0 0 0 12 0M12 17v4" stroke={c} strokeWidth="2" strokeLinecap="round" />
    </svg>,
  bass: (c = "#fff") =>
  <svg viewBox="0 0 24 24" width="16" height="16" fill="none">
      <path d="M7 6a5 5 0 0 1 9 3c0 4-5 6-9 9" stroke={c} strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round" />
      <circle cx="18" cy="9" r="1.2" fill={c} />
      <circle cx="18" cy="13" r="1.2" fill={c} />
    </svg>,
  drum: (c = "#fff") =>
  <svg viewBox="0 0 24 24" width="16" height="16" fill="none">
      <ellipse cx="12" cy="8" rx="8" ry="2.4" stroke={c} strokeWidth="1.6" />
      <path d="M4 8v6c0 1.3 3.6 2.4 8 2.4s8-1.1 8-2.4V8" stroke={c} strokeWidth="1.6" />
      <path d="M9 10l-2 9M15 10l2 9" stroke={c} strokeWidth="1.6" strokeLinecap="round" />
    </svg>,
  download: (c = "currentColor") =>
  <svg viewBox="0 0 24 24" width="18" height="18" fill="none">
      <path d="M12 4v11m0 0l-4-4m4 4l4-4M5 20h14" stroke={c} strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
    </svg>,
  share: (c = "currentColor") =>
  <svg viewBox="0 0 24 24" width="18" height="18" fill="none">
      <circle cx="6" cy="12" r="2.5" stroke={c} strokeWidth="1.8" />
      <circle cx="17" cy="6" r="2.5" stroke={c} strokeWidth="1.8" />
      <circle cx="17" cy="18" r="2.5" stroke={c} strokeWidth="1.8" />
      <path d="M8.3 11l6.4-3.5M8.3 13l6.4 3.5" stroke={c} strokeWidth="1.8" />
    </svg>,
  play: (c = "currentColor") =>
  <svg viewBox="0 0 24 24" width="14" height="14" fill={c}>
      <path d="M8 5v14l11-7z" />
    </svg>,
  pause: (c = "currentColor") =>
  <svg viewBox="0 0 24 24" width="14" height="14" fill={c}>
      <rect x="6" y="5" width="4" height="14" rx="1" />
      <rect x="14" y="5" width="4" height="14" rx="1" />
    </svg>,
  vol: (c = "currentColor") =>
  <svg viewBox="0 0 24 24" width="18" height="18" fill="none">
      <path d="M4 10v4h3l5 4V6L7 10H4z" fill={c} />
      <path d="M15.5 8.5a5 5 0 0 1 0 7M18 6a8 8 0 0 1 0 12" stroke={c} strokeWidth="1.6" strokeLinecap="round" />
    </svg>,
  heart: (c = "currentColor") =>
  <svg viewBox="0 0 24 24" width="14" height="14" fill="none">
      <path d="M12 20s-7-4.35-7-10a4 4 0 0 1 7-2.65A4 4 0 0 1 19 10c0 5.65-7 10-7 10z" stroke={c} strokeWidth="1.6" strokeLinejoin="round" />
    </svg>
};

// ─── deterministic music-like envelope generator ─────────────────────────────
// Returns an array of {top, bot} normalized amplitudes for a mirrored waveform.
// Each stem uses a different envelope shape so the music "looks" believable.
function buildMusicWave(seed, n, style) {
  const out = [];
  let s = seed * 7919;
  for (let i = 0; i < n; i++) {
    s = (s * 9301 + 49297) % 233280;
    const r = s / 233280 - 0.5;
    const t = i / n;
    let v;

    if (style === "vocals") {
      // breathy, phrased: low → high crests with silences
      const phrase = 0.5 + 0.5 * Math.sin(t * Math.PI * 4 - 1);
      const detail = 0.4 + 0.3 * Math.sin(t * 90 + seed);
      v = phrase * detail * (0.7 + r * 0.5);
    } else if (style === "drums") {
      // sharp peaks at regular intervals (beats)
      const beat = Math.abs(Math.sin(t * Math.PI * 16 + seed * 0.1));
      const accent = Math.pow(beat, 6) * 1.0;
      const ghost = Math.pow(0.5 + 0.5 * Math.sin(t * 130), 3) * 0.35;
      v = accent + ghost + Math.abs(r) * 0.15;
    } else if (style === "bass") {
      // rolling low oscillations
      const groove = 0.55 + 0.4 * Math.sin(t * Math.PI * 6 + seed * 0.5);
      const sub = 0.2 * Math.sin(t * 22 + seed);
      v = groove + sub + r * 0.15;
    } else {
      // "other" — atmospheric, fluctuating
      const pad = 0.45 + 0.35 * Math.sin(t * Math.PI * 3 + seed);
      const sparkle = 0.25 * Math.sin(t * 50 + seed * 2);
      v = pad + sparkle + Math.abs(r) * 0.3;
    }

    v = Math.max(0.05, Math.min(1, v));
    // slight asymmetry for organic feel
    const top = v;
    const bot = v * (0.78 + Math.abs(r) * 0.3);
    out.push({ top, bot });
  }
  return out;
}

// ─── mirrored waveform with rounded bars ─────────────────────────────────────
function Waveform({
  color, seed = 1, bars = 56, height = 36, progress = 0,
  style: waveStyle = "other", playing = false, glow = false,
  onSeek
}) {
  const data = useMemo(() => buildMusicWave(seed, bars, waveStyle), [seed, bars, waveStyle]);
  const [phase, setPhase] = useState(0);
  useEffect(() => {
    if (!playing) return;
    let raf;
    const tick = (t) => {setPhase(t / 1000);raf = requestAnimationFrame(tick);};
    raf = requestAnimationFrame(tick);
    return () => cancelAnimationFrame(raf);
  }, [playing]);

  const barW = 2.4;
  const gap = 1.8;
  const total = bars * (barW + gap) - gap;
  const center = height / 2;
  const max = height / 2 * 0.96;
  const gradId = `wgrad-${seed}-${color.replace("#", "")}`;
  const glowId = `wglow-${seed}-${color.replace("#", "")}`;

  const handleClick = (e) => {
    if (!onSeek) return;
    const rect = e.currentTarget.getBoundingClientRect();
    const u = Math.max(0, Math.min(1, (e.clientX - rect.left) / rect.width));
    onSeek(u);
  };

  return (
    <svg
      viewBox={`0 0 ${total} ${height}`}
      preserveAspectRatio="none"
      width="100%"
      height={height}
      onClick={onSeek ? handleClick : undefined}
      style={onSeek ? { cursor: "pointer" } : undefined}>
      
      <defs>
        <linearGradient id={gradId} x1="0" y1="0" x2="0" y2="1">
          <stop offset="0%" stopColor={color} stopOpacity="0.55" />
          <stop offset="50%" stopColor={color} stopOpacity="1" />
          <stop offset="100%" stopColor={color} stopOpacity="0.55" />
        </linearGradient>
        {glow &&
        <filter id={glowId} x="-20%" y="-20%" width="140%" height="140%">
            <feGaussianBlur stdDeviation="0.6" result="b" />
            <feMerge><feMergeNode in="b" /><feMergeNode in="SourceGraphic" /></feMerge>
          </filter>
        }
      </defs>
      {data.map((v, i) => {
        const u = i / bars;
        // small live wobble near the playhead when playing
        const dist = Math.abs(u - progress);
        const liveBoost = playing && dist < 0.06 ?
        (1 - dist / 0.06) * 0.25 * (0.5 + 0.5 * Math.sin(phase * 18 + i)) :
        0;
        const top = Math.min(1, v.top + liveBoost) * max;
        const bot = Math.min(1, v.bot + liveBoost) * max;
        const x = i * (barW + gap);
        const played = u < progress;
        return (
          <rect
            key={i}
            x={x}
            y={center - top}
            width={barW}
            height={top + bot}
            rx={barW / 2}
            fill={`url(#${gradId})`}
            opacity={played ? 1 : 0.30}
            filter={glow && played ? `url(#${glowId})` : undefined} />);


      })}
    </svg>);

}

// ─── stem card ───────────────────────────────────────────────────────────────
function StemCard({ stem, onToggle, onVolume, onPlay, onSeek, progress, masterPlaying, style, mixMode, mixSelected, onMixToggle }) {
  const isAudible = !stem.muted && (masterPlaying || stem.playing);
  return (
    <div
      className={"stem" + (stem.muted ? " muted" : "") + (stem.playing ? " soloed" : "") + (mixMode ? " mix-mode" : "") + (mixMode && mixSelected ? " mix-selected" : "")}
      style={{ ...style, "--accent": stem.color }}
      data-screen-label={`stem ${stem.name}`}
      onClick={mixMode ? (e) => { if (e.target.tagName !== "INPUT") onMixToggle(stem.id); } : undefined}>

      <div className="stem-head">
        <div className="stem-icon" style={{ background: stem.color }}>
          {stem.icon}
        </div>
        <div className="stem-name">{stem.name}</div>
        {mixMode ? (
          <label className="stem-mix-check" title="Bu stem'i mix'e ekle" onClick={(e) => e.stopPropagation()}>
            <input
              type="checkbox"
              checked={!!mixSelected}
              onChange={() => onMixToggle(stem.id)}
              style={{ accentColor: stem.color }} />
          </label>
        ) : (
          <button
            className={"stem-play" + (stem.playing ? " on" : "")}
            style={stem.playing ? { background: stem.color, color: "#fff", borderColor: stem.color } : {}}
            onClick={() => onPlay(stem.id)}
            title={stem.playing ? "Pause this stem" : "Play this stem only"}>

            {stem.playing ? I.pause() : I.play()}
          </button>
        )}
      </div>
      <div className="stem-wave">
        <Waveform
          color={stem.color}
          seed={stem.seed}
          bars={56}
          height={42}
          progress={progress}
          style={stem.id}
          playing={isAudible}
          glow={true}
          onSeek={onSeek} />
        
      </div>
      <div className="stem-controls">
        <div
          className={"tag solo" + (stem.solo ? " active" : "")}
          onClick={() => onToggle(stem.id, "solo")}
          title="Solo">
          S</div>
        <div
          className={"tag mute" + (stem.muted ? " active" : "")}
          onClick={() => onToggle(stem.id, "muted")}
          title="Mute">
          M</div>
        <div className="slider">
          <div className="slider-track">
            <div
              className="slider-fill"
              style={{ width: `${stem.vol * 100}%`, background: stem.color }} />
            
          </div>
          <div className="slider-thumb" style={{ left: `${stem.vol * 100}%` }} />
          <input
            type="range" min="0" max="100" step="1"
            value={Math.round(stem.vol * 100)}
            onChange={(e) => onVolume(stem.id, +e.target.value / 100)} />
          
        </div>
        <button className="dl-btn" title="Download stem">
          {I.download()}
        </button>
      </div>
    </div>);

}

// ─── curved connector path ───────────────────────────────────────────────────
function bezier(a, b, bend = 0.45) {
  const dx = b.x - a.x;
  const c1 = { x: a.x + dx * bend, y: a.y };
  const c2 = { x: b.x - dx * bend, y: b.y };
  return `M ${a.x} ${a.y} C ${c1.x} ${c1.y}, ${c2.x} ${c2.y}, ${b.x} ${b.y}`;
}

// ─── animated electric connector ─────────────────────────────────────────────
// Two layers: a thin neutral track + a colored "spark" pulse using
// stroke-dasharray + animating dashoffset. Plus three particles travelling
// along the path via <animateMotion> referencing the same path id.
function Connector({ id, d, color, active, speed = 1 }) {
  return (
    <g className={"connector" + (active ? " active" : "")}>
      {/* invisible reference path for animateMotion */}
      <path id={`path-${id}`} d={d} fill="none" stroke="none" />
      {/* neutral baseline */}
      <path d={d} className="wire-base" />
      {/* colored stream (dash flow) */}
      <path
        d={d}
        className="wire-flow"
        stroke={color}
        style={{ "--flow-speed": `${2.4 / speed}s`, opacity: active ? 1 : 0.18 }} />
      
      {/* soft glow underlay when active */}
      {active &&
      <path
        d={d}
        className="wire-glow"
        stroke={color} />

      }
      {/* travelling sparks */}
      {active && [0, 0.33, 0.66].map((offset, i) =>
      <circle key={i} r="3" fill={color} className="spark">
          <animateMotion
          dur={`${2.0 / speed}s`}
          repeatCount="indefinite"
          begin={`-${offset * (2.0 / speed)}s`}
          rotate="auto">
          
            <mpath href={`#path-${id}`} />
          </animateMotion>
          <animate
          attributeName="opacity"
          values="0; 1; 1; 0"
          keyTimes="0; 0.15; 0.85; 1"
          dur={`${2.0 / speed}s`}
          begin={`-${offset * (2.0 / speed)}s`}
          repeatCount="indefinite" />
        
        </circle>
      )}
    </g>);

}

// ─── main app ────────────────────────────────────────────────────────────────
// `audio` prop verilirse AppScreen "controlled" modda çalışır: state ve
// callback'ler dışarıdan (gerçek <audio> motoru) gelir. Verilmezse iç
// useState ile mock olarak çalışır (boş demo / processing aşaması). Tasarım
// her iki modda da birebir aynıdır — sadece bağlantı yeri değişir.
function AppScreen({ tweaks = {}, audio }) {
  const [intPlaying, setIntPlaying] = useState(true);
  const [intTime, setIntTime] = useState(88);
  const [intDuration, setIntDuration] = useState(225);
  const [intMaster, setIntMaster] = useState(0.72);
  const [intStems, setIntStems] = useState([
  { id: "other", name: "Other", color: "#2DB3A0", icon: I.note(), seed: 11, vol: 0.55, solo: false, muted: false, playing: false, pos: "tl" },
  { id: "vocals", name: "Vocals", color: "#E5444C", icon: I.mic(), seed: 27, vol: 0.62, solo: false, muted: false, playing: false, pos: "tr" },
  { id: "bass", name: "Bass", color: "#5B3A8F", icon: I.bass(), seed: 53, vol: 0.42, solo: false, muted: false, playing: false, pos: "bl" },
  { id: "drums", name: "Drums", color: "#F0A45F", icon: I.drum(), seed: 71, vol: 0.60, solo: false, muted: false, playing: false, pos: "br" }]
  );

  // Controlled vs. internal: tek tipte türev. Source of truth audio varsa o.
  const playing  = audio ? audio.playing  : intPlaying;
  const time     = audio ? audio.time     : intTime;
  const duration = audio ? audio.duration : intDuration;
  const master   = audio ? audio.master   : intMaster;
  const stems    = audio ? audio.stems    : intStems;
  const setMaster = audio ? audio.onMasterVolume : setIntMaster;

  // any stem in its own "play only this" mode?
  const anyStemSolo = stems.some((s) => s.playing);
  const ticking = playing || anyStemSolo;

  // progress tick — sadece mock modunda. Audio modunda gerçek currentTime
  // <audio> elementinden geldiği için fake tick'e gerek yok.
  useEffect(() => {
    if (audio) return;
    if (!ticking) return;
    const id = setInterval(() => {
      setIntTime((t) => t + 0.1 >= duration ? 0 : t + 0.1);
    }, 100);
    return () => clearInterval(id);
  }, [ticking, duration, audio]);

  const progress = duration > 0 ? time / duration : 0;
  const fmt = (s) => {
    const m = Math.floor(s / 60).toString().padStart(2, "0");
    const r = Math.floor(s % 60).toString().padStart(2, "0");
    return `${m}:${r}`;
  };

  const onToggle = (id, key) => {
    if (audio) { audio.onStemToggle(id, key); return; }
    setIntStems((arr) => arr.map((s) => s.id === id ? { ...s, [key]: !s[key] } : s));
  };
  const onVolume = (id, v) => {
    if (audio) { audio.onStemVolume(id, v); return; }
    setIntStems((arr) => arr.map((s) => s.id === id ? { ...s, vol: v } : s));
  };
  // Sync seek: clicking ANY waveform jumps the shared timeline.
  const onSeek = (u) => {
    if (audio) { audio.onSeek(u); return; }
    setIntTime(Math.max(0, Math.min(duration, u * duration)));
  };
  // per-stem play: toggle this stem's solo-play.
  const onStemPlay = (id) => {
    if (audio) { audio.onStemPlay(id); return; }
    setIntStems((arr) =>
    arr.map((s) => s.id === id ? { ...s, playing: !s.playing } : { ...s, playing: false })
    );
    setIntPlaying(false);
  };
  // master play: clear any per-stem solo and resume the full mix
  const onMasterPlay = () => {
    if (audio) { audio.onMasterPlay(); return; }
    setIntStems((arr) => arr.map((s) => ({ ...s, playing: false })));
    setIntPlaying((p) => !p);
  };

  // positions for stem cards
  const posMap = {
    tl: { left: "6%", top: "8%" },
    tr: { right: "6%", top: "8%" },
    bl: { left: "7%", bottom: "10%" },
    br: { right: "7%", bottom: "10%" }
  };
  const wireAnchors = {
    tl: { x: 0.30, y: 0.28 },
    tr: { x: 0.70, y: 0.28 },
    bl: { x: 0.30, y: 0.78 },
    br: { x: 0.70, y: 0.78 }
  };
  const center = { x: 0.5, y: 0.5 };

  // ─── Mix Modu state ─────────────────────────────────────────────────────
  // Sadece audio (gerçek job) modunda anlamlı; mock'ta ctx/jobId yok → buton gizli.
  const mixCtx    = audio && audio.ctx ? audio.ctx : null;
  const mixJobId  = audio && audio.jobId ? audio.jobId : null;
  const mixEnabled = !!(mixCtx && mixJobId);
  const [mixMode, setMixMode]         = useState(false);
  const [mixSelected, setMixSelected] = useState(() => new Set());
  const [mixFmt, setMixFmt]           = useState("mp3");
  const [mixBusy, setMixBusy]         = useState(false);
  const [mixError, setMixError]       = useState(null);
  const [mixNotice, setMixNotice]     = useState(null);

  const onMixToggleSelect = (id) => {
    setMixSelected((prev) => {
      const next = new Set(prev);
      if (next.has(id)) next.delete(id); else next.add(id);
      return next;
    });
    setMixError(null);
  };

  const toggleMixMode = () => {
    setMixMode((m) => {
      const nm = !m;
      if (!nm) { setMixSelected(new Set()); setMixError(null); setMixNotice(null); }
      return nm;
    });
  };

  const runMixAndDownload = async () => {
    if (mixSelected.size < 2) {
      setMixError("En az 2 stem seç");
      return;
    }
    setMixBusy(true);
    setMixError(null);
    setMixNotice(null);
    try {
      const body = new URLSearchParams();
      body.set("stems", Array.from(mixSelected).join(","));
      body.set("fmt", mixFmt);
      const res = await fetch(mixCtx + "/job/" + mixJobId + "/mix", {
        method: "POST",
        credentials: "same-origin",
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
        body: body.toString()
      });
      const data = await res.json();
      if (data.error) { setMixError(data.error); return; }
      // İndirme tetikle — yeni sekme yerine aynı sekmede attachment.
      const a = document.createElement("a");
      a.href = mixCtx + data.downloadUrl;
      a.download = "";
      document.body.appendChild(a);
      a.click();
      a.remove();
      setMixNotice("Mix hazır, indirme başladı.");
    } catch (err) {
      console.error("Mix hatası:", err);
      setMixError("Mix oluşturulamadı");
    } finally {
      setMixBusy(false);
    }
  };

  // size-aware connector drawing
  const canvasRef = useRef(null);
  const [size, setSize] = useState({ w: 1200, h: 700 });
  useEffect(() => {
    const ro = new ResizeObserver(() => {
      const el = canvasRef.current;
      if (!el) return;
      setSize({ w: el.clientWidth, h: el.clientHeight });
    });
    if (canvasRef.current) ro.observe(canvasRef.current);
    return () => ro.disconnect();
  }, []);

  return (
    <div className="app" data-screen-label="01 Splitnorder">
      <div className="mac-chrome">
        <div className="mac-dot r"></div>
        <div className="mac-dot y"></div>
        <div className="mac-dot g"></div>
        <div className="mac-url"></div>
      </div>

      <div className="app-header">
        <div className="app-logo"></div>
        <div className="app-title">Splitnorder</div>
        <button className="btn">
          {I.download()} Download All (ZIP)
        </button>
        <button className="btn btn-icon" title="Share">
          {I.share()}
        </button>
      </div>

      {/* Mix Modu floating panel — .app-header JSP'de gizli olduğu için ayrı overlay.
          Audio yüklendiğinde canvas'ın üstünde sağ-üstte sabit durur. */}
      {mixEnabled && (
        <div className={"mix-fab" + (mixMode ? " open" : "")}>
          <button
            className={"mix-fab-toggle" + (mixMode ? " on" : "")}
            onClick={toggleMixMode}
            title={mixMode ? "Mix modundan çık" : "Mix modunu aç"}>
            <svg viewBox="0 0 24 24" width="18" height="18" fill="none">
              <path d="M4 7h10M4 12h6M4 17h14" stroke="currentColor" strokeWidth="2" strokeLinecap="round" />
              <circle cx="17" cy="7" r="2.2" stroke="currentColor" strokeWidth="2" fill={mixMode ? "currentColor" : "none"} />
              <circle cx="13" cy="12" r="2.2" stroke="currentColor" strokeWidth="2" fill={mixMode ? "currentColor" : "none"} />
              <circle cx="20" cy="17" r="2.2" stroke="currentColor" strokeWidth="2" fill={mixMode ? "currentColor" : "none"} />
            </svg>
            <span>{mixMode ? "Mix Modu Açık" : "Mix Modu"}</span>
          </button>

          {mixMode && (
            <div className="mix-fab-body">
              <div className="mix-fab-hint">
                {mixSelected.size === 0 && "Birleştirmek istediğin stem'leri seç (≥2)"}
                {mixSelected.size === 1 && "En az 1 stem daha seç"}
                {mixSelected.size >= 2 && `${mixSelected.size} stem seçildi — birleştirmeye hazır`}
              </div>
              <div className="mix-fab-row">
                <div className="mix-fmt-group">
                  <button type="button"
                    className={"mix-fmt-btn" + (mixFmt === "mp3" ? " on" : "")}
                    onClick={() => setMixFmt("mp3")}>MP3</button>
                  <button type="button"
                    className={"mix-fmt-btn" + (mixFmt === "wav" ? " on" : "")}
                    onClick={() => setMixFmt("wav")}>WAV</button>
                </div>
                <button
                  className="btn mix-go-btn"
                  disabled={mixBusy || mixSelected.size < 2}
                  onClick={runMixAndDownload}>
                  {I.download()} {mixBusy ? "Hazırlanıyor…" : "Mixle ve İndir"}
                </button>
              </div>
              {(mixError || mixNotice) && (
                <div className={"mix-fab-msg" + (mixError ? " err" : " ok")}>
                  {mixError || mixNotice}
                </div>
              )}
            </div>
          )}
        </div>
      )}

      <div className="canvas" ref={canvasRef}>
        {/* faint logo watermark */}
        <div className="canvas-watermark"></div>

        {/* connectors */}
        <svg className="connectors" viewBox={`0 0 ${size.w} ${size.h}`} preserveAspectRatio="none">
          {stems.map((s) => {
            const a = { x: size.w * center.x, y: size.h * center.y };
            const b = { x: size.w * wireAnchors[s.pos].x, y: size.h * wireAnchors[s.pos].y };
            const d = bezier(a, b, 0.5);
            const audible = !s.muted && (playing || s.playing);
            return (
              <Connector
                key={s.id}
                id={s.id}
                d={d}
                color={s.color}
                active={audible}
                speed={s.playing ? 1.6 : 1} />);


          })}
        </svg>

        {/* stem cards */}
        {stems.map((s) =>
        <StemCard
          key={s.id}
          stem={s}
          style={posMap[s.pos]}
          onToggle={onToggle}
          onVolume={onVolume}
          onPlay={onStemPlay}
          onSeek={onSeek}
          masterPlaying={playing}
          progress={progress}
          mixMode={mixMode}
          mixSelected={mixSelected.has(s.id)}
          onMixToggle={onMixToggleSelect} />

        )}

        {/* preview node */}
        <MusicPreview
          trackName={tweaks.trackName || "Midnight Bloom"}
          artist={tweaks.artist || "Otherwave"}
          duration={duration}
          time={time}
          playing={playing}
          onPlay={onMasterPlay}
          onSeek={onSeek}
          progress={progress} />
        
      </div>

      {/* bottom bar */}
      <div className="bottom-bar">
        <div className="time-readout">
          {fmt(time)} <span className="total">/ {fmt(duration)}</span>
        </div>
        <div className="master-wave">
          <MasterWave stems={stems} progress={progress} masterPlaying={playing} onSeek={onSeek} />
        </div>
        <button className="master-play" onClick={onMasterPlay} title={playing ? "Pause" : "Play all"}>
          {playing ? I.pause() : I.play()}
        </button>
        <div className="master-vol">
          <span className="master-vol-label">Master</span>
          {I.vol("#5A6478")}
          <div className="slider" style={{ flex: 1 }}>
            <div className="slider-track">
              <div className="slider-fill" style={{ width: `${master * 100}%`, background: "#6DA8E8" }} />
            </div>
            <div className="slider-thumb" style={{ left: `${master * 100}%` }} />
            <input type="range" min="0" max="100" value={Math.round(master * 100)} onChange={(e) => setMaster(+e.target.value / 100)} />
          </div>
        </div>
      </div>
    </div>);

}

// ─── center music preview — small card sitting in front of a giant logo ─────
function MusicPreview({ trackName, artist, duration, time, playing, onPlay, onSeek, progress }) {
  const fmt = (s) => {
    const m = Math.floor(s / 60).toString().padStart(2, "0");
    const r = Math.floor(s % 60).toString().padStart(2, "0");
    return `${m}:${r}`;
  };
  return (
    <div className="preview-cluster">
      {/* Giant treble-clef logo standing tall behind the card */}
      <div className={"hero-logo" + (playing ? " on" : "")} aria-hidden="true">
        <div className="hero-halo"></div>
        <div className="hero-halo halo-2"></div>
        <img
          src="assets/logoRB.png"
          alt=""
          className="hero-logo-img"
          draggable="false" />
        
      </div>

      {/* The preview card itself */}
      <div className="preview-node" style={{ backgroundSize: "cover" }}>
        {/* Album art — placeholder cover for the loaded track */}
        <div className="preview-cover" title="Track cover">
          <div className="cover-art"></div>
          <div className="cover-shine"></div>
          <div className={"cover-spin" + (playing ? " on" : "")}></div>
        </div>

        <div className="preview-panel">
          <div className="preview-eyebrow">
            <span className={"eyebrow-dot" + (playing ? " on" : "")}></span>
            Now playing
          </div>
          <div className="preview-title">{trackName}</div>
          <div className="preview-artist">{artist}</div>
          <div className="preview-wave">
            <Waveform
              color="#4A5468"
              seed={101}
              bars={64}
              height={26}
              progress={progress}
              style="other"
              playing={playing}
              onSeek={onSeek} />
            
          </div>
          <div className="preview-time">
            <span>{fmt(time)}</span>
            <span className="dim">{fmt(duration)}</span>
          </div>
        </div>

        <button
          className={"preview-play" + (playing ? " on" : "")}
          onClick={onPlay}
          title={playing ? "Pause" : "Play all"}>
          
          {playing ? I.pause("#fff") : I.play("#fff")}
        </button>
      </div>
    </div>);

}

// ─── bottom master wave: stacked stems ──────────────────────────────────────
function MasterWave({ stems, progress, masterPlaying, onSeek }) {
  return (
    <div style={{ display: "flex", flexDirection: "column", gap: 3, height: "100%", justifyContent: "center" }}>
      {stems.map((s) =>
      <div key={s.id} style={{ height: 16, opacity: s.muted ? 0.22 : 1 }}>
          <Waveform
          color={s.color}
          seed={s.seed + 200}
          bars={130}
          height={16}
          progress={progress}
          style={s.id}
          playing={masterPlaying && !s.muted}
          onSeek={onSeek} />
        
        </div>
      )}
    </div>);

}

// AudioEngine'in tasarımı birebir kullanabilmesi için icon ve seed defaultlarını
// dışa aç. Bu export tasarımı değiştirmez, sadece kayıp veriyi paylaşır.
window.AppScreen = AppScreen;
window.SPLITNORDER_STEM_ICONS = {
  other:  I.note,
  vocals: I.mic,
  bass:   I.bass,
  drums:  I.drum
};
window.SPLITNORDER_STEM_DEFAULTS = [
  { id: "other",  name: "Other",  color: "#2DB3A0", seed: 11, pos: "tl" },
  { id: "vocals", name: "Vocals", color: "#E5444C", seed: 27, pos: "tr" },
  { id: "bass",   name: "Bass",   color: "#5B3A8F", seed: 53, pos: "bl" },
  { id: "drums",  name: "Drums",  color: "#F0A45F", seed: 71, pos: "br" }
];