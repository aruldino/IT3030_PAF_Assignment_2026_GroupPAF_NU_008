import React, { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import api from '../services/api';
import AdminLayout from '../components/common/AdminLayout';

/**
 * Admin User Management Page — Member 4 responsibility.
 */
function UserManagementPage() {
  const { user } = useAuth();
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [updating, setUpdating] = useState(null);
  const [filter, setFilter] = useState('ALL');

  useEffect(() => {
    fetchUsers();
  }, []);

  async function fetchUsers() {
    try {
      const response = await api.get('/users');
      setUsers(response.data);
    } catch (err) {
      setError('Failed to load users.');
    } finally {
      setLoading(false);
    }
  }

  async function handleApprove(userId) {
    setUpdating(userId);
    try {
      const response = await api.put(`/users/${userId}/approve`);
      setUsers(users.map(u => u.id === userId ? response.data : u));
    } catch (err) {
      alert('Failed to approve user.');
    } finally {
      setUpdating(null);
    }
  }

  async function handleSuspend(userId) {
    if (!window.confirm('Suspend this user?')) return;
    setUpdating(userId);
    try {
      const response = await api.put(`/users/${userId}/suspend`);
      setUsers(users.map(u => u.id === userId ? response.data : u));
    } catch (err) {
      alert('Failed to suspend user.');
    } finally {
      setUpdating(null);
    }
  }

  async function handleReactivate(userId) {
    setUpdating(userId);
    try {
      const response = await api.put(`/users/${userId}/reactivate`);
      setUsers(users.map(u => u.id === userId ? response.data : u));
    } catch (err) {
      alert('Failed to reactivate user.');
    } finally {
      setUpdating(null);
    }
  }

  async function handleRoleChange(userId, newRole) {
    setUpdating(userId);
    try {
      await api.put(`/users/${userId}/role`, { role: newRole });
      setUsers(users.map(u => u.id === userId ? { ...u, role: newRole } : u));
    } catch (err) {
      alert('Failed to update role.');
    } finally {
      setUpdating(null);
    }
  }

  async function handleDelete(userId) {
    if (!window.confirm('Are you sure you want to remove this user?')) return;
    try {
      await api.delete(`/users/${userId}`);
      setUsers(users.filter(u => u.id !== userId));
    } catch (err) {
      alert('Failed to delete user.');
    }
  }

  const stats = {
    all: users.length,
    active: users.filter(u => u.status === 'ACTIVE').length,
    pending: users.filter(u => u.status === 'PENDING').length,
    suspended: users.filter(u => u.status === 'SUSPENDED').length,
  };

  const filteredUsers = filter === 'ALL' ? users :
    users.filter(u => u.status === filter);

  const pendingCount = stats.pending;

  function getStatusBadge(status) {
    const styles = {
      ACTIVE: { backgroundColor: '#d4edda', color: '#155724' },
      PENDING: { backgroundColor: '#fff3cd', color: '#856404' },
      SUSPENDED: { backgroundColor: '#f8d7da', color: '#721c24' },
    };
    return (
      <span style={{
        ...styles[status],
        padding: '3px 10px',
        borderRadius: '12px',
        fontSize: '0.75rem',
        fontWeight: '600'
      }}>
        {status}
      </span>
    );
  }

  if (loading) return <AdminLayout><div style={{ padding: '2rem' }}>Loading users...</div></AdminLayout>;
  if (error) return <AdminLayout><div style={{ padding: '2rem', color: 'red' }}>{error}</div></AdminLayout>;

  return (
    <AdminLayout>
      <div style={{ padding: '2rem' }}>

        {/* Header */}
        <h2 style={{ marginBottom: '0.25rem' }}>👥 User Management</h2>
        <p style={{ color: '#888', marginBottom: '1.5rem' }}>
          Manage user accounts, roles and access
        </p>

        {/* Warning Banner */}
        {pendingCount > 0 && (
          <div style={{
            backgroundColor: '#fffbeb',
            border: '1px solid #fcd34d',
            borderRadius: '8px',
            padding: '12px 16px',
            marginBottom: '1.5rem',
            display: 'flex',
            alignItems: 'center',
            gap: '10px'
          }}>
            <span style={{ fontSize: '1.2rem' }}>⚠️</span>
            <span style={{ color: '#92400e', fontWeight: '500' }}>
              {pendingCount} user{pendingCount > 1 ? 's are' : ' is'} waiting for approval
            </span>
            <button
              onClick={() => setFilter('PENDING')}
              style={{
                marginLeft: 'auto',
                padding: '4px 12px',
                backgroundColor: '#f59e0b',
                color: 'white',
                border: 'none',
                borderRadius: '6px',
                cursor: 'pointer',
                fontSize: '0.85rem',
                fontWeight: '600'
              }}
            >
              Review Now
            </button>
          </div>
        )}

        {/* Stats */}
        <div style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(4, 1fr)',
          gap: '1rem',
          marginBottom: '1.5rem'
        }}>
          {[
            { label: 'Total Users', value: stats.all, color: '#3182ce', bg: '#ebf8ff' },
            { label: 'Active', value: stats.active, color: '#38a169', bg: '#f0fff4' },
            { label: 'Pending', value: stats.pending, color: '#d69e2e', bg: '#fffff0' },
            { label: 'Suspended', value: stats.suspended, color: '#e53e3e', bg: '#fff5f5' },
          ].map(stat => (
            <div key={stat.label} style={{
              backgroundColor: stat.bg,
              borderRadius: '10px',
              padding: '1rem 1.25rem',
              borderLeft: `4px solid ${stat.color}`
            }}>
              <div style={{ fontSize: '1.8rem', fontWeight: '700', color: stat.color }}>
                {stat.value}
              </div>
              <div style={{ fontSize: '0.85rem', color: '#666', marginTop: '2px' }}>
                {stat.label}
              </div>
            </div>
          ))}
        </div>

        {/* Filter Chips */}
        <div style={{ display: 'flex', gap: '8px', marginBottom: '1.5rem' }}>
          {['ALL', 'ACTIVE', 'PENDING', 'SUSPENDED'].map(f => (
            <button
              key={f}
              onClick={() => setFilter(f)}
              style={{
                padding: '6px 16px',
                borderRadius: '20px',
                border: '2px solid',
                borderColor: filter === f ? '#1a73e8' : '#e2e8f0',
                backgroundColor: filter === f ? '#1a73e8' : 'white',
                color: filter === f ? 'white' : '#555',
                cursor: 'pointer',
                fontWeight: '500',
                fontSize: '0.85rem',
                transition: 'all 0.2s'
              }}
            >
              {f} {f === 'ALL' ? `(${stats.all})` :
                   f === 'ACTIVE' ? `(${stats.active})` :
                   f === 'PENDING' ? `(${stats.pending})` :
                   `(${stats.suspended})`}
            </button>
          ))}
        </div>

        {/* Table */}
        <div style={{
          backgroundColor: 'white',
          borderRadius: '10px',
          boxShadow: '0 1px 4px rgba(0,0,0,0.08)',
          overflow: 'hidden'
        }}>
          <table style={{ width: '100%', borderCollapse: 'collapse' }}>
            <thead>
              <tr style={{ backgroundColor: '#f8fafc' }}>
                <th style={th}>User</th>
                <th style={th}>Email</th>
                <th style={th}>Status</th>
                <th style={th}>Role</th>
                <th style={th}>Actions</th>
              </tr>
            </thead>
            <tbody>
              {filteredUsers.length === 0 ? (
                <tr>
                  <td colSpan={5} style={{ padding: '2rem', textAlign: 'center', color: '#888' }}>
                    No users found
                  </td>
                </tr>
              ) : (
                filteredUsers.map(u => (
                  <tr key={u.id} style={{
                    borderBottom: '1px solid #f0f0f0',
                    backgroundColor: u.status === 'PENDING' ? '#fffbeb' : 'white'
                  }}>
                    {/* User */}
                    <td style={td}>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                        {u.profilePicture ? (
                          <img src={u.profilePicture} alt="profile"
                            style={{ width: '36px', height: '36px', borderRadius: '50%' }} />
                        ) : (
                          <div style={{
                            width: '36px', height: '36px', borderRadius: '50%',
                            backgroundColor: '#3182ce', display: 'flex',
                            alignItems: 'center', justifyContent: 'center',
                            color: 'white', fontWeight: '600'
                          }}>
                            {u.name?.charAt(0) || '?'}
                          </div>
                        )}
                        <span style={{ fontWeight: '500' }}>{u.name || '—'}</span>
                      </div>
                    </td>

                    {/* Email */}
                    <td style={td}>{u.email}</td>

                    {/* Status */}
                    <td style={td}>{getStatusBadge(u.status || 'ACTIVE')}</td>

                    {/* Role */}
                    <td style={td}>
                      <select
                        value={u.role}
                        onChange={e => handleRoleChange(u.id, e.target.value)}
                        disabled={updating === u.id || u.email === user?.email}
                        style={{
                          padding: '4px 8px',
                          borderRadius: '4px',
                          border: '1px solid #ccc',
                          fontSize: '0.85rem'
                        }}
                      >
                        <option value="USER">USER</option>
                        <option value="ADMIN">ADMIN</option>
                        <option value="TECHNICIAN">TECHNICIAN</option>
                      </select>
                    </td>

                    {/* Actions */}
                    <td style={td}>
                      {u.email !== user?.email && (
                        <div style={{ display: 'flex', gap: '6px', flexWrap: 'wrap' }}>
                          {u.status === 'PENDING' && (
                            <button onClick={() => handleApprove(u.id)}
                              disabled={updating === u.id}
                              style={actionBtn('#38a169')}>
                              ✅ Approve
                            </button>
                          )}
                          {u.status === 'ACTIVE' && (
                            <button onClick={() => handleSuspend(u.id)}
                              disabled={updating === u.id}
                              style={actionBtn('#d69e2e')}>
                              ⏸ Suspend
                            </button>
                          )}
                          {u.status === 'SUSPENDED' && (
                            <button onClick={() => handleReactivate(u.id)}
                              disabled={updating === u.id}
                              style={actionBtn('#3182ce')}>
                              ▶ Reactivate
                            </button>
                          )}
                          <button onClick={() => handleDelete(u.id)}
                            disabled={updating === u.id}
                            style={actionBtn('#e53e3e')}>
                            🗑
                          </button>
                        </div>
                      )}
                      {updating === u.id && (
                        <span style={{ fontSize: '0.8rem', color: '#888' }}>saving...</span>
                      )}
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>
    </AdminLayout>
  );
}

function actionBtn(color) {
  return {
    padding: '4px 10px',
    backgroundColor: color,
    color: 'white',
    border: 'none',
    borderRadius: '4px',
    cursor: 'pointer',
    fontSize: '0.8rem',
    fontWeight: '500'
  };
}

const th = {
  padding: '12px 16px',
  textAlign: 'left',
  fontWeight: '600',
  borderBottom: '2px solid #eee',
  color: '#555',
  fontSize: '0.85rem'
};

const td = {
  padding: '12px 16px',
  verticalAlign: 'middle'
};

export default UserManagementPage;