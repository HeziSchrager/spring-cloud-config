/*
 * Copyright 2013-2014 the original author or authors.
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
package org.springframework.cloud.config.server.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.config.server.ConfigServerHealthIndicator;
import org.springframework.cloud.config.server.ConfigServerProperties;
import org.springframework.cloud.config.server.EnvironmentRepository;
import org.springframework.cloud.config.server.MultipleJGitEnvironmentRepository;
import org.springframework.cloud.config.server.NativeEnvironmentRepository;
import org.springframework.cloud.config.server.SvnKitEnvironmentRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.util.StringUtils;

/**
 * @author Dave Syer
 *
 */
@Configuration
@ConditionalOnMissingBean(EnvironmentRepository.class)
@EnableConfigurationProperties(ConfigServerProperties.class)
public class EnvironmentRepositoryConfiguration {

	@Bean
	@ConditionalOnProperty(value = "spring.cloud.config.server.health.enabled", matchIfMissing = true)
	public ConfigServerHealthIndicator configServerHealthIndicator(EnvironmentRepository repository) {
		return new ConfigServerHealthIndicator(repository);
	}

	protected static class BaseRepositoryConfiguration {

		@Autowired
		private ConfigServerProperties server;

		protected String getDefaultLabel(EnvironmentRepository repository) {
			if (StringUtils.hasText(this.server.getDefaultLabel())) {
				return this.server.getDefaultLabel();
			}
			else {
				return repository.getDefaultLabel();
			}
		}

	}

	@Configuration
	@Profile("native")
	protected static class NativeRepositoryConfiguration {

		@Autowired
		private ConfigurableEnvironment environment;

		@Bean
		public EnvironmentRepository environmentRepository() {
			return new NativeEnvironmentRepository(this.environment);
		}

	}

	@Configuration
	@ConditionalOnMissingBean(EnvironmentRepository.class)
	protected static class GitRepositoryConfiguration extends BaseRepositoryConfiguration {

		@Autowired
		private ConfigurableEnvironment environment;

		@Bean
		public EnvironmentRepository environmentRepository() {
			MultipleJGitEnvironmentRepository repository = new MultipleJGitEnvironmentRepository(this.environment);
			repository.setDefaultLabel(getDefaultLabel(repository));
			return repository;
		}
	}

	@Configuration
	@Profile("subversion")
	protected static class SvnRepositoryConfiguration extends BaseRepositoryConfiguration {
		@Autowired
		private ConfigurableEnvironment environment;

		@Bean
		public EnvironmentRepository environmentRepository() {
			SvnKitEnvironmentRepository repository = new SvnKitEnvironmentRepository(this.environment);
			repository.setDefaultLabel(getDefaultLabel(repository));
			return repository;
		}
	}

}