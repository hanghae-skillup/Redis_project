package com.sparta.repository.booking;

import com.sparta.domain.movie.Seat;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

public class SeatRepository {
    private final EntityManager em;

    public SeatRepository(EntityManager em) {
        this.em = em;
    }

    public void save(Seat seat) {
        em.persist(seat);
    }

    public Optional<Seat> findById(Long id) {
        return Optional.ofNullable(em.find(Seat.class, id));
    }

    public Optional<Seat> findByTheaterIdAndSeatRowAndSeatColumn(Long theaterId, String row, int column) {
        List<Seat> result = em.createQuery(
                        "SELECT s FROM Seat s " +
                                "WHERE s.theater.id = :theaterId " +
                                "AND s.seatRow = :row " +
                                "AND s.seatColumn = :column",
                        Seat.class)
                .setParameter("theaterId", theaterId)
                .setParameter("row", row)
                .setParameter("column", column)
                .getResultList();
        return result.stream().findFirst();
    }

    public List<Seat> findByTheaterIdAndSeatRow(Long theaterId, String row) {
        return em.createQuery(
                        "SELECT s FROM Seat s " +
                                "WHERE s.theater.id = :theaterId " +
                                "AND s.seatRow = :row " +
                                "ORDER BY s.seatColumn",
                        Seat.class)
                .setParameter("theaterId", theaterId)
                .setParameter("row", row)
                .getResultList();
    }
}