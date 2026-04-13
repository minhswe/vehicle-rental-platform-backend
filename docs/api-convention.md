# API CONVENTION

## Naming

GET /vehicles
GET /vehicles/{id}
POST /vehicles
PUT /vehicles/{id}
DELETE /vehicles/{id}

## Response Format

{\
"success": true,\
"data": {},\
"message": "string"\
"traceId": "string"\
"timestamp": "ISO-8601"\
}

## Error Format

{\
"success": false,\
"message": "string"\
"errorCode": STRING_ENUM\
"traceId": "string"\
"timestamp": "ISO-8601"\
}

## HTTP Rules

200 OK &rarr; success GET\
201 Created &rarr; success POST\
400 Bad Request &rarr; validation/ business error\
401 Unauthorized &rarr; not login\
403 Forbidden &rarr; Permission denied\
404 Not Found &rarr; Resource not found\
500 Internal Server Error &rarr; System error / Internal server error\
