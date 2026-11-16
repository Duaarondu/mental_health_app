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

  useEffect(() => {
    getMe().then(setUser).catch(() => setUser(null));
  }, []);

  async function handleLogout() {
    try {
      await logout();
    } finally {
      setUser(null);
      setAuthView("landing");
    }
  }

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
                setUser(recovery.user);
                setRecovery(null);
              }}
            />
          ) : authView === "login" || authView === "anonymous" ? (
            <LoginPage
              key={authView}
              startAnonymous={authView === "anonymous"}
              onLoggedIn={setUser}
              onAnonymousStarted={setRecovery}
              onShowRegister={() => setAuthView("register")}
            />
          ) : (
            <RegisterPage onLoggedIn={setUser} onShowLogin={() => setAuthView("login")} />
          )}
        </AuthLayout>
        {crisis}
      </>
    );
  }

  return (
    <>
      <Header user={user} onCrisis={openCrisis} onLogout={handleLogout} />
      {user.role === "Therapist" ? <TherapistDashboard user={user} /> : <PatientHome user={user} />}
      <Footer />
      {crisis}
    </>
  );
}
