{{- define "elk.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" }}
{{- end }}

{{- define "elk.fullname" -}}
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

{{- define "elk.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" }}
{{- end }}

{{- define "elk.labels" -}}
helm.sh/chart: {{ include "elk.chart" . }}
{{ include "elk.selectorLabels" . }}
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}

{{- define "elk.selectorLabels" -}}
app.kubernetes.io/name: {{ include "elk.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}

{{- define "elk.componentLabels" -}}
{{ include "elk.selectorLabels" . }}
app.kubernetes.io/component: {{ .component }}
{{- end }}

{{- define "elk.elasticsearchFullname" -}}
{{- printf "%s-elasticsearch" (include "elk.fullname" .) }}
{{- end }}

{{- define "elk.logstashFullname" -}}
{{- printf "%s-logstash" (include "elk.fullname" .) }}
{{- end }}

{{- define "elk.kibanaFullname" -}}
{{- printf "%s-kibana" (include "elk.fullname" .) }}
{{- end }}

{{- define "elk.logstashConfigMapName" -}}
{{- printf "%s-logstash-pipeline" (include "elk.fullname" .) }}
{{- end }}
