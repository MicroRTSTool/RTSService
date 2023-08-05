//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.web.client.TestRestTemplate;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertTrue;
//
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//class RTSControllerTest {
//
//        @Autowired
//        private TestRestTemplate restTemplate;
//
//        @Test
//        void configurationTest() {
//            ResponseEntity<String> response = restTemplate.getForEntity("/configured-repos?repoName=Dilhasha/hipstershop&" +
//                    "pr=5&observabilityURL=http://34.136.11.235", String.class);
//            assertEquals(HttpStatus.OK, response.getStatusCode());
//            assertTrue(response.getBody().contains("Successfully configured Repo"));
//        }
//
//}