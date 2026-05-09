import type { StudioConfig } from "./types";

// Generate stable pseudo-random peaks so SSR/CSR match and waveforms look consistent.
function generatePeaks(seed: number, n = 96): number[] {
  const peaks: number[] = [];
  let s = seed;
  for (let i = 0; i < n; i++) {
    s = (s * 9301 + 49297) % 233280;
    const r = s / 233280;
    const env = Math.sin((i / n) * Math.PI); // soft envelope
    peaks.push(Math.max(0.08, env * (0.35 + r * 0.65)));
  }
  return peaks;
}

export const STEM_PEAKS: Record<string, number[]> = {
  vocals: generatePeaks(11),
  drums: generatePeaks(23),
  bass: generatePeaks(37),
  other: generatePeaks(53),
  input: generatePeaks(7, 64),
};

export const mockConfig: StudioConfig = {
  jobId: "demo-001",
  fileName: "input_song.mp3",
  inputUrl: "",
  stems: [
    { key: "vocals", label: "VOCALS", color: "#E8554E", soft: "#FDECEA", url: "" },
    { key: "drums", label: "DRUMS", color: "#F2A35E", soft: "#FEF3E6", url: "" },
    { key: "bass", label: "BASS", color: "#6B5B95", soft: "#EDE9F5", url: "" },
    { key: "other", label: "OTHER", color: "#3FB8AF", soft: "#E4F5F3", url: "" },
  ],
};
