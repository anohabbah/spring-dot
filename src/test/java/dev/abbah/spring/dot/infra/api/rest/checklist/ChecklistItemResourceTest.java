package dev.abbah.spring.dot.infra.api.rest.checklist;

import dev.abbah.spring.dot.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@Sql(statements = "TRUNCATE TABLE checklist_item RESTART IDENTITY", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class ChecklistItemResourceTest {

    @Autowired
    MockMvcTester mvc;

    // --- US1: Create ---

    @Test
    void createItem_returnsCreatedWithIdAndNameAndUnchecked() {
        assertThat(mvc.post().uri("/v1/checklist")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"name": "Buy groceries"}
                        """))
                .hasStatus(201)
                .bodyJson()
                .extractingPath("$.id").isNotNull();

        assertThat(mvc.post().uri("/v1/checklist")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"name": "Buy groceries"}
                        """))
                .hasStatus(201)
                .bodyJson()
                .extractingPath("$.name").isEqualTo("Buy groceries");

        assertThat(mvc.post().uri("/v1/checklist")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"name": "Buy groceries"}
                        """))
                .hasStatus(201)
                .bodyJson()
                .extractingPath("$.checked").isEqualTo(false);
    }

    @Test
    void createSecondItem_getsItsOwnId() {
        var first = mvc.post().uri("/v1/checklist")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"name": "Buy groceries"}
                        """);

        var second = mvc.post().uri("/v1/checklist")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"name": "Walk the dog"}
                        """);

        assertThat(first).hasStatus(201);
        assertThat(second).hasStatus(201);

        assertThat(first).bodyJson().extractingPath("$.id").isNotNull();
        assertThat(second).bodyJson().extractingPath("$.id").isNotNull();
    }

    @Test
    void createItem_blankName_returns400() {
        assertThat(mvc.post().uri("/v1/checklist")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"name": "   "}
                        """))
                .hasStatus(400);
    }

    @Test
    void createItem_missingName_returns400() {
        assertThat(mvc.post().uri("/v1/checklist")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {}
                        """))
                .hasStatus(400);
    }

    // --- US2: View ---

    @Test
    void getAllItems_returnsListOfItems() {
        assertThat(mvc.post().uri("/v1/checklist")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"name": "Buy groceries"}
                        """)).hasStatus(201);
        assertThat(mvc.post().uri("/v1/checklist")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"name": "Walk the dog"}
                        """)).hasStatus(201);

        assertThat(mvc.get().uri("/v1/checklist"))
                .hasStatus(200)
                .bodyJson()
                .extractingPath("$.length()").isEqualTo(2);
    }

    @Test
    void getAllItems_emptyList_returns200WithEmptyArray() {
        assertThat(mvc.get().uri("/v1/checklist"))
                .hasStatus(200)
                .bodyJson()
                .isLenientlyEqualTo("[]");
    }

    @Test
    void getItemById_returnsItem() {
        assertThat(mvc.post().uri("/v1/checklist")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"name": "Buy groceries"}
                        """)).hasStatus(201);

        assertThat(mvc.get().uri("/v1/checklist/1"))
                .hasStatus(200)
                .bodyJson()
                .extractingPath("$.name").isEqualTo("Buy groceries");
    }

    @Test
    void getItemById_nonExistent_returns404() {
        assertThat(mvc.get().uri("/v1/checklist/999"))
                .hasStatus(404);
    }

    // --- US3: Update ---

    @Test
    void updateItem_name_returnsUpdatedItem() {
        assertThat(mvc.post().uri("/v1/checklist")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"name": "Buy groceries"}
                        """)).hasStatus(201);

        assertThat(mvc.put().uri("/v1/checklist/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"name": "Buy organic groceries", "checked": false}
                        """))
                .hasStatus(200)
                .bodyJson()
                .extractingPath("$.name").isEqualTo("Buy organic groceries");
    }

    @Test
    void updateItem_checkedStatus_returnsUpdatedItem() {
        assertThat(mvc.post().uri("/v1/checklist")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"name": "Buy groceries"}
                        """)).hasStatus(201);

        assertThat(mvc.put().uri("/v1/checklist/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"name": "Buy groceries", "checked": true}
                        """))
                .hasStatus(200)
                .bodyJson()
                .extractingPath("$.checked").isEqualTo(true);
    }

    @Test
    void updateItem_nonExistent_returns404() {
        assertThat(mvc.put().uri("/v1/checklist/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"name": "Does not exist", "checked": false}
                        """))
                .hasStatus(404);
    }

    @Test
    void updateItem_blankName_returns400() {
        assertThat(mvc.post().uri("/v1/checklist")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"name": "Buy groceries"}
                        """)).hasStatus(201);

        assertThat(mvc.put().uri("/v1/checklist/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"name": "   ", "checked": false}
                        """))
                .hasStatus(400);
    }

    // --- US4: Delete ---

    @Test
    void deleteItem_returnsNoContent() {
        assertThat(mvc.post().uri("/v1/checklist")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"name": "Buy groceries"}
                        """)).hasStatus(201);

        assertThat(mvc.delete().uri("/v1/checklist/1"))
                .hasStatus(204);
    }

    @Test
    void deleteItem_thenGetReturns404() {
        assertThat(mvc.post().uri("/v1/checklist")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"name": "Buy groceries"}
                        """)).hasStatus(201);

        assertThat(mvc.delete().uri("/v1/checklist/1"))
                .hasStatus(204);

        assertThat(mvc.get().uri("/v1/checklist/1"))
                .hasStatus(404);
    }

    @Test
    void deleteItem_nonExistent_returns404() {
        assertThat(mvc.delete().uri("/v1/checklist/999"))
                .hasStatus(404);
    }

    // --- Edge Cases ---

    @Test
    void createItem_nameExceeding255Chars_returns400() {
        var longName = "a".repeat(256);
        assertThat(mvc.post().uri("/v1/checklist")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"name": "%s"}
                        """.formatted(longName)))
                .hasStatus(400);
    }

    @Test
    void createItem_extraFields_handledGracefully() {
        assertThat(mvc.post().uri("/v1/checklist")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"name": "Buy groceries", "extra": "ignored", "priority": 5}
                        """))
                .hasStatus(201)
                .bodyJson()
                .extractingPath("$.name").isEqualTo("Buy groceries");
    }
}
