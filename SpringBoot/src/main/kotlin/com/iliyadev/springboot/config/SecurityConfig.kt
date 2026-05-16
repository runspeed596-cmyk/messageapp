package com.iliyadev.springboot.config

import com.iliyadev.springboot.config.filters.JwtRequestFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val jwtRequestFilter: JwtRequestFilter
) {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .cors { } 
            .headers { headers ->
                headers.cacheControl { it.disable() }
            }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers("/api/health").permitAll()
                    .requestMatchers("/api/admin/auth/**").permitAll()
                    .requestMatchers("/api/home").permitAll()
                    .requestMatchers("/api/auth/**").permitAll()
                    .requestMatchers("/api/entertainment").permitAll()
                    .requestMatchers("/api/entertainment/media/**").permitAll()
                    .requestMatchers("/api/media/**").permitAll()
                    .requestMatchers("/api/admin/**").authenticated()
                    .requestMatchers("/api/elm-peak/events").permitAll()
                    .requestMatchers("/api/elm-peak/universities").permitAll()
                    .requestMatchers("/swagger-ui/**").permitAll()
                    .requestMatchers("/v3/api-docs/**").permitAll()
                    .requestMatchers("/uploads/**").permitAll()
                    .requestMatchers("/api/files/thumbnail/**").permitAll()
                    .requestMatchers("/api/reference-data/**").permitAll()
                    .requestMatchers("/api/locations/**").permitAll()
                    .requestMatchers("/api/health").permitAll()
                    .requestMatchers("/ws/**").permitAll()
                    .requestMatchers("/api/kelasor-online/**").permitAll()
                    .anyRequest().authenticated()
            }
            .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }
}
