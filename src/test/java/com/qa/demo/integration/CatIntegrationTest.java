package com.qa.demo.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qa.demo.domain.Cat;;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Sql(scripts = { "classpath:cat-schema.sql",
		"classpath:cat-data.sql" }, 
		executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
public class CatIntegrationTest {

	@Autowired
	private MockMvc mvc;

	@Autowired
	private ObjectMapper mapper;

	@Test
	void testCreate() throws Exception {
		Cat newCat = new Cat("Chairman Meow", true, true, 12);
		String newCatAsJson = this.mapper.writeValueAsString(newCat);
		RequestBuilder req = post("/create").content(newCatAsJson).contentType(MediaType.APPLICATION_JSON);

		ResultMatcher checkStatus = MockMvcResultMatchers.status().isCreated();
		Cat created = new Cat(2, "Chairman Meow", true, true, 12);
		String createdAsJson = this.mapper.writeValueAsString(created);
		ResultMatcher checkBody = content().json(createdAsJson);

		this.mvc.perform(req).andExpect(checkStatus).andExpect(checkBody);
	}

	@Test
	void testGet() throws Exception {
		this.mvc.perform(get("/get/1")).andExpect(status().isOk()).andExpect(
				content().json(this.mapper.writeValueAsString(new Cat(1, "Mr Bigglesworth", true, true, 27))));
	}

	@Test
	void testGetAll() throws Exception {
		this.mvc.perform(get("/getAll")).andExpect(status().isOk()).andExpect(
				content().json(this.mapper.writeValueAsString(List.of(new Cat(1, "Mr Bigglesworth", true, true, 27)))));
	}

	@Test
	void testDelete() throws Exception {
		this.mvc.perform(delete("/remove/1")).andExpect(status().isOk()).andExpect(
				content().json(this.mapper.writeValueAsString(new Cat(1, "Mr Bigglesworth", true, true, 27))));
	}

	@Test
	void testUpdate() throws Exception {
		this.mvc.perform(patch("/update/1").queryParam("name", "tiddles")).andExpect(status().isOk())
				.andExpect(content().json(this.mapper.writeValueAsString(new Cat(1, "tiddles", true, true, 27))));
	}

}
