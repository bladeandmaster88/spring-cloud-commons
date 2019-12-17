/*
 * Copyright 2012-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.loadbalancer.core;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.core.env.Environment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link ZonePreferenceServiceInstanceListSupplier}.
 *
 * @author Olga Maciaszek-Sharma
 */
class ZonePreferenceServiceInstanceListSupplierTests {

	private DiscoveryClientServiceInstanceListSupplier delegate = mock(
			DiscoveryClientServiceInstanceListSupplier.class);

	private Environment environment = mock(Environment.class);

	private ZonePreferenceServiceInstanceListSupplier supplier = new ZonePreferenceServiceInstanceListSupplier(
			delegate, environment);

	private ServiceInstance first = serviceInstance("test-1", "zone1");

	private ServiceInstance second = serviceInstance("test-2", "zone1");

	private ServiceInstance third = serviceInstance("test-3", "zone2");

	private ServiceInstance fourth = serviceInstance("test-4", "zone3");

	@Test
	void shouldFilterInstancesByZone() {
		when(environment.getProperty("spring.cloud.loadbalancer.zone"))
				.thenReturn("zone1");
		when(delegate.get()).thenReturn(Flux.just(Arrays.asList(first, second, third)));

		List<ServiceInstance> filtered = supplier.get().blockFirst();

		assertThat(filtered).hasSize(2);
		assertThat(filtered).contains(first, second);
		assertThat(filtered).doesNotContain(third);
		assertThat(filtered).doesNotContain(fourth);
	}

	@Test
	void shouldReturnAllInstancesIfNoZoneInstances() {
		when(environment.getProperty("spring.cloud.loadbalancer.zone"))
				.thenReturn("zone1");
		when(delegate.get()).thenReturn(Flux.just(Arrays.asList(third, fourth)));

		List<ServiceInstance> filtered = supplier.get().blockFirst();

		assertThat(filtered).hasSize(2);
		assertThat(filtered).contains(third, fourth);
	}

	@Test
	void shouldReturnAllInstancesIfNoZone() {
		when(environment.getProperty("spring.cloud.loadbalancer.zone")).thenReturn(null);
		when(delegate.get())
				.thenReturn(Flux.just(Arrays.asList(first, second, third, fourth)));

		List<ServiceInstance> filtered = supplier.get().blockFirst();

		assertThat(filtered).hasSize(4);
		assertThat(filtered).contains(first, second, third, fourth);
	}

	private DefaultServiceInstance serviceInstance(String instanceId, String zone1) {
		return new DefaultServiceInstance("test", instanceId, "http://test.test", 9080,
				false, buildZoneMetadata(zone1));
	}

	private Map<String, String> buildZoneMetadata(String zone) {
		Map<String, String> metadata = new HashMap<>();
		metadata.put("zone", zone);
		return metadata;
	}

}
