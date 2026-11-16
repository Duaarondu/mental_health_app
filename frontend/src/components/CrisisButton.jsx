import { PhoneIcon } from "./Icons.jsx";

// Always-visible "Need help now?" button. Opens the crisis screen.
export default function CrisisButton({ onClick, className = "" }) {
  return (
    <button type="button" className={`crisis-button ${className}`} onClick={onClick}>
      <PhoneIcon />
      Need help now?
    </button>
  );
}
