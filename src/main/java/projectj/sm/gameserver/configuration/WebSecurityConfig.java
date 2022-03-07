package projectj.sm.gameserver.configuration;

import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import projectj.sm.gameserver.exception.JwtAccessDeniedHandler;
import projectj.sm.gameserver.exception.JwtAuthenticationEntryPoint;
import projectj.sm.gameserver.filter.JwtFilter;
import projectj.sm.gameserver.security.JwtAuthTokenProvider;


@Log
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    private final JwtAuthenticationEntryPoint authenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
    private final JwtAuthTokenProvider tokenProvider;

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .httpBasic().disable()
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and().authorizeRequests()
                    .antMatchers("/v1/login").permitAll()
                    .antMatchers("/v1/kakao/login").permitAll()
                    .antMatchers("/v1/member/use").permitAll()
                    .antMatchers("/v1/member/save").permitAll()
                    .antMatchers("/v1/member/temporary").permitAll()
                    .antMatchers("/v1/**").authenticated()
                .anyRequest().permitAll()
                .and().headers().frameOptions().disable()
                .and().exceptionHandling()
                .authenticationEntryPoint(authenticationEntryPoint) // 인증실패하면 예외 던짐
                .accessDeniedHandler(jwtAccessDeniedHandler);// 접근거부되면 예외 던짐
        httpSecurity.addFilterBefore(new JwtFilter(tokenProvider), UsernamePasswordAuthenticationFilter.class);
        httpSecurity.cors();    // Response for preflight has invalid HTTP status code 401 문제 해결
    }

    @Override
    public void configure(WebSecurity web) {
        web.ignoring()
                .antMatchers("/h2-console/**")
                .antMatchers("/swagger-ui.html", "/webjars/**", "/swagger/**", "/swagger-resources/**", "/v2/api-docs/**")
                .requestMatchers(PathRequest.toStaticResources().atCommonLocations());
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }
}