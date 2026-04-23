/**
 * Login page — Google OAuth2 sign-in entry point.
 * Member 4 is responsible for implementing OAuth2 flow.
 *
 * The "Sign in with Google" button redirects to Spring Security's OAuth2
 * authorization endpoint, which handles the Google redirect.
 */
function LoginPage() {
  const backendUrl =
    import.meta.env.VITE_BACKEND_URL || "http://localhost:8080";

  function handleGoogleLogin() {
    // Redirect to Spring Boot OAuth2 authorization endpoint
    window.location.href = `${backendUrl}/oauth2/authorization/google`;
  }

  return (
    <div className="login-wrapper">
      <div className="login-card">
        <h1>🏫 Smart Campus Hub</h1>
        <p>
          Manage campus facilities, bookings, and incident tickets.
          <br />
          Sign in with your university Google account to get started.
        </p>

        <button className="btn-google" onClick={handleGoogleLogin}>
          <img
            src="https://www.gstatic.com/firebasejs/ui/2.0.0/images/auth/google.svg"
            alt="Google logo"
            width="20"
            height="20"
          />
          Sign in with Google
        </button>

        <p style={{ marginTop: "24px", fontSize: "0.8rem", color: "#999" }}>
          IT3030 PAF Assignment 2026 — SLIIT
        </p>
      </div>
    </div>
  );
}

export default LoginPage;
