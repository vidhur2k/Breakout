import com.almasb.ents.Entity;
import com.almasb.fxgl.app.FXGL;
import com.almasb.fxgl.entity.GameEntity;
import com.almasb.fxgl.physics.CollisionHandler;

public class BallBrickHandler extends CollisionHandler
{
    private Breakout app;

    public BallBrickHandler()
    {
        super(Type.BALL, Type.BRICK);
        app = (Breakout) FXGL.getApp();
    }

    @Override
    protected void onCollisionBegin(Entity ball, Entity brick)
    {
        HPComponent hp = brick.getComponentUnsafe(HPComponent.class);
        SpeedComponent speed = brick.getComponentUnsafe(SpeedComponent.class);

        hp.setValue(hp.getValue() - 1);

        if(hp.getValue() == 1)
            ((GameEntity) brick).setViewFromTextureWithBBox("brick.png");

        if(hp.getValue() <= 0)
        {
            brick.removeFromWorld();
            app.setScore(app.getScore() + 100);
        }

        if(speed != null)
        {
            ball.getControlUnsafe(BallControl.class)
                    .setSpeed(speed);
            speed.startTimer();
        }

        ball.getControlUnsafe(BallControl.class).setHasCollided(true);
    }




}
