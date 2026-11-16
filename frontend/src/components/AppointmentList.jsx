import { formatWhen } from "../format.js";

// Used by patients (shows the therapist) and therapists (shows the client)
export default function AppointmentList({ title, appointments, viewer, onCancel, emptyText }) {
  return (
    <section className="card stack">
      <h2>{title}</h2>
      {appointments.length === 0 ? (
        <p className="muted">{emptyText}</p>
      ) : (
        <ul className="appt-list">
          {appointments.map((a) => (
            <li key={a.id}>
              <div className="appt-main">
                <strong>{formatWhen(a.scheduledTime)}</strong>
                <span className="muted">
                  {viewer === "therapist" ? a.clientName : a.therapistName} · {a.duration} min
                </span>
              </div>
              <span className={`pill pill-${a.status.toLowerCase()}`}>{a.status}</span>
              {a.status === "Scheduled" && onCancel && (
                <button type="button" className="button ghost small" onClick={() => onCancel(a)}>
                  Cancel
                </button>
              )}
            </li>
          ))}
        </ul>
      )}
    </section>
  );
}
