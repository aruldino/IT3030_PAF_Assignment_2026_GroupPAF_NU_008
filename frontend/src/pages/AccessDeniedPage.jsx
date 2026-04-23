import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

/**
 * Shown to users whose accounts have been suspended.
 * Member 4 responsibility.
 */
function AccessDeniedPage() {
  const { user, login, logout } = useAuth();
  const navigate = useNavigate();
  const [checking, setChecking] = useState(false);
  const backendUrl =
    import.meta.env.VITE_BACKEND_URL || "http://localhost:8080";

  // Auto check every 10 seconds if reactivated
  useEffect(() => {
    const interval = setInterval(async () => {
      try {
        const token = localStorage.getItem("token");
        if (!token) return;

        const response = await fetch(`${backendUrl}/api/auth/me`, {
          headers: { Authorization: `Bearer ${token}` },
        });

        if (response.ok) {
          const updatedUser = await response.json();
          if (updatedUser.status === "ACTIVE") {
            login({ ...updatedUser, token });
            navigate("/");
          }
        }
      } catch (err) {
        // Keep waiting
      }
    }, 10000);

    return () => clearInterval(interval);
  }, []);

  async function handleCheckNow() {
    setChecking(true);
    try {
      const token = localStorage.getItem("token");
      const response = await fetch(`${backendUrl}/api/auth/me`, {
        headers: { Authorization: `Bearer ${token}` },
      });

      if (response.ok) {
        const updatedUser = await response.json();
        if (updatedUser.status === "ACTIVE") {
          login({ ...updatedUser, token });
          navigate("/");
        } else {
          alert("Account is still suspended.");
        }
      } else {
        alert("Account is still suspended.");
      }
    } catch (err) {
      alert("Error checking status.");
    } finally {
      setChecking(false);
    }
  }

  function handleLogout() {
    logout();
    navigate("/login");
  }

  return (
    <div
      style={{
        minHeight: "100vh",
        backgroundColor: "#f8fafc",
        display: "flex",
        flexDirection: "column",
        alignItems: "center",
        justifyContent: "center",
        padding: "2rem",
      }}
    >
      <div
        style={{
          backgroundColor: "white",
          borderRadius: "16px",
          padding: "3rem",
          maxWidth: "480px",
          width: "100%",
          boxShadow: "0 4px 24px rgba(0,0,0,0.08)",
          textAlign: "center",
        }}
      >
        <div style={{ fontSize: "4rem", marginBottom: "1rem" }}>🚫</div>

        <h2
          style={{
            color: "#e53e3e",
            marginBottom: "0.5rem",
            fontSize: "1.5rem",
          }}
        >
          Account Suspended
        </h2>

        <p style={{ color: "#666", lineHeight: "1.6", marginBottom: "1.5rem" }}>
          Your account has been suspended by an administrator. You no longer
          have access to Smart Campus Hub.
        </p>

        {/* User Info */}
        {user && (
          <div
            style={{
              backgroundColor: "#fff5f5",
              borderRadius: "10px",
              padding: "1rem",
              marginBottom: "1.5rem",
              display: "flex",
              alignItems: "center",
              gap: "12px",
            }}
          >
            {user.profilePicture ? (
              <img
                src={user.profilePicture}
                alt="profile"
                style={{ width: "44px", height: "44px", borderRadius: "50%" }}
              />
            ) : (
              <div
                style={{
                  width: "44px",
                  height: "44px",
                  borderRadius: "50%",
                  backgroundColor: "#e53e3e",
                  display: "flex",
                  alignItems: "center",
                  justifyContent: "center",
                  color: "white",
                  fontWeight: "600",
                  fontSize: "1.2rem",
                }}
              >
                {user.name?.charAt(0) || "?"}
              </div>
            )}
            <div style={{ textAlign: "left" }}>
              <div style={{ fontWeight: "600", fontSize: "0.95rem" }}>
                {user.name}
              </div>
              <div style={{ color: "#888", fontSize: "0.85rem" }}>
                {user.email}
              </div>
            </div>
          </div>
        )}

        {/* Info box */}
        <div
          style={{
            backgroundColor: "#fff5f5",
            border: "1px solid #feb2b2",
            borderRadius: "10px",
            padding: "1rem",
            marginBottom: "1.5rem",
            textAlign: "left",
          }}
        >
          <p
            style={{
              fontWeight: "600",
              color: "#c53030",
              marginBottom: "0.5rem",
              fontSize: "0.9rem",
            }}
          >
            ❌ What does this mean?
          </p>
          <ul
            style={{
              color: "#742a2a",
              fontSize: "0.85rem",
              paddingLeft: "1.2rem",
              margin: 0,
              lineHeight: "1.8",
            }}
          >
            <li>Your access to all features has been revoked</li>
            <li>This page checks automatically every 10 seconds</li>
            <li>You'll be redirected automatically if reactivated</li>
          </ul>
        </div>

        {/* Auto checking indicator */}
        <div
          style={{ fontSize: "0.8rem", color: "#888", marginBottom: "1rem" }}
        >
          🔄 Checking account status automatically...
        </div>

        {/* Check Now Button */}
        <button
          onClick={handleCheckNow}
          disabled={checking}
          style={{
            width: "100%",
            padding: "12px",
            backgroundColor: "#3182ce",
            color: "white",
            border: "none",
            borderRadius: "8px",
            cursor: checking ? "not-allowed" : "pointer",
            fontWeight: "600",
            fontSize: "0.95rem",
            marginBottom: "0.75rem",
            opacity: checking ? 0.7 : 1,
          }}
        >
          {checking ? "Checking..." : "🔍 Check Now"}
        </button>

        <button
          onClick={handleLogout}
          style={{
            width: "100%",
            padding: "12px",
            backgroundColor: "#f1f3f4",
            color: "#333",
            border: "none",
            borderRadius: "8px",
            cursor: "pointer",
            fontWeight: "600",
            fontSize: "0.95rem",
          }}
        >
          Sign Out
        </button>
      </div>
    </div>
  );
}

export default AccessDeniedPage;
