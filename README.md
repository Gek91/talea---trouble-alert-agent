deploy line



gcloud functions deploy talea-trouble-alert-agent --entry-point functions.PubSubChatFunction --runtime java11 --trigger-topic alert 
--set-env-vars CHAT_WEBHOOK={CHAT WEBHOOK} --region=europe-west1 --memory=128MB --max-instances=1


Needed to do
https://cloud.google.com/monitoring/support/notification-options#pubsub


message example
{"incident": {"incident_id": "0.lz90hdsmxxxc","resource_id": "","resource_name": "knet-unisalute-dev default:test","resource": {"type": "gae_app","labels": {"module_id":"default","project_id":"knet-unisalute-dev","version_id":"test","zone":"europe-west2-2"}},"resource_display_name": "default:test","resource_type_display_name": "GAE Application","metric":{"type": "appengine.googleapis.com/http/server/response_count", "displayName": "Response count"},"started_at": 1614110448,"policy_name": "We have a problem","condition_name": "Logs-based metric errors for test [COUNT]","condition": {"name":"projects/knet-unisalute-dev/alertPolicies/9413140536381520987/conditions/9413140536381521508","displayName":"Logs-based metric errors for test [COUNT]","conditionThreshold":{"filter":"metric.type=\"appengine.googleapis.com/http/server/response_count\" resource.type=\"gae_app\" metric.label.\"response_code\"=\"500\" resource.label.\"project_id\"=\"knet-unisalute-dev\"","aggregations":[{"alignmentPeriod":"60s","perSeriesAligner":"ALIGN_RATE"}],"comparison":"COMPARISON_GT","thresholdValue":0.1,"duration":"0s","trigger":{"count":1}}},"url": "https://console.cloud.google.com/monitoring/alerting/incidents/0.lz90hdsmxxxc?project=knet-unisalute-dev","state": "open","ended_at": null,"summary": "Response count for knet-unisalute-dev default:test with metric labels {loading=false} is above the threshold of 0.1 with a value of 0.117."},"version": "1.2"}