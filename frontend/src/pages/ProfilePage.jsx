import React, { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import api from '../services/api';

/**
 * User Profile Page — Member 4 responsibility.
 * Shows logged-in user's details and allows name editing.
 */
function ProfilePage() {
  const { user, login } = useAuth();
  const [editing, setEditing] = useState(false);
  const [name, setName] = useState(user?.name || '');
  const [saving, setSaving] = useState(false);
  const [success, setSuccess] = useState(false);

  async function handleSave() {
    if (!name.trim()) return;
    setSaving(true);
    setSuccess(false);
    try {
      const response = await api.put('/users/profile', { name });
      // Update auth context with new name
      login({ ...user, name: response.data.name });
      setSuccess(true);
      setEditing(false);
    } catch (err) {
      alert('Failed to update name.');
    } finally {
      setSaving(false);
    }
  }

  function getRoleBadgeColor(role) {
    switch (role) {
      case 'ADMIN': return '#e53e3e';
      case 'TECHNICIAN': return '#d69e2e';
      default: return '#38a169';
    }
  }

  function formatDate(dateString) {
    if (!dateString) return '—';
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'long',
      day: 'numeric'
    });
  }

  if (!user) return null;

  return (
    <div style={{
      maxWidth: '500px',
      margin: '3rem auto',
      padding: '2rem',
      backgroundColor: 'white',
      borderRadius: '12px',
      boxShadow: '0 2px 12px rgba(0,0,0,0.1)'
    }}>
      {/* Profile Picture */}
      <div style={{ textAlign: 'center', marginBottom: '2rem' }}>
        {user.profilePicture ? (
          <img
            src={user.profilePicture}
            alt="profile"
            style={{
              width: '100px',
              height: '100px',
              borderRadius: '50%',
              border: '3px solid #3182ce'
            }}
          />
        ) : (
          <div style={{
            width: '100px',
            height: '100px',
            borderRadius: '50%',
            backgroundColor: '#3182ce',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            fontSize: '2.5rem',
            margin: '0 auto'
          }}>
            👤
          </div>
        )}

        {/* Role Badge */}
        <div style={{ marginTop: '1rem' }}>
          <span style={{
            backgroundColor: getRoleBadgeColor(user.role),
            color: 'white',
            padding: '4px 16px',
            borderRadius: '20px',
            fontSize: '0.85rem',
            fontWeight: '600'
          }}>
            {user.role}
          </span>
        </div>
      </div>

      {/* Profile Details */}
      <div style={{ display: 'flex', flexDirection: 'column', gap: '1.2rem' }}>

        {/* Name */}
        <div>
          <label style={{ fontWeight: '600', color: '#555', fontSize: '0.9rem' }}>
            Full Name
          </label>
          {editing ? (
            <input
              type="text"
              value={name}
              onChange={e => setName(e.target.value)}
              style={{
                width: '100%',
                padding: '8px 12px',
                marginTop: '4px',
                border: '2px solid #3182ce',
                borderRadius: '6px',
                fontSize: '1rem',
                boxSizing: 'border-box'
              }}
            />
          ) : (
            <p style={{ margin: '4px 0 0', fontSize: '1rem' }}>{user.name || '—'}</p>
          )}
        </div>

        {/* Email */}
        <div>
          <label style={{ fontWeight: '600', color: '#555', fontSize: '0.9rem' }}>
            Email
          </label>
          <p style={{ margin: '4px 0 0', fontSize: '1rem', color: '#888' }}>
            {user.email}
          </p>
        </div>

        {/* Role */}
        <div>
          <label style={{ fontWeight: '600', color: '#555', fontSize: '0.9rem' }}>
            Role
          </label>
          <p style={{ margin: '4px 0 0', fontSize: '1rem', color: '#888' }}>
            {user.role} — assigned by admin
          </p>
        </div>

        {/* Member Since */}
        <div>
          <label style={{ fontWeight: '600', color: '#555', fontSize: '0.9rem' }}>
            Member Since
          </label>
          <p style={{ margin: '4px 0 0', fontSize: '1rem', color: '#888' }}>
            {formatDate(user.createdAt)}
          </p>
        </div>

        {/* Action Buttons */}
        <div style={{ display: 'flex', gap: '1rem', marginTop: '0.5rem' }}>
          {editing ? (
            <>
              <button
                onClick={handleSave}
                disabled={saving}
                style={{
                  flex: 1,
                  padding: '10px',
                  backgroundColor: '#3182ce',
                  color: 'white',
                  border: 'none',
                  borderRadius: '6px',
                  cursor: 'pointer',
                  fontWeight: '600'
                }}
              >
                {saving ? 'Saving...' : 'Save Changes'}
              </button>
              <button
                onClick={() => { setEditing(false); setName(user.name || ''); }}
                style={{
                  flex: 1,
                  padding: '10px',
                  backgroundColor: '#e2e8f0',
                  color: '#333',
                  border: 'none',
                  borderRadius: '6px',
                  cursor: 'pointer',
                  fontWeight: '600'
                }}
              >
                Cancel
              </button>
            </>
          ) : (
            <button
              onClick={() => setEditing(true)}
              style={{
                flex: 1,
                padding: '10px',
                backgroundColor: '#3182ce',
                color: 'white',
                border: 'none',
                borderRadius: '6px',
                cursor: 'pointer',
                fontWeight: '600'
              }}
            >
              ✏️ Edit Name
            </button>
          )}
        </div>

        {success && (
          <p style={{ color: '#38a169', textAlign: 'center', fontWeight: '600' }}>
            ✅ Name updated successfully!
          </p>
        )}
      </div>
    </div>
  );
}

export default ProfilePage;