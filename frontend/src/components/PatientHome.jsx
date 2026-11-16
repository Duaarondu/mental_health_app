import { useCallback, useEffect, useRef, useState } from "react";
import { cancelAppointment, getAppointments, getJournal } from "../api.js";
import { formatWhen, greeting, parseLocal } from "../format.js";
import AppointmentList from "./AppointmentList.jsx";
import BookingForm from "./BookingForm.jsx";
import Journal from "./Journal.jsx";
import MoodCheckIn from "./MoodCheckIn.jsx";
import ResourceList from "./ResourceList.jsx";

export default function PatientHome({ user, onCrisis }) {
  const [appointments, setAppointments] = useState([]);
  const [entries, setEntries] = useState([]);
  const [journalMood, setJournalMood] = useState(null);
  const [error, setError] = useState("");
  const journalTextRef = useRef(null);

  const load = useCallback(() => {
    getAppointments()
      .then((list) => {
        setAppointments(list);
        setError("");
      })
      .catch((err) => setError(err.message));
  }, []);

  const loadJournal = useCallback(() => {
    getJournal()
      .then(setEntries)
      .catch((err) => setError(err.message));
  }, []);

  useEffect(load, [load]);
  useEffect(loadJournal, [loadJournal]);

  // "Write about it" after a check-in: same mood, jump to the journal box
  function writeAbout(score) {
    setJournalMood(score);
    journalTextRef.current?.scrollIntoView({ behavior: "smooth", block: "center" });
    journalTextRef.current?.focus({ preventScroll: true });
  }

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
        <p className="muted lead">Take a moment for yourself.</p>
      </div>

      {user.anonymous && (
        <p className="info-banner">
          You're using MindCare anonymously. Therapists only see you as "{user.displayName}".
        </p>
      )}
      {error && <p className="error-text" role="alert">{error}</p>}

      <MoodCheckIn entries={entries} onSaved={loadJournal} onWrite={writeAbout} onCrisis={onCrisis} />

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

      <Journal ref={journalTextRef} entries={entries} mood={journalMood} onMoodChange={setJournalMood}
        onSaved={loadJournal} />

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
