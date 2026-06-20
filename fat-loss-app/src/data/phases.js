export const PHASE_META = [
  {
    label: "Phase 1", weeks: "Weeks 1-4", title: "Foundation",
    rpe: "RPE 7-8", accent: "#e94560",
    rest: "60 sec all sets", tempo: "3-1-1",
    keyFocus: "Straight sets. Learn the movements. Nail the tempo."
  },
  {
    label: "Phase 2", weeks: "Weeks 5-8", title: "Intensity",
    rpe: "RPE 8-9", accent: "#4a9eff",
    rest: "30-45 sec within supersets", tempo: "3-1-1",
    keyFocus: "All supersets active. Tighter rest. Add load vs Phase 1."
  },
  {
    label: "Phase 3", weeks: "Weeks 9-12", title: "Peak",
    rpe: "RPE 9-10", accent: "#22c55e",
    rest: "30 sec within supersets", tempo: "3-1-1",
    keyFocus: "Final compound set to 1-2 reps short of failure. Finisher circuit after Day A and C."
  }
];

export function getPhaseIndex(week) {
  if (week <= 4) return 0;
  if (week <= 8) return 1;
  return 2;
}
