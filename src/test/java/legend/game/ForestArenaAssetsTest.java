package legend.game;

import legend.core.IoHelper;
import legend.game.tim.Tim;
import legend.game.types.CContainer;
import legend.game.unpacker.FileData;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.*;

class ForestArenaAssetsTest {
  @Test
  void inspectOriginalForest() throws Exception {
    final Path root = Path.of("files/SECT/DRGN0.BIN/2498");
    final Path output = Path.of("build/forest-baseline/assets");
    Files.createDirectories(output);
    final var model = new CContainer("Forest", new FileData(Files.readAllBytes(root.resolve("0/0"))), 10);
    final var tim = new Tim(new FileData(Files.readAllBytes(root.resolve("2"))));
    final var palettes = new TreeSet<Integer>();
    final var report = new StringBuilder("TIM image " + tim.getImageRect() + " CLUT " + tim.getClutRect() + "\n");
    final var mcq = new legend.game.types.McqHeader(new FileData(Files.readAllBytes(root.resolve("1"))));
    report.append("MCQ ").append(mcq.screenWidth_14).append('x').append(mcq.screenHeight_16)
      .append(" offset ").append(mcq.screenOffsetX_28).append(',').append(mcq.screenOffsetY_2a).append('\n');
    final var backdrop = new BufferedImage(mcq.screenWidth_14, mcq.screenHeight_16, BufferedImage.TYPE_INT_RGB);
    int cx = mcq.clutX_0c, cy = mcq.clutY_0e, u = mcq.u_10 * 4 & 0xfc, v = mcq.v_12, page = mcq.u_10 & 0x3c0;
    for(int x = 0; x < mcq.screenWidth_14; x += 16) for(int y = 0; y < mcq.screenHeight_16; y += 16) {
      for(int dy = 0; dy < 16; dy++) for(int dx = 0; dx < 16; dx++) {
        final int packed = mcq.imageData.readUShort(((v + dy) * mcq.vramWidth_08 + page + (u + dx) / 4) * 2);
        final int index = packed >>> ((u + dx & 3) * 4) & 15;
        final int c = mcq.imageData.readUShort((cy * mcq.vramWidth_08 + cx + index) * 2);
        backdrop.setRGB(x + dx, y + dy, (c & 31) * 255 / 31 << 16 | (c >>> 5 & 31) * 255 / 31 << 8 | (c >>> 10 & 31) * 255 / 31);
      }
      v = v + 16 & 0xf0;
      if(v == 0) { u = u + 16 & 0xfc; if(u == 0) page += 64; }
      cy = cy + 1 & 0xff;
      if(cy == 0) cx += 16;
    }
    ImageIO.write(backdrop, "png", output.resolve("backdrop-original.png").toFile());
    for(int part = 0; part < model.tmdPtr_00.tmd.objTable.length; part++) {
      for(final var primitive : model.tmdPtr_00.tmd.objTable[part].primitives_10) {
        final int command = primitive.header() >>> 24;
        if((command & 4) == 0) continue;
        for(final var packet : primitive.data()) {
          final int clut = IoHelper.readUShort(packet, 2);
          palettes.add(clut);
          report.append("part=").append(part).append(" translucent=").append((command & 2) != 0)
            .append(" clut=").append(clut).append(" tpage=").append(IoHelper.readUShort(packet, 6)).append(" uv=");
          for(int vertex = 0; vertex < ((command & 8) != 0 ? 4 : 3); vertex++) {
            report.append(IoHelper.readUByte(packet, vertex * 4)).append(',').append(IoHelper.readUByte(packet, vertex * 4 + 1)).append(' ');
          }
          report.append('\n');
        }
      }
    }
    if(model.ptr_08 != null) for(int i = 0; i < 10; i++) {
      report.append("animation ").append(i).append(": ").append(java.util.Arrays.toString(model.ptr_08._00[i])).append('\n');
    }
    final var rect = tim.getClutRect();
    final var data = tim.getImageData();
    final var colors = tim.getClutData();
    assertEquals(64, tim.getImageRect().w);
    assertEquals(256, tim.getImageRect().h);
    assertEquals(448, rect.x);
    assertEquals(240, rect.y);
    assertEquals(64, rect.w);
    assertEquals(16, rect.h);
    assertArrayEquals(new short[] {448,128,64,64,5,2,-1}, model.ptr_08._00[0]);
    final var mappedAtlas = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
    final int[][] tilePalettes = {{15388,15452,15452,15516}, {15452,15580,15644,15708}, {15772,15836,15836,15836}, {15900,15964,16028,16028}};
    for(final int clut : palettes) {
      final int offset = ((clut >>> 6) - rect.y) * rect.w + ((clut & 63) * 16 - rect.x);
      report.append("palette ").append(clut).append(" STP indices:");
      for(int i = 0; i < 16; i++) if((colors.readUShort((offset + i) * 2) & 0x8000) != 0) report.append(' ').append(i);
      report.append('\n');
      final var image = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
      for(int y = 0; y < 256; y++) for(int x = 0; x < 256; x++) {
        final int index = data.readUByte(y * 128 + x / 2) >>> ((x & 1) * 4) & 15;
        final int c = colors.readUShort((offset + index) * 2);
        image.setRGB(x, y, c == 0 ? 0 : 0xff000000 | (c & 31) * 255 / 31 << 16 | (c >>> 5 & 31) * 255 / 31 << 8 | (c >>> 10 & 31) * 255 / 31);
        if(clut == tilePalettes[y / 64][x / 64] && y < 240) mappedAtlas.setRGB(x, y, image.getRGB(x, y));
      }
      ImageIO.write(image, "png", output.resolve("palette-" + clut + ".png").toFile());
    }
    ImageIO.write(mappedAtlas, "png", output.resolve("mapped-atlas-original.png").toFile());
    for(int cut = 5; cut <= 7; cut++) assertEquals(1, legend.game.submap.RetailSubmap.encounterData_800f64c4[cut].stage_03);
    assertEquals("assassin_cock_assassin_cock", legend.lodmod.LodEncounters.LEGACY[7]);
    Files.writeString(output.resolve("mapping.txt"), report);
  }

  @Test
  void authoredAtlasHasOpaqueGroundAndTransparentGaps() throws Exception {
    final var image = ImageIO.read(Path.of("gfx/battle/forest/arena.png").toFile());
    assertTrue(image.getColorModel().hasAlpha());
    assertEquals(image.getWidth(), image.getHeight());
    assertTrue(image.getWidth() >= 1024);
    for(final int[] tile : new int[][] {{64,64,128,128},{128,64,192,128},{192,64,256,128},{64,192,128,240}}) {
      for(int y = tile[1]; y < tile[3]; y++) for(int x = tile[0]; x < tile[2]; x++) {
        final int alpha = image.getRGB((int)((x + 0.5) * image.getWidth() / 256), (int)((y + 0.5) * image.getHeight() / 256)) >>> 24;
        assertTrue(alpha >= 128, "Ground cutout at UV " + x + "," + y);
      }
    }
    assertEquals(0, image.getRGB(image.getWidth() * 7 / 8, image.getHeight() * 7 / 8) >>> 24);
  }
}
