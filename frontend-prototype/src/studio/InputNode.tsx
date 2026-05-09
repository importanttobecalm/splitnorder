import { Handle, Position, type NodeProps } from "@xyflow/react";
import { Pause, Play, Music2 } from "lucide-react";
import { MiniWaveform } from "./MiniWaveform";
import { STEM_PEAKS } from "./mockData";

export interface InputNodeData {
  fileName: string;
  duration?: string;
  playing: boolean;
  progress: number;
  onTogglePlay: () => void;
}

// "After Midnight - The Midnight Drive.mp3" → { artist, title }
function parseTrack(filename: string): { artist: string | null; title: string } {
  const stripped = filename.replace(/\.[a-z0-9]+$/i, "");
  const idx = stripped.indexOf(" - ");
  if (idx > 0 && idx < stripped.length - 3) {
    return {
      artist: stripped.slice(0, idx).trim(),
      title: stripped.slice(idx + 3).trim(),
    };
  }
  return { artist: null, title: stripped };
}

export function InputNode({ data }: NodeProps) {
  const d = data as unknown as InputNodeData;
  const { artist, title } = parseTrack(d.fileName);
  const duration = d.duration ?? "03:45";

  return (
    <div
      className="bg-white flex items-center w-[400px]"
      style={{
        padding: "14px",
        gap: "14px",
        borderRadius: "26px 22px 28px 24px",
        boxShadow:
          "0 2px 4px rgba(31,34,48,.05), 0 18px 40px rgba(31,34,48,.10)",
        border: "1px solid rgba(31,34,48,0.06)",
      }}
    >
      <Handle type="target" position={Position.Left} />
      <Handle type="source" position={Position.Right} />

      {/* Album art — 4-color gradient hinting at the 4 stems below */}
      <div
        className="w-14 h-14 shrink-0 flex items-center justify-center text-white relative overflow-hidden"
        style={{
          borderRadius: "16px 14px 16px 14px",
          background:
            "linear-gradient(135deg, #E8554E 0%, #F2A35E 35%, #6B5B95 70%, #3FB8AF 100%)",
          boxShadow: "inset 0 1px 0 rgba(255,255,255,0.25)",
        }}
        aria-hidden
      >
        <Music2 size={22} strokeWidth={2} />
      </div>

      {/* Title block */}
      <div className="flex-1 min-w-0 flex flex-col gap-1">
        <div className="flex items-baseline gap-2 min-w-0">
          <div
            className="text-[14px] font-semibold text-ink leading-tight truncate"
            title={title}
          >
            {title}
          </div>
        </div>
        <div className="text-[11px] text-ink-mute leading-tight truncate">
          {artist ? `${artist} · ${duration}` : `Source track · ${duration}`}
        </div>
        <MiniWaveform
          peaks={STEM_PEAKS.input}
          color="#A8B0BD"
          height={16}
          progress={d.progress}
          className="mt-0.5"
        />
      </div>

      {/* Play */}
      <button
        onClick={d.onTogglePlay}
        className="w-10 h-10 rounded-full bg-ink text-white flex items-center justify-center
                   hover:bg-ink-soft transition shrink-0"
        title={d.playing ? "Pause" : "Play"}
      >
        {d.playing ? (
          <Pause size={14} fill="white" />
        ) : (
          <Play size={14} fill="white" className="ml-0.5" />
        )}
      </button>
    </div>
  );
}
