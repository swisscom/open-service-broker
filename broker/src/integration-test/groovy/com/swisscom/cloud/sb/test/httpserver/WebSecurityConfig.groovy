package com.swisscom.cloud.sb.test.httpserver

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter
import org.springframework.security.web.authentication.www.DigestAuthenticationEntryPoint
import org.springframework.security.web.authentication.www.DigestAuthenticationFilter

@Configuration
@EnableWebSecurity
class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private SimpleUserDetailsService userService
    @Autowired
    private HttpServerConfig httpServerConfig

    @Override
    UserDetailsService userDetailsServiceBean() {
        return userService
    }

    @Override
    protected void configure(AuthenticationManagerBuilder registry) throws Exception {
        registry.userDetailsService(userDetailsServiceBean())
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {


        switch (httpServerConfig.authenticationType) {
            case HttpServerConfig.AuthenticationType.NONE:
                http.authorizeRequests()
                        .anyRequest().permitAll()
                        .and()
                        .httpBasic().disable()
                break
            case HttpServerConfig.AuthenticationType.SIMPLE:
                break
            case HttpServerConfig.AuthenticationType.DIGEST:
                http.exceptionHandling().authenticationEntryPoint(digestEntryPoint())
                        .and().addFilterAfter(digestAuthenticationFilter(digestEntryPoint()), BasicAuthenticationFilter.class)
                        .antMatcher("/**")
                        .authorizeRequests().anyRequest().authenticated()
                break
        }
    }

    @Bean
    public DigestAuthenticationEntryPoint digestEntryPoint() {
        DigestAuthenticationEntryPoint digestAuthenticationEntryPoint = new DigestAuthenticationEntryPoint()
        digestAuthenticationEntryPoint.setKey("acegi")
        digestAuthenticationEntryPoint.setRealmName("Digest Realm")
        digestAuthenticationEntryPoint.setNonceValiditySeconds(10)
        return digestAuthenticationEntryPoint
    }

    @Bean
    public DigestAuthenticationFilter digestAuthenticationFilter(DigestAuthenticationEntryPoint digestAuthenticationEntryPoint) {
        DigestAuthenticationFilter digestAuthenticationFilter = new DigestAuthenticationFilter()
        digestAuthenticationFilter.setAuthenticationEntryPoint(digestEntryPoint())
        digestAuthenticationFilter.setUserDetailsService(userDetailsServiceBean())
        return digestAuthenticationFilter
    }

}
