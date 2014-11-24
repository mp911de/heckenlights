package de.paluch.heckenlights;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

import com.google.common.collect.ImmutableSet;
import de.paluch.heckenlights.tracking.TrackingMDCFilter;

@Configuration()
@ImportResource("classpath:META-INF/spring/applicationContext.xml")
@EnableAutoConfiguration
@ComponentScan
public class Application {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(Application.class);
        application.setLogStartupInfo(true);
        application.setAddCommandLineProperties(true);
        ApplicationContext ctx = application.run(args);

    }

    @Bean
    public FilterRegistrationBean contextFilterRegistrationBean() {
        FilterRegistrationBean registrationBean = new FilterRegistrationBean();
        registrationBean.setFilter(new TrackingMDCFilter());
        registrationBean.setOrder(1);
        registrationBean.setUrlPatterns(ImmutableSet.of("/*"));
        return registrationBean;
    }

}
