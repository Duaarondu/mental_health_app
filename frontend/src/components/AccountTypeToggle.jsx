// Patient / Therapist switch used on the login and sign-up pages
export default function AccountTypeToggle({ value, onChange }) {
  return (
    <div className="segmented" role="group" aria-label="I am a">
      {[
        ["patient", "Patient"],
        ["therapist", "Therapist"],
      ].map(([key, label]) => (
        <button
          key={key}
          type="button"
          aria-pressed={value === key}
          className={value === key ? "active" : ""}
          onClick={() => onChange(key)}
        >
          {label}
        </button>
      ))}
    </div>
  );
}
