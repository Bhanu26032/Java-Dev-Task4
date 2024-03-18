import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class BrickBreakerGame extends JFrame implements ActionListener {
    private final int WIDTH = 600;
    private final int HEIGHT = 400;
    private final int PADDLE_WIDTH = 80;
    private final int PADDLE_HEIGHT = 10;
    private final int BALL_DIAMETER = 20;
    private final int BRICK_WIDTH = 50;
    private final int BRICK_HEIGHT = 20;
    private final int NUM_BRICKS = 50;
    private final int PADDLE_Y = HEIGHT - 50;
    private final int BALL_START_X = WIDTH / 2 - BALL_DIAMETER / 2;
    private final int BALL_START_Y = HEIGHT / 2 - BALL_DIAMETER / 2;
    private final int DELAY = 10;
    private final int NUM_LIVES = 5;

    private Timer timer;
    private int ballX, ballY, ballXDir = 1, ballYDir = -1;
    private int paddleX = WIDTH / 2 - PADDLE_WIDTH / 2;
    private int score = 0;
    private int lives = NUM_LIVES;
    private boolean[] bricks;
    private boolean gameRunning = true;
    private int level = 1; // Default level is 1

    public BrickBreakerGame() {
        setTitle("Brick Breaker Game");
        setSize(WIDTH, HEIGHT);
        setResizable(false);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        bricks = new boolean[NUM_BRICKS];
        initBricksForLevel();

        // Set initial position of the ball
        ballX = BALL_START_X;
        ballY = BALL_START_Y;

        timer = new Timer(DELAY, this);
        timer.start();

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int key = e.getKeyCode();
                if (key == KeyEvent.VK_LEFT && paddleX > 0) {
                    paddleX -= 20;
                }
                if (key == KeyEvent.VK_RIGHT && paddleX < WIDTH - PADDLE_WIDTH) {
                    paddleX += 20;
                }
            }
        });

        setFocusable(true);
        setFocusTraversalKeysEnabled(false);

        setVisible(true);
    }

    private void initBricksForLevel() {
        bricks = new boolean[NUM_BRICKS];
        int[][] levelBricks = Levels.BRICK_CONFIGURATIONS[level - 1]; // Adjust index since levels start from 1
        int index = 0;
        for (int i = 0; i < levelBricks.length; i++) {
            for (int j = 0; j < levelBricks[i].length; j++) {
                bricks[index++] = levelBricks[i][j] == 1;
            }
        }
    }

    public void paint(Graphics g) {
        // Create an off-screen image for double buffering
        Image offScreenImage = createImage(getWidth(), getHeight());
        Graphics offScreenGraphics = offScreenImage.getGraphics();

        // Draw onto the off-screen graphics
        offScreenGraphics.setColor(Color.BLACK);
        offScreenGraphics.fillRect(0, 0, WIDTH, HEIGHT);

        // Draw paddle
        offScreenGraphics.setColor(Color.WHITE);
        offScreenGraphics.fillRect(paddleX, PADDLE_Y, PADDLE_WIDTH, PADDLE_HEIGHT);

        // Draw ball
        offScreenGraphics.setColor(Color.RED);
        offScreenGraphics.fillOval(ballX, ballY, BALL_DIAMETER, BALL_DIAMETER);

        // Draw bricks
        offScreenGraphics.setColor(Color.GREEN);
        int brickCount = 0;
        for (int i = 0; i < NUM_BRICKS / 10; i++) { // Adjusted loop condition
            for (int j = 0; j < 10; j++) { // Adjusted loop condition
                if (bricks[brickCount]) {
                    offScreenGraphics.fillRect(j * BRICK_WIDTH + 2, i * BRICK_HEIGHT + 50, BRICK_WIDTH - 2, BRICK_HEIGHT - 2);
                }
                brickCount++;
            }
        }

        // Draw score
        offScreenGraphics.setColor(Color.WHITE);
        offScreenGraphics.drawString("Score: " + score, 10, HEIGHT - 20); // Adjusted position

        // Draw lives
        offScreenGraphics.setColor(Color.WHITE);
        offScreenGraphics.drawString("Lives: " + lives, WIDTH - 60, HEIGHT - 20); // Adjusted position

        // Draw current level
        offScreenGraphics.setColor(Color.WHITE);
        offScreenGraphics.drawString("Level: " + level, WIDTH / 2 - 20, HEIGHT - 20); // Centered

        // Transfer the off-screen image to the screen
        g.drawImage(offScreenImage, 0, 0, this);
    }

    public void actionPerformed(ActionEvent e) {
        if (gameRunning) {
            ballX += ballXDir * 2;
            ballY += ballYDir * 2;

            if (ballX <= 0 || ballX >= WIDTH - BALL_DIAMETER) {
                ballXDir *= -1;
            }
            if (ballY <= 0) {
                ballYDir *= -1;
            }
            if (ballY >= HEIGHT - BALL_DIAMETER) {
                lives--;
                if (lives == 0) {
                    endGame();
                } else {
                    resetBall();
                }
            }

            if (ballY + BALL_DIAMETER >= PADDLE_Y && ballX + BALL_DIAMETER >= paddleX && ballX <= paddleX + PADDLE_WIDTH) {
                ballYDir *= -1;
            }

            int brickCount = 0;
            for (int i = 0; i < NUM_BRICKS / 10; i++) {
                for (int j = 0; j < 10; j++) {
                    Rectangle brickRect = new Rectangle(j * BRICK_WIDTH, i * BRICK_HEIGHT + 50, BRICK_WIDTH, BRICK_HEIGHT);
                    Rectangle ballRect = new Rectangle(ballX, ballY, BALL_DIAMETER, BALL_DIAMETER);
                    if (bricks[brickCount] && brickRect.intersects(ballRect)) {
                        bricks[brickCount] = false;
                        ballYDir *= -1;
                        score++;
                    }
                    brickCount++;
                }
            }

            if (score == NUM_BRICKS) {
                switchToNextLevel();
            }

            repaint();
        } else {
            timer.stop();
        }
    }

    private void resetBall() {
        ballX = BALL_START_X;
        ballY = BALL_START_Y;
        ballXDir = 1;
        ballYDir = -1;
    }

    private void endGame() {
        gameRunning = false;
        JOptionPane.showMessageDialog(this, "Game Over. Press Enter to start the game.");
        restartGame();
    }

    private void restartGame() {
        score = 0;
        lives = NUM_LIVES;
        resetBall();
        level = 1; // Reset level to 1
        initBricksForLevel(); // Initialize bricks for level 1
        gameRunning = true;
    }

    private void switchToNextLevel() {
        level++;
        if (level <= Levels.NUM_LEVELS) {
            initBricksForLevel();
            resetBall();
            gameRunning = true;
        } else {
            endGame();
        }
    }

    public static void main(String[] args) {
        new BrickBreakerGame();
    }
}
