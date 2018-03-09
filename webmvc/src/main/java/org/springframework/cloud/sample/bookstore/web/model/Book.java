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

package org.springframework.cloud.sample.bookstore.web.model;

import org.springframework.hateoas.Identifiable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class Book implements Identifiable<String> {
	@Column(length = 50)
	private final String id;

	@Column(length = 20)
	private final String isbn;

	@Column(length = 100)
	private final String title;

	@Column(length = 100)
	private final String author;

	@SuppressWarnings("unused")
	private Book() {
		this.id = null;
		this.isbn = null;
		this.title = null;
		this.author = null;
	}

	public Book(String isbn, String title, String author) {
		this.id = null;
		this.isbn = isbn;
		this.title = title;
		this.author = author;
	}

	public Book(String id, Book book) {
		this.id = id;
		this.isbn = book.getIsbn();
		this.title = book.getTitle();
		this.author = book.getAuthor();
	}

	@Override
	public String getId() {
		return this.id;
	}

	public String getIsbn() {
		return this.isbn;
	}

	public String getTitle() {
		return this.title;
	}

	public String getAuthor() {
		return this.author;
	}
}
