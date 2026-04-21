import { useState } from "react";

type UserProfile = {
  id: number;
  fullName: string;
  email: string;
  role: string;
};

interface HeaderProps {
  currentUser: UserProfile | null;
  onLogout: () => void;
  onDeleteAccount: () => void;
  notificationCount?: number;
  sidebarOpen?: boolean;
  setSidebarOpen?: (value: boolean) => void;
  onAlertsClick?: () => void;
}

export default function Header({ currentUser, onLogout, onDeleteAccount, notificationCount = 0, sidebarOpen = false, setSidebarOpen, onAlertsClick }: HeaderProps) {
  const [showDropdown, setShowDropdown] = useState(false);
  const displayRole =
    currentUser?.role === "USER" || currentUser?.role === "STUDENT"
      ? "Student"
      : currentUser?.role === "STAFF"
        ? "Staff"
        : currentUser?.role === "TECHNICIAN"
          ? "Technician"
          : currentUser?.role === "SUPER_ADMIN"
            ? "Super Admin"
            : currentUser?.role ?? "";

  return (
    <header className="app-header">
      <div className="header-container">
        {/* Mobile Menu Toggle */}
        <button
          type="button"
          className="menu-toggle"
          onClick={() => setSidebarOpen && setSidebarOpen(!sidebarOpen)}
          aria-label="Toggle sidebar menu"
        >
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
            <line x1="3" y1="12" x2="21" y2="12" />
            <line x1="3" y1="6" x2="21" y2="6" />
            <line x1="3" y1="18" x2="21" y2="18" />
          </svg>
        </button>

        {/* Logo and Website Name */}
        <div className="header-brand">
          <div className="logo">
            <svg viewBox="0 0 100 100" xmlns="http://www.w3.org/2000/svg">
              <circle cx="50" cy="50" r="48" fill="none" stroke="#6b7280" strokeWidth="6" />
              <rect x="28" y="30" width="10" height="40" fill="#4b5563" rx="2" />
              <rect x="45" y="20" width="10" height="50" fill="#6b7280" rx="2" />
              <rect x="62" y="35" width="10" height="35" fill="#9ca3af" rx="2" />
            </svg>
          </div>
          <div className="brand-text">
            <h1>UniHub</h1>
            <p>Smart Campus</p>
          </div>
        </div>

        {/* User Profile and Logout */}
        {currentUser && (
          <div className="header-user">
            <button 
              type="button"
              className="badge badge-light notification-badge" 
              aria-label={`${notificationCount} unread notifications`}
              onClick={onAlertsClick}
            >
              Notifications {notificationCount}
            </button>
            <button
              type="button"
              className="user-profile"
              onClick={() => setShowDropdown(!showDropdown)}
              aria-haspopup="menu"
              aria-expanded={showDropdown}
              aria-label={`Open account menu for ${currentUser.fullName}`}
            >
              <div className="user-avatar">
                {currentUser.fullName.charAt(0).toUpperCase()}
              </div>
              <div className="user-info">
                <div className="user-name">{currentUser.fullName}</div>
                <div className="user-role">{displayRole}</div>
              </div>
              <svg className={`dropdown-icon ${showDropdown ? "active" : ""}`} viewBox="0 0 20 20" fill="currentColor" aria-hidden="true">
                <path fillRule="evenodd" d="M5.293 7.293a1 1 0 011.414 0L10 10.586l3.293-3.293a1 1 0 111.414 1.414l-4 4a1 1 0 01-1.414 0l-4-4a1 1 0 010-1.414z" clipRule="evenodd" />
              </svg>
            </button>

            {/* Dropdown Menu */}
            {showDropdown && (
              <div className="dropdown-menu">
                <div className="dropdown-item user-details">
                  <div className="detail-label">Email</div>
                  <div className="detail-value">{currentUser.email}</div>
                </div>
                <hr className="dropdown-divider" />
                <button
                  type="button"
                  className="dropdown-item logout-btn"
                  onClick={() => {
                    setShowDropdown(false);
                    onLogout();
                  }}
                >
                  <svg viewBox="0 0 20 20" fill="currentColor">
                    <path fillRule="evenodd" d="M3 3a1 1 0 00-1 1v12a1 1 0 102 0V4a1 1 0 00-1-1zm7.707 5.293a1 1 0 010 1.414L9.414 10l1.293 1.293a1 1 0 01-1.414 1.414l-2-2a1 1 0 010-1.414l2-2a1 1 0 011.414 0z" clipRule="evenodd" />
                    <path fillRule="evenodd" d="M12.5 2a1 1 0 011 1v12a1 1 0 11-2 0V3a1 1 0 011-1z" clipRule="evenodd" />
                  </svg>
                  Logout
                </button>
                <hr className="dropdown-divider" />
                <button
                  type="button"
                  className="dropdown-item danger-link"
                  onClick={() => {
                    setShowDropdown(false);
                    onDeleteAccount();
                  }}
                >
                  Delete My Account
                </button>
              </div>
            )}
          </div>
        )}
      </div>
    </header>
  );
}
