import { useEffect, useState } from "react";
import { getMe, logout } from "./api.js";
import AuthLayout from "./components/AuthLayout.jsx";
import CrisisModal from "./components/CrisisModal.jsx";
import Footer from "./components/Footer.jsx";
import Header from "./components/Header.jsx";
import LandingPage from "./components/LandingPage.jsx";
import LoginPage from "./components/LoginPage.jsx";
import PatientHome from "./components/PatientHome.jsx";
import RecoveryCode from "./components/RecoveryCode.jsx";
import RegisterPage from "./components/RegisterPage.jsx";
import TherapistDashboard from "./components/TherapistDashboard.jsx";

export default function App() {
  // undefined = still checking, null = signed out, object = signed in
  const [user, setUser] = useState(undefined);
  const [authView, setAuthView] = useState("landing"); // "landing" | "login" | "register" | "anonymous"
  const [recovery, setRecovery] = useState(null); // shown once after starting anonymously
  const [crisisOpen, setCrisisOpen] = useState(false);
  const [page, setPage] = useState("dashboard"); // signed in: "home" | "dashboard"

  useEffect(() => {
    getMe().then(setUser).catch(() => setUser(null));
  }, []);

  async function handleLogout() {
    try {
      await logout();
    } finally {
      setUser(null);
      setAuthView("landing");
      setPage("dashboard");
    }
  }

  const signedIn = (u) => {
    setUser(u);
    setPage("dashboard");
  };

  const openCrisis = () => setCrisisOpen(true);
  const crisis = crisisOpen && <CrisisModal user={user} onClose={() => setCrisisOpen(false)} />;

  if (user === undefined) {
    return <p className="loading">Loading...</p>;
  }

  // Signed out: the public home page comes first
  if (!user && !recovery && authView === "landing") {
    return (
      <>
        <LandingPage
          onLogin={() => setAuthView("login")}
          onRegister={() => setAuthView("register")}
          onAnonymous={() => setAuthView("anonymous")}
          onCrisis={openCrisis}
        />
        {crisis}
      </>
    );
  }

  if (!user || recovery) {
    return (
      <>
        <AuthLayout onCrisis={openCrisis} onBack={recovery ? null : () => setAuthView("landing")}>
          {recovery ? (
            <RecoveryCode
              username={recovery.username}
              code={recovery.recoveryCode}
              onDone={() => {
                signedIn(recovery.user);
                setRecovery(null);
              }}
            />
          ) : authView === "login" || authView === "anonymous" ? (
            <LoginPage
              key={authView}
              startAnonymous={authView === "anonymous"}
              onLoggedIn={signedIn}
              onAnonymousStarted={setRecovery}
              onShowRegister={() => setAuthView("register")}
            />
          ) : (
            <RegisterPage onLoggedIn={signedIn} onShowLogin={() => setAuthView("login")} />
          )}
        </AuthLayout>
        {crisis}
      </>
    );
  }

  // Signed in, viewing the public home page
  if (page === "home") {
    return (
      <>
        <LandingPage
          user={user}
          onDashboard={() => setPage("dashboard")}
          onLogout={handleLogout}
          onCrisis={openCrisis}
        />
        {crisis}
      </>
    );
  }

  return (
    <>
      <Header user={user} page={page} onNavigate={setPage} onCrisis={openCrisis} onLogout={handleLogout} />
      {user.role === "Therapist" ? <TherapistDashboard user={user} /> : <PatientHome user={user} onCrisis={openCrisis} />}
      <Footer />
      {crisis}
    </>
  );
}
