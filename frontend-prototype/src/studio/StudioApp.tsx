import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import {
  ReactFlow,
  ReactFlowProvider,
  Background,
  BackgroundVariant,
  type Node,
  type Edge,
  type NodeTypes,
  type EdgeTypes,
} from "@xyflow/react";
import "@xyflow/react/dist/style.css";
import { Download, Share2 } from "lucide-react";

import { StemNode } from "./StemNode";
import { InputNode } from "./InputNode";
import { StemEdge } from "./StemEdge";
import { MasterPlayer } from "./MasterPlayer";
import { Logo } from "./Logo";
import { mockConfig } from "./mockData";
import type { StemKey } from "./types";

const nodeTypes: NodeTypes = {
  input: InputNode,
  stem: StemNode,
};
const edgeTypes: EdgeTypes = {
  stem: StemEdge,
};

const NODE_POSITIONS: Record<string, { x: number; y: number }> = {
  input: { x: 360, y: 220 },
  vocals: { x: 700, y: 60 },
  drums: { x: 700, y: 380 },
  bass: { x: 20, y: 380 },
  other: { x: 20, y: 60 },
};

export default function StudioApp() {
  const [playing, setPlaying] = useState(false);
  const [progress, setProgress] = useState(0);
  const [masterVolume, setMasterVolume] = useState(0.85);
  const [solo, setSolo] = useState<StemKey | null>(null);
  const [muted, setMuted] = useState<Record<StemKey, boolean>>({
    vocals: false,
    drums: false,
    bass: false,
    other: false,
  });
  const [volumes, setVolumes] = useState<Record<StemKey, number>>({
    vocals: 0.8,
    drums: 0.8,
    bass: 0.8,
    other: 0.8,
  });

  // Mock playback simulation (no audio yet — Faz 4'te WaveSurfer'a bağlanacak)
  const duration = 225; // 03:45
  const rafRef = useRef<number | null>(null);
  const lastTickRef = useRef<number>(0);

  useEffect(() => {
    if (!playing) {
      if (rafRef.current) cancelAnimationFrame(rafRef.current);
      return;
    }
    lastTickRef.current = performance.now();
    const tick = (t: number) => {
      const dt = (t - lastTickRef.current) / 1000;
      lastTickRef.current = t;
      setProgress((p) => {
        const np = p + dt / duration;
        if (np >= 1) {
          setPlaying(false);
          return 0;
        }
        return np;
      });
      rafRef.current = requestAnimationFrame(tick);
    };
    rafRef.current = requestAnimationFrame(tick);
    return () => {
      if (rafRef.current) cancelAnimationFrame(rafRef.current);
    };
  }, [playing]);

  const togglePlay = useCallback(() => setPlaying((p) => !p), []);
  const seek = useCallback((p: number) => setProgress(p), []);

  const onSolo = useCallback((k: StemKey) => {
    setSolo((s) => (s === k ? null : k));
  }, []);
  const onMute = useCallback((k: StemKey) => {
    setMuted((m) => ({ ...m, [k]: !m[k] }));
  }, []);
  const onVolume = useCallback((k: StemKey, v: number) => {
    setVolumes((vs) => ({ ...vs, [k]: v }));
  }, []);
  const onDownload = useCallback((k: StemKey) => {
    console.info("[mock] download", k);
  }, []);
  const onDownloadAll = useCallback(() => {
    console.info("[mock] download all (zip)");
  }, []);
  const onShare = useCallback(() => {
    if (navigator.share) {
      navigator.share({ title: "Splitnorder", url: location.href }).catch(() => {});
    } else {
      navigator.clipboard?.writeText(location.href);
    }
  }, []);

  const nodes: Node[] = useMemo(
    () => [
      {
        id: "input",
        type: "input",
        position: NODE_POSITIONS.input,
        data: {
          playing,
          progress,
          onTogglePlay: togglePlay,
        },
        draggable: false,
        selectable: false,
      },
      ...mockConfig.stems.map<Node>((s) => ({
        id: s.key,
        type: "stem",
        position: NODE_POSITIONS[s.key],
        data: {
          stemKey: s.key,
          label: s.label,
          color: s.color,
          soft: s.soft,
          solo: solo === s.key,
          muted: muted[s.key] || (solo !== null && solo !== s.key),
          progress,
          volume: volumes[s.key],
          onSolo,
          onMute,
          onVolume,
          onDownload,
        },
        draggable: false,
        selectable: false,
      })),
    ],
    [
      playing,
      progress,
      solo,
      muted,
      volumes,
      togglePlay,
      onSolo,
      onMute,
      onVolume,
      onDownload,
    ]
  );

  const edges: Edge[] = useMemo(
    () =>
      mockConfig.stems.map((s) => ({
        id: `e-${s.key}`,
        source: "input",
        target: s.key,
        type: "stem",
        data: { color: s.color },
      })),
    []
  );

  const current = progress * duration;

  return (
    <div className="h-screen w-screen flex flex-col">
      {/* Top bar */}
      <header className="flex items-center justify-between px-6 py-4 shrink-0">
        <div className="flex items-center gap-2.5">
          <Logo size={28} />
          <span className="font-display font-semibold text-[18px] text-ink tracking-tight">
            Splitnorder
          </span>
        </div>
        <div className="flex items-center gap-2">
          <button
            onClick={onDownloadAll}
            className="inline-flex items-center gap-2 px-3 py-2 rounded-lg
                       bg-white border border-canvas-line/80 hover:bg-canvas-soft
                       text-[13px] font-medium text-ink-soft transition shadow-card"
          >
            <Download size={14} />
            Download All (ZIP)
          </button>
          <button
            onClick={onShare}
            className="w-9 h-9 rounded-lg bg-white border border-canvas-line/80
                       hover:bg-canvas-soft text-ink-soft flex items-center justify-center transition shadow-card"
            title="Share"
          >
            <Share2 size={14} />
          </button>
        </div>
      </header>

      {/* Node graph */}
      <main className="flex-1 relative min-h-0">
        <ReactFlowProvider>
          <ReactFlow
            nodes={nodes}
            edges={edges}
            nodeTypes={nodeTypes}
            edgeTypes={edgeTypes}
            fitView
            fitViewOptions={{ padding: 0.18 }}
            proOptions={{ hideAttribution: true }}
            nodesDraggable={false}
            nodesConnectable={false}
            elementsSelectable={false}
            zoomOnScroll={false}
            zoomOnPinch={false}
            zoomOnDoubleClick={false}
            panOnDrag={false}
            panOnScroll={false}
            preventScrolling={false}
          >
            <Background
              variant={BackgroundVariant.Dots}
              gap={24}
              size={1}
              color="#D5DEE8"
            />
          </ReactFlow>
        </ReactFlowProvider>
      </main>

      {/* Master player */}
      <footer className="px-6 pb-5 pt-2 shrink-0">
        <MasterPlayer
          playing={playing}
          onTogglePlay={togglePlay}
          progress={progress}
          duration={duration}
          current={current}
          masterVolume={masterVolume}
          onMasterVolume={setMasterVolume}
          onSeek={seek}
        />
      </footer>
    </div>
  );
}
