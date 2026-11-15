import { useState } from "react";
import { bookAppointment } from "../api.js";

// Today's date as "YYYY-MM-DD", used to block past dates.
function todayString() {
  const d = new Date();
  const pad = (n) => String(n).padStart(2, "0");
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;
}

const EMPTY_FORM = { type: "", date: "", time: "" };

export default function BookingForm({ resources, onBooked }) {
  const [form, setForm] = useState(EMPTY_FORM);
  const [message, setMessage] = useState({ text: "", kind: "" });

  // One change handler for all three inputs, using each input's "name".
  function handleChange(e) {
    setForm({ ...form, [e.target.name]: e.target.value });
  }

  async function handleSubmit(e) {
    e.preventDefault(); // stop the page from reloading
    try {
      const text = await bookAppointment(form); // "Appointment booked successfully!"
      setMessage({ text, kind: "success" });
      setForm(EMPTY_FORM);
      onBooked(); // tell App to reload the appointment list
    } catch (err) {
      console.error(err);
      setMessage({ text: "Something went wrong. Please try again.", kind: "error" });
    }
  }

  return (
    <section id="book">
      <h2>Book an Appointment</h2>
      <form className="panel" onSubmit={handleSubmit}>
        <label htmlFor="type">Type of appointment</label>
        <select id="type" name="type" value={form.type} onChange={handleChange} required>
          <option value="">Choose one...</option>
          {resources.map((name) => (
            <option key={name} value={name}>
              {name}
            </option>
          ))}
        </select>

        <div className="row">
          <div>
            <label htmlFor="date">Date</label>
            <input
              type="date"
              id="date"
              name="date"
              min={todayString()}
              value={form.date}
              onChange={handleChange}
              required
            />
          </div>
          <div>
            <label htmlFor="time">Time</label>
            <input
              type="time"
              id="time"
              name="time"
              value={form.time}
              onChange={handleChange}
              required
            />
          </div>
        </div>

        <button type="submit" className="btn">
          Book appointment
        </button>
        <p className={`message ${message.kind}`}>{message.text}</p>
      </form>
    </section>
  );
}
