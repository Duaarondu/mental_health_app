import { useEffect, useState } from "react";
import { bookAppointment, getTherapists } from "../api.js";
import { todayString } from "../format.js";

export default function BookingForm({ onBooked }) {
  const [therapists, setTherapists] = useState([]);
  const [form, setForm] = useState({ therapistId: "", date: "", time: "", duration: "50" });
  const [message, setMessage] = useState({ text: "", kind: "" });
  const [busy, setBusy] = useState(false);

  useEffect(() => {
    getTherapists().then(setTherapists).catch(() => setTherapists([]));
  }, []);

  const set = (field) => (e) => setForm({ ...form, [field]: e.target.value });

  async function handleSubmit(e) {
    e.preventDefault();
    setBusy(true);
    setMessage({ text: "", kind: "" });
    try {
      await bookAppointment(Number(form.therapistId), `${form.date}T${form.time}`, Number(form.duration));
      setMessage({ text: "Session booked.", kind: "success" });
      setForm({ ...form, date: "", time: "" });
      onBooked();
    } catch (err) {
      setMessage({ text: err.message, kind: "error" });
    } finally {
      setBusy(false);
    }
  }

  return (
    <form className="card stack booking" onSubmit={handleSubmit}>
      <h2>Book a session</h2>
      {therapists.length === 0 ? (
        <p className="muted">No therapists are available to book yet.</p>
      ) : (
        <>
          <div className="row">
            <div className="field grow-2">
              <label htmlFor="book-ther">Therapist</label>
              <select id="book-ther" value={form.therapistId} onChange={set("therapistId")} required>
                <option value="">Choose a therapist</option>
                {therapists.map((t) => (
                  <option key={t.id} value={t.id}>
                    {t.name}{t.specializations ? ` · ${t.specializations}` : ""}
                  </option>
                ))}
              </select>
            </div>
            <div className="field grow">
              <label htmlFor="book-date">Date</label>
              <input id="book-date" type="date" min={todayString()} value={form.date} onChange={set("date")} required />
            </div>
            <div className="field grow">
              <label htmlFor="book-time">Time</label>
              <input id="book-time" type="time" step="900" value={form.time} onChange={set("time")} required />
            </div>
            <div className="field grow">
              <label htmlFor="book-dur">Length</label>
              <select id="book-dur" value={form.duration} onChange={set("duration")}>
                <option value="30">30 min</option>
                <option value="50">50 min</option>
              </select>
            </div>
          </div>
          <div className="row align-center">
            <button type="submit" className="button primary" disabled={busy}>
              {busy ? "Booking..." : "Book session"}
            </button>
            {message.text && (
              <p className={message.kind === "error" ? "error-text" : "success-text"} role="status">{message.text}</p>
            )}
          </div>
        </>
      )}
    </form>
  );
}
