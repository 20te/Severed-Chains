package legend.game;

import legend.game.characters.VitalsStat;
import legend.core.GameEngine;
import legend.game.combat.BattleTransitionMode;
import legend.game.combat.bent.MonsterBattleEntity;
import legend.game.combat.effects.TransformationMode;
import legend.game.scripting.ScriptState;
import legend.game.title.Ttle;
import legend.game.types.GameState52c;
import legend.lodmod.LodMod;
import legend.game.modding.coremod.CoreMod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.Timeout;
import legend.core.platform.input.InputKey;

import java.nio.file.Files;
import java.nio.file.Path;

import static legend.core.GameEngine.CONFIG;
import static legend.game.Scus94491BpeSegment_8006.battleState_8006e398;
import static legend.game.Scus94491BpeSegment_800b.gameState_800babc8;
import static legend.game.TestConfig.*;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.MethodName.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ArenaBaselineTest {

  private static final Logger LOGGER = LogManager.getFormatterLogger(ArenaBaselineTest.class);
  private Thread engine;
  private final int arenaStage = Integer.parseInt(System.getenv().getOrDefault("ARENA_STAGE", "2"));
  private final int arenaEncounter = Integer.parseInt(System.getenv().getOrDefault("ARENA_ENCOUNTER", "477"));

  @BeforeAll
  void bootEngine() throws Exception {
    Assumptions.assumeTrue(
      Files.isDirectory(Path.of("files")),
      "Skipped: game files not unpacked. Place ISOs in isos/ and run the game once."
    );

    LOGGER.info("[E2E] Engine boot starting");
    engine = new Thread(() -> Main.main(new String[0]));
    engine.start();
    Wait.waitFor(() -> !GameEngine.isLoading() && GameEngine.RENDERER.window() != null, 60000, "renderer window ready");
    // The automation desktop may not deliver a native focus event.
    final var focus = org.lwjgl.sdl.SDL_Event.create();
    focus.type(org.lwjgl.sdl.SDLEvents.SDL_EVENT_WINDOW_FOCUS_GAINED);
    focus.window().windowID(org.lwjgl.sdl.SDLVideo.SDL_GetWindowID(((legend.core.platform.SdlWindow)GameEngine.RENDERER.window()).getWindowPtr()));
    org.lwjgl.sdl.SDLEvents.SDL_PushEvent(focus);
    Wait.waitFor(() -> GameEngine.PLATFORM.getLastWindow() != null, 10000, "test input window ready");

    // Disable Discord causes crash when testing
    GameEngine.DISCORD.destroy();

    // Mute all audio BEFORE any key press so FMV/music never plays
    CONFIG.setConfig(CoreMod.MASTER_VOLUME_CONFIG.get(), 0.0f);
    CONFIG.setConfig(CoreMod.MUSIC_VOLUME_CONFIG.get(), 0.0f);
    CONFIG.setConfig(CoreMod.SFX_VOLUME_CONFIG.get(), 0.0f);
    CONFIG.setConfig(CoreMod.FMV_VOLUME_CONFIG.get(), 0.0f);
    LOGGER.info("[E2E] Audio muted");

    // skip the intro splash
    legend.core.DebugHelper.sleep(1000);
    Input.sendKeyPress(InputKey.RETURN);

    Wait.waitForFmvToStart();
    Input.sendKeyPress(InputKey.RETURN);
    Input.sendKeyPress(InputKey.RETURN);
    Wait.waitForFmvToStop();

    Wait.waitForEngineState(Ttle.class);
    Wait.waitFor(() -> {
      try {
        final var ready = Ttle.class.getDeclaredField("copyrightObj");
        ready.setAccessible(true);
        return ready.get(legend.game.EngineStates.currentEngineState_8004dd04) != null;
      } catch (ReflectiveOperationException e) { throw new AssertionError(e); }
    }, 30000, "title assets ready");

    SelesSequenceTest.onRender(Harness::createFreshGameState);

    CONFIG.setConfig(CoreMod.AUTO_TEXT_CONFIG.get(), true);
    CONFIG.setConfig(CoreMod.AUTO_TEXT_DELAY_CONFIG.get(), 0.0f);
    CONFIG.setConfig(CoreMod.BATTLE_TRANSITION_MODE_CONFIG.get(), BattleTransitionMode.INSTANT);
    CONFIG.setConfig(CoreMod.TRANSFORMATION_MODE_CONFIG.get(), TransformationMode.SHORT);
    if(!Boolean.parseBoolean(System.getenv("ARENA_PREVIEW"))) {
      CONFIG.setConfig(CoreMod.ADDITION_MODE_CONFIG.get(), legend.game.combat.AdditionMode.AUTOMATIC);
    }

    Harness.setSimpleRandSeed(RNG_SEED);
    Harness.setMersenneTwisterSeed(MT_SEED);

    LOGGER.info("[E2E] Fresh game state created - gold=%d, party=%d, chapter=%d",
      gameState_800babc8.gold_94,
      gameState_800babc8.charIds_88.size(),
      gameState_800babc8.chapterIndex_98
    );
  }

  @Test
  @Timeout(60)
  void test1_freshStateIsValid() {
    final GameState52c gs = gameState_800babc8;
    assertNotNull(gs, "Game state should exist");
    assertEquals(20, gs.gold_94, "Gold should be 20");
    assertEquals(1, gs.charIds_88.size(), "Party should have 1 character");
    assertEquals(0, gs.chapterIndex_98, "Chapter should be 0");
    LOGGER.info("[E2E] PASS: freshStateIsValid");
  }

  @Test
  void test2_battleStartsAndMonsterHasHp() throws Exception {
    LOGGER.info("[E2E] Starting battle encounter=%d stage=%d", arenaEncounter, arenaStage);
    final Path captures = Path.of(System.getenv().getOrDefault("ARENA_CAPTURE", "build/arena-remastered/runtime"));
    try { Files.createDirectories(captures); } catch (Exception e) { throw new RuntimeException(e); }
    final var frame = new java.util.concurrent.atomic.AtomicInteger();
    GameEngine.RENDERER.window().events().onDraw(() -> {
      if (legend.game.EngineStates.currentEngineState_8004dd04 instanceof legend.game.combat.Battle) {
        int n = frame.getAndIncrement();
        if (!Boolean.parseBoolean(System.getenv("ARENA_PREVIEW")) && n % 5 == 0 && n < 2400) {
          try { SelesSequenceTest.capture(captures.resolve(String.format("frame-%04d.png", n))); }
          catch (Exception e) { throw new RuntimeException(e); }
        }
      }
    });
    SelesSequenceTest.onRender(() -> {
      try {
        final var release = Ttle.class.getDeclaredMethod("deallocate");
        release.setAccessible(true);
        release.invoke(legend.game.EngineStates.currentEngineState_8004dd04);
      } catch (ReflectiveOperationException e) { throw new AssertionError(e); }
      legend.game.combat.SBtld.startLegacyEncounter(arenaEncounter, arenaStage);
      Harness.transitionToEngineState(legend.lodmod.LodEngineStateTypes.BATTLE.get());
    });
    Wait.waitForEngineState(legend.game.combat.Battle.class);
    Wait.waitForPlayerTurn();
    assertEquals(arenaStage, ((legend.game.combat.Battle)legend.game.EngineStates.currentEngineState_8004dd04).currentStage_800c66a4);
    assertEquals(Boolean.parseBoolean(System.getenv().getOrDefault("ARENA_EXPECT_BACKDROP", "true")),
      legend.game.Scus94491BpeSegment.battlePreloadedEntities_1f8003f4.skyboxTexture != null,
      "Stage 2 should use the replacement only when it is valid and available");
    final var stage = legend.game.Scus94491BpeSegment.battlePreloadedEntities_1f8003f4.stage_963c;
    assertEquals(Boolean.parseBoolean(System.getenv().getOrDefault("ARENA_EXPECT_SURFACES", "true")), stage.surfaceTexture != null);
    if(arenaStage == 1 || arenaStage == 2) assertTrue(stage._618[0] != 0, "Original stage texture animation must remain active");
    if(stage.surfacePalette != null) {
      assertEquals(64, stage.surfacePalette.width);
      assertEquals(16, stage.surfacePalette.height);
    }
    LOGGER.info("[E2E] Battle loaded, checking monster state");

    final ScriptState<MonsterBattleEntity> monster = battleState_8006e398.monsterBents_e50[0];
    assertNotNull(monster, "First monster slot should be populated");

    final VitalsStat hp = monster.innerStruct_00.stats.getStat(LodMod.HP_STAT.get());
    assertTrue(hp.getCurrent() > 0, "Monster HP should be > 0");
    assertEquals(hp.getMaxRaw(), hp.getCurrent(), "Monster HP should be full");

    LOGGER.info("[E2E] PASS: battleStartsAndMonsterHasHp - HP=%d/%d", hp.getCurrent(), hp.getMaxRaw());
    if(Boolean.parseBoolean(System.getenv("ARENA_PREVIEW"))) {
      SelesSequenceTest.onRender(() -> {
        GameEngine.RENDERER.window().show();
        org.lwjgl.sdl.SDLVideo.SDL_RaiseWindow(((legend.core.platform.SdlWindow)GameEngine.RENDERER.window()).getWindowPtr());
      });
      LOGGER.info("[PREVIEW] Seles combat ready; close the game window to end the preview");
      Wait.waitFor(() -> !engine.isAlive());
    }
  }

  @Test
  @Timeout(120)
  void test3_attackDamagesMonster() {
    assertNotNull(battleState_8006e398, "Battle state must exist (did test2 pass?)");

    final var monsterSlot = battleState_8006e398.monsterBents_e50[0];
    assertNotNull(monsterSlot, "Monster slot 0 should be populated");

    final VitalsStat hp = monsterSlot.innerStruct_00.stats.getStat(LodMod.HP_STAT.get());
    final int hpBefore = hp.getCurrent();
    LOGGER.info("[E2E] Monster HP before attack = %d / %d", hpBefore, hp.getMaxRaw());

    Wait.waitForPlayerTurn();
    Harness.selectBattleMenuIcon(0);
    Input.sendKeyPress(InputKey.RETURN);
    legend.core.DebugHelper.sleep(500);
    Input.sendKeyPress(InputKey.RETURN);
    legend.core.DebugHelper.sleep(15000);

    Wait.waitForPlayerTurn();

    final int hpAfter = hp.getCurrent();
    assertTrue(hpAfter < hpBefore, "Attack should damage the selected monster");
    LOGGER.info("[E2E] PASS: attackDamagesMonster - HP=%d after attack", hpAfter);
  }

  @Test
  void test4_paletteDarkeningStillAffectsSurfaces() throws Exception {
    final Path output = Path.of(System.getenv().getOrDefault("ARENA_CAPTURE", "build/arena-remastered/runtime"));
    SelesSequenceTest.onRender(() -> {
      try { SelesSequenceTest.capture(output.resolve("palette-normal.png")); }
      catch(Exception e) { throw new RuntimeException(e); }
      legend.game.combat.SEffe.applyScreenDarkening(8);
    });
    legend.core.DebugHelper.sleep(250);
    try {
      SelesSequenceTest.onRender(() -> {
        try { SelesSequenceTest.capture(output.resolve("palette-dark.png")); }
        catch(Exception e) { throw new RuntimeException(e); }
      });
    } finally {
      SelesSequenceTest.onRender(() -> legend.game.combat.SEffe.applyScreenDarkening(16));
    }
    final var normal = javax.imageio.ImageIO.read(output.resolve("palette-normal.png").toFile());
    final var dark = javax.imageio.ImageIO.read(output.resolve("palette-dark.png").toFile());
    long before = 0, after = 0;
    for(int y = normal.getHeight() * 5 / 8; y < normal.getHeight() * 11 / 16; y++) {
      for(int x = normal.getWidth() / 4; x < normal.getWidth() * 2 / 3; x++) {
        for(int shift = 0; shift < 24; shift += 8) {
          before += normal.getRGB(x, y) >> shift & 255;
          after += dark.getRGB(x, y) >> shift & 255;
        }
      }
    }
    assertTrue(after < before * 0.75, "Original palette darkening must darken the remastered ground");
  }

  @Test
  void test5_privateVramCannotLeakIntoArena() throws Exception {
    SelesSequenceTest.onRender(() -> {
      final var stage = legend.game.Scus94491BpeSegment.battlePreloadedEntities_1f8003f4.stage_963c;
      if(stage.surfaceTexture == null) return;
      GameEngine.GPU.useVramTexture();
      final int expected = org.lwjgl.opengl.GL11.glGetInteger(org.lwjgl.opengl.GL11.GL_TEXTURE_BINDING_2D);
      stage.surfaceTexture.use(1);
      final int privateTexture = org.lwjgl.opengl.GL11.glGetInteger(org.lwjgl.opengl.GL11.GL_TEXTURE_BINDING_2D);
      assertNotEquals(expected, privateTexture);
      final var queued = GameEngine.RENDERER.queueModel(stage.dobj2s_00[0].tmd_08.getObj(),
        new legend.core.gte.MV(), legend.core.renderer.QueuedModelBattleTmd.class)
        .texture(stage.surfaceTexture).texture(stage.surfacePalette, 2);
      try {
        final var use = legend.core.renderer.QueuedModel.class.getDeclaredMethod("useTexture");
        use.setAccessible(true);
        use.invoke(queued);
      } catch(ReflectiveOperationException e) { throw new AssertionError(e); }
      org.lwjgl.opengl.GL13.glActiveTexture(org.lwjgl.opengl.GL13.GL_TEXTURE1);
      assertEquals(expected, org.lwjgl.opengl.GL11.glGetInteger(org.lwjgl.opengl.GL11.GL_TEXTURE_BINDING_2D),
        "Arena overrides must restore main VRAM after a character binds private VRAM");
      queued.texture(stage.surfaceTexture, 1);
      try {
        final var use = legend.core.renderer.QueuedModel.class.getDeclaredMethod("useTexture");
        use.setAccessible(true);
        use.invoke(queued);
      } catch(ReflectiveOperationException e) { throw new AssertionError(e); }
      org.lwjgl.opengl.GL13.glActiveTexture(org.lwjgl.opengl.GL13.GL_TEXTURE1);
      assertEquals(privateTexture, org.lwjgl.opengl.GL11.glGetInteger(org.lwjgl.opengl.GL11.GL_TEXTURE_BINDING_2D),
        "Explicit character VRAM overrides must still take precedence");
      GameEngine.GPU.useVramTexture();
    });
  }

  @Test
  void test6_lightingOverridesAndShadowStateArePreserved() throws Exception {
    SelesSequenceTest.onRender(() -> {
      final var battle = (legend.game.combat.Battle)legend.game.EngineStates.currentEngineState_8004dd04;
      final var original = legend.game.combat.environment.Ambiance.stageAmbiance_801134fc[arenaStage];
      final var baseline = legend.game.combat.Battle.deffManager_800c693c.stageAmbiance_4c;
      assertEquals(arenaStage == 1 ? 0.384766f : 0.376953f, original.ambientColour_00.x);
      assertEquals(arenaStage == 1 ? 128 : 255, original._10[0].lightColour_0a.x, "Retail preset must remain untouched");
      assertEquals(arenaStage == 1 ? 0.32f : 0.34f, baseline.ambientColour_00.x);
      for(int i = 0; i < 3; i++) assertEquals(original._10[i].lightDirection_00, baseline._10[i].lightDirection_00);
      final var scripted = new legend.game.combat.environment.StageAmbiance4c().set(original);
      scripted.ambientColour_00.set(0.1f, 0.2f, 0.3f);
      scripted._10[0].lightColour_0a.set(10, 20, 30);
      try {
        battle.applyStageAmbiance(scripted);
        assertEquals(scripted.ambientColour_00, battle._800c6930.colour_00);
        assertEquals(10 / 255.0f, battle.lights_800c692c[0].light_00.r_0c);
      } finally {
        battle.scriptApplyStageAmbiance(null);
      }
      assertEquals(baseline.ambientColour_00, battle._800c6930.colour_00);
      assertEquals((arenaStage == 1 ? 128 : 224) / 255.0f, battle.lights_800c692c[0].light_00.r_0c);

      final var model = battleState_8006e398.playerBents_e40.getFirst().innerStruct_00.model_148;
      final var size = new org.joml.Vector3f(model.shadowSize_10c);
      final var offset = new org.joml.Vector3f(model.shadowOffset_118);
      final int type = model.shadowType_cc;
      final var shadow = legend.game.Scus94491BpeSegment_800b.shadowModel_800bda10;
      assertTrue(shadow.modelParts_00[0].tmd_08.getObj().shouldRender(legend.core.renderer.Translucency.B_MINUS_F, 0),
        "Shadow colour adjustment assumes the existing subtractive mesh");
      legend.game.combat.SEffe.renderBttlShadow(model);
      assertEquals(size.x * (arenaStage == 2 ? 1.08f : 1.0f) / 4, shadow.coord2_14.transforms.scale.x, 0.0001f);
      legend.game.combat.SEffe.renderBttlShadow(model);
      assertEquals(size.z * (arenaStage == 2 ? 0.94f : 1.0f) / 4, shadow.coord2_14.transforms.scale.z, 0.0001f);
      assertEquals(size, model.shadowSize_10c, "Render tuning must not accumulate into scripted size");
      assertEquals(offset, model.shadowOffset_118);
      assertEquals(type, model.shadowType_cc);
      try {
        model.shadowSize_10c.mul(0.7f);
        model.shadowOffset_118.set(5, 2, 9);
        model.shadowType_cc = 3;
        final int attachment = model.modelPartWithShadowIndex_cd;
        try {
          model.modelPartWithShadowIndex_cd = 0;
          legend.game.combat.SEffe.renderBttlShadow(model);
          assertEquals(model.shadowSize_10c.x * (arenaStage == 2 ? 1.08f : 1.0f) / 4, shadow.coord2_14.transforms.scale.x, 0.0001f);
          assertEquals(5 + model.modelParts_00[0].coord2_04.coord.transfer.x, shadow.coord2_14.coord.transfer.x, 0.0001f);
          assertEquals(9 + model.modelParts_00[0].coord2_04.coord.transfer.z, shadow.coord2_14.coord.transfer.z, 0.0001f);
        } finally {
          model.modelPartWithShadowIndex_cd = attachment;
          model.shadowSize_10c.set(size);
          model.shadowOffset_118.set(offset);
          model.shadowType_cc = type;
        }
        battle.currentStage_800c66a4 = 0;
        legend.game.combat.SEffe.renderBttlShadow(model);
        assertEquals(size.x / 4, shadow.coord2_14.transforms.scale.x, 0.0001f);
      } finally {
        battle.currentStage_800c66a4 = arenaStage;
      }
    });
  }

  @AfterAll
  void shutdownEngine() {
    if(engine != null) {
      LOGGER.info("[E2E] Shutting down engine thread...");
      engine.interrupt();
      try {
        engine.join(5000);
        if(engine.isAlive()) {
          LOGGER.warn("[E2E] Engine thread did not terminate within 5000ms and may still be running");
        } else {
          LOGGER.info("[E2E] Engine thread shut down");
        }
      } catch(final InterruptedException e) {
        Thread.currentThread().interrupt();
        LOGGER.warn("[E2E] Interrupted while waiting for engine thread to shut down", e);
      }
    }
  }
}
