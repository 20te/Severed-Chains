package legend.lodmod;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import legend.core.gpu.Bpp;
import legend.core.renderer.QueuedModelStandard;
import legend.core.renderer.Texture;
import legend.game.textures.ModernIcons;
import legend.game.inventory.ItemIcon;
import legend.game.textures.TextureAtlasIcon;
import legend.game.types.RenderableMetrics14;
import legend.game.types.UiPart;
import legend.game.types.UiType;
import org.legendofdragoon.modloader.registries.RegistryId;

import java.util.ArrayList;
import java.util.List;

import static legend.core.GameEngine.REGISTRIES;
import static legend.core.GameEngine.getTextureAtlas;

public class GoodsIcon extends ItemIcon {
  public GoodsIcon(final RegistryId id) {
    super(getIconIndex(id));
  }

  private static int getIconIndex(final RegistryId id) {
    getType();
    return idMap.getInt(id);
  }

  @Override
  protected UiType getUiType() {
    return getType();
  }

  private static UiType type;
  private static final Object2IntMap<RegistryId> idMap = new Object2IntOpenHashMap<>();

  public static UiType getType() {
    if(type == null) {
      type = loadType();
    }

    return type;
  }

  static void clear() {
    type = null;
    idMap.clear();
  }

  private static UiType loadType() {
    final List<Metrics> metrics = new ArrayList<>();
    TextureAtlasIcon fallback = null;

    for(final RegistryId id : REGISTRIES.goods) {
      final TextureAtlasIcon icon = getTextureAtlas().getIcon(id);

      if(icon != null) {
        if(fallback == null) {
          fallback = icon;
        }
        final Texture texture = icon.atlas.texture;
        final float textureWidth = texture.width;
        final float textureHeight = texture.height;
        idMap.put(id, metrics.size());
        metrics.add(new Metrics(icon.rect.x / textureWidth, icon.rect.y / textureHeight, 0, 0, 16, 16, icon.rect.w / textureWidth, icon.rect.h / textureHeight, texture, id));
      }
    }

    // New aliases keep the legacy index-zero fallback when modern icons are off.
    if(fallback != null) {
      final Texture texture = fallback.atlas.texture;
      for(final RegistryId id : REGISTRIES.goods) {
        if(!idMap.containsKey(id) && ModernIcons.hasRegistry(id)) {
          idMap.put(id, metrics.size());
          metrics.add(new Metrics(fallback.rect.x / (float)texture.width, fallback.rect.y / (float)texture.height, 0, 0, 16, 16,
            fallback.rect.w / (float)texture.width, fallback.rect.h / (float)texture.height, texture, id));
        }
      }
    }

    final UiPart[] parts = metrics.stream()
      .map(metric -> new UiPart(new Metrics[] {metric}, 1))
      .toArray(UiPart[]::new)
    ;

    return new UiType(parts);
  }

  public static class Metrics extends RenderableMetrics14 {
    private final Texture texture;
    public final RegistryId registryId;

    public Metrics(final float u, final float v, final int x, final int y, final int width, final int height, final float textureWidth, final float textureHeight) {
      this(u, v, x, y, width, height, textureWidth, textureHeight, getTextureAtlas().texture);
    }

    public Metrics(final float u, final float v, final int x, final int y, final int width, final int height, final float textureWidth, final float textureHeight, final Texture texture) {
      this(u, v, x, y, width, height, textureWidth, textureHeight, texture, null);
    }

    private Metrics(final float u, final float v, final int x, final int y, final int width, final int height, final float textureWidth, final float textureHeight, final Texture texture, final RegistryId registryId) {
      super(u, v, x, y, 0, Bpp.BITS_24.ordinal() << 7, width, height, textureWidth, textureHeight);
      this.texture = texture;
      this.registryId = registryId;
    }

    @Override
    public void useTexture(final QueuedModelStandard model) {
      model
        .texture(this.texture)
        .useTextureAlpha()
      ;
    }
  }
}
