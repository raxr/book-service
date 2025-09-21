package com.th.ascend.book;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class BookControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private BookRepository bookRepository;

    private String baseUrl(String path) {
        return "http://localhost:" + port + "/api/v1/books" + path;
    }

    @BeforeEach
    void clean() {
        bookRepository.deleteAll();
    }

    @Test
    void givenPostCreateBook_persistsAndReturnsCreatedBook() {
        RequestBook request = new RequestBook(
                "Nineteen Eighty-Four",
                "George Orwell",
                "1949-06-08",
                "CE"
        );

        ResponseEntity<ResponseBook> response = restTemplate.postForEntity(baseUrl(""), request, ResponseBook.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ResponseBook body = response.getBody();
        assertNotNull(body);
        assertTrue(body.id() > 0);
        assertEquals("Nineteen Eighty-Four", body.title());
        assertEquals("George Orwell", body.author());
        assertEquals(LocalDate.of(1949, 6, 8), body.publishedDate());

        List<BookEntity> all = bookRepository.findAll();
        assertEquals(1, all.size());
        BookEntity saved = all.getFirst();
        assertEquals("Nineteen Eighty-Four", saved.getTitle());
        assertEquals("George Orwell", saved.getAuthor());
        assertEquals(LocalDate.of(1949, 6, 8), saved.getPublishedDate());
    }

    @Test
    void givenGetBooksByAuthor_returnsSortedByPublishedDateDesc() {
        BookEntity b1 = new BookEntity();
        b1.setTitle("The Great Gatsby");
        b1.setAuthor("F. Scott Fitzgerald");
        b1.setPublishedDate(LocalDate.of(2020, 1, 15));
        BookEntity b2 = new BookEntity();
        b2.setTitle("To Kill a Mockingbird");
        b2.setAuthor("Harper Lee");
        b2.setPublishedDate(LocalDate.of(2019, 3, 20));
        BookEntity b3 = new BookEntity();
        b3.setTitle("1984");
        b3.setAuthor("George Orwell");
        b3.setPublishedDate(LocalDate.of(2021, 6, 10));
        BookEntity b4 = new BookEntity();
        b4.setTitle("Pride and Prejudice");
        b4.setAuthor("Jane Austen");
        b4.setPublishedDate(LocalDate.of(2018, 11, 25));
        BookEntity b5 = new BookEntity();
        b5.setTitle("Animal Farm");
        b5.setAuthor("George Orwell");
        b5.setPublishedDate(LocalDate.of(2020, 8, 5));
        BookEntity b6 = new BookEntity();
        b6.setTitle("The Catcher in the Rye");
        b6.setAuthor("J.D. Salinger");
        b6.setPublishedDate(LocalDate.of(2022, 2, 28));
        BookEntity b7 = new BookEntity();
        b7.setTitle("Brave New World");
        b7.setAuthor("Aldous Huxley");
        b7.setPublishedDate(LocalDate.of(2021, 12, 1));
        BookEntity b8 = new BookEntity();
        b8.setTitle("The Lord of the Rings");
        b8.setAuthor("J.R.R. Tolkien");
        b8.setPublishedDate(LocalDate.of(2023, 4, 15));
        BookEntity b9 = new BookEntity();
        b9.setTitle("Harry Potter and the Philosopher's Stone");
        b9.setAuthor("J.K. Rowling");
        b9.setPublishedDate(LocalDate.of(2023, 7, 20));
        BookEntity b10 = new BookEntity();
        b10.setTitle("Nineteen Eighty-Four");
        b10.setAuthor("George Orwell");
        b10.setPublishedDate(LocalDate.of(2022, 9, 10));

        bookRepository.saveAll(Arrays.asList(b1, b2, b3, b4, b5, b6, b7, b8, b9, b10));

        RequestBookByAuthor request = new RequestBookByAuthor("George Orwell");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<RequestBookByAuthor> httpEntity = new HttpEntity<>(request, headers);

        ResponseEntity<ResponseBook[]> response = restTemplate.exchange(
                baseUrl(""), HttpMethod.GET, httpEntity, ResponseBook[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        ResponseBook[] body = response.getBody();
        assertNotNull(body);
        assertEquals(3, body.length);

        // Expect sorted by published_date DESC
        assertEquals("Nineteen Eighty-Four", body[0].title());
        assertEquals(LocalDate.of(2022, 9, 10), body[0].publishedDate());
        assertEquals("1984", body[1].title());
        assertEquals(LocalDate.of(2021, 6, 10), body[1].publishedDate());
        assertEquals("Animal Farm", body[2].title());
        assertEquals(LocalDate.of(2020, 8, 5), body[2].publishedDate());
    }

    @Test
    void givenPostCreateBook_withInvalidYear_returnsBadRequest() {
        RequestBook invalid = new RequestBook(
                "Some Title",
                "An Author",
                "0999-01-01",
                "CE"
        );

        ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(
                baseUrl(""),
                invalid,
                ErrorResponse.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().message().contains("out of valid range"));
    }

    @Test
    void givenPostCreateBook_withBuddhistEraDate_convertsToGregorianAndPersists() {
        String json = """
                {
                  "title": "BE Date Book",
                  "author": "Thai Author",
                  "publishedDate": "2567-03-15",
                  "calendar": "BE"
                }
                """;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(json, headers);

        ResponseEntity<ResponseBook> response = restTemplate.postForEntity(baseUrl(""), entity, ResponseBook.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        ResponseBook body = response.getBody();
        assertNotNull(body);
        assertEquals("BE Date Book", body.title());
        assertEquals("Thai Author", body.author());
        assertEquals(LocalDate.of(2024, 3, 15), body.publishedDate());

        List<BookEntity> all = bookRepository.findAll();
        assertEquals(1, all.size());
        BookEntity saved = all.getFirst();
        assertEquals(LocalDate.of(2024, 3, 15), saved.getPublishedDate());
    }

    @Test
    void givenPostCreateBook_withLeapDayInCE_persists() {
        RequestBook request = new RequestBook(
                "Leap CE",
                "Tester",
                "2024-02-29",
                "CE"
        );
        ResponseEntity<ResponseBook> response = restTemplate.postForEntity(baseUrl(""), request, ResponseBook.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ResponseBook body = response.getBody();
        assertNotNull(body);
        assertEquals(LocalDate.of(2024, 2, 29), body.publishedDate());

        List<BookEntity> all = bookRepository.findAll();
        assertEquals(1, all.size());
        assertEquals(LocalDate.of(2024, 2, 29), all.getFirst().getPublishedDate());
    }

    @Test
    void givenPostCreateBook_withLeapDayInBE_convertsAndPersists() {
        String json = """
                {
                  "title": "Leap BE",
                  "author": "Tester",
                  "publishedDate": "2563-02-29",
                  "calendar": "BE"
                }
                """;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(json, headers);

        ResponseEntity<ResponseBook> response = restTemplate.postForEntity(baseUrl(""), entity, ResponseBook.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ResponseBook body = response.getBody();
        assertNotNull(body);
        assertEquals(LocalDate.of(2020, 2, 29), body.publishedDate());

        List<BookEntity> all = bookRepository.findAll();
        assertEquals(1, all.size());
        assertEquals(LocalDate.of(2020, 2, 29), all.getFirst().getPublishedDate());
    }

    @Test
    void givenPostCreateBook_withNonLeapDayInCE_returnsBadRequest() {
        RequestBook invalid = new RequestBook(
                "Bad Leap CE",
                "Tester",
                "2023-02-29",
                "CE"
        );
        ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(baseUrl(""), invalid, ErrorResponse.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().message().toLowerCase().contains("leap year"));
    }

    @Test
    void givenPostCreateBook_withNonLeapDayInBE_returnsBadRequest() {
        String json = """
                {
                  "title": "Bad Leap BE",
                  "author": "Tester",
                  "publishedDate": "2562-02-29",
                  "calendar": "BE"
                }
                """;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(json, headers);

        ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(baseUrl(""), entity, ErrorResponse.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().message().toLowerCase().contains("leap year"));
    }
}
