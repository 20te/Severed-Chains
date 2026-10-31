package legend.game.unpacker;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Protects the canonical data from worktree unpacking and script patching. */
public final class SharedGameData {
  private SharedGameData() { }

  private static JsonObject metadata(final Path root) throws IOException {
    final Path marker = root.resolve(".shared-game-data.json");
    if(!Files.exists(marker)) {
      return null;
    }
    try(final var reader = Files.newBufferedReader(marker)) {
      final JsonObject data = JsonParser.parseReader(reader).getAsJsonObject();
      if(data.get("schema").getAsInt() != 1) {
        throw new IOException("Unsupported shared game data schema");
      }
      return data;
    } catch(final RuntimeException e) {
      throw new IOException("Invalid shared game data metadata; rerun tools/share-game-data.ps1", e);
    }
  }

  public static boolean useExisting(final Path root, final int version) throws IOException {
    final JsonObject data = metadata(root);
    if(data == null) {
      return false;
    }
    final Path shared = Path.of(data.get("sharedRoot").getAsString());
    if(data.get("unpackedVersion").getAsInt() != version
      || !Files.readString(root.resolve("version")).trim().equals(Integer.toString(version))
      || !Files.readString(shared.resolve("version")).trim().equals(Integer.toString(version))) {
      throw new IOException("Shared data version changed. Unpack in the canonical checkout and rebuild this worktree's overlay.");
    }
    return true;
  }

  public static void requirePrivate(final Path root, final Path path, final boolean script) throws IOException {
    final JsonObject data = metadata(root);
    if(data == null) {
      return;
    }
    final Path local = root.toAbsolutePath().normalize();
    final Path target = path.toAbsolutePath().normalize();
    if(!target.startsWith(local) || target.equals(local)) {
      throw new IOException("Private game data path escapes files: " + path);
    }
    final String relative = local.relativize(target).toString().replace('\\', '/');
    if(script && !data.getAsJsonArray("privatePaths").contains(new com.google.gson.JsonPrimitive(relative))) {
      throw new IOException("New patch target " + relative + "; rerun tools/share-game-data.ps1 before starting the game");
    }
    final Path shared = Path.of(data.get("sharedRoot").getAsString()).toAbsolutePath().normalize();
    for(Path current = target; current != null && current.startsWith(local); current = current.getParent()) {
      final Path original = shared.resolve(local.relativize(current));
      if(Files.exists(current) && Files.exists(original) && Files.isSameFile(current, original)) {
        throw new IOException("Refusing to modify shared game data: " + path);
      }
    }
  }
}
