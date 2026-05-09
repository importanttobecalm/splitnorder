import {
  BaseEdge,
  getBezierPath,
  Position,
  useInternalNode,
  type EdgeProps,
} from "@xyflow/react";

interface StemEdgeData {
  color?: string;
}

// Pick which side of `from` is closest to `to` (left | right) so the bezier
// curve always connects on the inward-facing edge of each card.
function pickHorizontalSide(
  from: { x: number; y: number; w: number },
  to: { x: number; y: number }
): { pos: Position; x: number; y: number } {
  const fromCenterX = from.x + from.w / 2;
  if (to.x > fromCenterX) {
    return { pos: Position.Right, x: from.x + from.w, y: from.y };
  }
  return { pos: Position.Left, x: from.x, y: from.y };
}

export function StemEdge(props: EdgeProps) {
  const { id, source, target, data } = props;
  const sourceNode = useInternalNode(source);
  const targetNode = useInternalNode(target);
  const d = (data ?? {}) as unknown as StemEdgeData;
  const color = d.color ?? "#8A8FA3";

  if (!sourceNode || !targetNode) return null;

  const sw = sourceNode.measured?.width ?? (sourceNode.width ?? 220);
  const sh = sourceNode.measured?.height ?? (sourceNode.height ?? 60);
  const tw = targetNode.measured?.width ?? (targetNode.width ?? 230);
  const th = targetNode.measured?.height ?? (targetNode.height ?? 130);

  const sCenter = {
    x: sourceNode.position.x,
    y: sourceNode.position.y + sh / 2,
    w: sw,
  };
  const tCenter = {
    x: targetNode.position.x,
    y: targetNode.position.y + th / 2,
    w: tw,
  };

  const s = pickHorizontalSide(sCenter, {
    x: targetNode.position.x + tw / 2,
    y: tCenter.y,
  });
  const t = pickHorizontalSide(tCenter, {
    x: sourceNode.position.x + sw / 2,
    y: sCenter.y,
  });

  const [path] = getBezierPath({
    sourceX: s.x,
    sourceY: s.y,
    sourcePosition: s.pos,
    targetX: t.x,
    targetY: t.y,
    targetPosition: t.pos,
    curvature: 0.55,
  });

  const gradId = `edge-grad-${id}`;
  const filterId = `edge-glow-${id}`;

  return (
    <>
      <defs>
        <linearGradient
          id={gradId}
          gradientUnits="userSpaceOnUse"
          x1={s.x}
          y1={s.y}
          x2={t.x}
          y2={t.y}
        >
          <stop offset="0%" stopColor="#A8B0BD" />
          <stop offset="100%" stopColor={color} />
        </linearGradient>
        <filter id={filterId} x="-20%" y="-20%" width="140%" height="140%">
          <feGaussianBlur stdDeviation="3" />
        </filter>
      </defs>
      {/* Glow halo */}
      <BaseEdge
        id={`${id}-halo`}
        path={path}
        style={{
          stroke: color,
          strokeOpacity: 0.3,
          strokeWidth: 10,
          fill: "none",
          pointerEvents: "none",
          filter: `url(#${filterId})`,
        }}
      />
      {/* Main gradient stroke */}
      <BaseEdge
        id={id}
        path={path}
        style={{
          stroke: `url(#${gradId})`,
          strokeWidth: 5,
          strokeLinecap: "round",
          fill: "none",
          pointerEvents: "none",
        }}
      />
    </>
  );
}
