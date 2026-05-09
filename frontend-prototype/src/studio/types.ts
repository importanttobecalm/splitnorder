export type StemKey = "vocals" | "drums" | "bass" | "other";

export interface StemDef {
  key: StemKey;
  label: string;
  color: string;
  soft: string;
  url: string;
}

export interface StudioConfig {
  jobId: string;
  fileName: string;
  inputUrl: string;
  stems: StemDef[];
}
