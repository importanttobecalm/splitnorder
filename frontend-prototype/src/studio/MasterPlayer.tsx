import { Pause, Play, Volume2 } from "lucide-react";
import { mockConfig, STEM_PEAKS } from "./mockData";

interface Props {
  playing: boolean;
  onTogglePlay: () => void;
  progress: number;
  duration: number;
  current: number;
  masterVolume: number;
  onMasterVolume: (v: number) => void;
  onSeek: (p: number) => void;
}

function formatTime(s: number): string {
  if (!isFinite(s) || s < 0) s = 0;
  const m = Math.floor(s / 60);
  const sec = Math.floor(s % 60);
  return `${m.toString().padStart(2, "0")}:${sec.toString().padStart(2, "0")}`;
}

export function MasterPlayer({
  playing,
  onTogglePlay,
  progress,
  duration,
  current,
  masterVolume,
  onMasterVolume,
  onSeek,
}: Props) {
  const handleClick = (e: React.MouseEvent<HTMLDivElement>) => {
    const rect = e.currentTarget.getBoundingClientRect();
    const p = (e.clientX - rect.left) / rect.width;
    onSeek(Math.max(0, Math.min(1, p)));
  };

  const barW = 2;
  const gap = 2;
  const peakLen = STEM_PEAKS.vocals.length;
  const totalW = peakLen * (barW + gap);
  const playedBars = Math.floor(peakLen * progress);

  return (
    <div className="bg-white/95 backdrop-blur-sm border border-canvas-line/70 rounded-2xl shadow-card-lg
                    px-5 py-3 flex items-center gap-4">
      {/* timestamp */}
      <div className="font-mono text-[12px] text-ink-soft tabular-nums shrink-0">
        {formatTime(current)} / {formatTime(duration)}
      </div>

      {/* combined waveform */}
      <div
        className="flex-1 h-12 cursor-pointer relative overflow-hidden"
        onClick={handleClick}
      >
        <svg
          viewBox={`0 0 ${totalW} 48`}
          preserveAspectRatio="none"
          width="100%"
          height="48"
        >
          {mockConfig.stems.map((s, layerIdx) => {
            const peaks = STEM_PEAKS[s.key];
            return (
              <g key={s.key} opacity={0.85}>
                {peaks.map((p, i) => {
                  const offset = layerIdx * 0.5; // tiny vertical offset to layer colors
                  const h = Math.max(2, p * 36);
                  const y = 24 - h / 2 + offset;
                  const x = i * (barW + gap);
                  const played = i < playedBars;
                  return (
                    <rect
                      key={i}
                      x={x}
                      y={y}
                      width={barW}
                      height={h}
                      rx={1}
                      fill={s.color}
                      opacity={played ? 0.9 : 0.35}
                    />
                  );
                })}
              </g>
            );
          })}
        </svg>
        {/* playhead line */}
        <div
          className="absolute top-0 bottom-0 w-px bg-ink/40 pointer-events-none"
          style={{ left: `${progress * 100}%` }}
        />
      </div>

      {/* play/pause */}
      <button
        onClick={onTogglePlay}
        className="w-11 h-11 rounded-full bg-ink text-white flex items-center justify-center
                   hover:bg-ink-soft transition shrink-0"
        title={playing ? "Pause" : "Play"}
      >
        {playing ? (
          <Pause size={16} fill="white" />
        ) : (
          <Play size={16} fill="white" className="ml-0.5" />
        )}
      </button>

      {/* master volume */}
      <div className="flex items-center gap-2 shrink-0">
        <span className="text-[11px] text-ink-soft hidden sm:inline">Master volume</span>
        <Volume2 size={16} className="text-ink-soft" />
        <input
          type="range"
          min={0}
          max={100}
          value={Math.round(masterVolume * 100)}
          onChange={(e) => onMasterVolume(Number(e.target.value) / 100)}
          className="w-24 accent-ink"
          aria-label="Master volume"
        />
      </div>
    </div>
  );
}
