import React from 'react';
import { Link } from 'react-router-dom';

/**
 * Home page — entry point for all users.
 * Displays feature cards for all four modules.
 */
function HomePage() {
  const features = [
    {
      icon: '🏛️',
      title: 'Resource Catalogue',
      description:
        'Browse and manage campus facilities — lecture halls, labs, meeting rooms, and equipment.',
      // link: '/resources',  // Uncomment when Member 1 builds the page
      link: '#',
      member: 'Member 1',
    },
    {
      icon: '📅',
      title: 'Booking Management',
      description:
        'Book campus resources, track approval status, and check in with QR codes.',
      // link: '/bookings',  // Uncomment when Member 2 builds the page
      link: '#',
      member: 'Member 2',
    },
    {
      icon: '🔧',
      title: 'Incident Tickets',
      description:
        'Report maintenance issues, upload photos, and track technician progress in real time.',
      // link: '/tickets',  // Uncomment when Member 3 builds the page
      link: '#',
      member: 'Member 3',
    },
    {
      icon: '🔔',
      title: 'Notifications',
      description:
        'Stay updated with booking approvals, ticket assignments, and system announcements.',
      // link: '/notifications',  // Uncomment when Member 4 builds the page
      link: '#',
      member: 'Member 4',
    },
  ];

  return (
    <div>
      <div className="hero">
        <h1>🏫 Smart Campus Operations Hub</h1>
        <p>
          Your one-stop platform for managing campus facilities, bookings, incident
          tickets, and notifications.
        </p>
      </div>

      <div className="feature-grid">
        {features.map((feature) => (
          <Link key={feature.title} to={feature.link} className="feature-card">
            <div className="feature-card-icon">{feature.icon}</div>
            <h3>{feature.title}</h3>
            <p>{feature.description}</p>
            <small style={{ color: '#999', marginTop: '8px', display: 'block' }}>
              {feature.member}
            </small>
          </Link>
        ))}
      </div>
    </div>
  );
}

export default HomePage;
