package com.cauldron.bodyconquest.game_logic;

import com.cauldron.bodyconquest.gamestates.EncounterState;
import com.cauldron.bodyconquest.gamestates.GameStateManager;
import com.cauldron.bodyconquest.networking.Server;
import com.cauldron.bodyconquest.networking.utilities.Serialization;

public class Game extends Thread {

  // Place holder for server processes
  // This could stay for single player
  public volatile Communicator comms;

  private boolean running;
  private int FPS = 60; // The number of time the game will refresh each second
  private long targetTime = 1000 / FPS;
  private EncounterState encounterState;
  private Server server;

  private GameStateManager gsm;

  public Game(Server server, Communicator communicator) {
    this.server = server;
    comms = communicator;
    encounterState = new EncounterState(comms, server.getServerSender());
  }

  private void init() {
    running = true;
    gsm = new GameStateManager();
    //encounterState = new EncounterState(comms, server.getServerSender());
    gsm.setCurrentGameState(encounterState);
  }

  @Override
  public void run() {

    init();

    long start;
    long elapsed;
    long wait;

    // Game loop
    while (running) {

      start = System.nanoTime();

      update();

      elapsed = System.nanoTime() - start;

      wait = targetTime - elapsed / 1000000;
      if (wait < 0) wait = 5;

      try {
        Thread.sleep(wait);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  private void update() {
    gsm.update();
  }

  public EncounterState getEncounterState() {
    return encounterState;
  }
}
