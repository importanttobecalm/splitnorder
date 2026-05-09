interface Props {
  peaks: number[];
  color: string;
  height?: number;
  progress?: number; // 0..1, optional playhead
  className?: string;
}

export function MiniWaveform({
  peaks,
  color,
  height = 36,
  progress = 0,
  className,
}: Props) {
  const barW = 2;
  const gap = 2;
  const total = peaks.length * (barW + gap);
  const playedBars = Math.floor(peaks.length * progress);

  return (
    <svg
      viewBox={`0 0 ${total} ${height}`}
      preserveAspectRatio="none"
      width="100%"
      height={height}
      className={className}
      aria-hidden
    >
      {peaks.map((p, i) => {
        const h = Math.max(2, p * height);
        const y = (height - h) / 2;
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
            fill={color}
            opacity={played ? 1 : 0.45}
          />
        );
      })}
    </svg>
  );
}
