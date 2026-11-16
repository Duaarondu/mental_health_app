import { useCallback, useEffect, useState } from "react";
import { cancelAppointment, getAppointments, getCrisisAlerts, resolveCrisisAlert } from "../api.js";
import { formatWhen, greeting } from "../format.js";
import AppointmentList from "./AppointmentList.jsx";

export default function TherapistDashboard({ user }) {
  const [appointments, setAppointments] = useState([]);
  const [alerts, setAlerts] = useState([]);
  const [error, setError] = useState("");

  const load = useCallback(() => {
    Promise.all([getAppointments(), getCrisisAlerts()])
      .then(([appts, crisis]) => {
        setAppointments(appts);
        setAlerts(crisis);
        setError("");
      })
      .catch((err) => setError(err.message));
  }, []);

  useEffect(() => {
    load();
    const timer = setInterval(load, 30000); // check for new crisis alerts every 30 seconds
    return () => clearInterval(timer);
  }, [load]);

  async function handleResolve(clientId) {
    try {
      await resolveCrisisAlert(clientId);
      load();
    } catch (err) {
      setError(err.message);
    }
  }

  async function handleCancel(appt) {
    if (!window.confirm(`Cancel the session with ${appt.clientName} on ${formatWhen(appt.scheduledTime)}?`)) return;
    try {
      await cancelAppointment(appt.id);
      load();
    } catch (err) {
      setError(err.message);
    }
  }

  return (
    <main className="container page stack-lg">
      <div>
        <h1>{greeting()}, {user.displayName}</h1>
        <p className="muted lead">Here's your schedule and anyone who needs you.</p>
      </div>

      {!user.verified && (
        <p className="info-banner">
          Your account is waiting for verification. Patients can't book you until it's approved.
        </p>
      )}
      {error && <p className="error-text" role="alert">{error}</p>}

      <section className="card stack" aria-live="polite">
        <h2>Crisis alerts</h2>
        {alerts.length === 0 ? (
          <p className="muted">No one has asked for urgent help.</p>
        ) : (
          <ul className="alert-list">
            {alerts.map((a) => (
              <li key={a.clientId}>
                <span>
                  <strong>{a.clientName}</strong> pressed "Need help now" and asked you to follow up.
                </span>
                <button type="button" className="button primary small" onClick={() => handleResolve(a.clientId)}>
                  Mark as followed up
                </button>
              </li>
            ))}
          </ul>
        )}
      </section>

      <AppointmentList
        title="Your schedule"
        appointments={appointments}
        viewer="therapist"
        onCancel={handleCancel}
        emptyText="No sessions booked with you yet."
      />
    </main>
  );
}
