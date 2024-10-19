package dev.coms4156.project.config;

import dev.coms4156.project.interceptor.RrLoggingInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * This class configures the web settings for the system.
 * Citation:
 * I used the following tutorial to learn how to create SpringBoot interceptors:
 * <a href="https://www.tutorialspoint.com/spring_boot/spring_boot_interceptor.htm">interceptor</a>
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
  @Autowired
  private RrLoggingInterceptor rrLoggingInterceptor;

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(rrLoggingInterceptor).addPathPatterns("/**");
  }
}
