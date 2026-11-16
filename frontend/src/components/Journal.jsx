import { forwardRef, useState } from "react";
import { addJournalEntry, deleteJournalEntry } from "../api.js";
import { formatWhen } from "../format.js";
import { MOODS, moodLabel } from "../moods.js";
import { LockIcon, MoodFace } from "./Icons.jsx";

const MAX_LENGTH = 5000;
const PAGE = 10;

// Private journal. Text is encrypted by the server before it's saved; therapists can't read it.
const Journal = forwardRef(function Journal({ entries, mood, onMoodChange, onSaved }, textRef) {
  const [text, setText] = useState("");
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  const [shown, setShown] = useState(PAGE);

  async function handleSave(e) {
    e.preventDefault();
    if (!mood) {
      setError("Pick how you're feeling first.");
      return;
    }
    if (!text.trim()) {
      setError("Write a few words first.");
      return;
    }
    setSaving(true);
    setError("");
    try {
      await addJournalEntry(mood, text);
      setText("");
      onSaved();
    } catch (err) {
      setError(err.message);
    } finally {
      setSaving(false);
    }
  }

  async function handleDelete(entry) {
    if (!window.confirm("Delete this entry? This can't be undone.")) return;
    try {
      await deleteJournalEntry(entry.id);
      onSaved();
    } catch (err) {
      setError(err.message);
    }
  }

  return (
    <section className="card stack" aria-labelledby="journal-title">
      <div className="journal-head">
        <h2 id="journal-title">My journal</h2>
        <p className="private-note"><LockIcon /> Private. Encrypted before it's saved. Your therapist can't read it.</p>
      </div>

      <form className="stack" onSubmit={handleSave}>
        <div className="field">
          <span className="field-label" id="journal-mood-label">Mood</span>
          <div className="mood-chips" role="radiogroup" aria-labelledby="journal-mood-label">
            {MOODS.map((m) => (
              <button key={m.score} type="button" role="radio" aria-checked={mood === m.score}
                className={`mood-chip${mood === m.score ? " active" : ""}`} onClick={() => onMoodChange(m.score)}>
                <MoodFace score={m.score} size={22} />
                {m.label}
              </button>
            ))}
          </div>
        </div>
        <div className="field">
          <label htmlFor="journal-text">What's on your mind?</label>
          <textarea id="journal-text" ref={textRef} className="journal-text" value={text} maxLength={MAX_LENGTH}
            onChange={(e) => setText(e.target.value)}
            placeholder="What happened today? What helped, and what was hard?" />
          <small className="muted">{text.length} / {MAX_LENGTH}</small>
        </div>
        {error && <p className="error-text" role="alert">{error}</p>}
        <button type="submit" className="button primary" disabled={saving}>
          {saving ? "Saving..." : "Save entry"}
        </button>
      </form>

      <h3 className="small-heading">Past entries</h3>
      {entries.length === 0 ? (
        <p className="muted">Your check-ins and entries will show up here.</p>
      ) : (
        <>
          <ul className="journal-list">
            {entries.slice(0, shown).map((entry) => (
              <li key={entry.id}>
                <div className="journal-meta">
                  <span className="journal-mood"><MoodFace score={entry.moodScore} size={22} /> {moodLabel(entry.moodScore)}</span>
                  <span className="muted">{formatWhen(entry.entryDate)}</span>
                  <button type="button" className="button ghost small" onClick={() => handleDelete(entry)}
                    aria-label={`Delete entry from ${formatWhen(entry.entryDate)}`}>
                    Delete
                  </button>
                </div>
                {entry.content ? <p className="journal-body">{entry.content}</p> : <p className="muted">Mood check-in</p>}
              </li>
            ))}
          </ul>
          {entries.length > shown && (
            <button type="button" className="link-button" onClick={() => setShown(shown + PAGE)}>
              Show older entries
            </button>
          )}
        </>
      )}
    </section>
  );
});

export default Journal;
