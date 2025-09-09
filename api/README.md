# 👥 Employee Management API

This is a Spring Boot REST API developed as part of the **Reliaquest Coding Challenge**. It provides employee management functionality, including search, salary-based queries, and rate-limited access. The API integrates with a mock employee server using `WebClient`.

---

## 🚀 Features

- ✅ Get all employees
- 🔍 Search employees by name (case-insensitive)
- 🔎 Get an employee by ID
- 🪙 Retrieve the highest employee salary
- 🏅 List top 10 highest-earning employee names
- ➕ Create a new employee (with validation)
- ❌ Delete an employee by ID
- 🚦 Rate limiting using Guava (2 requests/sec)

---
### How to Start Spring Boot application

Start **Server** Spring Boot application.

Build Application
`./gradlew :api:rqChallenge`

Run application
`./gradlew :api:bootRun`

Run unit tests
`./gradlew test`

## 🧑‍💼 API Endpoints & Examples

### 1. Get All Employees

```http
GET /employee
```

# Employee API

This API provides access to a list of employee records in JSON format. It can be used to fetch basic information such as employee name, age, salary, title, and email.

## API Endpoint

### `GET /api/employees`

- **Base URL:** `http://localhost:8111`
- **Full Endpoint:** `http://localhost:8111/api/employees`
- **Method:** GET
- **Description:** Retrieves a list of all employees with detailed information.

---

## Response Format

- **Status Code:** `200 OK`
- **Content-Type:** `application/json`
- **Response Body:** An array of employee objects.

### Example Response

```json
[
   {
        "id": "63d11f1e-029e-4b76-a736-8c5a28543f3c",
        "employee_name": "Wes Lemke",
        "employee_salary": 481589,
        "employee_age": 47,
        "employee_title": "Farming Director",
        "employee_email": "fixsan@company.com"
    },
    {
        "id": "8accf67e-fdc5-4759-8631-7099192f8c71",
        "employee_name": "Mrs. Kai Lesch",
        "employee_salary": 245246,
        "employee_age": 18,
        "employee_title": "Global Farming Facilitator",
        "employee_email": "solarbreeze@company.com"
    }
  // ... more records
]
```

# 2. Employee Detail API

This API retrieves detailed information about a specific employee using their unique ID.

## Endpoint

### `GET /api/employees/{id}`

- **Base URL:** `http://localhost:8111`
- **Full Endpoint:** `http://localhost:8111/api/employees/63d11f1e-029e-4b76-a736-8c5a28543f3c`
- **Method:** GET
- **Description:** Fetches details of a single employee by their UUID.

---

## Path Parameter

| Parameter | Type   | Description              |
|-----------|--------|--------------------------|
| `id`      | string | UUID of the employee     |

---

## Example Request

```http
GET http://localhost:8111/api/employees/63d11f1e-029e-4b76-a736-8c5a28543f3c
```

## Response Format

- **Status Code:** `200 OK`
- **Content-Type:** `application/json`
- **Response Body:** Single Employee data by Id.

### Example Response

```json
{
    "id": "63d11f1e-029e-4b76-a736-8c5a28543f3c",
    "employee_name": "Wes Lemke",
    "employee_salary": 481589,
    "employee_age": 47,
    "employee_title": "Farming Director",
    "employee_email": "fixsan@company.com"
}
```
# 3. Delete Employee API

This API allows you to delete a specific employee by their unique ID.

---

## Endpoint

### `DELETE /api/employees/{id}`

- **Base URL:** `http://localhost:8111`
- **Full Endpoint Example:** `http://localhost:8111/api/employees/63d11f1e-029e-4b76-a736-8c5a28543f3c`
- **Method:** DELETE
- **Description:** Permanently deletes an employee record using their UUID.

---

## Path Parameter

| Parameter | Type   | Description                    |
|-----------|--------|--------------------------------|
| `id`      | String | UUID of the employee to delete |

---

## Example Request

```http
curl -X DELETE http://localhost:8111/api/employees/63d11f1e-029e-4b76-a736-8c5a28543f3c
```

### Example Response

```json
"Employee 'Wes Lemke' deleted successfully"
```

# 4. Top 10 Highest Earning Employees API

This API returns the names of the top 10 highest-paid employees.

---

## Endpoint

### `GET /api/employees/topTenHighestEarningEmployeeNames`

- **Base URL:** `http://localhost:8111`
- **Full Endpoint:** `http://localhost:8111/api/employees/topTenHighestEarningEmployeeNames`
- **Method:** GET
- **Description:** Retrieves a list of names for the top 10 employees ranked by salary.

---

## Example Request

```http
curl -X GET http://localhost:8111/api/employees/topTenHighestEarningEmployeeNames
```

### Example Response

```json
[
  "Moses Lebsack",
  "Coy Marvin DDS",
  "Mckinley Baumbach",
  "Kraig Roberts",
  "Edison Lynch",
  "Nathanial Emard",
  "Mr. Zane Reichert",
  "Ms. Tonia Spencer",
  "Nubia Pacocha IV",
  "Sommer Russel"
]
```

# Get Employee with Highest Salary API

This API retrieves the employee who has the highest salary from the system.

---

## Endpoint

### `GET /api/employees/highestSalary`

- **Base URL:** `http://localhost:8111`
- **Full Endpoint:** `http://localhost:8111/api/employees/highestSalary`
- **Method:** GET
- **Description:** Returns a single employee object representing the highest-paid employee.

---

## Example Request

```http
curl -X GET http://localhost:8111/api/employees/highestSalary \
  -H "Accept: application/json"
```

### Example Response

```json
495431
```

# 5. Search Employees API

This API allows you to search for employees by a **partial keyword match** in their name.

---

## Endpoint

### `GET /api/employees/search/{keyword}`

- **Base URL:** `http://localhost:8111`
- **Full Example Endpoint:**  
  `http://localhost:8111/api/employees/search/wi`
- **Method:** `GET`
- **Description:** Returns a list of employees whose names contain the specified search keyword (case-insensitive).

---

## Path Parameter

| Parameter | Type   | Description                              |
|-----------|--------|------------------------------------------|
| `keyword` | String | Keyword to search in employee names      |

---

## Example Request

```http
curl -X GET http://localhost:8111/api/employees/search/wi \
  -H "Accept: application/json"
```

# 6. Create Employee API

This API creates a new employee record in the system.

---

## Endpoint

### `POST /api/employees`

- **Base URL:** `http://localhost:8111`
- **Full URL:** `http://localhost:8111/api/employees`
- **Method:** `POST`
- **Description:** Accepts employee details in JSON format and adds the employee to the database.

---

## Request Headers

| Header           | Value               |
|------------------|---------------------|
| `Content-Type`   | `application/json`  |
| `Accept`         | `application/json`  |

---

## Request Body (JSON)

```json
{
  "name": "Adam Smith",
  "salary": 658990,
  "age": 75,
  "title": "DevOps Engineer",
  "email": "sonsing@company.com"
}
```

## Example Request

```http
curl -X POST http://localhost:8111/api/employees \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Adam Smith",
    "salary": 658990,
    "age": 75,
    "title": "DevOps Engineer"
  }'
```

## Example Response

```json
{
  "id": "e61e4fbd-da91-4f9f-b168-5f1bc2cabaa4",
  "employee_name": "Adam Smith",
  "employee_salary": 658990,
  "employee_age": 75,
  "employee_title": "DevOps Engineer",
  "employee_email": "bamity@company.com"
}
```

## 👤 Author

Developed by **Kalyani Vetal**  
[GitHub Profile](https://github.com/kalyanivetal)

This project was submitted as part of a technical assessment.
