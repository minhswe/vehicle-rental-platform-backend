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
"message": ""\
}

## Error Format

{\
"success": false,\
"message": "Vehicle not found"\
}

## HTTP Rules

200 OK
201 Created
400 Bad Request
401 Unauthorized
403 Forbidden
404 Not Found
500 Internal Server Error
