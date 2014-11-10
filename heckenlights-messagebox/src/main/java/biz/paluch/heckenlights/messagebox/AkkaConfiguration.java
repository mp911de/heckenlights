package biz.paluch.heckenlights.messagebox;

import java.io.File;

import akka.actor.ActorSystem;
import biz.paluch.heckenlights.messagebox.akka.SpringExtension;
import com.typesafe.config.ConfigFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
@Configuration
public class AkkaConfiguration {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private SpringExtension springExtension;

    @Bean
    public ActorSystem actorSystem() {

        ActorSystem system = ActorSystem.create("HeckenlightsMessages", ConfigFactory.parseFile(new File("akka.conf")));
        springExtension.initialize(applicationContext);
        return system;
    }

}
