package com.group4.ticketingservice.config

import com.google.gson.Gson
import com.group4.ticketingservice.filter.JwtAuthenticationFilter
import com.group4.ticketingservice.filter.JwtAuthorizationEntryPoint
import com.group4.ticketingservice.filter.JwtAuthorizationFilter
import com.group4.ticketingservice.utils.TokenProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.web.DefaultSecurityFilterChain

@EnableMethodSecurity
@Configuration
class SecurityConfig(
    private val jwtAuthorizationEntryPoint: JwtAuthorizationEntryPoint,
    private val tokenProvider: TokenProvider,
    private val gson: Gson
) {

    @Bean
    fun filterChain(http: HttpSecurity): DefaultSecurityFilterChain {
        http
            .cors { it.disable() }
            .formLogin { it.disable() }
            .httpBasic { it.disable() }
            .csrf { it.disable() }
            .authorizeHttpRequests {
                it.requestMatchers("/reservations/**").authenticated()
                it.requestMatchers("/bookmarks/**").authenticated()
                it.requestMatchers("/users/access_token_info").authenticated()
                it.anyRequest().permitAll()
            }
            .exceptionHandling { it.authenticationEntryPoint(jwtAuthorizationEntryPoint) }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .apply(CustomFilterConfigurer(tokenProvider, jwtAuthorizationEntryPoint, gson))

        return http.build()!!
    }

    class CustomFilterConfigurer(
        private val tokenProvider: TokenProvider,
        private val jwtAuthorizationEntryPoint: JwtAuthorizationEntryPoint,
        private val gson: Gson
    ) :
        AbstractHttpConfigurer<CustomFilterConfigurer?, HttpSecurity?>() {
        override fun configure(builder: HttpSecurity?) {
            val authenticationManager = builder?.getSharedObject(AuthenticationManager::class.java)
            val jwtAuthenticationFilter = JwtAuthenticationFilter(authenticationManager, tokenProvider, gson)
            jwtAuthenticationFilter.setFilterProcessesUrl("/users/signin")
            val jwtAuthorizationFilter = JwtAuthorizationFilter(authenticationManager, jwtAuthorizationEntryPoint, tokenProvider)
            builder?.addFilter(jwtAuthorizationFilter)
            builder?.addFilterAfter(jwtAuthenticationFilter, JwtAuthorizationFilter::class.java)
        }
    }

    @Bean
    fun passwordEncoder() = BCryptPasswordEncoder()
}
