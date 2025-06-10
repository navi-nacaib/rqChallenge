package com.reliaquest.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.reliaquest.api.model.Employee;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

class EmployeeServiceTest {

    private static final String API_URL = "http://localhost:8112/api/v1/employee";
    private EmployeeService service;
    private RestTemplate restTemplate;
    private MockRestServiceServer mockServer;

    @BeforeEach
    void setUp() {
        restTemplate = new RestTemplate();
        mockServer = MockRestServiceServer.bindTo(restTemplate).build();
        service = new EmployeeService(restTemplate);
    }

    @Test
    void getAllEmployees_returnsList() {
        String json =
                """
            {
              "data":[
                {
                  "id":"1",
                  "employee_name":"Alice",
                  "employee_salary":100,
                  "employee_age":30,
                  "employee_title":"Dev",
                  "employee_email":"alice@co.com"
                },
                {
                  "id":"2",
                  "employee_name":"Bob",
                  "employee_salary":200,
                  "employee_age":40,
                  "employee_title":"QA",
                  "employee_email":"bob@co.com"
                }
              ],
              "status":"OK"
            }
            """;

        mockServer
                .expect(requestTo(API_URL))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

        List<Employee> list = service.getAllEmployees();
        assertThat(list).hasSize(2).extracting(Employee::getName).containsExactly("Alice", "Bob");

        mockServer.verify();
    }

    @Test
    void searchByName_filtersCaseInsensitive() {
        // stub getAllEmployees
        String json =
                """
            {
              "data":[
                {"id":"1","employee_name":"Anna","employee_salary":50,"employee_age":25,"employee_title":"A","employee_email":"a"},
                {"id":"2","employee_name":"annette","employee_salary":60,"employee_age":26,"employee_title":"A","employee_email":"b"},
                {"id":"3","employee_name":"Beth","employee_salary":70,"employee_age":27,"employee_title":"B","employee_email":"c"}
              ],
              "status":"OK"
            }
            """;
        mockServer
                .expect(requestTo(API_URL))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

        List<Employee> found = service.searchByName("Ann");
        assertThat(found).extracting(Employee::getName).containsExactly("Anna", "annette");

        mockServer.verify();
    }

    @Test
    void searchById_findsCorrectOne() {
        String json =
                """
            {
              "data":[
                {"id":"42","employee_name":"Carol","employee_salary":70,"employee_age":30,"employee_title":"P","employee_email":"carol"}
              ],
              "status":"OK"
            }
            """;
        mockServer
                .expect(requestTo(API_URL))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

        Optional<Employee> maybe = service.searchById("42");
        assertThat(maybe).isPresent().get().extracting(Employee::getName).isEqualTo("Carol");

        mockServer.verify();
    }

    @Test
    void getHighestSalary_computesMax() {
        String json =
                """
            {
              "data":[
                {"id":"1","employee_name":"X","employee_salary":100,"employee_age":20,"employee_title":"T","employee_email":"x"},
                {"id":"2","employee_name":"Y","employee_salary":999,"employee_age":21,"employee_title":"T","employee_email":"y"}
              ],
              "status":"OK"
            }
            """;
        mockServer
                .expect(requestTo(API_URL))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

        int max = service.getHighestSalary();
        assertThat(max).isEqualTo(999);

        mockServer.verify();
    }

    @Test
    void getTopTenHighestEarningEmployeeNames_sortsAndLimits() {
        // build 12 employees with salaries 0..11
        String items = IntStream.range(0, 12)
                .mapToObj(i -> String.format(
                        """
                            {
                              "id":"%d",
                              "employee_name":"E%d",
                              "employee_salary":%d,
                              "employee_age":30,
                              "employee_title":"T",
                              "employee_email":"e%d"
                            }
                            """,
                        i, i, i, i))
                .collect(Collectors.joining(","));
        String json = """
            { "data":[%s], "status":"OK" }
            """.formatted(items);

        mockServer
                .expect(requestTo(API_URL))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

        List<String> top10 = service.getTopTenHighestEarningEmployeeNames();
        assertThat(top10).hasSize(10).containsExactly("E11", "E10", "E9", "E8", "E7", "E6", "E5", "E4", "E3", "E2");

        mockServer.verify();
    }

    @Test
    void createEmployee_postsAndReturnsNew() {
        Employee input = new Employee();
        input.setName("Zed");
        input.setSalary(500);
        input.setAge(35);
        input.setTitle("Lead");
        input.setEmail("zed@co");

        // expected server wrapper JSON
        String empJson =
                """
            {
              "id":"99",
              "employee_name":"Zed",
              "employee_salary":500,
              "employee_age":35,
              "employee_title":"Lead",
              "employee_email":"zed@co"
            }
            """;
        String wrapper =
                """
            {
              "data":%s,
              "status":"Successfully processed request.",
              "error":null
            }
            """
                        .formatted(empJson);

        mockServer
                .expect(requestTo(API_URL))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                // request body must match CreateMockEmployeeInput JSON
                .andExpect(content()
                        .json(
                                """
                      {
                        "name":"Zed",
                        "salary":500,
                        "age":35,
                        "title":"Lead"
                      }
                  """,
                                false))
                .andRespond(withSuccess(wrapper, MediaType.APPLICATION_JSON));

        Employee created = service.createEmployee(input);
        assertThat(created.getId()).isEqualTo("99");
        assertThat(created.getName()).isEqualTo("Zed");

        mockServer.verify();
    }

    @Test
    void deleteEmployee_sendsDeleteWithBody() {
        String id = "abc-123";
        // stub DELETE; body is DeleteMockEmployeeInput { name: id }
        mockServer
                .expect(requestTo(API_URL))
                .andExpect(method(HttpMethod.DELETE))
                .andExpect(header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                .andExpect(content().json("""
                      { "name":"abc-123" }
                  """))
                .andRespond(withSuccess());

        // should not throw
        service.deleteEmployee(id);

        mockServer.verify();
    }
}
