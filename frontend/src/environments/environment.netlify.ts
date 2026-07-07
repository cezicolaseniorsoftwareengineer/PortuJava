/**
 * Used only by the `netlify` build configuration (see angular.json), which swaps this in for
 * environment.ts via fileReplacements. The Angular app is served from a different origin than the
 * API here, so every request needs the absolute backend URL.
 */
export const environment = {
  production: true,
  apiBaseUrl: 'https://portujava-production.up.railway.app'
};
