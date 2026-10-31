package legend.game.wmap;

import legend.core.renderer.QueuedModelStandard;
import legend.core.gpu.Bpp;
import legend.core.gte.MV;
import legend.core.renderer.MeshObj;
import legend.core.renderer.QuadBuilder;
import legend.core.renderer.Translucency;
import org.joml.Vector3f;

import static legend.core.GameEngine.RENDERER;

public class MapMarker {
  private static final Vector3f[] colours = {
    new Vector3f(85.0f / 128.0f, 0.0f, 0.0f),
    new Vector3f(0.0f, 0.0f, 85.0f / 128.0f),
    new Vector3f(1.0f, 1.0f, 2.0f),
  };

  private final MeshObj[] sprites;
  private final MV transforms = new MV();
  private float size;
  private final String modernPrefix;
  private final boolean isTranslucent;

  public MapMarker(final String name, final int objArrayLength, final float size, final int uvSize, final int v, final boolean isTranslucent) {
    this.sprites = new MeshObj[objArrayLength];
    this.size = size;
    this.modernPrefix = "MapArrow".equals(name) && objArrayLength == 8 && uvSize == 16 && v == 32 && !isTranslucent ? "world/arrow/" :
      "CoolonPlaceMarker".equals(name) && objArrayLength == 3 && uvSize == 16 && v == 0 && isTranslucent ? "world/destination/" : null;
    this.isTranslucent = isTranslucent;

    for(int i = 0; i < objArrayLength; i++) {
      final QuadBuilder builder = new QuadBuilder(name + " (index " + i +')')
        .bpp(Bpp.BITS_4)
        .clut(640, 496)
        .vramPos(640, 256)
        .size(1.0f, 1.0f)
        .uv(i * uvSize, v)
        .uvSize(uvSize, uvSize);

      if(isTranslucent) {
        builder.translucency(Translucency.B_PLUS_F);
      }

      this.sprites[i] = builder.build();
    }
  }

  public float getSize() {
    return this.size;
  }

  public void setSize(final float size) {
    this.size = size;
  }

  public void render(final int spriteIndex, final int colourIndex, final float x, final float y, final float z) {
    this.transforms.scaling(this.size, this.size, 1.0f);
    this.transforms.transfer.set(x, y, z);
    final QueuedModelStandard modern = this.modernPrefix == null ? null :
      legend.game.textures.ModernIcons.render(this.modernPrefix + spriteIndex, this.transforms, 1, 1);
    if(modern != null) {
      modern.colour(colours[colourIndex]);
      if(this.isTranslucent) {
        modern.translucency(Translucency.B_PLUS_F);
      }
      return;
    }
    RENDERER.queueOrthoModel(this.sprites[spriteIndex], this.transforms, QueuedModelStandard.class)
      .colour(colours[colourIndex]);
  }

  public void delete() {
    for(final MeshObj sprite : this.sprites) {
      if(sprite != null) {
        sprite.delete();
      }
    }
  }
}
