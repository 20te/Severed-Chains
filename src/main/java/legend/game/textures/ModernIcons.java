package legend.game.textures;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import legend.core.gpu.Bpp;
import legend.core.gpu.Rect4i;
import legend.core.gte.MV;
import legend.core.renderer.Obj;
import legend.core.renderer.QuadBuilder;
import legend.core.renderer.QueuedModelStandard;
import legend.core.renderer.Texture;
import legend.game.types.Renderable58;
import legend.game.types.RenderableMetrics14;
import legend.lodmod.GoodsIcon;
import org.legendofdragoon.modloader.registries.RegistryId;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static legend.game.Menus.uiFile_800bdc3c;
import static legend.core.GameEngine.CONFIG;
import static legend.game.modding.coremod.CoreMod.MODERN_ICONS;

/** Packed replacements share the existing atlas renderer; unknown icons retain their original path. */
public final class ModernIcons {
  private static final Path DIRECTORY = Path.of("gfx/ui/modern/packed");
  private static final Map<String, Sprite> sprites = new HashMap<>();
  private static final Map<String, String> aliases = new HashMap<>();
  private static TextureAtlas atlas;

  private ModernIcons() { }

  public static boolean isEnabled() {
    return CONFIG.getConfig(MODERN_ICONS.get());
  }

  private record Entry(String name, int x, int y, int w, int h, int logicalWidth, int logicalHeight) { }
  private record Index(int width, int height, List<Entry> icons) { }
  private record Sprite(TextureAtlasIcon icon, int vertex) { }

  public static void load() {
    final Gson gson = new Gson();
    final Index index;
    try(var reader = Files.newBufferedReader(DIRECTORY.resolve("atlas.json"));
        var aliasReader = Files.newBufferedReader(DIRECTORY.resolve("aliases.json"))) {
      index = gson.fromJson(reader, Index.class);
      aliases.clear();
      aliases.putAll(gson.fromJson(aliasReader, new TypeToken<Map<String, String>>() { }.getType()));
    } catch(final IOException e) {
      throw new RuntimeException("Failed to load modern icon atlas", e);
    }

    if(atlas != null) {
      atlas.delete();
    }
    sprites.clear();
    final Texture texture = Texture.filteredPng("Modern icons", DIRECTORY.resolve("atlas.png"));
    if(texture.width != index.width || texture.height != index.height) {
      throw new IllegalStateException("Modern icon atlas dimensions do not match its index");
    }
    final QuadBuilder builder = new QuadBuilder("Modern icons");
    for(final Entry entry : index.icons) {
      if(entry.x < 0 || entry.y < 0 || entry.w <= 0 || entry.h <= 0 ||
        entry.x + entry.w > index.width || entry.y + entry.h > index.height) {
        throw new IllegalStateException("Invalid modern icon rectangle: " + entry.name);
      }
      builder.add().bpp(Bpp.BITS_24).posSize(1.0f, 1.0f)
        .uv(entry.x / (float)index.width, entry.y / (float)index.height)
        .uvSize(entry.w / (float)index.width, entry.h / (float)index.height);
    }
    final Map<RegistryId, TextureAtlasIcon> icons = new HashMap<>();
    atlas = new TextureAtlas(texture, builder.build(), icons);
    atlas.setPersistent(true);
    int vertex = 0;
    for(final Entry entry : index.icons) {
      final TextureAtlasIcon icon = new TextureAtlasIcon(atlas, new Rect4i(entry.x, entry.y, entry.w, entry.h), vertex);
      if(sprites.put(entry.name, new Sprite(icon, vertex)) != null) {
        throw new IllegalStateException("Duplicate modern icon: " + entry.name);
      }
      vertex += 4;
    }
    for(final String name : aliases.keySet()) {
      if(resolve(name) == null) {
        throw new IllegalStateException("Missing modern icon alias: " + name);
      }
    }
  }

  private static Sprite resolve(String name) {
    for(int remaining = aliases.size(); aliases.containsKey(name); remaining--) {
      if(remaining == 0) {
        throw new IllegalStateException("Cyclic modern icon alias: " + name);
      }
      name = aliases.get(name);
    }
    return sprites.get(name);
  }

  public static TextureAtlasIcon get(final String name) {
    final Sprite sprite = isEnabled() ? resolve(name) : null;
    return sprite == null ? null : sprite.icon;
  }

  public static TextureAtlasIcon registry(final RegistryId id) {
    return get(registryKey(id));
  }

  public static boolean hasRegistry(final RegistryId id) {
    return resolve(registryKey(id)) != null;
  }

  private static String registryKey(final RegistryId id) {
    return "registry/" + id.modId() + '/' + id.entryId();
  }

  public static TextureAtlasIcon registryOrOriginal(final RegistryId id) {
    final TextureAtlasIcon modern = registry(id);
    return modern != null ? modern : legend.core.GameEngine.getTextureAtlas().getIcon(id);
  }

  /** The caller's transform is unchanged, including for mirrored or rotated geometry. */
  public static QueuedModelStandard render(final String name, final MV transforms, final float width, final float height) {
    final TextureAtlasIcon icon = get(name);
    if(icon == null) {
      return null;
    }
    final MV scaled = new MV(transforms);
    scaled.scale(width, height, 1.0f);
    return icon.render(scaled);
  }

  public static RenderableMetrics14[] menuMetrics(final Renderable58 renderable, final RenderableMetrics14[] original) {
    if(!isEnabled() || original.length == 0 ||
      !renderable.metricsBlacklist.isEmpty() || renderable.metricsCount < original.length || renderable.useOriginalTpage) {
      return original;
    }
    if(original[0] instanceof GoodsIcon.Metrics goods && goods.registryId != null) {
      final Sprite sprite = resolve(registryKey(goods.registryId));
      return sprite == null ? original : new RenderableMetrics14[] {new Metrics(sprite, original[0], original[0], false, renderable.modernIconInset)};
    }
    if(uiFile_800bdc3c == null) {
      return original;
    }
    final String prefix;
    if(renderable.uiType_20 == uiFile_800bdc3c.itemIcons_c6a4()) {
      prefix = "item/";
    } else if(renderable.uiType_20 == uiFile_800bdc3c.uiElements_0000()) {
      prefix = "menu/";
    } else {
      return original;
    }
    String key = prefix + renderable.glyph_04;
    if(renderable.clut_30 != 0) {
      key += '/' + Integer.toString(renderable.clut_30);
    }
    final Sprite sprite = resolve(key);
    if(sprite == null) {
      return original;
    }
    final int glyph = renderable.glyph_04;
    final boolean cursor = prefix.equals("menu/") && (glyph >= 37 && glyph <= 52 || glyph >= 108 && glyph <= 111 || glyph >= 162 && glyph <= 209);
    if(cursor && original.length > 1) {
      // Keep the separate shadow/content blend passes when replacing a composed cursor.
      return new RenderableMetrics14[] {new Metrics(sprite, original[0], original[0], true, 0),
        new Metrics(sprite, original[0], original[1], false, 0)};
    }
    return new RenderableMetrics14[] {new Metrics(sprite, original[0], original[0], cursor, prefix.equals("item/") ? renderable.modernIconInset : 0)};
  }

  private static final class Metrics extends RenderableMetrics14 {
    private final boolean shadow;

    private Metrics(final Sprite sprite, final RenderableMetrics14 geometry, final RenderableMetrics14 material, final boolean shadow, final int inset) {
      super(0, 0, geometry.x_02 + inset, geometry.y_03 + inset, material.clut_04, material.tpage_06,
        geometry.width_08 - inset * 2, geometry.height_0a - inset * 2, 1, 1,
        Math.abs(geometry.widthScale_10), Math.abs(geometry.heightScale_12));
      this.vertexStart = sprite.vertex;
      this.shadow = shadow;
    }

    @Override
    public Obj getObj(final Obj original) {
      return atlas.getObj();
    }

    @Override
    public void useTexture(final QueuedModelStandard model) {
      model.texture(atlas.texture).useTextureAlpha();
      if(this.shadow) {
        model.colour(0.0f, 0.0f, 0.0f);
      }
    }
  }
}
