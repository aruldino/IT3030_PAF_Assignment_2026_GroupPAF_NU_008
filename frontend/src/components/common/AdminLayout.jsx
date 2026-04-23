import React from 'react';
import { Link, useLocation } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';

/**
 * Admin Layout with sidebar — Member 4 responsibility.
 * Wraps all admin pages with a consistent sidebar navigation.
 */
function AdminLayout({ children }) {
  const location = useLocation();
  const { user } = useAuth();

  const menuItems = [
    {
      icon: '👥',
      label: 'User Management',
      path: '/admin/users',
      active: true,
      member: 'Member 4'
    },
    {
      icon: '🏛️',
      label: 'Resource Management',
      path: '/admin/resources',
      active: false,
      member: 'Member 1'
    },
    {
      icon: '📅',
      label: 'Booking Management',
      path: '/admin/bookings',
      active: false,
      member: 'Member 2'
    },
    {
      icon: '🔧',
      label: 'Ticket Management',
      path: '/admin/tickets',
      active: false,
      member: 'Member 3'
    },
    {
      icon: '🔔',
      label: 'Notification Management',
      path: '/admin/notifications',
      active: false,
      member: 'Member 4'
    },
  ];

  return (
    <div style={{ display: 'flex', minHeight: 'calc(100vh - 60px)' }}>

      {/* Sidebar */}
      <div style={{
        width: '260px',
        minWidth: '260px',
        backgroundColor: '#1e293b',
        color: 'white',
        display: 'flex',
        flexDirection: 'column',
        padding: '0',
      }}>

        {/* Admin Info */}
        <div style={{
          padding: '24px 20px',
          borderBottom: '1px solid rgba(255,255,255,0.1)',
          display: 'flex',
          alignItems: 'center',
          gap: '12px'
        }}>
          {user?.profilePicture ? (
            <img
              src={user.profilePicture}
              alt="admin"
              style={{ width: '42px', height: '42px', borderRadius: '50%' }}
            />
          ) : (
            <div style={{
              width: '42px', height: '42px', borderRadius: '50%',
              backgroundColor: '#3182ce', display: 'flex',
              alignItems: 'center', justifyContent: 'center', fontSize: '1.2rem'
            }}>👤</div>
          )}
          <div>
            <div style={{ fontWeight: '600', fontSize: '0.9rem' }}>
              {user?.name || 'Admin'}
            </div>
            <div style={{
              fontSize: '0.75rem',
              color: '#e53e3e',
              backgroundColor: 'rgba(229,62,62,0.15)',
              padding: '1px 8px',
              borderRadius: '10px',
              display: 'inline-block',
              marginTop: '2px'
            }}>
              ADMIN
            </div>
          </div>
        </div>

        {/* Menu Label */}
        <div style={{
          padding: '16px 20px 8px',
          fontSize: '0.7rem',
          color: 'rgba(255,255,255,0.4)',
          letterSpacing: '1px',
          textTransform: 'uppercase'
        }}>
          Management
        </div>

        {/* Menu Items */}
        <nav style={{ flex: 1 }}>
          {menuItems.map(item => {
            const isCurrentPage = location.pathname === item.path;
            return (
              <div key={item.path}>
                {item.active ? (
                  <Link
                    to={item.path}
                    style={{
                      display: 'flex',
                      alignItems: 'center',
                      gap: '12px',
                      padding: '12px 20px',
                      color: isCurrentPage ? 'white' : 'rgba(255,255,255,0.7)',
                      textDecoration: 'none',
                      backgroundColor: isCurrentPage ? 'rgba(255,255,255,0.1)' : 'transparent',
                      borderLeft: isCurrentPage ? '3px solid #3182ce' : '3px solid transparent',
                      transition: 'all 0.2s',
                      fontSize: '0.9rem'
                    }}
                    onMouseEnter={e => {
                      if (!isCurrentPage) e.currentTarget.style.backgroundColor = 'rgba(255,255,255,0.05)';
                    }}
                    onMouseLeave={e => {
                      if (!isCurrentPage) e.currentTarget.style.backgroundColor = 'transparent';
                    }}
                  >
                    <span style={{ fontSize: '1.1rem' }}>{item.icon}</span>
                    <span>{item.label}</span>
                  </Link>
                ) : (
                  <div
                    style={{
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'space-between',
                      padding: '12px 20px',
                      color: 'rgba(255,255,255,0.3)',
                      cursor: 'not-allowed',
                      fontSize: '0.9rem'
                    }}
                  >
                    <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                      <span style={{ fontSize: '1.1rem' }}>{item.icon}</span>
                      <span>{item.label}</span>
                    </div>
                    <span style={{
                      fontSize: '0.65rem',
                      backgroundColor: 'rgba(255,255,255,0.1)',
                      padding: '2px 6px',
                      borderRadius: '8px',
                      color: 'rgba(255,255,255,0.4)'
                    }}>
                      {item.member}
                    </span>
                  </div>
                )}
              </div>
            );
          })}
        </nav>

        {/* Bottom */}
        <div style={{
          padding: '16px 20px',
          borderTop: '1px solid rgba(255,255,255,0.1)',
          fontSize: '0.75rem',
          color: 'rgba(255,255,255,0.3)'
        }}>
          Smart Campus Hub © 2026
        </div>
      </div>

      {/* Main Content */}
      <div style={{ flex: 1, backgroundColor: '#f8fafc', overflow: 'auto' }}>
        {children}
      </div>
    </div>
  );
}

export default AdminLayout;