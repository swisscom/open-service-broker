package com.swisscom.cloud.sb.broker.config

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    public static final String ROLE_CF_ADMIN = 'CF_ADMIN'
    public static final String ROLE_CF_EXT_ADMIN = 'CF_EXT_ADMIN'

    private final AuthenticationConfig authenticationConfig

    @Autowired
    WebSecurityConfig(AuthenticationConfig authenticationConfig) {
        this.authenticationConfig = authenticationConfig
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers('/version', '/v2/api-docs','/swagger-ui.html','/swagger-resources/**').permitAll()
                .antMatchers('/v2/cf-ext/**/*','/custom/**/*').hasRole(ROLE_CF_EXT_ADMIN)
                .antMatchers('/v2/**/*').hasRole(ROLE_CF_ADMIN)
                .anyRequest().authenticated().and()
                .httpBasic()
        http.csrf().disable()
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.inMemoryAuthentication().withUser(authenticationConfig.cfUsername).password(authenticationConfig.cfPassword).roles(ROLE_CF_ADMIN)
        auth.inMemoryAuthentication().withUser(authenticationConfig.cfExtUsername).password(authenticationConfig.cfExtPassword).roles(ROLE_CF_EXT_ADMIN)
    }
}