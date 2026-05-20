package roomescape.admin.reservation;

import java.time.Clock;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.exception.NotFoundException;
import roomescape.reservation.Reservation;
import roomescape.reservation.repository.ReservationRepository;

@Service
public class AdminReservationService {

    private final ReservationRepository reservationRepository;
    private final Clock clock;

    public AdminReservationService(ReservationRepository reservationRepository,
                                   Clock clock) {
        this.reservationRepository = reservationRepository;
        this.clock = clock;
    }

    @Transactional
    public void delete(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("예약을 찾을 수 없습니다."));

        reservation.validateNotPast(LocalDateTime.now(clock));

        reservationRepository.deleteById(id);
    }
}
