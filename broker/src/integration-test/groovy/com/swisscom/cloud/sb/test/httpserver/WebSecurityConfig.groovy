package com.swisscom.cloud.sb.test.httpserver

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter
import org.springframework.security.web.authentication.www.DigestAuthenticationEntryPoint
import org.springframework.security.web.authentication.www.DigestAuthenticationFilter

import static com.swisscom.cloud.sb.test.httpserver.HttpServerConfig.AuthenticationType.*

@EnableWebSecurity(debug = false)
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
            case NONE:
                http.authorizeRequests()
                        .anyRequest().permitAll()
                        .and()
                        .httpBasic().disable()
                break
            case SIMPLE:
                break
            case DIGEST:
                http.exceptionHandling().authenticationEntryPoint(digestEntryPoint())
                        .and().addFilterAfter(digestAuthenticationFilter(digestEntryPoint()), BasicAuthenticationFilter.class)
                        .antMatcher("/**")
                        .authorizeRequests().anyRequest().authenticated()
                break
            case MUTUAL:
                http.authorizeRequests().anyRequest().authenticated()
                        .and()
                        .x509().authenticationDetailsSource()
                        .userDetailsService(userDetailsService1())
                        .and().csrf().disable()
                        .httpBasic().disable()

                break
        }
    }

    @Bean
    public UserDetailsService userDetailsService1() {
        return new UserDetailsService() {
            @Override
            public UserDetails loadUserByUsername(String username) {
                if (username.equals("codependent-client1") || username.equals("codependent-client2") || username.equals("codependent-client")) {
                    return new User(username, "", AuthorityUtils.commaSeparatedStringToAuthorityList("ROLE_USER"));
                } else {
                    return null;
                }
            }
        };
    }


    @Bean
    DigestAuthenticationEntryPoint digestEntryPoint() {
        DigestAuthenticationEntryPoint digestAuthenticationEntryPoint = new DigestAuthenticationEntryPoint()
        digestAuthenticationEntryPoint.setKey("acegi")
        digestAuthenticationEntryPoint.setRealmName("Digest Realm")
        digestAuthenticationEntryPoint.setNonceValiditySeconds(10)
        return digestAuthenticationEntryPoint
    }

    @Bean
    DigestAuthenticationFilter digestAuthenticationFilter(DigestAuthenticationEntryPoint digestAuthenticationEntryPoint) {
        DigestAuthenticationFilter digestAuthenticationFilter = new DigestAuthenticationFilter()
        digestAuthenticationFilter.setAuthenticationEntryPoint(digestEntryPoint())
        digestAuthenticationFilter.setUserDetailsService(userDetailsServiceBean())
        return digestAuthenticationFilter
    }

}
