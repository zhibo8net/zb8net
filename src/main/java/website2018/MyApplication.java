package website2018;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;

import website2018.base.BaseEndPoint;
import website2018.filter.RestFilter;
import website2018.filter.WebFilter;
import website2018.service.LoginService;

@SpringBootApplication
@EnableScheduling
public class MyApplication {
    
    public static boolean DONT_RUN_SCHEDULED = false;
    public static boolean TEST_LIVE_SPIDER = false;

    @Autowired LoginService accountService;
    
    @Bean
    public TaskScheduler taskScheduler() {
        return new ConcurrentTaskScheduler();
    }
    
    @Bean
    public FilterRegistrationBean restFilter() {
        FilterRegistrationBean registrationBean = new FilterRegistrationBean();
        registrationBean.setFilter(new RestFilter(accountService));
        registrationBean.addUrlPatterns("/api/admin/*");

        return registrationBean;
    }

    @Bean
    public FilterRegistrationBean webFilter() {
        FilterRegistrationBean registrationBean = new FilterRegistrationBean();
        registrationBean.setFilter(new WebFilter());
        registrationBean.addUrlPatterns("/");
        registrationBean.addUrlPatterns("/live_1");
        registrationBean.addUrlPatterns("/projectVideo_1");
        registrationBean.addUrlPatterns("/gameVideo_1");
        registrationBean.addUrlPatterns("/recording_1");
        registrationBean.addUrlPatterns("/news_1");
        registrationBean.addUrlPatterns("/news_1/*");
        registrationBean.addUrlPatterns("/image_1");
        registrationBean.addUrlPatterns("/projectImage_1");
        registrationBean.addUrlPatterns("/viewImage_1/*");
        registrationBean.addUrlPatterns("/video_1/*");

        return registrationBean;
    }

    public static void main(String[] args) throws Exception {
        BaseEndPoint.USE_CACHE = false;
        SpringApplication.run(MyApplication.class, args);
    }

}