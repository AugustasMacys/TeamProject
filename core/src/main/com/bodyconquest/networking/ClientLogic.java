package main.com.bodyconquest.networking;

import main.com.bodyconquest.constants.Disease;
import main.com.bodyconquest.constants.Organ;
import main.com.bodyconquest.constants.PlayerType;
import main.com.bodyconquest.entities.BasicObject;
import main.com.bodyconquest.game_logic.Communicator;
import main.com.bodyconquest.game_logic.utils.Timer;
import main.com.bodyconquest.networking.utilities.MessageMaker;
import main.com.bodyconquest.networking.utilities.Serialization;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/** The type Client logic. */
public class ClientLogic extends Thread {

  private enum Logic {
    /** Body logic logic. */
    BODY_LOGIC,
    /** Race selection logic logic. */
    RACE_SELECTION_LOGIC,
    /** Encounter logic logic. */
    ENCOUNTER_LOGIC,
    /** Database logic logic. */
    DATABASE_LOGIC
  }

  private Logic currentLogic;

  private ClientReceiver clientReceiver;
  private Communicator communicator;
  private boolean run;

  /**
   * Instantiates a new Client logic.
   *
   * @param clientReceiver the client receiver
   * @param communicator the communicator
   */
  public ClientLogic(ClientReceiver clientReceiver, Communicator communicator) {
    this.clientReceiver = clientReceiver;
    this.communicator = communicator;
    this.run = true;
    currentLogic = null;
  }

  /** Deals with game logic tasks of the incoming messages */
  public void run() {
    while (run) {
      try {
        String message = clientReceiver.receivedMessages.take();

        if (currentLogic == null) {
          System.err.println("[ERROR] No client logic has been set.");
          continue;
        }

        if (message.startsWith(MessageMaker.FIRST_PICKER_HEADER)) {
          PlayerType player;
          int pointer = MessageMaker.FIRST_PICKER_HEADER.length();

          String encodedPlayerType =
              message.substring(pointer, pointer + PlayerType.getEncodedLength());
          player = PlayerType.decode(encodedPlayerType);

          communicator.setPicker(communicator.getPlayerType() == player);
          continue;
        }

        if (currentLogic == Logic.RACE_SELECTION_LOGIC) raceSelectionLogic(message);
        if (currentLogic == Logic.ENCOUNTER_LOGIC) encounterLogic(message);
        if (currentLogic == Logic.BODY_LOGIC) bodyLogic(message);
        if (currentLogic == Logic.DATABASE_LOGIC) databaseLogic(message);

      } catch (IOException | InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

  private void databaseLogic(String message) {
    int pointer;

    if (message.startsWith(MessageMaker.SET_LEADERBOARD_HEADER)) {
      pointer = MessageMaker.SET_LEADERBOARD_HEADER.length();

      message = message.substring(pointer);

      String values[] = message.split(" ");

      HashMap<String, Integer> board = new HashMap<String, Integer>();

      int i = 0;
      String username;
      Integer points;
      while (i < values.length) {
        username = values[i];
        points = Integer.parseInt(values[i + 1]);
        board.put(username, points);
        i += 2;
      }

      communicator.setBoard(board);
      communicator.setBoardIsSet(true);
    } else if (message.startsWith(MessageMaker.REGISTER_HEADER)) {
      pointer = MessageMaker.REGISTER_HEADER.length();

      message = message.substring(pointer + 1);

      // System.out.println(message);

      Integer value = Integer.parseInt(message);

      // successful registering
      if (value == 1) {
        communicator.setRegistered(true);
      } else {
        communicator.setRegistered(false);
      }
      communicator.setRegisteredIsSet(true);
    } else if (message.startsWith(MessageMaker.LOGIN_HEADER)) {
      pointer = MessageMaker.LOGIN_HEADER.length();

      message = message.substring(pointer + 1);

      String values[] = message.split(" ");

      Integer value = Integer.parseInt(values[0]);

      // successful logging in
      if (value == 1) {
        communicator.setLogged(true);
        communicator.setUsername(communicator.getPlayerType(), values[1]);
      } else {
        communicator.setLogged(false);
      }
      communicator.setLoggedIsSet(true);
    }
  }

  private void bodyLogic(String message) {
    int pointer;

    if (message.startsWith(MessageMaker.START_ENCOUNTER_HEADER)) {
      Organ organ;

      pointer = MessageMaker.START_ENCOUNTER_HEADER.length();

      String encodedOrgan = message.substring(pointer, pointer + Organ.getEncodedLength());
      organ = Organ.decode(encodedOrgan);

      communicator.setCurrentOrgan(organ);
      Timer.startTimer(500);
      communicator.setStartEncounter(true);
    } else if (message.startsWith(MessageMaker.SELECTED_ORGAN_HEADER)) {
      Organ organ;
      pointer = MessageMaker.SELECTED_ORGAN_HEADER.length();

      String encodedOrgan = message.substring(pointer, pointer + Organ.getEncodedLength());
      organ = Organ.decode(encodedOrgan);

      communicator.setSelectedOrgan(organ);
    } else if (message.startsWith(MessageMaker.SELECTED_ORGAN_HEADER)) {
      Organ organ;
      pointer = MessageMaker.SELECTED_ORGAN_HEADER.length();

      String encodedOrgan = message.substring(pointer, pointer + Organ.getEncodedLength());
      organ = Organ.decode(encodedOrgan);

      communicator.setSelectedOrgan(organ);
    }
  }

  private void raceSelectionLogic(String message) {
    int pointer;
    if (message.startsWith(MessageMaker.RACE_HEADER)) {
      Disease disease;
      PlayerType player;

      pointer = MessageMaker.RACE_HEADER.length();

      String encodedDisease = message.substring(pointer, pointer + Disease.getEncodedLength());
      disease = Disease.decode(encodedDisease);
      pointer += Disease.getEncodedLength() + 1;

      String encodedPlayerType =
          message.substring(pointer, pointer + PlayerType.getEncodedLength());
      player = PlayerType.decode(encodedPlayerType);

      if (communicator.getPlayerType() != player) communicator.setOpponentDisease(disease);
    } else if (message.startsWith(MessageMaker.FIRST_PICKER_HEADER)) {
      PlayerType firstPicker;

      pointer = MessageMaker.FIRST_PICKER_HEADER.length();

      String encodedPlayerType =
          message.substring(pointer, pointer + PlayerType.getEncodedLength());
      firstPicker = PlayerType.decode(encodedPlayerType);

      communicator.setPicker(firstPicker == communicator.getPlayerType());

    } else if (message.startsWith(MessageMaker.CHOOSE_RACE_HEADER)) {
      PlayerType player;

      pointer = MessageMaker.CHOOSE_RACE_HEADER.length();

      String encodedPlayerType =
          message.substring(pointer, pointer + PlayerType.getEncodedLength());
      player = PlayerType.decode(encodedPlayerType);

      communicator.setPicker(player == communicator.getPlayerType());
    } else if (message.equals(MessageMaker.START_BODY)) {
      communicator.setStartBodyScreen(true);
    } else if (message.startsWith(MessageMaker.USERNAME_)) {
      PlayerType player;
      pointer = MessageMaker.USERNAME_.length();

      String encodedPlayerType =
          message.substring(pointer, pointer + PlayerType.getEncodedLength());
      player = PlayerType.decode(encodedPlayerType);
      pointer += PlayerType.getEncodedLength() + 1;

      String username = message.substring(pointer);

      communicator.setUsername(player, username);
    }
  }

  private void encounterLogic(String message) throws IOException {
    int pointer;
    if (message.startsWith(MessageMaker.OBJECT_UPDATE_HEADER)) {
      pointer = MessageMaker.OBJECT_UPDATE_HEADER.length();
      String json = message.substring(pointer);
      CopyOnWriteArrayList<BasicObject> objects = Serialization.deserialize(json);
      communicator.populateObjectList(objects);

    } else if (message.startsWith(MessageMaker.HEALTH_HEADER)) {
      PlayerType player;
      int health;

      pointer = MessageMaker.HEALTH_HEADER.length();

      String encodedPlayerType =
          message.substring(pointer, pointer + PlayerType.getEncodedLength());
      player = PlayerType.decode(encodedPlayerType);
      pointer += PlayerType.getEncodedLength() + 1;

      String healthString = message.substring(pointer);
      health = Integer.parseInt(healthString);

      if (player == PlayerType.PLAYER_BOTTOM) {
        communicator.setBottomHealthPercentage(health);
      } else if (player == PlayerType.PLAYER_TOP || player == PlayerType.AI) {
        communicator.setTopHealthPercentage(health);
      }
    } else if (message.startsWith(MessageMaker.RESOURCES_HEADER)) {
      // System.out.println("THE MESSAGE: " + message);

      PlayerType player;
      int lipids;
      int sugars;
      int proteins;

      pointer = MessageMaker.RESOURCES_HEADER.length();

      String encodedPlayerType =
          message.substring(pointer, pointer + PlayerType.getEncodedLength());
      player = PlayerType.decode(encodedPlayerType);
      pointer += PlayerType.getEncodedLength() + 1;

      String lipidsString = message.substring(pointer, pointer + MessageMaker.RESOURCE_PADDING);
      lipids = Integer.parseInt(lipidsString);
      pointer += MessageMaker.RESOURCE_PADDING + 1;

      String sugarsString = message.substring(pointer, pointer + MessageMaker.RESOURCE_PADDING);
      sugars = Integer.parseInt(sugarsString);
      pointer += MessageMaker.RESOURCE_PADDING + 1;

      String proteinsString = message.substring(pointer, pointer + MessageMaker.RESOURCE_PADDING);
      proteins = Integer.parseInt(proteinsString);

      // Lots of this doesn't make sense, the client doesn't need to know their opponents resources
      // As it doesn't need to print it or make any decisions based on it
      // Also I dont know if both players are sent both resources? If so they shouldn't
      if (player == PlayerType.PLAYER_BOTTOM) {
        communicator.setLipidsBottom(lipids);
        communicator.setSugarsBottom(sugars);
        communicator.setProteinsBottom(proteins);
      } else {
        communicator.setLipidsTop(lipids);
        communicator.setSugarsTop(sugars);
        communicator.setProteinsTop(proteins);
      }
    } else if (message.startsWith(MessageMaker.POINTS_HEADER)) {
      int topPlayerPoints;
      int bottomPlayerPoints;
      pointer = MessageMaker.POINTS_HEADER.length();

      String topPointsString = message.substring(pointer, pointer + MessageMaker.POINTS_PADDING);
      topPlayerPoints = Integer.parseInt(topPointsString);
      pointer += MessageMaker.POINTS_PADDING + 1;

      String bottomPointsString = message.substring(pointer, pointer + MessageMaker.POINTS_PADDING);
      bottomPlayerPoints = Integer.parseInt(bottomPointsString);

      communicator.setScoreTop(topPlayerPoints);
      communicator.setScoreBottom(bottomPlayerPoints);

    } else if (message.startsWith(MessageMaker.ORGAN_CLAIMED)) {
      pointer = MessageMaker.ORGAN_CLAIMED.length();
      String playerString = message.substring(pointer, pointer + PlayerType.getEncodedLength());
      pointer += PlayerType.getEncodedLength() + 1; // for underscore
      String organString = message.substring(pointer, pointer + Organ.getEncodedLength());

      PlayerType player = PlayerType.decode(playerString);
      Organ organ = Organ.decode(organString);

      if (player == communicator.getPlayerType()) {
        communicator.addOrgan(organ);
      } else {
        communicator.addOponentOrgan(organ);
      }
    }
  }

  /** Sets race selection logic. */
  public void setRaceSelectionLogic() {
    currentLogic = Logic.RACE_SELECTION_LOGIC;
  }

  /** Sets body logic. */
  public void setBodyLogic() {
    currentLogic = Logic.BODY_LOGIC;
  }

  /** Sets encounter logic. */
  public void setEncounterLogic() {
    currentLogic = Logic.ENCOUNTER_LOGIC;
  }

  /** Sets database logic. */
  public void setDatabaseLogic() {
    currentLogic = Logic.DATABASE_LOGIC;
  }

  /** Stop running. */
  public void stopRunning() {
    run = false;
  }
}
