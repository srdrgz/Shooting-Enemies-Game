package Final;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;
import javax.swing.*;

public class CE203_1905888_Ass2 extends JFrame {

    public JPanel parentPanel;
    public MenuScreen menu;
    public GameScreen game;
    public static JButton playButton;
    public JButton startButton;

    public CE203_1905888_Ass2() {

        //Create parent panel where the game and menu panel will go
        parentPanel = new JPanel();
        parentPanel.setLayout(new BorderLayout(10, 10));
        JPanel scoresPanel = new JPanel();

        //Instantiate the menu and game screen
        menu = new MenuScreen();
        game = new GameScreen();

        //Create start and play again buttons
        startButton = new JButton("Start");
        playButton = new JButton("Play again");

        //Adding ActionListeners to buttons
        startButton.addActionListener(new MouseActionListener(this, 1));
        playButton.addActionListener(new MouseActionListener(this, 2));


       //Adding Menu and start button to parentPanel
        parentPanel.add(menu, BorderLayout.CENTER);
        parentPanel.add(startButton, BorderLayout.SOUTH);

        //Add keyBoard listener
        parentPanel.setFocusable(true);
        parentPanel.requestFocusInWindow();
        parentPanel.addKeyListener(new KeyBoardListener(this));

        //Add parentPanel to frame
        add(parentPanel);

        //Window configuration
        setTitle("1905888");
        pack();
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocation(350,5);
        setVisible(true);
    }
    public static void main(String[] args) {
        new CE203_1905888_Ass2();
    }
}
class MenuScreen extends JPanel {

    //Constructor
    public MenuScreen(){
        //Panel configuration
        setPreferredSize(new Dimension(600,200));
        setBackground(new Color(255,100,255));

        //Game title
        JPanel titlePanel = new JPanel();
        JLabel title = new JLabel("Covid-19 safety game");
        titlePanel.add(title);

        //Game instructions
        JTextArea text = new JTextArea("\n - The red enemies represent people not wearing masks. \n \n - Use the arrows 'UP', 'DOWN', 'RIGHT' and 'LEFT' to move the player around the screen. \n\n - Use the 'SPACE' key to shoot masks to the red enemies to turn them green(Covid-19 safe).\n \n- If the player is hit by the red people, the player loses a life (the player has three lives). \n ");
        text.setBackground(new Color(255,100,255));

        //Adding components to panel
        add(titlePanel);
        add(text);
    }
}
class GameScreen extends JPanel implements Runnable {

    public static int WIDTH = 600;
    public static int HEIGHT = 600;

    public Thread thread;

    private BufferedImage image;
    private Graphics2D g;

    public static ArrayList<Mask> masks;
    public static ArrayList<Enemy> enemies;
    public static ArrayList<Enemy> enemiesHit;
    public Player player;

    private long startRoundTimer;
    private boolean startRound;
    public boolean running;

    //Constructor
    public GameScreen() {
        super();
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setFocusable(true);
        requestFocus();

    }
    @Override
    public void addNotify(){
        super.addNotify();
        if (thread == null) {
            thread = new Thread(this);
            thread.start();
        }
    }
    @Override
    public void run() {
        running = true;

        image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        g = (Graphics2D) image.getGraphics();

        player = new Player(WIDTH / 2, HEIGHT / 2, Color.BLACK,15);
        masks = new ArrayList<>();
        enemies = new ArrayList<>();
        enemiesHit = new ArrayList<>();
        startRoundTimer = 0;
        startRound = true;

        long startTime;
        long TimeMilliseconds;
        long waitTime;
        //int FPS = 30;
        long targetTime = 33;

        //GAME LOOP
        while (running) {
            CE203_1905888_Ass2.playButton.setEnabled(false);
            startTime = System.nanoTime();

            gameDraw();
            gameUpdate();
            gameGenerate();

            TimeMilliseconds = (System.nanoTime() - startTime) / 1000000;
            waitTime = targetTime - TimeMilliseconds;
            try {
                Thread.sleep(Math.abs(waitTime));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        //Draws image for game over and displays top five scores
        g.setColor(new Color(255, 100, 255));
        g.fillRect(0, 0, WIDTH, HEIGHT);
        g.setColor(Color.BLACK);
        g.setFont((new Font("Sans", Font.BOLD, 16)));
        try {
            String s = "G A M E   O V E R    " + "SCORE:  " + enemiesHit.size() ;
            int length = (int) g.getFontMetrics().getStringBounds(s, g).getWidth();
            g.drawString(s, (WIDTH - length) / 2, (HEIGHT - length) / 2);
            String s2 = "TOP SCORES: ";
            g.drawString(s2, 240, 300);
            String s3 = Scores.storeScore(enemiesHit.size());;
            g.drawString(s3, 220, 350);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        gameDraw();

        //enables play again button
        CE203_1905888_Ass2.playButton.setEnabled(true);
    }

    //Create new enemies

    //Updates all elements active
    private void gameUpdate() {

        //Create new enemies
        if(startRoundTimer == 0 && enemies.size()== 0){
            startRound = true;
            startRoundTimer = System.nanoTime();
        }

        //Amount of red enemies on screen increases depending on the score
        if(startRound && enemies.size() < 3){
            Enemy.createEnemy(enemies);
        }
        if(startRound && enemies.size() < 6 && enemiesHit.size() >= 15){
            Enemy.createEnemy(enemies);
        }

        if(startRound && enemies.size() < 12 && enemiesHit.size() >= 30){
            Enemy.createEnemy(enemies);
        }
        if(startRound && enemies.size() < 20 && enemiesHit.size() >= 50){
            Enemy.createEnemy(enemies);
        }

        //update masks/bullets and removing them from the array once out of the window bounds
        if (masks.size() > 0) {
            for (int i = 0; i < masks.size(); i++) {
                masks.get(i).update();
                if (masks.get(i).delete()) {
                    masks.remove(masks.get(i));
                    i--;
                }
            }
        }

        //update player
        player.update();

        //update enemies and hit enemies
        for (Enemy item : enemies) {
            item.update();
        }
        for (Enemy value : enemiesHit) {
            value.update();
        }

        //Player-enemy collision
        for (Enemy enemy : enemies) {
            double dx2 = player.getPosX() - enemy.getPosX();
            double dy2 = player.getPosY() - enemy.getPosY();
            double distPlayer = Math.sqrt(dx2 * dx2 + dy2 * dy2);
            if (distPlayer < player.getRadius() + enemy.getRadius() - 10) {
                player.PlayerIsHit();
                if(player.lives == 2){
                    player.color = Color.DARK_GRAY;
                }
                if(player.lives == 1){
                    player.color = Color.GRAY;
                }
                if(player.lives == 0){
                    player.color = Color.WHITE;
                }
                if(player.lives < 0){
                    running = false;
                }
                enemies.remove(enemy);
                break;
            }
        }

        //Mask(bullet)-enemy collision
        for (Mask m : masks) {
            double mx = m.getPosX();
            double my = m.getPosY();
            double mr = m.getRadius();
            for (int j = 0; j < enemies.size(); j++) {
                Enemy e = enemies.get(j);
                double ex = e.getPosX();
                double ey = e.getPosY();
                double er = e.getRadius();

                double dx1 = mx - ex;
                double dy1 = my - ey;
                double distanceMasks = Math.sqrt((dx1 * dx1 + dy1 * dy1));
                if (distanceMasks < mr + er) {
                    e.isHit(true);
                    enemies.remove(e);
                    enemiesHit.add(e);
                    break;
                }
            }

        }
    }

    //draws everything active on the game
    private void gameGenerate () {
        //draw background
        g.setColor(new Color(255,100,255));
        //g.setColor(new Color(250,150,70));
        g.fillRect(0, 0, WIDTH, HEIGHT);

        //draw lives and score
        g.setColor(Color.BLACK);
        g.drawString("SCORE: " + enemiesHit.size(), 10, 20);
        g.drawString("LIVES: " + player.lives, 10, 40);

        //Amount of enemies increases each round
        g.setFont((new Font("Sans", Font.BOLD, 16)));
        if(enemiesHit.size() < 6){
            g.drawString("R O U N D    1",250, 500);
        }
        if(enemiesHit.size() >= 15 && enemiesHit.size() < 30 ){
            g.drawString("R O U N D    2",250, 500);
        }
        if(enemiesHit.size() >= 30 && enemiesHit.size() < 50){
            g.drawString("R O U N D    3",250, 500);
        }
        if(enemiesHit.size() >= 50 ){
            g.drawString("R O U N D    4",250, 500);
        }
        //draw player
        player.draw(g);

        //draw bullets/masks
        if (masks.size() > 0) {
            for (Mask mask : masks) {
                mask.draw(g);
            }
        }

        //draw enemies and hit enemies
        for (Enemy enemy : enemies) {
            enemy.draw(g);
        }
        for (Enemy enemy : enemiesHit) {
            enemy.draw(g);
        }
    }

    //drawing into image and then onto the screen (double buffering)
    public void gameDraw () {
        Graphics g2 = this.getGraphics();
        g2.drawImage(image, 0, 0, null);
        g2.dispose();
    }
}
class MouseActionListener implements ActionListener {
    CE203_1905888_Ass2 f; //Game passed through to allow for manipulation
    int action;
    public MouseActionListener(CE203_1905888_Ass2 f, int action){
        this.f = f;
        this.action = action;
    }
    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        //Start button in Menu
        if(action == 1){
            f.parentPanel.remove(f.menu);
            f.parentPanel.remove(f.startButton);
            f.parentPanel.add(f.playButton, BorderLayout.SOUTH);
            f.parentPanel.add(f.game, BorderLayout.CENTER);
            f.parentPanel.revalidate();
            f.parentPanel.repaint();
            f.pack();
        }

        //Play again button which is disabled while playing a round
        if(action == 2){
            f.parentPanel.remove(f.game);
            GameScreen newGame = new GameScreen();
            f.parentPanel.add(newGame);
            f.parentPanel.setFocusable(true);
            f.parentPanel.requestFocusInWindow();
            f.parentPanel.addKeyListener(new KeyBoardListener(f));
            f.parentPanel.revalidate();
            f.parentPanel.repaint();
            f.pack();
        }
    }
}
class KeyBoardListener implements KeyListener {
    CE203_1905888_Ass2 frame;  // game passed through to allow for game manipulation
    public KeyBoardListener(CE203_1905888_Ass2 frame){
        this.frame = frame;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()){

            //Moves player with arrows
            case KeyEvent.VK_UP:
                this.frame.game.player.setUp(true);
                break;
            case KeyEvent.VK_DOWN:
                this.frame.game.player.setDown(true);
                break;
            case KeyEvent.VK_LEFT:
                this.frame.game.player.setLeft(true);
                break;
            case KeyEvent.VK_RIGHT:
                this.frame.game.player.setRight(true);
                break;

            //Allows firing with space key
            case KeyEvent.VK_SPACE:
                Player.firing = true;
                break;
            case KeyEvent.VK_S:

                break;
        }
    }
    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()){
            case KeyEvent.VK_UP:
                this.frame.game.player.setUp(false);
                break;
            case KeyEvent.VK_DOWN:
                this.frame.game.player.setDown(false);
                break;
            case KeyEvent.VK_LEFT:
                this.frame.game.player.setLeft(false);
                break;
            case KeyEvent.VK_RIGHT:
                this.frame.game.player.setRight(false);
                break;
        }
    }
    @Override
    public void keyTyped(KeyEvent keyEvent) {
    }
}
abstract class Shape {
    public int radius;
    public int posX;
    public int posY;
    public Color color;

    public Shape(int posX, int posY, Color color,int radius ){
        this.posX = posX;
        this.posY = posY;
        this.radius = radius;
        this.color = color;
    }
    public abstract void draw(Graphics2D g);
    public abstract void update();
}
class Player extends Shape {

    private int speed, dirX, dirY;
    public int lives;
    public static boolean left, right, upPlayer, down;
    public static boolean firing = false;

    //Constructor
    public Player(int posX, int posY, Color color, int radius) {
        super(posX, posY, color, radius);
        dirX = 0;
        dirY = 0;
        speed = 5;
        lives = 3;
    }

    public void setLeft(boolean b) {
        left = b;
    }
    public void setRight(boolean b) {
        right = b;
    }
    public void setUp(boolean b) {
        upPlayer = b;
    }
    public void setDown(boolean b) {
        down = b;
    }

    //If player is hit and removes a life
    public void PlayerIsHit(){
        lives = lives - 1;
    }
    public int getPosX(){ return posX;}
    public int getPosY(){ return posY;}
    public int getRadius(){ return radius;}

    //updates player position
    @Override
    public void update(){
        if (left) dirX = -speed;
        if (right) dirX = speed;
        if (upPlayer) dirY = -speed;
        if (down) dirY = speed;
        posX += dirX;
        posY += dirY;
        if(posX < radius) posX = radius;
        if(posY < radius) posY = radius;
        if(posX > 585-radius) posX = 585-radius;
        if(posY > 600-radius) posY = 600-radius;
        dirX = 0;
        dirY = 0;

        //make masks(bullets) move in the direction the player is moving (if the player is still it will shoot upwards)
        if(firing){
            Mask newMask = new Mask(posX, posY, color.WHITE, 5);
            GameScreen.masks.add(newMask);
            if(left) newMask.setLeft(true);
            if(right) newMask.moveRight(true);
            if(down) newMask.setDown(true);
            if(upPlayer) newMask.setUp(true);
            if((left && right && upPlayer && down) == false)newMask.setUp(true);
            firing = false;
        }

    }

    @Override
    public void draw(Graphics2D g) {
        g.setColor(color);
        g.fillOval(posX-radius,posY-radius,2*radius , 2*radius );

    }
}

// Enemies: people not wearing masks

class Enemy extends Shape {
    private double dx, dy, rad, speed;
    private boolean onScreen, hit;

    //Constructor
    public Enemy(int posX, int posY, Color color, int radius){
        super(posX,posY,color, radius);

        speed = 5;

        double angle = Math.random() * 140 + 20;
        rad = Math.toRadians(angle);
        dx = Math.cos(rad) * speed;
        dy = Math.sin(rad) * speed;

        onScreen = false;
        hit = false;
    }

    //Creates a new enemy and ass it to an array of enemies
    public static void createEnemy(ArrayList<Enemy> enemies){
        enemies.add(new Enemy((int) (Math.random() * GameScreen.WIDTH /2) + GameScreen.WIDTH/4, -5, Color.RED,15));
    }
    public double getPosX(){ return posX;}
    public double getPosY(){ return posY;}
    public int getRadius(){ return radius;}

    //Checks if enemy is hit and changes the color when hit
    public boolean isHit(boolean b){
        hit = b;
        color = Color.GREEN;
        return hit;
    }

    //updates position of enemy and bounce off bounds of window
    @Override
    public void update(){
        posX += dx;
        posY += dy;
        if(!onScreen){
            if(posX>radius && posX < 580 && posY>radius && posY < 595) onScreen = true;
        }
        if( posX < radius && dx < 0) dx = -dx;
        if( posY < radius && dy < 0) dy = -dy;
        if( posX > 580 && dx > 0) dx = -dx;
        if( posY > 590 && dy > 0) dy = -dy;

    }
    public void draw(Graphics2D g) {
        g.setColor(color);
        g.fillOval((int )posX-radius,(int )posY-radius,2*radius , 2*radius );
    }

}
//Bullets the player will shoot
class Mask extends Shape {

    private boolean up, down, left, right;
    public int speed, distX, distY;

    public Mask(int posX, int posY, Color color, int radius) {
        super(posX, posY, color, radius);
        this.speed = 10;
    }

    //Method to remove masks from the array if they are out of the window bounds
    public boolean delete(){
        if (posX < 0 + radius || posY < 0 + radius || posX > 585 - radius || posY > 600 - radius) return true;
        return false;
    }

    public int getPosX(){ return posX;}
    public int getPosY(){ return posY;}
    public int getRadius(){ return radius;}

    public void setUp(boolean a){
        up = true;
    }
    public void setDown(boolean a){
        down = true;
    }
    public void setLeft(boolean a){
        left = true;
    }
    public void moveRight(boolean a){
        right = true;
    }

    //updates masks position
    @Override
    public void update(){
        if (left) distX = -speed;
        if (right) distX = speed;
        if (up) distY = -speed;
        if (down) distY = speed;
        this.posX += distX;
        this.posY += distY;
        distX = 0;
        distY = 0;
    }

    @Override
    public void draw(Graphics2D g) {
        g.setColor(color);
        g.fillOval(posX-radius,posY-radius,2*radius , 2*radius );
    }
}
class Scores {
    //Array to store new scores
    public static ArrayList<Integer> scores = new ArrayList<>();

    /*Adds the new score to the array, prints the scores into a file (scores.txt).
    Then it adds the scores to a set to avoid repeated scores, clears the array and adds the values of the set back into the array.
    Then the array is sorted in descending order and the 5 top scores are printed
    */
    public static String storeScore(Integer newScore) throws FileNotFoundException {
        scores.add(newScore);
        File outFile = new File("scores.txt");
        PrintWriter output = new PrintWriter(outFile);
        for(int i = 0; i < scores.size() ; i++){
            output.println(scores.get(i));
        }
        output.close();
        String s = "";
        Set<Integer> nonRepeatedScores = new HashSet<>();
        nonRepeatedScores.addAll(scores);
        scores.clear();
        scores.addAll(nonRepeatedScores);
        scores.sort(Collections.reverseOrder());
        for(int i = 0; i < scores.size() ; i++) {
            if(scores.size() > 5){
                for(int j = 0; j < 5 ; j++) {
                    s = s + scores.get(j)+"      ";
                }
                break;
            }
            else {
                s = s  + scores.get(i)+"       ";
            }
        }
        return s;
    }
}