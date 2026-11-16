// "2026-11-19T15:00:00" -> a Date in the browser's local time (no time-zone shifting)
export function parseLocal(isoDateTime) {
  const [date, time = "00:00"] = isoDateTime.split("T");
  const [y, m, d] = date.split("-").map(Number);
  const [hh, mm] = time.split(":").map(Number);
  return new Date(y, m - 1, d, hh, mm);
}

// -> "Thu, Nov 19 · 3:00 PM"
export function formatWhen(isoDateTime) {
  const d = parseLocal(isoDateTime);
  const day = d.toLocaleDateString("en-US", { weekday: "short", month: "short", day: "numeric" });
  const time = d.toLocaleTimeString("en-US", { hour: "numeric", minute: "2-digit" });
  return `${day} · ${time}`;
}

// "Good morning" / "Good afternoon" / "Good evening"
export function greeting() {
  const h = new Date().getHours();
  if (h < 12) return "Good morning";
  if (h < 18) return "Good afternoon";
  return "Good evening";
}

// Today's date as "YYYY-MM-DD", used to block past dates in date inputs
export function todayString() {
  const d = new Date();
  const pad = (n) => String(n).padStart(2, "0");
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;
}
