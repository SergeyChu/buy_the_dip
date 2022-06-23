package buythedip.springbeans;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ForkJoinPoolFactoryBean;
import org.springframework.stereotype.Component;

import java.util.concurrent.ForkJoinPool;


@Component
public class WebRequestsForkJoinPool {
    final ForkJoinPoolFactoryBean forkJoinPoolFactoryBean;

    private final Logger logger = LogManager.getLogger(WebRequestsForkJoinPool.class);

    @Autowired
    public WebRequestsForkJoinPool() {
        this.forkJoinPoolFactoryBean = new ForkJoinPoolFactoryBean();
        forkJoinPoolFactoryBean.setUncaughtExceptionHandler((thread, throwable) ->
                logger.error(String.format("Got exception in thread pool executor %s in thread %s, " +
                                "the exception was not reflected in status message",
                        throwable.getMessage(), thread.getName()), throwable)
        );
        forkJoinPoolFactoryBean.afterPropertiesSet();
    }

    // Creating dedicated pool instead of common one for API requests
    // as usage of commonPool might worsen parallelism of CompletableFuture and ParallelStreams
    public ForkJoinPool getForkJoinPool() {
        return forkJoinPoolFactoryBean.getObject();
    }

}
