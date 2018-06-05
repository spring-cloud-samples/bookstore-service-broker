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

package org.springframework.cloud.sample.bookstore.web.service;

import org.springframework.cloud.sample.bookstore.web.model.Book;
import org.springframework.cloud.sample.bookstore.web.model.BookStore;
import org.springframework.cloud.sample.bookstore.web.repository.BookStoreRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class BookStoreService {
	private BookStoreRepository repository;

	public BookStoreService(BookStoreRepository bookStoreRepository) {
		this.repository = bookStoreRepository;
	}

	public BookStore createBookStore(String storeId) {
		BookStore bookStore = new BookStore(storeId);

		return repository.save(bookStore);
	}

	public BookStore createBookStore() {
		return createBookStore(generateRandomId());
	}

	public BookStore getBookStore(String storeId) {
		Optional<BookStore> store = repository.findById(storeId);
		return store.orElseThrow(() -> new IllegalArgumentException("Invalid book store ID " + storeId + "."));
	}

	public void deleteBookStore(String id) {
		repository.deleteById(id);
	}

	public Book putBookInStore(String storeId, Book book) {
		String bookId = generateRandomId();
		Book bookWithId = new Book(bookId, book);

		BookStore store = getBookStore(storeId);
		store.addBook(bookWithId);

		repository.save(store);

		return bookWithId;
	}

	public Book getBookFromStore(String storeId, String bookId) {
		BookStore store = getBookStore(storeId);
		return store.getBookById(bookId)
				.orElseThrow(() -> new IllegalArgumentException("Invalid book ID " + storeId + ":" + bookId + "."));
	}

	public Book removeBookFromStore(String storeId, String bookId) {
		BookStore store = getBookStore(storeId);
		return store.remove(bookId)
				.orElseThrow(() -> new IllegalArgumentException("Invalid book ID " + storeId + ":" + bookId + "."));
	}

	private String generateRandomId() {
		return UUID.randomUUID().toString();
	}
}
