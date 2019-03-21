/*
 * Copyright 2002-2018 the original author or authors.
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

package org.springframework.cloud.sample.bookstore.web.service;

import org.springframework.cloud.sample.bookstore.web.model.Book;
import org.springframework.cloud.sample.bookstore.web.model.BookStore;
import org.springframework.cloud.sample.bookstore.web.repository.BookStoreRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.UUID;

@Service
public class BookStoreService {
	private BookStoreRepository repository;

	public BookStoreService(BookStoreRepository bookStoreRepository) {
		this.repository = bookStoreRepository;
	}

	public Mono<BookStore> createBookStore(String storeId) {
		BookStore bookStore = new BookStore(storeId);

		return repository.save(bookStore);
	}

	public Mono<BookStore> createBookStore() {
		return createBookStore(generateRandomId());
	}

	public Mono<BookStore> getBookStore(String storeId) {
		return repository.findById(storeId);
	}

	public void deleteBookStore(String id) {
		repository.deleteById(id);
	}

	public Book putBookInStore(String storeId, Book book) {
		String bookId = generateRandomId();
		Book bookWithId = new Book(bookId, book);

		getBookStore(storeId).subscribe(store -> {
			store.addBook(bookWithId);
			repository.save(store);
		});

		return bookWithId;
	}

	public Optional<Book> getBookFromStore(String storeId, String bookId) {
		return getBookStore(storeId).map(store -> store.getBookById(bookId));
	}

	public Book removeBookFromStore(String storeId, String bookId) {
		getBookStore(storeId)
			.doOnError(() -> throw new IllegalArgumentException("Invalid book ID " + storeId + ":" + bookId + "."))
			.subscribe(store -> store.remove(bookId));
	}

	private String generateRandomId() {
		return UUID.randomUUID().toString();
	}
}
