import CrisisButton from "./CrisisButton.jsx";
import { HeartIcon } from "./Icons.jsx";

// Header for signed-in users: Home / Dashboard links on the left, crisis + account on the right
export default function Header({ user, page, onNavigate, onCrisis, onLogout }) {
  const initials = user.anonymous
    ? "?"
    : `${user.firstName?.[0] ?? ""}${user.lastName?.[0] ?? ""}`.toUpperCase();

  const navItem = (key, label) => (
    <button
      type="button"
      className={`nav-link ${page === key ? "active" : ""}`}
      aria-current={page === key ? "page" : undefined}
      onClick={() => onNavigate(key)}
    >
      {label}
    </button>
  );

  return (
    <header className="site-header">
      <div className="container header-inner">
        <div className="header-left">
          <button type="button" className="brand brand-button" onClick={() => onNavigate("home")}>
            <HeartIcon color="#3E6B57" />
            MindCare
          </button>
          <nav className="main-nav" aria-label="Main">
            {navItem("home", "Home")}
            {navItem("dashboard", "Dashboard")}
          </nav>
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
