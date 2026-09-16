// Backend base URL. Local dev talks to the Spring Boot app on 8080;
// anything else (GitHub Pages) talks to the Render service.
// Update RENDER_URL to match the URL Render assigns to the rpl-backend service.
const RENDER_URL = "https://rpl-backend.onrender.com";
const API_BASE = ["localhost", "127.0.0.1"].includes(location.hostname)
  ? "http://localhost:8080"
  : RENDER_URL;
