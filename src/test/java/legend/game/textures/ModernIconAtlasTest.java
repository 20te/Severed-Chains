package legend.game.textures;

import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.Rectangle;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

class ModernIconAtlasTest {
  @Test
  void packedArtworkHasValidGeometryAndCoverage() throws Exception {
    final Path directory = Path.of("gfx/ui/modern/packed");
    final var index = JsonParser.parseString(Files.readString(directory.resolve("atlas.json"))).getAsJsonObject();
    final var image = ImageIO.read(directory.resolve("atlas.png").toFile());
    assertEquals(index.get("width").getAsInt(), image.getWidth());
    assertEquals(index.get("height").getAsInt(), image.getHeight());
    assertTrue(image.getWidth() <= 4096 && image.getHeight() <= 4096);
    final var names = new HashSet<String>();
    final var occupied = new ArrayList<Rectangle>();
    for(final var element : index.getAsJsonArray("icons")) {
      final var icon = element.getAsJsonObject();
      final String name = icon.get("name").getAsString();
      assertTrue(names.add(name), name);
      final Rectangle rect = new Rectangle(icon.get("x").getAsInt(), icon.get("y").getAsInt(),
        icon.get("w").getAsInt(), icon.get("h").getAsInt());
      assertTrue(rect.x >= 2 && rect.y >= 2 && rect.x + rect.width < image.getWidth() && rect.y + rect.height < image.getHeight(), name);
      assertTrue(rect.width >= icon.get("logicalWidth").getAsInt() * 8, name);
      assertTrue(rect.height >= icon.get("logicalHeight").getAsInt() * 8, name);
      for(final Rectangle other : occupied) {
        assertFalse(other.intersects(rect), name);
      }
      occupied.add(rect);
      boolean visible = false;
      for(int y = rect.y - 1; y <= rect.y + rect.height; y++) {
        for(int x = rect.x - 1; x <= rect.x + rect.width; x++) {
          final int alpha = image.getRGB(x, y) >>> 24;
          if(rect.contains(x, y)) {
            visible |= alpha > 127;
          } else {
            assertEquals(0, alpha, name + " gutter");
          }
        }
      }
      assertTrue(visible, name);
    }
    assertEquals(391, names.size());
    for(int i = 0; i < 8; i++) assertTrue(names.contains("battle/list-cursor/" + i), "Shared item/addition/spell list cursor");
    for(int i = 0; i <= 46; i++) assertTrue(names.contains("item/" + i));
    assertTrue(names.contains("item/57") && names.contains("item/58"));
    assertFalse(names.contains("item/64"));
    assertFalse(names.contains("menu/35"), "Stock is text");
    assertTrue(names.contains("menu/59") && names.contains("menu/67"), "Reduced-motion scroll frames");
    for(int i = 0; i < 8; i++) assertTrue(names.contains("world/arrow/" + i));
    for(int i = 0; i < 7; i++) assertTrue(names.contains("dialogue/" + i));

    final var aliases = JsonParser.parseString(Files.readString(directory.resolve("aliases.json"))).getAsJsonObject();
    for(int i = 13; i <= 20; i++) assertTrue(aliases.has("menu/" + i));
    for(final String alias : aliases.keySet()) {
      String target = alias;
      final var seen = new HashSet<String>();
      while(aliases.has(target)) {
        assertTrue(seen.add(target), "Cyclic alias: " + alias);
        target = aliases.get(target).getAsString();
      }
      assertTrue(names.contains(target), alias);
    }
  }
}
