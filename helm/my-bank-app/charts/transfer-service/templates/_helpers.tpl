{{- define "transfer-service.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" }}
{{- end }}

{{- define "transfer-service.fullname" -}}
{{- if .Values.fullnameOverride }}
{{- .Values.fullnameOverride | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- $name := default .Chart.Name .Values.nameOverride }}
{{- if contains $name .Release.Name }}
{{- .Release.Name | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- printf "%s-%s" .Release.Name $name | trunc 63 | trimSuffix "-" }}
{{- end }}
{{- end }}
{{- end }}

{{- define "transfer-service.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" }}
{{- end }}

{{- define "transfer-service.labels" -}}
helm.sh/chart: {{ include "transfer-service.chart" . }}
{{ include "transfer-service.selectorLabels" . }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}

{{- define "transfer-service.selectorLabels" -}}
app.kubernetes.io/name: {{ include "transfer-service.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}

{{- define "transfer-service.postgresHost" -}}
{{- .Values.postgres.host | default (printf "%s-postgres" .Release.Name) }}
{{- end }}

{{- define "transfer-service.postgresSecretName" -}}
{{- .Values.postgres.secretName | default (printf "%s-credentials" (include "transfer-service.postgresHost" .)) }}
{{- end }}

{{- define "transfer-service.gatewayHost" -}}
{{- .Values.gateway.host | default (printf "%s-gateway" .Release.Name) }}
{{- end }}

{{- define "transfer-service.keycloakHost" -}}
{{- .Values.keycloak.host | default (printf "%s-keycloak" .Release.Name) }}
{{- end }}

{{- define "transfer-service.keycloakRealm" -}}
{{- .Values.keycloak.realm | default .Values.global.keycloak.realm | default "mybank" }}
{{- end }}

{{- define "transfer-service.keycloakIssuerUri" -}}
{{- .Values.keycloak.issuerUri | default .Values.global.keycloak.issuerUri | default "http://localhost/realms/mybank" }}
{{- end }}

{{- define "transfer-service.keycloakJwkSetUri" -}}
{{- printf "http://%s:%v/realms/%s/protocol/openid-connect/certs" (include "transfer-service.keycloakHost" .) (.Values.keycloak.port | default 8080) (include "transfer-service.keycloakRealm" .) }}
{{- end }}

{{- define "transfer-service.kafkaBootstrapServers" -}}
{{- $svcKafka := .Values.kafka | default dict -}}
{{- $globalKafka := .Values.global.kafka | default dict -}}
{{- $svcKafka.bootstrapServers | default $globalKafka.bootstrapServers | default (printf "%s-kafka:9092" .Release.Name) }}
{{- end }}

{{- define "transfer-service.kafkaTopic" -}}
{{- $svcKafka := .Values.kafka | default dict -}}
{{- $globalKafka := .Values.global.kafka | default dict -}}
{{- $svcKafka.topic | default $globalKafka.topic | default "bank.notifications" }}
{{- end }}
