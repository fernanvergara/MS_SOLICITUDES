package co.com.sti.model.request.paginator;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class Pagination {
    private int page;
    private int size;
    private List<SortBy> sortBy;
}
