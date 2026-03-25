package com.diamssword.redCrystal.generated;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public class GlyphGenerator {
	static void main() throws IOException {
		var runes = TextureGenerator.getImages("glyphs");
		var common = getCommonResource();
		System.out.println("starting glyph generation...");
		System.out.println("Common is at: " + Paths.get(common).toAbsolutePath());
		String lang = JsonGenerator.readFile(Path.of(getServerResource(), "Languages/en-US/server.lang"));
		for(Map.Entry<String, BufferedImage> entry : runes.entrySet()) {
			var k = entry.getKey();
			var v = entry.getValue();
			try {
				System.out.println("generating glyph " + k);
				ImageIO.write(v, "png", Paths.get(common, "UI/Custom/Pages/RedCrystal/Glyphs/" + k + "@2x.png").toFile());
				ImageIO.write(TextureGenerator.stichGlyph(v), "png", Paths.get(common, "Items/RedCrystal/Glyphs/" + k + ".png").toFile());
				JsonGenerator.writeFile(JsonGenerator.replaceVars(JsonGenerator.readFile("glyph_model.json"), "Id", k), Path.of(getServerResource(), "RedCrystal/Glyphs/" + k + ".json"), true);
				lang = JsonGenerator.appendLangKeyIfNotFound(lang, "RedCrystal.glyph." + k + ".name", k);
				lang = JsonGenerator.appendLangKeyIfNotFound(lang, "RedCrystal.glyph." + k + ".desc", k + "'s description");
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
		JsonGenerator.writeFile(lang, Path.of(getServerResource(), "Languages/en-US/server.lang"), false);
	}

	public static String getServerResource() {

		return "src/main/resources/Server/";
	}

	public static String getCommonResource() {

		return "src/main/resources/Common/";
	}

	public static Path getDevAsset(String subfolder) {
		return Paths.get("src/devAssets/" + subfolder);
	}
}
