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
import org.springframework.cloud.broker.bookstore.webmvc.resource.BookResource;
import org.springframework.cloud.broker.bookstore.webmvc.resource.BookResourceAssembler;
import org.springframework.cloud.broker.bookstore.webmvc.service.BookStoreService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/bookstores/{bookStoreId}/books")
public class BookController extends BaseController {
	private final BookStoreService bookStoreService;

	public BookController(BookStoreService bookStoreService) {
		this.bookStoreService = bookStoreService;
	}

	@PutMapping
	public ResponseEntity<BookResource> addBook(@PathVariable String bookStoreId, @RequestBody Book book) {
		Book savedBook = bookStoreService.putBookInStore(bookStoreId, book);
		return createResponse(bookStoreId, savedBook, HttpStatus.CREATED);
	}

	@GetMapping("/{bookId}")
	public ResponseEntity<BookResource> getBook(@PathVariable String bookStoreId, @PathVariable String bookId) {
		Book book = bookStoreService.getBookFromStore(bookStoreId, bookId);
		return createResponse(bookStoreId, book, HttpStatus.OK);
	}

	@DeleteMapping("/{bookId}")
	public ResponseEntity<BookResource> deleteBook(@PathVariable String bookStoreId, @PathVariable String bookId) {
		Book book = bookStoreService.removeBookFromStore(bookStoreId, bookId);
		return createResponse(bookStoreId, book, HttpStatus.OK);
	}

	private ResponseEntity<BookResource> createResponse(String bookStoreId, Book book, HttpStatus httpStatus) {
		BookResource bookResource = new BookResourceAssembler().toResource(book, bookStoreId);
		return new ResponseEntity<>(bookResource, httpStatus);
	}
}