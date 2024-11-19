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

package org.springframework.cloud.sample.bookstore.web.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class BookStore {

	@Id
	private String id;

	private List<Book> books = new ArrayList<>();

	@SuppressWarnings("unused")
	private BookStore() {
		this.id = null;
	}

	public BookStore(String id) {
		this.id = id;
	}

	public String getId() {
		return this.id;
	}

	public List<Book> getBooks() {
		return this.books;
	}

	public void addBook(Book book) {
		this.books.add(book);
	}

	public Optional<Book> getBookById(String bookId) {
		return this.books.stream().filter((book) -> book.getId().equals(bookId)).findFirst();
	}

	public Optional<Book> remove(String bookId) {
		Optional<Book> book = getBookById(bookId);

		book.ifPresent(this.books::remove);

		return book;
	}

}
