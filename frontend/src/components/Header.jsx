import { useEffect, useState } from "react";
import { getWelcome } from "../api.js";

export default function Header() {
  const [welcome, setWelcome] = useState("Loading...");

  useEffect(() => {
    getWelcome()
      .then(setWelcome)
      .catch(() => setWelcome("Welcome!"));
  }, []);

  return (
    <header className="hero">
      <nav className="nav">
        <span className="logo">MindCare</span>
        <div className="nav-links">
          <a href="#resources">Resources</a>
          <a href="#book">Book</a>
          <a href="#appointments">My Appointments</a>
        </div>
      </nav>
      <div className="hero-text">
        <h1>{welcome}</h1>
        <p>Support is here when you need it. Explore resources or book a session below.</p>
        <a href="#book" className="btn btn-light">
          Book an appointment
        </a>
      </div>
    </header>
  );
}
