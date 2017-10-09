package org.jolokia.push;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

import org.jolokia.http.HttpRequestHandler;
import org.jolokia.util.JulLogHandler;
import org.jolokia.util.LogHandler;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * PushWorker
	TODO Config file
		- Config file not exist시 정상 동작 체크
	    - 전송 주기 설정
		- Reload config file
		- Add instance in config file
  	
  	Push json example:
		{"request":{"mbean":"java.lang:type=Memory","type":"read"},"instance":"com.skplanet.abt.template.DemoApplication","host":"SKP1001291MN001.local","value":{"ObjectPendingFinalizationCount":0,"Verbose":false,"HeapMemoryUsage":{"init":268435456,"committed":387448832,"max":3817865216,"used":140176912},"NonHeapMemoryUsage":{"init":2555904,"committed":54919168,"max":-1,"used":53721896},"ObjectName":{"objectName":"java.lang:type=Memory"}},"timestamp":1498384387,"status":200}
		{"request":{"mbean":"java.lang:name=PS MarkSweep,type=GarbageCollector","type":"read"},"instance":"com.skplanet.abt.template.DemoApplication","host":"SKP1001291MN001.local","value":{"MemoryPoolNames":["PS Eden Space","PS Survivor Space","PS Old Gen"],"LastGcInfo":{"duration":24,"memoryUsageBeforeGc":{"PS Eden Space":{"init":67108864,"committed":134217728,"max":1409286144,"used":0},"Code Cache":{"init":2555904,"committed":6750208,"max":251658240,"used":6727168},"Compressed Class Space":{"init":0,"committed":2883584,"max":1073741824,"used":2650512},"PS Survivor Space":{"init":11010048,"committed":11010048,"max":11010048,"used":10981552},"PS Old Gen":{"init":179306496,"committed":179306496,"max":2863661056,"used":2071048},"Metaspace":{"init":0,"committed":21757952,"max":-1,"used":21346208}},"GcThreadCount":8,"startTime":1136,"endTime":1160,"id":1,"memoryUsageAfterGc":{"PS Eden Space":{"init":67108864,"committed":134217728,"max":1409286144,"used":0},"Code Cache":{"init":2555904,"committed":6750208,"max":251658240,"used":6727168},"Compressed Class Space":{"init":0,"committed":2883584,"max":1073741824,"used":2650512},"PS Survivor Space":{"init":11010048,"committed":11010048,"max":11010048,"used":0},"PS Old Gen":{"init":179306496,"committed":112197632,"max":2863661056,"used":11675800},"Metaspace":{"init":0,"committed":21757952,"max":-1,"used":21346208}}},"CollectionTime":23,"Valid":true,"CollectionCount":1,"Name":"PS MarkSweep","ObjectName":{"objectName":"java.lang:name=PS MarkSweep,type=GarbageCollector"}},"timestamp":1498384387,"status":200}
		{"request":{"mbean":"java.lang:name=PS Scavenge,type=GarbageCollector","type":"read"},"instance":"com.skplanet.abt.template.DemoApplication","host":"SKP1001291MN001.local","value":{"MemoryPoolNames":["PS Eden Space","PS Survivor Space"],"LastGcInfo":{"duration":13,"memoryUsageBeforeGc":{"PS Eden Space":{"init":67108864,"committed":200802304,"max":1413480448,"used":200802304},"Code Cache":{"init":2555904,"committed":13303808,"max":251658240,"used":13231232},"Compressed Class Space":{"init":0,"committed":4194304,"max":1073741824,"used":3983552},"PS Survivor Space":{"init":11010048,"committed":6815744,"max":6815744,"used":6419832},"PS Old Gen":{"init":179306496,"committed":112197632,"max":2863661056,"used":11683992},"Metaspace":{"init":0,"committed":32505856,"max":-1,"used":31766256}},"GcThreadCount":8,"startTime":2599,"endTime":2612,"id":5,"memoryUsageAfterGc":{"PS Eden Space":{"init":67108864,"committed":264241152,"max":1402994688,"used":0},"Code Cache":{"init":2555904,"committed":13303808,"max":251658240,"used":13231232},"Compressed Class Space":{"init":0,"committed":4194304,"max":1073741824,"used":3983552},"PS Survivor Space":{"init":11010048,"committed":11010048,"max":11010048,"used":10982864},"PS Old Gen":{"init":179306496,"committed":112197632,"max":2863661056,"used":15749224},"Metaspace":{"init":0,"committed":32505856,"max":-1,"used":31766256}}},"CollectionTime":41,"Valid":true,"CollectionCount":5,"Name":"PS Scavenge","ObjectName":{"objectName":"java.lang:name=PS Scavenge,type=GarbageCollector"}},"timestamp":1498384387,"status":200}
 */
public class PushWorker {
	HttpRequestHandler requestHandler;
	JSONArray typeList;
	List<EventLogger> producerList;
	LogHandler logHandler;

	String host="";
	String instance="";
	
	public PushWorker(HttpRequestHandler pRequestHandler, JSONArray pTypeList, List<EventLogger> pProducerList, LogHandler pLogHandler) {
        requestHandler = pRequestHandler;
        typeList = pTypeList;
        producerList = pProducerList;
        logHandler = pLogHandler;
        init();
    }
	
	public void init() {		
		host = execCommand("hostname");
//		host = 	InetAddress.getLocalHost().getHostName();

		instance = System.getProperty("sun.java.command");
//		instance = ManagementFactory.getRuntimeMXBean().getName();
		
	    Timer jobScheduler = new Timer();
	    jobScheduler.scheduleAtFixedRate(new ScheduledJob(), 1000, 60000);
	}
	
	private String execCommand(String execCommand) {
        Process proc;
        Scanner s=null;
        String output="localhost";
		try {
			proc = Runtime.getRuntime().exec(execCommand);
			s = new Scanner(proc.getInputStream()).useDelimiter("\\A");
			if (s.hasNext())
				output = s.next();
		} catch (IOException e) {
			logHandler.error("execCommand exception", e);
		} finally {
			if (s != null) s.close();
		}
        return output.trim();
    }
	
	/*
	 * handleGetTrequest example:
	 * 	uri = "/jolokia/read/java.lang:type=Memory/HeapMemoryUsage";
     *  pathInfo = "read/java.lang:type=Memory/HeapMemoryUsage"; 
     */
	public void push() {
		HashMap<String, String[]> parameterMap = null;
		JSONArray jarray = new JSONArray();
	   
	    logHandler.debug("push start: " + new Date().toString());
	    logHandler.debug("confTypes size=" + typeList.size());
	    logHandler.debug("producerList size=" + producerList.size());
	    for (int i=0; i<typeList.size(); i++) {
		    	String type = (String) typeList.get(i);
		    	String uri = "/jolokia";
		    	String pathInfo = "/read/" + type;
		    	
		    	JSONObject json = (JSONObject) requestHandler.handleGetRequest(uri, pathInfo, parameterMap);
		    	json.put("host", host);
		    	json.put("instance", instance);
			    logHandler.debug("json=" + json);
			    System.out.println("json output=" + json);
			    jarray.add(json);
	    }
	    
	    // Send event
	    if (jarray.size() > 1) {
		    	for (EventLogger producer: producerList) {
		    		producer.logEvent(jarray.toString());
		    	}
	    }
	}
	
	class ScheduledJob extends TimerTask {
	    public void run() {
	    	push ();
	   }
	}
}
