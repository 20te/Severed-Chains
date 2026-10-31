package legend.game.submap;

import legend.core.renderer.QueuedModelStandard;
import legend.core.gpu.Bpp;
import legend.core.gte.MV;
import legend.core.renderer.MeshObj;
import legend.core.renderer.Obj;
import legend.core.renderer.QuadBuilder;
import legend.game.textures.ModernIcons;

import static legend.core.GameEngine.RENDERER;

public class MapIndicator {
  private final Obj[] indicators = new MeshObj[3];
  private final MV transforms = new MV();

  private enum IndicatorType {
    PLAYER(0),
    DOOR(1),
    ALERT(2),
    ;

    public final int indicator;

    IndicatorType(final int indicator) {
      this.indicator = indicator;
    }
  }

  private void createPlayerIndicator(final int cX, final int cY) {
    this.indicators[IndicatorType.PLAYER.indicator] = new QuadBuilder("PlayerIndicator")
      .vramPos(960, 256)
      .bpp(Bpp.BITS_4)
      .clut(cX, cY)
      .uv(0, 0)
      .size(16, 16)
      .uvSize(16, 16)
      .build();
  }

  private void createDoorIndicator(final int cX, final int cY) {
    this.indicators[IndicatorType.DOOR.indicator] = (new QuadBuilder("DoorIndicator")
      .vramPos(960, 256)
      .bpp(Bpp.BITS_4)
      .clut(cX, cY)
      .uv(0, 0)
      .size(8, 16)
      .uvSize(8, 15)
      .build());
  }

  private void createAlertIndicator() {
    this.indicators[IndicatorType.ALERT.indicator] = new QuadBuilder("AlertIndicator")
      .vramPos(960, 256)
      .bpp(Bpp.BITS_4)
      .clut(976, 464)
      .uv(0, 0)
      .size(24, 24)
      .uvSize(24, 24)
      .build();
  }

  public void renderPlayerIndicator(final float x, final float y, final float z, final float r, final float g, final float b, final int cX, final int cY, final int uX, final int uY) {
    this.transforms.transfer.set(x, y, z);

    final int state = cX == 976 && cY == 472 ? 2 : cX == 992 && cY == 273 ? 1 : cX == 992 && cY == 272 ? 0 : -1;
    final QueuedModelStandard modern = uY == 96 && uX >= 0 && uX % 16 == 0 && state >= 0 ?
      ModernIcons.render("player/" + uX / 16 + '/' + state, this.transforms, 16, 16) : null;
    if(modern != null) {
      modern.colour(r, g, b);
      return;
    }

    if(this.indicators[IndicatorType.PLAYER.indicator] == null) {
      this.createPlayerIndicator(cX, cY);
    }

    RENDERER.queueOrthoModel(this.indicators[IndicatorType.PLAYER.indicator], this.transforms, QueuedModelStandard.class)
      .colour(r, g, b)
      .clutOverride(cX, cY)
      .uvOffset(uX, uY);
  }

  public void renderDoorIndicator(final float x, final float y, final float z, final float r, final float g, final float b, final int cX, final int cY, final int uX, final int uY) {
    this.transforms.transfer.set(x, y, z);

    final int state = cY - 272;
    final QueuedModelStandard modern = cX == 992 && state >= 0 && state < 3 && uY == 0 ?
      ModernIcons.render("door/" + (uX - 128) + '/' + state, this.transforms, 8, 16) : null;
    if(modern != null) {
      modern.colour(r, g, b);
      return;
    }

    if(this.indicators[IndicatorType.DOOR.indicator] == null) {
      this.createDoorIndicator(cX, cY);
    }

    RENDERER.queueOrthoModel(this.indicators[IndicatorType.DOOR.indicator], this.transforms, QueuedModelStandard.class)
      .colour(r, g, b)
      .clutOverride(cX, cY)
      .uvOffset(uX, uY);
  }

  public void renderAlertIndicator(final float x, final float y, final float z, final int uX, final int uY) {
    this.transforms.transfer.set(x, y, z);

    if(uX == 24 && uY == 64 && ModernIcons.render("alert", this.transforms, 24, 24) != null) {
      return;
    }

    if(this.indicators[IndicatorType.ALERT.indicator] == null) {
      this.createAlertIndicator();
    }

    RENDERER.queueOrthoModel(this.indicators[IndicatorType.ALERT.indicator], this.transforms, QueuedModelStandard.class)
      .uvOffset(uX, uY);
  }

  public void destroy() {
    for(int i = 0; i < this.indicators.length; i++) {
      if(this.indicators[i] != null) {
        this.indicators[i].delete();
        this.indicators[i] = null;
      }
    }
  }
}
