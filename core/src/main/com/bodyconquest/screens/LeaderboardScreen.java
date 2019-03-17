package main.com.bodyconquest.screens;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import main.com.bodyconquest.constants.Assets;
import main.com.bodyconquest.rendering.BodyConquest;
import main.com.bodyconquest.screens.AbstractGameScreen;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static java.util.Map.Entry.comparingByValue;
import static java.util.stream.Collectors.toMap;

public class LeaderboardScreen extends AbstractGameScreen implements Screen {


    private HashMap<String,Integer> leaderboard;
    private LinkedHashMap sorted;
    private Texture header;
    private int place;
    private Table table;



    /**
     * Instantiates a new Leaderboard Screen.
     *
     * @param game the game
     */
    public LeaderboardScreen(BodyConquest game) {
        super(game);
        leaderboard = new HashMap<>();
        leaderboard.put("Alexandru",20);
        leaderboard.put("Augustas",16);
        leaderboard.put("Brandon",30);
        leaderboard.put("Gintare",15);
        leaderboard.put("Paul",14);
        leaderboard.put("Anton",14);
        leaderboard.put("Speed",13);
        leaderboard.put("Tim",12);
        leaderboard.put("Jack",11);
        leaderboard.put("Rose",10);
        loadAssets();
        getAssets();
    }

    @Override
    public void render(float delta) {
        super.render(delta);
        sortLeaderboard();
        game.batch.begin();
        game.batch.draw(header,BodyConquest.V_WIDTH / 2.0f - header.getWidth() / 2.0f, 450);
        game.usernameFont.getData().setScale(1.0f, 1.0f);
        for(Object s: sorted.keySet()){
            place++;
            switch (place){
                case 1:
                    game.usernameFont.draw(game.batch, Integer.toString(place) + ".  \t " + s + "  " + Integer.toString(leaderboard.get(s)),BodyConquest.V_WIDTH / 2.0f - 60.0f,400.0f);
                    break;
                case 2:
                    game.usernameFont.draw(game.batch, Integer.toString(place) + ".  \t " + s + "  " + Integer.toString(leaderboard.get(s)),BodyConquest.V_WIDTH / 2.0f - 60.0f,360.0f);
                    break;
                case 3:
                    game.usernameFont.draw(game.batch, Integer.toString(place) + ".  \t " + s + "  " + Integer.toString(leaderboard.get(s)),BodyConquest.V_WIDTH / 2.0f - 60.0f,320.0f );
                    break;
                case 4:
                    game.usernameFont.draw(game.batch, Integer.toString(place) + ".  \t " + s + "  " + Integer.toString(leaderboard.get(s)),BodyConquest.V_WIDTH / 2.0f - 60.0f,280.0f );
                    break;
                case 5:
                    game.usernameFont.draw(game.batch, Integer.toString(place) + ".  \t " + s + "  " + Integer.toString(leaderboard.get(s)),BodyConquest.V_WIDTH / 2.0f - 60.0f,240.0f );
                    break;
                case 6:
                    game.usernameFont.draw(game.batch, Integer.toString(place) + ".  \t " + s + "  " + Integer.toString(leaderboard.get(s)),BodyConquest.V_WIDTH / 2.0f - 60.0f,200.0f );
                    break;
                case 7:
                    game.usernameFont.draw(game.batch, Integer.toString(place) + ".  \t " + s + "  " + Integer.toString(leaderboard.get(s)),BodyConquest.V_WIDTH / 2.0f - 60.0f,160.0f );
                    break;
                case 8:
                    game.usernameFont.draw(game.batch, Integer.toString(place) + ".  \t " + s + "  " + Integer.toString(leaderboard.get(s)),BodyConquest.V_WIDTH / 2.0f - 60.0f,120.0f );
                    break;
                case 9:
                    game.usernameFont.draw(game.batch, Integer.toString(place) + ".  \t " + s + "  " + Integer.toString(leaderboard.get(s)),BodyConquest.V_WIDTH / 2.0f - 60.0f,80.0f );
                    break;
                case 10:
                    game.usernameFont.draw(game.batch, Integer.toString(place) + ".\t " + s + "  " + Integer.toString(leaderboard.get(s)),BodyConquest.V_WIDTH / 2.0f - 60.0f,40.0f );
                    break;
            }

        }
        game.batch.end();
        place = 0;
    }

    @Override
    public void loadAssets() {
        super.loadAssets();
        manager.load(Assets.headerLeaderboard,Texture.class);
        manager.finishLoading();
    }

    @Override
    public void getAssets() {
        super.getAssets();
        header = manager.get(Assets.headerLeaderboard,Texture.class);
    }

    public HashMap<String,Integer> getLeaderboard(){
        return leaderboard;
    }


    public void receiveLeaderboard(HashMap<String,Integer> leaderboard){
        this.leaderboard = leaderboard;
    }

    public void sortLeaderboard(){
        this.sorted = leaderboard
                .entrySet()
                .stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .collect(
                        toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2,
                                LinkedHashMap::new));
    }
}
