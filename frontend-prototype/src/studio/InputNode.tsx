import { Handle, Position, type NodeProps } from "@xyflow/react";
import { Pause, Play, Music2 } from "lucide-react";
import { MiniWaveform } from "./MiniWaveform";
import { STEM_PEAKS } from "./mockData";

export interface InputNodeData {
  fileName: string;
  playing: boolean;
  progress: number;
  onTogglePlay: () => void;
}

export function InputNode({ data }: NodeProps) {
  const d = data as unknown as InputNodeData;
  return (
    <div
      className="bg-white flex items-center w-[420px]"
      style={{
        padding: "20px",
        gap: "16px",
        borderRadius: "30px 26px 32px 28px",
        boxShadow:
          "0 2px 4px rgba(31,34,48,.05), 0 18px 40px rgba(31,34,48,.10)",
        border: "1px solid rgba(31,34,48,0.06)",
      }}
    >
      <Handle type="target" position={Position.Left} />
      <Handle type="source" position={Position.Right} />

      {/* Album art */}
      <div
        className="w-11 h-11 bg-canvas-soft border border-canvas-line flex items-center justify-center text-ink-soft shrink-0"
        style={{ borderRadius: "14px 12px 14px 12px" }}
      >
        <Music2 size={18} />
      </div>

      {/* Title + waveform */}
      <div className="flex-1 min-w-0 flex flex-col gap-2">
        <div
          className="text-[13px] font-semibold text-ink leading-tight truncate"
          title={d.fileName}
        >
          {d.fileName}
        </div>
        <MiniWaveform
          peaks={STEM_PEAKS.input}
          color="#8A8FA3"
          height={20}
          progress={d.progress}
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
