Jolokia for push model.

Added new feature to version 1.3.7 that jolokia agent pushes JMX data to HTTP server.
Until now this feature is experimental, but it's working.

## Features
* Push JMX data - Via HTTP only. Can be used with Logstash.

## Example JMX data flow
* jolokia agent -> Logstash -> Kafka/Elasticsearch ...

## Hot to use
* Download and unzip jolokia-core-1.3.7.jar.zip from https://github.com/thyun/jolokia/files/1491443/jolokia-core-1.3.7.jar.zip
* Add jar to your project
* Build and install your project
* Make jolokia-push.conf like below and put the file to your install directory of your project
  * types : Specify metrics you want to collect.
  * producers : Specify HTTP server address (You can use Logstash as your receiver)
```
{
        "types": [
                "java.lang:type=Memory",
                "java.lang:type=GarbageCollector,name=PS MarkSweep",
                "java.lang:type=GarbageCollector,name=PS Scavenge"
        ],
        "producers": [
        {
                        "type": "http",
                        "url": "http://172.22.241.214:8080/v1/track"
                }
        ]
}
```
* Run your project

## Screenshots of Grafana
* Used Logstash to store JMX data to Elasticsearch.
* Used Grafana to draw charts from Elasticsearch.
![Grafana](https://user-images.githubusercontent.com/4827162/32880796-4a335658-caf2-11e7-8d48-7ed34c5a27fe.png)
