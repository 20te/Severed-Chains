package legend.game.unpacker;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class SharedGameDataTest {
  @TempDir Path temporary;

  @Test
  void normalCheckoutsKeepTheirExistingBehaviour() throws IOException {
    assertFalse(SharedGameData.useExisting(this.temporary, 5));
    SharedGameData.requirePrivate(this.temporary, this.temporary.resolve("script"), true);
  }

  @Test
  void overlayRejectsSharedFilesNewTargetsAndVersionChanges() throws IOException {
    final Path shared = Files.createDirectory(this.temporary.resolve("shared"));
    final Path local = Files.createDirectory(this.temporary.resolve("local"));
    Files.writeString(shared.resolve("version"), "5");
    Files.writeString(local.resolve("version"), "5");
    Files.writeString(shared.resolve("script"), "original");
    Files.copy(shared.resolve("script"), local.resolve("script"));
    final JsonObject data = new JsonObject();
    data.addProperty("schema", 1);
    data.addProperty("sharedRoot", shared.toString());
    data.addProperty("unpackedVersion", 5);
    final JsonArray paths = new JsonArray();
    paths.add("script");
    paths.add("linked");
    data.add("privatePaths", paths);
    Files.writeString(local.resolve(".shared-game-data.json"), data.toString());
    assertTrue(SharedGameData.useExisting(local, 5));
    SharedGameData.requirePrivate(local, local.resolve("script"), true);
    SharedGameData.requirePrivate(local, local.resolve("patches/cache"), false);
    assertThrows(IOException.class, () -> SharedGameData.requirePrivate(local, local.resolve("new"), true));
    assertThrows(IOException.class, () -> SharedGameData.requirePrivate(local, local.resolve("../escape"), false));
    Files.createLink(shared.resolve("linked"), shared.resolve("script"));
    Files.createLink(local.resolve("linked"), shared.resolve("script"));
    assertThrows(IOException.class, () -> SharedGameData.requirePrivate(local, local.resolve("linked"), true));
    Files.writeString(shared.resolve("version"), "6");
    assertThrows(IOException.class, () -> SharedGameData.useExisting(local, 5));
  }
}
