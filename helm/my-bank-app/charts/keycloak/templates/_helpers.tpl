{{- define "keycloak.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" }}
{{- end }}

{{- define "keycloak.fullname" -}}
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

{{- define "keycloak.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" }}
{{- end }}

{{- define "keycloak.labels" -}}
helm.sh/chart: {{ include "keycloak.chart" . }}
{{ include "keycloak.selectorLabels" . }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}

{{- define "keycloak.selectorLabels" -}}
app.kubernetes.io/name: {{ include "keycloak.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}

{{- define "keycloak.adminSecretName" -}}
{{- printf "%s-admin" (include "keycloak.fullname" .) }}
{{- end }}

{{- define "keycloak.realmConfigMapName" -}}
{{- printf "%s-realm" (include "keycloak.fullname" .) }}
{{- end }}

{{- define "keycloak.hostname" -}}
{{- .Values.hostname | default .Values.global.keycloak.hostname | default "http://localhost" }}
{{- end }}

{{- define "keycloak.realm" -}}
{{- .Values.realm | default .Values.global.keycloak.realm | default "mybank" }}
{{- end }}
