package com.xombified23.pandagame.desktop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

public class GameScreen implements Screen {
    // General
    // private final MyGame game;
    private Stage stage;
    private OrthographicCamera camera;

    // Actors
    private MainTileActor[][] mainTileActorMap;
    private FloorActor[][] floorActorMap;
    private MonsterActor[][] monsterActorMap;
    private PlayerActor playerActor;
    private BackgroundActor backgroundActor;

    // Textures
    private TextureAtlas floorAtlas;
    private TextureAtlas playerAtlas;
    private Texture backTexture;
    private Texture fogTexture;
    private Texture monsterTexture;

    // Others
    private FPSLogger fpsLogger;
    private Table UImainTable;
    private Group gameAreaGroup;

    // Double array to handle shortest path
    private int[][] mapSteps;

    public GameScreen(final MainGame game) {
        // Assign Game and SpriteBatch object
        // this.game = game;

        // Instantiate New Objects
        gameAreaGroup = new Group();
        UImainTable = new Table();
        stage = new Stage();
        fpsLogger = new FPSLogger();
        camera = new OrthographicCamera();
        floorAtlas = new TextureAtlas(Gdx.files.internal("jei/PurpleTiles/PurpleTiles.atlas"));
        playerAtlas = new TextureAtlas(Gdx.files.internal("jei/Warrior/Warrior_all/Atlas/Hero_Atlas.atlas"));
        fogTexture = new Texture(Gdx.files.internal("others/blacktile.png"));
        backTexture = new Texture(Gdx.files.internal("others/background.png"));
        monsterTexture = new Texture(Gdx.files.internal("others/playerSprite.png"));
        mapSteps = new int[Parameters.NUM_X_TILES][Parameters.NUM_Y_TILES];
    }

    @Override
    public void dispose() {
        backTexture.dispose();
        playerAtlas.dispose();
        floorAtlas.dispose();
        fogTexture.dispose();
        monsterTexture.dispose();
        stage.dispose();
    }

    @Override
    public void render(float delta) {
        camera.update();
        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();

        // This line causes memory leak!
        // Table.drawDebug(stage);

        if (playerActor.getActions().size == 0) {
            revealAround();
        }

        // fpsLogger.log();
    }

    @Override
    public void resize(int width, int height) {
        camera.viewportWidth = width;
        camera.viewportHeight = height;

        camera.position.set(width / 2f, height / 2f, 0);
    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void show() {
        // Setup camera to align Stage with Tiled Map
        stage.getViewport().setCamera(camera);

        // Add input interface to the Stage
        Gdx.input.setInputProcessor(stage);

        backgroundActor = new BackgroundActor(backTexture);
        // gameAreaGroup.addActor(backgroundActor);

        // Inflate rest of Actors
        createFloorTilesActors();
        createMainTilesActors();

        // table.add(group);
        spawnPlayer();
        revealAround();
        spawnMonsters(Parameters.NUM_MONSTERS);

        // Add Stage Touch
        addStageTouch();

        // Add the group of actors to the stage
        // stage.addActor(gameAreaGroup);

        // TODO: UI Placeholder
        UImainTable.debug();
        UImainTable.setBounds(0, 0, Parameters.SCREEN_WIDTH, Parameters.SCREEN_HEIGHT);
        UImainTable.add(gameAreaGroup).expand().left().bottom();
        UImainTable.row();
        // UImainTable.add(new PlayerActor(0, 0, playerAtlas)).expandX().left().height(420);
        stage.addActor(UImainTable);

    }

    @Override
    public void pause() {
        // Irrelevant on desktop, ignore this
    }

    @Override
    public void resume() {
        // Irrelevant on desktop, ignore this
    }

    /**
     * Create Actors for Tiled Map
     */
    private void createMainTilesActors() {
        mainTileActorMap = new MainTileActor[Parameters.NUM_X_TILES][Parameters.NUM_Y_TILES];

        for (int j = 0; j < Parameters.NUM_Y_TILES; j++) {
            for (int i = 0; i < Parameters.NUM_X_TILES; i++) {
                mainTileActorMap[i][j] = new MainTileActor(i, j, fogTexture);
                // stage.addActor(mainTileActorMap[i][j]);
                gameAreaGroup.addActor(mainTileActorMap[i][j]);
            }
        }
    }

    /**
     * Create Floor UI Tiles and store them in an array
     */
    private void createFloorTilesActors() {
        if (floorAtlas == null) {
            throw new Error();
        }

        floorActorMap = new FloorActor[Parameters.NUM_X_TILES][Parameters.NUM_Y_TILES];

        for (int j = 0; j < Parameters.NUM_Y_TILES; j++) {
            for (int i = 0; i < Parameters.NUM_X_TILES; i++) {
                Random rand = new Random();
                TextureRegion floorTile;
                switch (rand.nextInt(3)) {
                    case 0:
                        floorTile = floorAtlas.findRegion("Single_flatjpg");
                        break;
                    case 1:
                        floorTile = floorAtlas.findRegion("Single_rugged");
                        break;
                    default:
                        floorTile = floorAtlas.findRegion("Single_clean");
                }

                if (floorTile == null) {
                    throw new Error();
                }

                floorActorMap[i][j] = new FloorActor(i, j, floorTile);
                // stage.addActor(floorActorMap[i][j]);

                // backgroundActor.addActor(floorActorMap[i][j]);
                gameAreaGroup.addActor(floorActorMap[i][j]);
            }
        }
    }

    /**
     * Spawn monsters
     */
    private void spawnMonsters(int numMonsters) {
        if (numMonsters > (Parameters.MAX_NUM_MONSTERS)) {
            throw new Error();
        }

        monsterActorMap = new MonsterActor[Parameters.NUM_X_TILES][Parameters.NUM_Y_TILES];
        Random rand = new Random();
        int xTile;
        int yTile;
        int count = 0;

        while (count < numMonsters) {
            xTile = rand.nextInt(Parameters.NUM_X_TILES);
            yTile = rand.nextInt(Parameters.NUM_Y_TILES);

            if (!mainTileActorMap[xTile][yTile].isRevealed() && !mainTileActorMap[xTile][yTile].itContainsMonster()) {

                monsterActorMap[xTile][yTile] = new MonsterActor(xTile, yTile, monsterTexture, mainTileActorMap);
                mainTileActorMap[xTile][yTile].setContainsMonster(true);
                // stage.addActor(monsterActorMap[xTile][yTile]);
                gameAreaGroup.addActor(monsterActorMap[xTile][yTile]);
                count++;
            }
        }
    }

    /**
     * Spawn player at either corner
     */
    private void spawnPlayer() {
        Random rand = new Random();
        int randomInt = rand.nextInt(4);
        int x;
        int y;

        switch (randomInt) {
            case 0:
                x = 0;
                y = 0;
                break;
            case 1:
                x = 0;
                y = Parameters.NUM_Y_TILES - 1;
                break;
            case 2:
                x = Parameters.NUM_X_TILES - 1;
                y = 0;
                break;
            default:
                x = Parameters.NUM_X_TILES - 1;
                y = Parameters.NUM_Y_TILES - 1;
                break;
        }

        playerActor = new PlayerActor(x, y, playerAtlas);
        // stage.addActor(playerActor);
        gameAreaGroup.addActor(playerActor);
    }

    /**
     * Add Input Listener to the Stage
     */
    private void addStageTouch() {
        stage.addListener(new InputListener() {
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                Actor currActor = stage.hit(x, y, true);
                if (currActor != null) {
                    if (currActor instanceof MainTileActor) {
                        System.out.println("MapActor clicked");
                        if (((MainTileActor) currActor).isRevealed()) {
                            if (playerActor.getActions().size == 0) {
                                movePlayer(((MainTileActor) currActor).getXTile(), ((MainTileActor) currActor).getYTile());
                            }
                        }

                    } else if (currActor instanceof MonsterActor) {
                        System.out.println("MonsterActor clicked");
                        int currX = ((MonsterActor) currActor).getXTile();
                        int currY = ((MonsterActor) currActor).getYTile();

                        if (mainTileActorMap[currX][currY].isRevealed()
                                && Math.abs(playerActor.getXTile() - currX) <= 1
                                && Math.abs(playerActor.getYTile() - currY) <= 1
                                && playerActor.getPlayerStatus() == PlayerActor.PlayerStatus.STANDING) {
                            mainTileActorMap[currX][currY].setContainsMonster(false);
                            currActor.remove();
                        }

                    } else {
                        // TODO: Abilities?
                        System.out.println("Nothing clicked");
                    }
                }
                return true;
            }
        });
    }

    /**
     * Reveal around the player
     */
    private void revealAround() {
        int pX = playerActor.getXTile();
        int pY = playerActor.getYTile();

        playerActor.setPlayerStatus(PlayerActor.PlayerStatus.STANDING);

        // Reveal tiles with the player
        mainTileActorMap[pX][pY].setRevealed(true);

        if (pX - 1 >= 0) {
            mainTileActorMap[pX - 1][pY].setRevealed(true);
        }
        if (pY - 1 >= 0) {
            mainTileActorMap[pX][pY - 1].setRevealed(true);
        }
        if (pX + 1 < Parameters.NUM_X_TILES) {
            mainTileActorMap[pX + 1][pY].setRevealed(true);
        }
        if (pY + 1 < Parameters.NUM_Y_TILES) {
            mainTileActorMap[pX][pY + 1].setRevealed(true);
        }
    }

    /**
     * Move players algorithm
     * @param destXTile destination X
     * @param destYTile destination Y
     */
    private void movePlayer(int destXTile, int destYTile) {
        class Point {
            int x;
            int y;
        }
        int count = 0;

        // Reset mapSteps to check for shortest path
        for (int j = 0; j < Parameters.NUM_Y_TILES; j++) {
            for (int i = 0; i < Parameters.NUM_X_TILES; i++) {
                mapSteps[i][j] = -1;
            }
        }

        Queue<Point> queue = new LinkedList<Point>();
        Point initPos = new Point();
        initPos.x = destXTile;
        initPos.y = destYTile;
        queue.add(initPos);

        // Add first tile weighted as 0. Subsequent tiles away from initial point, will increase "count"
        mapSteps[initPos.x][initPos.y] = 0;

        while (!queue.isEmpty()) {
            Point currPos = queue.poll();

            // If path is found, pass the mapSteps to let PlayerActor animate and render
            if (currPos.x == playerActor.getXTile() && currPos.y == playerActor.getYTile()) {
                playerActor.moveCoord(mapSteps, destXTile, destYTile);
                break;
            }

            // Add an extra count for each step away from destination
            ++count;

            if ((currPos.x - 1 >= 0) && (mainTileActorMap[currPos.x - 1][currPos.y].isRevealed())
                    && !mainTileActorMap[currPos.x - 1][currPos.y].itContainsMonster()
                    && (mapSteps[currPos.x - 1][currPos.y] == -1)) {
                Point newPos = new Point();
                newPos.x = currPos.x - 1;
                newPos.y = currPos.y;
                queue.add(newPos);
                mapSteps[newPos.x][newPos.y] = count;
            }

            if (currPos.x + 1 < Parameters.NUM_X_TILES && mainTileActorMap[currPos.x + 1][currPos.y].isRevealed()
                    && !mainTileActorMap[currPos.x + 1][currPos.y].itContainsMonster()
                    && mapSteps[currPos.x + 1][currPos.y] == -1) {
                Point newPos = new Point();
                newPos.x = currPos.x + 1;
                newPos.y = currPos.y;
                queue.add(newPos);
                mapSteps[newPos.x][newPos.y] = count;
            }

            if (currPos.y - 1 >= 0 && mainTileActorMap[currPos.x][currPos.y - 1].isRevealed()
                    && !mainTileActorMap[currPos.x][currPos.y - 1].itContainsMonster()
                    && mapSteps[currPos.x][currPos.y - 1] == -1) {
                Point newPos = new Point();
                newPos.x = currPos.x;
                newPos.y = currPos.y - 1;
                queue.add(newPos);
                mapSteps[newPos.x][newPos.y] = count;
            }

            if (currPos.y + 1 < Parameters.NUM_Y_TILES && mainTileActorMap[currPos.x][currPos.y + 1].isRevealed()
                    && !mainTileActorMap[currPos.x][currPos.y + 1].itContainsMonster()
                    && mapSteps[currPos.x][currPos.y + 1] == -1) {
                Point newPos = new Point();
                newPos.x = currPos.x;
                newPos.y = currPos.y + 1;
                queue.add(newPos);
                mapSteps[newPos.x][newPos.y] = count;
            }
        } // end of while loop
    }
}

