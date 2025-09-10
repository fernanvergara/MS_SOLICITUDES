package co.com.sti.model.drivenports;

import reactor.core.publisher.Mono;

public interface IUserExtras {
    Mono<Boolean> verifyUser(String identificaction);

}
