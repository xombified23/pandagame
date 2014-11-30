package com.xombified23.pandagame.android;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;

/**
 *  Created by Xombified on 8/9/2014.
 */
public class MonsterActor extends Actor {
    private int xTile;
    private int yTile;
    private Texture monsterTexture;
    private boolean isRevealed;

    // TODO: USE INTERFACE for MainTileActor
    public MonsterActor(int xTile, int yTile, Texture monsterTexture) {
        this.xTile = xTile;
        this.yTile = yTile;
        isRevealed = false;

        this.monsterTexture = monsterTexture;
        setBounds(xTile * Parameters.TILE_PIXEL_WIDTH, yTile * Parameters.TILE_PIXEL_HEIGHT, Parameters.TILE_PIXEL_WIDTH,
                Parameters.TILE_PIXEL_HEIGHT);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (isRevealed()) {
            setAggroPerimeter(xTile, yTile);
            batch.draw(monsterTexture, getX(), getY(), monsterTexture.getWidth() * 5, monsterTexture.getHeight() * 5);
        }
    }

    public boolean isRevealed() {
        return isRevealed;
    }

    public void setRevealed(boolean revealed) {
        isRevealed = revealed;
    }

    public int getXTile() {
        return xTile;
    }

    public int getYTile() {
        return yTile;
    }

    private void setAggroPerimeter(int xTile, int yTile) {
        if (xTile-1 >= 0)
            References.mainTileActorMap[xTile-1][yTile].setAggro(true);
        if (xTile-1 >= 0 && yTile-1 >= 0)
            References.mainTileActorMap[xTile-1][yTile-1].setAggro(true);
        if (yTile-1 >= 0)
            References.mainTileActorMap[xTile][yTile-1].setAggro(true);
        if (xTile+1 < Parameters.NUM_X_TILES && yTile-1 >= 0)
            References.mainTileActorMap[xTile+1][yTile-1].setAggro(true);
        if (xTile+1 < Parameters.NUM_X_TILES)
            References.mainTileActorMap[xTile+1][yTile].setAggro(true);
        if (xTile+1 < Parameters.NUM_X_TILES && yTile+1 < Parameters.NUM_Y_TILES)
            References.mainTileActorMap[xTile+1][yTile+1].setAggro(true);
        if (yTile+1 < Parameters.NUM_Y_TILES)
            References.mainTileActorMap[xTile][yTile+1].setAggro(true);
        if (xTile-1 >= 0 && yTile+1 < Parameters.NUM_Y_TILES)
            References.mainTileActorMap[xTile-1][yTile+1].setAggro(true);
    }
}
