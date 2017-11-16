Jolokia for push model.

Added new feature to version 1.3.7 that jolokia agent pushes JMX data via HTTP.
Until now this feature is experimental, but it's working.

## Features
* Push JMX data - Via HTTP only. Can be used with Logstash.

## Example JMX data flow
* jolokia agent -> Logstash -> Kafka/Elasticsearch ...

## Screenshots of Grafana

