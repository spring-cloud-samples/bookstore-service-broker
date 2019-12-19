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

import java.util.UUID;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import org.springframework.cloud.sample.bookstore.web.model.Book;
import org.springframework.cloud.sample.bookstore.web.model.BookStore;
import org.springframework.cloud.sample.bookstore.web.repository.BookStoreRepository;
import org.springframework.stereotype.Service;

@Service
public class BookStoreService {

	private final BookStoreRepository repository;

	public BookStoreService(BookStoreRepository bookStoreRepository) {
		this.repository = bookStoreRepository;
	}

	public Mono<BookStore> createBookStore(String storeId) {
		return Mono.fromCallable(() -> repository.save(new BookStore(storeId)))
			.publishOn(Schedulers.boundedElastic());
	}

	public Mono<BookStore> createBookStore() {
		return generateRandomId()
			.flatMap(this::createBookStore);
	}

	public Mono<BookStore> getBookStore(String storeId) {
		return Mono.fromCallable(() -> repository.findById(storeId))
			.publishOn(Schedulers.boundedElastic())
			.flatMap(Mono::justOrEmpty)
			.switchIfEmpty(Mono.error(new IllegalArgumentException("Invalid book store ID " + storeId + ".")));
	}

	public Mono<Void> deleteBookStore(String id) {
		return Mono.fromCallable(() -> {
			repository.deleteById(id);
			return null;
		})
			.publishOn(Schedulers.boundedElastic())
			.then();
	}

	public Mono<Book> putBookInStore(String storeId, Book book) {
		return generateRandomId()
			.flatMap(bookId -> Mono.just(new Book(bookId, book)))
			.flatMap(bookWithId -> getBookStore(storeId)
				.flatMap(store -> Mono.fromCallable(() -> {
					store.addBook(bookWithId);
					repository.save(store);
					return bookWithId;
				})
					.publishOn(Schedulers.boundedElastic())));
	}

	public Mono<Book> getBookFromStore(String storeId, String bookId) {
		return getBookStore(storeId)
			.flatMap(store -> Mono.justOrEmpty(store.getBookById(bookId)))
			.switchIfEmpty(Mono.error(new IllegalArgumentException("Invalid book ID " + storeId + ":" + bookId + ".")));
	}

	public Mono<Book> removeBookFromStore(String storeId, String bookId) {
		return getBookStore(storeId)
			.flatMap(store -> Mono.justOrEmpty(store.remove(bookId))
				.switchIfEmpty(
					Mono.error(new IllegalArgumentException("Invalid book ID " + storeId + ":" + bookId + ".")))
				.flatMap(book -> Mono.fromCallable(() -> {
					repository.save(store);
					return book;
				})
					.publishOn(Schedulers.boundedElastic())));
	}

	private Mono<String> generateRandomId() {
		return Mono.fromCallable(() -> UUID.randomUUID().toString())
			.publishOn(Schedulers.boundedElastic());
	}

}
