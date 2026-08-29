# AGENTS.md — Workspace Guide for AI Coding Agents (Codex, etc.)

*(This file is the canonical source. `CLAUDE.md` and `.clinerules/project-rules.md`
mirror it for Claude Code and Cline respectively — update all three together if
these rules change. `.github/copilot-instructions.md` and `.cursorrules` are thin
pointers back to this file for Copilot/Cursor.)*

Read this before doing anything in this workspace. It applies regardless of which
AI coding tool opened it (Claude Code, Codex, Cline, Copilot, Cursor, or any other
agent) — these are the user's standing rules for this project, not tool-specific
preferences.

## What this workspace is

`Springboot Gradle Lab` is **both** a real, working Spring Boot + Gradle
microservice (`src/main/java/com/gradle/...`) **and** the home of a
self-maintained, four-file interview-prep reference built on top of it. The
user is a Java/Spring Boot backend developer (works on a real production
system — the "HDFC" loan-origination project, referenced throughout these
notes — for Intellect Design Arena) using this workspace to **prepare for
interviews and build durable, memorable understanding**, not just to ship
code. Treat requests through that lens: explain *why*, not just *what*; keep
language simple; ground answers in real code wherever possible.

## The four documentation files — never create a fifth

Each has ONE fixed job. Fold new material into the matching file's existing
structure — **never spin up a new standalone `.md` file** for "urgent" notes,
a "temporary" cram sheet, or to keep one topic separate. (A prior stray file,
`3-ioc-container--dependency-injection.md`, was an empty accidental split of
what already lives in file 1's §3 — it was removed; don't repeat that
mistake.)

1. **`Springboot Gradle Lab.md`** — Spring Boot concepts grounded in *this
   repo's own code* (`src/main/java/com/gradle/...`). Chapter-wise, organized
   under 8 umbrellas (Orientation → Core Fundamentals → Web Layer →
   Cross-Cutting → Messaging → Data → Config/Build/Testing → Interview/System
   Design Bridge) with a jump-link ToC and Mermaid diagrams. Every chapter
   states where the concept lives in this project's actual source.
2. **`System Design.md`** — HLD/LLD interview Q&A. Format: *Question → plain
   English answer → diagram/table only when it genuinely helps*. Every
   question tagged `[HLD]` or `[LLD]`. Organized into Parts, with Part 5 as
   the capstone: real, grounded discussion of the actual HDFC production
   project (architecture, VAPT findings, real file/method references like
   `InterfaceServiceDispatcher.java`). Uses 🏦 for real-project grounding
   callouts and 🧠 for "memorize this line" one-liners.
3. **`cloud.md`** — AWS/GCP/Azure interview notes, same Q&A format, tagged
   `[CLOUD]`, organized into 5 umbrellas ending in CI/CD & Deployment (the
   umbrella most grounded in this project's real Helm/deployment artifacts).
4. **`kubectl.md`** — kubectl & Linux operational notes, including a
   project-specific section (§6, "Project Notes (dit-cr2)") and the hard
   safety boundary in §7 (see below).

When the user pastes an interview question or asks to study a concept: don't
just answer in chat and stop. Find or build the real grounded example in this
codebase (or a sibling lab — see below), verify it compiles/runs, and add it
to the correct file/section, extending the umbrella/Part structure rather
than bolting on an unstructured aside. Give the full explanation in chat too,
with exact file:line references — don't make the user go read the file to
get the answer.

## Hard boundary: kubectl/cluster access is read-only, always

`kubectl.md` §7 documents this project's live guardrails and they apply to
every AI agent in this workspace, not just the human: for the `dit-cr2`
sandbox namespace, only `get`, `describe`, `logs`, and `rollout history` are
allowed without asking. **Never run `delete`, `exec`, `apply`, `scale`,
`edit`, or `rollout restart`** without explicit sign-off from the user, every
single time — not just the first time. `exec` is conditionally allowed only
for the narrow, documented `javap`-inspection procedure in §9 (extract one
class to `/tmp`, inspect, delete immediately), and still requires sign-off
first. Never touch the PEM key permissions setup in §7.

## Cross-project grounding: sibling study workspaces

This workspace lives under `D:\Le\Mega\Dev Projects`, alongside several other
personal study repos: `Coding Practice`, `SQL`, `Java-Lab`, `Springboot Lab`,
`My-Angular-Lab`, `My-ReactJS-Lab`, `Angular-NGRX-Lab`, `NodeJs`, `ExpressJS`,
`JavaScipt Lab`, `hackathon-java-assignment`, `Git-Lab`, `Happy Renting`,
`JobSheet_Application`, `Notes`, `Profile`, `Learning Material`. These notes
already cross-reference several of them by name (e.g. "Springboot Lab" for a
simpler timeout example, "NodeJs" for an honest "no real backend example"
callout) — that pattern is expected and encouraged: when a concept is better
illustrated by real code sitting in a sibling repo, name that repo and cite
the real file, the same way the HDFC references work.

**Treat every sibling workspace as read-only from here.** Never create, edit,
or delete files outside this workspace's own root
(`D:\Le\Mega\Dev Projects\Springboot Gradle Lab`) unless the user is
explicitly directing work inside that other workspace (in which case that
workspace's own `AGENTS.md`/`CLAUDE.md`/`.clinerules`, if present, govern —
several already have one). Several sibling repos already follow this same
multi-file-AI-agent-rules convention; if none exists yet in a sibling repo
the user wants to work in, it's reasonable to set one up there too, following
this same trio pattern, tailored to that repo's own content.

## Verifying changes

After a code change under `src/`, actually build/run it — `./gradlew build`
or `./gradlew test` (`gradlew.bat` on a native Windows shell) — before
declaring it done. Don't claim something works without having verified it.

## Portable workspace continuity

This repository is the durable handoff point across machines and coding agents;
MEGA may sync a working copy, but Git/GitHub records the history. At the start of
work, read this file and the relevant one of the four documentation files before
acting. Keep reusable project context, verified commands, and learning in those
tracked files so another agent can resume without relying on prior chat history.

Never commit machine-specific credentials, certificates, heap dumps, IDE state,
or absolute local setup as the only instruction. Database and JWT secrets belong
in environment variables; `Springboot Gradle Lab.md` Chapter 14.1 contains the
portable CMD/PowerShell setup. If local paths differ on another PC, preserve the
logical workflow and update only machine-local variables.

## End every response that touches files with a highlighted list

**Files changed:**
- `path/to/file` — one-clause note (new / edited / deleted)

Keep it terse — this is a changelog, not a re-explanation.
