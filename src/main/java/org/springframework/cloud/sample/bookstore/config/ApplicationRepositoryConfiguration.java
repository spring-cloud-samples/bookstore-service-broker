package org.springframework.cloud.sample.bookstore.config;

import org.springframework.cloud.sample.bookstore.servicebroker.repository.ServiceBindingRepository;
import org.springframework.cloud.sample.bookstore.servicebroker.repository.ServiceInstanceRepository;
import org.springframework.cloud.sample.bookstore.web.repository.BookStoreRepository;
import org.springframework.cloud.sample.bookstore.web.repository.UserRepository;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;

@Configuration
@EnableReactiveMongoRepositories(basePackageClasses = {BookStoreRepository.class,
	UserRepository.class, ServiceBindingRepository.class, ServiceInstanceRepository.class})
public class ApplicationRepositoryConfiguration {
}
