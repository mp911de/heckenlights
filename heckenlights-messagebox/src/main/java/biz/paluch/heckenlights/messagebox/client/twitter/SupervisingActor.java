package biz.paluch.heckenlights.messagebox.client.twitter;

import biz.paluch.heckenlights.messagebox.akka.SpringExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.routing.FromConfig;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
@Component
@Scope("prototype")
public class SupervisingActor extends UntypedActor {

    private final LoggingAdapter log = Logging.getLogger(getContext().system(), "Supervisor");

    @Autowired
    private SpringExtension springExtension;

    private ActorRef actorRef;

    @Override
    public void preStart() throws Exception {

        log.info("Starting up");

        actorRef = getContext().actorOf(springExtension.props("writeMessageActor").withRouter(new FromConfig()), "writeMessageActor");
        getContext().watch(actorRef);

    }

    @Override
    public void onReceive(Object message) throws Exception {
        actorRef.tell(message, self());
    }
}
