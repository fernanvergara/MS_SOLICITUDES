package co.com.sti.config;

import co.com.sti.model.apply.gateways.ApplyRepository;
import co.com.sti.model.drivenports.IUserExistenceChecker;
import co.com.sti.usecase.applyloan.ApplyLoanUseCase;
import co.com.sti.usecase.transaction.TransactionExecutor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

@Configuration
@ComponentScan(basePackages = "co.com.sti.usecase",
        includeFilters = {
                @ComponentScan.Filter(type = FilterType.REGEX, pattern = "^.+UseCase$")
        },
        useDefaultFilters = false)
public class UseCasesConfig {

    @Bean
    public ApplyLoanUseCase applyLoanUseCase(ApplyRepository applyRepository,
                                             IUserExistenceChecker userExistenceChecker,
                                             TransactionExecutor transactionExecutor){
        return new ApplyLoanUseCase(applyRepository, userExistenceChecker, transactionExecutor);
    }
}
