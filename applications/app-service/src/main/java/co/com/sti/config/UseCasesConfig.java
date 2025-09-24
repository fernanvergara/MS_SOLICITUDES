package co.com.sti.config;

import co.com.sti.model.apply.gateways.ApplyRepository;
import co.com.sti.model.drivenports.IUserExtras;
import co.com.sti.model.notification.gateways.SQSGateway;
import co.com.sti.usecase.applyloan.ApplyLoanUseCase;
import co.com.sti.usecase.requestapplylist.RequestApplyListUseCase;
import co.com.sti.usecase.transaction.TransactionExecutor;
import co.com.sti.usecase.updateapply.UpdateApplyUseCase;
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
                                             IUserExtras userExistenceChecker,
                                             TransactionExecutor transactionExecutor){
        return new ApplyLoanUseCase(applyRepository, userExistenceChecker, transactionExecutor);
    }

    @Bean
    public RequestApplyListUseCase requestApplyListUseCase(ApplyRepository applyRepository){
        return new RequestApplyListUseCase(applyRepository);
    }

    @Bean
    public UpdateApplyUseCase updateApplyUseCase(ApplyRepository applyRepository, SQSGateway sqsGateway, IUserExtras userExtras, TransactionExecutor transactionExecutor){
        return new UpdateApplyUseCase(applyRepository, sqsGateway,  userExtras,  transactionExecutor);
    }
}
