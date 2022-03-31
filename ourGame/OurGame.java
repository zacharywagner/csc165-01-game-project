package ourGame;

import tage.*;

public class OurGame extends VariableFrameRateGame {
    private static Engine engine;
    public static Engine getEngine() { return engine; }
    public static void main(String[] args) {
        OurGame game = new OurGame();
        engine = new Engine(game);
        game.initializeSystem();
        game.game_loop();
    }

    @Override
    public void loadShapes() {

    }
    
    @Override
    public void loadTextures() {

    }

    @Override
    public void buildObjects() {

    }

    @Override
    public void initializeGame() {

    }

    @Override
    public void update() {

    }
    
}
