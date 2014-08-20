package com.xombified23.pandagame.desktop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;

/**
 * Created by Xombified on 8/9/2014.
 */
public class MonsterActor extends Actor {
    private int xTile;
    private int yTile;
    private Texture monsterTexture;

    public MonsterActor (int x, int y) {
        xTile = x;
        yTile = y;
        monsterTexture = new Texture(Gdx.files.internal("playerSprite.PNG"));
        setBounds(xTile * Parameters.tilePixelWidth, yTile * Parameters.tilePixelHeight, Parameters.tilePixelWidth,
                Parameters.tilePixelHeight);
    }

    public void dispose() {
        monsterTexture.dispose();
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.draw(monsterTexture, getX(), getY());
    }
}