import { Handle, Position, type NodeProps } from "@xyflow/react";
import { Download, Drum, Mic, Music } from "lucide-react";
import { MiniWaveform } from "./MiniWaveform";
import { STEM_PEAKS } from "./mockData";
import type { StemKey } from "./types";

export interface StemNodeData {
  stemKey: StemKey;
  label: string;
  color: string;
  soft: string;
  solo: boolean;
  muted: boolean;
  progress: number;
  volume: number;
  onSolo: (k: StemKey) => void;
  onMute: (k: StemKey) => void;
  onVolume: (k: StemKey, v: number) => void;
  onDownload: (k: StemKey) => void;
}

function StemIcon({ k }: { k: StemKey }) {
  switch (k) {
    case "vocals":
      return <Mic size={22} strokeWidth={2.2} />;
    case "drums":
      return <Drum size={22} strokeWidth={2.2} />;
    case "bass":
      // Lucide has no bass clef; use Unicode glyph styled as an icon.
      return (
        <span
          aria-hidden
          className="block leading-none font-serif"
          style={{ fontSize: 28, marginTop: -2 }}
        >
          𝄢
        </span>
      );
    case "other":
      return <Music size={22} strokeWidth={2.2} />;
  }
}

export function StemNode({ data }: NodeProps) {
  const d = data as unknown as StemNodeData;
  const peaks = STEM_PEAKS[d.stemKey];

  return (
    <div
      className="bg-white rounded-2xl px-4 py-3 w-[260px] select-none transition"
      style={{
        border: `2px solid ${d.solo ? d.color : "rgba(31,34,48,0.06)"}`,
        boxShadow: d.solo
          ? `0 0 0 4px ${d.color}22, 0 12px 32px rgba(31,34,48,.10)`
          : "0 1px 2px rgba(31,34,48,.04), 0 12px 28px rgba(31,34,48,.08)",
        background: d.solo ? d.soft : "#FFFFFF",
        opacity: d.muted ? 0.55 : 1,
      }}
    >
      <Handle type="target" position={Position.Left} />
      <Handle type="source" position={Position.Right} />

      {/* Header: icon box + label */}
      <div className="flex items-center gap-2.5 mb-3">
        <div
          className="w-10 h-10 rounded-[10px] flex items-center justify-center text-white shrink-0"
          style={{ backgroundColor: d.color }}
        >
          <StemIcon k={d.stemKey} />
        </div>
        <span
          className="text-[14px] font-bold tracking-wider text-ink"
        >
          {d.label}
        </span>
      </div>

      {/* Mini waveform */}
      <div className="mb-3">
        <MiniWaveform peaks={peaks} color={d.color} height={28} progress={d.progress} />
      </div>

      {/* Controls row: S | M | volume slider | download */}
      <div className="flex items-center gap-2">
        <button
          onClick={() => d.onSolo(d.stemKey)}
          aria-pressed={d.solo}
          className="w-7 h-7 rounded-md text-[11px] font-bold transition shrink-0"
          style={
            d.solo
              ? {
                  backgroundColor: d.soft,
                  border: `2px solid ${d.color}`,
                  color: d.color,
                }
              : {
                  backgroundColor: "#F4F8FB",
                  color: "#4A4F62",
                }
          }
          title="Solo"
        >
          S
        </button>
        <button
          onClick={() => d.onMute(d.stemKey)}
          aria-pressed={d.muted && !d.solo}
          className="w-7 h-7 rounded-md text-[11px] font-bold transition shrink-0"
          style={
            d.muted && !d.solo
              ? { backgroundColor: "#8A8FA3", color: "#FFFFFF" }
              : { backgroundColor: "#F4F8FB", color: "#4A4F62" }
          }
          title="Mute"
        >
          M
        </button>

        <input
          type="range"
          min={0}
          max={100}
          value={Math.round(d.volume * 100)}
          onChange={(e) => d.onVolume(d.stemKey, Number(e.target.value) / 100)}
          className="stem-volume flex-1 min-w-0"
          style={
            {
              ["--stem-color" as string]: d.color,
              ["--stem-pct" as string]: `${Math.round(d.volume * 100)}%`,
            } as React.CSSProperties
          }
          aria-label={`${d.label} volume`}
        />

        <button
          onClick={() => d.onDownload(d.stemKey)}
          className="w-7 h-7 rounded-md bg-canvas-soft text-ink-soft hover:bg-canvas-line
                     flex items-center justify-center transition shrink-0"
          title="Download"
        >
          <Download size={14} />
        </button>
      </div>
    </div>
  );
}
