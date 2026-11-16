# Contributing: Fork and Pull Request Workflow

We use the **fork workflow**. Everyone works in their own copy (fork) of the repo, and changes come back to the main repo through **pull requests (PRs)**. CI checks every PR automatically before it can be merged.

Main repo: **https://github.com/Duaarondu/mental_health_app**

Commands below are for Windows PowerShell. They work the same on Mac and Linux, except use `./mvnw` instead of `.\mvnw.cmd`.

---

## One-time setup

### 1. Install the tools
- [Git](https://git-scm.com/downloads), or `winget install Git.Git`
- **Either** [Docker Desktop](https://www.docker.com/products/docker-desktop/) **or** JDK 21+ and Node.js 20.19+

Tell Git who you are (only needed once per computer):
```
git config --global user.name "Your Name"
git config --global user.email "you@example.com"
```

### 2. Fork the repo
Go to https://github.com/Duaarondu/mental_health_app and click **Fork** (top right), then **Create fork**.
You now have your own copy at `https://github.com/YOUR-USERNAME/mental_health_app`.

### 3. Clone YOUR fork
```
git clone https://github.com/YOUR-USERNAME/mental_health_app.git
cd mental_health_app
```

### 4. Connect it to the main repo
This lets you pull in everyone else's changes:
```
git remote add upstream https://github.com/Duaarondu/mental_health_app.git
git remote -v
```
You should see `origin` (your fork) and `upstream` (the main repo).

### 5. Check that it runs
```
docker compose up --build
```
Open http://localhost:3000. (Or run without Docker; see the README.)

---

## Every time you work on something

### 1. Get the latest code
```
git checkout main
git pull upstream main
git push origin main
```

### 2. Make a branch for your task
Name it after what you're doing:
```
git checkout -b feature/login-page
```
Examples: `feature/mood-tracker`, `fix/date-format`, `docs/readme-update`

### 3. Make your changes, then commit
```
git add .
git commit -m "Add login page with form validation"
```
Commit often, with short messages that say what changed.

### 4. Push the branch to your fork
```
git push -u origin feature/login-page
```

### 5. Open a pull request
1. Go to your fork on GitHub. You'll see a **Compare & pull request** button. Click it.
2. Make sure it says: **base repository: `Duaarondu/mental_health_app`, base: `main`** ← **your branch**
3. Write what you changed and how to test it, then click **Create pull request**.

### 6. Wait for CI and review
- The **checks** at the bottom of the PR will run (backend tests, frontend build, Docker build).
- ❌ If a check fails, click **Details** to see why, fix it on the same branch, then commit and `git push` again. The PR updates by itself.
- ✅ When checks pass and a teammate approves, the repo owner merges it.

### 7. After your PR is merged, clean up
```
git checkout main
git pull upstream main
git push origin main
git branch -d feature/login-page
```

---

## If you get a merge conflict

A conflict means you and someone else changed the same lines. To fix it:
```
git checkout feature/your-branch
git pull upstream main
```
Git marks the conflicting parts in the files like this:
```
<<<<<<< HEAD
your version
=======
their version
>>>>>>> upstream/main
```
Edit the file to keep the right code, delete the `<<<<<<<`, `=======`, `>>>>>>>` lines, then:
```
git add .
git commit -m "Resolve merge conflict"
git push
```

---

## Team rules

1. **Never push directly to `main`.** Always use a branch and a PR.
2. **One task per branch, one branch per PR.** Small PRs are easier to review.
3. **Pull from `upstream` before starting** new work.
4. **Don't merge a PR with failing checks.**
5. **Don't commit** `target/`, `node_modules/`, `dist/`, `.idea/` or `.vscode/`. The `.gitignore` already handles this, so just don't force-add them.
6. Test your change locally before opening a PR.
