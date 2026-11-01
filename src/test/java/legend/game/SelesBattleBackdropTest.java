package legend.game;

import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SelesBattleBackdropTest {
  @Test
  void preservesAspectAndHorizontalContinuity() throws Exception {
    final var image = ImageIO.read(Path.of("gfx/battle/seles/backdrop.png").toFile());
    assertTrue(image.getWidth() >= 256 && image.getHeight() >= 384);
    assertEquals((long)image.getWidth() * 384, (long)image.getHeight() * 256);

    long difference = 0;
    for(int y = 0; y < image.getHeight(); y++) {
      final int left = image.getRGB(0, y);
      final int right = image.getRGB(image.getWidth() - 1, y);
      for(int shift = 0; shift < 24; shift += 8) {
        difference += Math.abs((left >> shift & 255) - (right >> shift & 255));
      }
    }
    // The retail panorama averages 3.07; allow minor generated texture variation.
    assertTrue(difference / (image.getHeight() * 3.0) <= 4.0, "Backdrop repeat edges must remain visually continuous");
  }
}
