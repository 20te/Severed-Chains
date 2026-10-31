package legend.game.inventory;

import legend.game.saves.BoolConfigEntry;
import legend.game.saves.ConfigCategory;
import legend.game.saves.ConfigStorageLocation;

public class ModernIconsConfigEntry extends BoolConfigEntry {
  public ModernIconsConfigEntry() {
    super(true, ConfigStorageLocation.CAMPAIGN, ConfigCategory.USER_INTERFACE);
  }

  @Override
  public boolean hasHelp() {
    return true;
  }
}
