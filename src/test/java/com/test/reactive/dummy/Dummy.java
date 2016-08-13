package com.test.reactive.dummy;

import static org.springframework.web.client.reactive.ClientWebRequestBuilders.get;
import static org.springframework.web.client.reactive.ResponseExtractors.body;

import java.util.concurrent.CountDownLatch;

import org.junit.Before;
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

public class Dummy {

	private static final MediaType APPLICATION_JSON = MediaType.APPLICATION_JSON;
	private WebClient webClient;
	private MockWebServer server;

	@Before
	public void setup() {
		this.server = new MockWebServer();
		this.webClient = new WebClient(new ReactorClientHttpConnector());
	}

	@Test
	public void test() {

		// Extract as string

		Mono<Student> result = this.webClient.perform(get("http://localhost:8080/students/1").accept(APPLICATION_JSON))
				.extract(body(Student.class));
		Student str = result.block();
		System.out.println(str.toString());

	}

	@Test
	public void postTestReal() {

		Mono<Student> result1 = this.webClient
				.perform(ClientWebRequestBuilders.post("http://localhost:8080/students").body(new Student(20, "Tom"))
						.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
				.extract(body(Student.class));

		Student str1 = result1.block();

		System.out.println(str1.toString());
	}

	@Test
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
	public void fluxTest() throws InterruptedException {

		CountDownLatch latch = new CountDownLatch(1);

		Flux<Student> result = this.webClient
				.perform(get("http://localhost:8080/students/list").accept(APPLICATION_JSON))
				.extract(ResponseExtractors.bodyStream(Student.class));

		result.subscribe(new Subscriber<Student>() {

			@Override
			public void onSubscribe(Subscription s) {
				s.request(Long.MAX_VALUE);
			}

			@Override
			public void onNext(Student t) {
				System.out.println(t.toString());
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
	public void shouldGetJsonAsFluxOfPojos() throws Exception {

		CountDownLatch latch = new CountDownLatch(1);

		HttpUrl baseUrl = server.url("/pojos");
		this.server.enqueue(new MockResponse().setHeader("Content-Type", "application/json")
				.setBody("[{\"bar\":\"bar1\",\"foo\":\"foo1\"},{\"bar\":\"bar2\",\"foo\":\"foo2\"}]"));

		Flux<Pojo> result = this.webClient.perform(get(baseUrl.toString()).accept(MediaType.APPLICATION_JSON))
				.extract(ResponseExtractors.bodyStream(Pojo.class));

		
		
		Subscriber<Pojo> subscriber=  new Subscriber<Pojo>() {

			private Subscription s;
			
			@Override
			public void onSubscribe(Subscription s) {
				this.s=s;
				s.request(1);
			}

			@Override
			public void onNext(Pojo t) {
				System.out.println(t.toString());
				//s.cancel();
				//latch.countDown();
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
		
		result.subscribe(subscriber);
		
		latch.await();
	}

	@Test
	public void postTestRealMono() {

		Mono<Student> result1 = this.webClient.perform(
				ClientWebRequestBuilders.post("http://localhost:8080/students/mono").body(new Student(20, "Tom"))
						.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
				.extract(body(Student.class));

		Student str1 = result1.block();

		System.out.println(str1.toString());
	}

}
