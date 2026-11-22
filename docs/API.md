# API reference

Base URL: `http://localhost:8080`

All requests and responses use JSON unless noted. Errors share one shape:

```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Smartphone not found with id: 'SP-404'",
  "path": "/api/smartphones/SP-404",
  "timestamp": "2026-01-01T10:15:30",
  "validationErrors": null
}
```

Validation failures add a `validationErrors` map of field name to message.

## Authentication

`POST /api/auth/login` authenticates and creates an HTTP session. Reuse the
returned `JSESSIONID` cookie for protected endpoints.

```bash
curl -c cookies.txt -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' \
  http://localhost:8080/api/auth/login
```

```json
{
  "empid": 1,
  "empname": "Admin",
  "empuname": "admin",
  "role": "ADMIN",
  "success": true,
  "message": "Signed in"
}
```

Customers authenticate with their email address in the `username` field.

`POST /api/auth/register` accepts `name`, `email`, `phone`, `address`,
`password` and `confirmPassword`.

`GET /api/auth/me` returns the current principal, or `success: false` when the
session is anonymous.

## Products

### List products

`GET /api/smartphones?keyword=pixel&minPrice=100&maxPrice=1200&minRam=8&minStorage=128&page=0&size=20`

```json
{
  "content": [
    {
      "smartid": "SP-1A2B3C4D",
      "smartname": "Pixel 9",
      "smartprice": 799.00,
      "smartqty": 12,
      "smartram": 8,
      "smartstorage": 128,
      "smartimg": null
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1,
  "first": true,
  "last": true
}
```

### Get a product

`GET /api/smartphones/{smartid}`

### Create, update, delete (admin)

```bash
curl -b cookies.txt -H "Content-Type: application/json" \
  -d '{"smartname":"Pixel 10","smartprice":899.00,"smartqty":20,"smartram":12,"smartstorage":256}' \
  http://localhost:8080/api/smartphones
```

`PUT /api/smartphones/{smartid}` uses the same body. `DELETE` removes the
product and its dependent records.

Images can be supplied as a Base64 string in `imageBase64`; the browser form
uploads a file instead.

### Restock (admin or employee)

`POST /api/smartphones/{smartid}/restock?quantity=25`

## Cart

Cart endpoints require an authenticated session. The cart belongs to the
signed-in username.

| Method | Path                     | Description                       |
| ------ | ------------------------ | --------------------------------- |
| GET    | `/api/cart`              | Current cart and totals           |
| GET    | `/api/cart/count`        | Total item quantity               |
| POST   | `/api/cart/items`        | Add `{ "smartid": "...", "quantity": 2 }` |
| PUT    | `/api/cart/items/{id}`   | Set `{ "quantity": 3 }`           |
| DELETE | `/api/cart/items/{id}`   | Remove one line                   |
| DELETE | `/api/cart`              | Empty the cart                    |
| POST   | `/api/cart/checkout`     | Place the order                   |

Checkout body (optional):

```json
{
  "customerId": 7,
  "notes": "Call before delivery"
}
```

`customerId` is required when staff check out on behalf of a customer and is
ignored for customer self-service.

## Orders

| Method | Path                          | Access           |
| ------ | ----------------------------- | ---------------- |
| GET    | `/api/orders`                 | Authenticated    |
| GET    | `/api/orders/{orderid}`       | Owner or staff   |
| POST   | `/api/orders/{orderid}/cancel`| Owner or staff   |
| PUT    | `/api/orders/{orderid}/status`| Admin / employee |

Status update body:

```json
{ "status": "SHIPPED" }
```

Allowed transitions: `PENDING → PROCESSING`, `PENDING → SHIPPED`,
`PROCESSING → SHIPPED`, `SHIPPED → DELIVERED`, and any non-delivered state to
`CANCELLED`. Delivered orders cannot be cancelled.

## Reviews

`GET /api/products/{smartid}/reviews` returns approved reviews.

`POST /api/products/{smartid}/reviews` (customer only):

```json
{ "rating": 5, "comment": "Excellent battery life" }
```

Reviews start as `PENDING`. Admins approve or reject them:

`PUT /api/reviews/{reviewid}/moderate`

```json
{ "status": "APPROVED" }
```

## Wishlist

| Method | Path                        | Description              |
| ------ | --------------------------- | ------------------------ |
| GET    | `/api/wishlist`             | Products saved by the customer |
| POST   | `/api/wishlist/{smartid}`   | Add a product            |
| DELETE | `/api/wishlist/{smartid}`   | Remove a product         |

## Coupons

| Method | Path                                          | Description                     |
| ------ | --------------------------------------------- | ------------------------------- |
| GET    | `/api/coupons/validate?code=WELCOME10&amount=200` | Validate a code against a subtotal |

```json
{
  "code": "WELCOME10",
  "description": "Ten percent off",
  "discountType": "PERCENT",
  "discountValue": 10,
  "discountAmount": 20.00,
  "totalAfterDiscount": 180.00,
  "valid": true
}
```

An unknown, inactive, expired or below-minimum code returns `400 Bad Request`
with the reason in `message`. Codes are managed in the admin UI under
`/admin/promotions` and are applied by posting `couponCode` with
`POST /api/cart/checkout` or `POST /cart/checkout`.

## Real-time channel

`GET /ws` speaks STOMP over WebSocket (SockJS is also available). Subscribe to:

| Destination        | Event                                              |
| ------------------ | -------------------------------------------------- |
| `/topic/inventory` | `{"type":"STOCK_CHANGED","smartid":"SP-1","quantity":4}` |
| `/topic/orders`    | `{"type":"ORDER_CHANGED","orderid":12,"status":"SHIPPED"}` |
| `/user/queue/cart` | `{"type":"CART_CHANGED","itemCount":3}`            |

Events are published after the surrounding transaction commits.

## Administration

| Method | Path                                     | Description                 |
| ------ | ---------------------------------------- | --------------------------- |
| GET    | `/api/admin/dashboard`                   | Aggregated metrics          |
| GET    | `/api/admin/inventory/low-stock?threshold=3` | Products at or below threshold |
| GET    | `/api/admin/inventory/history`           | Stock movement audit trail  |
| POST   | `/api/admin/inventory/{smartid}/restock?quantity=10` | Add stock       |
| POST   | `/api/admin/inventory/{smartid}/adjust`  | Set an absolute quantity    |

Adjust body:

```json
{ "newQuantity": 4, "notes": "Stock count correction" }
```

Dashboard response:

```json
{
  "productCount": 12,
  "customerCount": 34,
  "employeeCount": 4,
  "orderCount": 51,
  "pendingOrders": 3,
  "pendingReviews": 2,
  "lowStockCount": 1,
  "totalRevenue": 18450.00,
  "revenueToday": 899.00,
  "revenueThisMonth": 4210.00,
  "topProducts": [
    { "smartid": "SP-1A2B3C4D", "smartname": "Pixel 9", "quantitySold": 18, "revenue": 14382.00 }
  ],
  "employeePerformance": [
    { "empid": 2, "empname": "Sam", "quantitySold": 20, "revenue": 16000.00 }
  ],
  "topCustomers": [
    { "custid": 7, "custname": "Alice", "orderCount": 4, "revenue": 3196.00 }
  ],
  "lastSevenDays": [
    { "day": "2026-01-01", "orderCount": 3, "revenue": 2397.00 }
  ]
}
```

## OpenAPI

The generated specification is served at `/api-docs` and the Swagger UI at
`/swagger-ui.html`. Both require an authenticated session because they expose
administrative operations.
