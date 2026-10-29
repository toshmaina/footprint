package ke.co.skyworld.internship.domain.beans;

import java.util.List;

public record PagedResponse<T>(List<T> items, int page, int pageSize, long totalCount) {
}