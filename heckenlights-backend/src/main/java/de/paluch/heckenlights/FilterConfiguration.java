package de.paluch.heckenlights;

import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.common.collect.ImmutableSet;
import de.paluch.heckenlights.tracking.TrackingMDCFilter;

@Configuration
public class FilterConfiguration {

    @Bean
    public FilterRegistrationBean contextFilterRegistrationBean() {
        FilterRegistrationBean registrationBean = new FilterRegistrationBean();
        registrationBean.setFilter(new TrackingMDCFilter());
        registrationBean.setOrder(1);
        registrationBean.setEnabled(true);
        registrationBean.setUrlPatterns(ImmutableSet.of("/*"));
        return registrationBean;
    }
}
