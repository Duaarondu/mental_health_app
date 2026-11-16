// The 1-5 mood scale used by check-ins and the journal (stored as JOURNAL_ENTRY.moodScore)
export const MOODS = [
  { score: 1, label: "Very low" },
  { score: 2, label: "Low" },
  { score: 3, label: "Okay" },
  { score: 4, label: "Good" },
  { score: 5, label: "Great" },
];

export const moodLabel = (score) => MOODS.find((m) => m.score === score)?.label ?? "";
