/* global React, AppScreen */
// Splitnorder Audio Engine — UI çizmez. 4 hidden <audio> elementi + master
// state'i yönetir, AppScreen'i "controlled mode"da render eder. Tasarımın
// orijinal alt master bar + stem kartları (vocals/drums/bass/other) gerçek
// audio'yu kontrol eder.
//
// Context7 + react.dev pattern'leri:
//   - "Lifting state up" — state tek bir yerde (engine), AppScreen prop'tan okur
//   - useRef + useEffect — imperative <audio> API'yi React state'e sync eder
//   - onPlay/onPause/onTimeUpdate — DOM event'leri state'e geri yansır

function AudioEngine(props) {
  const ctx = props.ctx || "";
  const jobId = props.jobId;
  const tweaks = props.tweaks || {};

  const [playing, setPlaying] = React.useState(false);
  const [time, setTime] = React.useState(0);
  const [duration, setDuration] = React.useState(225);
  const [master, setMaster] = React.useState(0.8);

  // Stem state — AppScreen'in beklediği TAM shape (icon ve seed dahil),
  // app.jsx export'undan (window.SPLITNORDER_STEM_DEFAULTS). Bu sayede
  // StemCard waveform'u doğru seed ile çizilir, icon doğru render edilir.
  const [stems, setStems] = React.useState(() => {
    const defaults = window.SPLITNORDER_STEM_DEFAULTS || [];
    const icons    = window.SPLITNORDER_STEM_ICONS || {};
    return defaults.map(d => ({
      id: d.id, name: d.name, color: d.color, pos: d.pos, seed: d.seed,
      icon: icons[d.id] ? icons[d.id]() : null,
      vol: 0.8, solo: false, muted: false, playing: false
    }));
  });

  // 4 audio ref (id → element)
  const refs = React.useRef({});
  const setRef = (id) => (el) => { refs.current[id] = el; };

  // Helper: solo varsa o stem dışındakileri sustur. solo yoksa muted'a göre.
  const isStemAudible = React.useCallback((s, allStems, masterPlaying, anyStemPlaying) => {
    if (s.muted) return false;
    const anySolo = allStems.some(x => x.solo);
    if (anySolo) return s.solo;
    // Tek stem izole çalıyor (s.playing) → diğerleri duyulmaz
    if (anyStemPlaying) return s.playing;
    return masterPlaying;
  }, []);

  // 1) Play/pause sync — master VEYA herhangi bir stem izole çalıyorsa
  React.useEffect(() => {
    const anyStemPlaying = stems.some(s => s.playing);
    const shouldBePlaying = playing || anyStemPlaying;

    stems.forEach(s => {
      const a = refs.current[s.id];
      if (!a) return;
      const audible = isStemAudible(s, stems, playing, anyStemPlaying);

      if (shouldBePlaying && audible) {
        a.play().catch(err => console.warn("audio play reddedildi:", s.id, err));
      } else {
        a.pause();
      }
    });
  }, [playing, stems, isStemAudible]);

  // 2) Volume sync — her audio.volume = stem.vol * masterVolume
  React.useEffect(() => {
    stems.forEach(s => {
      const a = refs.current[s.id];
      if (!a) return;
      a.volume = Math.max(0, Math.min(1, s.vol * master));
    });
  }, [stems, master]);

  // 3) Master ref (vocals) timeupdate/loadedmetadata → state
  React.useEffect(() => {
    const masterAudio = refs.current.vocals;
    if (!masterAudio) return;
    const onTime = () => {
      setTime(masterAudio.currentTime);
      // Drift düzeltme — sadece çalarken
      if (!masterAudio.paused) {
        ["drums", "bass", "other"].forEach(id => {
          const a = refs.current[id];
          if (a && Math.abs(a.currentTime - masterAudio.currentTime) > 0.1) {
            a.currentTime = masterAudio.currentTime;
          }
        });
      }
    };
    const onMeta = () => setDuration(masterAudio.duration || 225);
    const onEnded = () => { setPlaying(false); setStems(arr => arr.map(s => ({...s, playing: false}))); };
    masterAudio.addEventListener("timeupdate", onTime);
    masterAudio.addEventListener("loadedmetadata", onMeta);
    masterAudio.addEventListener("ended", onEnded);
    return () => {
      masterAudio.removeEventListener("timeupdate", onTime);
      masterAudio.removeEventListener("loadedmetadata", onMeta);
      masterAudio.removeEventListener("ended", onEnded);
    };
  }, []);

  // ───── Callback'ler — AppScreen prop interface'i ─────

  // u: 0..1 progress
  const onSeek = React.useCallback((u) => {
    const t = Math.max(0, Math.min(duration, u * duration));
    Object.values(refs.current).forEach(a => { if (a) a.currentTime = t; });
    setTime(t);
  }, [duration]);

  // key: "solo" | "muted"
  const onStemToggle = React.useCallback((id, key) => {
    setStems(arr => arr.map(s => s.id === id ? { ...s, [key]: !s[key] } : s));
  }, []);

  const onStemVolume = React.useCallback((id, v) => {
    setStems(arr => arr.map(s => s.id === id ? { ...s, vol: v } : s));
  }, []);

  // Stem izole çal: bu stem playing=true, diğerleri playing=false, master pause
  const onStemPlay = React.useCallback((id) => {
    setStems(arr => arr.map(s =>
      s.id === id ? { ...s, playing: !s.playing } : { ...s, playing: false }
    ));
    setPlaying(false);
  }, []);

  // Master play: tüm stem playing=false, master toggle
  const onMasterPlay = React.useCallback(() => {
    setStems(arr => arr.map(s => ({ ...s, playing: false })));
    setPlaying(p => !p);
  }, []);

  const audioApi = {
    playing, time, duration, master, stems,
    onSeek, onStemToggle, onStemVolume, onStemPlay, onMasterPlay,
    onMasterVolume: setMaster
  };

  return (
    <React.Fragment>
      {/* Hidden audio elementleri — DOM'da ama görünmüyor */}
      <div style={{ position: "absolute", width: 0, height: 0, overflow: "hidden" }} aria-hidden="true">
        {stems.map(s => (
          <audio
            key={s.id}
            ref={setRef(s.id)}
            src={ctx + "/job/" + jobId + "/stream/" + s.id}
            preload="metadata" />
        ))}
      </div>

      {/* AppScreen orijinal tasarım — controlled mode'da, gerçek audio bağlı */}
      <AppScreen tweaks={tweaks} audio={audioApi} />
    </React.Fragment>
  );
}

window.AudioEngine = AudioEngine;
