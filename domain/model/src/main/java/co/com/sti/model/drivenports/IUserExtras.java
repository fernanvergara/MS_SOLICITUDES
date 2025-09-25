package co.com.sti.model.drivenports;

import co.com.sti.model.drivenports.dto.UserDTO;
import reactor.core.publisher.Mono;

public interface IUserExtras {

//    Mono<Boolean> verifyUser(String identificaction);

    Mono<UserDTO> dataUser(String identification);
}
