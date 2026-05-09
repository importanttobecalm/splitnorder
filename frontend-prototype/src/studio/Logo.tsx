// 4-color treble clef logo — vertical 4-band split echoing the 4 stems.
// Simplified vector approximation of the user's logo.jpeg.
export function Logo({ size = 28 }: { size?: number }) {
  const s = size;
  return (
    <svg
      width={s}
      height={s}
      viewBox="0 0 64 64"
      fill="none"
      aria-label="Splitnorder logo"
    >
      <defs>
        <clipPath id="clef-clip">
          <path d="M32 6
                   c 4 0 8 4 8 10
                   c 0 6 -4 9 -8 11
                   c -6 3 -10 8 -10 14
                   c 0 7 5 12 12 12
                   c 6 0 10 -4 10 -9
                   c 0 -4 -3 -7 -7 -7
                   c -3 0 -6 2 -6 5
                   c 0 2 1 3 3 3
                   M30 22
                   c 0 8 1 16 2 24
                   c 1 6 0 10 -4 12
                   c -3 1 -6 -1 -6 -4
                   c 0 -2 1 -3 3 -3" />
        </clipPath>
      </defs>
      <g clipPath="url(#clef-clip)">
        <rect x="0" y="0" width="64" height="16" fill="#E8554E" />
        <rect x="0" y="16" width="64" height="16" fill="#F2A35E" />
        <rect x="0" y="32" width="64" height="16" fill="#6B5B95" />
        <rect x="0" y="48" width="64" height="16" fill="#3FB8AF" />
      </g>
      {/* Soft outline so clef stays readable on light bg */}
      <path
        d="M32 6 c 4 0 8 4 8 10 c 0 6 -4 9 -8 11
           c -6 3 -10 8 -10 14 c 0 7 5 12 12 12
           c 6 0 10 -4 10 -9 c 0 -4 -3 -7 -7 -7
           c -3 0 -6 2 -6 5 c 0 2 1 3 3 3
           M30 22 c 0 8 1 16 2 24 c 1 6 0 10 -4 12
           c -3 1 -6 -1 -6 -4 c 0 -2 1 -3 3 -3"
        stroke="rgba(31,34,48,0.15)"
        strokeWidth="0.6"
        fill="none"
      />
    </svg>
  );
}
