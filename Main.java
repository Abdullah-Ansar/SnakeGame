package com.project.snakegame;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class Main extends Application {

    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final int TILE_SIZE = 25;
    private static final int ROWS = HEIGHT / TILE_SIZE;
    private static final int COLUMNS = WIDTH / TILE_SIZE;

    private boolean gameOver = false;
    private List<Point> snake;
    private Point food;
    private Direction direction = Direction.RIGHT;

    private GraphicsContext gc;
    private Timeline gameLoop;

    private int score = 0;
    private Text scoreText;
    private Button restartButton;

    private enum Direction {
        UP, DOWN, LEFT, RIGHT
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            Pane root = new Pane();
            Canvas canvas = new Canvas(WIDTH, HEIGHT);
            gc = canvas.getGraphicsContext2D();

            scoreText = new Text("Score: 0");
            scoreText.setFont(Font.font(20));
            scoreText.setFill(Color.BLACK);
            scoreText.setX(10);  // Position the score text at the top-left corner
            scoreText.setY(20);  // Adjust Y position to fit the text within the window


            restartButton = new Button("Restart"); // Initialize restartButton
            restartButton.getStyleClass().add("button-fancy");
            restartButton.setVisible(false);
            restartButton.setOnAction(event -> restartGame());
            StackPane buttonContainer = new StackPane(restartButton);
            buttonContainer.setPrefSize(WIDTH, HEIGHT);

            root.getChildren().addAll(canvas, scoreText, buttonContainer);

            Scene scene = new Scene(root);
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/com/project/snakegame/styles.css")).toExternalForm());

            primaryStage.setTitle("Snake Game");
            primaryStage.setScene(scene);
            primaryStage.show();

            scene.setOnKeyPressed(event -> {
                KeyCode key = event.getCode();
                if (key == KeyCode.UP && direction != Direction.DOWN) {
                    direction = Direction.UP;
                } else if (key == KeyCode.DOWN && direction != Direction.UP) {
                    direction = Direction.DOWN;
                } else if (key == KeyCode.LEFT && direction != Direction.RIGHT) {
                    direction = Direction.LEFT;
                } else if (key == KeyCode.RIGHT && direction != Direction.LEFT) {
                    direction = Direction.RIGHT;
                }
            });

            initGame();
            runGameLoop(restartButton);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initGame() {
        snake = new ArrayList<>();
        snake.add(new Point(COLUMNS / 2, ROWS / 2));
        snake.add(new Point(COLUMNS / 2 - 1, ROWS / 2));
        snake.add(new Point(COLUMNS / 2 - 2, ROWS / 2));
        spawnFood();
        gameOver = false;
        direction = Direction.RIGHT;
        score = 0;
        updateScore();
    }

    private void runGameLoop(Button restartButton) {
        gameLoop = new Timeline(new KeyFrame(Duration.millis(200), e -> {
            if (!gameOver) {
                updateGame();
                drawGame();
            } else {
                drawGameOver();
                restartButton.setVisible(true);
                gameLoop.stop();
            }
        }));
        gameLoop.setCycleCount(Timeline.INDEFINITE);
        gameLoop.play();
    }

    private void updateGame() {
        Point head = snake.get(0);
        Point newPoint = null;

        switch (direction) {
            case UP:
                newPoint = new Point(head.x, head.y - 1);
                break;
            case DOWN:
                newPoint = new Point(head.x, head.y + 1);
                break;
            case LEFT:
                newPoint = new Point(head.x - 1, head.y);
                break;
            case RIGHT:
                newPoint = new Point(head.x + 1, head.y);
                break;
        }

        if (newPoint.x < 0 || newPoint.x >= COLUMNS || newPoint.y < 0 || newPoint.y >= ROWS || snake.contains(newPoint)) {
            gameOver = true;
            return;
        }

        snake.add(0, newPoint);

        if (newPoint.equals(food)) {
            score++;
            updateScore();
            spawnFood();
        } else {
            snake.remove(snake.size() - 1);
        }
    }

    private void drawGame() {
        gc.clearRect(0, 0, WIDTH, HEIGHT);

        gc.setFill(Color.GREEN);
        for (Point point : snake) {
            gc.fillRect(point.x * TILE_SIZE, point.y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
        }

        gc.setFill(Color.RED);
        gc.fillRect(food.x * TILE_SIZE, food.y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
    }

    private void drawGameOver() {
        gc.setFill(Color.BLACK);
        gc.fillText("Game Over", WIDTH / 2.0 - 30, HEIGHT / 2.0);
    }

    private void spawnFood() {
        Random rand = new Random();
        int x, y;
        do {
            x = rand.nextInt(COLUMNS);
            y = rand.nextInt(ROWS);
        } while (snake.contains(new Point(x, y)));

        food = new Point(x, y);
    }

    private void restartGame() {
        initGame(); // Reset the game state
        restartButton.setVisible(false); // Hide the restart button again
        gameLoop.play(); // Restart the game loop
    }


    private void updateScore() {
        scoreText.setText("Score: " + score);
    }

    private static class Point {
        int x, y;

        Point(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Point point = (Point) o;
            return x == point.x && y == point.y;
        }

        @Override
        public int hashCode() {
            return 31 * x + y;
        }
    }
}
