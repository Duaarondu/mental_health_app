// All calls to the Spring Boot backend live in this one file.
// In development, Vite forwards /api/... to http://localhost:8080 (see vite.config.js).
// Login uses a session cookie, which the browser sends automatically.

const API = "/api";

async function request(path, { method = "GET", body } = {}) {
  let res;
  try {
    res = await fetch(`${API}${path}`, {
      method,
      credentials: "same-origin",
      headers: body !== undefined ? { "Content-Type": "application/json" } : undefined,
      body: body !== undefined ? JSON.stringify(body) : undefined,
    });
  } catch {
    throw new Error("Could not reach the server. Please try again in a moment.");
  }

  const text = await res.text();
  let data = null;
  if (text) {
    try {
      data = JSON.parse(text);
    } catch {
      data = text; // plain-text responses like /api/home
    }
  }

  if (!res.ok) {
    const message =
      (data && typeof data === "object" && data.error) ||
      (res.status === 401 ? "Please sign in." : `Something went wrong (error ${res.status}).`);
    const error = new Error(message);
    error.status = res.status;
    throw error;
  }
  return data;
}

// ---------- Auth ----------

// The logged-in user, or null if nobody is signed in
export async function getMe() {
  try {
    return await request("/auth/me");
  } catch (err) {
    if (err.status === 401) return null;
    throw err;
  }
}

// accountType: "patient" | "therapist"
export const login = (username, password, accountType) =>
  request("/auth/login", { method: "POST", body: { username, password, accountType } });

// { username, password, firstName, lastName, accountType, specializations }
export const register = (details) => request("/auth/register", { method: "POST", body: details });

// Returns { user, username, recoveryCode }
export const startAnonymous = (nickname) =>
  request("/auth/anonymous", { method: "POST", body: { nickname } });

export const logout = () => request("/auth/logout", { method: "POST" });

// ---------- Public info ----------

export const getWelcome = () => request("/home");
export const getResources = () => request("/resources");
export const getTherapists = () => request("/therapists");

// ---------- Appointments ----------

export const getAppointments = () => request("/appointments");

// scheduledTime like "2026-11-20T15:00", duration 30 or 50
export const bookAppointment = (therapistId, scheduledTime, duration) =>
  request("/appointments", { method: "POST", body: { therapistId, scheduledTime, duration } });

export const cancelAppointment = (id) => request(`/appointments/${id}/cancel`, { method: "POST" });

// ---------- Crisis ----------

// Patient taps "Let my therapist know". Returns { notified, therapistNames }
export const sendCrisisAlert = () => request("/crisis/alert", { method: "POST" });

// Therapist dashboard
export const getCrisisAlerts = () => request("/therapist/alerts");
export const resolveCrisisAlert = (clientId) =>
  request(`/therapist/alerts/${clientId}/resolve`, { method: "POST" });

// ---------- Mood check-ins & journal (patients) ----------

// Newest first: [{ id, entryDate, moodScore, content }]
export const getJournal = () => request("/journal");

// moodScore 1-5. Leave content empty for a quick mood check-in.
export const addJournalEntry = (moodScore, content = "") =>
  request("/journal", { method: "POST", body: { moodScore, content } });

export const deleteJournalEntry = (id) => request(`/journal/${id}`, { method: "DELETE" });
