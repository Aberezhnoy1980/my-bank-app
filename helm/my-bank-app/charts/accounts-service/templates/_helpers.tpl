{{- define "accounts-service.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" }}
{{- end }}

{{- define "accounts-service.fullname" -}}
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

{{- define "accounts-service.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" }}
{{- end }}

{{- define "accounts-service.labels" -}}
helm.sh/chart: {{ include "accounts-service.chart" . }}
{{ include "accounts-service.selectorLabels" . }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}

{{- define "accounts-service.selectorLabels" -}}
app.kubernetes.io/name: {{ include "accounts-service.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}

{{- define "accounts-service.postgresHost" -}}
{{- .Values.postgres.host | default (printf "%s-postgres" .Release.Name) }}
{{- end }}

{{- define "accounts-service.postgresSecretName" -}}
{{- .Values.postgres.secretName | default (printf "%s-credentials" (include "accounts-service.postgresHost" .)) }}
{{- end }}

{{- define "accounts-service.gatewayHost" -}}
{{- .Values.gateway.host | default (printf "%s-gateway" .Release.Name) }}
{{- end }}

{{- define "accounts-service.keycloakHost" -}}
{{- .Values.keycloak.host | default (printf "%s-keycloak" .Release.Name) }}
{{- end }}

{{- define "accounts-service.keycloakRealm" -}}
{{- .Values.keycloak.realm | default .Values.global.keycloak.realm | default "mybank" }}
{{- end }}

{{- define "accounts-service.keycloakIssuerUri" -}}
{{- .Values.keycloak.issuerUri | default .Values.global.keycloak.issuerUri | default "http://localhost/realms/mybank" }}
{{- end }}

{{- define "accounts-service.keycloakJwkSetUri" -}}
{{- printf "http://%s:%v/realms/%s/protocol/openid-connect/certs" (include "accounts-service.keycloakHost" .) (.Values.keycloak.port | default 8080) (include "accounts-service.keycloakRealm" .) }}
{{- end }}

{{- define "accounts-service.kafkaBootstrapServers" -}}
{{- $svcKafka := .Values.kafka | default dict -}}
{{- $globalKafka := .Values.global.kafka | default dict -}}
{{- $svcKafka.bootstrapServers | default $globalKafka.bootstrapServers | default (printf "%s-kafka:9092" .Release.Name) }}
{{- end }}

{{- define "accounts-service.kafkaTopic" -}}
{{- $svcKafka := .Values.kafka | default dict -}}
{{- $globalKafka := .Values.global.kafka | default dict -}}
{{- $svcKafka.topic | default $globalKafka.topic | default "bank.notifications" }}
{{- end }}
