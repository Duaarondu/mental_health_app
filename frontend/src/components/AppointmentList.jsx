// "2026-11-20" -> "Fri, Nov 20, 2026"
function formatDate(isoDate) {
  if (!isoDate) return "";
  const [year, month, day] = isoDate.split("-").map(Number);
  return new Date(year, month - 1, day).toLocaleDateString("en-US", {
    weekday: "short",
    month: "short",
    day: "numeric",
    year: "numeric",
  });
}

// "14:30" -> "2:30 PM"
function formatTime(time) {
  if (!time) return "";
  const [hours, minutes] = time.split(":").map(Number);
  const suffix = hours >= 12 ? "PM" : "AM";
  return `${hours % 12 || 12}:${String(minutes).padStart(2, "0")} ${suffix}`;
}

export default function AppointmentList({ appointments }) {
  return (
    <section id="appointments">
      <h2>My Appointments</h2>
      <ul className="panel list">
        {appointments.length === 0 ? (
          <li className="empty">No appointments booked yet.</li>
        ) : (
          appointments.map((appt, index) => (
            // The backend doesn't give appointments an id, so we use the index.
            <li key={index}>
              <span>
                {formatDate(appt.date)} at {formatTime(appt.time)}
              </span>
              <span className="tag">{appt.type}</span>
            </li>
          ))
        )}
      </ul>
    </section>
  );
}
