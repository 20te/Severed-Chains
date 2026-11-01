package legend.game;

import org.junit.jupiter.api.Test;
import javax.imageio.ImageIO;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

class ForestBattleBackdropTest {
  @Test
  void preservesMappingAndReportsRepeatEdges() throws Exception {
    final var image = ImageIO.read(Path.of("gfx/battle/forest/backdrop.png").toFile());
    assertNotNull(image);
    assertTrue(image.getWidth() >= 1024);
    assertEquals((long)image.getWidth() * 384, (long)image.getHeight() * 256);
    long difference = 0;
    for(int y = 0; y < image.getHeight(); y++) {
      for(int shift = 0; shift < 24; shift += 8) {
        difference += Math.abs((image.getRGB(0, y) >> shift & 255) - (image.getRGB(image.getWidth() - 1, y) >> shift & 255));
      }
    }
    Files.createDirectories(Path.of("build/forest-backdrop"));
    Files.writeString(Path.of("build/forest-backdrop/edge-review.txt"),
      "Mean edge RGB difference: " + difference / (image.getHeight() * 3.0) + "\nVisual runtime review is required; this statistic does not establish seamlessness.\n");
  }
}
