/*
 * Copyright 2002-2020 the original author or authors.
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

package org.springframework.cloud.sample.bookstore.web.security;

public final class SecurityAuthorities {

	private SecurityAuthorities() {

	}

	/**
	 * ROLE_ADMIN security authority.
	 */
	public static final String ADMIN = "ROLE_ADMIN";

	/**
	 * ROLE_FULL_ACCESS security authority.
	 */
	public static final String FULL_ACCESS = "ROLE_FULL_ACCESS";

	/**
	 * ROLE_READ_ONLY security authority.
	 */
	public static final String READ_ONLY = "ROLE_READ_ONLY";

	/**
	 * BOOK_STORE_ID_PREFIX security authority.
	 */
	public static final String BOOK_STORE_ID_PREFIX = "BOOK_STORE_";

}
