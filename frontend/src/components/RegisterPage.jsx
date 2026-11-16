import { useState } from "react";
import { register } from "../api.js";
import AccountTypeToggle from "./AccountTypeToggle.jsx";

export default function RegisterPage({ onLoggedIn, onShowLogin }) {
  const [form, setForm] = useState({
    accountType: "patient",
    firstName: "",
    lastName: "",
    username: "",
    password: "",
    specializations: "",
  });
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);

  const set = (field) => (e) => setForm({ ...form, [field]: e.target.value });

  async function handleSubmit(e) {
    e.preventDefault();
    setError("");
    setBusy(true);
    try {
      onLoggedIn(await register(form));
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  }

  const therapist = form.accountType === "therapist";

  return (
    <div className="card auth-card">
      <h2>Create an account</h2>
      <form onSubmit={handleSubmit} className="stack">
        <AccountTypeToggle value={form.accountType} onChange={(v) => setForm({ ...form, accountType: v })} />
        <div className="row">
          <div className="field grow">
            <label htmlFor="reg-first">First name</label>
            <input id="reg-first" autoComplete="given-name" value={form.firstName} onChange={set("firstName")} required maxLength={50} />
          </div>
          <div className="field grow">
            <label htmlFor="reg-last">Last name</label>
            <input id="reg-last" autoComplete="family-name" value={form.lastName} onChange={set("lastName")} required maxLength={50} />
          </div>
        </div>
        <div className="field">
          <label htmlFor="reg-user">Username</label>
          <input id="reg-user" autoComplete="username" value={form.username} onChange={set("username")} required
            minLength={3} maxLength={50} pattern="[A-Za-z0-9_.\-]+" title="Letters, numbers, dots, dashes or underscores" />
        </div>
        <div className="field">
          <label htmlFor="reg-pass">Password</label>
          <input id="reg-pass" type="password" autoComplete="new-password" value={form.password} onChange={set("password")}
            required minLength={8} maxLength={72} />
          <small className="muted">At least 8 characters.</small>
        </div>
        {therapist && (
          <div className="field">
            <label htmlFor="reg-spec">Specializations</label>
            <input id="reg-spec" placeholder="e.g. Anxiety, grief" value={form.specializations} onChange={set("specializations")} maxLength={255} />
            <small className="muted">New therapist accounts are verified before patients can book them.</small>
          </div>
        )}
        {error && <p className="error-text" role="alert">{error}</p>}
        <button type="submit" className="button primary wide" disabled={busy}>
          {busy ? "Creating account..." : "Create account"}
        </button>
      </form>
      <p className="center muted">
        Already have an account?{" "}
        <button type="button" className="link-button" onClick={onShowLogin}>Sign in</button>
      </p>
    </div>
  );
}
