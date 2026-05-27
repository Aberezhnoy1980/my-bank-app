{{- define "cash-service.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" }}
{{- end }}

{{- define "cash-service.fullname" -}}
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

{{- define "cash-service.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" }}
{{- end }}

{{- define "cash-service.labels" -}}
helm.sh/chart: {{ include "cash-service.chart" . }}
{{ include "cash-service.selectorLabels" . }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}

{{- define "cash-service.selectorLabels" -}}
app.kubernetes.io/name: {{ include "cash-service.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}

{{- define "cash-service.postgresHost" -}}
{{- .Values.postgres.host | default (printf "%s-postgres" .Release.Name) }}
{{- end }}

{{- define "cash-service.postgresSecretName" -}}
{{- .Values.postgres.secretName | default (printf "%s-credentials" (include "cash-service.postgresHost" .)) }}
{{- end }}

{{- define "cash-service.gatewayHost" -}}
{{- .Values.gateway.host | default (printf "%s-gateway" .Release.Name) }}
{{- end }}

{{- define "cash-service.keycloakHost" -}}
{{- .Values.keycloak.host | default (printf "%s-keycloak" .Release.Name) }}
{{- end }}

{{- define "cash-service.keycloakRealm" -}}
{{- .Values.keycloak.realm | default .Values.global.keycloak.realm | default "mybank" }}
{{- end }}

{{- define "cash-service.keycloakIssuerUri" -}}
{{- .Values.keycloak.issuerUri | default .Values.global.keycloak.issuerUri | default "http://localhost/realms/mybank" }}
{{- end }}

{{- define "cash-service.keycloakJwkSetUri" -}}
{{- printf "http://%s:%v/realms/%s/protocol/openid-connect/certs" (include "cash-service.keycloakHost" .) (.Values.keycloak.port | default 8080) (include "cash-service.keycloakRealm" .) }}
{{- end }}

{{- define "cash-service.kafkaBootstrapServers" -}}
{{- $svcKafka := .Values.kafka | default dict -}}
{{- $globalKafka := .Values.global.kafka | default dict -}}
{{- $svcKafka.bootstrapServers | default $globalKafka.bootstrapServers | default (printf "%s-kafka:9092" .Release.Name) }}
{{- end }}

{{- define "cash-service.kafkaTopic" -}}
{{- $svcKafka := .Values.kafka | default dict -}}
{{- $globalKafka := .Values.global.kafka | default dict -}}
{{- $svcKafka.topic | default $globalKafka.topic | default "bank.notifications" }}
{{- end }}
