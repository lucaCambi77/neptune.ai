package com.neptune.ai.task;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

/** Integeration tests for {@link TaskController} */
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class IntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Test
  void shouldGetStats() throws Exception {

    String point =
        """
        {
          "symbol" : "ABC",
          "values" : [10.0,10.0,20.0,40.0,20.0,20.0,20.0,20.0,20.0,20.0]
        }
""";

    this.mockMvc
        .perform(post("/add_batch").content(point).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isCreated());

    this.mockMvc
        .perform(
            get("/stats")
                .param("symbol", "ABC")
                .param("k", "10")
                .contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(
            content()
                .json(
                    """
                        {"min": 10.0,"max": 40.0,"last": 20.0,"avg":20.0,"var": 60.0}"""));
  }

  @Test
  void shouldThrowWhenStatsNotAvailableForSymbol() throws Exception {
    this.mockMvc
        .perform(
            get("/stats")
                .param("symbol", "ABC")
                .param("k", "4")
                .contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isBadRequest());
  }

  @Test
  void shouldAddPoint() throws Exception {
    String point =
        """
        {
          "symbol" : "ABC",
          "value" : 10.0
        }
""";

    this.mockMvc
        .perform(post("/add").content(point).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isCreated())
        .andExpect(
            content()
                .json(
                    """
  {"sequence":1,"symbol":"ABC","value":10.0}"""));
  }

  @Test
  void shouldThrowWhenKIsGreaterThanPointListSize() throws Exception {
    String point =
        """
        {
          "symbol" : "ABC",
          "value" : 10.0
        }
""";

    this.mockMvc
        .perform(post("/add").content(point).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isCreated());

    this.mockMvc
        .perform(
            get("/stats")
                .param("symbol", "ABC")
                .param("k", "4")
                .contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isBadRequest());
  }

  @Test
  void shouldThrowWhenKIsGreaterThanMAxValueAllowed() throws Exception {
    this.mockMvc
        .perform(
            get("/stats")
                .param("symbol", "ABC")
                .param("k", "100000000")
                .contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isBadRequest());
  }

  @Test
  void shouldAddPoints() throws Exception {
    String point =
        """
        {
          "symbol" : "ABC",
          "values" : [10.0 ,20.0]
        }
""";

    this.mockMvc
        .perform(post("/add_batch").content(point).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isCreated())
        .andExpect(
            content()
                .json(
                    """
                        [{"sequence":1,"symbol":"ABC","value":10.0}
                        ,{"sequence":2,"symbol":"ABC","value":20.0}
                        ]"""));
  }
}
