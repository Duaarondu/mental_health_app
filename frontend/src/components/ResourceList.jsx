// Short descriptions shown under each resource name from the backend.
const DESCRIPTIONS = {
  Counseling: "Talk one-on-one with a trained counselor in a safe, confidential space.",
  "Stress Management": "Learn practical techniques to handle pressure, anxiety, and burnout.",
  "Support Groups": "Connect with others who understand what you're going through.",
};

export default function ResourceList({ resources }) {
  return (
    <section id="resources">
      <h2>Resources</h2>
      <div className="cards">
        {resources.map((name) => (
          <div className="card" key={name}>
            <h3>{name}</h3>
            <p>{DESCRIPTIONS[name] || "Learn more about this resource."}</p>
          </div>
        ))}
      </div>
    </section>
  );
}
