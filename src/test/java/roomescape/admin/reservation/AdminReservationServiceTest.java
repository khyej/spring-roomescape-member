package roomescape.admin.reservation;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.exception.InvalidStateException;
import roomescape.reservation.Reservation;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.theme.Theme;
import roomescape.time.ReservationTime;

@ExtendWith(MockitoExtension.class)
class AdminReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private Clock clock;

    @InjectMocks
    private AdminReservationService adminReservationService;

    private final Theme theme = new Theme(1L, "공포의 방", "무서운 방", "http://s3.com");
    private final ReservationTime reservationTime = new ReservationTime(1L, LocalTime.of(10, 0));

    @Test
    void 이미_지난_예약_삭제시_400() {
        mockTime(LocalDate.of(2026, 5, 20), LocalTime.of(12, 0));

        Reservation reservation = new Reservation(1L, "동키", theme, LocalDate.of(2026, 5, 20), reservationTime);
        given(reservationRepository.findById(1L)).willReturn(Optional.of(reservation));

        assertThatThrownBy(() -> adminReservationService.delete(1L))
                .isInstanceOf(InvalidStateException.class);
    }

    private void mockTime(LocalDate date, LocalTime time) {
        Instant fixedInstant = date.atTime(time).atZone(ZoneId.of("Asia/Seoul")).toInstant();
        given(clock.instant()).willReturn(fixedInstant);
        given(clock.getZone()).willReturn(ZoneId.of("Asia/Seoul"));
    }
}
