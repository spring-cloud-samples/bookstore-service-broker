/*
 * Copyright 2002-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.broker.bookstore.webmvc.config;

import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.cloud.broker.bookstore.webmvc.security.SecurityAuthorities;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		// @formatter:off
		http
				.csrf().disable()
				.authorizeRequests()
					.antMatchers("/bookstores/**").authenticated()
					.antMatchers("/v2/**").hasRole(SecurityAuthorities.ADMIN)
					.requestMatchers(EndpointRequest.to("info", "health")).permitAll()
					.requestMatchers(EndpointRequest.toAnyEndpoint()).hasRole(SecurityAuthorities.ADMIN)
				.and()
					.httpBasic();
		// @formatter:on
	}

	@Bean
	public InMemoryUserDetailsManager userDetailsService() {
		return new InMemoryUserDetailsManager(adminUser());
	}

	@SuppressWarnings("deprecation")
	private UserDetails adminUser() {
		return User.withDefaultPasswordEncoder()
				.username("admin")
				.password("supersecret")
				.roles(SecurityAuthorities.ADMIN, SecurityAuthorities.FULL_ACCESS)
				.build();
	}
}