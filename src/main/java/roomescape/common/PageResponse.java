package roomescape.common;

import java.util.List;

public record PageResponse<T>(
        List<T> items,
        int page,
        int size,
        boolean hasNext
) {
    public static <T> PageResponse<T> of(List<T> items, int page, int size, boolean hasNext) {
        return new PageResponse<>(items, page, size, hasNext);
    }
}
