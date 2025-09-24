package co.com.sti.api.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplyUpdateDTO {
    private Long idApply;
    private Integer idState;
}
