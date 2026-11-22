(function () {
    'use strict';

    var THEME_COOKIE = 'smartshop-theme';
    var THEME_KEY = 'smartshop-theme';

    function preferredTheme() {
        var match = document.cookie.match(/(?:^|;\s*)smartshop-theme=(dark|light)/);
        if (match) {
            return match[1];
        }
        try {
            var stored = window.localStorage.getItem(THEME_KEY);
            if (stored === 'dark' || stored === 'light') {
                return stored;
            }
        } catch (ignore) {
        }
        return document.documentElement.getAttribute('data-theme') || 'light';
    }

    function applyTheme(theme) {
        document.documentElement.setAttribute('data-bs-theme', theme);
        document.documentElement.setAttribute('data-theme', theme);
        document.documentElement.style.colorScheme = theme;
        try {
            window.localStorage.setItem(THEME_KEY, theme);
        } catch (ignore) {
        }
        document.querySelectorAll('[data-theme-toggle-label]').forEach(function (label) {
            label.textContent = theme === 'dark' ? 'Light mode' : 'Dark mode';
        });
        var toggle = document.querySelector('[data-theme-toggle] input[name="theme"]');
        if (toggle) {
            toggle.value = theme === 'dark' ? 'light' : 'dark';
        }
    }

    function initTheme() {
        applyTheme(preferredTheme());
        // Reflect the OS choice while the visitor has not picked a side yet.
        if (window.matchMedia) {
            var query = window.matchMedia('(prefers-color-scheme: dark)');
            var onChange = function (event) {
                var stored = null;
                try {
                    stored = window.localStorage.getItem(THEME_KEY);
                } catch (ignore) {
                }
                var cookie = document.cookie.match(/(?:^|;\s*)smartshop-theme=(dark|light)/);
                if (stored || cookie) {
                    return;
                }
                applyTheme(event.matches ? 'dark' : 'light');
            };
            if (query.addEventListener) {
                query.addEventListener('change', onChange);
            } else if (query.addListener) {
                query.addListener(onChange);
            }
        }
    }

    function initDataTables() {
        if (!window.jQuery || !window.jQuery.fn || !window.jQuery.fn.DataTable) {
            return;
        }
        window.jQuery('table[data-datatable]').each(function () {
            var $table = window.jQuery(this);
            if (!window.jQuery.fn.DataTable.isDataTable(this)) {
                $table.DataTable({
                    pageLength: 10,
                    order: [],
                    columnDefs: [{ targets: 'no-sort', orderable: false }]
                });
            }
        });
    }

    function printSection(id) {
        var section = document.getElementById(id);
        if (!section) {
            return;
        }
        var original = document.body.innerHTML;
        document.body.innerHTML = section.innerHTML;
        window.print();
        document.body.innerHTML = original;
        window.location.reload();
    }

    function initOfflineBanner() {
        var banner = document.querySelector('[data-offline-banner]');
        if (!banner) {
            return;
        }
        var sync = function () {
            banner.hidden = window.navigator.onLine;
        };
        window.addEventListener('online', sync);
        window.addEventListener('offline', sync);
        sync();
    }

    function registerServiceWorker() {
        if (!('serviceWorker' in navigator) || window.location.protocol === 'file:') {
            return;
        }
        window.addEventListener('load', function () {
            navigator.serviceWorker.register('/sw.js', { scope: '/' })
                .catch(function () {
                    // A blocked or unsupported worker must never break the page.
                });
        });
    }

    document.addEventListener('DOMContentLoaded', function () {
        initTheme();
        initDataTables();
        initOfflineBanner();
        registerServiceWorker();
        document.querySelectorAll('[data-print-target]').forEach(function (button) {
            button.addEventListener('click', function () {
                printSection(button.getAttribute('data-print-target'));
            });
        });
        document.querySelectorAll('[data-retry-online]').forEach(function (button) {
            button.addEventListener('click', function () {
                if (window.navigator.onLine) {
                    window.location.reload();
                } else {
                    window.location.href = '/offline';
                }
            });
        });
    });

    window.smartShopPrint = printSection;
    window.smartShopTheme = { apply: applyTheme, preferred: preferredTheme };
})();