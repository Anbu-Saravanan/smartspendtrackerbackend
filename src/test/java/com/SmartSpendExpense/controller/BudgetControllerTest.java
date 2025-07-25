//package com.SmartSpendExpense.controller;
//
//import com.SmartSpendExpense.dto.request.BudgetRequestDTO;
//import com.SmartSpendExpense.dto.response.BudgetResponseDTO;
//import com.SmartSpendExpense.model.User;
//import com.SmartSpendExpense.repository.BudgetRepository;
//import com.SmartSpendExpense.repository.UserRepository;
//import com.SmartSpendExpense.service.BudgetService;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.Mockito;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Import;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.http.MediaType;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContext;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.test.context.support.WithMockUser;
//import org.springframework.test.web.servlet.MockMvc;
//
//import java.math.BigDecimal;
//import java.util.*;
//
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.Mockito.when;
//import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//@WebMvcTest(BudgetController.class)
//@Import(BudgetControllerTest.MockConfig.class)
//@WithMockUser(username = "anbu.saravanan11211@gmail.com", roles = {"ADMIN"})
//class BudgetControllerTest {
//
//    @Configuration
//    static class MockConfig {
//        @Bean public BudgetService budgetService() { return Mockito.mock(BudgetService.class); }
//        @Bean public BudgetRepository budgetRepository() { return Mockito.mock(BudgetRepository.class); }
//        @Bean public UserRepository userRepository() { return Mockito.mock(UserRepository.class); }
//    }
//
//    @Autowired private MockMvc mockMvc;
//    @Autowired private BudgetService budgetService;
//    @Autowired private BudgetRepository budgetRepository;
//    @Autowired private UserRepository userRepository;
//    @Autowired private ObjectMapper objectMapper;
//
//    private String userId;
//    private String email;
//    private BudgetResponseDTO responseDTO;
//
//    @BeforeEach
//    void setup() {
//        userId = "testUserId";
//        email = "test@email.com";
//
//        // Setup SecurityContextHolder mock (simulate logged-in user)
//        Authentication authentication = Mockito.mock(Authentication.class);
//        when(authentication.getPrincipal()).thenReturn(email);
//        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
//        when(securityContext.getAuthentication()).thenReturn(authentication);
//        SecurityContextHolder.setContext(securityContext);
//
//        // Setup User mock
//        User user = new User();
//        user.setId(userId);
//        user.setEmail(email);
//        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
//
//        // Example BudgetResponseDTO
//        responseDTO = new BudgetResponseDTO();
//        responseDTO.setId("budget123");
//        responseDTO.setCategory("Food");
//       // responseDTO.setLimit(BigDecimal.valueOf(1000));
//        responseDTO.setMonth(7);
//        responseDTO.setYear(2025);
//    }
//
//    @Test
//    void testSetBudget() throws Exception {
//        BudgetRequestDTO requestDTO = new BudgetRequestDTO();
//        requestDTO.setCategory("Food");
//        ///requestDTO.setLimit(BigDecimal.valueOf(1000));
//        requestDTO.setMonth(7);
//        requestDTO.setYear(2025);
//
//        when(budgetService.createOrUpdateBudget(any(BudgetRequestDTO.class), eq(userId)))
//                .thenReturn(responseDTO);
//
//        mockMvc.perform(post("/api/budgets/set")
//                        .with(csrf())
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(requestDTO)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.category").value("Food"))
//                .andExpect(jsonPath("$.limit").value(1000));
//    }
//
//    @Test
//    void testGetBudgets() throws Exception {
//        when(budgetService.getBudgets(userId)).thenReturn(Collections.singletonList(responseDTO));
//
//        mockMvc.perform(get("/api/budgets/all"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$[0].category").value("Food"))
//                .andExpect(jsonPath("$[0].limit").value(1000));
//    }
//
//    @Test
//    void testDeleteBudget() throws Exception {
//        Mockito.doNothing().when(budgetRepository).deleteById("budget123");
//
//        mockMvc.perform(delete("/api/budgets/delete/{id}", "budget123").with(csrf()))
//                .andExpect(status().isNoContent());
//    }
//}
