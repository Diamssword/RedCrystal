package com.diamssword.redCrystal.generated;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class JsonGenerator {
	public static String readFile(String subpath) throws IOException {
		return readFile(GlyphGenerator.getDevAsset(subpath));
	}

	public static String readFile(Path fullPath) throws IOException {
		var reader = new FileReader(fullPath.toFile());
		var str = reader.readAllAsString();
		reader.close();
		return str;
	}

	public static void writeFile(String text, Path path, boolean checkExist) throws IOException {
		if(!checkExist || !path.toFile().exists()) {
			var writer = new FileWriter(path.toFile());
			writer.write(text);
			writer.close();
		}
	}

	public static String replaceVars(String text, Map<String, String> pairs) {

		for(Map.Entry<String, String> entry : pairs.entrySet()) {
			text = text.replaceAll("\\{\\$" + entry.getKey() + "}", entry.getValue());
		}
		return text;
	}

	public static String replaceVars(String text, String... pairs) {
		Map<String, String> map = new HashMap<>();
		for(int i = 0; i < pairs.length; i += 2) {
			if(i + 1 < pairs.length) {
				map.put(pairs[i], pairs[i + 1]);
			}
		}
		return replaceVars(text, map);
	}

	public static String appendLangKeyIfNotFound(String content, String key, String text) {
		if(!findLangKey(content, key)) {
			return content + "\n" + key + "=[TMP]" + text;
		}
		return content;
	}

	public static boolean findLangKey(String content, String key) {
		return content.lines().anyMatch(l -> {
			if(!l.trim().startsWith("#")) {
				var ind = l.indexOf("=");
				if(ind > -1) {
					return l.substring(0, ind).equals(key);
				}
			}
			return false;
		});
	}
}
