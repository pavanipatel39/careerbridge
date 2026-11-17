const CAREER_IMAGES = {"career-1":"career-fullstack","career-2":"career-uiux","career-3":"sprint-analytics","career-4":"sprint-backend","career-5":"career-marketing","career-6":"sprint-debug"};
const INTERN_IMAGES = {"intern-1":"sprint-debug","intern-2":"sprint-mobile","intern-3":"sprint-backend","intern-4":"sprint-analytics","intern-5":"sprint-api","intern-6":"sprint-uiux"};
const imgPath = (n, fb) => "assets/images/" + (n || fb) + ".png";
/**
 * CareerBridge - Core Vanilla JavaScript
 * Handles navigation, state, interactive modals, tabs, filters,
 * submissions, authentication simulation, theme switching, and notifications.
 */

// --- Reusable SVG Icons Utility ---
const ICONS = {
  check: `<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"><polyline points="20 6 9 17 4 12"/></svg>`,
  briefcase: `<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="2" y="7" width="20" height="14" rx="2" ry="2"/><path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16"/></svg>`,
  clock: `<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>`,
  dollar: `<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="1" x2="12" y2="23"/><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/></svg>`,
  calendar: `<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>`,
  award: `<svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="8" r="7"/><polyline points="8.21 13.89 7 23 12 20 17 23 15.79 13.88"/></svg>`,
  code: `<svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="16 18 22 12 16 6"/><polyline points="8 6 2 12 8 18"/></svg>`,
  design: `<svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><path d="M12 2a10 10 0 0 1 10 10c0 4-3 6-6 6h-1a2 2 0 0 0-2 2c0 1.1-.9 2-2 2a10 10 0 0 1-9-10C2 6.5 6.5 2 12 2z"/></svg>`,
  data: `<svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><ellipse cx="12" cy="5" rx="9" ry="3"/><path d="M21 12c0 1.66-4 3-9 3s-9-1.34-9-3"/><path d="M3 5v14c0 1.66 4 3 9 3s9-1.34 9-3V5"/></svg>`,
  business: `<svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z"/></svg>`,
  compass: `<svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><polygon points="16.24 7.76 14.12 14.12 7.76 16.24 9.88 9.88 16.24 7.76"/></svg>`,
  info: `<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#38BDF8" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg>`,
  success: `<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#10B981" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></svg>`,
  error: `<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#EF4444" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>`
};

// --- Default Preloaded Application State ---
const DEFAULT_STATE = {
  user: {
    name: "Sarah Sharma",
    email: "sarah.sharma@example.edu",
    role: "Student / Aspiring Engineer",
    headline: "Full-Stack Web Developer & UI Enthusiast",
    location: "Bangalore, India",
    university: "Apex Institute of Technology",
    degree: "B.Tech Computer Science (2023 - 2027)",
    gpa: "8.8 / 10",
    bio: "Passionate computer science student dedicated to building user-friendly, responsive web apps. Actively completing industry-grade micro-internships to bridge the gap between academic theory and production software.",
    github: "github.com/sarah-sharma",
    linkedin: "linkedin.com/in/sarah-sharma-dev",
    readinessScore: 85,
    modulesCompleted: 18,
    totalModules: 25,
    hoursLogged: 124,
    skills: ["HTML5", "Modern CSS", "JavaScript (ES6+)", "React", "Git", "Figma", "Node.js", "SQL"]
  },
  settings: {
    theme: "light",
    emailNotifs: true,
    taskReminders: true,
    weeklyDigest: true,
    smsAlerts: false
  },
  tasks: [
    {
      id: "task-1",
      title: "FinTech Dynamics - Responsive Analytics Dashboard Component",
      company: "FinTech Dynamics",
      duration: "8 Hours",
      reward: "$150 + Verified Certificate",
      deadline: "In 3 Days (Nov 15, 2026)",
      status: "in-progress",
      description: "Build an interactive metrics display with vanilla CSS grid, stat counters, and responsive charts.",
      submission: null
    },
    {
      id: "task-2",
      title: "GreenFuture Studio - Mobile App UX Audit & Wireframes",
      company: "GreenFuture Studio",
      duration: "6 Hours",
      reward: "$120 + Industry Endorsement",
      deadline: "In 5 Days (Nov 17, 2026)",
      status: "in-progress",
      description: "Audit current customer onboarding experience and provide 5 annotated Figma wireframes.",
      submission: null
    },
    {
      id: "task-3",
      title: "DataCorp Analytics - Customer Churn SQL Queries",
      company: "DataCorp Analytics",
      duration: "10 Hours",
      reward: "$200 + Mentorship Call",
      deadline: "Submitted Yesterday",
      status: "under-review",
      description: "Perform cohort analysis on 50,000 subscriber records and identify top 3 cancellation factors.",
      submission: {
        repo: "https://github.com/sarah-sharma/datacorp-churn-sql",
        preview: "https://datacorp-analysis-demo.netlify.app",
        notes: "Completed all 3 SQL scripts and added Markdown summary."
      }
    },
    {
      id: "task-4",
      title: "BrandGrowth Media - 5 SEO Optimized Tech Explainer Articles",
      company: "BrandGrowth Media",
      duration: "5 Hours",
      reward: "$100 + Certificate",
      deadline: "Completed Last Week",
      status: "completed",
      description: "Draft five 800-word guides breaking down cloud computing concepts for non-engineers.",
      submission: {
        repo: "https://drive.google.com/file/d/brandgrowth-docs",
        preview: "https://brandgrowth.io/blog/cloud-guide",
        notes: "Accepted and published with 98/100 reviewer grade."
      }
    }
  ],
  careers: [
    {
      id: "career-1",
      title: "Full Stack Web Developer",
      category: "tech",
      salary: "$95,000 / year",
      duration: "6 Months",
      level: "Intermediate",
      tags: ["HTML/CSS", "JavaScript", "React", "Node.js", "PostgreSQL"],
      description: "Master both client and server software engineering. Build scalable applications from design to deployment.",
      phases: [
        { phase: "Phase 1: Web Fundamentals", items: ["Semantic HTML5, CSS Grid & Flexbox", "JavaScript ES6+ fundamentals", "DOM Manipulation & Async APIs"] },
        { phase: "Phase 2: Frontend Architecture", items: ["Component-driven design", "State management", "Responsive web accessibility (a11y)"] },
        { phase: "Phase 3: Backend & Databases", items: ["RESTful API design in Node.js / Express", "Relational databases & SQL queries", "Authentication & JWT security"] },
        { phase: "Phase 4: Capstone & Micro-Internships", items: ["Deliver 3 company micro-internships", "Full stack capstone portfolio", "Technical interview readiness"] }
      ]
    },
    {
      id: "career-2",
      title: "UI/UX & Product Designer",
      category: "design",
      salary: "$88,000 / year",
      duration: "5 Months",
      level: "All Levels",
      tags: ["Figma", "Design Systems", "User Research", "Wireframing"],
      description: "Craft intuitive, beautiful user experiences and interactive prototypes for web and mobile products.",
      phases: [
        { phase: "Phase 1: Design Fundamentals", items: ["Typography, color theory & spacing", "Visual hierarchy & layout principles", "Heuristic evaluation"] },
        { phase: "Phase 2: Figma Mastery", items: ["Auto-layout & component variants", "Interactive prototyping & smart animate", "Design token systems"] },
        { phase: "Phase 3: User Research & Testing", items: ["Conducting user interviews", "Usability testing & feedback loops", "Information architecture"] },
        { phase: "Phase 4: Real-World Case Studies", items: ["Redesign an existing mobile product", "Complete 2 design audit micro-internships", "Portfolio presentation"] }
      ]
    },
    {
      id: "career-3",
      title: "Data Scientist & AI Specialist",
      category: "data",
      salary: "$105,000 / year",
      duration: "7 Months",
      level: "Advanced",
      tags: ["Python", "SQL", "Pandas", "Scikit-Learn", "Machine Learning"],
      description: "Transform raw data into strategic intelligence and train intelligent machine learning models.",
      phases: [
        { phase: "Phase 1: Data Analytics Foundations", items: ["Python for data analysis", "SQL querying & data aggregation", "Exploratory data analysis (EDA)"] },
        { phase: "Phase 2: Data Visualization", items: ["Matplotlib & Seaborn storytelling", "Interactive dashboards in Tableau/PowerBI", "Statistical significance testing"] },
        { phase: "Phase 3: Machine Learning", items: ["Supervised & unsupervised models", "Feature engineering & cross-validation", "Model metrics & evaluation"] },
        { phase: "Phase 4: Industry Micro-Internships", items: ["Customer churn prediction model", "Real-world dataset cleaning", "Executive presentation deck"] }
      ]
    },
    {
      id: "career-4",
      title: "Cloud & DevOps Engineer",
      category: "tech",
      salary: "$110,000 / year",
      duration: "6 Months",
      level: "Intermediate",
      tags: ["AWS", "Docker", "CI/CD", "Linux", "Kubernetes"],
      description: "Automate delivery pipelines and scale infrastructure reliably across modern cloud providers.",
      phases: [
        { phase: "Phase 1: Linux & Networking", items: ["Bash scripting & Linux commands", "TCP/IP, DNS & HTTP protocols", "Git workflow automation"] },
        { phase: "Phase 2: Containers & Orchestration", items: ["Docker containerization", "Multi-container Docker Compose", "Kubernetes cluster concepts"] },
        { phase: "Phase 3: Cloud & CI/CD", items: ["AWS core services (EC2, S3, IAM)", "GitHub Actions automated CI/CD", "Infrastructure as Code (Terraform)"] }
      ]
    },
    {
      id: "career-5",
      title: "Product Manager",
      category: "business",
      salary: "$100,000 / year",
      duration: "5 Months",
      level: "Intermediate",
      tags: ["Product Strategy", "Agile/Scrum", "User Stories", "Roadmapping"],
      description: "Bridge business, tech, and user needs to lead high-impact products from concept to launch.",
      phases: [
        { phase: "Phase 1: Product Discovery", items: ["Market research & competitor analysis", "User problem definition", "Value proposition design"] },
        { phase: "Phase 2: Product Execution", items: ["Writing PRDs & user stories", "Agile sprint management", "Cross-functional team leadership"] },
        { phase: "Phase 3: Metrics & Growth", items: ["Defining North Star metrics & KPIs", "A/B testing & retention loops", "Go-to-market strategy"] }
      ]
    },
    {
      id: "career-6",
      title: "Cybersecurity Analyst",
      category: "tech",
      salary: "$92,000 / year",
      duration: "6 Months",
      level: "Beginner-Friendly",
      tags: ["Network Security", "Ethical Hacking", "SOC", "CompTIA"],
      description: "Defend organizational assets against modern threats, analyze vulnerabilities, and monitor security incidents.",
      phases: [
        { phase: "Phase 1: Security Fundamentals", items: ["Threat landscapes & attack vectors", "Cryptography & secure protocols", "Identity & access management"] },
        { phase: "Phase 2: Network Defense", items: ["Firewalls, IDS/IPS configuration", "Wireshark packet analysis", "Vulnerability scanning with Nessus"] },
        { phase: "Phase 3: Incident Response", items: ["SIEM log monitoring (Splunk)", "Incident containment procedures", "Digital forensics basics"] }
      ]
    }
  ],
  internships: [
    {
      id: "intern-1",
      title: "Frontend Metrics Dashboard Component",
      company: "FinTech Dynamics",
      logo: "assets/images/company-fintech.svg",
      category: "tech",
      duration: "8 Hours",
      stipend: "$150",
      deadline: "In 4 Days",
      tags: ["HTML/CSS", "JavaScript", "Frontend"],
      verified: true,
      description: "Develop a clean, responsive financial metrics card component that displays currency conversions, percentage changes, and a sparkline chart."
    },
    {
      id: "intern-2",
      title: "Mobile App UX Onboarding Audit",
      company: "GreenFuture Studio",
      logo: "assets/images/company-greenfuture.svg",
      category: "design",
      duration: "6 Hours",
      stipend: "$120",
      deadline: "In 5 Days",
      tags: ["UI/UX", "Figma", "User Research"],
      verified: true,
      description: "Review GreenFuture's sustainability app onboarding flow. Identify friction points and propose 5 annotated wireframes in Figma."
    },
    {
      id: "intern-3",
      title: "Customer Churn SQL Analysis",
      company: "DataCorp Analytics",
      logo: "assets/images/company-datacorp.svg",
      category: "data",
      duration: "10 Hours",
      stipend: "$200",
      deadline: "In 6 Days",
      tags: ["SQL", "Data Analytics", "Reporting"],
      verified: true,
      description: "Execute SQL queries on an e-commerce subscription database to calculate cohort retention rates and draft an executive finding report."
    },
    {
      id: "intern-4",
      title: "Mental Wellness Social Media Content Pack",
      company: "HealthPlus Global",
      logo: "assets/images/company-healthplus.svg",
      category: "marketing",
      duration: "5 Hours",
      stipend: "$100",
      deadline: "In 3 Days",
      tags: ["Marketing", "Social Media", "Copywriting"],
      verified: true,
      description: "Design 6 educational carousel slides and write captions focusing on stress management for university students."
    },
    {
      id: "intern-5",
      title: "REST API Documentation & Postman Collection",
      company: "SaaSify Cloud",
      logo: "assets/images/company-saasify.svg",
      category: "tech",
      duration: "7 Hours",
      stipend: "$140",
      deadline: "In 4 Days",
      tags: ["API", "Postman", "Backend"],
      verified: true,
      description: "Document 8 cloud provisioning API endpoints, build an exportable Postman test collection, and write sample curl requests."
    },
    {
      id: "intern-6",
      title: "Web Accessibility (WCAG 2.1 AA) Review",
      company: "EduTech Next",
      logo: "assets/images/company-edutech.svg",
      category: "tech",
      duration: "5 Hours",
      stipend: "$110",
      deadline: "In 2 Days",
      tags: ["Accessibility", "HTML5", "Quality Assurance"],
      verified: true,
      description: "Run automated and manual keyboard navigation checks on our student portal and submit an actionable remediation report."
    }
  ]
};

// --- Storage Helper Functions ---
function getAppState() {
  const stored = localStorage.getItem("careerbridge_state");
  if (!stored) {
    saveAppState(DEFAULT_STATE);
    return DEFAULT_STATE;
  }
  try {
    return JSON.parse(stored);
  } catch (e) {
    saveAppState(DEFAULT_STATE);
    return DEFAULT_STATE;
  }
}

function saveAppState(state) {
  localStorage.setItem("careerbridge_state", JSON.stringify(state));
}

// --- Backend connection & login session ---
// Works when the page is opened from the Java server (http://localhost:8080), from Live Server, or from a file.
const API_BASE = (location.protocol === "file:" ||
  (/^(localhost|127\.0\.0\.1)$/.test(location.hostname) && ["3000", "5000", "5173", "5500", "5501", "8000", "8081"].includes(location.port)))
  ? "http://localhost:8080" : "";

function setSession(user, provider) {
  localStorage.setItem("careerbridge_session", JSON.stringify({ email: user.email, name: user.name, provider: provider || "local", at: Date.now() }));
}
function getSession() {
  try { return JSON.parse(localStorage.getItem("careerbridge_session")); } catch (e) { return null; }
}
function clearSession() { localStorage.removeItem("careerbridge_session"); }

function apiErrorMessage(err) {
  return err instanceof TypeError
    ? "Cannot reach the server. Start run-windows.bat and open http://localhost:8080"
    : err.message;
}

function finishLogin(data, provider, message) {
  const state = getAppState();
  state.user.name = data.user.name;
  state.user.email = data.user.email;
  saveAppState(state);
  setSession(data.user, provider);
  showToast(message, "success");
  setTimeout(() => { window.location.href = "dashboard.html"; }, 750);
}

// Pages that need a signed-in user: otherwise go to the login page (no more silent default account).
(function guardProtectedPages() {
  const page = location.pathname.split("/").pop();
  const protectedPages = ["dashboard.html", "careers.html", "internships.html", "tasks.html", "progress.html", "profile.html", "settings.html"];
  if (protectedPages.includes(page) && !getSession()) window.location.replace("login.html");
})();

// Real Google sign-in (needs GOOGLE_CLIENT_ID on the server). Without it the button explains what is missing.
async function initGoogleButtons() {
  const buttons = document.querySelectorAll(".google-signin-btn");
  if (!buttons.length) return;
  const notReady = () => buttons.forEach(b => b.addEventListener("click", () =>
    showToast("Google sign-in is not set up yet. Please use email and password.", "info")));
  let clientId = "";
  try {
    const r = await fetch(API_BASE + "/api/config");
    clientId = (await r.json()).googleClientId || "";
  } catch (e) { /* backend not running */ }
  if (!clientId) { notReady(); return; }

  const script = document.createElement("script");
  script.src = "https://accounts.google.com/gsi/client";
  script.async = true;
  script.onerror = notReady;
  script.onload = () => {
    window.google.accounts.id.initialize({
      client_id: clientId,
      callback: (resp) => {
        fetch(API_BASE + "/api/auth/google", { method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify({ credential: resp.credential }) })
          .then(async r => { const data = await r.json(); if (!r.ok) throw new Error(data.message || "Google sign-in failed"); return data; })
          .then(data => finishLogin(data, "google", "Signed in with Google! Loading Dashboard..."))
          .catch(err => showToast(apiErrorMessage(err), "error"));
      }
    });
    buttons.forEach(b => {
      const holder = document.createElement("div");
      holder.className = "google-btn-holder";
      b.replaceWith(holder);
      window.google.accounts.id.renderButton(holder, { theme: "outline", size: "large", width: 320, text: "continue_with" });
    });
  };
  document.head.appendChild(script);
}

// --- Initialize App on DOM Ready ---
document.addEventListener("DOMContentLoaded", () => {
  initTheme();
  initNavigation();
  initAuthForms();
  initGoogleButtons();
  document.querySelectorAll(".sidebar-logout-btn").forEach(a => a.addEventListener("click", clearSession));
  initDashboard();
  initCareers();
  initInternships();
  initTasks();
  initProgress();
  initProfile();
  initSettings();
});

// --- Theme Management ---
function initTheme() {
  const state = getAppState();
  const theme = state.settings.theme || "light";
  document.documentElement.setAttribute("data-theme", theme);

  const themeCheckbox = document.getElementById("theme-toggle-checkbox");
  if (themeCheckbox) {
    themeCheckbox.checked = (theme === "dark");
    themeCheckbox.addEventListener("change", (e) => {
      const newTheme = e.target.checked ? "dark" : "light";
      document.documentElement.setAttribute("data-theme", newTheme);
      state.settings.theme = newTheme;
      saveAppState(state);
      showToast(`Switched to ${newTheme} mode`, "info");
    });
  }
}

// --- Navigation & Header ---
function initNavigation() {
  const hamburgerBtn = document.getElementById("hamburger-btn");
  const navMenu = document.getElementById("nav-menu");
  if (hamburgerBtn && navMenu) {
    hamburgerBtn.addEventListener("click", () => {
      navMenu.classList.toggle("open-mobile");
      hamburgerBtn.setAttribute("aria-expanded", navMenu.classList.contains("open-mobile"));
    });
  }

  const sidebarToggleBtn = document.getElementById("sidebar-toggle-btn");
  const appSidebar = document.getElementById("app-sidebar");
  if (sidebarToggleBtn && appSidebar) {
    sidebarToggleBtn.addEventListener("click", () => {
      appSidebar.classList.toggle("open");
    });
  }

  document.addEventListener("click", (e) => {
    if (appSidebar && appSidebar.classList.contains("open")) {
      if (!appSidebar.contains(e.target) && !e.target.closest("#sidebar-toggle-btn")) {
        appSidebar.classList.remove("open");
      }
    }
  });

  const notifBtn = document.getElementById("notif-btn");
  const notifDropdown = document.getElementById("notif-dropdown");
  if (notifBtn && notifDropdown) {
    notifBtn.addEventListener("click", (e) => {
      e.stopPropagation();
      notifDropdown.classList.toggle("show");
      const counter = document.getElementById("notif-counter");
      if (counter) counter.style.display = "none";
    });

    document.addEventListener("click", (e) => {
      if (!notifDropdown.contains(e.target) && !notifBtn.contains(e.target)) {
        notifDropdown.classList.remove("show");
      }
    });
  }

  document.querySelectorAll(".password-toggle-btn").forEach(btn => {
    btn.addEventListener("click", () => {
      const targetId = btn.getAttribute("data-target");
      const input = document.getElementById(targetId);
      if (input) {
        if (input.type === "password") {
          input.type = "text";
          btn.innerHTML = `<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"/><line x1="1" y1="1" x2="23" y2="23"/></svg>`;
        } else {
          input.type = "password";
          btn.innerHTML = `<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>`;
        }
      }
    });
  });
}

// --- Authentication Pages Simulation ---
function initAuthForms() {
  const loginForm = document.getElementById("login-form");
  if (loginForm) {
    loginForm.addEventListener("submit", (e) => {
      e.preventDefault();
      const email = document.getElementById("login-email")?.value;
      const pass = document.getElementById("login-password")?.value;

      if (!email || !pass) {
        showToast("Please enter both email and password.", "error");
        return;
      }

      fetch(API_BASE + "/api/auth/login", { method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify({ email, password: pass }) })
        .then(async r => { const data = await r.json(); if (!r.ok) throw new Error(data.message || "Login failed"); return data; })
        .then(data => finishLogin(data, "local", "Signed in successfully! Loading Dashboard..."))
        .catch(err => showToast(apiErrorMessage(err), "error"));
    });

    const demoBtn = document.getElementById("demo-login-btn");
    if (demoBtn) {
      demoBtn.addEventListener("click", () => {
        const st = getAppState();
        setSession({ email: st.user.email, name: st.user.name }, "demo");
        showToast("Demo mode: signed in as " + st.user.name + ". Loading dashboard...", "success");
        setTimeout(() => {
          window.location.href = "dashboard.html";
        }, 500);
      });
    }
  }

  const signupForm = document.getElementById("signup-form");
  if (signupForm) {
    signupForm.addEventListener("submit", (e) => {
      e.preventDefault();
      const name = document.getElementById("signup-name")?.value;
      const email = document.getElementById("signup-email")?.value;
      const pass = document.getElementById("signup-password")?.value;

      if (!name || !email || !pass) {
        showToast("Please complete all required fields.", "error");
        return;
      }

      fetch(API_BASE + "/api/auth/register", { method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify({ name, email, password: pass }) })
        .then(async r => { const data = await r.json(); if (!r.ok) throw new Error(data.message || "Registration failed"); return data; })
        .then(data => finishLogin(data, "local", "Account created successfully! Welcome to CareerBridge."))
        .catch(err => showToast(apiErrorMessage(err), "error"));
    });
  }

  const forgotForm = document.getElementById("forgot-password-form");
  if (forgotForm) {
    forgotForm.addEventListener("submit", (e) => {
      e.preventDefault();
      const email = document.getElementById("forgot-email")?.value;
      if (!email) {
        showToast("Please enter your registered email address.", "error");
        return;
      }

      const container = document.getElementById("forgot-form-container");
      const successBox = document.getElementById("forgot-success-box");
      const sentEmailSpan = document.getElementById("sent-email-target");

      if (container && successBox) {
        if (sentEmailSpan) sentEmailSpan.textContent = email;
        container.style.display = "none";
        successBox.style.display = "block";
        showToast("Password reset link sent!", "success");
      }
    });
  }
}

// --- Dashboard Home View ---
function initDashboard() {
  const dashContainer = document.getElementById("dashboard-page");
  if (!dashContainer) return;

  const state = getAppState();

  const nameDisplays = document.querySelectorAll(".dynamic-user-name");
  nameDisplays.forEach(el => el.textContent = state.user.name);

  const taskListEl = document.getElementById("dash-active-tasks-list");
  if (taskListEl) {
    const activeTasks = state.tasks.filter(t => t.status !== "completed").slice(0, 2);
    taskListEl.innerHTML = activeTasks.map(task => `
      <div class="task-card">
        <div class="task-info-left">
          <div class="task-status-indicator ${task.status === 'in-progress' ? 'status-in-progress' : 'status-under-review'}"></div>
          <div class="task-details">
            <h4>${escapeHTML(task.title)}</h4>
            <div class="task-meta">
              <span>${ICONS.briefcase} ${escapeHTML(task.company)}</span>
              <span>${ICONS.clock} ${escapeHTML(task.deadline)}</span>
              <span>${ICONS.dollar} ${escapeHTML(task.reward)}</span>
            </div>
          </div>
        </div>
        <div>
          ${task.status === 'in-progress' 
            ? `<button class="btn btn-primary-orange btn-sm" onclick="openSubmitModal('${task.id}')">Submit Work</button>`
            : `<span class="badge badge-blue">Under Review</span>`
          }
        </div>
      </div>
    `).join("");
  }
}

// --- Explore Career Paths View ---
function initCareers() {
  const careersGrid = document.getElementById("careers-grid");
  if (!careersGrid) return;

  const state = getAppState();

  function renderCareers(filterCategory = "all", searchQuery = "") {
    let filtered = state.careers;

    if (filterCategory !== "all") {
      filtered = filtered.filter(c => c.category === filterCategory);
    }

    if (searchQuery.trim() !== "") {
      const q = searchQuery.toLowerCase();
      filtered = filtered.filter(c => 
        c.title.toLowerCase().includes(q) || 
        c.description.toLowerCase().includes(q) ||
        c.tags.some(t => t.toLowerCase().includes(q))
      );
    }

    if (filtered.length === 0) {
      careersGrid.innerHTML = `
        <div style="grid-column: 1/-1; text-align: center; padding: 48px; background: var(--bg-card); border-radius: var(--radius-lg); border: 1px dashed var(--border-color);">
          <p style="font-size: 1.05rem; color: var(--text-muted); margin-bottom: 12px;">No career paths match your criteria.</p>
          <button class="btn btn-outline btn-sm" onclick="resetCareerFilters()">Clear Filters</button>
        </div>
      `;
      return;
    }

    careersGrid.innerHTML = filtered.map(career => `
      <div class="career-card">
        <div class="card-cover"><img src="${imgPath(CAREER_IMAGES[career.id], 'career-fullstack')}" alt="${escapeHTML(career.title)}" loading="lazy"></div>
        <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 6px;">
          <h3 class="career-card-title">${escapeHTML(career.title)}</h3>
          <span class="badge badge-blue">${escapeHTML(career.level)}</span>
        </div>
        <p class="career-card-desc">${escapeHTML(career.description)}</p>
        
        <div style="display: flex; gap: 14px; font-size: 0.85rem; color: var(--text-secondary); margin-bottom: 16px; align-items: center;">
          <span style="display: inline-flex; align-items: center; gap: 4px;">${ICONS.dollar} <strong>${escapeHTML(career.salary)}</strong></span>
          <span style="display: inline-flex; align-items: center; gap: 4px;">${ICONS.calendar} ${escapeHTML(career.duration)}</span>
        </div>

        <div class="tag-cloud">
          ${career.tags.map(t => `<span class="tag">${escapeHTML(t)}</span>`).join("")}
        </div>

        <div style="margin-top: auto; display: flex; gap: 10px;">
          <button class="btn btn-primary-blue btn-sm" style="flex: 1;" onclick="openRoadmapModal('${career.id}')">
            View Roadmap
          </button>
          <button class="btn btn-outline btn-sm" onclick="setGoalTrack('${escapeHTML(career.title)}')">
            Set Goal
          </button>
        </div>
      </div>
    `).join("");
  }

  renderCareers();

  const filterTabs = document.querySelectorAll("#career-filter-tabs .tab-btn");
  filterTabs.forEach(tab => {
    tab.addEventListener("click", () => {
      filterTabs.forEach(t => t.classList.remove("active"));
      tab.classList.add("active");
      const category = tab.getAttribute("data-category");
      const searchInput = document.getElementById("careers-search-input");
      renderCareers(category, searchInput ? searchInput.value : "");
    });
  });

  const searchInput = document.getElementById("careers-search-input");
  if (searchInput) {
    searchInput.addEventListener("input", (e) => {
      const activeTab = document.querySelector("#career-filter-tabs .tab-btn.active");
      const category = activeTab ? activeTab.getAttribute("data-category") : "all";
      renderCareers(category, e.target.value);
    });
  }

  window.resetCareerFilters = () => {
    filterTabs.forEach(t => t.classList.remove("active"));
    const allTab = document.querySelector("#career-filter-tabs .tab-btn[data-category='all']");
    if (allTab) allTab.classList.add("active");
    if (searchInput) searchInput.value = "";
    renderCareers("all", "");
  };
}

// --- Micro-Internships View ---
function initInternships() {
  const internGrid = document.getElementById("internships-grid");
  if (!internGrid) return;

  const state = getAppState();

  function renderInternships(category = "all", searchQuery = "") {
    let list = state.internships;

    if (category !== "all") {
      list = list.filter(i => i.category === category);
    }

    if (searchQuery.trim() !== "") {
      const q = searchQuery.toLowerCase();
      list = list.filter(i => 
        i.title.toLowerCase().includes(q) ||
        i.company.toLowerCase().includes(q) ||
        i.description.toLowerCase().includes(q) ||
        i.tags.some(t => t.toLowerCase().includes(q))
      );
    }

    if (list.length === 0) {
      internGrid.innerHTML = `
        <div style="grid-column: 1/-1; text-align: center; padding: 48px; background: var(--bg-card); border-radius: var(--radius-lg); border: 1px dashed var(--border-color);">
          <p style="font-size: 1.05rem; color: var(--text-muted); margin-bottom: 12px;">No micro-internships found for your query.</p>
          <button class="btn btn-outline btn-sm" onclick="resetInternshipFilters()">Reset Filters</button>
        </div>
      `;
      return;
    }

    internGrid.innerHTML = list.map(item => `
      <div class="internship-card">
        <div class="card-cover"><img src="${imgPath(INTERN_IMAGES[item.id], 'sprint-debug')}" alt="${escapeHTML(item.title)}" loading="lazy"></div>
        <div class="internship-header">
          <div class="company-logo-wrap">
            <img src="${item.logo}" alt="${escapeHTML(item.company)}" onerror="this.src='assets/icons/logo.svg'">
          </div>
          <div class="company-info">
            <h4>${escapeHTML(item.company)}</h4>
            <div class="company-meta">
              <span>${ICONS.clock} ${escapeHTML(item.duration)}</span>
              <span>•</span>
              <span>${ICONS.calendar} ${escapeHTML(item.deadline)}</span>
            </div>
          </div>
        </div>

        <h3 class="internship-title">${escapeHTML(item.title)}</h3>
        <p class="internship-desc">${escapeHTML(item.description)}</p>

        <div class="tag-cloud">
          ${item.tags.map(t => `<span class="tag">${escapeHTML(t)}</span>`).join("")}
        </div>

        <div class="internship-pills">
          <div class="reward-info">
            <span class="reward-amount">${escapeHTML(item.stipend)}</span>
            <span class="reward-duration">Verified Certificate</span>
          </div>
          <button class="btn btn-primary-orange btn-sm" onclick="openApplyModal('${item.id}')">
            Apply Now
          </button>
        </div>
      </div>
    `).join("");
  }

  renderInternships();

  const filterTabs = document.querySelectorAll("#intern-filter-tabs .tab-btn");
  filterTabs.forEach(tab => {
    tab.addEventListener("click", () => {
      filterTabs.forEach(t => t.classList.remove("active"));
      tab.classList.add("active");
      const category = tab.getAttribute("data-category");
      const searchInput = document.getElementById("intern-search-input");
      renderInternships(category, searchInput ? searchInput.value : "");
    });
  });

  const searchInput = document.getElementById("intern-search-input");
  if (searchInput) {
    searchInput.addEventListener("input", (e) => {
      const activeTab = document.querySelector("#intern-filter-tabs .tab-btn.active");
      const category = activeTab ? activeTab.getAttribute("data-category") : "all";
      renderInternships(category, e.target.value);
    });
  }

  window.resetInternshipFilters = () => {
    filterTabs.forEach(t => t.classList.remove("active"));
    const allTab = document.querySelector("#intern-filter-tabs .tab-btn[data-category='all']");
    if (allTab) allTab.classList.add("active");
    if (searchInput) searchInput.value = "";
    renderInternships("all", "");
  };
}

// --- Tasks View ---
function initTasks() {
  const taskContainer = document.getElementById("tasks-page-list");
  if (!taskContainer) return;

  const state = getAppState();

  function renderTasks(filterStatus = "all") {
    let list = state.tasks;
    if (filterStatus !== "all") {
      list = list.filter(t => t.status === filterStatus);
    }

    if (list.length === 0) {
      taskContainer.innerHTML = `
        <div style="text-align: center; padding: 48px; background: var(--bg-card); border-radius: var(--radius-lg); border: 1px dashed var(--border-color);">
          <p style="font-size: 1.05rem; color: var(--text-muted);">No tasks in this category.</p>
        </div>
      `;
      return;
    }

    taskContainer.innerHTML = list.map(task => {
      let badgeClass = "badge-yellow";
      let statusText = "In Progress";
      if (task.status === "under-review") {
        badgeClass = "badge-blue";
        statusText = "Under Review";
      } else if (task.status === "completed") {
        badgeClass = "badge-green";
        statusText = "Completed & Verified";
      }

      return `
        <div class="task-card">
          <div class="task-info-left" style="align-items: flex-start;">
            <div class="task-status-indicator ${task.status === 'in-progress' ? 'status-in-progress' : (task.status === 'under-review' ? 'status-under-review' : 'status-completed')}" style="margin-top: 6px;"></div>
            <div class="task-details">
              <div style="display: flex; gap: 10px; align-items: center; margin-bottom: 6px;">
                <h4 style="font-size: 1.1rem;">${escapeHTML(task.title)}</h4>
                <span class="badge ${badgeClass}">${statusText}</span>
              </div>
              <p style="font-size: 0.9rem; color: var(--text-secondary); margin-bottom: 8px;">${escapeHTML(task.description)}</p>
              <div class="task-meta">
                <span>${ICONS.briefcase} ${escapeHTML(task.company)}</span>
                <span>${ICONS.clock} ${escapeHTML(task.duration)}</span>
                <span>${ICONS.dollar} ${escapeHTML(task.reward)}</span>
                <span>${ICONS.calendar} ${escapeHTML(task.deadline)}</span>
              </div>
            </div>
          </div>
          <div>
            ${task.status === 'in-progress' ? `
              <button class="btn btn-primary-orange btn-sm" onclick="openSubmitModal('${task.id}')">
                Submit Work
              </button>
            ` : (task.status === 'under-review' ? `
              <button class="btn btn-outline btn-sm" onclick="viewSubmissionDetails('${task.id}')">
                View Submission
              </button>
            ` : `
              <button class="btn btn-primary-blue btn-sm" onclick="downloadCertificate('${escapeHTML(task.company)}')">
                ${ICONS.award} View Certificate
              </button>
            `)}
          </div>
        </div>
      `;
    }).join("");
  }

  renderTasks();

  const taskTabs = document.querySelectorAll("#task-status-tabs .tab-btn");
  taskTabs.forEach(tab => {
    tab.addEventListener("click", () => {
      taskTabs.forEach(t => t.classList.remove("active"));
      tab.classList.add("active");
      const status = tab.getAttribute("data-status");
      renderTasks(status);
    });
  });
}

// --- Progress & Analytics View ---
function initProgress() {
  const progressPage = document.getElementById("progress-page");
  if (!progressPage) return;

  setTimeout(() => {
    document.querySelectorAll(".skill-meter-fill").forEach(bar => {
      const width = bar.getAttribute("data-width");
      if (width) bar.style.width = width;
    });
  }, 200);
}

// --- Profile View ---
function initProfile() {
  const profilePage = document.getElementById("profile-page");
  if (!profilePage) return;

  const state = getAppState();

  const nameEl = document.getElementById("prof-name");
  const headlineEl = document.getElementById("prof-headline");
  const bioEl = document.getElementById("prof-bio");
  const locationEl = document.getElementById("prof-location");
  const eduEl = document.getElementById("prof-edu");

  if (nameEl) nameEl.textContent = state.user.name;
  if (headlineEl) headlineEl.textContent = state.user.headline;
  if (bioEl) bioEl.textContent = state.user.bio;
  if (locationEl) locationEl.textContent = state.user.location;
  if (eduEl) eduEl.textContent = state.user.degree;

  const editProfBtn = document.getElementById("edit-profile-btn");
  const editModal = document.getElementById("edit-profile-modal");
  const editForm = document.getElementById("edit-profile-form");

  if (editProfBtn && editModal) {
    editProfBtn.addEventListener("click", () => {
      document.getElementById("edit-name").value = state.user.name;
      document.getElementById("edit-headline").value = state.user.headline;
      document.getElementById("edit-bio").value = state.user.bio;
      document.getElementById("edit-location").value = state.user.location;
      editModal.classList.add("show");
    });

    if (editForm) {
      editForm.addEventListener("submit", (e) => {
        e.preventDefault();
        state.user.name = document.getElementById("edit-name").value;
        state.user.headline = document.getElementById("edit-headline").value;
        state.user.bio = document.getElementById("edit-bio").value;
        state.user.location = document.getElementById("edit-location").value;

        saveAppState(state);
        editModal.classList.remove("show");
        showToast("Profile updated successfully!", "success");

        if (nameEl) nameEl.textContent = state.user.name;
        if (headlineEl) headlineEl.textContent = state.user.headline;
        if (bioEl) bioEl.textContent = state.user.bio;
        if (locationEl) locationEl.textContent = state.user.location;
      });
    }
  }
}

// --- Settings View ---
function initSettings() {
  const settingsPage = document.getElementById("settings-page");
  if (!settingsPage) return;

  const state = getAppState();

  const setName = document.getElementById("settings-name");
  const setEmail = document.getElementById("settings-email");
  const setHeadline = document.getElementById("settings-headline");

  if (setName) setName.value = state.user.name;
  if (setEmail) setEmail.value = state.user.email;
  if (setHeadline) setHeadline.value = state.user.headline;

  const form = document.getElementById("account-settings-form");
  if (form) {
    form.addEventListener("submit", (e) => {
      e.preventDefault();
      state.user.name = setName.value;
      state.user.email = setEmail.value;
      state.user.headline = setHeadline.value;
      saveAppState(state);
      showToast("Account settings saved!", "success");
    });
  }

  const pwForm = document.getElementById("password-change-form");
  if (pwForm) {
    pwForm.addEventListener("submit", (e) => {
      e.preventDefault();
      const current = document.getElementById("cur-pass").value;
      const newP = document.getElementById("new-pass").value;
      const confP = document.getElementById("conf-pass").value;

      if (newP !== confP) {
        showToast("New passwords do not match.", "error");
        return;
      }
      pwForm.reset();
      showToast("Password updated successfully!", "success");
    });
  }
}

// ==========================================================================
// Interactive Modals Logic
// ==========================================================================

// 1. Roadmap Modal
window.openRoadmapModal = (careerId) => {
  const state = getAppState();
  const career = state.careers.find(c => c.id === careerId);
  if (!career) return;

  const modal = document.getElementById("roadmap-modal");
  const content = document.getElementById("roadmap-modal-content");
  if (!modal || !content) return;

  content.innerHTML = `
    <div style="margin-bottom: 20px;">
      <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 8px;">
        <h3 style="font-size: 1.4rem;">${escapeHTML(career.title)}</h3>
        <span class="badge badge-blue">${escapeHTML(career.level)}</span>
      </div>
      <p style="font-size: 0.95rem; color: var(--text-secondary); margin-bottom: 12px;">${escapeHTML(career.description)}</p>
      <div style="display: flex; gap: 16px; font-size: 0.88rem; color: var(--text-muted); align-items: center;">
        <span style="display: inline-flex; align-items: center; gap: 4px;">${ICONS.dollar} Expected Salary: <strong>${escapeHTML(career.salary)}</strong></span>
        <span style="display: inline-flex; align-items: center; gap: 4px;">${ICONS.clock} Estimated Duration: <strong>${escapeHTML(career.duration)}</strong></span>
      </div>
    </div>

    <h4 style="font-size: 1.05rem; margin-bottom: 14px; color: var(--text-primary);">Curriculum & Project Milestones</h4>
    <div style="display: flex; flex-direction: column; gap: 14px;">
      ${career.phases.map((p, idx) => `
        <div style="background: var(--bg-subtle); border-radius: var(--radius-md); padding: 14px 18px; border-left: 4px solid var(--primary);">
          <h5 style="font-size: 0.95rem; font-weight: 700; color: var(--text-primary); margin-bottom: 6px;">${escapeHTML(p.phase)}</h5>
          <ul style="padding-left: 18px; font-size: 0.88rem; color: var(--text-secondary); display: flex; flex-direction: column; gap: 4px;">
            ${p.items.map(i => `<li>${escapeHTML(i)}</li>`).join("")}
          </ul>
        </div>
      `).join("")}
    </div>
  `;

  modal.classList.add("show");
};

// 2. Micro-Internship Application Modal
window.openApplyModal = (internshipId) => {
  const state = getAppState();
  const item = state.internships.find(i => i.id === internshipId);
  if (!item) return;

  const modal = document.getElementById("apply-modal");
  const titleEl = document.getElementById("apply-modal-title");
  const companyEl = document.getElementById("apply-modal-company");
  const form = document.getElementById("apply-modal-form");
  const hiddenId = document.getElementById("apply-internship-id");

  if (!modal) return;
  if (titleEl) titleEl.textContent = item.title;
  if (companyEl) companyEl.textContent = `${item.company} • ${item.duration} • ${item.stipend}`;
  if (hiddenId) hiddenId.value = internshipId;

  modal.classList.add("show");

  if (form) {
    form.onsubmit = (e) => {
      e.preventDefault();
      const newTask = {
        id: "task-" + Date.now(),
        title: `${item.company} - ${item.title}`,
        company: item.company,
        duration: item.duration,
        reward: `${item.stipend} + Verified Certificate`,
        deadline: "In 7 Days",
        status: "in-progress",
        description: item.description,
        submission: null
      };

      fetch(API_BASE + "/api/applications", { method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify({ email: state.user.email, internshipId }) })
        .then(async response => { const data = await response.json(); if (!response.ok) throw new Error(data.message || "Application could not be saved"); return data; })
        .then(() => {
          state.tasks.unshift(newTask);
          saveAppState(state);
          modal.classList.remove("show");
          form.reset();
          showToast(`Applied successfully to ${item.company}! Added to your active tasks.`, "success");
        })
        .catch(error => showToast(apiErrorMessage(error), "error"));
    };
  }
};

// 3. Submit Work Modal
window.openSubmitModal = (taskId) => {
  const state = getAppState();
  const task = state.tasks.find(t => t.id === taskId);
  if (!task) return;

  const modal = document.getElementById("submit-modal");
  const titleEl = document.getElementById("submit-task-title");
  const form = document.getElementById("submit-work-form");
  const hiddenId = document.getElementById("submit-task-id");

  if (!modal) return;
  if (titleEl) titleEl.textContent = task.title;
  if (hiddenId) hiddenId.value = taskId;

  modal.classList.add("show");

  if (form) {
    form.onsubmit = (e) => {
      e.preventDefault();
      const repoUrl = document.getElementById("submit-repo-url").value;
      const liveUrl = document.getElementById("submit-live-url").value;
      const notes = document.getElementById("submit-notes").value;

      task.status = "under-review";
      task.submission = {
        repo: repoUrl,
        preview: liveUrl,
        notes: notes
      };

      saveAppState(state);
      modal.classList.remove("show");
      form.reset();
      showToast("Work submitted for review! Employer will verify within 48 hours.", "success");

      initTasks();
      initDashboard();
    };
  }
};

// 4. View Submission Modal
window.viewSubmissionDetails = (taskId) => {
  const state = getAppState();
  const task = state.tasks.find(t => t.id === taskId);
  if (!task || !task.submission) return;

  alert(`Submission for: ${task.title}\n\nRepository: ${task.submission.repo}\nPreview URL: ${task.submission.preview}\nNotes: ${task.submission.notes}\nStatus: Under Review by Mentor`);
};

// 5. Download Certificate Simulation
window.downloadCertificate = (company) => {
  showToast(`Generating verified credential certificate for ${company}...`, "info");
  setTimeout(() => {
    showToast(`Certificate downloaded! Verified on blockchain ledger.`, "success");
  }, 1000);
};

// 6. Set Goal Track
window.setGoalTrack = (trackTitle) => {
  showToast(`Goal set to "${trackTitle}"! Roadmap updated.`, "success");
};

// Global Close Modals
window.closeModal = (modalId) => {
  const modal = document.getElementById(modalId);
  if (modal) modal.classList.remove("show");
};

document.addEventListener("keydown", (e) => {
  if (e.key === "Escape") {
    document.querySelectorAll(".modal-backdrop.show").forEach(m => m.classList.remove("show"));
  }
});

document.addEventListener("click", (e) => {
  if (e.target.classList.contains("modal-backdrop")) {
    e.target.classList.remove("show");
  }
});

// --- Toast Notification Helper ---
function showToast(message, type = "info") {
  let container = document.getElementById("toast-container");
  if (!container) {
    container = document.createElement("div");
    container.id = "toast-container";
    container.className = "toast-container";
    document.body.appendChild(container);
  }

  const toast = document.createElement("div");
  toast.className = `toast ${type === 'success' ? 'toast-success' : (type === 'error' ? 'toast-error' : '')}`;
  
  let icon = ICONS.info;
  if (type === "success") icon = ICONS.success;
  if (type === "error") icon = ICONS.error;

  toast.innerHTML = `<span>${icon}</span> <span>${escapeHTML(message)}</span>`;
  container.appendChild(toast);

  setTimeout(() => {
    toast.style.opacity = "0";
    toast.style.transform = "translateY(10px)";
    toast.style.transition = "all 0.25s ease";
    setTimeout(() => toast.remove(), 250);
  }, 3500);
}

// --- Icon Helpers ---
function getCareerIcon(category) {
  switch (category) {
    case "tech": return ICONS.code;
    case "design": return ICONS.design;
    case "data": return ICONS.data;
    case "business": return ICONS.business;
    default: return ICONS.compass;
  }
}

// --- Sanitization Helper ---
function escapeHTML(str) {
  if (!str) return "";
  return str.toString()
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;")
    .replace(/'/g, "&#039;");
}
