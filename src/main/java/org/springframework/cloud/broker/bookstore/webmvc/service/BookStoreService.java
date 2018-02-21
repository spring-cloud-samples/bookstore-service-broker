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

package org.springframework.cloud.broker.bookstore.webmvc.service;

import org.springframework.cloud.broker.bookstore.webmvc.model.Book;
import org.springframework.cloud.broker.bookstore.webmvc.model.BookStore;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class BookStoreService {
	private final Map<String, BookStore> bookStoreById = new HashMap<>();

	public BookStore createBookStore(String storeId) {
		BookStore bookStore = new BookStore(storeId);
		bookStoreById.put(storeId, bookStore);
		return bookStore;
	}

	public BookStore createBookStore() {
		return createBookStore(generateRandomId());
	}

	public BookStore getBookStore(String storeId) {
		BookStore store = bookStoreById.get(storeId);
		if (store == null) {
			throw new IllegalArgumentException("Invalid book store ID " + storeId + ".");
		}
		return store;
	}

	public void deleteBookStore(String id) {
		bookStoreById.remove(id);
	}

	public Book putBookInStore(String storeId, Book book) {
		String bookId = generateRandomId();
		Book bookWithId = new Book(bookId, book);

		BookStore store = getBookStore(storeId);
		store.put(bookId, bookWithId);

		return bookWithId;
	}

	public Book getBookFromStore(String storeId, String bookId) {
		BookStore store = getBookStore(storeId);
		return store.get(bookId);
	}

	public Book removeBookFromStore(String storeId, String bookId) {
		BookStore store = getBookStore(storeId);
		return store.remove(bookId);
	}

	private String generateRandomId() {
		return UUID.randomUUID().toString();
	}
}
