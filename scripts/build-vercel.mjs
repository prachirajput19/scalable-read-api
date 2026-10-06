import { cp, mkdir, writeFile } from 'node:fs/promises';
import { dirname, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';

const projectRoot = resolve(dirname(fileURLToPath(import.meta.url)), '..');
const staticRoot = resolve(projectRoot, 'src/main/resources/static');
const outputRoot = resolve(projectRoot, 'dist');
const apiBaseUrl = (process.env.API_BASE_URL ?? '').trim().replace(/\/+$/, '');

if (process.env.VERCEL && !apiBaseUrl) {
  throw new Error('Set the API_BASE_URL Vercel environment variable to the deployed Render API URL.');
}

if (apiBaseUrl) {
  const parsedApiBaseUrl = new URL(apiBaseUrl);
  if (parsedApiBaseUrl.protocol !== 'https:' && parsedApiBaseUrl.hostname !== 'localhost') {
    throw new Error('API_BASE_URL must use HTTPS outside localhost.');
  }
}

await mkdir(outputRoot, { recursive: true });
await cp(resolve(staticRoot, 'index.html'), resolve(outputRoot, 'index.html'));
await writeFile(resolve(outputRoot, 'api-config.js'), `window.API_BASE_URL = ${JSON.stringify(apiBaseUrl)};\n`);
