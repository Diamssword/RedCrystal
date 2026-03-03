package com.diamssword.redCrystal.generated;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TextureGenerator {

	public static void main() throws IOException {
		var common = "src/main/resources/Common/";
		var runes = getImages("glyphs");
		System.out.println("starting texture generation...");
		System.out.println("Common is at: " + Paths.get(common).toAbsolutePath());
		runes.forEach((k, v) -> {
			try {
				System.out.println("generating glyph " + k);
				ImageIO.write(v, "png", Paths.get(common, "UI/Custom/Pages/RedCrystal/Glyphs/" + k + "@2x.png").toFile());
				ImageIO.write(stichGlyph(v), "png", Paths.get(common, "Items/RedCrystal/Glyphs/" + k + ".png").toFile());
			} catch(IOException e) {
				e.printStackTrace();
			}
		});
	}

	private static BufferedImage stichGlyph(BufferedImage base) {
		BufferedImage texture = new BufferedImage(
				base.getWidth() * 2,
				base.getHeight() * 2,
				BufferedImage.TYPE_INT_ARGB   // keeps transparency
		);
		var g2d = texture.createGraphics();
		//hytale entity model dosen't allow for transparency
		base = cleanUpTransparency(base);
		g2d.setComposite(AlphaComposite.Src);
		g2d.drawImage(base, 0, 0, null);
		g2d.drawImage(base, 0, base.getHeight(), null);
		g2d.dispose();
		return texture;
	}

	private static BufferedImage cleanUpTransparency(BufferedImage source) {
		BufferedImage target = new BufferedImage(
				source.getWidth(),
				source.getHeight(),
				BufferedImage.TYPE_INT_ARGB
		);

		int width = source.getWidth();
		int height = source.getHeight();

		for(int y = 0; y < height; y++) {
			for(int x = 0; x < width; x++) {

				int argb = source.getRGB(x, y);

				int alpha = (argb >> 24) & 0xFF;
				int rgb = argb & 0x00FFFFFF;

				if(alpha >= 128) {
					alpha = 255; // fully opaque
				} else {
					alpha = 0;   // fully transparent
				}

				int newPixel = (alpha << 24) | rgb;

				target.setRGB(x, y, newPixel);
			}
		}
		return target;
	}

	private static Map<String, BufferedImage> getImages(String subfolder) throws IOException {

		Path path = Paths.get("src/devAssets/" + subfolder);
		var imgs = Files.list(path).filter(p -> !Files.isDirectory(p) && p.getFileName().toString().endsWith(".png"));
		Map<String, BufferedImage> res = new HashMap<>();
		for(Path o : imgs.toList()) {
			try {
				var name = o.getFileName().toString();
				res.put(name.substring(0, name.length() - 4), ImageIO.read(o.toFile()));
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
		return res;
	}
}
