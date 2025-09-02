package co.com.sti.model.drivenports;

import reactor.core.publisher.Mono;

public interface IUserExistenceChecker {
    Mono<Boolean> verifyUser(String identificaction);
}
