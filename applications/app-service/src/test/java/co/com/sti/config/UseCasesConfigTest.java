package co.com.sti.config;

import co.com.sti.model.apply.gateways.ApplyRepository;
import co.com.sti.model.drivenports.IUserExtras;
import co.com.sti.usecase.applyloan.ApplyLoanUseCase;
import co.com.sti.usecase.transaction.TransactionExecutor;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

public class UseCasesConfigTest {

    @Test
    void testApplyLoanUseCaseBeanCreation() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(TestConfig.class)) {
            ApplyLoanUseCase applyLoanUseCase = context.getBean(ApplyLoanUseCase.class);
            assertNotNull(applyLoanUseCase, "El bean ApplyLoanUseCase no debe ser nulo");
        }
    }

    @Test
    void testUseCaseBeansExist() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(TestConfig.class)) {
            String[] beanNames = context.getBeanDefinitionNames();

            boolean useCaseBeanFound = false;
            for (String beanName : beanNames) {
                if (beanName.endsWith("UseCase")) {
                    useCaseBeanFound = true;
                    System.out.println("Bean de Use Case encontrado: " + beanName);
                    Object bean = context.getBean(beanName);
                    assertNotNull(bean, "El bean " + beanName + " no debe ser nulo");
                }
            }

            assertTrue(useCaseBeanFound, "No se encontraron beans que terminen con 'UseCase'.");
        }
    }

    @Configuration
    @Import(UseCasesConfig.class)
    static class TestConfig {
        @Bean
        public ApplyRepository applyRepository() {
            return mock(ApplyRepository.class);
        }

        @Bean
        public IUserExtras userExistenceChecker() {
            return mock(IUserExtras.class);
        }

        @Bean
        public TransactionExecutor transactionExecutor() {
            return mock(TransactionExecutor.class);
        }
    }
}