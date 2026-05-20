package roomescape.reservation;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.Page;
import roomescape.exception.AlreadyInUseException;
import roomescape.exception.NotFoundException;
import roomescape.reservation.dto.ReservationRequest;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.dto.ReservationsResponse;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.theme.Theme;
import roomescape.theme.repository.ThemeRepository;
import roomescape.time.ReservationTime;
import roomescape.time.repository.ReservationTimeRepository;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final Clock clock;

    public ReservationService(ReservationRepository reservationRepository,
                              ReservationTimeRepository reservationTimeRepository,
                              ThemeRepository themeRepository,
                              Clock clock) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.clock = clock;
    }

    @Transactional
    public ReservationResponse create(ReservationRequest reservationRequest) {
        ReservationTime reservationTime = getReservationTime(reservationRequest);
        Theme theme = getTheme(reservationRequest);

        Reservation reservation = new Reservation(
                reservationRequest.userName(),
                theme,
                reservationRequest.date(),
                reservationTime
        );

        reservation.validateNotPast(LocalDateTime.now(clock));
        validateDuplicate(reservation);

        Reservation saved = reservationRepository.save(reservation);
        return ReservationResponse.from(saved);
    }

    public Page<ReservationResponse> read(int page, int size) {
        Page<Reservation> reservations = reservationRepository.findAll(page, size);
        List<ReservationResponse> items = reservations.items().stream()
                .map(ReservationResponse::from)
                .toList();

        return new Page<>(items, page, size, reservations.hasNext());
    }

    public ReservationsResponse readByUserName(String userName) {
        List<ReservationResponse> reservationsResponse = reservationRepository.findByUserName(userName).stream()
                .map(ReservationResponse::from)
                .toList();

        return ReservationsResponse.from(reservationsResponse);
    }

    @Transactional
    public ReservationResponse update(long id, ReservationRequest reservationRequest, String userName) {

        LocalDateTime now = LocalDateTime.now(clock);
        Reservation reservation = getReservation(id);

        reservation.validateOwner(userName);
        reservation.validateNotPast(now);

        ReservationTime reservationTime = getReservationTime(reservationRequest);
        Theme theme = getTheme(reservationRequest);

        Reservation updateReservation = new Reservation(
                id,
                reservation.getUserName(),
                theme,
                reservationRequest.date(),
                reservationTime
        );

        updateReservation.validateNotPast(now);
        validateDuplicate(updateReservation);

        reservationRepository.update(updateReservation);
        return ReservationResponse.from(updateReservation);
    }

    @Transactional
    public void delete(Long id, String userName) {
        Reservation reservation = getReservation(id);

        reservation.validateOwner(userName);
        reservation.validateNotPast(LocalDateTime.now(clock));

        reservationRepository.deleteById(id);
    }

    private Reservation getReservation(long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("예약을 찾을 수 없습니다."));
    }

    private Theme getTheme(ReservationRequest reservationRequest) {
        return themeRepository.findById(reservationRequest.themeId())
                .orElseThrow(() -> new NotFoundException("테마를 찾을 수 없습니다."));
    }

    private ReservationTime getReservationTime(ReservationRequest reservationRequest) {
        return reservationTimeRepository.findById(reservationRequest.timeId())
                .orElseThrow(() -> new NotFoundException("예약 시간을 찾을 수 없습니다."));
    }


    private void validateDuplicate(Reservation reservation) {
        if (reservationRepository.existsByThemeIdAndDateAndTimeId(
                reservation.getTheme().getId(),
                reservation.getDate(),
                reservation.getTime().getId())
        ) {
            throw new AlreadyInUseException("이미 예약된 테마•날짜•시간입니다.");
        }
    }
}
