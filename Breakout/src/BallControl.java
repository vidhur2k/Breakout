import com.almasb.ents.AbstractControl;
import com.almasb.ents.Entity;
import com.almasb.fxgl.app.FXGL;
import com.almasb.fxgl.entity.component.PositionComponent;
import com.almasb.fxgl.physics.PhysicsComponent;
import com.almasb.fxgl.time.LocalTimer;
import javafx.geometry.Point2D;
import javafx.util.Duration;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class BallControl extends AbstractControl
{
    private double defaultSpeed, speed;
    private PhysicsComponent physics;
    private PositionComponent position;
    private boolean hasCollided;
    private List<SpeedComponent> speedComponentList;
    //private int duration;
    //private LocalTimer timer;

    public BallControl(double speed)
    {
        defaultSpeed = speed;
        this.speed = speed;
        speedComponentList = new LinkedList<SpeedComponent>();
        //timer = FXGL.newLocalTimer();
    }

    @Override
    public void onAdded(Entity entity)
    {
        physics = entity.getComponentUnsafe(PhysicsComponent.class);
        position = entity.getComponentUnsafe(PositionComponent.class);
    }

    @Override
    public void onUpdate(Entity entity, double v)
    {
//        if(duration > 0 && timer.elapsed(Duration.millis(duration)))
//        {
//            speed = defaultSpeed;
//            duration = 0;
//        }

        updateSpeed();

        if(hasCollided)
        {
            if(Math.abs(physics.getLinearVelocity().getX()) < speed)
                physics.setLinearVelocity(
                        Math.signum(physics.getLinearVelocity().getX()) * speed,
                        physics.getLinearVelocity().getY());

            if(Math.abs(physics.getLinearVelocity().getY()) < speed)
                physics.setLinearVelocity(
                        physics.getLinearVelocity().getX(),
                        Math.signum(physics.getLinearVelocity().getY()) * speed);
        }
        else
        {
            if(Math.abs(physics.getLinearVelocity().getX()) < 0.1)
                physics.setLinearVelocity(
                        Math.signum(physics.getLinearVelocity().getX()) * 1.1,
                        physics.getLinearVelocity().getY());

            if(Math.abs(physics.getLinearVelocity().getY()) < 0.1)
                physics.setLinearVelocity(
                        physics.getLinearVelocity().getX(),
                        Math.signum(physics.getLinearVelocity().getY()) * 1.1);
        }
    }

    private void updateSpeed()
    {
        // get rid of completed SpeedComponents
        speedComponentList =
                speedComponentList.stream()
                        .filter(e -> !e.timerDone())
                        .collect(Collectors.toList());

        speed = defaultSpeed;

        if(!speedComponentList.isEmpty())
            speedComponentList.forEach(e -> speed *= e.getValue());

        System.out.println(speed);
    }

    public double getSpeed()
    {
        return speed;
    }

    public void setSpeed(double speed)
    {
        this.speed = speed;
    }

    public void setSpeed(SpeedComponent speedComponent)
    {
        speedComponentList.add(speedComponent);
    }

    public void setHasCollided(boolean hasCollided)
    {
        this.hasCollided = hasCollided;
    }

    public void left()
    {
        if(position.getX() >= speed + 128 / 2 - 24 / 2)
            physics.setLinearVelocity(-speed, 0);
        else
            stop();
    }

    public void right()
    {
        double width = 640;

        if(position.getX() <= width - speed - 128 / 2 - 24 / 2)
            physics.setLinearVelocity(speed, 0);
        else
            stop();
    }

    public void stop()
    {
        physics.setLinearVelocity(0, 0);
    }

    public void start(Point2D mouse)
    {
        double x = mouse.getX() - position.getX();
        double y = mouse.getY() - position.getY();

        double t = Math.atan2(y, x);

        x = 2 * speed * Math.cos(t);
        y = 2 * speed * Math.sin(t);

        physics.setLinearVelocity(x, y);
    }




}
