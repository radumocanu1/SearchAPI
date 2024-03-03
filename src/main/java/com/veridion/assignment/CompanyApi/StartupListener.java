import com.veridion.assignment.CompanyApi.threadServices.ThreadRunner;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class StartupListener implements ApplicationListener<ContextRefreshedEvent> {

    private final ThreadRunner threadRunner;


    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        threadRunner.startReaderThread();
    }
}
