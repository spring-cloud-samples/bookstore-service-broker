package org.springframework.cloud.sample.bookstore.config;

import org.springframework.cloud.sample.bookstore.servicebroker.repository.ServiceBrokerRepositoryPackageMarker;
import org.springframework.cloud.sample.bookstore.web.repository.WebRepositoryPackageMarker;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;

@Configuration
@EnableReactiveMongoRepositories(
		basePackageClasses = { ServiceBrokerRepositoryPackageMarker.class, WebRepositoryPackageMarker.class })
public class ApplicationRepositoryConfiguration {

}
