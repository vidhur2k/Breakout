import com.almasb.ents.component.DoubleComponent;
import com.almasb.fxgl.app.FXGL;
import com.almasb.fxgl.time.LocalTimer;
import javafx.util.Duration;

public class SpeedComponent extends DoubleComponent
{
    private int duration;   //  measured in ms
    private LocalTimer timer;

    public SpeedComponent(double value, int duration)
    {
        super(value);
        this.duration = duration;
        timer = FXGL.newLocalTimer();
    }

    public boolean timerDone()
    {
        return timer.elapsed(Duration.millis(duration));
    }

    public void startTimer()
    {
        timer.capture();
    }



}
