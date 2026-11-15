// All calls to the Spring Boot backend live in this one file.
// The Vite proxy (vite.config.js) forwards /api/... to http://localhost:8080.

const API = "/api";

// GET /api/home -> "Welcome to our Mental Health App!" (plain text)
export async function getWelcome() {
  const res = await fetch(`${API}/home`);
  if (!res.ok) throw new Error(`GET /home failed: ${res.status}`);
  return res.text();
}

// GET /api/resources -> ["Counseling", "Stress Management", "Support Groups"]
export async function getResources() {
  const res = await fetch(`${API}/resources`);
  if (!res.ok) throw new Error(`GET /resources failed: ${res.status}`);
  return res.json();
}

// GET /api/appointments -> [{ type, date, time }, ...]
export async function getAppointments() {
  const res = await fetch(`${API}/appointments`);
  if (!res.ok) throw new Error(`GET /appointments failed: ${res.status}`);
  return res.json();
}

// POST /api/appointments with { type, date, time }
// -> "Appointment booked successfully!" (plain text)
export async function bookAppointment(appointment) {
  const res = await fetch(`${API}/appointments`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(appointment),
  });
  if (!res.ok) throw new Error(`POST /appointments failed: ${res.status}`);
  return res.text();
}
