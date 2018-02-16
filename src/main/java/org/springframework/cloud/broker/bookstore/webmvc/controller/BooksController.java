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

package org.springframework.cloud.broker.bookstore.webmvc.controller;

import org.springframework.cloud.broker.bookstore.webmvc.model.Book;
import org.springframework.cloud.broker.bookstore.webmvc.service.BookStoreService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/bookstore")
public class BooksController {
	private final BookStoreService bookStoreService;

	public BooksController(BookStoreService bookStoreService) {
		this.bookStoreService = bookStoreService;
	}

	@GetMapping("/{bookStoreId}/books/{bookId}")
	public Map<String, Book> get(@PathVariable String bookStoreId, @PathVariable String bookId) {
		return Collections.singletonMap(bookId, bookStoreService.getBookFromStore(bookStoreId, bookId));
	}

	@PutMapping("/{bookStoreId}/books/{bookId}")
	public Map<String, Book> put(@PathVariable String bookStoreId, @PathVariable String bookId, @RequestBody Book book) {
		bookStoreService.putBookInStore(bookStoreId, bookId, book);
		return Collections.singletonMap(bookId, bookStoreService.getBookFromStore(bookStoreId, bookId));
	}

	@DeleteMapping("/{bookStoreId}/books/{bookId}")
	public Map<String, Book> delete(@PathVariable String bookStoreId, @PathVariable String bookId) {
		return Collections.singletonMap(bookId, bookStoreService.removeBookFromStore(bookStoreId, bookId));
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<Map<String, String>> badInstanceId(IllegalArgumentException e) {
		Map<String, String> responseBody = Collections.singletonMap("errorMessage", e.getMessage());
		return new ResponseEntity<>(responseBody, HttpStatus.BAD_REQUEST);
	}
}
