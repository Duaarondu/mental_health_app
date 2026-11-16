// Shown once after starting anonymously: the only way back into the account later.
export default function RecoveryCode({ username, code, onDone }) {
  return (
    <div className="card auth-card stack">
      <h2>Save this to come back later</h2>
      <p className="muted">
        You're anonymous, so there's no email to reset a password. To see your bookings again from another
        device, sign in as a patient with these details. We can't show them again.
      </p>
      <dl className="code-box">
        <dt>Username</dt>
        <dd>{username}</dd>
        <dt>Recovery code (password)</dt>
        <dd>{code}</dd>
      </dl>
      <button type="button" className="button primary wide" onClick={onDone}>
        I've saved it, continue
      </button>
    </div>
  );
}
