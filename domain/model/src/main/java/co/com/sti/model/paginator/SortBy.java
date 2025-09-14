package co.com.sti.model.paginator;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class SortBy {
    private String property;
    private String direction;
}
