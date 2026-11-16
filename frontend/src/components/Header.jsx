import CrisisButton from "./CrisisButton.jsx";
import { HeartIcon } from "./Icons.jsx";

export default function Header({ user, onCrisis, onLogout }) {
  const initials = user.anonymous
    ? "?"
    : `${user.firstName?.[0] ?? ""}${user.lastName?.[0] ?? ""}`.toUpperCase();

  return (
    <header className="site-header">
      <div className="container header-inner">
        <div className="brand">
          <HeartIcon color="#3E6B57" />
          MindCare
        </div>
        <div className="header-actions">
          <CrisisButton onClick={onCrisis} />
          <span className="avatar" aria-hidden="true">{initials}</span>
          <span className="header-name">{user.displayName}</span>
          <button type="button" className="button ghost" onClick={onLogout}>Sign out</button>
        </div>
      </div>
    </header>
  );
}
