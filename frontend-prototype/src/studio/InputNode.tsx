import { Handle, Position, type NodeProps } from "@xyflow/react";
import { Pause, Play, Music2 } from "lucide-react";
import { MiniWaveform } from "./MiniWaveform";
import { STEM_PEAKS } from "./mockData";

export interface InputNodeData {
  playing: boolean;
  progress: number;
  onTogglePlay: () => void;
}

export function InputNode({ data }: NodeProps) {
  const d = data as unknown as InputNodeData;
  return (
    <div
      className="bg-white rounded-2xl px-4 py-3 w-[220px] flex items-center gap-3"
      style={{
        boxShadow:
          "0 2px 4px rgba(31,34,48,.05), 0 18px 40px rgba(31,34,48,.10)",
        border: "1px solid rgba(31,34,48,0.06)",
      }}
    >
      <Handle type="target" position={Position.Left} />
      <Handle type="source" position={Position.Right} />

      <div className="w-9 h-9 rounded-xl bg-canvas-soft border border-canvas-line/70 flex items-center justify-center text-ink-soft shrink-0">
        <Music2 size={16} />
      </div>

      <div className="flex-1 min-w-0">
        <div className="text-[12px] font-semibold text-ink leading-tight mb-1 truncate">
          input node
        </div>
        <MiniWaveform
          peaks={STEM_PEAKS.input}
          color="#8A8FA3"
          height={20}
          progress={d.progress}
        />
      </div>

      <button
        onClick={d.onTogglePlay}
        className="w-9 h-9 rounded-full bg-ink text-white flex items-center justify-center
                   hover:bg-ink-soft transition shrink-0"
        title={d.playing ? "Pause" : "Play"}
      >
        {d.playing ? <Pause size={14} fill="white" /> : <Play size={14} fill="white" className="ml-0.5" />}
      </button>
    </div>
  );
}
