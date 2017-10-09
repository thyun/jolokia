package org.jolokia.push;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.jolokia.util.JulLogHandler;
import org.jolokia.util.LogHandler;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * PushConfig
 */
public class PushConfig {
	private static final LogHandler logHandler = new JulLogHandler();
	private static final String file = "jolokia-push.conf";
	private static JSONObject config = null;

	public static JSONObject getConfig() {
		if (config != null)
			return config;
		
		// Get config form file or resource
		InputStream stream = getConfigInputStream();
		if (stream == null) {
			config = new JSONObject();
			return config;
		}
		
		// Get config json
		Reader r = new InputStreamReader(stream);
		JSONParser parser = new JSONParser();
		try {
			config = (JSONObject) parser.parse(r);
		} catch (ParseException e) {
			logHandler.error("getConfig parse exception", e);
		} catch (IOException e) {
			logHandler.error("getConfig exception", e);
		}
		return config;
	}
	
	private static InputStream getConfigInputStream() {
		String workDir = System.getProperty("user.dir");
		String path = workDir + "/" + file;
		InputStream inputStream = null;
		try {
			inputStream = new FileInputStream(new File(path));
			logHandler.debug("getConfigInputStream config from file: " + path);
			return inputStream;
		} catch (FileNotFoundException e) {
			// Possible
		}
		
		inputStream = PushConfig.class.getClassLoader().getResourceAsStream(file);
		logHandler.debug("getConfigInputStream config from resource: " + file);
		return inputStream;
	}

	public static JSONArray createTypeList() {
		JSONObject config = getConfig();
		return (JSONArray) config.getOrDefault("types", new JSONArray());
	}
	
	public static List<EventLogger> createProducerList() {
		JSONObject config = getConfig();
		JSONArray confProducers = (JSONArray) config.getOrDefault("producers", new JSONArray());
		ArrayList<EventLogger> producerList = new ArrayList<EventLogger>();
		
		for (int i=0; i<confProducers.size(); i++) {
			JSONObject confProducer = (JSONObject) confProducers.get(i);
			
			try {
				if ("http".equals(confProducer.get("type"))) {
					HashMap<String, Object> props = new HashMap<String, Object>();
					props.put(HttpPostLogger.URL_PROP_NAME, confProducer.get("url"));
					producerList.add(new HttpPostLogger(props));
					logHandler.debug("createPruducerList added producer: type=" + confProducer.get("type") + ", url=" + confProducer.get("url"));
				}
			} catch (NoSuchAlgorithmException e) {
				logHandler.error("createProducerList exception", e);
			}
		}
		return producerList;
	}
	
}
