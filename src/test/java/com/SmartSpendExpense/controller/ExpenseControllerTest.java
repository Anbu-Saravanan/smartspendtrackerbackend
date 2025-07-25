//package com.SmartSpendExpense.controller;
//
//import com.SmartSpendExpense.dto.request.ExpenseRequestDTO;
//import com.SmartSpendExpense.dto.response.ExpenseResponseDTO;
//import com.SmartSpendExpense.model.Expense;
//import com.SmartSpendExpense.model.User;
//import com.SmartSpendExpense.repository.BudgetRepository;
//import com.SmartSpendExpense.repository.ExpenseRepository;
//import com.SmartSpendExpense.repository.NotificationRepository;
//import com.SmartSpendExpense.repository.UserRepository;
//import com.SmartSpendExpense.service.ExpenseService;
//import com.SmartSpendExpense.service.NotificationService;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.Mockito;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Import;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.http.MediaType;
//import org.springframework.mail.javamail.JavaMailSender;
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
//@WebMvcTest(ExpenseController.class)
//@Import(ExpenseControllerTest.MockConfig.class) // <--- ADD THIS!
//@WithMockUser(username="anbu.saravanan11211@gmail.com", roles={"ADMIN"})
//class ExpenseControllerTest {
//
//    private JavaMailSender javaMailSender;
//
//    @Configuration
//    static class MockConfig {
//        @Bean
//        public ExpenseService expenseService() {
//            return Mockito.mock(ExpenseService.class);
//        }
//        @Bean
//        public ExpenseRepository expenseRepository() {
//            return Mockito.mock(ExpenseRepository.class);
//        }
//        @Bean
//        public UserRepository userRepository() {
//            return Mockito.mock(UserRepository.class);
//        }
//        @Bean
//        public BudgetRepository budgetRepository() {
//            return Mockito.mock(BudgetRepository.class);
//        }
//        @Bean
//        public NotificationService notificationService() {
//            return Mockito.mock(NotificationService.class);
//        }
//
//        @Bean
//        public JavaMailSender javaMailSender() {
//            return Mockito.mock(JavaMailSender.class);
//        }
//        @Bean
//        public NotificationRepository notificationRepository(){
//            return Mockito.mock(NotificationRepository.class);
//        }
//    }
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @Autowired
//    private ExpenseService expenseService;
//
//    @Autowired
//    private ExpenseRepository expenseRepository;
//
//    @Autowired
//    private UserRepository userRepository;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    // ...rest of your setup and tests as before...
//    private String userId;
//    private String email;
//    private Expense expense;
//    private ExpenseResponseDTO responseDTO;
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
//
//        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
//
//        // Example expense and DTO
//        expense = new Expense();
//        expense.setId("expenseId");
//        expense.setTitle("Lunch");
//        expense.setAmount(BigDecimal.valueOf(50));
//        expense.setCategory("Food");
//        expense.setType("EXPENSE");
//        expense.setDate(new Date());
//
//        responseDTO = new ExpenseResponseDTO();
//        responseDTO.setId(expense.getId());
//        responseDTO.setTitle(expense.getTitle());
//        responseDTO.setAmount(expense.getAmount());
//        responseDTO.setCategory(expense.getCategory());
//        responseDTO.setType(expense.getType());
//        responseDTO.setDate(expense.getDate());
//    }
//
//    @Test
//    void testCreateExpense() throws Exception {
//        ExpenseRequestDTO requestDTO = new ExpenseRequestDTO();
//        requestDTO.setTitle("Lunch");
//        requestDTO.setAmount(BigDecimal.valueOf(50));
//        requestDTO.setCategory("Food");
//        requestDTO.setType("EXPENSE");
//        requestDTO.setDate(new Date());
//        requestDTO.setDescription("Test Desc");
//
//        when(expenseService.createExpense(any(ExpenseRequestDTO.class), eq(userId)))
//                .thenReturn(responseDTO);
//
//        mockMvc.perform(post("/api/expenses/create")
//                        .with(csrf())
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(requestDTO)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.title").value("Lunch"))
//                .andExpect(jsonPath("$.amount").value(50));
//    }
//
//    @Test
//    void testGetExpenses() throws Exception {
//        when(expenseRepository.findByUserId(userId)).thenReturn(Collections.singletonList(expense));
//        when(expenseService.toResponseDTO(any(Expense.class))).thenReturn(responseDTO);
//
//        mockMvc.perform(get("/api/expenses/all"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$[0].title").value("Lunch"));
//    }
//
//    @Test
//    void testGetExpenseById() throws Exception {
//        // Create a responseDTO with id "abc123" for this test
//        responseDTO.setId("abc123");
//        when(expenseService.getExpenseById(eq("abc123"), eq(userId)))
//                .thenReturn(Optional.of(responseDTO));
//
//        mockMvc.perform(get("/api/expenses/getById/{id}", "abc123"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.id").value("abc123"));
//    }
//}
