package com.test.reactive.dummy;

import static org.springframework.web.client.reactive.ClientWebRequestBuilders.get;
import static org.springframework.web.client.reactive.ResponseExtractors.body;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.client.reactive.ClientWebRequestBuilders;
import org.springframework.web.client.reactive.ResponseExtractors;
import org.springframework.web.client.reactive.WebClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.test.reactive.domain.Student;

import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class StudentControllerTest {

	private static final MediaType APPLICATION_JSON = MediaType.APPLICATION_JSON;
	private static final int Min = 0;
	private static final int Max = 100;
	private WebClient webClient;
	private MockWebServer server;

	@Before
	public void setup() {
		this.server = new MockWebServer();
		this.webClient = new WebClient(new ReactorClientHttpConnector());
	}

	@Test
	@Ignore
	public void test() {

		// Extract as string

		Mono<Student> result = this.webClient.perform(get("http://localhost:8080/students/1").accept(APPLICATION_JSON))
				.extract(body(Student.class));
		Student str = result.block();
		System.out.println(str.toString());

	}

	@Test
	@Ignore
	public void postTestReal() {

		Mono<Student> result1 = this.webClient
				.perform(ClientWebRequestBuilders.post("http://localhost:8080/students").body(new Student(20, "Tom"))
						.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
				.extract(body(Student.class));

		Student str1 = result1.block();

		System.out.println(str1.toString());
	}

	@Test
	@Ignore
	public void shouldGetJsonAsMonoOfPojo() throws Exception {

		okhttp3.HttpUrl baseUrl = server.url("/pojo");
		this.server.enqueue(new MockResponse().setHeader("Content-Type", "application/json")
				.setBody("{\"bar\":\"barbar\",\"foo\":\"foofoo\"}"));

		Mono<Pojo> result = this.webClient.perform(get(baseUrl.toString()).accept(MediaType.APPLICATION_JSON))
				.extract(body(Pojo.class));

		Pojo pojo = result.block();

		System.out.println(pojo.getBar());

	}

	@Test
	@Ignore
	public void getTest() throws JsonProcessingException {
		// Extract as string

		ObjectMapper objectMapper = new ObjectMapper();
		System.out.println(objectMapper.writeValueAsString(new Student(20, "Tom")));

		okhttp3.HttpUrl baseUrl = server.url("/students/1");

		this.server.enqueue(new MockResponse().setHeader("Content-Type", "application/json")
				.setBody(objectMapper.writeValueAsString(new Student(20, "Tom"))));

		Mono<Student> result = this.webClient.perform(get(baseUrl.toString()).accept(APPLICATION_JSON))
				.extract(body(Student.class));

		Student str = result.block();
		System.out.println(str.toString());

	}

	@Test
	@Ignore
	public void shouldPostPojoAsJson() throws Exception {

		okhttp3.HttpUrl baseUrl = server.url("/pojo/capitalize");
		this.server.enqueue(new MockResponse().setHeader("Content-Type", "application/json")
				.setBody("{\"bar\":\"BARBAR\",\"foo\":\"FOOFOO\"}"));

		Pojo spring = new Pojo("foofoo", "barbar");
		Mono<Pojo> result = this.webClient
				.perform(ClientWebRequestBuilders.post(baseUrl.toString()).body(spring)
						.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
				.extract(body(Pojo.class));

		Pojo pojo = result.block();

		System.out.println(pojo.getBar());

	}

	@Test
	@Ignore
	public void fluxTest() throws InterruptedException {

		CountDownLatch latch = new CountDownLatch(1);

		Flux<Student> result = this.webClient
				.perform(get("http://localhost:8080/students/list").accept(APPLICATION_JSON))
				.extract(ResponseExtractors.bodyStream(Student.class));

		/*
		 * result.subscribe(new Subscriber<Student>() { private Subscription s;
		 * @Override public void onSubscribe(Subscription s) { this.s=s; s.request(1); }
		 * @Override public void onNext(Student t) { s.request(1); System.out.println("Time--->"+new
		 * Date(System.currentTimeMillis())+"-->" + t.toString()); }
		 * @Override public void onError(Throwable t) { System.out.println("I am in error"); latch.countDown(); }
		 * @Override public void onComplete() { System.out.println("I am complete"); latch.countDown(); } });
		 */

		Iterator<Student> itr = result.toIterable().iterator();

		while (itr.hasNext()) {

			System.out.println("Time--->" + new Date(System.currentTimeMillis()) + "-->" + itr.next());
		}

		// latch.await();

	}

	@Test
	@Ignore
	public void shouldGetJsonAsFluxOfPojos() throws Exception {

		CountDownLatch latch = new CountDownLatch(1);

		HttpUrl baseUrl = server.url("/pojos");
		this.server
				.enqueue(new MockResponse().setHeader("Content-Type", "application/json").setBody(jsonStudentString()));

		Flux<Student> result = this.webClient.perform(get(baseUrl.toString()).accept(MediaType.APPLICATION_JSON))
				.extract(ResponseExtractors.bodyStream(Student.class));

		Subscriber<Student> subscriber = new Subscriber<Student>() {

			Subscription s;

			@Override
			public void onSubscribe(Subscription s) {
				this.s = s;
				s.request(1);
			}

			@Override
			public void onNext(Student t) {
				System.out.println(t.toString());
				s.request(1);
			}

			@Override
			public void onError(Throwable t) {
				System.out.println("I am in error");
				latch.countDown();
			}

			@Override
			public void onComplete() {
				System.out.println("I am complete");
				latch.countDown();
			}
		};

		Mono<List<Student>> mono = result.collectList();
		mono.subscribe(new Subscriber<List<Student>>() {

			@Override
			public void onSubscribe(Subscription s) {
				s.request(Long.MAX_VALUE);

			}

			@Override
			public void onNext(List<Student> t) {
				t.stream().forEach(System.out::println);
			}

			@Override
			public void onError(Throwable t) {
				System.out.println("I am in error");
				latch.countDown();

			}

			@Override
			public void onComplete() {
				System.out.println("I am complete");
				latch.countDown();
			}

		});

		latch.await();
	}

	@Test
	@Ignore
	public void postTestRealMono() {

		Mono<Student> result1 = this.webClient.perform(
				ClientWebRequestBuilders.post("http://localhost:8080/students/mono").body(new Student(20, "Tom"))
						.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
				.extract(body(Student.class));

		Student str1 = result1.block();

		System.out.println(str1.toString());
	}

	private String jsonStudentString() throws JsonProcessingException {

		List<Student> list = new ArrayList<>();

		for (int i = 0; i < 30; i++) {
			list.add(new Student(i, "Tom" + i));
		}

		ObjectMapper mapper = new ObjectMapper();

		System.out.println(mapper.writeValueAsString(list));

		return mapper.writeValueAsString(list);
	}

	Flux<Student> fluxStudent = Flux.create(emitter -> {

		for (int i = 0; i < 300; i++) {

			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			System.out.println("I am emitting new student-->" + new Date(System.currentTimeMillis()));
			emitter.next(new Student(i, "i"));
		}

		System.out.println("I am complete" + new Date(System.currentTimeMillis()));
		emitter.complete();
	});

}
