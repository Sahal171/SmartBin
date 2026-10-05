package com.smartbin;

import com.smartbin.dto.RetrieveRequest;
import com.smartbin.dto.TextMessageRequest;
import com.smartbin.service.TransferCodeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SmartBinApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TransferCodeService codeService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Context loads successfully")
    void contextLoads() {
    }

    @Test
    @DisplayName("Transfer code is 6 characters alphanumeric and unambiguous")
    void testCodeGeneration() {
        for (int i = 0; i < 50; i++) {
            String code = codeService.generateUniqueCode();
            assertNotNull(code);
            assertEquals(6, code.length());
            assertFalse(code.contains("0"));
            assertFalse(code.contains("O"));
            assertFalse(code.contains("1"));
            assertFalse(code.contains("I"));
            assertTrue(code.matches("^[2-9A-HJ-NP-Z]{6}$"));
        }
    }

    @Test
    @DisplayName("End-to-End File Flow: Upload -> Metadata -> Download -> Delete")
    void testEndToEndFileFlow() throws Exception {
        byte[] content = "Hello SmartBin Transfer Test Content!".getBytes();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-document.txt",
                MediaType.TEXT_PLAIN_VALUE,
                content
        );

        // 1. Upload File
        String responseBody = mockMvc.perform(multipart("/api/files/upload").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transferCode").isNotEmpty())
                .andExpect(jsonPath("$.transferType").value("FILE"))
                .andExpect(jsonPath("$.filename").value("test-document.txt"))
                .andExpect(jsonPath("$.fileSize").value(content.length))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String code = objectMapper.readTree(responseBody).get("transferCode").asText();
        assertNotNull(code);

        // 2. Retrieve Metadata via POST /api/files/retrieve
        RetrieveRequest retrieveReq = new RetrieveRequest(code);
        mockMvc.perform(post("/api/files/retrieve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(retrieveReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transferCode").value(code))
                .andExpect(jsonPath("$.transferType").value("FILE"))
                .andExpect(jsonPath("$.filename").value("test-document.txt"))
                .andExpect(jsonPath("$.fileSize").value(content.length));

        // 3. Retrieve Metadata via GET /api/files/{code}
        mockMvc.perform(get("/api/files/" + code))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transferCode").value(code))
                .andExpect(jsonPath("$.filename").value("test-document.txt"));

        // 4. Download File via GET /api/files/{code}/download
        mockMvc.perform(get("/api/files/" + code + "/download"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString("test-document.txt")))
                .andExpect(content().bytes(content));

        // 5. Delete File via DELETE /api/files/{code}
        mockMvc.perform(delete("/api/files/" + code))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").isNotEmpty());

        // 6. Verify subsequent lookup returns 404
        mockMvc.perform(get("/api/files/" + code))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Invalid or expired transfer code."));
    }

    @Test
    @DisplayName("End-to-End Text Message Flow: Share Text -> Retrieve Text -> Copy/Download -> Delete")
    void testEndToEndTextFlow() throws Exception {
        String message = "https://example.com/important-link-or-wifi-password";
        TextMessageRequest textReq = new TextMessageRequest(message, "Quick WiFi Password");

        // 1. Share Text
        String responseBody = mockMvc.perform(post("/api/files/text")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(textReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transferCode").isNotEmpty())
                .andExpect(jsonPath("$.transferType").value("TEXT"))
                .andExpect(jsonPath("$.textContent").value(message))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String code = objectMapper.readTree(responseBody).get("transferCode").asText();
        assertNotNull(code);

        // 2. Retrieve Text
        mockMvc.perform(get("/api/files/" + code))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transferCode").value(code))
                .andExpect(jsonPath("$.transferType").value("TEXT"))
                .andExpect(jsonPath("$.textContent").value(message));

        // 3. Download text note as .txt
        mockMvc.perform(get("/api/files/" + code + "/download"))
                .andExpect(status().isOk())
                .andExpect(content().string(message));

        // 4. Clean up
        mockMvc.perform(delete("/api/files/" + code))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Invalid transfer code returns unified 404 error")
    void testInvalidTransferCode() throws Exception {
        mockMvc.perform(get("/api/files/NONEXISTENT"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Invalid or expired transfer code."));
    }
}
