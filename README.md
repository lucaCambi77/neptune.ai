## Task :

Your task is to design and implement a high-performance RESTful service capable
of handling the rigorous demands of high-frequency trading systems. This service
will act as a component in the company ABC trading infrastructure, managing and
analysing financial data in near real-time.

## Requirements :

* Java17
* Gradle

## Build

* To build the application

```bash
./gradlew clean build
```

## Run

* To run the application

```bash
java -jar build/libs/task.jar
```

## Endpoints

### Add Point

Add a data point for a specific symbol

**URL**: `/add/`

**Method**: `POST`

**Body**:

```json
{
  "symbol": "ABC",
  "value": 10.0
}
```

**Success Response**:

**Code**: `201 CREATED`

```json
{
  "sequence": 1,
  "symbol": "ABC",
  "value": 155.55555555555566
}
```

### Add Points

Add data points in a batch for a specific symbol

**URL**: `/add_batch/`

**Method**: `POST`

**Body**:

```json
{
  "symbol": "ABC",
  "values": [
    10.0,
    20.0
  ]
}
```

**Success Response**:

**Code**: `201 CREATED`

```json
[
  {
    "sequence": 1,
    "symbol": "ABC",
    "value": 10.0
  },
  {
    "sequence": 2,
    "symbol": "ABC",
    "value": 20.0
  }
]
```

### Get Stats

Get statistical analyses of recent trading data

**URL**: `/stats/`

**Method**: `GET`

#### URL Parameters

- symbol : The financial instrument's identifier.
- k : last 1e{k} data points to analyze. It must be an integer from 1 to 7

**Success Response**:

**Code**: `200 OK`

```json
{
  "min": 10.0,
  "max": 10.0,
  "last": 10.0,
  "avg": 10.0,
  "var": 0.0
}
```

**Error Response**:

**Code**: `400 BAD REQUEST`

- Statistics are not available for a specific symbol

```json
{
  "message": "No stats available for symbol A",
  "timestamp": "2024-06-28T00:09:22.337646391"
}
```

- Requested k is greater than the points list size for a specific symbol

```json
{
  "message": "Only 10 points are available for symbol ABC",
  "timestamp": "2024-06-28T00:10:58.198783186"
}
```

- Requested k is greater than the max value allowed

```json
{
  "message": "k can't be greater than 10000000",
  "timestamp": "2024-06-28T00:21:27.281892622"
}
```
