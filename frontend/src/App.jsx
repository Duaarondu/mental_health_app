import { useEffect, useState } from "react";
import { getAppointments, getResources } from "./api.js";
import Header from "./components/Header.jsx";
import ResourceList from "./components/ResourceList.jsx";
import BookingForm from "./components/BookingForm.jsx";
import AppointmentList from "./components/AppointmentList.jsx";
import Footer from "./components/Footer.jsx";

export default function App() {
  // App keeps the data that more than one component needs:
  // - resources: shown as cards AND used as the booking form's type options
  // - appointments: shown in the list AND refreshed after the form books one
  const [resources, setResources] = useState([]);
  const [appointments, setAppointments] = useState([]);
  const [error, setError] = useState("");

  async function loadAppointments() {
    try {
      setAppointments(await getAppointments());
    } catch (err) {
      console.error(err);
      setError("Could not reach the backend. Is Spring Boot running on port 8080?");
    }
  }

  useEffect(() => {
    getResources()
      .then(setResources)
      .catch((err) => {
        console.error(err);
        setError("Could not reach the backend. Is Spring Boot running on port 8080?");
      });
    loadAppointments();
  }, []);

  return (
    <>
      <Header />
      <main>
        {error && <p className="banner-error">{error}</p>}
        <ResourceList resources={resources} />
        <BookingForm resources={resources} onBooked={loadAppointments} />
        <AppointmentList appointments={appointments} />
      </main>
      <Footer />
    </>
  );
}
