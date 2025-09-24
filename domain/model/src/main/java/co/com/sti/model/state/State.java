package co.com.sti.model.state;

import lombok.Getter;

import java.util.Set;
import java.util.stream.Stream;

@Getter
public enum State {
    PENDING(1, "PENDIENTE", "La solicitud está en espera de revisión."),
    REVIEW(2, "REVISION", "La solicitud esta siendo revisada por un asesor."),
    REJECTED(3, "RECHAZADA", "La solicitud ha sido aprobada."),
    APPROVED(4,"APROBADA","La solicitud ha sido rechazada."),
    ARCHIVED(5, "ARCHIVADA", "La solicitud fue descartada y pasada a archivo");

    private final int idState;
    private final String name;
    private final String description;

    State(int idState, String name, String description) {
        this.idState = idState;
        this.name = name;
        this.description = description;
    }

    public static State getById(int idState) {
        return Stream.of(State.values())
                .filter(state -> state.getIdState() == idState)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Estado no existe con ID: " + idState));
    }

    public static boolean isFinalState(int idState) {
        Set<Integer> finalStates = Set.of(REJECTED.getIdState(), APPROVED.getIdState());
        return finalStates.contains(idState);
    }
}
