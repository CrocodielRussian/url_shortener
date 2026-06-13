# URL Shortener

A full-stack URL shortener built with Angular, Spring Boot, PostgreSQL, Redis, and Nginx.

The application allows users to register, log in, create short links, manage their personal URL history, and open public short links that redirect to the original long URLs.

Production deployment is designed for Kubernetes. The project can run locally with Docker Compose, on Minikube, or in a cloud Kubernetes cluster such as Yandex Managed Kubernetes.

## Tech Stack

* Frontend: Angular 21
* Backend: Spring Boot 3, Spring Security, JWT
* Database: PostgreSQL
* Cache: Redis
* Reverse proxy: Nginx
* Build tools: Gradle, npm
* Container runtime: Docker
* Local orchestration: Docker Compose, Minikube
* Production orchestration: Kubernetes
* Production database option: Yandex Managed PostgreSQL
* Production secrets option: Yandex Lockbox + External Secrets Operator

## Features

* User registration and login
* JWT-protected API requests
* Create short links from long URLs
* View the authenticated user's saved links
* Delete one link
* Delete all links of the authenticated user
* Public redirect from short links to original URLs
* Redis caching for faster short URL resolution
* Nginx reverse proxy for frontend, API, and public redirects
* Kubernetes-ready deployment

## Architecture

```text
User
  ↓
Nginx
  ↓
Angular frontend
  ↓
Spring Boot backend
  ↓
PostgreSQL + Redis
```

Nginx is the public entry point.

It handles:

```text
/                 -> Angular frontend
/api/...          -> Spring Boot backend
/{8-hex-code}     -> Spring Boot backend redirect endpoint
```

The backend stores URL mappings in PostgreSQL and uses Redis as a cache for faster redirect lookups.

## Project Structure

```text
.
├── frontend/              # Angular application
├── nginx/                 # Nginx Dockerfile and reverse proxy config
├── src/main/java/         # Spring Boot backend
├── src/main/resources/    # Spring Boot configuration
├── docker-compose.yml     # Local Docker Compose stack
├── Dockerfile             # Backend Docker build
├── build.gradle           # Backend dependencies and build tasks
└── README.md
```

## Configuration

The backend is configured through environment variables.

Important variables:

| Variable                     | Description                               |
| ---------------------------- | ----------------------------------------- |
| `SPRING_DATASOURCE_URL`      | PostgreSQL JDBC URL                       |
| `SPRING_DATASOURCE_USERNAME` | PostgreSQL username                       |
| `SPRING_DATASOURCE_PASSWORD` | PostgreSQL password                       |
| `SPRING_REDIS_HOST`          | Redis host                                |
| `SPRING_REDIS_PORT`          | Redis port                                |
| `JWT_SECRET`                 | Secret key for signing JWT tokens         |
| `JWT_EXPIRATION`             | JWT expiration time in milliseconds       |
| `APP_BASE_URL`               | Base URL used when generating short links |
| `SERVER_PORT`                | Backend server port                       |

Example `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: ${SPRING_DATASOURCE_URL}
    username: ${SPRING_DATASOURCE_USERNAME}
    password: ${SPRING_DATASOURCE_PASSWORD}

  jpa:
    hibernate:
      ddl-auto: ${SPRING_JPA_HIBERNATE_DDL_AUTO:update}
    show-sql: ${SPRING_JPA_SHOW_SQL:false}

  data:
    redis:
      host: ${SPRING_REDIS_HOST}
      port: ${SPRING_REDIS_PORT}

  cache:
    type: redis
    redis:
      time-to-live: ${SPRING_CACHE_REDIS_TTL:3600000}

server:
  port: ${SERVER_PORT:8080}

app:
  base-url: ${APP_BASE_URL}

jwt:
  secret: ${JWT_SECRET}
  expiration: ${JWT_EXPIRATION}
```

Example local values:

```env
SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/urldb
SPRING_DATASOURCE_USERNAME=user
SPRING_DATASOURCE_PASSWORD=password

SPRING_REDIS_HOST=redis
SPRING_REDIS_PORT=6379

JWT_SECRET=change-me-long-secret-change-me-long-secret
JWT_EXPIRATION=3600000

APP_BASE_URL=http://localhost
SERVER_PORT=8080
```

For production with a real domain:

```env
APP_BASE_URL=http://urlshortener.ru
```

## Quick Start With Docker Compose

From the project root:

```bash
docker compose up --build
```

The application is served by Nginx:

```text
http://localhost
```

Docker Compose starts:

* `nginx` on host port `80`
* `backend` on container port `8080`
* `postgres` on container port `5432`
* `redis` on container port `6379`

The backend is not exposed directly to the host. Nginx proxies requests to:

```text
http://backend:8080
```

## Nginx Configuration

Example Nginx configuration:

```nginx
server {
    listen 80;
    server_name localhost;

    root /usr/share/nginx/html;
    index index.html;

    location /api/ {
        proxy_pass http://backend:8080;

        proxy_http_version 1.1;

        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_set_header Authorization $http_authorization;
    }

    location ~ "^/[0-9a-f]{8}$" {
        proxy_pass http://backend:8080;

        proxy_http_version 1.1;

        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    location / {
        try_files $uri $uri/ /index.html;
    }
}
```

For production domain deployment:

```nginx
server_name urlshortener.ru www.urlshortener.ru;
```

## API Overview

### Authentication

Register a user:

```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "user",
  "password": "password"
}
```

Login:

```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "user",
  "password": "password"
}
```

Both endpoints return a JWT token:

```json
{
  "token": "jwt-token",
  "id": 1,
  "username": "user"
}
```

### URLs

URL management endpoints require a JWT token:

```http
Authorization: Bearer <jwt-token>
```

Create a short URL:

```http
POST /api/url/add
Content-Type: application/json
Authorization: Bearer <jwt-token>

{
  "url": "https://example.com/very/long/link"
}
```

Response:

```json
{
  "id": 1,
  "shortUrl": "http://localhost/a1b2c3d4",
  "longUrl": "https://example.com/very/long/link"
}
```

Get the current user's URLs:

```http
GET /api/url/my
Authorization: Bearer <jwt-token>
```

Delete one URL:

```http
DELETE /api/url/{id}
Authorization: Bearer <jwt-token>
```

Delete all current user's URLs:

```http
DELETE /api/url
Authorization: Bearer <jwt-token>
```

### Redirects

Short links are public and do not require authentication:

```http
GET /{shortUrl}
```

Example:

```text
http://localhost/a1b2c3d4
```

If the short code exists, the backend returns:

```http
302 Found
Location: https://example.com/very/long/link
```

## Local Development

### Backend

The backend requires PostgreSQL and Redis.

You can start PostgreSQL and Redis through Docker Compose, or run them manually.

Start the backend:

```bash
gradle bootRun
```

The backend listens on:

```text
http://localhost:8080
```

### Frontend

Install dependencies:

```bash
cd frontend
npm install
```

Run the Angular development server:

```bash
npm start
```

Angular runs on:

```text
http://localhost:4200
```

When running the frontend separately from Docker, make sure API requests are routed to the Spring Boot backend.

In the Docker setup this is handled by Nginx.

## Build

Build the backend:

```bash
gradle build
```

Build the frontend:

```bash
cd frontend
npm run build
```

Build Docker images locally:

```bash
docker build -t url-shortener-backend:local .
docker build -t url-shortener-nginx:local ./nginx
```

## Deploying on Minikube

Minikube is useful for testing the Kubernetes deployment locally.

### 1. Start Minikube

```bash
minikube start
```

Check the cluster:

```bash
kubectl get nodes
```

### 2. Build Images Inside Minikube

Build the backend image:

```bash
minikube image build -t backend:local .
```

Build the Nginx/frontend image:

```bash
minikube image build -t nginx:local ./nginx
```

In Minikube manifests, use:

```yaml
image: backend:local
imagePullPolicy: Never
```

and:

```yaml
image: nginx:local
imagePullPolicy: Never
```

### 3. Create Backend ConfigMap

Example `k8s/minikube/backend-config.yaml`:

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: backend-config
data:
  SPRING_DATASOURCE_URL: "jdbc:postgresql://postgres:5432/urldb"
  SPRING_DATASOURCE_USERNAME: "user"

  SPRING_REDIS_HOST: "redis"
  SPRING_REDIS_PORT: "6379"

  SPRING_JPA_HIBERNATE_DDL_AUTO: "update"
  SPRING_JPA_SHOW_SQL: "false"
  SPRING_CACHE_REDIS_TTL: "3600000"

  SERVER_PORT: "8080"
  APP_BASE_URL: "http://shortener.local"

  JWT_EXPIRATION: "3600000"
```

### 4. Create Backend Secret

Example `k8s/minikube/backend-secret.yaml`:

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: backend-secret
type: Opaque
stringData:
  SPRING_DATASOURCE_PASSWORD: "password"
  JWT_SECRET: "change-me-long-secret-change-me-long-secret"
```

For local Minikube testing, this file may be used only locally. Do not commit real secrets to Git.

### 5. Deploy PostgreSQL

Example `postgres-deployment.yaml`:

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: postgres
spec:
  replicas: 1
  selector:
    matchLabels:
      app.kubernetes.io/name: postgres
  template:
    metadata:
      labels:
        app.kubernetes.io/name: postgres
    spec:
      containers:
        - name: postgres
          image: postgres:16-alpine
          ports:
            - containerPort: 5432
          env:
            - name: POSTGRES_DB
              value: urldb
            - name: POSTGRES_USER
              value: user
            - name: POSTGRES_PASSWORD
              valueFrom:
                secretKeyRef:
                  name: backend-secret
                  key: SPRING_DATASOURCE_PASSWORD
```

Example `postgres-service.yaml`:

```yaml
apiVersion: v1
kind: Service
metadata:
  name: postgres
spec:
  type: ClusterIP
  selector:
    app.kubernetes.io/name: postgres
  ports:
    - name: postgres
      port: 5432
      targetPort: 5432
```

### 6. Deploy Redis

Example `redis-deployment.yaml`:

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: redis
spec:
  replicas: 1
  selector:
    matchLabels:
      app.kubernetes.io/name: redis
  template:
    metadata:
      labels:
        app.kubernetes.io/name: redis
    spec:
      containers:
        - name: redis
          image: redis:7-alpine
          ports:
            - containerPort: 6379
```

Example `redis-service.yaml`:

```yaml
apiVersion: v1
kind: Service
metadata:
  name: redis
spec:
  type: ClusterIP
  selector:
    app.kubernetes.io/name: redis
  ports:
    - name: redis
      port: 6379
      targetPort: 6379
```

### 7. Deploy Backend

Example `backend-deployment.yaml`:

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: backend
spec:
  replicas: 1
  selector:
    matchLabels:
      app.kubernetes.io/name: backend
  template:
    metadata:
      labels:
        app.kubernetes.io/name: backend
    spec:
      containers:
        - name: backend
          image: backend:local
          imagePullPolicy: Never
          ports:
            - containerPort: 8080
          envFrom:
            - configMapRef:
                name: backend-config
          env:
            - name: SPRING_DATASOURCE_PASSWORD
              valueFrom:
                secretKeyRef:
                  name: backend-secret
                  key: SPRING_DATASOURCE_PASSWORD
            - name: JWT_SECRET
              valueFrom:
                secretKeyRef:
                  name: backend-secret
                  key: JWT_SECRET
```

Example `backend-service.yaml`:

```yaml
apiVersion: v1
kind: Service
metadata:
  name: backend
spec:
  type: ClusterIP
  selector:
    app.kubernetes.io/name: backend
  ports:
    - name: http
      port: 8080
      targetPort: 8080
```

### 8. Deploy Nginx

Example `nginx-deployment.yaml`:

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: nginx
spec:
  replicas: 1
  selector:
    matchLabels:
      app.kubernetes.io/name: nginx
  template:
    metadata:
      labels:
        app.kubernetes.io/name: nginx
    spec:
      containers:
        - name: nginx
          image: nginx:local
          imagePullPolicy: Never
          ports:
            - containerPort: 80
```

Example `nginx-service.yaml`:

```yaml
apiVersion: v1
kind: Service
metadata:
  name: nginx
spec:
  type: NodePort
  selector:
    app.kubernetes.io/name: nginx
  ports:
    - name: http
      port: 80
      targetPort: 80
      nodePort: 30080
```

### 9. Apply Minikube Manifests

```bash
kubectl apply -f k8s/minikube/
```

Check resources:

```bash
kubectl get pods
kubectl get svc
```

### 10. Access the Application on Minikube

Option 1: use Minikube service URL:

```bash
minikube service nginx
```

Option 2: use a local domain.

Get Minikube IP:

```bash
minikube ip
```

Add this line to `/etc/hosts` on Linux/macOS:

```text
<MINIKUBE_IP> shortener.local
```

On Windows, add the same line to:

```text
C:\Windows\System32\drivers\etc\hosts
```

Then open:

```text
http://shortener.local:30080
```

If you expose Nginx on port `80` through an Ingress or LoadBalancer tunnel, you can use:

```text
http://shortener.local
```

## Deploying on Kubernetes

This section describes deployment to a real Kubernetes cluster, for example Yandex Managed Kubernetes.

Production deployment uses:

```text
Nginx LoadBalancer -> Backend ClusterIP -> Managed PostgreSQL + Redis
```

Only Nginx is exposed publicly.

The backend, Redis, and PostgreSQL should not be exposed directly to the internet.

### 1. Build and Push Docker Images

Create a Yandex Container Registry:

```bash
yc container registry create --name url-shortener-registry
```

Get registry ID:

```bash
REGISTRY_ID=$(yc container registry get --name url-shortener-registry --format json | jq -r .id)
```

Configure Docker authentication:

```bash
yc container registry configure-docker
```

Build and push backend image:

```bash
docker build -t cr.yandex/$REGISTRY_ID/backend:1.0.0 .
docker push cr.yandex/$REGISTRY_ID/backend:1.0.0
```

Build and push Nginx/frontend image:

```bash
docker build -t cr.yandex/$REGISTRY_ID/nginx:1.0.0 ./nginx
docker push cr.yandex/$REGISTRY_ID/nginx:1.0.0
```

### 2. Allow Kubernetes Nodes to Pull Images

The Kubernetes node service account must have permission to pull images from the registry.

Get the node service account ID:

```bash
yc managed-kubernetes cluster get <CLUSTER_ID> --format json \
  | jq -r '.node_service_account_id'
```

Grant pull permission:

```bash
FOLDER_ID=$(yc config get folder-id)

yc resource-manager folder add-access-binding $FOLDER_ID \
  --role container-registry.images.puller \
  --subject serviceAccount:<NODE_SERVICE_ACCOUNT_ID>
```

### 3. Configure Managed PostgreSQL

For production, the application can use Yandex Managed PostgreSQL instead of an in-cluster PostgreSQL pod.

Example JDBC URL:

```text
jdbc:postgresql://c-<POSTGRES_CLUSTER_ID>.rw.mdb.yandexcloud.net:6432/urldb?targetServerType=master&sslmode=disable
```

Use `sslmode=disable` only when the Kubernetes cluster and the PostgreSQL cluster are in the same private cloud network and public access is not required.

For public PostgreSQL access, configure SSL properly.

### 4. Create Backend ConfigMap

Example `k8s/production/backend-config.yaml`:

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: backend-config
data:
  SPRING_DATASOURCE_URL: "jdbc:postgresql://c-<POSTGRES_CLUSTER_ID>.rw.mdb.yandexcloud.net:6432/urldb?targetServerType=master&sslmode=disable"
  SPRING_DATASOURCE_USERNAME: "user"

  SPRING_REDIS_HOST: "redis"
  SPRING_REDIS_PORT: "6379"

  SPRING_JPA_HIBERNATE_DDL_AUTO: "update"
  SPRING_JPA_SHOW_SQL: "false"
  SPRING_CACHE_REDIS_TTL: "3600000"

  SERVER_PORT: "8080"
  APP_BASE_URL: "http://urlshortener.ru"

  JWT_EXPIRATION: "3600000"
```

### 5. Configure Secrets

For production, secrets should not be stored directly in Git.

Recommended approach:

```text
Yandex Lockbox -> External Secrets Operator -> Kubernetes Secret -> Backend Pod
```

Required secret values:

```text
SPRING_DATASOURCE_PASSWORD
JWT_SECRET
```

If the PostgreSQL password is stored in Lockbox under the key:

```text
postgresql_password
```

then map it to the backend environment variable:

```yaml
secretKey: SPRING_DATASOURCE_PASSWORD
remoteRef:
  property: postgresql_password
```

Example `SecretStore`:

```yaml
apiVersion: external-secrets.io/v1
kind: SecretStore
metadata:
  name: yandex-lockbox-store
spec:
  provider:
    yandexlockbox:
      auth:
        authorizedKeySecretRef:
          name: yc-auth
          key: authorized-key
```

Example `ExternalSecret`:

```yaml
apiVersion: external-secrets.io/v1
kind: ExternalSecret
metadata:
  name: backend-external-secret
spec:
  refreshInterval: 1h
  secretStoreRef:
    name: yandex-lockbox-store
    kind: SecretStore
  target:
    name: backend-secret
    creationPolicy: Owner
  data:
    - secretKey: SPRING_DATASOURCE_PASSWORD
      remoteRef:
        key: <LOCKBOX_SECRET_ID>
        property: postgresql_password

    - secretKey: JWT_SECRET
      remoteRef:
        key: <LOCKBOX_SECRET_ID>
        property: JWT_SECRET
```

### 6. Deploy Redis

Redis can run inside Kubernetes as a private `ClusterIP` service.

```bash
kubectl apply -f k8s/production/redis-deployment.yaml
kubectl apply -f k8s/production/redis-service.yaml
```

### 7. Deploy Backend

Example production backend image:

```yaml
image: cr.yandex/<REGISTRY_ID>/backend:1.0.0
imagePullPolicy: IfNotPresent
```

Apply backend manifests:

```bash
kubectl apply -f k8s/production/backend-config.yaml
kubectl apply -f k8s/production/backend-deployment.yaml
kubectl apply -f k8s/production/backend-service.yaml
```

Check backend:

```bash
kubectl get pods
kubectl logs deployment/backend
```

### 8. Deploy Nginx

Example production Nginx image:

```yaml
image: cr.yandex/<REGISTRY_ID>/nginx:1.0.0
imagePullPolicy: IfNotPresent
```

Expose Nginx using a `LoadBalancer` service.

Example `nginx-service.yaml`:

```yaml
apiVersion: v1
kind: Service
metadata:
  name: nginx
spec:
  type: LoadBalancer
  selector:
    app.kubernetes.io/name: nginx
  ports:
    - name: http
      port: 80
      targetPort: 80
```

Apply:

```bash
kubectl apply -f k8s/production/nginx-deployment.yaml
kubectl apply -f k8s/production/nginx-service.yaml
```

Get the external IP:

```bash
kubectl get svc nginx
```

The application will be available at:

```text
http://<EXTERNAL-IP>
```

### 9. Configure Domain

If the domain is managed by Beget DNS, create DNS records:

```text
@      A      <NGINX_EXTERNAL_IP>
www    A      <NGINX_EXTERNAL_IP>
```

Example:

```text
@      A      158.160.202.54
www    A      158.160.202.54
```

Check DNS:

```bash
dig urlshortener.ru
dig www.urlshortener.ru
```

Check the site:

```bash
curl -v http://urlshortener.ru/
```

The response should contain the Angular application HTML.

### 10. Update APP_BASE_URL

After the domain is configured, make sure the backend generates short links with the real domain:

```yaml
APP_BASE_URL: "http://urlshortener.ru"
```

Apply and restart backend:

```bash
kubectl apply -f k8s/production/backend-config.yaml
kubectl rollout restart deployment/backend
```

Now generated short links should look like:

```text
http://urlshortener.ru/a1b2c3d4
```

## Useful Kubernetes Commands

Check all resources:

```bash
kubectl get pods
kubectl get svc
kubectl get deploy
kubectl get configmap
kubectl get secret
```

Check backend logs:

```bash
kubectl logs deployment/backend
```

Restart backend:

```bash
kubectl rollout restart deployment/backend
```

Check Nginx external IP:

```bash
kubectl get svc nginx
```

Check External Secrets:

```bash
kubectl get secretstore
kubectl get externalsecret
kubectl get secret backend-secret
```

## Notes

* Short codes are generated as 8-character hexadecimal strings.
* Nginx proxies `/api/` requests to the backend.
* Nginx proxies root-level short links matching `/{8 hex characters}` to the backend redirect endpoint.
* All protected URL management endpoints require a valid JWT.
* Public redirect endpoints do not require authentication.
* Redis is used to cache URL lookups.
* PostgreSQL stores users, URL mappings, and link metadata.
* In production, only Nginx should be exposed publicly.
