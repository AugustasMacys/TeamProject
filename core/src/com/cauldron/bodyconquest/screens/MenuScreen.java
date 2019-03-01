package com.cauldron.bodyconquest.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.cauldron.bodyconquest.constants.Constants;
import com.cauldron.bodyconquest.constants.GameType;
import com.cauldron.bodyconquest.game_logic.Communicator;
import com.cauldron.bodyconquest.networking.Client;
import com.cauldron.bodyconquest.networking.Server;
import com.cauldron.bodyconquest.rendering.BodyConquest;

import java.io.IOException;
import java.net.SocketException;

public class MenuScreen implements Screen {

  private BodyConquest game;
  private Texture background;
  private Texture title;
  private Texture playButtonMultiplayer;
  private Texture playButtonSinglePlayer;
  private Texture settingsButton;
  private Texture creditsButton;
  private Texture exitButton;
  private Rectangle multiplayerBounds;
  private Rectangle singleplayerBounds;
  private Rectangle settingsBounds;
  private Rectangle creditsBounds;
  private Rectangle exitBounds;

  public static long timeOfServer;

  OrthographicCamera camera;

  private Server server;
  private Client client;

  public MenuScreen(BodyConquest game) {
    this.game = game;
    camera = new OrthographicCamera();
    camera.setToOrtho(false, BodyConquest.V_WIDTH, BodyConquest.V_HEIGHT);

    background = new Texture("core/assets/background_new.png");
    title = new Texture("core/assets/title_new.png");
    playButtonMultiplayer = new Texture("core/assets/multiplayer_new.png");
    playButtonSinglePlayer = new Texture("core/assets/singleplayer_new.png");
    settingsButton = new Texture("core/assets/settings_new.png");
    creditsButton = new Texture("core/assets/credits_new.png");
    exitButton = new Texture("core/assets/exit_new.png");

    singleplayerBounds =
        new Rectangle(
            BodyConquest.V_WIDTH / 2 - playButtonSinglePlayer.getWidth() / 2,
            300,
            playButtonSinglePlayer.getWidth(),
            playButtonSinglePlayer.getHeight());

    multiplayerBounds =
        new Rectangle(
            BodyConquest.V_WIDTH / 2 - playButtonMultiplayer.getWidth() / 2,
            240,
            playButtonMultiplayer.getWidth(),
            playButtonMultiplayer.getHeight());

    settingsBounds =
        new Rectangle(
            BodyConquest.V_WIDTH / 2 - settingsButton.getWidth() / 2,
            180,
            settingsButton.getWidth(),
            settingsButton.getHeight());
    creditsBounds =
        new Rectangle(
            BodyConquest.V_WIDTH / 2 - creditsButton.getWidth() / 2,
            120,
            creditsButton.getWidth(),
            creditsButton.getHeight());
    exitBounds =
        new Rectangle(
            BodyConquest.V_WIDTH / 2 - exitButton.getWidth() / 2,
            60,
            exitButton.getWidth(),
            exitButton.getHeight());
  }

  @Override
  public void show() {}

  @Override
  public void render(float delta) {

    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

    camera.update();
    game.batch.setProjectionMatrix(camera.combined);
    game.batch.begin();
    game.batch.draw(background, 0, 0, BodyConquest.V_WIDTH, BodyConquest.V_HEIGHT);
    game.batch.draw(title, BodyConquest.V_WIDTH / 2 - title.getWidth() / 2, 450);
    game.batch.draw(
        playButtonSinglePlayer,
        BodyConquest.V_WIDTH / 2 - playButtonSinglePlayer.getWidth() / 2,
        300, 100, 100);
    game.batch.draw(
        playButtonMultiplayer,
        BodyConquest.V_WIDTH / 2 - playButtonMultiplayer.getWidth() / 2,
        240);
    game.batch.draw(settingsButton, BodyConquest.V_WIDTH / 2 - settingsButton.getWidth() / 2, 180);
    game.batch.draw(creditsButton, BodyConquest.V_WIDTH / 2 - creditsButton.getWidth() / 2, 120);
    game.batch.draw(exitButton, BodyConquest.V_WIDTH / 2 - exitButton.getWidth() / 2, 60);

    checkPressed();

    game.batch.end();
  }

  public void checkPressed() {

    Vector3 tmp = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
    camera.unproject(tmp);
    if (Gdx.input.justTouched()) {

      if (multiplayerBounds.contains(tmp.x, tmp.y)) {
        playButtonSound();
        System.out.println("Multiplayer Is touched");
        game.setScreen(new HostScreen(game));
        dispose();
      }

      if (singleplayerBounds.contains(tmp.x, tmp.y)) {
        playButtonSound();
        // System.out.println("Singleplayer Is touched");
        server = new Server();
        timeOfServer = System.currentTimeMillis();
        client = new Client();
        Communicator communicator = new Communicator();
        game.setServer(server);
        game.setClient(client);
        game.setScreen(new RaceSelection(game, communicator, GameType.SINGLE_PLAYER));
        dispose();
      }
      if (settingsBounds.contains(tmp.x, tmp.y)) {
        playButtonSound();
        System.out.println("Settings Is touched");
        game.setScreen(new SettingsScreen(game));
        dispose();
      }
      if (creditsBounds.contains(tmp.x, tmp.y)) {
        playButtonSound();
        System.out.println("Credits Is touched");
        game.setScreen(new CreditsScreen(game));
        dispose();
      }

      if (exitBounds.contains(tmp.x, tmp.y)) {
        playButtonSound();
        dispose();
        Gdx.app.exit();
        System.exit(0);
      }
    }
  }

  public Server getServer() {
    return server;
  }

  public Client getClient() {
    return client;
  }

  @Override
  public void resize(int width, int height) {}

  @Override
  public void pause() {}

  @Override
  public void resume() {}

  @Override
  public void hide() {}

  @Override
  public void dispose() {
    background.dispose();
    title.dispose();
    playButtonSinglePlayer.dispose();
    playButtonMultiplayer.dispose();
    settingsButton.dispose();
    creditsButton.dispose();
  }

  public void playButtonSound() {
    game.audioPlayer.playSFX("button_click");
  }
}
