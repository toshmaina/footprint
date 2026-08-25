package ke.co.skyworld.internship.repository;

import java.util.List;

public record PageResult<T>(List<T> items, long totalCount) {
}