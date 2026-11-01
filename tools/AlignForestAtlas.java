import java.awt.AlphaComposite;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import javax.imageio.ImageIO;

// Offline correction of the recorded ImageGen candidate; game UVs remain unchanged.
class AlignForestAtlas {
  public static void main(String[] args) throws Exception {
    var source = ImageIO.read(Path.of("gfx/battle/forest/arena-candidate.png").toFile());
    if(source.getWidth() != 1254 || source.getHeight() != 1254) throw new IllegalArgumentException("Unexpected candidate dimensions");
    // Remove saturated export fringe pixels, not the natural leaf colours.
    for(int y = 0; y < source.getHeight(); y++) for(int x = 0; x < source.getWidth(); x++) {
      int c = source.getRGB(x,y), r = c >>> 16 & 255, green = c >>> 8 & 255, b = c & 255;
      if((r > 235 && b < 40 && (green > 220 || green < 40)) || (green > 235 && r < 40 && b < 40)) source.setRGB(x,y,0);
    }
    var atlas = new BufferedImage(1024, 1024, BufferedImage.TYPE_INT_ARGB);
    // Source rectangles measured from the generated boundaries, destination in original UV units.
    int[][] regions = {
      {0,0,344,329, 0,0,64,64},
      {349,0,1248,318, 64,0,256,64},
      {0,338,310,650, 0,64,64,128},
      {328,338,670,656, 64,64,128,128},
      {680,338,960,656, 128,64,192,128},
      {964,338,1245,656, 192,64,256,128},
      {329,668,637,958, 64,129,128,192},
      {640,668,947,958, 128,129,192,192},
      {950,668,1254,958, 192,129,256,192},
      {0,967,317,1189, 0,192,64,240},
      {328,968,641,1185, 64,192,128,240},
      {650,966,878,1190, 128,192,176,240}
    };
    var g = atlas.createGraphics();
    g.setComposite(AlphaComposite.Src);
    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
    for(var r : regions) g.drawImage(source, r[4]*4,r[5]*4,r[6]*4,r[7]*4,r[0],r[1],r[2],r[3],null);
    // Keep the reference animation tile; runtime still samples its animated original VRAM.
    var original = ImageIO.read(Path.of("build/forest-baseline/assets/mapped-atlas-original.png").toFile());
    g.drawImage(original,0,512,256,768,0,128,64,192,null);
    g.dispose();
    ImageIO.write(atlas,"png",Path.of("gfx/battle/forest/arena.png").toFile());
  }
}
