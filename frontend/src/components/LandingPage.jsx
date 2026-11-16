import { useEffect, useState } from "react";
import { getResources, getWelcome } from "../api.js";
import CrisisButton from "./CrisisButton.jsx";
import Footer from "./Footer.jsx";
import { HeartIcon } from "./Icons.jsx";

const DESCRIPTIONS = {
  Counseling: "One-on-one sessions with a licensed therapist",
  "Stress Management": "Breathing, sleep and coping techniques",
  "Support Groups": "Talk with others who understand",
};

// The public home page: what everyone sees before signing in.
export default function LandingPage({ onLogin, onRegister, onAnonymous, onCrisis }) {
  const [welcome, setWelcome] = useState("");
  const [resources, setResources] = useState([]);

  useEffect(() => {
    getWelcome().then(setWelcome).catch(() => setWelcome(""));
    getResources().then(setResources).catch(() => setResources([]));
  }, []);

  return (
    <>
      <header className="site-header">
        <div className="container header-inner">
          <div className="header-left">
            <div className="brand">
              <HeartIcon color="#3E6B57" />
              MindCare
            </div>
            <button type="button" className="button outline login-button" onClick={onLogin}>
              Log in
            </button>
          </div>
          <CrisisButton onClick={onCrisis} />
        </div>
      </header>

      <main>
        <section className="landing-hero">
          <div className="container landing-hero-inner">
            {welcome && <p className="landing-eyebrow">{welcome}</p>}
            <h1>A calmer place to start.</h1>
            <p className="landing-lead">
              Book sessions with a therapist, check in with yourself, and get help any time you need it. With an
              account or without one.
            </p>
            <div className="row">
              <button type="button" className="button light" onClick={onRegister}>Create an account</button>
              <button type="button" className="button outline-light" onClick={onAnonymous}>
                Continue anonymously
              </button>
            </div>
          </div>
        </section>

        <div className="container page stack-lg">
          <section className="stack">
            <h2>How MindCare helps</h2>
            <div className="row-lg">
              <div className="card stack">
                <h3 className="small-heading">Book a therapist</h3>
                <p className="muted">Pick a licensed therapist and a time that works for you, then manage your sessions in one place.</p>
              </div>
              <div className="card stack">
                <h3 className="small-heading">Stay anonymous if you want</h3>
                <p className="muted">No name or email needed. Choose a nickname, and that's all your therapist sees.</p>
              </div>
              <div className="card stack">
                <h3 className="small-heading">Help in a crisis</h3>
                <p className="muted">The "Need help now?" button is on every page, with 24/7 lines you can call or text right away.</p>
              </div>
            </div>
          </section>

          {resources.length > 0 && (
            <section className="stack">
              <h2>Resources</h2>
              <div className="row-lg">
                {resources.map((name) => (
                  <div className="resource" key={name}>
                    <strong>{name}</strong>
                    <span>{DESCRIPTIONS[name] || "Learn more about this resource."}</span>
                  </div>
                ))}
              </div>
            </section>
          )}

          <section className="card landing-cta">
            <div>
              <h2>Ready when you are</h2>
              <p className="muted">Already have an account? Log in to see your appointments.</p>
            </div>
            <button type="button" className="button primary" onClick={onLogin}>Log in</button>
          </section>
        </div>
      </main>

      <Footer />
    </>
  );
}
