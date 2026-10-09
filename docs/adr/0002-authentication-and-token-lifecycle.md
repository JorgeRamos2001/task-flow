# ADR 0002: Authentication and token lifecycle

- Status: Accepted with implementation details to validate
- Date: 2026-10-08

## Decision

- Local passwords are BCrypt hashes. Google OAuth2 authenticates through Spring Security and then establishes a TaskFlow session.
- Google login starts at `/oauth2/authorization/google` and returns through `/login/oauth2/code/google`. The callback sets the refresh cookie and redirects only to an allow-listed frontend route; Angular obtains its access token through `/api/v1/auth/refresh`.
- Protected REST APIs use a 15-minute signed access JWT. The Angular app holds it in memory and sends it as a Bearer token.
- A seven-day opaque refresh token is delivered in a `Secure`, `HttpOnly`, `SameSite` cookie. Persist only its hash and session metadata; rotate on refresh.
- Logout revokes the refresh session and adds the presented access token identifier (`jti`) to a revocation table until its expiry.
- Password-reset tokens are random, single-use, short-lived, and stored hashed. A successful reset revokes all refresh sessions for that account.
- Password-reset responses do not disclose whether an account exists. Apply rate limits to login and reset requests.

## Consequences and open details

- Login, registration, OAuth callbacks, refresh, password recovery, health checks, and public invitation/token routes require carefully defined public/security rules; other REST routes require a valid access token.
- Cookie-based refresh/logout endpoints need explicit CSRF/origin protections.
- Google-only accounts have no local password unless a separate authenticated set-password flow is added.
- Access tokens remain valid for up to their 15-minute lifetime after a password reset unless immediate account-wide access-token invalidation is added.
