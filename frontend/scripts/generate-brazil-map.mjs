import { writeFileSync } from 'node:fs';
import { fileURLToPath } from 'node:url';

const SOURCE_URL =
  'https://servicodados.ibge.gov.br/api/v3/malhas/paises/BR' +
  '?formato=application/vnd.geo+json&qualidade=minima&intrarregiao=UF';
const OUTPUT_URL = new URL('../src/app/treasure-map/brazil-map.data.ts', import.meta.url);
const VIEWBOX_WIDTH = 640;
const VIEWBOX_HEIGHT = 660;
const PADDING = 28;

const stateMetadata = new Map(Object.entries({
  '11': ['RO', 'Rondônia'],
  '12': ['AC', 'Acre'],
  '13': ['AM', 'Amazonas'],
  '14': ['RR', 'Roraima'],
  '15': ['PA', 'Pará'],
  '16': ['AP', 'Amapá'],
  '17': ['TO', 'Tocantins'],
  '21': ['MA', 'Maranhão'],
  '22': ['PI', 'Piauí'],
  '23': ['CE', 'Ceará'],
  '24': ['RN', 'Rio Grande do Norte'],
  '25': ['PB', 'Paraíba'],
  '26': ['PE', 'Pernambuco'],
  '27': ['AL', 'Alagoas'],
  '28': ['SE', 'Sergipe'],
  '29': ['BA', 'Bahia'],
  '31': ['MG', 'Minas Gerais'],
  '32': ['ES', 'Espírito Santo'],
  '33': ['RJ', 'Rio de Janeiro'],
  '35': ['SP', 'São Paulo'],
  '41': ['PR', 'Paraná'],
  '42': ['SC', 'Santa Catarina'],
  '43': ['RS', 'Rio Grande do Sul'],
  '50': ['MS', 'Mato Grosso do Sul'],
  '51': ['MT', 'Mato Grosso'],
  '52': ['GO', 'Goiás'],
  '53': ['DF', 'Distrito Federal']
}));

const response = await fetch(SOURCE_URL);
if (!response.ok) {
  throw new Error(`IBGE map request failed with HTTP ${response.status}`);
}

const geoJson = await response.json();
if (geoJson.type !== 'FeatureCollection' || geoJson.features?.length !== 27) {
  throw new Error('IBGE map must contain exactly 27 federative units');
}
const receivedCodes = new Set(geoJson.features.map((feature) => feature.properties?.codarea));
const missingCodes = [...stateMetadata.keys()].filter((code) => !receivedCodes.has(code));
if (receivedCodes.size !== 27 || missingCodes.length > 0) {
  throw new Error(`IBGE map has duplicated or missing federative units: ${missingCodes.join(', ')}`);
}

const allCoordinates = geoJson.features.flatMap((feature) => flattenCoordinates(feature.geometry.coordinates));
const longitudes = allCoordinates.map(([longitude]) => longitude);
const latitudes = allCoordinates.map(([, latitude]) => latitude);
const minLongitude = Math.min(...longitudes);
const maxLongitude = Math.max(...longitudes);
const minLatitude = Math.min(...latitudes);
const maxLatitude = Math.max(...latitudes);
const geographicWidth = maxLongitude - minLongitude;
const geographicHeight = maxLatitude - minLatitude;
const scale = Math.min(
  (VIEWBOX_WIDTH - PADDING * 2) / geographicWidth,
  (VIEWBOX_HEIGHT - PADDING * 2) / geographicHeight
);
const projectedWidth = geographicWidth * scale;
const projectedHeight = geographicHeight * scale;
const offsetX = (VIEWBOX_WIDTH - projectedWidth) / 2;
const offsetY = (VIEWBOX_HEIGHT - projectedHeight) / 2;

const project = ([longitude, latitude]) => [
  offsetX + (longitude - minLongitude) * scale,
  offsetY + (maxLatitude - latitude) * scale
];

const states = geoJson.features.map((feature) => {
  const ibgeCode = feature.properties?.codarea;
  const metadata = stateMetadata.get(ibgeCode);
  if (!metadata) {
    throw new Error(`Unknown IBGE federative unit code: ${ibgeCode}`);
  }
  if (feature.geometry.type !== 'Polygon' && feature.geometry.type !== 'MultiPolygon') {
    throw new Error(`Unsupported geometry type for ${ibgeCode}: ${feature.geometry.type}`);
  }

  const polygons = feature.geometry.type === 'Polygon'
    ? [feature.geometry.coordinates]
    : feature.geometry.coordinates;
  const projectedPolygons = polygons.map((polygon) => polygon.map((ring) => ring.map(project)));
  const path = projectedPolygons
    .flatMap((polygon) => polygon.map(ringToPath))
    .join(' ');
  const primaryRing = projectedPolygons
    .map((polygon) => polygon[0])
    .sort((left, right) => Math.abs(ringArea(right)) - Math.abs(ringArea(left)))[0];
  const [anchorX, anchorY] = ringCentroid(primaryRing);

  return {
    ibgeCode,
    code: metadata[0],
    name: metadata[1],
    path,
    anchorX: round(anchorX),
    anchorY: round(anchorY)
  };
}).sort((left, right) => Number(left.ibgeCode) - Number(right.ibgeCode));

const output = `// Generated from the official IBGE Geographic Mesh API. Do not edit paths manually.\n` +
  `// Regenerate with: node scripts/generate-brazil-map.mjs\n` +
  `export const BRAZIL_MAP_SOURCE = '${SOURCE_URL}' as const;\n` +
  `export const BRAZIL_MAP_VIEWBOX = '0 0 ${VIEWBOX_WIDTH} ${VIEWBOX_HEIGHT}' as const;\n\n` +
  `export interface BrazilMapState {\n` +
  `  readonly ibgeCode: string;\n` +
  `  readonly code: string;\n` +
  `  readonly name: string;\n` +
  `  readonly path: string;\n` +
  `  readonly anchorX: number;\n` +
  `  readonly anchorY: number;\n` +
  `}\n\n` +
  `export const BRAZIL_MAP_STATES: readonly BrazilMapState[] = ${JSON.stringify(states, null, 2)};\n`;

writeFileSync(fileURLToPath(OUTPUT_URL), output, 'utf8');
console.log(`Generated ${states.length} official state paths at ${fileURLToPath(OUTPUT_URL)}`);

function flattenCoordinates(value) {
  if (typeof value[0] === 'number') {
    return [value];
  }
  return value.flatMap(flattenCoordinates);
}

function ringToPath(ring) {
  return ring.map(([x, y], index) => `${index === 0 ? 'M' : 'L'}${round(x)} ${round(y)}`).join('') + 'Z';
}

function ringArea(ring) {
  let area = 0;
  for (let index = 0; index < ring.length - 1; index += 1) {
    area += ring[index][0] * ring[index + 1][1] - ring[index + 1][0] * ring[index][1];
  }
  return area / 2;
}

function ringCentroid(ring) {
  let crossSum = 0;
  let xSum = 0;
  let ySum = 0;
  for (let index = 0; index < ring.length - 1; index += 1) {
    const [x1, y1] = ring[index];
    const [x2, y2] = ring[index + 1];
    const cross = x1 * y2 - x2 * y1;
    crossSum += cross;
    xSum += (x1 + x2) * cross;
    ySum += (y1 + y2) * cross;
  }
  if (Math.abs(crossSum) < Number.EPSILON) {
    return ring[0];
  }
  return [xSum / (3 * crossSum), ySum / (3 * crossSum)];
}

function round(value) {
  return Number(value.toFixed(1));
}
