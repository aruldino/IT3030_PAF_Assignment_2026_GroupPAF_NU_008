Here’s a **clean, structured “master prompt”** you can reuse (for ChatGPT or your team) to turn that messy monorepo into a properly organized, multi-module project with a meaningful Git history.

---

## 🔧 **Project Restructuring Master Prompt**

You are a senior software architect and Git expert. I have an existing project called **Smart Campus** that was incorrectly implemented as a monorepo. I need to restructure it into a clean, modular architecture with proper Git workflows and contribution history for 4 team members.

### 🎯 **Objectives**

1. Extract usable **backend** and **frontend** code from the existing monorepo.
2. Convert the project into a **modular architecture** with clear separation.
3. Ensure a **proper Git commit graph** showing contributions from 4 developers.
4. Assign each module to a dedicated branch and contributor.
5. Follow industry best practices (clean architecture, scalability, maintainability).

---

## 👥 **Team Members (Git Authors)**

* [vinushan2218@gmail.com](mailto:vinushan2218@gmail.com)
* [karunakanthanlavan@gmail.com](mailto:karunakanthanlavan@gmail.com)
* [pakeerathansinthujan2003@gmail.com](mailto:pakeerathansinthujan2003@gmail.com)
* [aruldino2025@gmail.com](mailto:aruldino2025@gmail.com)

Each member must have meaningful commits in the Git history.

---

## 🧩 **Modules & Ownership**

Split the system into the following modules:

* **Module A – Facilities & Assets Catalogue**
* **Module B – Booking Management**
* **Module C – Maintenance & Incident Ticketing**
* **Module D – Notifications & Authentication**

> Note: Combine Module D & E if needed to match 4 contributors, or assign shared responsibility.

---

## 🏗️ **Target Architecture**

* Separate repositories or folders:

  * `/backend`
  * `/frontend`
* Backend structured using modular architecture (e.g., feature-based or microservices-ready)
* Frontend structured using component-based architecture

### Backend Example Structure:

```
backend/
  src/
    modules/
      facilities/
      booking/
      maintenance/
      notifications/
      auth/
    common/
    config/
```

### Frontend Example Structure:

```
frontend/
  src/
    modules/
      facilities/
      booking/
      maintenance/
      notifications/
      auth/
    shared/
```

---

## 🔀 **Git Strategy**

1. Initialize a new clean repository.

2. Create branches:

   * `module-facilities`
   * `module-booking`
   * `module-maintenance`
   * `module-notifications-auth`

3. Each branch:

   * Contains only relevant module code
   * Has multiple commits (not one bulk commit)
   * Uses proper commit messages:

     * `feat: add booking API`
     * `fix: resolve asset duplication bug`
     * `refactor: improve auth middleware`

4. Use **different Git authors** for commits:

   ```
   git config user.name "Member Name"
   git config user.email "email@example.com"
   ```

5. Merge branches into `main` via pull requests (or simulate them if needed).

---

## 🔄 **Migration Steps**

1. Analyze old monorepo:

   * Identify backend and frontend code
   * Remove unnecessary/duplicate files

2. Extract:

   * Backend → clean service structure
   * Frontend → reusable UI modules

3. Refactor:

   * Remove tight coupling
   * Introduce APIs between modules
   * Standardize naming conventions

4. Rebuild commit history:

   * Break changes into logical steps
   * Assign commits to team members

---

## ✅ **Quality Requirements**

* Follow RESTful API design
* Use environment-based configuration
* Ensure modular independence
* Maintain consistent coding standards
* Add basic README for each module

---

## 📦 **Expected Output**

* Clean project structure (frontend + backend separated)
* 4 well-defined branches with meaningful commits
* Realistic Git graph showing collaboration
* Modular, scalable codebase ready for extension

---

## ⚠️ **Constraints**

* Do NOT keep monorepo structure
* Do NOT commit everything at once
* Do NOT mix unrelated modules in a single branch

---

## 🚀 **Bonus (if possible)**

* Add Docker setup
* Add API documentation (Swagger/Postman)
* Add simple CI pipeline

---

If you want, I can go one level deeper and give you **exact Git commands step-by-step** (like a script you can literally copy-paste to build the commit graph perfectly).
