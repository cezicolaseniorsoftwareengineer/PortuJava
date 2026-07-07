/**
 * Default environment: used by `ng serve` and the plain `ng build` (embedded-monolith setup, where
 * Angular is served by the same Spring Boot origin it calls). Relative paths are correct here - never
 * point this at an absolute URL.
 */
export const environment = {
  production: false,
  apiBaseUrl: ''
};
