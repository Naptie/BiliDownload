package me.naptie.bilidownload.utils;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.Map;

public class ConfigManager {

	private static boolean initialized = false;
	private static File file;
	private static Yaml yaml;
	private static Map<String, Object> content;

	public static void init(File f) {
		if (!initialized || file.compareTo(f) == 0) {
			file = f;
			DumperOptions options = new DumperOptions();
			options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
			yaml = new Yaml(options);
			FileInputStream inputStream = null;
			try {
				inputStream = new FileInputStream(file);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			content = yaml.load(inputStream);
			initialized = true;
		}
	}

	public static Map<String, Object> get() {
		return content;
	}

	public static void dump(Map<String, Object> data) throws IOException {
		yaml.dump(data, new FileWriter(file));
	}

}
