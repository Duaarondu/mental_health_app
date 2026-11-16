import { useState } from "react";
import { addJournalEntry } from "../api.js";
import { formatTime, isToday } from "../format.js";
import { MOODS, moodLabel } from "../moods.js";
import { MoodFace } from "./Icons.jsx";

// "How are you feeling right now?" One tap saves a mood check-in (a journal entry with no text).
export default function MoodCheckIn({ entries, onSaved, onWrite, onCrisis }) {
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  const [again, setAgain] = useState(false);

  const latestToday = entries.find((e) => isToday(e.entryDate));
  const showPicker = !latestToday || again;

  async function checkIn(score) {
    setSaving(true);
    setError("");
    try {
      await addJournalEntry(score);
      setAgain(false);
      onSaved();
    } catch (err) {
      setError(err.message);
    } finally {
      setSaving(false);
    }
  }

  return (
    <section className="card stack checkin" aria-labelledby="checkin-title">
      <h2 id="checkin-title">How are you feeling right now?</h2>

      {showPicker ? (
        <div className="mood-picker" role="group" aria-label="Pick your mood">
          {MOODS.map((m) => (
            <button key={m.score} type="button" className="mood-option" disabled={saving}
              onClick={() => checkIn(m.score)}>
              <MoodFace score={m.score} size={40} />
              <span>{m.label}</span>
            </button>
          ))}
        </div>
      ) : (
        <div className="checkin-done">
          <span className="checkin-face"><MoodFace score={latestToday.moodScore} size={44} /></span>
          <div className="stack-sm">
            <p className="checkin-summary">
              You checked in at {formatTime(latestToday.entryDate)}: <strong>{moodLabel(latestToday.moodScore)}</strong>
            </p>
            <div className="row-sm">
              <button type="button" className="button outline small" onClick={() => onWrite(latestToday.moodScore)}>
                Write about it
              </button>
              <button type="button" className="button ghost small" onClick={() => setAgain(true)}>
                Check in again
              </button>
            </div>
          </div>
        </div>
      )}

      {!showPicker && latestToday.moodScore === 1 && (
        <p className="gentle-note">
          Sorry today is hard. You don't have to handle it alone. If you want to talk to someone now,{" "}
          <button type="button" className="link-button inline" onClick={onCrisis}>see support options</button>{" "}
          or call or text <a href="tel:988">988</a>.
        </p>
      )}
      {error && <p className="error-text" role="alert">{error}</p>}
    </section>
  );
}
