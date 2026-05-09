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

const TRACK_HEIGHT = 11; // px per stem row
const BAR_W = 2;
const BAR_GAP = 2;

interface StackProps {
  half: "left" | "right";
  progress: number; // 0..1 global
  onSeek: (globalP: number) => void;
}

/**
 * Renders a 4-track stacked waveform for one half of the master player.
 * `progress` is global 0..1; left half shows 0..0.5 mapped to 0..1,
 * right half shows 0.5..1 mapped to 0..1.
 */
function StemWaveStack({ half, progress, onSeek }: StackProps) {
  const localProgress =
    half === "left"
      ? Math.max(0, Math.min(1, progress * 2))
      : Math.max(0, Math.min(1, (progress - 0.5) * 2));

  const handleClick = (e: React.MouseEvent<HTMLDivElement>) => {
    const rect = e.currentTarget.getBoundingClientRect();
    const localP = (e.clientX - rect.left) / rect.width;
    const clamped = Math.max(0, Math.min(1, localP));
    const global = half === "left" ? clamped * 0.5 : 0.5 + clamped * 0.5;
    onSeek(global);
  };

  return (
    <div
      className="flex-1 min-w-0 flex flex-col gap-[2px] cursor-pointer relative"
      onClick={handleClick}
    >
      {mockConfig.stems.map((s) => {
        const fullPeaks = STEM_PEAKS[s.key];
        const peaks =
          half === "left"
            ? fullPeaks.slice(0, Math.floor(fullPeaks.length / 2))
            : fullPeaks.slice(Math.floor(fullPeaks.length / 2));
        const totalW = peaks.length * (BAR_W + BAR_GAP);
        const playedBars = Math.floor(peaks.length * localProgress);
        return (
          <svg
            key={s.key}
            viewBox={`0 0 ${totalW} ${TRACK_HEIGHT}`}
            preserveAspectRatio="none"
            width="100%"
            height={TRACK_HEIGHT}
            aria-hidden
          >
            {peaks.map((p, i) => {
              const h = Math.max(2, p * TRACK_HEIGHT);
              const y = (TRACK_HEIGHT - h) / 2;
              const x = i * (BAR_W + BAR_GAP);
              const played = i < playedBars;
              return (
                <rect
                  key={i}
                  x={x}
                  y={y}
                  width={BAR_W}
                  height={h}
                  rx={1}
                  fill={s.color}
                  opacity={played ? 0.95 : 0.32}
                />
              );
            })}
          </svg>
        );
      })}
      {/* Local playhead line */}
      {localProgress > 0 && localProgress < 1 && (
        <div
          className="absolute top-0 bottom-0 w-px bg-ink/40 pointer-events-none"
          style={{ left: `${localProgress * 100}%` }}
        />
      )}
    </div>
  );
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
  return (
    <div
      className="bg-white/95 backdrop-blur-sm border border-canvas-line rounded-2xl
                 px-5 py-3 flex items-center gap-4"
      style={{
        boxShadow: "0 1px 2px rgba(31,34,48,.04), 0 12px 28px rgba(31,34,48,.06)",
      }}
    >
      {/* Timestamp */}
      <div className="font-mono text-[12px] text-ink-soft tabular-nums shrink-0">
        {formatTime(current)} / {formatTime(duration)}
      </div>

      {/* Left stem-stack waveform (first half) */}
      <StemWaveStack half="left" progress={progress} onSeek={onSeek} />

      {/* Center play button */}
      <button
        onClick={onTogglePlay}
        className="w-14 h-14 rounded-full text-white flex items-center justify-center
                   transition shrink-0"
        style={{
          backgroundColor: "#4A90E2",
          boxShadow:
            "0 4px 12px rgba(74,144,226,0.35), 0 1px 2px rgba(31,34,48,.1)",
        }}
        title={playing ? "Pause" : "Play"}
      >
        {playing ? (
          <Pause size={20} fill="white" />
        ) : (
          <Play size={20} fill="white" className="ml-0.5" />
        )}
      </button>

      {/* Right stem-stack waveform (second half) */}
      <StemWaveStack half="right" progress={progress} onSeek={onSeek} />

      {/* Master volume */}
      <div className="flex items-center gap-2 shrink-0">
        <span className="text-[11px] text-ink-soft hidden sm:inline">
          Master volume
        </span>
        <Volume2 size={16} className="text-ink-soft" />
        <input
          type="range"
          min={0}
          max={100}
          value={Math.round(masterVolume * 100)}
          onChange={(e) => onMasterVolume(Number(e.target.value) / 100)}
          className="stem-volume w-24"
          style={
            {
              ["--stem-color" as string]: "#4A90E2",
              ["--stem-pct" as string]: `${Math.round(masterVolume * 100)}%`,
            } as React.CSSProperties
          }
          aria-label="Master volume"
        />
      </div>
    </div>
  );
}

