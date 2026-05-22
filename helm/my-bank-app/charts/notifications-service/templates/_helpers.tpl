{{- define "notifications-service.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" }}
{{- end }}

{{- define "notifications-service.fullname" -}}
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

{{- define "notifications-service.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" }}
{{- end }}

{{- define "notifications-service.labels" -}}
helm.sh/chart: {{ include "notifications-service.chart" . }}
{{ include "notifications-service.selectorLabels" . }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}

{{- define "notifications-service.selectorLabels" -}}
app.kubernetes.io/name: {{ include "notifications-service.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}

{{- define "notifications-service.postgresHost" -}}
{{- .Values.postgres.host | default (printf "%s-postgres" .Release.Name) }}
{{- end }}

{{- define "notifications-service.postgresSecretName" -}}
{{- .Values.postgres.secretName | default (printf "%s-credentials" (include "notifications-service.postgresHost" .)) }}
{{- end }}

{{- define "notifications-service.keycloakHost" -}}
{{- .Values.keycloak.host | default (printf "%s-keycloak" .Release.Name) }}
{{- end }}

{{- define "notifications-service.keycloakRealm" -}}
{{- .Values.keycloak.realm | default .Values.global.keycloak.realm | default "mybank" }}
{{- end }}

{{- define "notifications-service.keycloakIssuerUri" -}}
{{- .Values.keycloak.issuerUri | default .Values.global.keycloak.issuerUri | default "http://localhost/realms/mybank" }}
{{- end }}

{{- define "notifications-service.keycloakJwkSetUri" -}}
{{- printf "http://%s:%v/realms/%s/protocol/openid-connect/certs" (include "notifications-service.keycloakHost" .) (.Values.keycloak.port | default 8080) (include "notifications-service.keycloakRealm" .) }}
{{- end }}
