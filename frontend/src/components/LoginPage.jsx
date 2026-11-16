import { useState } from "react";
import { login, startAnonymous as createAnonymousAccount } from "../api.js";
import AccountTypeToggle from "./AccountTypeToggle.jsx";

export default function LoginPage({ onLoggedIn, onAnonymousStarted, onShowRegister, startAnonymous = false, notice }) {
  const [accountType, setAccountType] = useState("patient");
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);

  const [showAnon, setShowAnon] = useState(startAnonymous);
  const [nickname, setNickname] = useState("");
  const [anonError, setAnonError] = useState("");

  async function handleLogin(e) {
    e.preventDefault();
    setError("");
    setBusy(true);
    try {
      onLoggedIn(await login(username, password, accountType));
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  }

  async function handleAnonymous(e) {
    e.preventDefault();
    setAnonError("");
    setBusy(true);
    try {
      onAnonymousStarted(await createAnonymousAccount(nickname));
    } catch (err) {
      setAnonError(err.message);
    } finally {
      setBusy(false);
    }
  }

  return (
    <div className="card auth-card">
      <h2>Welcome back</h2>
      {notice && <p className="info-banner" role="status">{notice}</p>}
      <form onSubmit={handleLogin} className="stack">
        <AccountTypeToggle value={accountType} onChange={setAccountType} />
        <div className="field">
          <label htmlFor="login-user">Username</label>
          <input id="login-user" autoComplete="username" value={username}
            onChange={(e) => setUsername(e.target.value)} required />
        </div>
        <div className="field">
          <label htmlFor="login-pass">Password</label>
          <input id="login-pass" type="password" autoComplete="current-password" value={password}
            onChange={(e) => setPassword(e.target.value)} required />
        </div>
        {error && <p className="error-text" role="alert">{error}</p>}
        <button type="submit" className="button primary wide" disabled={busy}>
          {busy ? "Signing in..." : `Sign in as ${accountType}`}
        </button>
      </form>
      <p className="center muted">
        New here?{" "}
        <button type="button" className="link-button" onClick={onShowRegister}>Create an account</button>
      </p>

      <div className="divider"><span>or</span></div>

      {!showAnon ? (
        <button type="button" className="anon-button" onClick={() => setShowAnon(true)}>
          <strong>Continue anonymously</strong>
          <span>We don't ask for your name or email. You can still book a therapist.</span>
        </button>
      ) : (
        <form onSubmit={handleAnonymous} className="stack anon-form">
          <div className="field">
            <label htmlFor="anon-nick">Nickname (optional)</label>
            <input id="anon-nick" maxLength={30} placeholder="e.g. Quiet Fox" value={nickname}
              onChange={(e) => setNickname(e.target.value)} />
            <small className="muted">This is the only name your therapist will see.</small>
          </div>
          {anonError && <p className="error-text" role="alert">{anonError}</p>}
          <button type="submit" className="button outline wide" disabled={busy}>Continue anonymously</button>
        </form>
      )}
    </div>
  );
}
