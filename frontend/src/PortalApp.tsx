import { useEffect, useRef, useState, type FormEvent, type RefObject } from "react";
import campusHero from "./assets/campus-hero.svg";
import { StatCard } from "./shared/components/StatCard";
import Header from "./components/Header";
import AnnouncementsList from "./components/AnnouncementsList";
import "./portal.css";

const API_BASE = "/api";
const resourceTypes = ["LECTURE_HALL", "LAB", "MEETING_ROOM", "EQUIPMENT"];
const resourceStatuses = ["ACTIVE", "OUT_OF_SERVICE", "MAINTENANCE", "UNDER_MAINTENANCE"];
const bookingStatuses = ["PENDING", "APPROVED", "REJECTED", "CANCELLED"];
const maintenancePriorities = ["LOW", "MEDIUM", "HIGH", "CRITICAL"];
const maintenanceStatuses = ["OPEN", "IN_PROGRESS", "RESOLVED", "CLOSED"];
const departments = [
  "Faculty of Computing",
  "Faculty of Engineering",
  "Faculty of Business",
  "Faculty of Science",
  "Student Affairs",
  "Administration",
  "IT Support",
];
const academicYears = ["Year 1", "Year 2", "Year 3", "Year 4"];
const semesters = ["Semester 1", "Semester 2"];
const navItems = ["home", "summary", "dashboard", "resources", "bookings", "maintenance"];

function isValidPage(value: string | null | undefined): value is string {
  return Boolean(value && navItems.includes(value));
}

function getInitialPage() {
  if (typeof window === "undefined") {
    return "home";
  }

  try {
    const hashPage = window.location.hash.replace(/^#/, "");
    if (isValidPage(hashPage)) {
      return hashPage;
    }

    const savedPage = window.localStorage.getItem("smart-campus-active-page");
    if (isValidPage(savedPage)) {
      return savedPage;
    }
  } catch {
    // Ignore storage or hash access errors and use the default page.
  }

  return "home";
}

type Announcement = {
  id: number;
  title: string;
  content: string;
  author: string;
  createdAt: string;
};

type Notification = {
  id: number;
  title: string;
  message: string;
  category: string;
  read: boolean;
  createdAt: string;
};

type NotificationPreferences = {
  resourceAlerts: boolean;
  bookingAlerts: boolean;
  maintenanceAlerts: boolean;
  announcementAlerts: boolean;
};

type AlertState = { type: "success" | "error"; message: string } | null;
type AlertHistoryItem = { id: number; type: "success" | "error"; message: string; createdAt: string };

type Resource = {
  id: number;
  name: string;
  type: string;
  capacity: number;
  location: string;
  availabilityWindow: string;
  status: string;
  description: string;
};

type ResourceUsage = {
  resourceId: number;
  resourceName: string;
  bookingCount: number;
};

type Booking = {
  id: number;
  resource: Resource;
  requestedBy: string;
  department: string;
  academicYear?: string | null;
  semester?: string | null;
  bookingDate: string;
  startTime: string;
  endTime: string;
  purpose: string;
  expectedAttendees: number;
  status: string;
};

type MaintenanceTicket = {
  id: number;
  resource: Resource;
  issueType: string;
  description: string;
  reportedBy: string;
  priority: string;
  status: string;
  assignedTechnician?: string | null;
  createdAt?: string | null;
  assignedAt?: string | null;
  resolvedAt?: string | null;
};

type UserProfile = {
  id: number;
  fullName: string;
  email: string;
  role: string;
};

type ResourceForm = {
  name: string;
  type: string;
  capacity: string;
  location: string;
  availabilityWindow: string;
  status: string;
  description: string;
};

type BookingForm = {
  resourceId: string;
  requestedBy: string;
  department: string;
  academicYear: string;
  semester: string;
  bookingDate: string;
  startTime: string;
  endTime: string;
  purpose: string;
  expectedAttendees: string;
};

type TicketForm = {
  resourceId: string;
  issueType: string;
  description: string;
  reportedBy: string;
  priority: string;
};

type LoginForm = { email: string; password: string };
type RegisterForm = { fullName: string; email: string; password: string; confirmPassword: string; role: string };
type AnnouncementForm = { title: string; content: string };
type LoginErrors = Partial<Record<"email" | "password", string>>;
type RegisterErrors = Partial<Record<"fullName" | "email" | "password" | "confirmPassword" | "role", string>>;
type AuthErrors = { login: LoginErrors; register: RegisterErrors };
type UserDeleteResponse = { message: string };

type SummaryStats = {
  totalResources: number;
  activeResources: number;
  outOfServiceResources: number;
  pendingBookings: number;
  approvedBookings: number;
  openMaintenanceTickets: number;
  resolvedMaintenanceTickets: number;
};

type PdfSection = {
  title: string;
  lines: string[];
};

const emptyResource: ResourceForm = { name: "", type: "LECTURE_HALL", capacity: "", location: "", availabilityWindow: "", status: "ACTIVE", description: "" };
const emptyBooking: BookingForm = { resourceId: "", requestedBy: "", department: "", academicYear: "", semester: "", bookingDate: "", startTime: "", endTime: "", purpose: "", expectedAttendees: "" };
const emptyTicket: TicketForm = { resourceId: "", issueType: "", description: "", reportedBy: "", priority: "MEDIUM" };
const emptyLogin: LoginForm = { email: "", password: "" };
const emptyRegister: RegisterForm = { fullName: "", email: "", password: "", confirmPassword: "", role: "USER" };
const emptyAnnouncement: AnnouncementForm = { title: "", content: "" };

function sanitizePdfText(value: string) {
  return value
    .replace(/\\/g, "\\\\")
    .replace(/\(/g, "\\(")
    .replace(/\)/g, "\\)")
    .replace(/[^\x20-\x7E]/g, "?");
}

function wrapPdfText(value: string, maxLength = 88) {
  const words = value.split(/\s+/).filter(Boolean);
  const lines: string[] = [];
  let current = "";

  words.forEach((word) => {
    if (!current) {
      current = word;
      return;
    }

    if ((current + " " + word).length > maxLength) {
      lines.push(current);
      current = word;
    } else {
      current += ` ${word}`;
    }
  });

  if (current) {
    lines.push(current);
  }

  return lines.length > 0 ? lines : [value];
}

function buildPdfBytes(title: string, sections: PdfSection[]) {
  const width = 595;
  const height = 842;
  const marginLeft = 48;
  const top = 790;
  const lineHeight = 16;
  const bottom = 52;
  const maxLinesPerPage = Math.floor((top - bottom) / lineHeight);

  const lines: string[] = [];
  lines.push(title);
  lines.push(`Generated on ${new Date().toLocaleString()}`);
  lines.push("");

  sections.forEach((section) => {
    lines.push(section.title);
    section.lines.forEach((line) => {
      wrapPdfText(`- ${line}`).forEach((wrapped) => lines.push(wrapped));
    });
    lines.push("");
  });

  const pages: string[][] = [];
  let currentPage: string[] = [];
  lines.forEach((line) => {
    if (currentPage.length >= maxLinesPerPage) {
      pages.push(currentPage);
      currentPage = [];
    }
    currentPage.push(line);
  });
  if (currentPage.length > 0) {
    pages.push(currentPage);
  }

  const objects: string[] = [];
  const addObject = (content: string) => {
    objects.push(content);
    return objects.length;
  };

  const catalogNum = addObject("");
  const pagesNum = addObject("");
  const pageNums: number[] = [];
  const contentNums: number[] = [];
  pages.forEach(() => {
    pageNums.push(addObject(""));
    contentNums.push(addObject(""));
  });
  const fontNum = addObject("<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>");

  pages.forEach((page, pageIndex) => {
    let y = top;
    const contentParts: string[] = ["BT", "/F1 12 Tf"];
    page.forEach((line, lineIndex) => {
      if (!line) {
        y -= lineHeight;
        return;
      }

      const isTitle = pageIndex === 0 && lineIndex === 0;
      const isSection = line && !line.startsWith("- ");
      const fontSize = isTitle ? 18 : isSection ? 14 : 11;
      contentParts.push(`/F1 ${fontSize} Tf`);
      contentParts.push(`1 0 0 1 ${marginLeft + (isSection ? 0 : 10)} ${y} Tm (${sanitizePdfText(line)}) Tj`);
      y -= lineHeight;
    });
    contentParts.push("ET");
    const contentStream = contentParts.join("\n");
    const contentObjNum = contentNums[pageIndex] - 1;
    const pageObjNum = pageNums[pageIndex] - 1;
    objects[contentObjNum] = `<< /Length ${contentStream.length} >>\nstream\n${contentStream}\nendstream`;
    objects[pageObjNum] = `<< /Type /Page /Parent ${pagesNum} 0 R /MediaBox [0 0 ${width} ${height}] /Resources << /Font << /F1 ${fontNum} 0 R >> >> /Contents ${contentNums[pageIndex]} 0 R >>`;
  });

  const kids = pageNums.map((num) => `${num} 0 R`).join(" ");
  objects[pagesNum - 1] = `<< /Type /Pages /Kids [ ${kids} ] /Count ${pages.length} >>`;
  objects[catalogNum - 1] = `<< /Type /Catalog /Pages ${pagesNum} 0 R >>`;

  let pdf = "%PDF-1.4\n";
  const offsets = [0];
  objects.forEach((body, index) => {
    offsets.push(pdf.length);
    pdf += `${index + 1} 0 obj\n${body}\nendobj\n`;
  });

  const xrefOffset = pdf.length;
  pdf += `xref\n0 ${objects.length + 1}\n`;
  pdf += "0000000000 65535 f \n";
  offsets.slice(1).forEach((offset) => {
    pdf += `${String(offset).padStart(10, "0")} 00000 n \n`;
  });
  pdf += `trailer\n<< /Size ${objects.length + 1} /Root ${catalogNum} 0 R >>\nstartxref\n${xrefOffset}\n%%EOF\n`;

  return new TextEncoder().encode(pdf);
}

export default function PortalApp() {
  const [currentUser, setCurrentUser] = useState<UserProfile | null>(null);
  const [activePage, setActivePage] = useState(getInitialPage);
  const [authTab, setAuthTab] = useState("login");
  const [alert, setAlert] = useState<AlertState>(null);
  const [alertHistory, setAlertHistory] = useState<AlertHistoryItem[]>([]);
  const [booting, setBooting] = useState(true);
  const [loading, setLoading] = useState(false);
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const [summary, setSummary] = useState<SummaryStats | null>(null);
  const [resources, setResources] = useState<Resource[]>([]);
  const [resourceAnalytics, setResourceAnalytics] = useState<ResourceUsage[]>([]);
  const [resourceSearch, setResourceSearch] = useState("");
  const [resourceTypeFilter, setResourceTypeFilter] = useState("");
  const [resourceLocationFilter, setResourceLocationFilter] = useState("");
  const [resourceCapacityFilter, setResourceCapacityFilter] = useState("");
  const [resourceStatusFilter, setResourceStatusFilter] = useState("");
  const [resourceSuggestions, setResourceSuggestions] = useState<Resource[]>([]);
  const [resourceCsvFile, setResourceCsvFile] = useState<File | null>(null);
  const [bookings, setBookings] = useState<Booking[]>([]);
  const [tickets, setTickets] = useState<MaintenanceTicket[]>([]);
  const [users, setUsers] = useState<UserProfile[]>([]);
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [notificationPreferences, setNotificationPreferences] = useState<NotificationPreferences | null>(null);
  const [notificationPreferenceForm, setNotificationPreferenceForm] = useState<NotificationPreferences>({
    resourceAlerts: true,
    bookingAlerts: true,
    maintenanceAlerts: true,
    announcementAlerts: true,
  });
  const [resourceForm, setResourceForm] = useState<ResourceForm>(emptyResource);
  const [bookingForm, setBookingForm] = useState<BookingForm>(emptyBooking);
  const [ticketForm, setTicketForm] = useState<TicketForm>(emptyTicket);
  const [editingTicketId, setEditingTicketId] = useState<number | null>(null);
  const [loginForm, setLoginForm] = useState<LoginForm>(emptyLogin);
  const [registerForm, setRegisterForm] = useState<RegisterForm>(emptyRegister);
  const [authErrors, setAuthErrors] = useState<AuthErrors>({ login: {}, register: {} });
  const [announcementForm, setAnnouncementForm] = useState<AnnouncementForm>(emptyAnnouncement);
  const [editingAnnouncementId, setEditingAnnouncementId] = useState<number | null>(null);
  const [editingResourceId, setEditingResourceId] = useState<number | null>(null);
  const [bookingFilter, setBookingFilter] = useState("");
  const [ticketFilter, setTicketFilter] = useState("");
  const [announcements, setAnnouncements] = useState<Announcement[]>([]);
  const resourceFormRef = useRef<HTMLElement | null>(null);
  const resourceNameInputRef = useRef<HTMLInputElement | null>(null);
  const maintenanceFormRef = useRef<HTMLElement | null>(null);
  const maintenanceIssueInputRef = useRef<HTMLInputElement | null>(null);
  const announcementFormRef = useRef<HTMLElement | null>(null);
  const announcementTitleInputRef = useRef<HTMLInputElement | null>(null);
  const announcementContentInputRef = useRef<HTMLTextAreaElement | null>(null);
  const alertTimerRef = useRef<number | null>(null);

  useEffect(() => { bootstrap(); }, []);

  useEffect(() => {
    const handleHashChange = () => {
      const hashPage = window.location.hash.replace(/^#/, "");
      if (isValidPage(hashPage)) {
        setActivePage(hashPage);
      }
    };

    window.addEventListener("hashchange", handleHashChange);
    handleHashChange();
    return () => window.removeEventListener("hashchange", handleHashChange);
  }, []);

  useEffect(() => {
    if (!alert) {
      return;
    }

    setAlertHistory((current) => [
      {
        id: Date.now(),
        type: alert.type,
        message: alert.message,
        createdAt: new Date().toISOString(),
      },
      ...current,
    ].slice(0, 6));

    if (alertTimerRef.current) {
      window.clearTimeout(alertTimerRef.current);
    }

    alertTimerRef.current = window.setTimeout(() => {
      setAlert(null);
    }, 4000);

    return () => {
      if (alertTimerRef.current) {
        window.clearTimeout(alertTimerRef.current);
      }
    };
  }, [alert]);

  useEffect(() => {
    if (!currentUser) {
      return;
    }

    const refreshNotifications = () => {
      api<Notification[]>("/notifications", {}, true).then((data) => {
        if (data) {
          setNotifications(data);
        }
      });
    };

    refreshNotifications();
    const interval = window.setInterval(refreshNotifications, 15000);
    return () => window.clearInterval(interval);
  }, [currentUser]);

  async function bootstrap() {
    const profile = await api<UserProfile>("/auth/me", {}, true);
    if (profile) {
      setCurrentUser(profile);
      await loadWorkspace(profile.role);
    }
    setBooting(false);
  }

  async function api<T>(path: string, options: RequestInit = {}, quiet = false): Promise<T | null> {
    try {
      const headers: Record<string, string> = { ...(options.headers as Record<string, string> | undefined) };
      if (options.body) headers["Content-Type"] = "application/json";
      const response = await fetch(`${API_BASE}${path}`, { ...options, headers, credentials: "include" });
      const raw = response.status === 204 ? "" : await response.text();
      let data: unknown = null;
      if (raw) {
        try {
          data = JSON.parse(raw) as T;
        } catch {
          data = { message: raw };
        }
      }
      if (!response.ok) {
        if (response.status === 401) setCurrentUser(null);
        throw new Error(formatError(data));
      }
      return data as T;
    } catch (error) {
      if (!quiet) setAlert({ type: "error", message: error instanceof Error ? error.message : "Request failed" });
      return null;
    }
  }

  async function loadWorkspace(role?: string) {
    setLoading(true);
    const shouldLoadUsers = role === "SUPER_ADMIN" || currentUser?.role === "SUPER_ADMIN";
    const [summaryData, bookingData, ticketData, announcementData, notificationData, notificationPreferenceData, userData] = await Promise.all([
      api("/dashboard/summary", {}, true),
      api(`/bookings${bookingFilter ? `?status=${bookingFilter}` : ""}`, {}, true),
      api(`/maintenance${ticketFilter ? `?status=${ticketFilter}` : ""}`, {}, true),
      api("/announcements", {}, true),
      api("/notifications", {}, true),
      api<NotificationPreferences>("/notifications/preferences", {}, true),
      shouldLoadUsers ? api<UserProfile[]>("/users", {}, true) : Promise.resolve(null),
    ]);
    if (summaryData) setSummary(summaryData);
    if (bookingData) setBookings(bookingData);
    if (ticketData) setTickets(ticketData);
    if (announcementData) setAnnouncements(announcementData);
    if (notificationData) setNotifications(notificationData);
    if (notificationPreferenceData) {
      setNotificationPreferences(notificationPreferenceData);
      setNotificationPreferenceForm(notificationPreferenceData);
    }
    if (userData) setUsers(userData);
    if (!shouldLoadUsers) setUsers([]);
    await loadResourceWorkspace();
    setLoading(false);
  }

    async function loadResourceWorkspace() {
      const query = new URLSearchParams();
      if (resourceSearch.trim()) query.set("q", resourceSearch.trim());
      if (resourceTypeFilter) query.set("type", resourceTypeFilter);
      if (resourceLocationFilter) query.set("location", resourceLocationFilter);
      if (resourceCapacityFilter) query.set("capacity", resourceCapacityFilter);
      if (resourceStatusFilter) query.set("status", resourceStatusFilter);

    const [resourceData, analyticsData] = await Promise.all([
      api<Resource[]>(`/resources${query.toString() ? `?${query.toString()}` : ""}`, {}, true),
      api<ResourceUsage[]>("/resources/analytics/most-booked", {}, true),
    ]);

    if (resourceData) setResources(resourceData);
    if (analyticsData) setResourceAnalytics(analyticsData);
  }

    useEffect(() => {
      if (!currentUser) {
        return;
      }

      loadResourceWorkspace();
    }, [currentUser, resourceTypeFilter, resourceLocationFilter, resourceCapacityFilter, resourceStatusFilter, bookingFilter, ticketFilter]);

    useEffect(() => {
      if (!currentUser) {
        return;
      }

      const handle = window.setTimeout(() => {
        loadResourceWorkspace();
      }, 250);

      return () => window.clearTimeout(handle);
    }, [currentUser, resourceSearch]);

    useEffect(() => {
      if (!currentUser) {
        return;
      }

    const query = resourceSearch.trim();
    if (!query) {
      setResourceSuggestions([]);
      return;
    }

    const handle = window.setTimeout(async () => {
      const suggestions = await api<Resource[]>(`/resources/suggestions?q=${encodeURIComponent(query)}`, {}, true);
      if (suggestions) {
        setResourceSuggestions(suggestions);
      }
    }, 250);

    return () => window.clearTimeout(handle);
  }, [currentUser, resourceSearch]);

  useEffect(() => {
    if (currentUser) loadWorkspace();
  }, [bookingFilter, ticketFilter]);

  useEffect(() => {
    if (activePage !== "resources" || editingResourceId === null) {
      return;
    }

    resourceFormRef.current?.scrollIntoView({ behavior: "smooth", block: "start" });
    window.setTimeout(() => {
      resourceNameInputRef.current?.focus();
    }, 150);
  }, [activePage, editingResourceId]);

  useEffect(() => {
    if (activePage !== "dashboard" || editingAnnouncementId === null) {
      return;
    }

    announcementFormRef.current?.scrollIntoView({ behavior: "smooth", block: "start" });
    window.setTimeout(() => {
      announcementTitleInputRef.current?.focus();
      announcementTitleInputRef.current?.select();
    }, 150);
  }, [activePage, editingAnnouncementId]);

  useEffect(() => {
    if (activePage !== "maintenance" || editingTicketId === null) {
      return;
    }

    maintenanceFormRef.current?.scrollIntoView({ behavior: "smooth", block: "start" });
    window.setTimeout(() => {
      maintenanceIssueInputRef.current?.focus();
      maintenanceIssueInputRef.current?.select();
    }, 150);
  }, [activePage, editingTicketId]);

  useEffect(() => {
    try {
      window.localStorage.setItem("smart-campus-active-page", activePage);
      window.history.replaceState(null, "", `#${activePage}`);
    } catch {
      // Ignore storage errors.
    }
  }, [activePage]);

  useEffect(() => {
    setSidebarOpen(false);
  }, [activePage]);

  useEffect(() => {
    setAuthErrors({ login: {}, register: {} });
  }, [authTab]);

  function formatError(data: unknown) {
    if (typeof data === "object" && data !== null && "validationErrors" in data) {
      const errors = (data as { validationErrors?: Record<string, string> }).validationErrors;
      if (errors) return Object.values(errors).join(" ");
    }
    if (typeof data === "object" && data !== null && "message" in data) {
      return String((data as { message?: string }).message ?? "Something went wrong");
    }
    return "Something went wrong";
  }

  function pretty(value: string) {
    return value.replaceAll("_", " ").toLowerCase().replace(/\b\w/g, (char) => char.toUpperCase());
  }

function isEmail(value: string) {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value.trim());
}

function hasOnlyLettersAndSpaces(value: string) {
  return /^[A-Za-z ]+$/.test(value.trim());
}

function hasThreeLetters(value: string) {
  return value.trim().replace(/[^A-Za-z]/g, "").length >= 3;
}

function isStrongPassword(value: string) {
  return /^(?=.*[A-Z])(?=.*[a-z])(?=.*\d)(?=.*[^A-Za-z0-9]).{8,}$/.test(value);
}

  function validateLoginForm(form: LoginForm): LoginErrors {
    const errors: LoginErrors = {};
    if (!form.email.trim()) {
      errors.email = "Email is required.";
    } else if (!isEmail(form.email)) {
      errors.email = "Enter a valid email address.";
    }
    if (!form.password.trim()) {
      errors.password = "Password is required.";
    }
    return errors;
  }

  function validateRegisterForm(form: RegisterForm): RegisterErrors {
    const errors: RegisterErrors = {};
    if (!form.fullName.trim()) {
      errors.fullName = "Full name is required.";
    } else if (!hasOnlyLettersAndSpaces(form.fullName)) {
      errors.fullName = "Full name can contain only letters and spaces.";
    } else if (!hasThreeLetters(form.fullName)) {
      errors.fullName = "Full name must contain at least 3 letters.";
    }
    if (!form.email.trim()) {
      errors.email = "Email is required.";
    } else if (!isEmail(form.email)) {
      errors.email = "Enter a valid email address.";
    }
    if (!form.role) {
      errors.role = "Please choose a role.";
    }
    if (!form.password.trim()) {
      errors.password = "Password is required.";
    } else if (!isStrongPassword(form.password)) {
      errors.password = "Password must be 8+ characters with 1 uppercase, 1 lowercase, 1 number, and 1 symbol.";
    }
    if (!form.confirmPassword.trim()) {
      errors.confirmPassword = "Please confirm your password.";
    } else if (form.password !== form.confirmPassword) {
      errors.confirmPassword = "Passwords do not match.";
    }
    return errors;
  }

  async function handleLogin(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const errors = validateLoginForm(loginForm);
    if (Object.keys(errors).length > 0) {
      setAuthErrors((current) => ({ ...current, login: errors }));
      setAlert({ type: "error", message: "Please fix the login form fields." });
      return;
    }
    setAuthErrors((current) => ({ ...current, login: {} }));
    const user = await api<UserProfile>("/auth/login", { method: "POST", body: JSON.stringify(loginForm) });
    if (user) {
      setCurrentUser(user);
      setAlert({ type: "success", message: `Welcome ${user.fullName}` });
      await loadWorkspace(user.role);
    }
  }

  async function handleRegister(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const errors = validateRegisterForm(registerForm);
    if (Object.keys(errors).length > 0) {
      setAuthErrors((current) => ({ ...current, register: errors }));
      setAlert({ type: "error", message: "Please fix the registration form fields." });
      return;
    }
    setAuthErrors((current) => ({ ...current, register: {} }));
    const user = await api<UserProfile>("/auth/register", { method: "POST", body: JSON.stringify({ fullName: registerForm.fullName, email: registerForm.email, password: registerForm.password, role: registerForm.role }) });
    if (user) {
      setCurrentUser(user);
      setAlert({ type: "success", message: "Account created successfully." });
      await loadWorkspace(user.role);
    }
  }

  async function handleLogout() {
    await api("/auth/logout", { method: "POST" }, true);
    setCurrentUser(null);
    setSummary(null);
    setResources([]);
    setBookings([]);
    setTickets([]);
    setUsers([]);
    setEditingAnnouncementId(null);
    setEditingTicketId(null);
    setAlert({ type: "success", message: "Logged out successfully." });
  }

  async function handleDeleteMyAccount() {
    const confirmed = window.confirm("Do you want to delete your account? This action cannot be undone.");
    if (!confirmed) {
      return;
    }

    const result = await api<UserDeleteResponse>("/auth/me", { method: "DELETE" });
    if (result) {
      setCurrentUser(null);
      setSummary(null);
      setResources([]);
      setBookings([]);
      setTickets([]);
      setUsers([]);
      setAlert({ type: "success", message: result.message ?? "Your account has been deleted." });
    }
  }

  async function handleDeleteUserAccount(id: number) {
    const confirmed = window.confirm("Do you want to delete this user account? This action cannot be undone.");
    if (!confirmed) {
      return;
    }

    const result = await api<UserDeleteResponse>(`/users/${id}`, { method: "DELETE" });
    if (result) {
      if (currentUser?.id === id) {
        setCurrentUser(null);
        setSummary(null);
        setResources([]);
        setBookings([]);
        setTickets([]);
        setUsers([]);
        setAlert({ type: "success", message: result.message ?? "Your account has been deleted." });
        return;
      }

      setAlert({ type: "success", message: result.message ?? "User account deleted." });
      await loadWorkspace(currentUser?.role);
    }
  }

  async function saveResource(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const payload = { ...resourceForm, capacity: Number(resourceForm.capacity) };
    const path = editingResourceId ? `/resources/${editingResourceId}` : "/resources";
    const method = editingResourceId ? "PUT" : "POST";
    const data = await api<Resource>(path, { method, body: JSON.stringify(payload) });
    if (data) {
      setResourceForm(emptyResource);
      setEditingResourceId(null);
      setAlert({ type: "success", message: editingResourceId ? "Resource updated." : "Resource added." });
      await loadResourceWorkspace();
    }
  }

    async function importResources() {
      if (!resourceCsvFile) {
        setAlert({ type: "error", message: "Please choose a CSV file first." });
        return;
      }

      const csvText = await resourceCsvFile.text();

      const response = await fetch(`${API_BASE}/resources/import/csv`, {
        method: "POST",
        headers: {
          "Content-Type": "text/csv",
        },
        credentials: "include",
        body: csvText,
      });

    if (response.ok) {
      setResourceCsvFile(null);
      setAlert({ type: "success", message: "Resources imported successfully." });
      await loadResourceWorkspace();
      return;
    }

    const raw = await response.text();
    setAlert({ type: "error", message: raw || "CSV import failed." });
  }

  async function saveBooking(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const payload = { ...bookingForm, resourceId: Number(bookingForm.resourceId), expectedAttendees: Number(bookingForm.expectedAttendees) };
    if (await api("/bookings", { method: "POST", body: JSON.stringify(payload) })) {
      setBookingForm(emptyBooking);
      setAlert({ type: "success", message: "Booking request submitted." });
      await loadWorkspace();
    }
  }

  async function saveTicket(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const payload = { ...ticketForm, resourceId: Number(ticketForm.resourceId) };
    const isEditingTicket = editingTicketId !== null;
    const path = isEditingTicket ? `/maintenance/${editingTicketId}` : "/maintenance";
    const method = isEditingTicket ? "PUT" : "POST";

    if (await api(path, { method, body: JSON.stringify(payload) })) {
      setTicketForm(emptyTicket);
      setEditingTicketId(null);
      setAlert({ type: "success", message: isEditingTicket ? "Maintenance ticket updated." : "Maintenance ticket created." });
      await loadWorkspace();
    }
  }

  const startTicketEdit = (ticket: MaintenanceTicket) => {
    setTicketForm({
      resourceId: String(ticket.resource.id),
      issueType: ticket.issueType ?? "",
      description: ticket.description ?? "",
      reportedBy: ticket.reportedBy ?? "",
      priority: ticket.priority ?? "MEDIUM",
    });
    setEditingTicketId(ticket.id);
    setActivePage("maintenance");
  };

  const cancelTicketEdit = () => {
    setTicketForm(emptyTicket);
    setEditingTicketId(null);
  };

  async function saveAnnouncement(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (currentUser?.role !== "ADMIN") {
      setAlert({ type: "error", message: "Only administrators can post announcements." });
      return;
    }

    const isEditingAnnouncement = editingAnnouncementId !== null;
    const payload = {
      title: announcementForm.title,
      content: announcementForm.content,
    };

    const path = isEditingAnnouncement ? `/announcements/${editingAnnouncementId}` : "/announcements";
    const method = isEditingAnnouncement ? "PUT" : "POST";

    if (await api(path, { method, body: JSON.stringify(payload) })) {
      setAnnouncementForm(emptyAnnouncement);
      setEditingAnnouncementId(null);
      setAlert({ type: "success", message: isEditingAnnouncement ? "Announcement updated." : "Announcement posted." });
      await loadWorkspace();
    }
  }

  if (booting) return <div className="startup-screen">Preparing Smart Campus portal...</div>;
  if (!currentUser) return <AuthView {...{ authTab, setAuthTab, loginForm, setLoginForm, registerForm, setRegisterForm, handleLogin, handleRegister, alert, campusHero, authErrors, setAuthErrors }} />;

  return <DashboardView {...{ currentUser, activePage, setActivePage, navItems, alert, summary, resources, bookings, tickets, users, alertHistory, notifications, notificationPreferences, notificationPreferenceForm, setNotificationPreferenceForm, resourceForm, setResourceForm, bookingForm, setBookingForm, ticketForm, setTicketForm, saveResource, saveBooking, saveTicket, saveAnnouncement, resourceTypes, resourceStatuses, bookingStatuses, maintenancePriorities, maintenanceStatuses, pretty, handleLogout, handleDeleteMyAccount, handleDeleteUserAccount, campusHero, editingResourceId, setEditingResourceId, setAlert, loadWorkspace, setBookingFilter, bookingFilter, setTicketFilter, ticketFilter, loading, resourceFormRef, resourceNameInputRef, maintenanceFormRef, maintenanceIssueInputRef, announcementFormRef, announcementTitleInputRef, announcementContentInputRef, announcements, announcementForm, setAnnouncementForm, editingAnnouncementId, setEditingAnnouncementId, editingTicketId, setEditingTicketId, sidebarOpen, setSidebarOpen, startTicketEdit, cancelTicketEdit, resourceSearch, setResourceSearch, resourceTypeFilter, setResourceTypeFilter, resourceLocationFilter, setResourceLocationFilter, resourceCapacityFilter, setResourceCapacityFilter, resourceStatusFilter, setResourceStatusFilter, resourceSuggestions, resourceCsvFile, setResourceCsvFile, resourceAnalytics, importResources }} />;
}

function AuthView({ authTab, setAuthTab, loginForm, setLoginForm, registerForm, setRegisterForm, handleLogin, handleRegister, alert, campusHero, authErrors, setAuthErrors }: {
  authTab: string;
  setAuthTab: (value: string) => void;
  loginForm: LoginForm;
  setLoginForm: (value: LoginForm) => void;
  registerForm: RegisterForm;
  setRegisterForm: (value: RegisterForm) => void;
  handleLogin: (event: FormEvent<HTMLFormElement>) => void;
  handleRegister: (event: FormEvent<HTMLFormElement>) => void;
  alert: AlertState;
  campusHero: string;
  authErrors: AuthErrors;
  setAuthErrors: (value: AuthErrors | ((current: AuthErrors) => AuthErrors)) => void;
}) {
  return (
    <div className="auth-shell">
      <section className="auth-hero"><div><span className="eyebrow">Smart Campus Portal</span><h1>Proper login system, home page, menu bar, and dashboard.</h1><p>This portal now has protected login, validation checks, and a more complete website experience.</p><div className="demo-card"><strong>Demo accounts</strong><p>admin@smartcampus.lk / Admin@123</p><p>user@smartcampus.lk / User@123</p><p>tech@smartcampus.lk / Tech@123</p></div></div><img src={campusHero} alt="Campus visual" /></section>
      <section className="auth-panel">
        <div className="tab-row"><button className={authTab === "login" ? "tab active" : "tab"} onClick={() => setAuthTab("login")}>Login</button><button className={authTab === "register" ? "tab active" : "tab"} onClick={() => setAuthTab("register")}>Register</button></div>
        {alert && (
          <div className={`alert auth-alert ${alert.type}`} role="alert" aria-live="assertive">
            <span className="auth-alert-icon">{alert.type === "error" ? "!" : "i"}</span>
            <span>{alert.message || "Please fix the highlighted fields."}</span>
          </div>
        )}
        {authTab === "login" ? (
          <form className="auth-form" onSubmit={handleLogin} noValidate>
            <input
              type="email"
              value={loginForm.email}
              onChange={(e) => {
                setLoginForm({ ...loginForm, email: e.target.value });
                if (authErrors.login.email) setAuthErrors((current) => ({ ...current, login: { ...current.login, email: "" } }));
              }}
              placeholder="Email"
              aria-invalid={Boolean(authErrors.login.email)}
            />
            {authErrors.login.email && <span className="field-error">{authErrors.login.email}</span>}
            <input
              type="password"
              value={loginForm.password}
              onChange={(e) => {
                setLoginForm({ ...loginForm, password: e.target.value });
                if (authErrors.login.password) setAuthErrors((current) => ({ ...current, login: { ...current.login, password: "" } }));
              }}
              placeholder="Password"
              aria-invalid={Boolean(authErrors.login.password)}
            />
            {authErrors.login.password && <span className="field-error">{authErrors.login.password}</span>}
            <button className="primary-button wide">Login</button>
          </form>
        ) : (
          <form className="auth-form" onSubmit={handleRegister} noValidate>
            <input
              value={registerForm.fullName}
              onChange={(e) => {
                setRegisterForm({ ...registerForm, fullName: e.target.value });
                if (authErrors.register.fullName) setAuthErrors((current) => ({ ...current, register: { ...current.register, fullName: "" } }));
              }}
              placeholder="Full name"
              aria-invalid={Boolean(authErrors.register.fullName)}
            />
            <small className="field-hint">Letters only, at least 3 letters total.</small>
            {authErrors.register.fullName && <span className="field-error">{authErrors.register.fullName}</span>}
            <input
              type="email"
              value={registerForm.email}
              onChange={(e) => {
                setRegisterForm({ ...registerForm, email: e.target.value });
                if (authErrors.register.email) setAuthErrors((current) => ({ ...current, register: { ...current.register, email: "" } }));
              }}
              placeholder="Email"
              aria-invalid={Boolean(authErrors.register.email)}
            />
            {authErrors.register.email && <span className="field-error">{authErrors.register.email}</span>}
            <select
              value={registerForm.role}
              onChange={(e) => {
                setRegisterForm({ ...registerForm, role: e.target.value });
                if (authErrors.register.role) setAuthErrors((current) => ({ ...current, register: { ...current.register, role: "" } }));
              }}
              aria-invalid={Boolean(authErrors.register.role)}
            >
              <option value="USER">User</option>
              <option value="TECHNICIAN">Technician</option>
              <option value="ADMIN">Admin</option>
            </select>
            {authErrors.register.role && <span className="field-error">{authErrors.register.role}</span>}
            <input
              type="password"
              minLength={8}
              value={registerForm.password}
              onChange={(e) => {
                setRegisterForm({ ...registerForm, password: e.target.value });
                if (authErrors.register.password) setAuthErrors((current) => ({ ...current, register: { ...current.register, password: "" } }));
              }}
              placeholder="Password"
              aria-invalid={Boolean(authErrors.register.password)}
            />
            <small className="field-hint">8+ chars, with 1 uppercase, 1 lowercase, 1 number, and 1 symbol.</small>
            {authErrors.register.password && <span className="field-error">{authErrors.register.password}</span>}
            <input
              type="password"
              minLength={8}
              value={registerForm.confirmPassword}
              onChange={(e) => {
                setRegisterForm({ ...registerForm, confirmPassword: e.target.value });
                if (authErrors.register.confirmPassword) setAuthErrors((current) => ({ ...current, register: { ...current.register, confirmPassword: "" } }));
              }}
              placeholder="Confirm password"
              aria-invalid={Boolean(authErrors.register.confirmPassword)}
            />
            {authErrors.register.confirmPassword && <span className="field-error">{authErrors.register.confirmPassword}</span>}
            <button className="primary-button wide">Create account</button>
          </form>
        )}
      </section>
    </div>
  );
}

function DashboardView(props: {
  currentUser: UserProfile;
  activePage: string;
  setActivePage: (value: string) => void;
  navItems: string[];
  alert: AlertState;
  summary: SummaryStats | null;
  resources: Resource[];
  bookings: Booking[];
  tickets: MaintenanceTicket[];
  users: UserProfile[];
  resourceForm: ResourceForm;
  setResourceForm: (value: ResourceForm) => void;
  bookingForm: BookingForm;
  setBookingForm: (value: BookingForm) => void;
  ticketForm: TicketForm;
  setTicketForm: (value: TicketForm) => void;
  saveResource: (event: FormEvent<HTMLFormElement>) => void;
  saveBooking: (event: FormEvent<HTMLFormElement>) => void;
  saveTicket: (event: FormEvent<HTMLFormElement>) => void;
  saveAnnouncement: (event: FormEvent<HTMLFormElement>) => void;
  resourceTypes: string[];
  resourceStatuses: string[];
  bookingStatuses: string[];
  maintenancePriorities: string[];
  maintenanceStatuses: string[];
  pretty: (value: string) => string;
  handleLogout: () => void;
  handleDeleteMyAccount: () => void;
  handleDeleteUserAccount: (id: number) => void;
  campusHero: string;
  editingResourceId: number | null;
  setEditingResourceId: (value: number | null) => void;
  setAlert: (value: AlertState) => void;
  loadWorkspace: () => Promise<void>;
  setBookingFilter: (value: string) => void;
  bookingFilter: string;
  setTicketFilter: (value: string) => void;
  ticketFilter: string;
  loading: boolean;
  resourceFormRef: RefObject<HTMLElement>;
  resourceNameInputRef: RefObject<HTMLInputElement>;
  maintenanceFormRef: RefObject<HTMLElement>;
  maintenanceIssueInputRef: RefObject<HTMLInputElement>;
  announcementFormRef: RefObject<HTMLElement>;
  announcementTitleInputRef: RefObject<HTMLInputElement>;
  announcementContentInputRef: RefObject<HTMLTextAreaElement>;
  announcements: Announcement[];
  alertHistory: AlertHistoryItem[];
  notifications: Notification[];
  notificationPreferences: NotificationPreferences | null;
  notificationPreferenceForm: NotificationPreferences;
  setNotificationPreferenceForm: (value: NotificationPreferences) => void;
  resourceSearch: string;
  setResourceSearch: (value: string) => void;
  resourceTypeFilter: string;
  setResourceTypeFilter: (value: string) => void;
  resourceLocationFilter: string;
  setResourceLocationFilter: (value: string) => void;
  resourceCapacityFilter: string;
  setResourceCapacityFilter: (value: string) => void;
  resourceStatusFilter: string;
  setResourceStatusFilter: (value: string) => void;
  resourceSuggestions: Resource[];
  resourceCsvFile: File | null;
  setResourceCsvFile: (value: File | null) => void;
  resourceAnalytics: ResourceUsage[];
  importResources: () => Promise<void>;
  announcementForm: AnnouncementForm;
  setAnnouncementForm: (value: AnnouncementForm) => void;
  editingAnnouncementId: number | null;
  setEditingAnnouncementId: (value: number | null) => void;
  editingTicketId: number | null;
  setEditingTicketId: (value: number | null) => void;
  sidebarOpen: boolean;
  setSidebarOpen: (value: boolean) => void;
  currentUser: UserProfile;
  startTicketEdit: (ticket: MaintenanceTicket) => void;
  cancelTicketEdit: () => void;
}) {
  const { currentUser, activePage, setActivePage, navItems, alert, summary, resources, bookings, tickets, users, resourceForm, setResourceForm, bookingForm, setBookingForm, ticketForm, setTicketForm, saveResource, saveBooking, saveTicket, saveAnnouncement, resourceTypes, resourceStatuses, bookingStatuses, maintenancePriorities, maintenanceStatuses, pretty, handleLogout, handleDeleteMyAccount, handleDeleteUserAccount, campusHero, editingResourceId, setEditingResourceId, setAlert, loadWorkspace, setBookingFilter, bookingFilter, setTicketFilter, ticketFilter, loading, resourceFormRef, resourceNameInputRef, maintenanceFormRef, maintenanceIssueInputRef, announcementFormRef, announcementTitleInputRef, announcementContentInputRef, announcements, alertHistory, notifications, notificationPreferences, notificationPreferenceForm, setNotificationPreferenceForm, resourceSearch, setResourceSearch, resourceTypeFilter, setResourceTypeFilter, resourceLocationFilter, setResourceLocationFilter, resourceCapacityFilter, setResourceCapacityFilter, resourceStatusFilter, setResourceStatusFilter, resourceSuggestions, resourceCsvFile, setResourceCsvFile, resourceAnalytics, importResources, announcementForm, setAnnouncementForm, editingAnnouncementId, setEditingAnnouncementId, editingTicketId, setEditingTicketId, sidebarOpen, setSidebarOpen, startTicketEdit, cancelTicketEdit } = props;
    const isSuperAdmin = currentUser.role === "SUPER_ADMIN";
    const isAdmin = currentUser.role === "ADMIN" || isSuperAdmin;
    const activeResources = resources.filter((resource) => resource.status === "ACTIVE");
    const unreadNotificationCount = notifications.filter((notification) => !notification.read).length;
    const smartSearchPresets = [
      { label: "Labs", query: "lab", type: "LAB" },
      { label: "Lecture Halls", query: "lecture hall", type: "LECTURE_HALL" },
      { label: "Meeting Rooms", query: "meeting room", type: "MEETING_ROOM" },
      { label: "Equipment", query: "equipment", type: "EQUIPMENT" },
      { label: "Large Capacity", query: "large", minCapacity: "100" },
      { label: "Available Now", query: "available", status: "ACTIVE" },
    ];
    const parseApiError = (data: unknown) => {
    if (typeof data === "object" && data !== null && "validationErrors" in data) {
      const errors = (data as { validationErrors?: Record<string, string> }).validationErrors;
      if (errors) return Object.values(errors).join(" ");
    }
    if (typeof data === "object" && data !== null && "message" in data) {
      return String((data as { message?: string }).message ?? "Something went wrong");
    }
    return "Something went wrong";
    };
    const applySmartSearchPreset = (preset: { query: string; type?: string; minCapacity?: string; status?: string }) => {
      setResourceSearch(preset.query);
      setResourceTypeFilter(preset.type ?? "");
      setResourceLocationFilter("");
      setResourceCapacityFilter(preset.minCapacity ?? "");
      setResourceStatusFilter(preset.status ?? "");
    };
    const applyResourceSuggestion = (resource: Resource) => {
      setResourceSearch(resource.name);
      setResourceTypeFilter(resource.type);
      setResourceLocationFilter(resource.location);
      setResourceCapacityFilter(String(resource.capacity));
      setResourceStatusFilter(resource.status);
    };
    const normalizeResourceStatus = (status: string) => (status === "MAINTENANCE" ? "UNDER_MAINTENANCE" : status);
    const updateBooking = async (id: number, status: string) => { const response = await fetch(`${API_BASE}/bookings/${id}/status?status=${status}`, { method: "PATCH", credentials: "include" }); if (response.ok) { setAlert({ type: "success", message: `Booking ${pretty(status)}.` }); loadWorkspace(); } };
  const updateTicket = async (id: number, status: string) => { const q = new URLSearchParams({ status }); if (status !== "OPEN") q.append("assignedTechnician", "Campus Technical Team"); const response = await fetch(`${API_BASE}/maintenance/${id}/status?${q.toString()}`, { method: "PATCH", credentials: "include" }); if (response.ok) { setAlert({ type: "success", message: `Ticket moved to ${pretty(status)}.` }); loadWorkspace(); } };
  const markNotificationRead = async (id: number) => {
    const response = await api<Notification>(`/notifications/${id}/read`, { method: "POST" }, true);
    if (response) {
      setAlert({ type: "success", message: "Notification marked as read." });
      loadWorkspace();
    }
  };
  const saveNotificationPreferences = async () => {
    const response = await api<NotificationPreferences>("/notifications/preferences", {
      method: "PUT",
      body: JSON.stringify(notificationPreferenceForm),
    });
    if (response) {
      setNotificationPreferences(response);
      setNotificationPreferenceForm(response);
      setAlert({ type: "success", message: "Notification preferences updated." });
      loadWorkspace();
    }
  };
  const startAnnouncementEdit = (announcement: Announcement) => {
    setAnnouncementForm({
      title: announcement.title ?? "",
      content: announcement.content ?? "",
    });
    setEditingAnnouncementId(announcement.id);
    setActivePage("dashboard");
  };
  const cancelAnnouncementEdit = () => {
    setAnnouncementForm(emptyAnnouncement);
    setEditingAnnouncementId(null);
  };
  const removeResource = async (id: number) => {
    const confirmed = window.confirm("Do you want to delete this resource? This action cannot be undone.");
    if (!confirmed) {
      return;
    }

    const response = await fetch(`${API_BASE}/resources/${id}`, {
      method: "DELETE",
      credentials: "include",
    });

    if (response.ok) {
      setAlert({ type: "success", message: "Resource deleted." });
      loadResourceWorkspace();
      return;
    }

    const raw = await response.text();
    let parsed: unknown = raw;
    if (raw) {
      try {
        parsed = JSON.parse(raw) as unknown;
      } catch {
        parsed = raw;
      }
    }

    setAlert({
      type: "error",
      message: parseApiError(parsed) || "Resource could not be deleted.",
    });
  };
  const removeAnnouncement = async (id: number) => {
    const confirmed = window.confirm("Do you want to delete this announcement? This action cannot be undone.");
    if (!confirmed) {
      return;
    }

    const response = await fetch(`${API_BASE}/announcements/${id}`, {
      method: "DELETE",
      credentials: "include",
    });

    if (response.ok) {
      setAlert({ type: "success", message: "Announcement deleted." });
      loadWorkspace();
      return;
    }

    const raw = await response.text();
    let parsed: unknown = raw;
    if (raw) {
      try {
        parsed = JSON.parse(raw) as unknown;
      } catch {
        parsed = raw;
      }
    }

      setAlert({
        type: "error",
        message: parseApiError(parsed) || "Announcement could not be deleted.",
      });
  };

  const formatSla = (ticket: MaintenanceTicket) => {
    if (!ticket.createdAt) {
      return "SLA unavailable";
    }

    const createdAt = new Date(ticket.createdAt).getTime();
    const assignedAt = ticket.assignedAt ? new Date(ticket.assignedAt).getTime() : Date.now();
    const resolvedAt = ticket.resolvedAt ? new Date(ticket.resolvedAt).getTime() : Date.now();
    const responseMinutes = Math.max(0, Math.round((assignedAt - createdAt) / 60000));
    const resolutionMinutes = Math.max(0, Math.round((resolvedAt - createdAt) / 60000));
    const responseLabel = responseMinutes <= 60 ? "within response SLA" : "response SLA breached";
    const resolutionLabel = resolutionMinutes <= 480 ? "within resolution SLA" : "resolution SLA breached";
    return `Response ${responseMinutes}m (${responseLabel}), Resolution ${resolutionMinutes}m (${resolutionLabel})`;
  };

  const downloadSummaryPdf = () => {
    const sections: PdfSection[] = [
      {
        title: "Operational Snapshot",
        lines: [
          `Total resources: ${summary?.totalResources ?? resources.length}`,
          `Active resources: ${summary?.activeResources ?? activeResources.length}`,
          `Pending bookings: ${summary?.pendingBookings ?? bookings.filter((item) => item.status === "PENDING").length}`,
          `Open maintenance tickets: ${summary?.openMaintenanceTickets ?? tickets.filter((item) => item.status === "OPEN").length}`,
        ],
      },
      {
        title: "Alert History",
        lines: alertHistory.length
          ? alertHistory.map((entry) => `${new Date(entry.createdAt).toLocaleString()} | ${entry.type.toUpperCase()} | ${entry.message}`)
          : ["No alert history available."],
      },
      {
        title: "Notification History",
        lines: notifications.length
          ? notifications.map((notification) => `${new Date(notification.createdAt).toLocaleString()} | ${notification.read ? "READ" : "NEW"} | ${notification.category} | ${notification.title}`)
          : ["No notifications available."],
      },
      {
        title: "Booking History",
        lines: bookings.length
          ? bookings.map((booking) => `${booking.bookingDate} ${booking.startTime}-${booking.endTime} | ${booking.resource.name} | ${booking.requestedBy} | ${booking.status}`)
          : ["No booking history available."],
      },
      {
        title: "Ticket History",
        lines: tickets.length
          ? tickets.map((ticket) => `${ticket.createdAt ? new Date(ticket.createdAt).toLocaleString() : "Unknown time"} | ${ticket.issueType} | ${ticket.resource.name} | ${ticket.priority} | ${ticket.status}`)
          : ["No ticket history available."],
      },
      {
        title: "Resource Analytics",
        lines: resourceAnalytics.length
          ? resourceAnalytics.map((item) => `${item.resourceName} - ${item.bookingCount} bookings`)
          : ["No analytics available."],
      },
    ];

    const pdfBytes = buildPdfBytes("Smart Campus Summary Report", sections);
    const blob = new Blob([pdfBytes], { type: "application/pdf" });
    const url = URL.createObjectURL(blob);
    const link = document.createElement("a");
    link.href = url;
    link.download = `smart-campus-summary-${new Date().toISOString().replace(/[:.]/g, "-")}.pdf`;
    document.body.appendChild(link);
    link.click();
    link.remove();
    window.setTimeout(() => URL.revokeObjectURL(url), 1000);
    setAlert({ type: "success", message: "Summary PDF generated successfully." });
  };

  const removeTicket = async (id: number) => {
    const confirmed = window.confirm("Do you want to delete this maintenance ticket? This action cannot be undone.");
    if (!confirmed) {
      return;
    }

    const response = await fetch(`${API_BASE}/maintenance/${id}`, {
      method: "DELETE",
      credentials: "include",
    });

    if (response.ok) {
      setAlert({ type: "success", message: "Maintenance ticket deleted." });
      loadWorkspace();
      return;
    }

    const raw = await response.text();
    let parsed: unknown = raw;
    if (raw) {
      try {
        parsed = JSON.parse(raw) as unknown;
      } catch {
        parsed = raw;
      }
    }

    setAlert({
      type: "error",
      message: parseApiError(parsed) || "Maintenance ticket could not be deleted.",
    });
  };

  const handleAlertsClick = () => {
    setActivePage("dashboard");
    // Scroll to alerts section after state update
    window.setTimeout(() => {
      const alertsElement = document.querySelector('[data-section="alerts"]');
      if (alertsElement) {
        alertsElement.scrollIntoView({ behavior: "smooth", block: "start" });
      }
    }, 100);
  };

  return (
    <div className="site-container">
      <Header currentUser={currentUser} onLogout={handleLogout} onDeleteAccount={handleDeleteMyAccount} notificationCount={notifications.filter((notification) => !notification.read).length} sidebarOpen={sidebarOpen} setSidebarOpen={setSidebarOpen} onAlertsClick={handleAlertsClick} />
      <div className="site-shell">
        {alert && <div className={`alert toast-banner ${alert.type}`} role="status" aria-live="polite">{alert.message}</div>}
      <div className="portal-layout">
        {sidebarOpen && <div className="sidebar-overlay" onClick={() => setSidebarOpen(false)} />}
        <aside className={`sidebar ${sidebarOpen ? 'active' : ''}`}>
          <div className="demo-card"><strong>Campus Modules</strong><p>Home, Dashboard, Resources, Bookings, and Maintenance are available in one workspace.</p></div>
          <nav className="nav-stack">{navItems.map((item) => <button key={item} className={activePage === item ? "nav-link active" : "nav-link"} onClick={() => setActivePage(item)}>{pretty(item)}</button>)}</nav>
        </aside>
        <main className="main-content">
          {activePage === "home" && <section className="hero-card"><div><span className="eyebrow light">Smart Campus Operations Hub</span><h1>Centralize campus resources, bookings, maintenance, and user access in one dashboard.</h1><p>Track operations clearly, switch between modules quickly, and present a professional system that matches your assignment flow.</p><div className="row"><button className="primary-button" onClick={() => setActivePage("dashboard")}>Open Dashboard</button><button className="secondary-button" onClick={() => setActivePage("resources")}>Manage Resources</button></div></div><img src={campusHero} alt="Campus illustration" /></section>}
          {activePage === "summary" && (
            <>
              <section className="two-column">
                <article className="panel">
                  <div className="row between">
                    <div>
                      <h3>Summary Export</h3>
                      <p>Generate a PDF summary with timestamps for alerts, notifications, bookings, tickets, and analytics.</p>
                    </div>
                    <button type="button" className="primary-button" onClick={downloadSummaryPdf}>
                      Download PDF
                    </button>
                  </div>
                  <div className="mini-metrics" style={{ marginTop: "16px" }}>
                    <div><strong>{alertHistory.length}</strong><span>Alerts</span></div>
                    <div><strong>{notifications.length}</strong><span>Notifications</span></div>
                    <div><strong>{bookings.length}</strong><span>Bookings</span></div>
                    <div><strong>{tickets.length}</strong><span>Tickets</span></div>
                  </div>
                </article>
                <article className="panel">
                  <h3>What the PDF Includes</h3>
                  <p>The generated file contains separated history sections with date and time entries for clean viva and submission use.</p>
                  <div className="notification-pref-grid">
                    <div><strong>Alert History</strong><span>Success and error actions</span></div>
                    <div><strong>Notification History</strong><span>Read and unread campus alerts</span></div>
                    <div><strong>Booking History</strong><span>Booking requests and status flow</span></div>
                    <div><strong>Ticket History</strong><span>Maintenance updates and SLA info</span></div>
                  </div>
                </article>
              </section>
              <section className="two-column" data-section="alerts">
                <article className="panel">
                  <div className="row between">
                    <h3>Alert History</h3>
                    <span className="badge">{alertHistory.length}</span>
                  </div>
                  <div className="table-list">
                    {alertHistory.map((entry) => (
                      <div key={entry.id} className={`table-card ${entry.type}`}>
                        <div>
                          <strong>{entry.type === "success" ? "Success" : "Error"}</strong>
                          <p>{entry.message}</p>
                          <p className="muted">{new Date(entry.createdAt).toLocaleString()}</p>
                        </div>
                      </div>
                    ))}
                    {alertHistory.length === 0 && <p>No alert history yet.</p>}
                  </div>
                </article>
                <article className="panel">
                  <div className="row between">
                    <h3>Notification History</h3>
                    <span className="badge">{notifications.length}</span>
                  </div>
                  <div className="table-list">
                    {notifications.map((notification) => (
                      <button 
                        key={notification.id} 
                        className={`table-card ${!notification.read ? "unread" : ""}`}
                        onClick={() => markNotificationRead(notification.id)}
                        style={{ 
                          cursor: "pointer", 
                          border: "none", 
                          background: "inherit", 
                          padding: "var(--spacing-sm)",
                          borderRadius: "4px",
                          textAlign: "left",
                          width: "100%"
                        }}
                      >
                        <div>
                          <strong>{notification.title}</strong>
                          <p>{notification.message}</p>
                          <p className="muted">{notification.category} | {notification.read ? "✓ Read" : "◉ New"} | {new Date(notification.createdAt).toLocaleString()}</p>
                        </div>
                      </button>
                    ))}
                    {notifications.length === 0 && <p>No notifications yet.</p>}
                  </div>
                </article>
              </section>
              <section className="two-column">
                <article className="panel">
                  <div className="row between">
                    <h3>Booking History</h3>
                    <span className="badge">{bookings.length}</span>
                  </div>
                  <div className="table-list">
                    {bookings.map((booking) => (
                      <div key={booking.id} className="table-card">
                        <div>
                          <strong>{booking.resource.name}</strong>
                          <p>{booking.bookingDate} | {booking.startTime} - {booking.endTime}</p>
                          <p>{booking.requestedBy} | {booking.department}</p>
                          <p className="muted">{pretty(booking.status)}</p>
                        </div>
                      </div>
                    ))}
                    {bookings.length === 0 && <p>No booking history yet.</p>}
                  </div>
                </article>
                <article className="panel">
                  <div className="row between">
                    <h3>Ticket History</h3>
                    <span className="badge">{tickets.length}</span>
                  </div>
                  <div className="table-list">
                    {tickets.map((ticket) => (
                      <div key={ticket.id} className="table-card">
                        <div>
                          <strong>{ticket.issueType}</strong>
                          <p>{ticket.resource.name}</p>
                          <p>{ticket.priority} | {pretty(ticket.status)}</p>
                          <p className="muted">{ticket.createdAt ? new Date(ticket.createdAt).toLocaleString() : "Unknown time"}</p>
                        </div>
                      </div>
                    ))}
                    {tickets.length === 0 && <p>No ticket history yet.</p>}
                  </div>
                </article>
              </section>
              <section className="panel">
                <div className="row between">
                  <h3>Resource Analytics</h3>
                  <span className="badge">{resourceAnalytics.length}</span>
                </div>
                <div className="mini-metrics">
                  {resourceAnalytics.slice(0, 6).map((item) => (
                    <div key={item.resourceId}>
                      <strong>{item.bookingCount}</strong>
                      <span>{item.resourceName}</span>
                    </div>
                  ))}
                  {resourceAnalytics.length === 0 && <div><strong>0</strong><span>No analytics yet</span></div>}
                </div>
              </section>
            </>
          )}
          {activePage === "dashboard" && (
            <>
              <div className="dashboard-banner">
                <h1>Dashboard</h1>
                <p>Campus operations overview and live status at a glance</p>
              </div>
              <section className="summary-grid">
                <StatCard label="Total Resources" value={summary?.totalResources ?? 0} />
                <StatCard label="Active Resources" value={summary?.activeResources ?? 0} />
                <StatCard label="Pending Bookings" value={summary?.pendingBookings ?? 0} />
                <StatCard label="Approved Bookings" value={summary?.approvedBookings ?? 0} />
                <StatCard label="Open Tickets" value={summary?.openMaintenanceTickets ?? 0} />
                <StatCard label="Resolved Tickets" value={summary?.resolvedMaintenanceTickets ?? 0} />
              </section>
              <section className="two-column">
                <article className="panel">
                  <h3>Operational Summary</h3>
                  <p>{activeResources.length} active resources are available now.</p>
                  <p>{bookings.filter((item) => item.status === "PENDING").length} bookings are waiting for review.</p>
                  <p>{tickets.filter((item) => item.status === "OPEN").length} maintenance tickets are open.</p>
                </article>
                <article className="panel image-panel">
                  <img src={campusHero} alt="Dashboard visual" />
                </article>
              </section>
              <section className="two-column">
                <article className="panel">
                  <h3>My Account</h3>
                  <p><strong>Name:</strong> {currentUser.fullName}</p>
                  <p><strong>Email:</strong> {currentUser.email}</p>
                  <p><strong>Role:</strong> {pretty(currentUser.role)}</p>
                  <button className="danger-button" onClick={handleDeleteMyAccount}>
                    Delete My Account
                  </button>
                </article>
                {isSuperAdmin ? (
                  <article className="panel">
                    <h3>User Management</h3>
                    <p>Super administrators can delete any account in the system.</p>
                    <div className="table-list">
                      {users.map((user) => (
                        <div key={user.id} className="table-card">
                          <div>
                            <strong>{user.fullName}</strong>
                            <p>{user.email}</p>
                            <p>{pretty(user.role)}</p>
                          </div>
                          <div className="table-actions">
                            <button
                              type="button"
                              className="danger-button"
                              onClick={() => handleDeleteUserAccount(user.id)}
                            >
                              Delete
                            </button>
                          </div>
                        </div>
                      ))}
                      {users.length === 0 && <p>No user accounts found.</p>}
                    </div>
                  </article>
                ) : (
                  <article className="panel">
                    <h3>Account Control</h3>
                    <p>Your account can be deleted at any time from the profile menu or this panel.</p>
                  </article>
                )}
              </section>
              {isAdmin && (
                <section className="two-column">
                  <article ref={announcementFormRef} className="panel">
                    <h3>{editingAnnouncementId ? "Edit Announcement" : "Post Announcement"}</h3>
                    <p>Publish an official update for staff and students.</p>
                    <form className="form-grid" onSubmit={saveAnnouncement}>
                      <input
                        ref={announcementTitleInputRef}
                        value={announcementForm.title}
                        onChange={(e) => setAnnouncementForm({ ...announcementForm, title: e.target.value })}
                        placeholder="Announcement title"
                        required
                      />
                      <textarea
                        ref={announcementContentInputRef}
                        rows="6"
                        value={announcementForm.content}
                        onChange={(e) => setAnnouncementForm({ ...announcementForm, content: e.target.value })}
                        placeholder="Announcement content"
                        required
                      />
                      <div className="row">
                        <button className="primary-button">{editingAnnouncementId ? "Update announcement" : "Post announcement"}</button>
                        {editingAnnouncementId && (
                          <button type="button" className="secondary-button" onClick={cancelAnnouncementEdit}>
                            Cancel edit
                          </button>
                        )}
                      </div>
                    </form>
                  </article>
                  <article className="panel">
                    <h3>Announcements</h3>
                    <p>Live updates are shown below and stored in the backend.</p>
                  </article>
                </section>
              )}
              <section className="two-column">
                <article className="panel">
                  <div className="row between">
                    <h3>Notifications</h3>
                    <span className="badge">{unreadNotificationCount} unread</span>
                  </div>
                  <p>Real-time campus alerts and system updates appear here.</p>
                  <div className="notification-list">
                    {notifications.map((notification) => (
                      <article key={notification.id} className={`notification-item ${notification.read ? "read" : "unread"}`}>
                        <div className="row between">
                          <span className="chip">{pretty(notification.category)}</span>
                          <span className="muted">{new Date(notification.createdAt).toLocaleString()}</span>
                        </div>
                        <h4>{notification.title}</h4>
                        <p>{notification.message}</p>
                        <div className="row between">
                          <span className={notification.read ? "muted" : "pill info"}>{notification.read ? "Read" : "New"}</span>
                          {!notification.read && (
                            <button type="button" className="secondary-button" onClick={() => markNotificationRead(notification.id)}>
                              Mark read
                            </button>
                          )}
                        </div>
                      </article>
                    ))}
                    {notifications.length === 0 && <p>No notifications available.</p>}
                  </div>
                </article>
                <article className="panel">
                  <h3>Notification Preferences</h3>
                  <p>Choose which alerts should reach your dashboard.</p>
                  <div className="notification-pref-grid">
                    <label><input type="checkbox" checked={notificationPreferenceForm.resourceAlerts} onChange={(e) => setNotificationPreferenceForm({ ...notificationPreferenceForm, resourceAlerts: e.target.checked })} /> Resource alerts</label>
                    <label><input type="checkbox" checked={notificationPreferenceForm.bookingAlerts} onChange={(e) => setNotificationPreferenceForm({ ...notificationPreferenceForm, bookingAlerts: e.target.checked })} /> Booking alerts</label>
                    <label><input type="checkbox" checked={notificationPreferenceForm.maintenanceAlerts} onChange={(e) => setNotificationPreferenceForm({ ...notificationPreferenceForm, maintenanceAlerts: e.target.checked })} /> Maintenance alerts</label>
                    <label><input type="checkbox" checked={notificationPreferenceForm.announcementAlerts} onChange={(e) => setNotificationPreferenceForm({ ...notificationPreferenceForm, announcementAlerts: e.target.checked })} /> Announcement alerts</label>
                  </div>
                  <div className="row">
                    <button type="button" className="primary-button" onClick={saveNotificationPreferences}>Save preferences</button>
                  </div>
                  <div className="mini-metrics" style={{ marginTop: "16px" }}>
                    <div><strong>{notificationPreferences?.resourceAlerts ? "On" : "Off"}</strong><span>Resource</span></div>
                    <div><strong>{notificationPreferences?.bookingAlerts ? "On" : "Off"}</strong><span>Booking</span></div>
                    <div><strong>{notificationPreferences?.maintenanceAlerts ? "On" : "Off"}</strong><span>Maintenance</span></div>
                    <div><strong>{notificationPreferences?.announcementAlerts ? "On" : "Off"}</strong><span>Announcement</span></div>
                  </div>
                </article>
              </section>
              <section className="panel">
                <div className="row between">
                  <h3>Alert History</h3>
                  <span className="chip subtle">Last {Math.min(alertHistory.length, 6)}</span>
                </div>
                <div className="alert-history">
                  {alertHistory.map((entry) => (
                    <div key={entry.id} className={`history-item ${entry.type}`}>
                      <div className="row between">
                        <strong>{entry.type === "success" ? "Success" : "Error"}</strong>
                        <span className="muted">{new Date(entry.createdAt).toLocaleTimeString()}</span>
                      </div>
                      <p>{entry.message}</p>
                    </div>
                  ))}
                  {alertHistory.length === 0 && <p>No recent alerts yet.</p>}
                </div>
              </section>
              <AnnouncementsList announcements={announcements} currentUser={currentUser} onEditAnnouncement={startAnnouncementEdit} onDeleteAnnouncement={removeAnnouncement} />
            </>
          )}
            {activePage === "resources" && <><section className="two-column"><article ref={resourceFormRef} className="panel"><h3>{editingResourceId ? "Edit Resource" : "Add Resource"}</h3><form className="form-grid" onSubmit={saveResource}><input ref={resourceNameInputRef} value={resourceForm.name} onChange={(e) => setResourceForm({ ...resourceForm, name: e.target.value })} placeholder="Resource name" required /><select value={resourceForm.type} onChange={(e) => setResourceForm({ ...resourceForm, type: e.target.value })}>{resourceTypes.map((type) => <option key={type} value={type}>{pretty(type)}</option>)}</select><input type="number" min="1" value={resourceForm.capacity} onChange={(e) => setResourceForm({ ...resourceForm, capacity: e.target.value })} placeholder="Capacity" required /><input value={resourceForm.location} onChange={(e) => setResourceForm({ ...resourceForm, location: e.target.value })} placeholder="Location" required /><input value={resourceForm.availabilityWindow} onChange={(e) => setResourceForm({ ...resourceForm, availabilityWindow: e.target.value })} placeholder="Availability window" required /><select value={resourceForm.status} onChange={(e) => setResourceForm({ ...resourceForm, status: e.target.value })}>{resourceStatuses.map((status) => <option key={status} value={status}>{pretty(status)}</option>)}</select><textarea rows="4" value={resourceForm.description} onChange={(e) => setResourceForm({ ...resourceForm, description: e.target.value })} placeholder="Description" required /><div className="row"><button className="primary-button wide">{editingResourceId ? "Save" : "Add"}</button>{editingResourceId && <button type="button" className="secondary-button" onClick={() => { setEditingResourceId(null); setResourceForm(emptyResource); }}>Cancel</button>}</div></form></article><article className="panel"><h3>Resources</h3><p>Manage lecture halls, labs, meeting rooms, and equipment with structured forms and quick updates.</p></article></section><section className="two-column"><article className="panel"><div className="panel-header"><div><h3>Smart Search</h3><p>Search by name, type, location, description, or availability. Results rank by best match first.</p></div><span className="badge">{resources.length} found</span></div><div className="search-chip-row">{smartSearchPresets.map((preset) => <button key={preset.label} type="button" className="chip subtle" onClick={() => applySmartSearchPreset(preset)}>{preset.label}</button>)}</div><div className="form-grid"><input value={resourceSearch} onChange={(e) => setResourceSearch(e.target.value)} placeholder="Try: lab, hall, projector, block, 100+" list="resource-suggestions" /><datalist id="resource-suggestions">{resourceSuggestions.map((item) => <option key={item.id} value={item.name}>{item.name}</option>)}</datalist>{resourceSuggestions.length > 0 && <div className="search-suggestions">{resourceSuggestions.slice(0, 5).map((item, index) => <button key={item.id} type="button" className={`suggestion-card ${index === 0 ? "best" : ""}`} onClick={() => applyResourceSuggestion(item)}><strong>{item.name}</strong><span>{pretty(item.type)} • {item.location}</span><small>{item.capacity} seats • {pretty(item.status)}</small></button>)}</div>}<select value={resourceTypeFilter} onChange={(e) => setResourceTypeFilter(e.target.value)}><option value="">All types</option>{resourceTypes.map((type) => <option key={type} value={type}>{pretty(type)}</option>)}</select><input value={resourceLocationFilter} onChange={(e) => setResourceLocationFilter(e.target.value)} placeholder="Location filter" /><input type="number" min="1" value={resourceCapacityFilter} onChange={(e) => setResourceCapacityFilter(e.target.value)} placeholder="Minimum capacity" /><select value={resourceStatusFilter} onChange={(e) => setResourceStatusFilter(e.target.value)}><option value="">All status</option>{resourceStatuses.map((status) => <option key={status} value={status}>{pretty(status)}</option>)}</select><div className="row"><button type="button" className="secondary-button" onClick={() => { setResourceSearch(""); setResourceTypeFilter(""); setResourceLocationFilter(""); setResourceCapacityFilter(""); setResourceStatusFilter(""); setResourceSuggestions([]); }}>Clear filters</button></div></div></article><article className="panel"><h3>Bulk Import & Analytics</h3><p>Upload a CSV with columns: name,type,capacity,location,availabilityWindow,status,description.</p><div className="form-grid"><input type="file" accept=".csv,text/csv" onChange={(e) => setResourceCsvFile(e.target.files?.[0] ?? null)} /><button type="button" className="primary-button" onClick={importResources}>Import CSV</button></div><div className="mini-metrics" style={{ marginTop: "16px" }}>{resourceAnalytics.slice(0, 4).map((item) => <div key={item.resourceId}><strong>{item.bookingCount}</strong><span>{item.resourceName}</span></div>)}{resourceAnalytics.length === 0 && <div><strong>0</strong><span>No bookings yet</span></div>}</div></article></section><section className="card-grid">{resources.map((resource, index) => <article key={resource.id} className={`resource-card ${index === 0 && resourceSearch.trim() ? "best-match" : ""}`}><div className="row between"><span className={`pill ${resource.status === "ACTIVE" ? "ok" : "warn"}`}>{pretty(resource.status)}</span><span className="muted">{pretty(resource.type)}</span></div><h3>{resource.name}</h3><p>{resource.description}</p><p><strong>Location:</strong> {resource.location}</p><p><strong>Capacity:</strong> {resource.capacity}</p><p><strong>Availability:</strong> {resource.availabilityWindow}</p><div className="row"><button className="secondary-button" onClick={() => { setActivePage("resources"); setEditingResourceId(resource.id); setResourceForm({ name: resource.name, type: resource.type, capacity: String(resource.capacity), location: resource.location, availabilityWindow: resource.availabilityWindow, status: resource.status, description: resource.description }); }}>Edit</button><button className="danger-button" onClick={() => removeResource(resource.id)}>Delete</button></div></article>)}</section></>}
          {activePage === "bookings" && <section className="two-column"><article className="panel"><h3>Booking Form</h3><form className="form-grid" onSubmit={saveBooking}><select value={bookingForm.resourceId} onChange={(e) => setBookingForm({ ...bookingForm, resourceId: e.target.value })} required><option value="">Select resource</option>{activeResources.map((resource) => <option key={resource.id} value={resource.id}>{resource.name}</option>)}</select><input value={bookingForm.requestedBy} onChange={(e) => setBookingForm({ ...bookingForm, requestedBy: e.target.value })} placeholder="Requested by" required /><select value={bookingForm.department} onChange={(e) => setBookingForm({ ...bookingForm, department: e.target.value })} required><option value="">Select department</option>{departments.map((department) => <option key={department} value={department}>{department}</option>)}</select><select value={bookingForm.academicYear} onChange={(e) => setBookingForm({ ...bookingForm, academicYear: e.target.value })} required><option value="">Select year</option>{academicYears.map((year) => <option key={year} value={year}>{year}</option>)}</select><select value={bookingForm.semester} onChange={(e) => setBookingForm({ ...bookingForm, semester: e.target.value })} required><option value="">Select semester</option>{semesters.map((semester) => <option key={semester} value={semester}>{semester}</option>)}</select><input type="date" value={bookingForm.bookingDate} onChange={(e) => setBookingForm({ ...bookingForm, bookingDate: e.target.value })} required /><div className="row"><input type="time" value={bookingForm.startTime} onChange={(e) => setBookingForm({ ...bookingForm, startTime: e.target.value })} required /><input type="time" value={bookingForm.endTime} onChange={(e) => setBookingForm({ ...bookingForm, endTime: e.target.value })} required /></div><input type="number" min="1" value={bookingForm.expectedAttendees} onChange={(e) => setBookingForm({ ...bookingForm, expectedAttendees: e.target.value })} placeholder="Expected attendees" required /><textarea rows="4" value={bookingForm.purpose} onChange={(e) => setBookingForm({ ...bookingForm, purpose: e.target.value })} placeholder="Purpose" required /><button className="primary-button wide">Submit Booking</button></form></article><article className="panel"><div className="row between"><h3>Booking Queue</h3><select value={bookingFilter} onChange={(e) => setBookingFilter(e.target.value)}><option value="">All</option>{bookingStatuses.map((status) => <option key={status} value={status}>{pretty(status)}</option>)}</select></div><div className="table-list">{bookings.map((booking) => <div key={booking.id} className="table-card"><div><strong>{booking.resource.name}</strong><p>{booking.requestedBy} from {booking.department}</p><p>{booking.academicYear ? `${booking.academicYear} | ${booking.semester ?? ""}` : "Academic year not specified"}</p><p>{booking.bookingDate} | {booking.startTime} - {booking.endTime}</p><p>{booking.purpose}</p></div><div className="table-actions"><span className={`pill ${booking.status === "APPROVED" ? "ok" : booking.status === "REJECTED" ? "muted-pill" : "info"}`}>{pretty(booking.status)}</span>{booking.status === "PENDING" && <><button className="primary-button" onClick={() => updateBooking(booking.id, "APPROVED")}>Approve</button><button className="danger-button" onClick={() => updateBooking(booking.id, "REJECTED")}>Reject</button></>}</div></div>)}</div></article></section>}
          {activePage === "maintenance" && (
            <section className="two-column">
              <article ref={maintenanceFormRef} className="panel">
                <h3>{editingTicketId ? "Edit Maintenance Ticket" : "Maintenance Form"}</h3>
                <form className="form-grid" onSubmit={saveTicket}>
                  <select
                    value={ticketForm.resourceId}
                    onChange={(e) => setTicketForm({ ...ticketForm, resourceId: e.target.value })}
                    required
                  >
                    <option value="">Select resource</option>
                    {resources.map((resource) => (
                      <option key={resource.id} value={resource.id}>
                        {resource.name}
                      </option>
                    ))}
                  </select>
                  <input
                    ref={maintenanceIssueInputRef}
                    value={ticketForm.issueType}
                    onChange={(e) => setTicketForm({ ...ticketForm, issueType: e.target.value })}
                    placeholder="Issue type"
                    required
                  />
                  <input
                    value={ticketForm.reportedBy}
                    onChange={(e) => setTicketForm({ ...ticketForm, reportedBy: e.target.value })}
                    placeholder="Reported by"
                    required
                  />
                  <select
                    value={ticketForm.priority}
                    onChange={(e) => setTicketForm({ ...ticketForm, priority: e.target.value })}
                  >
                    {maintenancePriorities.map((priority) => (
                      <option key={priority} value={priority}>
                        {pretty(priority)}
                      </option>
                    ))}
                  </select>
                  <textarea
                    rows="5"
                    value={ticketForm.description}
                    onChange={(e) => setTicketForm({ ...ticketForm, description: e.target.value })}
                    placeholder="Issue description"
                    required
                  />
                  <div className="row">
                    <button className="primary-button wide">{editingTicketId ? "Update Ticket" : "Create Ticket"}</button>
                    {editingTicketId && (
                      <button type="button" className="secondary-button" onClick={cancelTicketEdit}>
                        Cancel edit
                      </button>
                    )}
                  </div>
                </form>
              </article>
              <article className="panel">
                <div className="row between">
                  <h3>Ticket Queue</h3>
                  <select value={ticketFilter} onChange={(e) => setTicketFilter(e.target.value)}>
                    <option value="">All</option>
                    {maintenanceStatuses.map((status) => (
                      <option key={status} value={status}>
                        {pretty(status)}
                      </option>
                    ))}
                  </select>
                </div>
                <div className="table-list">
                  {tickets.map((ticket) => (
                    <div key={ticket.id} className="table-card">
                      <div>
                        <strong>{ticket.issueType}</strong>
                        <p>{ticket.resource.name}</p>
                        <p>{ticket.description}</p>
                        <p>
                          Reported by {ticket.reportedBy} | Priority {pretty(ticket.priority)}
                        </p>
                        <p className="muted">
                          Technician: {ticket.assignedTechnician ? ticket.assignedTechnician : "Unassigned"}
                        </p>
                        <p className="muted">
                          Created {ticket.createdAt ? new Date(ticket.createdAt).toLocaleString() : "just now"}
                        </p>
                        <p className="muted">{formatSla(ticket)}</p>
                      </div>
                      <div className="table-actions">
                        <span
                          className={`pill ${
                            ticket.status === "RESOLVED" ? "ok" : ticket.status === "IN_PROGRESS" ? "info" : "warn"
                          }`}
                        >
                          {pretty(ticket.status)}
                        </span>
                        {ticket.status === "OPEN" && (
                          <button className="secondary-button" onClick={() => updateTicket(ticket.id, "IN_PROGRESS")}>
                            Start Work
                          </button>
                        )}
                        {ticket.status !== "RESOLVED" && (
                          <button className="primary-button" onClick={() => updateTicket(ticket.id, "RESOLVED")}>
                            Resolve
                          </button>
                        )}
                        {isAdmin && (
                          <>
                            <button type="button" className="secondary-button" onClick={() => startTicketEdit(ticket)}>
                              Edit
                            </button>
                            <button type="button" className="danger-button" onClick={() => removeTicket(ticket.id)}>
                              Delete
                            </button>
                          </>
                        )}
                      </div>
                    </div>
                  ))}
                </div>
              </article>
            </section>
          )}
          {loading && <div className="loading-bar">Loading workspace data...</div>}
        </main>
      </div>
      </div>
    </div>
  );
}
