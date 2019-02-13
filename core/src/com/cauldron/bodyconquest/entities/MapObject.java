package com.cauldron.bodyconquest.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.cauldron.bodyconquest.gamestates.EncounterState;

/*
Contains all the properties and methods that all map objects must have.
It also extends Actor.
*/

public abstract class MapObject extends Actor {

  // public static final MapObject emptyUnit = new MapObject();

  // private Dimension dimension;
  private float cwidth;
  private float cheight;

  private Rectangle bounds;

  public Image sprite;
  protected TextureRegion currentFrame;
  protected Texture texture;

  protected float speed;
  protected boolean collideable;

  public EncounterState screen;

  public MapObject() {
    // This might be a mistake
    setWidth(0);
    setHeight(0);
    bounds = new Rectangle();
  }

  public void setX(float x) {
    super.setX(x);
    bounds.setX(x);
  }

  public void setY(float y) {
    super.setY(y);
    bounds.setY(y);
  }

  public float getCwidth() {
    return cwidth;
  }

  public float getCheight() {
    return cheight;
  }

  public void moveUp(float delta) {
    setY(getY() + (delta * speed));
  }

  public void moveDown(float delta) {
    setY(getY() - (delta * speed));
  }

  public void moveLeft(float delta) {
    setX(getX() - (delta * speed));
  }

  public void moveRight(float delta) {
    setX(getX() + (delta * speed));
  }

  /* maybe int depending on implementation. */
  public float distFrom(MapObject object) {
    return distFrom(object.getCentreX(), object.getCentreY());
  }

  public float distFrom(float x, float y) {
    double xDif = (double) this.getCentreX() - (double) x;
    double yDif = (double) this.getCentreY() - (double) y;
    // System.out.println((float) Math.sqrt(Math.pow(xDif, 2) + Math.pow(yDif, 2)));
    return (float) Math.sqrt(Math.pow(xDif, 2) + Math.pow(yDif, 2));
  }

  public float getCentreX() {
    return getX() + (getWidth() / 2);
  }

  public float getCentreY() {
    return getY() + (getHeight() / 2);
  }

  public boolean isCollideable() { return collideable; }

  public Rectangle getBounds()
  {
    return new Rectangle(getX(), getY(), getWidth(), getHeight());
  }

  public boolean checkCollision(MapObject object){
    if(object.getBounds().overlaps(this.bounds)) return true;
    return false;
  }

  @Override
  public void draw(Batch batch, float parentAlpha) {
    Color color = getColor();
    batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
    batch.draw(currentFrame, getX(), getY(), getWidth(), getHeight());
  }

}
