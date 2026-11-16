import { useEffect, useState } from "react";
import { getWelcome } from "../api.js";
import CrisisButton from "./CrisisButton.jsx";
import { HeartIcon } from "./Icons.jsx";

// Green panel on the left, form on the right (stacks on phones)
export default function AuthLayout({ onCrisis, onBack, children }) {
  const [welcome, setWelcome] = useState("");

  useEffect(() => {
    getWelcome().then(setWelcome).catch(() => setWelcome(""));
  }, []);

  return (
    <div className="auth-page">
      <aside className="auth-panel">
        <div className="brand brand-light">
          <HeartIcon color="#FFFFFF" size={36} />
          MindCare
        </div>
        <div className="auth-panel-text">
          <h1>A calmer place to start.</h1>
          <p>Book sessions with your therapist and find support from people who understand.</p>
          {welcome && <p className="auth-welcome">{welcome}</p>}
        </div>
        <p className="auth-panel-foot">
          In crisis? Call or text <strong>988</strong>, any time.
        </p>
      </aside>
      <main className="auth-main">
        {onBack && (
          <button type="button" className="link-button auth-back" onClick={onBack}>
            ← Back to home
          </button>
        )}
        <CrisisButton onClick={onCrisis} className="auth-crisis" />
        {children}
      </main>
    </div>
  );
}
