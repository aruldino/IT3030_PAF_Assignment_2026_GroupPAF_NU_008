// @ts-nocheck
export default function AnnouncementsList({ announcements, currentUser, onEditAnnouncement, onDeleteAnnouncement }) {
  const isAdmin = currentUser?.role === "ADMIN";

  const formatDate = (dateString) => {
    const date = new Date(dateString);
    return {
      day: date.getDate(),
      month: date.toLocaleDateString("en-US", { month: "short" }),
      time: date.toLocaleTimeString("en-US", { hour: "2-digit", minute: "2-digit" }),
    };
  };

  return (
    <div className="announcements-section">
      <div className="announcements-header">
        <h2>Announcements</h2>
      </div>
      {announcements && announcements.length > 0 ? (
        <div className="announcements-list">
          {announcements.map((announcement) => {
            const { day, month, time } = formatDate(announcement.createdAt || new Date());
            return (
              <div key={announcement.id} className="announcement-card">
                <div className="announcement-date">
                  <div className="day">{day}</div>
                  <div className="month-time">
                    {month} <br /> {time}
                  </div>
                </div>
                <div className="announcement-content">
                  <h3>{announcement.title}</h3>
                  <p>{announcement.description || announcement.content}</p>
                  <div className="announcement-footer">
                    <div className="announcement-author">
                      By {announcement.author || "System"}
                    </div>
                    {isAdmin && (
                      <div className="announcement-actions">
                        <button
                          type="button"
                          className="secondary-button announcement-edit-button"
                          onClick={() => onEditAnnouncement?.(announcement)}
                        >
                          Edit
                        </button>
                        <button
                          type="button"
                          className="danger-button announcement-delete-button"
                          onClick={() => onDeleteAnnouncement?.(announcement.id)}
                        >
                          Delete
                        </button>
                      </div>
                    )}
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      ) : (
        <div className="empty-state">
          <p>No announcements at the moment</p>
        </div>
      )}
    </div>
  );
}


