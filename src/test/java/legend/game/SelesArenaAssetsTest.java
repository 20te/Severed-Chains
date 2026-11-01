package legend.game;

import legend.core.IoHelper;
import legend.game.types.CContainer;
import legend.game.tim.Tim;
import legend.game.unpacker.FileData;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.TreeMap;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.*;

class SelesArenaAssetsTest {
  @Test
  void inspectRetailMapping() throws Exception {
    final Path root = Path.of("files/SECT/DRGN0.BIN/2499");
    final var model = new CContainer("Seles stage", new FileData(Files.readAllBytes(root.resolve("0/0"))), 10);
    final var tim = new Tim(new FileData(Files.readAllBytes(root.resolve("2"))));
    final var usage = new TreeMap<Integer, Integer>();
    final var report = new StringBuilder();
    for(int part = 0; part < model.tmdPtr_00.tmd.objTable.length; part++) {
      final var palettes = new TreeSet<Integer>();
      final var pages = new TreeSet<Integer>();
      for(final var primitive : model.tmdPtr_00.tmd.objTable[part].primitives_10) {
        if((primitive.header() >>> 24 & 4) == 0) continue;
        for(final var packet : primitive.data()) {
          final int clut = IoHelper.readUShort(packet, 2);
          palettes.add(clut);
          pages.add(IoHelper.readUShort(packet, 6));
          usage.merge(clut, 1, Integer::sum);
          if(part == 6) {
            report.append("polygon part=").append(part).append(" header=").append(Integer.toHexString(primitive.header()))
              .append(" clut=").append(clut).append(" uv0=").append(IoHelper.readUByte(packet, 0)).append(',').append(IoHelper.readUByte(packet, 1)).append('\n');
          }
        }
      }
      report.append("part ").append(part).append(" palettes ").append(palettes).append(" pages ").append(pages).append('\n');
    }
    report.append("animation metadata: ").append(model.ptr_08 != null).append('\n');
    if(model.ptr_08 != null) for(int i = 0; i < 10; i++) report.append(i).append(": ").append(java.util.Arrays.toString(model.ptr_08._00[i])).append('\n');
    report.append("TIM image ").append(tim.getImageRect()).append(" CLUT ").append(tim.getClutRect()).append('\n');
    final Path output = Path.of("build/arena-textures/original");
    Files.createDirectories(output);
    final var data = tim.getImageData();
    final var paletteData = tim.getClutData();
    final var rect = tim.getClutRect();
    assertEquals(java.util.Set.of(15708), java.util.Arrays.stream(model.tmdPtr_00.tmd.objTable[6].primitives_10)
      .flatMap(p -> java.util.Arrays.stream(p.data())).map(p -> IoHelper.readUShort(p, 2)).collect(java.util.stream.Collectors.toSet()));
    for(int index = 0; index < 16; index++) {
      assertEquals(0, paletteData.readUShort((5 * 64 + index) * 2) & 0x8000,
        "Tree cutout must remain opaque even though its primitives enable translucency");
    }
    for(final int clut : usage.keySet()) {
      final int offset = ((clut >>> 6) - rect.y) * rect.w + ((clut & 63) * 16 - rect.x);
      final var image = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
      for(int y = 0; y < 256; y++) for(int x = 0; x < 256; x++) {
        final int packed = data.readUByte(y * 128 + x / 2);
        final int index = packed >>> ((x & 1) * 4) & 15;
        final int color = paletteData.readUShort((offset + index) * 2);
        final int rgb = ((color & 31) * 255 / 31 << 16) | ((color >>> 5 & 31) * 255 / 31 << 8) | ((color >>> 10 & 31) * 255 / 31);
        image.setRGB(x, y, color == 0 ? 0 : 0xff000000 | rgb);
      }
      ImageIO.write(image, "png", output.resolve("palette-" + clut + ".png").toFile());
    }
    assertEquals(9, usage.size());
    assertArrayEquals(new short[] {484, 64, 64, 64, 3, 16, -1}, model.ptr_08._00[0]);
    assertEquals(448, tim.getImageRect().x);
    assertEquals(240, tim.getClutRect().y);
    final var remaster = ImageIO.read(Path.of("gfx/battle/seles/arena.png").toFile());
    assertEquals(remaster.getWidth(), remaster.getHeight());
    assertTrue(remaster.getWidth() >= 1024);
    assertTrue(remaster.getColorModel().hasAlpha());
    assertEquals(0, remaster.getRGB(remaster.getWidth() / 2, remaster.getHeight() - 1) >>> 24);
    assertTrue((remaster.getRGB(remaster.getWidth() / 4, remaster.getHeight() / 8) >>> 24) > 250);
    Files.writeString(output.resolve("mapping.txt"), report + "usage " + usage);
  }
}
