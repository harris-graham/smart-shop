/*
 * SmartShop service worker.
 *
 * Strategy
 *  - Precache the application shell on install so the storefront opens offline.
 *  - Static assets: stale-while-revalidate (fast, always fresh enough).
 *  - Navigations: network first, falling back to the cached page and finally to
 *    the offline placeholder.
 *  - Everything else (API, admin POSTs): network only, never cached.
 */
'use strict';

const VERSION = 'smartshop-v1';
const SHELL_CACHE = VERSION + '-shell';
const RUNTIME_CACHE = VERSION + '-runtime';
const OFFLINE_URL = '/offline';

const SHELL_ASSETS = [
    '/offline',
    '/css/bootstrap.min.css',
    '/css/dataTables.bootstrap5.min.css',
    '/css/app.css',
    '/js/jquery-3.5.1.js',
    '/js/bootstrap.bundle.min.js',
    '/js/jquery.dataTables.min.js',
    '/js/dataTables.bootstrap5.min.js',
    '/js/app.js',
    '/js/realtime.js',
    '/assets/icon.svg',
    '/manifest.webmanifest'
];

self.addEventListener('install', (event) => {
    event.waitUntil(
        caches.open(SHELL_CACHE)
            .then((cache) => cache.addAll(SHELL_ASSETS))
            .then(() => self.skipWaiting())
    );
});

self.addEventListener('activate', (event) => {
    event.waitUntil(
        caches.keys()
            .then((keys) => Promise.all(
                keys.filter((key) => key !== SHELL_CACHE && key !== RUNTIME_CACHE)
                    .map((key) => caches.delete(key))
            ))
            .then(() => self.clients.claim())
    );
});

function isStaticAsset(url) {
    return /\/(css|js|assets|images)\//.test(url.pathname)
        || /\.(?:png|jpg|jpeg|svg|webp|ico|woff2?)$/i.test(url.pathname);
}

function isCacheableNavigation(url) {
    return url.origin === self.location.origin
        && !url.pathname.startsWith('/api/')
        && !url.pathname.startsWith('/admin');
}

async function staleWhileRevalidate(request) {
    const cache = await caches.open(RUNTIME_CACHE);
    const cached = await cache.match(request);
    const network = fetch(request)
        .then((response) => {
            if (response && response.ok) {
                cache.put(request, response.clone());
            }
            return response;
        })
        .catch(() => cached);
    return cached || network;
}

async function networkFirst(request) {
    try {
        const response = await fetch(request);
        if (response && response.ok) {
            const cache = await caches.open(RUNTIME_CACHE);
            cache.put(request, response.clone());
        }
        return response;
    } catch (error) {
        const cached = await caches.match(request);
        if (cached) {
            return cached;
        }
        return caches.match(OFFLINE_URL);
    }
}

self.addEventListener('fetch', (event) => {
    const request = event.request;
    if (request.method !== 'GET') {
        return;
    }

    const url = new URL(request.url);
    if (url.origin !== self.location.origin || url.pathname.startsWith('/api/')) {
        return;
    }

    if (request.mode === 'navigate' && isCacheableNavigation(url)) {
        event.respondWith(networkFirst(request));
        return;
    }

    if (isStaticAsset(url)) {
        event.respondWith(staleWhileRevalidate(request));
    }
});

self.addEventListener('message', (event) => {
    if (event.data === 'skip-waiting') {
        self.skipWaiting();
    }
});