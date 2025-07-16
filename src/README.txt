# Feeding Frenzy - Game Instructions

## How to Play

Welcome to Feeding Frenzy! You control an orange fish in a pond filled with other fish of various sizes and colors.

### Objective
Start as a small fish and eat smaller fish to grow larger while avoiding being eaten by bigger fish. Win by becoming the largest fish in the pond!

### Controls
- **Arrow Keys**: Move your fish
  - Up Arrow: Accelerate upward
  - Down Arrow: Accelerate downward  
  - Left Arrow: Accelerate left
  - Right Arrow: Accelerate right

### Game Rules
1. You can only eat fish smaller than you
2. Larger fish will eat you if they touch you
3. Your fish wraps around the screen edges (exit one side, enter the opposite side)
4. New fish continuously enter from the sides of the screen
5. Each fish you eat makes you grow based on its size
6. Fish of the same size cannot eat each other

### Winning and Losing
- **Win**: Become the largest fish in the pond
- **Lose**: Lose all your lives by getting eaten

### Visual Indicators
- Your fish: Orange ellipse (Cyan when speed boosted)
- Your current size: Displayed in the top-left corner
- Score: Shows your current points
- Lives: Number of remaining lives (in red)
- Other fish: Various colors, moving left or right across the screen
- Yellow stars: Size snacks (instant growth bonus)
- Magenta squares: Speed snacks (temporary speed boost)

## Running the Game
Run the `testGame` method in the `ExamplesFeedingFrenzy` class to start playing.

## Extra Credit Features Implemented

### 1. Inertia System
- Your fish doesn't stop immediately when you release keys - it drifts to a halt
- As your fish grows larger, it becomes harder to accelerate and takes longer to stop
- Creates realistic physics where bigger fish feel "heavier"

### 2. Scoring System
- Eating fish awards points equal to 2x their size
- Size snacks: +50 points
- Speed snacks: +25 points

### 3. Special Snacks
- **Size Snacks (Yellow Stars)**: Instantly grow by 10 units, worth 50 points
- **Speed Snacks (Magenta Squares)**: 5-second speed boost (1.5x speed), worth 25 points
- Snacks appear every 3 seconds at random locations

### 4. Multiple Lives
- Start with 3 lives
- When eaten, respawn at center with original size
- Game ends only when all lives are lost

## Design Notes
- Uses functional programming style with immutable data structures
- Implements custom linked lists for managing collections of fish and snacks
- All game state changes create new objects rather than modifying existing ones
- Movement physics use velocity and acceleration for smooth, realistic motion