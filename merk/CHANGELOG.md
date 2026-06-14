# Changelog

All notable changes to Merk are documented here.

## [1.0.4] - 2026-06-14

### Fixed
- Login Flow v2 poll URL rewriting (Tailscale/subdomain setups): the poll endpoint returned by the server can point to an internal hostname/IP that doesn't match the user-entered address. The app now rewrites the poll URL's scheme/host/port to match what the user typed, preventing "failed to connect" errors and login hangs (confirmed with Tailscale + custom subdomain on IodéOS 6/Android 15).

---

## [1.0.3] - 2026-06-12

### Added
- GPLv3 LICENSE file

---

## [1.0.2] - 2026-06-12

### Added
- In-app text size setting (Small / Default / Large / Extra Large) in Settings > Display, scales the entire typography system

---

## [1.0.1] - 2026-06-09

### Added
- Nextcloud server notice on the login screen with a link to find a hosting partner

### Fixed
- Deprecated icon warnings (ArrowBack, Logout) replaced with AutoMirrored equivalents

---

## [1.0.0] - 2026-06-09

### Initial release

- Bookmark list with search, tag filtering, and favicon initials
- In-app browser with progress bar, open-in-browser and edit shortcuts
- Add, edit and delete bookmarks with title, description, tags and folder
- Create new folders directly from the save screen
- Share any URL from Chrome or any app directly to Merk
- Nextcloud Login Flow v2 authentication — no passwords stored
- Offline-first sync with Nextcloud Bookmarks API
- Real-time sync after every save or delete
- Tasks.org integration — turn any bookmark into a reminder with one tap
- Biometric app lock (fingerprint / PIN)
- Light and dark mode with one-tap toggle
- Save confirmation snackbar after adding or editing a bookmark
- Support section with donation link, website and GitHub links
