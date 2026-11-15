// The page is served by Spring Boot itself, so API calls use relative paths
// (no CORS setup needed).
const API = "/api";

// Short descriptions shown under each resource from the backend.
const RESOURCE_DESCRIPTIONS = {
    "Counseling": "Talk one-on-one with a trained counselor in a safe, confidential space.",
    "Stress Management": "Learn practical techniques to handle pressure, anxiety, and burnout.",
    "Support Groups": "Connect with others who understand what you're going through."
};

// ---------- Home: GET /api/home ----------
async function loadWelcome() {
    const heading = document.getElementById("welcome");
    try {
        const res = await fetch(`${API}/home`);
        heading.textContent = await res.text();
    } catch (err) {
        heading.textContent = "Welcome!";
        console.error("Could not load welcome message:", err);
    }
}

// ---------- Resources: GET /api/resources ----------
async function loadResources() {
    const list = document.getElementById("resource-list");
    const typeSelect = document.getElementById("type");

    try {
        const res = await fetch(`${API}/resources`);
        const resources = await res.json();

        list.innerHTML = "";
        resources.forEach(name => {
            // Resource card
            const card = document.createElement("div");
            card.className = "card";

            const title = document.createElement("h3");
            title.textContent = name;

            const desc = document.createElement("p");
            desc.textContent = RESOURCE_DESCRIPTIONS[name] || "Learn more about this resource.";

            card.append(title, desc);
            list.appendChild(card);

            // Also use each resource as an appointment type option
            const option = document.createElement("option");
            option.value = name;
            option.textContent = name;
            typeSelect.appendChild(option);
        });
    } catch (err) {
        list.innerHTML = '<p class="empty">Could not load resources. Is the backend running?</p>';
        console.error("Could not load resources:", err);
    }
}

// ---------- Appointments: GET /api/appointments ----------
async function loadAppointments() {
    const list = document.getElementById("appointment-list");

    try {
        const res = await fetch(`${API}/appointments`);
        const appointments = await res.json();

        list.innerHTML = "";
        if (appointments.length === 0) {
            list.innerHTML = '<li class="empty">No appointments booked yet.</li>';
            return;
        }

        appointments.forEach(appt => {
            const item = document.createElement("li");

            const when = document.createElement("span");
            when.textContent = `${formatDate(appt.date)} at ${formatTime(appt.time)}`;

            const tag = document.createElement("span");
            tag.className = "tag";
            tag.textContent = appt.type;

            item.append(when, tag);
            list.appendChild(item);
        });
    } catch (err) {
        list.innerHTML = '<li class="empty">Could not load appointments. Is the backend running?</li>';
        console.error("Could not load appointments:", err);
    }
}

// ---------- Book: POST /api/appointments ----------
async function bookAppointment(event) {
    event.preventDefault(); // stop the page from reloading

    const message = document.getElementById("form-message");
    const appointment = {
        type: document.getElementById("type").value,
        date: document.getElementById("date").value,  // e.g. "2026-11-20"
        time: document.getElementById("time").value   // e.g. "14:30"
    };

    try {
        const res = await fetch(`${API}/appointments`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(appointment)
        });

        if (!res.ok) throw new Error(`Server responded with ${res.status}`);

        message.textContent = await res.text(); // "Appointment booked successfully!"
        message.className = "message success";
        event.target.reset();
        loadAppointments();
    } catch (err) {
        message.textContent = "Something went wrong. Please try again.";
        message.className = "message error";
        console.error("Could not book appointment:", err);
    }
}

// ---------- Helpers ----------
function formatDate(isoDate) {
    if (!isoDate) return "";
    const [year, month, day] = isoDate.split("-").map(Number);
    return new Date(year, month - 1, day).toLocaleDateString("en-US", {
        weekday: "short", month: "short", day: "numeric", year: "numeric"
    });
}

function formatTime(time) {
    if (!time) return "";
    const [hours, minutes] = time.split(":").map(Number);
    const suffix = hours >= 12 ? "PM" : "AM";
    return `${hours % 12 || 12}:${String(minutes).padStart(2, "0")} ${suffix}`;
}

// ---------- Start ----------
document.addEventListener("DOMContentLoaded", () => {
    // Don't allow booking dates in the past
    const today = new Date();
    const pad = n => String(n).padStart(2, "0");
    document.getElementById("date").min =
        `${today.getFullYear()}-${pad(today.getMonth() + 1)}-${pad(today.getDate())}`;

    document.getElementById("booking-form").addEventListener("submit", bookAppointment);

    loadWelcome();
    loadResources();
    loadAppointments();
});
