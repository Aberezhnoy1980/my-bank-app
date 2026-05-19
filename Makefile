COMPOSE_BASE = docker compose -f docker-compose.yml
COMPOSE_SECURE = docker compose -f docker-compose.yml -f docker-compose.secure.yml

.PHONY: up up-secure down down-secure ps logs smoke-a smoke-c

up:
	$(COMPOSE_BASE) up --build -d

up-secure:
	$(COMPOSE_SECURE) up --build -d

down:
	$(COMPOSE_BASE) down

down-secure:
	$(COMPOSE_SECURE) down

ps:
	$(COMPOSE_BASE) ps

logs:
	$(COMPOSE_BASE) logs -f gateway

smoke-a:
	curl -s http://localhost:8081/actuator/health
	curl -s http://localhost:8081/api/accounts/me
	curl -s -X PUT http://localhost:8081/api/accounts/me -H "Content-Type: application/json" -d '{"fullName":"Demo User","birthDate":"1995-05-20"}'
	curl -s -X POST http://localhost:8081/api/cash/deposit -H "Content-Type: application/json" -d '{"amount":100.50}'
	curl -s -X POST http://localhost:8081/api/transfers -H "Content-Type: application/json" -d '{"recipientUsername":"alice.user","amount":50.00}'
	curl -s -X POST http://localhost:8081/api/notifications -H "Content-Type: application/json" -d '{"eventType":"MANUAL_TEST","message":"hello"}'

smoke-c:
	curl -s -X POST "http://localhost:8090/realms/mybank/protocol/openid-connect/token" \
		-d "grant_type=password" \
		-d "client_id=mybank-front" \
		-d "client_secret=$${KEYCLOAK_FRONT_SECRET:-front-secret-change-me}" \
		-d "username=demo.user" \
		-d "password=demo" \
		-o /tmp/token.json
	export TOKEN=$$(python3 -c "import json; print(json.load(open('/tmp/token.json'))['access_token'])") && \
	curl -s -w "\nHTTP:%{http_code}\n" http://localhost:8081/actuator/health -H "Authorization: Bearer $$TOKEN" && \
	curl -s -w "\nHTTP:%{http_code}\n" http://localhost:8081/api/accounts/me -H "Authorization: Bearer $$TOKEN"
