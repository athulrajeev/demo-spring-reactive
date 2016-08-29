package com.test.reactive.controller;

import java.util.Arrays;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.test.reactive.domain.Student;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(value = "/students")
public class StudentController {

	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
	public Mono<Student> get(@PathVariable String id) {
		return Mono.just(new Student(20, "Tom"));
	}

	@RequestMapping(value = "/list", method = RequestMethod.GET)
	public Flux<Student> list() {
		return Flux.fromIterable(Arrays.asList(new Student(20, "tom"), new Student(21, "tom"), new Student(22, "tom"),
				new Student(20, "tom")));
	}

	@RequestMapping(method = RequestMethod.POST)
	public Mono<Student> post(@RequestBody Student student) {
		return Mono.just(new Student(20, "Tom"));
	}

	@RequestMapping(value = "/hello", method = RequestMethod.GET)
	public String string() {
		return "Hello World";
	}

	@RequestMapping(value = "/mono", method = RequestMethod.POST)
	public Mono<Student> getNonMono(@RequestBody Mono<Student> student) {

		return student.map(t -> {
			return new Student(20, t.getName().toUpperCase());
		});
	}
}
