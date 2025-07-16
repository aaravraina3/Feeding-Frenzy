import tester.*;
import javalib.worldimages.*;
import javalib.funworld.*;
import java.awt.Color;
import java.util.Random;

// Represents a fish in the game
abstract class AFish {
  int x;
  int y;
  int size;
  Color color;

  AFish(int x, int y, int size, Color color) {
    this.x = x;
    this.y = y;
    this.size = size;
    this.color = color;
  }

  // Draw this fish as an ellipse
  WorldImage draw() {
    return new EllipseImage(this.size * 2, this.size, OutlineMode.SOLID, this.color);
  }

  // Can this fish eat the other fish based on size comparison?
  boolean canEat(AFish other) {
    return this.size > other.size;
  }

  // Is this fish touching the other fish based on distance between centers?
  boolean isTouching(AFish other) {
    double distance = Math.sqrt(Math.pow(this.x - other.x, 2) + Math.pow(this.y - other.y, 2));
    return distance < (this.size + other.size) / 2;
  }
}

// Represents the player's fish with velocity-based movement and inertia
class PlayerFish extends AFish {
  double velocityX;
  double velocityY;
  double acceleration;
  double maxSpeed;
  int speedBoostTimer;

  // Constructor for new player fish at given position and size
  PlayerFish(int x, int y, int size) {
    super(x, y, size, Color.ORANGE);
    this.velocityX = 0;
    this.velocityY = 0;
    this.acceleration = 0.8;
    this.maxSpeed = 8.0;
    this.speedBoostTimer = 0;
  }

  // Constructor with full velocity and boost state
  PlayerFish(int x, int y, int size, double vx, double vy, int speedBoostTimer) {
    super(x, y, size, Color.ORANGE);
    this.velocityX = vx;
    this.velocityY = vy;
    this.acceleration = 0.8;
    this.maxSpeed = 8.0;
    this.speedBoostTimer = speedBoostTimer;
  }

  // Calculate inertia factor based on size - bigger fish have more inertia
  double getInertiaFactor() {
    return Math.max(0.92, 0.98 - (this.size - 20) * 0.0008);
  }

  // Calculate acceleration factor based on size - bigger fish accelerate slower
  double getAccelerationFactor() {
    return Math.max(0.4, 1.0 - (this.size - 20) * 0.008);
  }

  // Update velocity based on arrow key input, applying size-based modifiers
  PlayerFish updateVelocity(String key) {
    double accelFactor = this.getAccelerationFactor();
    double boost = this.speedBoostTimer > 0 ? 1.5 : 1.0;
    double newVx = this.velocityX;
    double newVy = this.velocityY;

    if (key.equals("up")) {
      newVy -= this.acceleration * accelFactor * boost;
    }
    else if (key.equals("down")) {
      newVy += this.acceleration * accelFactor * boost;
    }
    else if (key.equals("left")) {
      newVx -= this.acceleration * accelFactor * boost;
    }
    else if (key.equals("right")) {
      newVx += this.acceleration * accelFactor * boost;
    }

    double currentMaxSpeed = this.maxSpeed * boost;
    newVx = Math.max(-currentMaxSpeed, Math.min(currentMaxSpeed, newVx));
    newVy = Math.max(-currentMaxSpeed, Math.min(currentMaxSpeed, newVy));

    return new PlayerFish(this.x, this.y, this.size, newVx, newVy, this.speedBoostTimer);
  }

  // Move the fish based on current velocity, applying inertia
  PlayerFish moveWithInertia() {
    double inertia = this.getInertiaFactor();
    double newVx = this.velocityX * inertia;
    double newVy = this.velocityY * inertia;

    if (Math.abs(newVx) < 0.1) {
      newVx = 0;
    }

    if (Math.abs(newVy) < 0.1) {
      newVy = 0;
    }

    int newX = (int) (this.x + newVx);
    int newY = (int) (this.y + newVy);
    int newBoostTimer = Math.max(0, this.speedBoostTimer - 1);

    return new PlayerFish(newX, newY, this.size, newVx, newVy, newBoostTimer);
  }

  // Wrap the fish around screen edges for continuous play area
  PlayerFish wrapAround(int width, int height) {
    int newX = this.x;
    int newY = this.y;

    if (this.x < 0) {
      newX = width;
    }
    if (this.x > width) {
      newX = 0;
    }
    if (this.y < 0) {
      newY = height;
    }
    if (this.y > height) {
      newY = 0;
    }

    return new PlayerFish(newX, newY, this.size, this.velocityX, this.velocityY,
        this.speedBoostTimer);
  }

  // Grow the player fish by the given amount
  PlayerFish grow(int amount) {
    return new PlayerFish(this.x, this.y, this.size + amount, this.velocityX, this.velocityY,
        this.speedBoostTimer);
  }

  // Apply a temporary speed boost to this fish
  PlayerFish applySpeedBoost() {
    return new PlayerFish(this.x, this.y, this.size, this.velocityX, this.velocityY, 300);
  }

  // Draw the player fish with color based on boost state
  @Override
  WorldImage draw() {
    Color drawColor = this.speedBoostTimer > 0 ? Color.CYAN : Color.ORANGE;
    return new EllipseImage(this.size * 2, this.size, OutlineMode.SOLID, drawColor);
  }
}

// Represents an AI-controlled background fish that moves horizontally
class BackgroundFish extends AFish {
  int speed;
  boolean movingRight;

  BackgroundFish(int x, int y, int size, Color color, int speed, boolean movingRight) {
    super(x, y, size, color);
    this.speed = speed;
    this.movingRight = movingRight;
  }

  // Move this fish horizontally based on its direction
  BackgroundFish move() {
    if (this.movingRight) {
      return new BackgroundFish(this.x + this.speed, this.y, this.size, this.color, this.speed,
          this.movingRight);
    }
    else {
      return new BackgroundFish(this.x - this.speed, this.y, this.size, this.color, this.speed,
          this.movingRight);
    }
  }

  // Wrap this fish around horizontal screen edges
  BackgroundFish wrapAround(int width) {
    if (this.movingRight && this.x > width + this.size) {
      return new BackgroundFish(-this.size, this.y, this.size, this.color, this.speed,
          this.movingRight);
    }
    else if (!this.movingRight && this.x < -this.size) {
      return new BackgroundFish(width + this.size, this.y, this.size, this.color, this.speed,
          this.movingRight);
    }
    return this;
  }
}

// Abstract class representing a special power-up snack
abstract class Snack {
  int x;
  int y;
  int size = 10;

  Snack(int x, int y) {
    this.x = x;
    this.y = y;
  }

  // Draw this snack with its specific appearance
  abstract WorldImage draw();

  // Check if this snack is touching the given player
  boolean isTouching(PlayerFish player) {
    double distance = Math.sqrt(Math.pow(this.x - player.x, 2) + Math.pow(this.y - player.y, 2));
    return distance < (this.size + player.size) / 2;
  }
}

// Size snack that provides instant growth when collected
class SizeSnack extends Snack {
  SizeSnack(int x, int y) {
    super(x, y);
  }

  // Draw this snack as a yellow star
  WorldImage draw() {
    return new StarImage(this.size, OutlineMode.SOLID, Color.YELLOW);
  }
}

// Speed snack that provides temporary speed boost when collected
class SpeedSnack extends Snack {
  SpeedSnack(int x, int y) {
    super(x, y);
  }

  // Draw this snack as a magenta diamond shape
  WorldImage draw() {
    return new OverlayImage(
        new RectangleImage(this.size, this.size, OutlineMode.SOLID, Color.MAGENTA),
        new CircleImage(this.size / 2, OutlineMode.SOLID, Color.MAGENTA));
  }
}

// Interface for a list of fish
interface ILoFish {
  // Move all fish in this list one step
  ILoFish moveAll();

  // Wrap all fish around the screen width
  ILoFish wrapAll(int width);

  // Draw all fish in this list onto the given scene
  WorldScene drawAll(WorldScene scene);

  // Check collisions between all fish and the player, returning results
  CollisionResult checkCollisions(PlayerFish player);

  // Add a new fish to the front of this list
  ILoFish add(BackgroundFish fish);

  // Count how many fish are smaller than the given size
  int countSmallerThan(int size);

  // Count the total number of fish in this list
  int count();
}

// Empty list of fish
class MtLoFish implements ILoFish {
  // Move all fish in empty list (returns empty list)
  public ILoFish moveAll() {
    return this;
  }

  // Wrap all fish in empty list (returns empty list)
  public ILoFish wrapAll(int width) {
    return this;
  }

  // Draw all fish in empty list (returns unchanged scene)
  public WorldScene drawAll(WorldScene scene) {
    return scene;
  }

  // Check collisions in empty list (no collisions possible)
  public CollisionResult checkCollisions(PlayerFish player) {
    return new CollisionResult(player, this, false, 0);
  }

  // Add a fish to empty list, creating a new non-empty list
  public ILoFish add(BackgroundFish fish) {
    return new ConsLoFish(fish, this);
  }

  // Count fish smaller than size in empty list (always 0)
  public int countSmallerThan(int size) {
    return 0;
  }

  // Count total fish in empty list (always 0)
  public int count() {
    return 0;
  }
}

// Non-empty list of fish
class ConsLoFish implements ILoFish {
  BackgroundFish first;
  ILoFish rest;

  ConsLoFish(BackgroundFish first, ILoFish rest) {
    this.first = first;
    this.rest = rest;
  }

  // Move all fish by moving first and recursively moving rest
  public ILoFish moveAll() {
    return new ConsLoFish(this.first.move(), this.rest.moveAll());
  }

  // Wrap all fish by wrapping first and recursively wrapping rest
  public ILoFish wrapAll(int width) {
    return new ConsLoFish(this.first.wrapAround(width), this.rest.wrapAll(width));
  }

  // Draw all fish by drawing first then recursively drawing rest
  public WorldScene drawAll(WorldScene scene) {
    return this.rest.drawAll(scene.placeImageXY(this.first.draw(), this.first.x, this.first.y));
  }

  // Check collisions with first fish, then recursively check rest
  public CollisionResult checkCollisions(PlayerFish player) {
    if (player.isTouching(this.first)) {
      if (player.canEat(this.first)) {
        CollisionResult restResult = this.rest.checkCollisions(player.grow(this.first.size / 5));
        return new CollisionResult(restResult.player, restResult.remainingFish,
            restResult.playerDied, restResult.pointsGained + this.first.size * 2);
      }
      else if (this.first.canEat(player)) {
        return new CollisionResult(player, this, true, 0);
      }
    }
    CollisionResult restResult = this.rest.checkCollisions(player);
    return new CollisionResult(restResult.player,
        new ConsLoFish(this.first, restResult.remainingFish), restResult.playerDied,
        restResult.pointsGained);
  }

  // Add a fish to the front of this list
  public ILoFish add(BackgroundFish fish) {
    return new ConsLoFish(fish, this);
  }

  // Count fish smaller than size, including first if applicable
  public int countSmallerThan(int size) {
    if (this.first.size < size) {
      return 1 + this.rest.countSmallerThan(size);
    }
    else {
      return this.rest.countSmallerThan(size);
    }
  }

  // Count total fish including first plus rest
  public int count() {
    return 1 + this.rest.count();
  }
}

// Represents the result of checking collisions between player and fish
class CollisionResult {
  PlayerFish player;
  ILoFish remainingFish;
  boolean playerDied;
  int pointsGained;

  CollisionResult(PlayerFish player, ILoFish remainingFish, boolean playerDied, int pointsGained) {
    this.player = player;
    this.remainingFish = remainingFish;
    this.playerDied = playerDied;
    this.pointsGained = pointsGained;
  }
}

// Interface for a list of snacks
interface ILoSnack {
  // Add a snack to this list
  ILoSnack add(Snack snack);

  // Check which snacks the player collected and apply effects
  SnackResult checkSnacks(PlayerFish player);

  // Draw all snacks in this list onto the scene
  WorldScene drawAll(WorldScene scene);
}

// Empty list of snacks
class MtLoSnack implements ILoSnack {
  // Add a snack to empty list, creating new non-empty list
  public ILoSnack add(Snack snack) {
    return new ConsLoSnack(snack, this);
  }

  // Check snacks in empty list (no snacks to check)
  public SnackResult checkSnacks(PlayerFish player) {
    return new SnackResult(player, this, 0);
  }

  // Draw all snacks in empty list (returns unchanged scene)
  public WorldScene drawAll(WorldScene scene) {
    return scene;
  }
}

// Non-empty list of snacks
class ConsLoSnack implements ILoSnack {
  Snack first;
  ILoSnack rest;

  ConsLoSnack(Snack first, ILoSnack rest) {
    this.first = first;
    this.rest = rest;
  }

  // Add a snack to the front of this list
  public ILoSnack add(Snack snack) {
    return new ConsLoSnack(snack, this);
  }

  // Check if player collected first snack, then check rest
  public SnackResult checkSnacks(PlayerFish player) {
    if (this.first.isTouching(player)) {
      if (this.first instanceof SizeSnack) {
        SnackResult restResult = this.rest.checkSnacks(player.grow(10));
        return new SnackResult(restResult.player, restResult.remainingSnacks,
            restResult.pointsGained + 50);
      }
      else {
        SnackResult restResult = this.rest.checkSnacks(player.applySpeedBoost());
        return new SnackResult(restResult.player, restResult.remainingSnacks,
            restResult.pointsGained + 25);
      }
    }
    SnackResult restResult = this.rest.checkSnacks(player);
    return new SnackResult(restResult.player,
        new ConsLoSnack(this.first, restResult.remainingSnacks), restResult.pointsGained);
  }

  // Draw all snacks by drawing first then recursively drawing rest
  public WorldScene drawAll(WorldScene scene) {
    return this.rest.drawAll(scene.placeImageXY(this.first.draw(), this.first.x, this.first.y));
  }
}

// Represents the result of checking snack collection
class SnackResult {
  PlayerFish player;
  ILoSnack remainingSnacks;
  int pointsGained;

  SnackResult(PlayerFish player, ILoSnack remainingSnacks, int pointsGained) {
    this.player = player;
    this.remainingSnacks = remainingSnacks;
    this.pointsGained = pointsGained;
  }
}

// Represents the game world for Feeding Frenzy
class FeedingFrenzyWorld extends World {
  int width = 800;
  int height = 600;

  PlayerFish player;
  ILoFish backgroundFish;
  ILoSnack snacks;
  Random rand;
  int tickCount;
  boolean gameOver;
  boolean won;
  int score;
  int lives;

  // Constructor for real games with random generation
  FeedingFrenzyWorld() {
    this(new Random());
  }

  // Constructor for testing with specified Random seed
  FeedingFrenzyWorld(Random rand) {
    this.rand = rand;
    this.player = new PlayerFish(width / 2, height / 2, 20);
    this.backgroundFish = new MtLoFish();
    this.snacks = new MtLoSnack();
    this.tickCount = 0;
    this.gameOver = false;
    this.won = false;
    this.score = 0;
    this.lives = 3;

    this.backgroundFish = this.initializeFish(5);
  }

  // Constructor with all fields for creating new world states
  FeedingFrenzyWorld(PlayerFish player, ILoFish fish, ILoSnack snacks, Random rand, int tickCount,
      boolean gameOver, boolean won, int score, int lives) {
    this.player = player;
    this.backgroundFish = fish;
    this.snacks = snacks;
    this.rand = rand;
    this.tickCount = tickCount;
    this.gameOver = gameOver;
    this.won = won;
    this.score = score;
    this.lives = lives;
  }

  // Initialize the world with n random fish
  ILoFish initializeFish(int n) {
    if (n <= 0) {
      return new MtLoFish();
    }
    else {
      return this.initializeFish(n - 1).add(this.makeRandomFish());
    }
  }

  // Create a random background fish with random properties
  BackgroundFish makeRandomFish() {
    int y = this.rand.nextInt(height - 40) + 20;
    int size = this.rand.nextInt(30) + 10;
    boolean movingRight = this.rand.nextBoolean();
    int x = movingRight ? -size : width + size;
    int speed = this.rand.nextInt(3) + 1;
    Color color = new Color(this.rand.nextInt(256), this.rand.nextInt(256), this.rand.nextInt(256));

    return new BackgroundFish(x, y, size, color, speed, movingRight);
  }

  // Create a random snack at a random position
  Snack makeRandomSnack() {
    int x = this.rand.nextInt(width - 40) + 20;
    int y = this.rand.nextInt(height - 40) + 20;

    if (this.rand.nextBoolean()) {
      return new SizeSnack(x, y);
    }
    else {
      return new SpeedSnack(x, y);
    }
  }

  // Handle arrow key presses to control player movement
  public World onKeyEvent(String key) {
    if (!this.gameOver
        && (key.equals("up") || key.equals("down") || key.equals("left") || key.equals("right"))) {
      return new FeedingFrenzyWorld(this.player.updateVelocity(key), this.backgroundFish,
          this.snacks, this.rand, this.tickCount, this.gameOver, this.won, this.score, this.lives);
    }
    return this;
  }

  // Update the world state on each tick of the game
  public World onTick() {
    if (this.gameOver) {
      return this;
    }

    PlayerFish movedPlayer = this.player.moveWithInertia().wrapAround(width, height);

    ILoFish movedFish = this.backgroundFish.moveAll().wrapAll(width);

    SnackResult snackResult = this.snacks.checkSnacks(movedPlayer);

    CollisionResult collisionResult = movedFish.checkCollisions(snackResult.player);

    if (collisionResult.playerDied) {
      if (this.lives > 1) {
        return new FeedingFrenzyWorld(new PlayerFish(width / 2, height / 2, 20),
            collisionResult.remainingFish, snackResult.remainingSnacks, this.rand,
            this.tickCount + 1, false, false,
            this.score + snackResult.pointsGained + collisionResult.pointsGained, this.lives - 1);
      }
      else {
        return new FeedingFrenzyWorld(collisionResult.player, collisionResult.remainingFish,
            snackResult.remainingSnacks, this.rand, this.tickCount + 1, true, false,
            this.score + snackResult.pointsGained + collisionResult.pointsGained, this.lives);
      }
    }

    ILoFish newFishList = collisionResult.remainingFish;
    if ((this.tickCount + 1) % 60 == 0) {
      newFishList = newFishList.add(this.makeRandomFish());
    }

    ILoSnack newSnackList = snackResult.remainingSnacks;
    if ((this.tickCount + 1) % 180 == 0) {
      newSnackList = newSnackList.add(this.makeRandomSnack());
    }

    boolean hasWon = newFishList.count() > 0
        && newFishList.countSmallerThan(collisionResult.player.size) == newFishList.count();

    return new FeedingFrenzyWorld(collisionResult.player, newFishList, newSnackList, this.rand,
        this.tickCount + 1, hasWon, hasWon,
        this.score + snackResult.pointsGained + collisionResult.pointsGained, this.lives);
  }

  // Draw the current game scene
  public WorldScene makeScene() {
    WorldScene scene = new WorldScene(width, height);

    if (this.gameOver) {
      scene = scene.placeImageXY(new RectangleImage(width, height, OutlineMode.SOLID, Color.WHITE),
          width / 2, height / 2);

      String message = this.won ? "You Win!" : "Game Over!";
      Color messageColor = this.won ? Color.GREEN : Color.RED;
      WorldImage text = new TextImage(message, 48, messageColor);
      scene = scene.placeImageXY(text, width / 2, height / 2);

      WorldImage scoreText = new TextImage("Final Score: " + this.score, 32, Color.BLACK);
      scene = scene.placeImageXY(scoreText, width / 2, height / 2 + 50);

      WorldImage subtext = new TextImage(
          "You were the " + (this.won ? "biggest" : "eaten") + " fish!", 24, Color.BLACK);
      scene = scene.placeImageXY(subtext, width / 2, height / 2 + 90);
    }
    else {
      scene = scene.placeImageXY(
          new RectangleImage(width, height, OutlineMode.SOLID, new Color(150, 200, 220)), width / 2,
          height / 2);

      scene = this.snacks.drawAll(scene);

      scene = this.backgroundFish.drawAll(scene);

      scene = scene.placeImageXY(this.player.draw(), this.player.x, this.player.y);

      WorldImage hudBg = new RectangleImage(120, 80, OutlineMode.SOLID, Color.BLACK);
      scene = scene.placeImageXY(hudBg, 60, 40);

      WorldImage sizeText = new TextImage("Size: " + this.player.size, 16, Color.WHITE);
      scene = scene.placeImageXY(sizeText, 50, 20);

      WorldImage scoreText = new TextImage("Score: " + this.score, 16, Color.WHITE);
      scene = scene.placeImageXY(scoreText, 50, 40);

      WorldImage livesText = new TextImage("Lives: " + this.lives, 16, Color.RED);
      scene = scene.placeImageXY(livesText, 50, 60);

      if (this.player.speedBoostTimer > 0) {
        WorldImage boostText = new TextImage("SPEED BOOST!", 20, Color.MAGENTA);
        scene = scene.placeImageXY(boostText, width / 2, 30);
      }
    }

    return scene;
  }
}

// Examples and tests for the Feeding Frenzy game
class ExamplesFeedingFrenzy {
  Random testRand = new Random(42);
  FeedingFrenzyWorld testWorld = new FeedingFrenzyWorld(testRand);
  PlayerFish player1 = new PlayerFish(100, 100, 20);
  PlayerFish player2 = new PlayerFish(200, 200, 30);
  BackgroundFish bg1 = new BackgroundFish(50, 50, 15, Color.RED, 2, true);
  BackgroundFish bg2 = new BackgroundFish(300, 100, 25, Color.GREEN, 3, false);
  SizeSnack sizeSnack = new SizeSnack(100, 100);
  SpeedSnack speedSnack = new SpeedSnack(200, 200);

  // Test that inertia physics work correctly
  boolean testInertia(Tester t) {
    PlayerFish p = new PlayerFish(100, 100, 20);
    PlayerFish pWithVelocity = p.updateVelocity("right");
    // After applying inertia factor of 0.92 to velocity of 0.8, we get 0.736
    // When cast to int, this becomes 0, so x stays at 100
    PlayerFish movedFish = pWithVelocity.moveWithInertia();
    return t.checkExpect(pWithVelocity.velocityX, 0.8) && t.checkExpect(movedFish.x, 100)
        && t.checkExpect(movedFish.velocityY, 0.0);
  }

  // Test that fish eating logic works based on size
  boolean testCanEat(Tester t) {
    return t.checkExpect(player2.canEat(player1), true)
        && t.checkExpect(player1.canEat(player2), false)
        && t.checkExpect(player1.canEat(bg1), true);
  }

  // Test that background fish move correctly
  boolean testBackgroundMove(Tester t) {
    return t.checkExpect(bg1.move().x, 52) && t.checkExpect(bg2.move().x, 297);
  }

  // Test that player fish grows correctly
  boolean testGrow(Tester t) {
    return t.checkExpect(player1.grow(5).size, 25);
  }

  // Test that speed boost is applied correctly
  boolean testSpeedBoost(Tester t) {
    PlayerFish boosted = player1.applySpeedBoost();
    return t.checkExpect(boosted.speedBoostTimer, 300);
  }

  // Test that fish are drawn with correct properties
  boolean testFishDrawing(Tester t) {
    PlayerFish p = new PlayerFish(400, 300, 20);
    return t.checkExpect(p.x, 400) && t.checkExpect(p.y, 300)
        && t.checkExpect(p.color, Color.ORANGE);
  }

  // Run the game with bigBang
  boolean testGame(Tester t) {
    FeedingFrenzyWorld world = new FeedingFrenzyWorld();
    world.bigBang(world.width, world.height, 0.05);
    return true;
  }
}