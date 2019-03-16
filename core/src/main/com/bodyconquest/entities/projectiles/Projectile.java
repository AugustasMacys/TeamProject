package main.com.bodyconquest.entities.projectiles;

import main.com.bodyconquest.entities.MapObject;
import main.com.bodyconquest.entities.Troops.Troop;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class Projectile extends MapObject {

  protected int damage;
  protected double maxTravelDistance;
  protected double distanceTraveled;

  protected boolean piercing;
  //protected CopyOnWriteArrayList<Troop> hitTroops;

  protected double xInit;
  protected double yInit;

  protected boolean remove;

  public Projectile() {
    super();
    // Maybe a better way of doing this
    //hitTroops = new CopyOnWriteArrayList<Troop>();
    this.collidable = true;
    initDefault();
  }

  private void initDefault() {
    // Maxed out because I don't want to do fine tuning right now
    stopSpeed = 100000;
    acceleration = 100000;
    //mapObjectType = Assets.UnitType.FLU;
    distanceTraveled = 0;
    moving = true;
    collidable = true;
    remove = false;
  }

  public void hit(Troop troop) {
    if (remove) return;
//    if (piercing) {
//      if (hitTroops.contains(troop)) return;
//      hitTroops.add(troop);
//      troop.hit(damage);
//      return;
//    }
    troop.hit(damage);
    setRemove();
    // Make sure this immediately removes projectile of necessary and
    // doesn't hit enemies multiple unless intended to do so
  }

  public void setRemove() {
    remove = true;
  }

  public boolean getRemove() {
    return remove;
  }

  public void update() {
    double xPrev, yPrev;
    if(distanceTraveled >= maxTravelDistance) setRemove();
    xPrev = getX();
    yPrev = getY();
    move();
    distanceTraveled = distFrom(xInit, yInit);
  }

  public void checkHit(CopyOnWriteArrayList<Troop> enemies) {
    for(Troop enemy : enemies) {
      if(checkCollision(enemy)/*getBounds().intersects(enemy.getBounds())*/) {
        hit(enemy);
      }
    }
  }

  @Override
  public Shape getBounds() {
    Shape originalRect = super.getBounds();
    AffineTransform trans = AffineTransform.getRotateInstance(getDirection() + Math.PI, getX() + ((getWidth() - getCwidth()) / 2), getY() + ((getHeight() - getCheight()) / 2));
    return trans.createTransformedShape(originalRect);
  }
}