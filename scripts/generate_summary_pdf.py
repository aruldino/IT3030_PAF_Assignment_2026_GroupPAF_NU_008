from __future__ import annotations

from pathlib import Path
from textwrap import wrap


OUTPUT = Path(__file__).resolve().parents[1] / "SmartCampusSummary.pdf"


def build_lines() -> list[str]:
    sections = [
        ("Smart Campus Operations Hub", [
            "Spring Boot backend + React frontend",
            "A modular campus management platform for resources, bookings, maintenance, notifications, and role-based access",
        ]),
        ("Project Overview", [
            "The system centralizes campus operations in one responsive dashboard.",
            "It follows layered architecture with controller, service, repository, DTO, and global exception handling.",
            "The codebase is built to look production-inspired, scalable, and presentation-ready for the PAF assignment.",
        ]),
        ("Member 1 - Facilities Catalogue and Resource Management", [
            "Resource CRUD for lecture halls, labs, meeting rooms, and equipment",
            "Search and filters by type, capacity, location, and status",
            "Availability endpoint, smart suggestions, CSV import, and analytics",
            "Status lifecycle: ACTIVE, OUT_OF_SERVICE, MAINTENANCE",
        ]),
        ("Member 2 - Booking Workflow and Conflict Checking", [
            "Booking requests with date, time, purpose, and attendees",
            "Workflow: PENDING -> APPROVED / REJECTED -> CANCELLED",
            "Conflict detection for overlapping resource bookings",
            "Booking history, approval/rejection flow, and auto-expiry for pending bookings",
        ]),
        ("Member 3 - Incident Tickets and Technician Updates", [
            "Ticket creation with category, description, priority, and optional attachments",
            "Workflow: OPEN -> IN_PROGRESS -> RESOLVED -> CLOSED",
            "Technician assignment, SLA tracking, and resolution timestamps",
            "Comment ownership rules, edit/delete support, and max 3 attachments",
        ]),
        ("Member 4 - Notifications, Roles, and Authentication", [
            "Notifications panel, mark-as-read, and notification preferences",
            "Role system: USER, ADMIN, TECHNICIAN, SUPER_ADMIN",
            "Session-based secure login with OAuth-compatible route naming",
            "Frontend polling for near real-time notification updates",
        ]),
        ("Key APIs", [
            "Resources: /resources, /resources/{id}/availability, /resources/suggestions, /resources/analytics/most-booked",
            "Bookings: /bookings, /bookings/my, /bookings/conflicts, /bookings/history",
            "Tickets: /tickets, /tickets/{id}/assign, /tickets/{id}/status, /tickets/{id}/comments, /tickets/{id}/sla",
            "Auth and Users: /auth/login, /auth/login/oauth, /users/me, /users/role",
            "Notifications: /notifications, /notifications/preferences, /notifications/{id}/read",
        ]),
        ("Front End", [
            "Responsive React UI with dashboard cards, forms, tables, and workflow status badges",
            "Fixed top navigation bar, alert toasts, notification badge, and alert history panel",
            "Smart search, CSV upload, booking queue, and ticket queue views",
        ]),
        ("Quality and CI/CD", [
            "Global validation and exception handling",
            "GitHub Actions workflow for backend tests and frontend build",
            "Clean REST naming, proper HTTP codes, and maintainable architecture",
        ]),
        ("Conclusion", [
            "This project demonstrates a real-world Smart Campus Operations Hub suitable for the PAF assignment.",
            "It is modular, secure, innovative, and ready for report and viva presentation.",
        ]),
    ]

    lines: list[str] = []
    for title, bullets in sections:
        lines.append(title)
        for bullet in bullets:
            wrapped = wrap(f"- {bullet}", width=92)
            lines.extend(wrapped or [""])
        lines.append("")
    return lines


def escape_pdf_text(text: str) -> str:
    return text.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)")


def make_pdf(lines: list[str]) -> bytes:
    width = 595
    height = 842
    margin_left = 50
    top = 790
    line_height = 16
    bottom = 50
    max_lines = (top - bottom) // line_height

    pages: list[list[str]] = []
    current: list[str] = []
    for line in lines:
        if len(current) >= max_lines or (line == "" and current and current[-1] == ""):
            pages.append(current)
            current = []
        current.append(line)
    if current:
        pages.append(current)

    objects: list[bytes] = []
    # 1 catalog, 2 pages, then for each page: page + content
    page_obj_numbers = []
    content_obj_numbers = []

    def add_object(body: str | bytes) -> int:
        if isinstance(body, str):
            body = body.encode("utf-8")
        objects.append(body)
        return len(objects)

    catalog_num = add_object("")
    pages_num = add_object("")
    for _ in pages:
        page_obj_numbers.append(add_object(""))
        content_obj_numbers.append(add_object(""))

    font_num = add_object("<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>")

    for idx, page_lines in enumerate(pages):
        y = top
        stream_lines = ["BT", "/F1 12 Tf"]
        if idx == 0:
            stream_lines = ["BT", "/F1 18 Tf"]
        for line_index, line in enumerate(page_lines):
            if not line:
                y -= line_height
                continue
            font_size = 18 if idx == 0 and line_index == 0 else (14 if line and not line.startswith("- ") and not line.startswith("  ") else 12)
            if idx == 0 and line_index == 0:
                stream_lines.append(f"/F1 {font_size} Tf")
                stream_lines.append(f"1 0 0 1 {margin_left} {y} Tm ({escape_pdf_text(line)}) Tj")
            else:
                if not line.startswith("- ") and line and not line.startswith("  "):
                    stream_lines.append(f"/F1 14 Tf")
                    stream_lines.append(f"1 0 0 1 {margin_left} {y} Tm ({escape_pdf_text(line)}) Tj")
                else:
                    stream_lines.append(f"/F1 11 Tf")
                    stream_lines.append(f"1 0 0 1 {margin_left + 12} {y} Tm ({escape_pdf_text(line)}) Tj")
            y -= line_height
            if line.startswith("- ") and line.endswith(":"):
                y -= 2
        stream_lines.append("ET")
        stream = "\n".join(stream_lines).encode("utf-8")
        content_obj_num = content_obj_numbers[idx]
        page_obj_num = page_obj_numbers[idx]
        objects[content_obj_num - 1] = b"<< /Length %d >>\nstream\n%s\nendstream" % (len(stream), stream)
        objects[page_obj_num - 1] = (
            f"<< /Type /Page /Parent {pages_num} 0 R /MediaBox [0 0 {width} {height}] "
            f"/Resources << /Font << /F1 {font_num} 0 R >> >> /Contents {content_obj_num} 0 R >>"
        ).encode("utf-8")

    kids = " ".join(f"{num} 0 R" for num in page_obj_numbers)
    objects[pages_num - 1] = f"<< /Type /Pages /Kids [ {kids} ] /Count {len(page_obj_numbers)} >>".encode("utf-8")
    objects[catalog_num - 1] = f"<< /Type /Catalog /Pages {pages_num} 0 R >>".encode("utf-8")

    pdf = bytearray()
    pdf.extend(b"%PDF-1.4\n")
    offsets = [0]
    for idx, body in enumerate(objects, start=1):
        offsets.append(len(pdf))
        pdf.extend(f"{idx} 0 obj\n".encode("utf-8"))
        pdf.extend(body)
        pdf.extend(b"\nendobj\n")

    xref_offset = len(pdf)
    pdf.extend(f"xref\n0 {len(objects) + 1}\n".encode("utf-8"))
    pdf.extend(b"0000000000 65535 f \n")
    for offset in offsets[1:]:
        pdf.extend(f"{offset:010d} 00000 n \n".encode("utf-8"))
    pdf.extend(
        (
            f"trailer\n<< /Size {len(objects) + 1} /Root {catalog_num} 0 R >>\n"
            f"startxref\n{xref_offset}\n%%EOF\n"
        ).encode("utf-8")
    )
    return bytes(pdf)


def main() -> None:
    lines = build_lines()
    OUTPUT.write_bytes(make_pdf(lines))
    print(f"Wrote {OUTPUT}")


if __name__ == "__main__":
    main()
