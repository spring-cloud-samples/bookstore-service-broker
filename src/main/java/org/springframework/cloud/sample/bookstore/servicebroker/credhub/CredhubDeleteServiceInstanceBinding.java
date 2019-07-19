package org.springframework.cloud.sample.bookstore.servicebroker.credhub;

import org.springframework.cloud.servicebroker.model.binding.DeleteServiceInstanceBindingRequest;
import org.springframework.cloud.servicebroker.model.binding.DeleteServiceInstanceBindingResponse;
import org.springframework.credhub.core.CredHubOperations;
import org.springframework.credhub.support.CredentialName;
import org.springframework.credhub.support.ServiceInstanceCredentialName;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;

public class CredhubDeleteServiceInstanceBinding extends CredHubPersistingWorkflow {

	private static final Logger LOG = Loggers.getLogger(CredhubDeleteServiceInstanceBinding.class);

	private final CredHubOperations credHubOperations;

	public CredhubDeleteServiceInstanceBinding(CredHubOperations credHubOperations, String appName) {
		super(appName);
		this.credHubOperations = credHubOperations;
	}

	public Mono<DeleteServiceInstanceBindingResponse.DeleteServiceInstanceBindingResponseBuilder> buildResponse(DeleteServiceInstanceBindingRequest request, DeleteServiceInstanceBindingResponse.DeleteServiceInstanceBindingResponseBuilder responseBuilder) {
		LOG.debug("Preparing delete of credentials for service_id '{}' and binding_id '{}'", request.getServiceDefinitionId(), request.getBindingId());

		Mono<ServiceInstanceCredentialName> credentialNameMono = buildCredentialName(request.getServiceDefinitionId(), request.getBindingId());
		return credentialNameMono
			.filter(this::credentialExists)
			.map(this::deleteBindingCredentials)
			.thenReturn(responseBuilder);
	}

	private boolean credentialExists(CredentialName credentialName) {
		if (credentialName == null) {
			return false;
		}
		return !credHubOperations.credentials().findByName(credentialName).isEmpty();
	}

	private Mono<Void> deleteBindingCredentials(CredentialName credentialName) {
		LOG.debug("Deleting credentials with name '{}'", credentialName.getName());
		credHubOperations.credentials().deleteByName(credentialName);
		return Mono.never();
	}

}
