{{- define "front.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" }}
{{- end }}

{{- define "front.fullname" -}}
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

{{- define "front.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" }}
{{- end }}

{{- define "front.labels" -}}
helm.sh/chart: {{ include "front.chart" . }}
{{ include "front.selectorLabels" . }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}

{{- define "front.selectorLabels" -}}
app.kubernetes.io/name: {{ include "front.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}

{{- define "front.gatewayHost" -}}
{{- .Values.gateway.host | default (printf "%s-gateway" .Release.Name) }}
{{- end }}

{{- define "front.keycloakHost" -}}
{{- .Values.keycloak.host | default (printf "%s-keycloak" .Release.Name) }}
{{- end }}

{{- define "front.keycloakRealm" -}}
{{- .Values.keycloak.realm | default .Values.global.keycloak.realm | default "mybank" }}
{{- end }}

{{- define "front.publicBaseUrl" -}}
{{- .Values.publicBaseUrl | default .Values.global.keycloak.publicBaseUrl | default "http://localhost" }}
{{- end }}

{{- define "front.oidcIssuerUri" -}}
{{- .Values.keycloak.oidcIssuerUri | default .Values.global.keycloak.issuerUri | default (printf "%s/realms/%s" (include "front.publicBaseUrl" .) (include "front.keycloakRealm" .)) }}
{{- end }}
