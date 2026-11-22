/*
 * Minimal STOMP 1.2 client for the SmartShop real-time channel.
 *
 * Only the frames the app needs are implemented (CONNECT, SUBSCRIBE, MESSAGE,
 * DISCONNECT), which keeps the page free of third-party dependencies. It talks
 * to the SockJS endpoint at /ws, which accepts raw WebSocket upgrades, and
 * reconnects with a backoff when the socket drops.
 */
(function () {
    'use strict';

    var ENDPOINT = (window.location.protocol === 'https:' ? 'wss://' : 'ws://')
        + window.location.host + '/ws';
    var RECONNECT_MIN_MS = 1000;
    var RECONNECT_MAX_MS = 30000;

    var socket = null;
    var connected = false;
    var reconnectDelay = RECONNECT_MIN_MS;
    var reconnectTimer = null;
    var sequence = 0;
    var subscriptions = [];
    var subscribers = {};

    function frame(command, headers, body) {
        var lines = [command];
        Object.keys(headers || {}).forEach(function (name) {
            lines.push(name + ':' + headers[name]);
        });
        return lines.join('\n') + '\n\n' + (body || '') + '\0';
    }

    function parse(raw) {
        if (raw.charCodeAt(0) === 0x0a) {
            raw = raw.substring(1);
        }
        var terminator = raw.indexOf('\0');
        if (terminator >= 0) {
            raw = raw.substring(0, terminator);
        }
        var split = raw.indexOf('\n\n');
        if (split < 0) {
            return null;
        }
        var head = raw.substring(0, split);
        var body = raw.substring(split + 2);
        var lines = head.split('\n');
        var command = lines.shift();
        var headers = {};
        lines.forEach(function (line) {
            var index = line.indexOf(':');
            if (index > 0) {
                headers[line.substring(0, index)] = line.substring(index + 1);
            }
        });
        return { command: command, headers: headers, body: body };
    }

    function dispatch(payload) {
        if (!payload || typeof payload !== 'object') {
            return;
        }
        var name = 'smartshop:' + String(payload.type || 'message').toLowerCase();
        document.dispatchEvent(new CustomEvent(name, { detail: payload }));

        if (payload.type === 'STOCK_CHANGED') {
            updateStock(payload);
        } else if (payload.type === 'ORDER_CHANGED') {
            updateOrders(payload);
        } else if (payload.type === 'CART_CHANGED') {
            updateCartBadge(payload.itemCount);
        }
    }

    function updateStock(payload) {
        document.querySelectorAll('[data-stock-smartid]').forEach(function (node) {
            if (node.getAttribute('data-stock-smartid') !== payload.smartid) {
                return;
            }
            var note = node.querySelector('[data-stock-note]');
            if (note) {
                note.textContent = '(' + payload.quantity + ' in stock)';
                return;
            }
            node.textContent = payload.quantity;
            node.classList.toggle('text-danger', payload.quantity <= 0);
        });
        document.querySelectorAll('[data-stock-row]').forEach(function (row) {
            if (row.getAttribute('data-stock-row') !== payload.smartid) {
                return;
            }
            row.classList.add('table-warning');
            window.setTimeout(function () {
                row.classList.remove('table-warning');
            }, 2000);
        });
        var list = document.querySelector('[data-live="inventory"]');
        if (list) {
            scheduleReload(list.getAttribute('data-live-refresh-url') || '/admin/inventory');
        }
    }

    function updateOrders(payload) {
        var badge = document.querySelector('[data-live="orders-status"]');
        if (badge && (!payload.orderid
            || badge.getAttribute('data-order-id') === String(payload.orderid))) {
            badge.textContent = payload.status;
        }
        if (document.querySelector('[data-live="orders"]')) {
            scheduleReload(window.location.pathname + window.location.search);
        }
    }

    function updateCartBadge(count) {
        document.querySelectorAll('[data-cart-count]').forEach(function (node) {
            node.textContent = count;
            node.classList.toggle('d-none', Number(count) <= 0);
        });
    }

    var reloadTimer = null;
    function scheduleReload(url) {
        window.clearTimeout(reloadTimer);
        reloadTimer = window.setTimeout(function () {
            window.location.assign(url);
        }, 1500);
    }

    function subscribe(destination) {
        if (connected) {
            subscribeNow(destination);
        } else if (subscriptions.indexOf(destination) < 0) {
            subscriptions.push(destination);
        }
    }

    function send(text) {
        if (socket && socket.readyState === window.WebSocket.OPEN) {
            socket.send(text);
        }
    }

    function scheduleReconnect() {
        if (reconnectTimer) {
            return;
        }
        reconnectTimer = window.setTimeout(function () {
            reconnectTimer = null;
            connect();
        }, reconnectDelay);
        reconnectDelay = Math.min(reconnectDelay * 2, RECONNECT_MAX_MS);
    }

    function handleMessage(event) {
        var parsed = parse(event.data);
        if (!parsed) {
            return;
        }
        if (parsed.command === 'MESSAGE') {
            try {
                dispatch(JSON.parse(parsed.body));
            } catch (ignore) {
            }
        } else if (parsed.command === 'CONNECTED') {
            onConnected();
        } else if (parsed.command === 'ERROR') {
            connected = false;
        }
    }

    function connect() {
        if (!window.WebSocket) {
            return;
        }
        socket = new window.WebSocket(ENDPOINT);
        socket.onopen = function () {
            send(frame('CONNECT', { 'accept-version': '1.2,1.1,1.0', host: window.location.host }));
        };
        socket.onmessage = handleMessage;
        socket.onclose = function () {
            connected = false;
            scheduleReconnect();
        };
        socket.onerror = function () {
            connected = false;
        };
        document.dispatchEvent(new CustomEvent('smartshop:connection', {
            detail: { connected: false }
        }));
    }

    function onConnected() {
        connected = true;
        reconnectDelay = RECONNECT_MIN_MS;
        document.dispatchEvent(new CustomEvent('smartshop:connection', {
            detail: { connected: true }
        }));
        subscriptions.forEach(subscribeNow);
        subscriptions = [];
    }

    function subscribeNow(destination) {
        sequence += 1;
        var id = 'sub-' + sequence;
        subscribers[id] = destination;
        send(frame('SUBSCRIBE', { id: id, destination: destination }));
    }

    window.smartShopRealtime = {
        connect: connect,
        subscribe: subscribe,
        isConnected: function () {
            return connected;
        }
    };

    subscribe('/topic/inventory');
    subscribe('/topic/orders');
    subscribe('/user/queue/cart');

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', connect);
    } else {
        connect();
    }
})();