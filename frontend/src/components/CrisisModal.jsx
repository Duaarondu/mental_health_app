import { useEffect, useRef, useState } from "react";
import { sendCrisisAlert } from "../api.js";
import { CloseIcon } from "./Icons.jsx";

// Shown when someone presses "Need help now?".
// Opening it never contacts anyone. Only the "Let my therapist know" button does.
export default function CrisisModal({ user, onClose }) {
  const [alertState, setAlertState] = useState({ status: "idle", message: "" });
  const dialogRef = useRef(null);
  const isPatient = user && user.role !== "Therapist";

  useEffect(() => {
    dialogRef.current?.focus();
    const onKey = (e) => e.key === "Escape" && onClose();
    window.addEventListener("keydown", onKey);
    return () => window.removeEventListener("keydown", onKey);
  }, [onClose]);

  async function notifyTherapist() {
    setAlertState({ status: "sending", message: "" });
    try {
      const result = await sendCrisisAlert();
      setAlertState({
        status: "done",
        message: result.notified
          ? `${result.therapistNames.join(" and ")} will see an alert and can follow up with you.`
          : "You don't have a therapist yet, so nobody was notified. The numbers above are available right now.",
      });
    } catch (err) {
      setAlertState({ status: "error", message: err.message });
    }
  }

  return (
    <div className="modal-backdrop" onClick={onClose}>
      <section
        className="modal"
        role="dialog"
        aria-modal="true"
        aria-labelledby="crisis-title"
        tabIndex={-1}
        ref={dialogRef}
        onClick={(e) => e.stopPropagation()}
      >
        <div className="modal-head">
          <div>
            <h2 id="crisis-title">You're not alone. Help is available right now.</h2>
            <p className="muted">Free, 24/7 support from trained counselors.</p>
          </div>
          <button type="button" className="icon-button" aria-label="Close" onClick={onClose}>
            <CloseIcon />
          </button>
        </div>

        <div className="crisis-grid">
          <a className="crisis-option urgent" href="tel:988">
            <strong>Call 988</strong>
            <span>Suicide &amp; Crisis Lifeline</span>
          </a>
          <a className="crisis-option urgent" href="sms:988">
            <strong>Text 988</strong>
            <span>If talking feels like too much</span>
          </a>
          <a className="crisis-option" href="https://988lifeline.org/chat" target="_blank" rel="noreferrer">
            <strong>Chat online</strong>
            <span>988lifeline.org</span>
          </a>
          <a className="crisis-option" href="sms:741741?body=HOME">
            <strong>Text HOME to 741741</strong>
            <span>Crisis Text Line</span>
          </a>
        </div>

        <p className="danger-note">
          In immediate danger? Call <a href="tel:911">911</a>.
        </p>

        {isPatient && (
          <div className="notify-box">
            <div>
              <strong>Let my therapist know I reached out</strong>
              <p>They'll see an alert on their dashboard and can follow up with you. Nothing is sent unless you press the button.</p>
            </div>
            {alertState.status === "done" ? (
              <p className="success-text" role="status">{alertState.message}</p>
            ) : (
              <>
                <button
                  type="button"
                  className="button primary"
                  onClick={notifyTherapist}
                  disabled={alertState.status === "sending"}
                >
                  {alertState.status === "sending" ? "Sending..." : "Let my therapist know"}
                </button>
                {alertState.status === "error" && <p className="error-text" role="alert">{alertState.message}</p>}
              </>
            )}
          </div>
        )}

        <div>
          <h3 className="small-heading">While you wait: breathe with me</h3>
          <p className="muted">In for 4 · hold for 4 · out for 6. Repeat a few times.</p>
        </div>

        <button type="button" className="link-button" onClick={onClose}>
          Back to MindCare
        </button>
      </section>
    </div>
  );
}
