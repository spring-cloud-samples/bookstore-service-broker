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

import org.springframework.cloud.broker.bookstore.webmvc.model.BookStore;
import org.springframework.cloud.broker.bookstore.webmvc.resource.BookStoreResource;
import org.springframework.cloud.broker.bookstore.webmvc.resource.BookStoreResourceAssembler;
import org.springframework.cloud.broker.bookstore.webmvc.service.BookStoreService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/bookstores")
public class BookStoreController extends BaseController {
	private final BookStoreService bookStoreService;

	public BookStoreController(BookStoreService bookStoreService) {
		this.bookStoreService = bookStoreService;
	}

	@PutMapping
	public ResponseEntity<BookStoreResource> addBooks() {
		BookStore bookStore = bookStoreService.createBookStore();
		return createResponse(bookStore);
	}

	@GetMapping("/{bookStoreId}")
	public ResponseEntity<BookStoreResource> getBooks(@PathVariable String bookStoreId) {
		BookStore bookStore = bookStoreService.getBookStore(bookStoreId);
		return createResponse(bookStore);
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<Map<String, String>> badBookStoreId(IllegalArgumentException e) {
		return super.badBookStoreId(e);
	}

	private ResponseEntity<BookStoreResource> createResponse(BookStore bookStore) {
		BookStoreResource bookStoreResource = new BookStoreResourceAssembler().toResource(bookStore);
		return new ResponseEntity<>(bookStoreResource, HttpStatus.OK);
	}
}