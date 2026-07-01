# CarFinder: CarDekho Take-Home Assignment

A car research tool that takes a buyer from "I don't know what to buy" to a short, ranked list of real cars that fit their budget and needs, either by filtering a dataset directly, or by describing what they want in plain English to a chat assistant.

**Live app:** https://cardekho-assignment-red.vercel.app
**API:** https://cardekho-assignment-production-1dbe.up.railway.app
**Repo:** https://github.com/DevangKaushik/Cardekho-Assignment
**Screen recording:** [ADD LINK]

## What I built and why

Two ways into the same underlying dataset, because "confused buyer" splits into two real personas:

- **Filter search**: make, body type, fuel type, transmission, seats, and price range, backed by `GET /api/cars` and `GET /api/cars/filters`. For someone who already has rough criteria ("automatic SUV under 15 lakh") and wants to browse and compare.
- **Chat assistant**: a free-text box ("7-seater diesel SUV under 15 lakhs") that gets parsed into the same filter parameters, matched against the dataset, and turned into a natural-language recommendation by Gemini. For someone who doesn't know the right filter labels but can describe their situation.

Both paths hit the same filtering logic (`CarFilterService`) and the same dataset, so the two entry points never disagree with each other. The chat assistant is explicitly grounded: Gemini is only ever shown the top 3 dataset matches for a query and instructed to recommend from that list, compare them on price vs. running cost, and say so honestly if nothing fits well; it can't invent a car or a spec. If Gemini isn't configured (no API key) or the call fails, the app falls back to a plain "found N cars matching X, Y, Z" reply instead of breaking the experience.

The dataset (`cars_ds_final.csv`, ~1,200+ variant rows) is cleaned once by `scripts/build_dataset.py` into a typed `cars.json`: parsing power/torque out of free-text strings, normalizing price into an integer (paise-free) plus a display string, and coercing "Yes/No" columns into booleans for safety features (ABS, ESP, airbags, ISOFIX, etc.).

### What I deliberately cut

- **No auth / user accounts.** No login, no saved shortlists across sessions. The brief is about narrowing down a purchase decision in one sitting, not building a CRM, and auth would have eaten a big chunk of the 2-3 hour budget for zero product value in that scope.
- **No relational database.** The dataset is read-only and fits comfortably in memory, so it's loaded once at startup from a JSON file (`CarDataService`) instead of standing up Postgres/JPA, writing migrations, and seeding it. That setup cost wasn't worth it for data that never changes at runtime; the tradeoff is no persistence of user actions (shortlists, chat history), which is the main thing a real DB would buy back.
- **No car comparison view, no reviews.** The source CSV has spec/safety/price columns but no review text, so nothing there to surface. A side-by-side compare screen was a natural next feature but was lower priority than getting both search paths working end-to-end.

## Tech stack and why

- **Backend: Spring Boot 4.1 / Java 21.** My primary backend stack, so I could move fast without fighting the framework. Plain `@RestController`s over a service layer, `record`s for the `Car` model and DTOs (immutable, no boilerplate getters), Jackson for JSON binding.
- **Frontend: React 19 + TypeScript + Vite.** My primary frontend stack. Vite for instant dev-server startup and a fast build; TypeScript to keep the `Car`/`FilterOptions`/`ChatResponse` shapes honest between the two components (`FilterBar`, `CarList`, `ChatWidget`) that both consume the API.
- **Gemini (`gemini-2.5-flash`) for the chat recommendation.** Cheap and fast enough for a short grounded summary; called server-side only via `GEMINI_API_KEY` so the key never reaches the browser. The keyword/regex parsing that extracts budget, body type, seats, etc. from the user's message is plain Java (`ChatSuggestionService`), not an LLM call: no need to pay for an API round-trip just to detect "under 15 lakh."
- **JSON file over a database**, as above: the dataset is static, so a DB and ORM would have added setup time without adding capability for this scope.
- **Vercel (frontend) + Railway (backend)** for deployment: both are a `git push` away from a live URL, which matched the "runnable in under 2 minutes" constraint better than configuring a VM.

## What I delegated to AI vs. did manually

I used Claude Code for most of the implementation and drove it in small, reviewable steps rather than one giant "build the whole app" prompt.

**Where it helped most:** boilerplate, genuinely good at it. The Spring Boot project structure, the `Car` record with 40+ fields, the CRUD-style controllers and DTOs, the CSV-cleaning script (regex to pull `"maxpower @ rpm"` apart into `powerBhp`/`powerRpm`, normalizing "Yes"/"No" columns into booleans), the dataset-grounding logic in `ChatSuggestionService`/`GeminiService`, and the CSS for all three components, all mechanical, well-specified work it produced correctly on the first or second pass.

**Where it needed the most steering:** configuration and deployment, not the actual application logic. It didn't fail technically anywhere in the business/product code; the gaps were in things like CORS origins for the deployed frontend URL, environment-variable wiring for `GEMINI_API_KEY` across local vs. Railway, and getting the Vercel build/output settings right. Those needed me to spell out the exact use case and environment (which host, which port, which origin is calling which) rather than letting it guess. That's not something the code itself can infer, so it's on me to state it clearly.

## What I'd add with another 4 hours

- **Session memory for the chat assistant**, likely via Redis, so it's a conversation instead of independent one-shot queries: remembering "I said 15 lakh budget and a family of 5" across turns instead of re-parsing every message from scratch. Right now each message to `/api/chat` is stateless.
- **Turn it from a lookup tool into an actual salesman.** Concretely: have it ask a clarifying follow-up when a query is too broad or too narrow instead of just returning "0 cars matched" or a huge list ("Do you care more about running cost or upfront price?"); track what the user has already rejected in-session so it doesn't re-suggest it; and let it proactively push back with a trade-off ("that SUV is ₹2L cheaper, but sedan X gets 6 kmpl better mileage, and over 5 years that's roughly ₹X in fuel savings") instead of only reacting to what's asked.
- **A comparison view**: pick 2-3 cars from search or chat and see specs/safety/price side by side, which the dataset already supports but the UI doesn't expose yet.
- **Persistence for shortlists**: once there's any reason to store user state, a real database (Postgres) replaces the in-memory JSON load, and shortlists/chat history survive a refresh.
- **Basic tests** around `CarFilterService` and the regex-based `ChatSuggestionService` parsing, since those are the two places silent bugs would be easiest to miss.

## Running it locally

**Backend** (Java 21, Maven):
```bash
cd backend
GEMINI_API_KEY=your_key_here ./mvnw spring-boot:run
```
Runs on `http://localhost:8080`. `GEMINI_API_KEY` is optional: without it, the chat assistant still works but falls back to a plain keyword-match reply instead of an AI-generated recommendation.

**Frontend** (Node):
```bash
cd frontend
npm install
npm run dev
```
Runs on `http://localhost:5173` and talks to `http://localhost:8080` by default (override with `VITE_API_URL`).
