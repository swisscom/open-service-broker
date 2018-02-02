package com.swisscom.cloud.sb.broker.config

import com.swisscom.cloud.sb.broker.security.ApplicationUserDetailsService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder

@Configuration
@EnableWebSecurity
class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    public static final String ROLE_CF_ADMIN = 'CF_ADMIN'
    public static final String ROLE_CF_EXT_ADMIN = 'CF_EXT_ADMIN'

    @Autowired
    private ApplicationUserDetailsService userDetailsService

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder()
    }

    @Bean
    DaoAuthenticationProvider authProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider()
        authProvider.userDetailsService = userDetailsService
        authProvider.passwordEncoder = passwordEncoder()
        return authProvider
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(authProvider())
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

}