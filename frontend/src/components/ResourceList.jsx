import { useEffect, useState } from "react";
import { getResources } from "../api.js";

const DESCRIPTIONS = {
  Counseling: "One-on-one sessions with a licensed therapist",
  "Stress Management": "Breathing, sleep and coping techniques",
  "Support Groups": "Talk with others who understand",
};

export default function ResourceList() {
  const [resources, setResources] = useState([]);

  useEffect(() => {
    getResources().then(setResources).catch(() => setResources([]));
  }, []);

  return (
    <section className="card stack">
      <h2>Resources</h2>
      {resources.map((name) => (
        <div className="resource" key={name}>
          <strong>{name}</strong>
          <span>{DESCRIPTIONS[name] || "Learn more about this resource."}</span>
        </div>
      ))}
    </section>
  );
}
