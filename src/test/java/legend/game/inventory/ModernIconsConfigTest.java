package legend.game.inventory;

import legend.game.saves.ConfigCategory;
import legend.game.saves.ConfigCollection;
import legend.game.saves.ConfigStorageLocation;
import org.junit.jupiter.api.Test;
import org.legendofdragoon.modloader.registries.RegistryId;

import static org.junit.jupiter.api.Assertions.*;

class ModernIconsConfigTest {
  private final ModernIconsConfigEntry modern = new ModernIconsConfigEntry() {
    @Override
    public RegistryId getRegistryId() {
      return new RegistryId("lod_core", "modern_icons");
    }
  };

  private final IconSetConfigEntry iconSet = new IconSetConfigEntry() {
    @Override
    public RegistryId getRegistryId() {
      return new RegistryId("lod_core", "icon_set");
    }
  };

  @Test
  void oldCampaignsDefaultOnAndTheValueRoundTrips() {
    assertTrue(this.modern.getDefaultValue());
    assertEquals(ConfigStorageLocation.CAMPAIGN, this.modern.storageLocation);
    assertEquals(ConfigCategory.USER_INTERFACE, this.modern.category);
    assertTrue(this.modern.hasEditControl());
    assertTrue(this.modern.hasHelp());
    assertTrue(new ConfigCollection(false).getConfig(this.modern));
    for(final boolean value : new boolean[] {false, true}) {
      assertEquals(value, this.modern.deserializer.apply(this.modern.serializer.apply(value)));
    }
    assertTrue(this.modern.deserializer.apply(new byte[0]));
  }

  @Test
  void switchingRetainsBothExistingIconSetPreferences() {
    final ConfigCollection config = new ConfigCollection(false);
    for(final IconSet preference : IconSet.values()) {
      config.setConfig(this.iconSet, preference);
      config.setConfig(this.modern, false);
      assertFalse(config.getConfig(this.modern));
      assertEquals(preference, config.getConfig(this.iconSet));
      config.setConfig(this.modern, true);
      assertTrue(config.getConfig(this.modern));
      assertEquals(preference, config.getConfig(this.iconSet));
    }
  }

  @Test
  void campaignCopiesAndClearsDoNotNeedChangeCallbacks() {
    final ConfigCollection saved = new ConfigCollection(false);
    saved.setConfig(this.modern, false);
    saved.setConfig(this.iconSet, IconSet.RETAIL);
    final ConfigCollection active = new ConfigCollection(false);
    active.copyConfigFrom(saved);
    assertFalse(active.getConfig(this.modern));
    assertEquals(IconSet.RETAIL, active.getConfig(this.iconSet));
    active.clearConfig();
    assertTrue(active.getConfig(this.modern));
  }
}
