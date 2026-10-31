import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;

/** Offline raster baking, using only the JDK. Creative source art is kept unchanged. */
class Pack {
  record Icon(String name, BufferedImage image, int width, int height, int x, int y) { }

  public static void main(String[] args) throws Exception {
    Path plan = Path.of(args[0]);
    Map<String, BufferedImage> sources = new HashMap<>();
    List<Icon> icons = new ArrayList<>();
    int x = 2, y = 2, row = 0;
    for (String line : Files.readAllLines(plan)) {
      String[] f = line.split("\t", -1);
      int[] n = new int[11];
      for (int i = 0; i < 11; i++) n[i] = Integer.parseInt(f[i + 2]);
      BufferedImage full = sources.get(f[1]);
      if (full == null) {
        full = ImageIO.read(Path.of(f[1]).toFile());
        if (full == null) throw new IllegalArgumentException("Invalid source: " + f[1]);
        sources.put(f[1], full);
      }
      BufferedImage source = n[0] < 0 ? full : full.getSubimage(n[0], n[1], n[2], n[3]);
      int rotation = n[10];
      if (rotation != 0) {
        boolean swap = rotation % 180 != 0;
        BufferedImage turned = new BufferedImage(swap ? source.getHeight() : source.getWidth(),
            swap ? source.getWidth() : source.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = turned.createGraphics();
        g.translate(turned.getWidth() / 2.0, turned.getHeight() / 2.0);
        g.rotate(Math.toRadians(rotation));
        g.translate(-source.getWidth() / 2.0, -source.getHeight() / 2.0);
        g.drawImage(source, 0, 0, null);
        g.dispose();
        source = turned;
      }
      int left = source.getWidth(), top = source.getHeight(), right = -1, bottom = -1;
      for (int sy = 0; sy < source.getHeight(); sy++) for (int sx = 0; sx < source.getWidth(); sx++) {
        if ((source.getRGB(sx, sy) >>> 24) > 8) {
          left = Math.min(left, sx); top = Math.min(top, sy);
          right = Math.max(right, sx); bottom = Math.max(bottom, sy);
        }
      }
      if (right < left) throw new IllegalArgumentException("Empty artwork: " + f[0]);
      int width = Integer.parseInt(f[14]), height = Integer.parseInt(f[15]);
      BufferedImage baked = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
      Graphics2D g = baked.createGraphics();
      g.setComposite(AlphaComposite.Src);
      g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
      g.drawImage(source, n[6] * width / n[4], n[7] * height / n[5], n[8] * width / n[4],
          n[9] * height / n[5], left, top, right + 1, bottom + 1, null);
      g.dispose();
      List<Integer> palette = new ArrayList<>();
      if (!f[13].isEmpty()) for (String word : f[13].split(",")) {
        int c = Integer.parseInt(word);
        palette.add((c & 31) * 255 / 31 << 16 | (c >> 5 & 31) * 255 / 31 << 8 | (c >> 10 & 31) * 255 / 31);
      }
      palette.sort(Comparator.comparingDouble(Pack::luminance));
      for (int sy = 0; sy < height; sy++) for (int sx = 0; sx < width; sx++) {
        int argb = baked.getRGB(sx, sy);
        if ((argb >>> 24) <= 4) { baked.setRGB(sx, sy, 0); continue; }
        if (!palette.isEmpty()) {
          // Recolour saturated masters without treating red/blue as dark grey.
          double value = Math.max(argb >> 16 & 255, Math.max(argb >> 8 & 255, argb & 255));
          double pos = value / 255 * (palette.size() - 1);
          int a = palette.get((int)pos), b = palette.get(Math.min(palette.size() - 1, (int)pos + 1));
          double t = pos - (int)pos;
          int colour = 0;
          for (int shift : new int[]{0, 8, 16}) colour |= (int)(((a >> shift & 255) * (1-t)) + ((b >> shift & 255) * t)) << shift;
          baked.setRGB(sx, sy, argb & 0xff000000 | colour);
        }
      }
      if (x + width + 2 > 4096) { x = 2; y += row + 4; row = 0; }
      icons.add(new Icon(f[0], baked, n[4], n[5], x, y));
      x += width + 4; row = Math.max(row, height);
    }
    int height = Integer.highestOneBit(y + row + 3) << 1;
    if (height > 4096) throw new IllegalStateException("Atlas exceeds4096px: " + height);
    BufferedImage atlas = new BufferedImage(4096, height, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g = atlas.createGraphics();
    g.setComposite(AlphaComposite.Src);
    StringBuilder index = new StringBuilder("{\n  \"width\":4096,\n  \"height\":" + height + ",\n  \"icons\":[\n");
    for (int i = 0; i < icons.size(); i++) {
      Icon icon = icons.get(i);
      g.drawImage(icon.image, icon.x, icon.y, null);
      if (i != 0) index.append(",\n");
      index.append("    {\"name\":\"").append(icon.name).append("\",\"x\":").append(icon.x)
          .append(",\"y\":").append(icon.y).append(",\"w\":").append(icon.image.getWidth())
          .append(",\"h\":").append(icon.image.getHeight()).append(",\"logicalWidth\":").append(icon.width)
          .append(",\"logicalHeight\":").append(icon.height).append('}');
    }
    g.dispose();
    index.append("\n  ]\n}\n");
    Files.writeString(plan.resolveSibling("atlas.json"), index);
    ImageIO.write(atlas, "png", plan.resolveSibling("atlas.png").toFile());
    System.out.println("Baked " + icons.size() + " icons into4096x" + height + " atlas.");
  }

  static double luminance(int rgb) {
    return (rgb >> 16 & 255) * .2126 + (rgb >> 8 & 255) * .7152 + (rgb & 255) * .0722;
  }
}
