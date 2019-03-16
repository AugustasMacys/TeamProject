package main.com.bodyconquest.gamestates;

import com.badlogic.gdx.Gdx;
import main.com.bodyconquest.constants.*;
import main.com.bodyconquest.entities.BasicObject;
import main.com.bodyconquest.entities.Map;
import main.com.bodyconquest.entities.MapObject;
import main.com.bodyconquest.entities.Troops.Bacteria;
import main.com.bodyconquest.entities.Troops.Bases.Base;
import main.com.bodyconquest.entities.Troops.Flu;
import main.com.bodyconquest.entities.Troops.Troop;
import main.com.bodyconquest.entities.Troops.Virus;
import main.com.bodyconquest.entities.abilities.Ability;
import main.com.bodyconquest.entities.projectiles.Projectile;
import main.com.bodyconquest.entities.resources.Resources;
import main.com.bodyconquest.game_logic.BasicTestAI;
import main.com.bodyconquest.game_logic.Game;
import main.com.bodyconquest.game_logic.MultiplayerTestAI;
import main.com.bodyconquest.game_logic.Player;
import main.com.bodyconquest.networking.Server;
import main.com.bodyconquest.networking.ServerSender;
import main.com.bodyconquest.networking.utilities.MessageMaker;
import main.com.bodyconquest.networking.utilities.Serialization;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.CopyOnWriteArrayList;

/** The {@link GameState} where all of the encounter logic takes place. */
public class EncounterState extends GameState {

  /**
   * An enumeration for the different assignments {@link Troop}s can have to determine how Troops
   * move and who/what they attack.
   */

  /**
   * An enumeration for the different lanes {@link Troop}s can be assigned to, to determine how
   * those Troops move.
   */

  /** The map object that holds all information that needs to be known about the map. */
  private Map map;

  // Troop Arrays (Data type and usage is subject to future change)
  /** The list that stores all MapObject currently on the map. */
  private CopyOnWriteArrayList<MapObject> allMapObjects;

  /** The list that stores all the troops belonging to the top player. */
  private CopyOnWriteArrayList<Troop> troopsTop;
  /** The list that stores all the troops belonging to the bottom player. */
  private CopyOnWriteArrayList<Troop> troopsBottom;

  /** The list that stores all the projectiles that belong to the bottom player. */
  private CopyOnWriteArrayList<Projectile> projectilesBottom;
  /** The list that stores all the projectiles that belong to the top player. */
  private CopyOnWriteArrayList<Projectile> projectilesTop;

  /**
   * The communicator object which acts as a place holder for the Server (and possibly in future the
   * Client). This may remain for quick and easy implementation of single player without using a
   * Client/Server.
   */
  private ServerSender serverSender;

  private Base topBase;
  private Base bottomBase;

  private Player topPlayer;
  private Player bottomPlayer;

  private int totalScoreTop;
  private int totalScoreBottom;

  int counter = 0;

  // Move resources in side of player
  private Resources topResources;
  private Resources bottomResources;

  private Organ organ;
  private boolean end;

  /** Constructor. */
  public EncounterState(Game game, Organ organ) {
    super(game);
    this.organ = organ;
    Server server = game.getServer();
    serverSender = server.getServerSender();
    // map = new Map();

    game.startEncounterLogic(this);

    allMapObjects = new CopyOnWriteArrayList<MapObject>();

    topPlayer = game.getPlayerTop();
    bottomPlayer = game.getPlayerBottom();

    // Initialise unit arrays
    troopsBottom = new CopyOnWriteArrayList<Troop>();
    troopsTop = new CopyOnWriteArrayList<Troop>();

    // Create player bases
    //bottomBase = new InfluenzaBase(Lane.ALL, PlayerType.PLAYER_BOTTOM);
    bottomBase = bottomPlayer.getNewBase();
    bottomBase.setPosition(Assets.baseBottomX, Assets.baseBottomY);
    troopsBottom.add(bottomBase);
    allMapObjects.add(bottomBase);

    //topBase = new InfluenzaBase(Lane.ALL, PlayerType.PLAYER_TOP);
    topBase = topPlayer.getNewBase();
    topBase.setPosition(Assets.baseTopX, Assets.baseTopY);
    troopsTop.add(topBase);
    allMapObjects.add(topBase);

    projectilesBottom = new CopyOnWriteArrayList<Projectile>();
    projectilesTop = new CopyOnWriteArrayList<Projectile>();

    totalScoreBottom = bottomPlayer.getScore();
    totalScoreTop = topPlayer.getScore();

    // Maybe make constructors for these in the Player class so they can be modified for each player
    // And the modifications can be kept consistent across Encounters
    bottomResources = new Resources(server, PlayerType.PLAYER_BOTTOM);
    topResources = new Resources(server, PlayerType.PLAYER_TOP);

    bottomResources.start();
    topResources.start();

    end = false;

    if (game.getGameType() == GameType.SINGLE_PLAYER) {
      BasicTestAI ai = new BasicTestAI(this, PlayerType.PLAYER_TOP, topResources);
      ai.start();
    } else {
      MultiplayerTestAI ai = new MultiplayerTestAI(this);
      ai.start();
    }
  }

  /**
   * Check attack interactions between the two troop lists. Initiates any resulting attack sequences
   * caused from troops being eligible to attack another troop.
   *
   * @param troopsP1 First list of troops.
   * @param troopsP2 Second list of troops.
   */
  private void checkAttack(
      CopyOnWriteArrayList<Troop> troopsP1, CopyOnWriteArrayList<Troop> troopsP2) {
    CopyOnWriteArrayList<Troop> deadTroops = new CopyOnWriteArrayList<Troop>();

    for (Troop troop : troopsP1) {
      if (troop.isDead()) {
        deadTroops.add(troop);
        continue;
      }
      troop.checkAttack(troopsP2);
    }

    for (Troop troop : deadTroops) {
      if(troop.getPlayerType() == PlayerType.PLAYER_TOP){
        //System.out.println("Kill Points BP: " + troop.getKillingPoints());
        totalScoreBottom += troop.getKillingPoints();
      }
      else if(troop.getPlayerType() == PlayerType.PLAYER_BOTTOM){
        totalScoreTop += troop.getKillingPoints();
      }
      troopsP1.remove(troop);
      allMapObjects.remove(troop);
    }
  }

  /**
   * Check collision interactions between the projectiles in the given projectile list with the
   * troops in the given troop list. Initiates any resulting attack sequences caused from troops
   * being hit.
   *
   * @param projectiles The list of projectiles to check interactions with.
   * @param enemies The list of troops to check interactions with.
   */
  private void checkProjectiles(
      CopyOnWriteArrayList<Projectile> projectiles, CopyOnWriteArrayList<Troop> enemies) {
    CopyOnWriteArrayList<Projectile> finishedProjectiles = new CopyOnWriteArrayList<Projectile>();
    for (Projectile proj : projectiles) {
      proj.checkHit(enemies);
      if (proj.getRemove()) finishedProjectiles.add(proj);
    }

    for (Projectile proj : finishedProjectiles) {
      projectiles.remove(proj);
      allMapObjects.remove(proj);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void update() {
    counter ++;
   // Timer.startTimer(20);

//    try {
//      Thread.sleep(20);
//    } catch (InterruptedException e) {
//      e.printStackTrace();
//    }

    // ! Important if you do not want to update encounter state, bases health should go to minus
    // because
    // Encounter state is instantiated before encounter screen and it starts getting health before
    // game is started of the base

    //    if(comms.getBottomHealthPercentage() >= 0 && comms.getTopHealthPercentage() >= 0){
    //
    //     System.out.println(comms.getBottomHealthPercentage());


    for (MapObject mo : allMapObjects) mo.update();

    // Update All Units
    checkAttack(troopsTop, troopsBottom);
    checkAttack(troopsBottom, troopsTop);
    checkProjectiles(projectilesTop, troopsBottom);
    checkProjectiles(projectilesBottom, troopsTop);

    // Change this so it only add new objects
    CopyOnWriteArrayList<BasicObject> sentObjects = new CopyOnWriteArrayList<BasicObject>();
    for (MapObject o : allMapObjects) sentObjects.add(o.getBasicObject());

    if (counter == 3){
      String json = "";
      try {
        json = Serialization.serialize(sentObjects);

        serverSender.sendObjectUpdates(json);

        double healthBottom = bottomBase.getHealth();
        double healthBottomMax = bottomBase.getMaxHealth();
        double healthPercentage = (healthBottom / healthBottomMax) * 100.0;
        int healthB = (int) healthPercentage;
        String messageb = MessageMaker.healthUpdate(healthB, PlayerType.PLAYER_BOTTOM);

        double healthTop = topBase.getHealth();
        double healthTopMax = topBase.getMaxHealth();
        double healthPercentageT = (healthTop / healthTopMax) * 100.0;
        int healthT = (int) healthPercentageT;

        String messaget = MessageMaker.healthUpdate(healthT, PlayerType.PLAYER_TOP);

        serverSender.sendMessage(messageb);
        serverSender.sendMessage(messaget);
        String pointsMessage = MessageMaker.pointsMessage(totalScoreTop, totalScoreBottom);
        serverSender.sendMessage(pointsMessage);
        counter = 0;



      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    // TO DO: send this to the client

    // }

  }

  /**
   * Called by player AI's or players to spawn troops.
   *
   * @param unitType The unit/troop to be spawned.
   * @param lane The lane the unit/troop will be assigned to.
   * @param playerType The player the unit/troop will be assigned to.
   */
  public void spawnUnit(UnitType unitType, Lane lane, PlayerType playerType) {
    Troop troop = null;

    // Initialise troop type
    if (unitType.equals(UnitType.BACTERIA)) {
      if(playerType == PlayerType.PLAYER_BOTTOM){
        if(bottomResources.canAfford(Bacteria.LIPIDS_COST,Bacteria.SUGARS_COST,Bacteria.PROTEINS_COST)){
          bottomResources.buy(Bacteria.LIPIDS_COST,Bacteria.SUGARS_COST,Bacteria.PROTEINS_COST);
          troop = new Bacteria(lane, playerType);
        } else{

        }
      } else if(playerType == PlayerType.PLAYER_TOP){
        troop = new Bacteria(lane, playerType);
      }
    } else if (unitType.equals(UnitType.FLU)) {
      if(playerType == PlayerType.PLAYER_BOTTOM){
        if(bottomResources.canAfford(Flu.LIPIDS_COST, Flu.SUGARS_COST, Flu.PROTEINS_COST)){
          bottomResources.buy(Flu.LIPIDS_COST, Flu.SUGARS_COST, Flu.PROTEINS_COST);
          troop = new Flu(this, playerType, lane);
        }else{

        }
      }else if(playerType == PlayerType.PLAYER_TOP){
        troop = new Flu(this, playerType, lane);
      }
    } else if (unitType.equals(UnitType.VIRUS)) {
      if(playerType == PlayerType.PLAYER_BOTTOM){
        if(bottomResources.canAfford(Virus.LIPIDS_COST, Virus.SUGARS_COST, Virus.PROTEINS_COST)){
          bottomResources.buy(Virus.LIPIDS_COST, Virus.SUGARS_COST, Virus.PROTEINS_COST);
          troop = new Virus(lane, playerType);
        }else{

        }
      }else if(playerType == PlayerType.PLAYER_TOP){
        troop = new Virus(lane, playerType);
      }
    }

    // Return if invalid troop, lane or player type is used
    if (troop == null || lane == null || playerType == null) return;

    // Spawn units for bottom player
    if (playerType.equals(PlayerType.PLAYER_BOTTOM)) {
      if (lane == Lane.BOTTOM) {
        troop.setPosition(
            Assets.BP_BOT_LANE_SPAWN_X - (troop.getWidth() / 2.0),
            Assets.BP_BOT_LANE_SPAWN_Y - (troop.getHeight() / 2.0));
      } else if (lane == Lane.MIDDLE) {
        troop.setPosition(
            Assets.BP_MID_LANE_SPAWN_X - (troop.getWidth() / 2.0),
            Assets.BP_MID_LANE_SPAWN_Y - (troop.getHeight() / 2.0));
      } else if (lane == Lane.TOP) {
        troop.setPosition(
            Assets.BP_TOP_LANE_SPAWN_X - (troop.getWidth() / 2.0),
            Assets.BP_TOP_LANE_SPAWN_Y - (troop.getHeight() / 2.0));
      }
      troopsBottom.add(troop);
    }

    // Spawn units for top player
    if (playerType.equals(PlayerType.PLAYER_TOP)) {
      if (lane == Lane.BOTTOM) {
        troop.setPosition(
            Assets.TP_BOT_LANE_SPAWN_X - (troop.getWidth() / 2.0),
            Assets.TP_BOT_LANE_SPAWN_Y - (troop.getHeight() / 2.0));
      } else if (lane == Lane.MIDDLE) {
        troop.setPosition(
            Assets.TP_MID_LANE_SPAWN_X - (troop.getWidth() / 2.0),
            Assets.TP_MID_LANE_SPAWN_Y - (troop.getHeight() / 2.0));
      } else if (lane == Lane.TOP) {
        troop.setPosition(
            Assets.TP_TOP_LANE_SPAWN_X - (troop.getWidth() / 2.0),
            Assets.TP_TOP_LANE_SPAWN_Y - (troop.getHeight() / 2.0));
      }
      troopsTop.add(troop);
    }
    allMapObjects.add(troop);
  }

  /**
   * Called by ranged (projectile using) MapObjects to add their projectile to the list of
   * MapObjects.
   *
   * @param projectile The projectile to be added to the EncounterState/Map.
   * @param playerType The player that the projectile belongs to.
   */
  public void addProjectile(Projectile projectile, PlayerType playerType) {
    if (playerType == null || projectile == null) return;

    if (playerType == PlayerType.PLAYER_BOTTOM) {
      projectilesBottom.add(projectile);
    } else if (playerType == PlayerType.PLAYER_TOP) {
      projectilesTop.add(projectile);
    } else {
      return;
    }
    allMapObjects.add(projectile);
  }

  private void checkPressed() {
    if (Gdx.input.isKeyJustPressed(1)) {
      // activate1();

    }
  }

  private void endGame(PlayerType player) {
    if(player == PlayerType.PLAYER_BOTTOM) {
      totalScoreBottom += organ.getOrganScore();
    } else {
      totalScoreTop += organ.getOrganScore();
    }

    bottomPlayer.setScore(totalScoreBottom);
    topPlayer.setScore(totalScoreTop);

    end = true;

    //game.
  }

  public CopyOnWriteArrayList<Troop> getTroopsTop() {
    return troopsTop;
  }

  public CopyOnWriteArrayList<Troop> getTroopsBottom() {
    return troopsBottom;
  }

  public Resources getBottomResources() {
    return bottomResources;
  }

  public Resources getTopResources() {
    return topResources;
  }

  public void castAbility(AbilityType abilityType, PlayerType playerType, int xDest, int yDest) {
    // Implement functionality
  }

  public void castAbility(AbilityType abilityType, PlayerType playerType, Lane lane) {
    try {
      @SuppressWarnings("unchecked")
      Ability ability =
          (Ability)
              abilityType
                  .getAssociatedClass()
                  .getDeclaredConstructor(PlayerType.class, Lane.class)
                  .newInstance(playerType, lane);
      ability.cast(this);
    } catch (InstantiationException
        | IllegalAccessException
        | NoSuchMethodException
        | InvocationTargetException e) {
      e.printStackTrace();
    }
  }
}