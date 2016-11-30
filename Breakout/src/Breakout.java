import com.almasb.ents.Entity;
import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.entity.Entities;
import com.almasb.fxgl.entity.GameEntity;
import com.almasb.fxgl.entity.component.CollidableComponent;
import com.almasb.fxgl.entity.component.PositionComponent;
import com.almasb.fxgl.entity.component.TypeComponent;
import com.almasb.fxgl.gameplay.Level;
import com.almasb.fxgl.input.Input;
import com.almasb.fxgl.input.UserAction;
import com.almasb.fxgl.parser.TextLevelParser;
import com.almasb.fxgl.physics.*;
import com.almasb.fxgl.settings.GameSettings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Point2D;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import org.jbox2d.dynamics.BodyType;

public class Breakout extends GameApplication
{
    private boolean started;
    private GameEntity paddle, ball;
    private PaddleControl paddleControl;
    private BallControl ballControl;
    private IntegerProperty lvl, score, balls;
    private TextLevelParser parsa;
    private Level level;

    @Override
    protected void initSettings(GameSettings gameSettings)
    {
        gameSettings.setTitle("Breakout");
        gameSettings.setVersion("0.1");
        gameSettings.setWidth(640);
        gameSettings.setHeight(700);
        gameSettings.setIntroEnabled(false);
        gameSettings.setMenuEnabled(false);
        gameSettings.setShowFPS(false);
    }

    @Override
    protected void initInput()
    {
        Input input = getInput();

        input.addAction(new UserAction("Start")
        {
            @Override
            protected void onActionBegin()
            {
                if(!started)
                {
                    ballControl.start(input.getMousePositionUI());
                    started = true;
                }
            }
        }, MouseButton.PRIMARY);

        input.addAction(new UserAction("Left")
        {
            @Override
            protected void onAction()
            {
                paddleControl.left();

                if(!started)
                    ballControl.left();
            }

            @Override
            protected void onActionEnd()
            {
                paddleControl.stop();

                if(!started)
                    ballControl.stop();
            }
        }, KeyCode.A);

        input.addAction(new UserAction("Right")
        {
            @Override
            protected void onAction()
            {
                paddleControl.right();

                if(!started)
                    ballControl.right();
            }

            @Override
            protected void onActionEnd()
            {
                paddleControl.stop();

                if(!started)
                    ballControl.stop();
            }
        }, KeyCode.D);

        input.addAction(new UserAction("Clear")
        {
            @Override
            protected void onActionBegin()
            {
                getGameWorld()
                        .getEntitiesByType(Type.BRICK)
                        .forEach(e -> e.removeFromWorld());
            }
        }, KeyCode.C);
    }

    @Override
    protected void initAssets()
    {
        getAssetLoader().cache();
    }

    @Override
    protected void initGame()
    {
        started = false;

        parsa = new TextLevelParser();
        parsa.addEntityProducer('B', (x, y) ->
                EntityFactory.newBasicBrick(x * 64, y * 32 + 30));

        parsa.addEntityProducer('S', (x, y) ->
                EntityFactory.newStrongBrick(x * 64, y * 32 + 30));

        parsa.addEntityProducer('P', (x, y) ->
                EntityFactory.newSpeedBrick(x * 64, y * 32 + 30));


        lvl = new SimpleIntegerProperty();
        level = parsa.parse("breakout" + lvl.get() + ".txt");

        score = new SimpleIntegerProperty();
        balls = new SimpleIntegerProperty(3);

        initScreenBounds();
        initPaddle();
        initBall();
        initBricks();
    }

    private void initScreenBounds()
    {
        Entity walls = Entities.makeScreenBounds(150);
        walls.addComponent(new TypeComponent(Type.SCREEN));
        walls.addComponent(new CollidableComponent(true));

        GameEntity bottom =
                Entities.builder()
                        .type(Type.BOTTOM)
                        .build();

        bottom.setY(getHeight());
        bottom.getBoundingBoxComponent()
                .addHitBox(new HitBox("BODY",
                        BoundingShape.box(getWidth(), 150)));

        PhysicsComponent physics = new PhysicsComponent();
        physics.setBodyType(BodyType.STATIC);
        bottom.addComponent(physics);
        bottom.addComponent(new CollidableComponent(true));

        getGameWorld().addEntities(walls, bottom);
    }

    private void initPaddle()
    {
        paddle = Entities.builder().type(Type.PADDLE).build();
        paddle.setX(getWidth() / 2 - 128 / 2);
        paddle.setY(getHeight() -  24);
        paddle.setViewFromTextureWithBBox("paddle.png");

        PhysicsComponent physics = new PhysicsComponent();
        physics.setBodyType(BodyType.KINEMATIC);
        paddle.addComponent(physics);
        paddle.addComponent(new CollidableComponent(true));

        paddleControl = new PaddleControl();
        paddle.addControl(paddleControl);

        getGameWorld().addEntity(paddle);
    }

    private void initBall()
    {
        PositionComponent position =
                paddle.getPositionComponent();

        double x = position.getX() + 128 / 2 - 24 / 2;
        double y = position.getY() - 24;
        double speed = 5.0;

        ball = EntityFactory.newBall(x, y, speed);
        ballControl = ball.getControlUnsafe(BallControl.class);

        getGameWorld().addEntity(ball);
    }

    private void initBricks()
    {
        level.getEntities()
                .stream()
                .forEach(getGameWorld()::addEntity);
                        // e -> getGameWorld().addEntity(e)
    }

    @Override
    protected void initPhysics()
    {
        getPhysicsWorld().setGravity(0, 0);

        getPhysicsWorld().addCollisionHandler(new BallBrickHandler());

        getPhysicsWorld().addCollisionHandler(
                new CollisionHandler(Type.BALL, Type.PADDLE)
        {
            @Override
            protected void onCollisionBegin(Entity a, Entity b)
            {
                PhysicsComponent physics =
                        ball.getComponentUnsafe(PhysicsComponent.class);

                double x;
                double y = physics.getLinearVelocity().getY();

                if(ball.getX() + 24 / 2 > paddle.getX() + 128 / 2)
                    x = Math.abs(physics.getLinearVelocity().getX());
                else
                    x = -Math.abs(physics.getLinearVelocity().getX());

                physics.setLinearVelocity(x, y);
            }
        });

        getPhysicsWorld().addCollisionHandler(
                new CollisionHandler(Type.BALL, Type.BOTTOM)
        {
            @Override
            protected void onCollisionBegin(Entity a, Entity b)
            {
                a.removeFromWorld();
                balls.set(balls.get() - 1);
            }
        });
    }

    @Override
    protected void initUI()
    {
        Text scoreText = new Text();
        scoreText.setTranslateX(5);
        scoreText.setTranslateY(20);
        scoreText.setFont(Font.font(18));
        scoreText.textProperty().bind(score.asString("Score: %d"));

        Text ballsText = new Text();
        ballsText.setTranslateX(getWidth() - 70);
        ballsText.setTranslateY(20);
        ballsText.setFont(Font.font(18));
        ballsText.textProperty().bind(balls.asString("Balls: %d"));

        getGameScene().addUINodes(scoreText, ballsText);
    }

    @Override
    protected void onUpdate(double v)
    {
        checkBalls();
        checkBricks();
    }

    private void checkBalls()
    {
        if(balls.get() <= 0)
            gameOver();
        else if(getGameWorld()
                .getEntitiesByType(Type.BALL).isEmpty())
            resetBall();
    }

    private void checkBricks()
    {
        if(getGameWorld()
                .getEntitiesByType(Type.BRICK)
                .isEmpty())
        {
            lvl.set(lvl.get() + 1);
            level = parsa.parse("breakout" + lvl.get() + ".txt");
            initBricks();
            resetBall();
        }
    }

    private void resetBall()
    {
        ball.removeFromWorld();
        started = false;
        initBall();
    }

    public int getScore()
    {
        return score.get();
    }

    public void setScore(int score)
    {
        this.score.set(score);
    }

    private void gameOver()
    {
        int fontSize = 36;

        Text gameOver = new Text("Game Over!");
        int xOffset = gameOver.getText().length() / 2 * fontSize / 2;

        gameOver.setX(getWidth() / 2 - xOffset);
        gameOver.setY(getHeight() / 2);
        gameOver.setFont(Font.font(fontSize));

        getGameScene().addUINodes(gameOver);
    }

    public static void main(String[] args)
    {
        launch(args);
    }
}
