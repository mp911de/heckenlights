package de.paluch.heckenlights;

import java.time.Clock;
import java.util.TimeZone;

import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParamBean;
import org.apache.http.params.HttpParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

import com.google.common.collect.ImmutableSet;

import de.paluch.heckenlights.application.RuleService;
import de.paluch.heckenlights.model.RuleState;
import de.paluch.heckenlights.tracking.TrackingMDCFilter;

@Configuration
@SpringBootApplication
@ImportResource("classpath:META-INF/spring/applicationContext.xml")
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    ThreadSafeClientConnManager threadSafeClientConnManager() {
        return new ThreadSafeClientConnManager();
    }

    @Bean
    RuleState ruleState() {
        return new RuleState();
    }

    @Bean
    BasicHttpParams httpConnectionParams(@Value("${midirelay.connectTimeout}") int connectTimeout,
            @Value("${midirelay.readTimeout}") int readTimeout) {

        BasicHttpParams params = new BasicHttpParams();
        HttpConnectionParamBean bean = new HttpConnectionParamBean(new BasicHttpParams());

        bean.setConnectionTimeout(connectTimeout);
        bean.setSoTimeout(readTimeout);

        return params;
    }

    @Bean
    DefaultHttpClient httpClient(ClientConnectionManager clientConnectionManager, HttpParams httpParams) {
        return new DefaultHttpClient(clientConnectionManager, httpParams);
    }

    @Bean
    public FilterRegistrationBean contextFilterRegistrationBean() {
        FilterRegistrationBean registrationBean = new FilterRegistrationBean();
        registrationBean.setFilter(new TrackingMDCFilter());
        registrationBean.setOrder(1);
        registrationBean.setEnabled(true);
        registrationBean.setUrlPatterns(ImmutableSet.of("/*"));
        return registrationBean;
    }

    @Bean
    Clock clock(RuleService ruleService) {
        return Clock.system(TimeZone.getTimeZone(ruleService.getRules().getTimezone()).toZoneId());
    }
}
