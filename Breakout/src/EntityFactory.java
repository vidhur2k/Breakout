import com.almasb.fxgl.entity.Entities;
import com.almasb.fxgl.entity.GameEntity;
import com.almasb.fxgl.entity.component.CollidableComponent;
import com.almasb.fxgl.physics.BoundingShape;
import com.almasb.fxgl.physics.HitBox;
import com.almasb.fxgl.physics.PhysicsComponent;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.FixtureDef;

public class EntityFactory
{
    public static GameEntity newBall(double x, double y, double speed)
    {
        FixtureDef def = new FixtureDef();
        def.setDensity(0.3f);
        def.setRestitution(1.0f);

        PhysicsComponent physics = new PhysicsComponent();
        physics.setBodyType(BodyType.DYNAMIC);
        physics.setFixtureDef(def);

        return Entities.builder()
                .at(x, y)
                .type(Type.BALL)
                .viewFromTexture("ball.png")
                .bbox(new HitBox("BODY", BoundingShape.circle(12)))
                .with(physics)
                .with(new CollidableComponent(true))
                .with(new BallControl(speed))
                .build();
    }

    public static GameEntity newBrick(double x, double y)
    {
        PhysicsComponent physics = new PhysicsComponent();
        physics.setBodyType(BodyType.STATIC);

        return Entities.builder()
                .at(x, y)
                .type(Type.BRICK)
                .with(physics)
                .with(new CollidableComponent(true))
                .build();
    }

    public static GameEntity newBasicBrick(double x, double y)
    {
        BrickType type = BrickType.BASIC;

        GameEntity brick = newBrick(x, y);
        brick.addComponent(new SubTypeComponent(type));
        brick.setViewFromTextureWithBBox(type.texture);
        brick.addComponent(new HPComponent(1));

        return brick;
    }

    public static GameEntity newStrongBrick(double x, double y)
    {
        BrickType type = BrickType.STRONG;

        GameEntity brick = newBrick(x, y);
        brick.addComponent(new SubTypeComponent(type));
        brick.setViewFromTextureWithBBox(type.texture);
        brick.addComponent(new HPComponent(2));

        return brick;
    }

    public static GameEntity newSpeedBrick(double x, double y)
    {
        BrickType type = BrickType.SPEED;

        GameEntity brick = newBrick(x, y);
        brick.addComponent(new SubTypeComponent(type));
        brick.setViewFromTextureWithBBox(type.texture);
        brick.addComponent(new HPComponent(1));
        brick.addComponent(new SpeedComponent(1.1, 5000));

        return brick;
    }



}
