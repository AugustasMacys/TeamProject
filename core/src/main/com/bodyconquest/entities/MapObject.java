package main.com.bodyconquest.entities;

import main.com.bodyconquest.constants.MapObjectType;
import main.com.bodyconquest.gamestates.EncounterState;

import java.awt.*;
import java.awt.geom.Area;

/**
 * This class contains all the properties and methods that all items will require to interact and
 * respond inside the EncounterState.
 */
public abstract class MapObject {

  // Directions in degrees with up being 0
  private final double UP_DIRECTION = 90; // 0;
  private final double DOWN_DIRECTION = 270;
  private final double LEFT_DIRECTION = 180; // -90;
  private final double RIGHT_DIRECTION = 360; // 90;

  // Type of the object
  protected MapObjectType mapObjectType;

  // Current x and y
  private double x;
  private double y;
  // Destination x and y
  private double dx;
  private double dy;
  // Object width and height
  private int width;
  private int height;
  // Collision box width and height
  private int cwidth;
  private int cheight;

  // Movement attributes
  /**
   * The rate at which the current speed increases per tick until the MapObject's movement reaches
   * maxSpeed.
   */
  protected double acceleration;
  /**
   * The represents the distance that the MapObject is moving per tick in the direction represented
   * by the direction variable.
   */
  protected double currentSpeed;
  /** The maximum speed at which this MapObject can move. */
  protected double maxSpeed;
  /** The rate of deceleration applied to the currentSpeed when this MapObject stops moving. */
  protected double stopSpeed;
  /**
   * The direction the MapObject will move in Radians with up or north being 0 Radians and down or
   * south being -&pi; Radians or &pi; Radians (-180 Degrees or 180 Degrees).
   */
  private double direction;

  // Properties
  /** Represents whether this MapObject can collide with other units. */
  protected boolean collidable;

  // States
  /**
   * Represents whether the unit is currently move (Note: This does not mean that it's currentSpeed
   * is 0 rather that is currently at 0 OR decelerating.
   */
  protected boolean moving;

  /** Constructor. */
  public MapObject() {
    setWidth(0);
    setHeight(0);
  }

  /**
   * Set the x co-ordinate value of this MapObject.
   *
   * @param x The x value to be set.
   */
  public void setX(double x) {
    this.x = x;
  }

  /**
   * Set the y co-ordinate value of this MapObject.
   *
   * @param y The y value to be set.
   */
  public void setY(double y) {
    this.y = y;
  }

  /**
   * Get the width of the collision box of this MapObject.
   *
   * @return The width of the collision box of this MapObject.
   */
  protected double getCwidth() {
    return cwidth;
  }

  /**
   * Get the height of the collision box of this MapObject.
   *
   * @return The height of the collision box of this MapObject.
   */
  protected double getCheight() {
    return cheight;
  }

  /**
   * Get the type of MapObject which includes all possible types.
   *
   * @return The type of the MapObject.
   */
  public MapObjectType getMapObjectType() {
    return mapObjectType;
  }

  /**
   * Set the type of MapObject.
   *
   * @param mapObjectType The type of the MapObject to be set.
   */
  public void setMapObjectType(MapObjectType mapObjectType) {
    this.mapObjectType = mapObjectType;
  }

  /**
   * Get the euclidean distance between the centre of this MapObject and the centre of the given
   * MapObject.
   *
   * @param mapObject The MapObject to find the distance to.
   * @return The euclidean distance between this MapObject and the given MapObject.
   */
  protected double distFrom(MapObject mapObject) {
    return distFrom(mapObject.getCentreX(), mapObject.getCentreY());
  }

  /**
   * Get the euclidean distance between the centre of this MapObject and the given co-ordinate.
   *
   * @param x The x value of the co-ordinate.
   * @param y The y value of the co-ordinate.
   * @return The euclidean distance between this MapObject and the given co-ordinate.
   */
  protected double distFrom(double x, double y) {
    double xDif = this.getCentreX() - x;
    double yDif = this.getCentreY() - y;
    return Math.sqrt(Math.pow(xDif, 2) + Math.pow(yDif, 2));
  }

  /**
   * Get the x co-ordinate at the centre of this MapObject.
   *
   * @return The x co-ordinate at the centre of this MapObject.
   */
  public double getCentreX() {
    return getX() + (getWidth() / 2.0f);
  }

  /**
   * Get the y co-ordinate at the centre of this MapObject.
   *
   * @return The y co-ordinate at the centre of this MapObject.
   */
  public double getCentreY() {
    return getY() + (getHeight() / 2.0f);
  }

  /**
   * Check if the MapObject is collidable.
   *
   * @return True if the MapObject is collidable.
   */
  public boolean isCollidable() {
    return collidable;
  }

  /**
   * Get the {@link Shape} that represents the collision boundary of this MapObject.
   *
   * @return The {@link Shape} that represents the collision boundary of this MapObject.
   */
  public Shape getBounds() {
    return new Rectangle(
        (int) x + ((width - cwidth) / 2), (int) y + ((height - cheight) / 2), cwidth, cheight);
  }

  /**
   * Check if this MapObject and the given MapObject are colliding.
   *
   * @param mapObject The mapObject that is being checked against.
   * @return True if this MapObject and the given MapObject are colliding.
   */
  protected boolean checkCollision(MapObject mapObject) {
    Area areaA = new Area(this.getBounds());
    areaA.intersect(new Area(mapObject.getBounds()));
    return !areaA.isEmpty();
  }

  /** Set the movement direction of this MapObject to be upwards (+Y direction). */
  protected void setDirectionUp() {
    direction = Math.toRadians(UP_DIRECTION);
  }

  /** Set the movement direction of this MapObject to be downwards (-Y direction). */
  protected void setDirectionDown() {
    direction = Math.toRadians(DOWN_DIRECTION);
  }

  /** Set the movement direction of this MapObject to the left (-X direction). */
  protected void setDirectionLeft() {
    direction = Math.toRadians(LEFT_DIRECTION);
  }

  /** Set the movement direction of this MapObject to the right (+X direction). */
  protected void setDirectionRight() {
    direction = Math.toRadians(RIGHT_DIRECTION);
  }

  /**
   * Set the movement direction of this MapObject to be upwards and to the left (+Y and -X
   * direction).
   */
  protected void setDirectionUpLeft() {
    direction = Math.toRadians((UP_DIRECTION + LEFT_DIRECTION) / 2);
  }

  /**
   * Set the movement direction of this MapObject to be upwards and to the right (+Y and +X
   * direction).
   */
  public void setDirectionUpRight() {
    direction = Math.toRadians((UP_DIRECTION + RIGHT_DIRECTION) / 2);
  }

  /**
   * Set the movement direction of this MapObject to be downwards and to the left (-Y and -X
   * direction).
   */
  public void setDirectionDownLeft() {
    direction = Math.toRadians((-DOWN_DIRECTION + LEFT_DIRECTION) / 2);
  }

  /**
   * Set the movement direction of this MapObject to be downwards and to the right (-Y and +X
   * direction).
   */
  protected void setDirectionDownRight() {
    direction = Math.toRadians((DOWN_DIRECTION + RIGHT_DIRECTION) / 2);
  }

  /**
   * Set the movement direction of this MapObject to that of the given angle.
   *
   * @param angle The direction of movement in Radians (with 0 Radians as up).
   */
  private void setDirection(double angle) {
    direction = angle;
  }

  /**
   * Get the movement direction of this MapObject.
   *
   * @return The movement direction of this MapObject.
   */
  protected double getDirection() {
    return direction;
  }

  /**
   * Get the width of the MapObject.
   *
   * @return The width of the MapObject.
   */
  public int getWidth() {
    return width;
  }

  /**
   * Set the width of the MapObject. (Defaulted to 0 in the constructor)
   *
   * @param width The width of the MapObject.
   */
  public void setWidth(int width) {
    this.width = width;
  }

  /**
   * Get the height of the MapObject.
   *
   * @return The height of the MapObject.
   */
  public int getHeight() {
    return height;
  }

  /**
   * Get the height of the MapObject. (Defaulted to 0 in the constructor)
   *
   * @param height The height of the MapObject.
   */
  public void setHeight(int height) {
    this.height = height;
  }

  /**
   * Set the width of the collision boundary of this MapObject.
   *
   * @param cwidth The width of the collision boundary.
   */
  private void setCWidth(int cwidth) {
    this.cwidth = cwidth;
  }

  /**
   * Set the height of the collision boundary of this MapObject.
   *
   * @param cheight The height of the collision boundary.
   */
  private void setCHeight(int cheight) {
    this.cheight = cheight;
  }

  /**
   * Get the x co-ordinate value of this MapObject.
   *
   * @return The x co-ordinate value of this MapObject.
   */
  public double getX() {
    return x;
  }

  /**
   * Get the y co-ordinate value of this MapObject.
   *
   * @return The y co-ordinate value of this MapObject.
   */
  public double getY() {
    return y;
  }

  /**
   * Get the maximum speed of this MapObject.
   *
   * @return The maximum speed of this MapObject.
   */
  protected double getMaxSpeed() {
    return maxSpeed;
  }

  /**
   * Set the x and y co-ordinate values of this MapObject.
   *
   * @param x The x co-ordinate value.
   * @param y The y co-ordinate value.
   */
  public void setPosition(double x, double y) {
    setX(x);
    setY(y);
  }

  /**
   * Set the width and height of this MapObject.
   *
   * @param width The width of this MapObject.
   * @param height The height of this MapObject.
   */
  protected void setSize(int width, int height) {
    setWidth(width);
    setHeight(height);
  }

  /**
   * Set the width and height of the collision boundary of this MapObject.
   *
   * @param cwidth The width of the collision boundary of this MapObject.
   * @param cheight The height of the collision boundary of this Mapobject.
   */
  protected void setCSize(int cwidth, int cheight) {
    setCWidth(cwidth);
    setCHeight(cheight);
  }

  /**
   * Set the movement state of this MapObject.
   *
   * @param b The movement state of this MapObject.
   */
  protected void setMoving(boolean b) {
    moving = b;
  }

  /**
   * Set the movement direction of this MapObject to point to the given MapObject.
   *
   * @param mapObject The MapObject to set the movement direction towards.
   */
  public void moveTowards(MapObject mapObject) {
    moveTowards(mapObject.getCentreX(), mapObject.getCentreY());
  }

  /**
   * Set the movement direction of this MapObject to point towards the given co-ordinate value.
   *
   * @param x The x co-ordinate to set the movement direction towards.
   * @param y The y co-ordinate to set the movement direction towards.
   */
  protected void moveTowards(double x, double y) {
    double relX = x - this.getCentreX();
    double relY = y - this.getCentreY();
    double angle = Math.atan((y - this.getCentreY()) / (x - this.getCentreX()));
    if (relX < 0 && relY >= 0) {
      angle = angle - Math.PI;
    } else if (relY <= 0 && relX <= 0) {
      angle += Math.PI;
    } else if (relY < 0 && relX >= 0) {
      angle = (2 * Math.PI) - angle;
    }
    setDirection(angle);
  }

  /**
   * Sets the next position to move towards, this the x and y positions are set once collisions are
   * checked.
   */
  public void move() {
    dx = x;
    dy = y;

    if (moving) {
      if (currentSpeed < maxSpeed) currentSpeed += acceleration;
      currentSpeed = Math.min(currentSpeed, maxSpeed);
    } else {
      if (currentSpeed == 0) return;
      currentSpeed -= stopSpeed;
      currentSpeed = Math.max(currentSpeed, 0);
    }
    dx += currentSpeed * Math.cos(direction);
    dy += currentSpeed * Math.sin(direction);

    // This may need to exist outside of move in the future
    checkCollisions();
  }

  /**
   * An abstract update method that is called by the {@link
   * EncounterState} each tick. Usually contains move and check
   * collisions method calls.
   */
  public abstract void update();

  // Right now this does nothing but in the future this will check if the object is out of bounds or
  // trying to walk into
  // another wall / unit and stop the x and/or y values from changing
  // This may mean that all move commands for map objects will need to be called, then all check
  // collisions will need to
  // be called after

  /**
   * Check if the MapObject will collide if it makes its next movement, if there are no collisions
   * the MapObject finalises the movement. (Currently no collisions are checked)
   */
  private void checkCollisions() {
    x = dx;
    y = dy;
  }

  /**
   * The simplified object that is sent to the client. Creates a {@link BasicObject} representation
   * of this MapObject for the View/Renderer/Client to process.
   *
   * @return A {@link BasicObject} representation of this MapObject.
   */
  public BasicObject getBasicObject() {
    BasicObject bo = new BasicObject();
    bo.setX(x);
    bo.setY(y);
    bo.setWidth(width);
    bo.setHeight(height);
    bo.setCwidth(cwidth);
    bo.setCheight(cheight);
    bo.setDirection(direction);
    bo.setCurrentSpeed(currentSpeed);
    bo.setMapObjectType(mapObjectType);
    bo.setRotation((direction + Math.PI) * (180 / Math.PI));
    //bo.setRotation(direction + Math.PI);
    return bo;
  }
}