import { useCallback, useEffect, useState } from "react";
import { cancelAppointment, getAppointments } from "../api.js";
import { formatWhen, greeting, parseLocal } from "../format.js";
import AppointmentList from "./AppointmentList.jsx";
import BookingForm from "./BookingForm.jsx";
import ResourceList from "./ResourceList.jsx";

export default function PatientHome({ user }) {
  const [appointments, setAppointments] = useState([]);
  const [error, setError] = useState("");

  const load = useCallback(() => {
    getAppointments()
      .then((list) => {
        setAppointments(list);
        setError("");
      })
      .catch((err) => setError(err.message));
  }, []);

  useEffect(load, [load]);

  async function handleCancel(appt) {
    if (!window.confirm(`Cancel your session on ${formatWhen(appt.scheduledTime)}?`)) return;
    try {
      await cancelAppointment(appt.id);
      load();
    } catch (err) {
      setError(err.message);
    }
  }

  const now = new Date();
  const next = appointments.find((a) => a.status === "Scheduled" && parseLocal(a.scheduledTime) > now);

  return (
    <main className="container page stack-lg">
      <div>
        <h1>{greeting()}, {user.firstName}</h1>
        <p className="muted lead">Take a moment. How are you feeling right now?</p>
      </div>

      {user.anonymous && (
        <p className="info-banner">
          You're using MindCare anonymously. Therapists only see you as "{user.displayName}".
        </p>
      )}
      {error && <p className="error-text" role="alert">{error}</p>}

      <div className="row-lg">
        <section className="card next-session">
          <h2 className="eyebrow">Next session</h2>
          {next ? (
            <>
              <p className="next-when">{formatWhen(next.scheduledTime)}</p>
              <p>{next.therapistName} · {next.duration} minutes</p>
            </>
          ) : (
            <p className="next-when small">Nothing booked yet</p>
          )}
        </section>
        <BookingForm onBooked={load} />
      </div>

      <div className="row-lg">
        <AppointmentList
          title="My appointments"
          appointments={appointments}
          viewer="patient"
          onCancel={handleCancel}
          emptyText="You haven't booked any sessions yet."
        />
        <ResourceList />
      </div>
    </main>
  );
}
