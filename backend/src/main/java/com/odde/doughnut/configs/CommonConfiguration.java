package com.odde.doughnut.configs;

import java.util.stream.Stream;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.stereotype.Component;

@Component
public class CommonConfiguration {

  void commonConfig(HttpSecurity http, HttpSecurity authenticationFilterConfigurer)
      throws Exception {

    String[] backendRoutes = {
      "/login",
      "/error",
      "/odd-e.png",
      "/odd-e.ico",
      "/webjars/**",
      "/attachments/**",
      "/assets/**",
      "/api/**"
    };

    // the following array has to be in sync with the frontend routes in ApplicationController.java
    // Because java annotation does not allow variable, we have to repeat the routes here.
    String[] frontendRoutes = {
      "/",
      "/bazaar/**",
      "/circles/**",
      "/notebooks/**",
      "/assessment/**",
      "/n**",
      "/reviews/**",
      "/answers/**",
      "/links/**",
      "/failure-report-list/**",
      "/admin-dashboard/**",
      "/assessmentAndCertificateHistory"
    };

    String[] allRoutes =
        Stream.concat(Stream.of(backendRoutes), Stream.of(frontendRoutes)).toArray(String[]::new);

    http.authorizeHttpRequests(
            auth ->
                auth.requestMatchers("/robots.txt")
                    .permitAll()
                    .requestMatchers(allRoutes)
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .logout(
            l ->
                l.logoutUrl("/logout")
                    .logoutSuccessUrl("/")
                    .invalidateHttpSession(true)
                    .permitAll());
  }
}
