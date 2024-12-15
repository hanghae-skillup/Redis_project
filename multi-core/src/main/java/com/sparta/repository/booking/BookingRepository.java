package com.sparta.repository.booking;

import com.sparta.domain.booking.Booking;
import com.sparta.domain.movie.Screening;
import jakarta.persistence.EntityManager;
import java.util.List;

public class BookingRepository {
    private final EntityManager em;

    public BookingRepository(EntityManager em) {
        this.em = em;
    }

    public void save(Booking booking) {
        em.persist(booking);
    }

    public void saveAll(List<Booking> bookings) {
        for (Booking booking : bookings) {
            em.persist(booking);
        }
    }

    public Booking findById(Long id) {
        return em.find(Booking.class, id);
    }

    public List<Booking> findByScreening(Screening screening) {
        return em.createQuery(
                        "SELECT b FROM Booking b WHERE b.screening = :screening",
                        Booking.class)
                .setParameter("screening", screening)
                .getResultList();
    }

    public int countByScreeningIdAndUserId(Long screeningId, Long userId) {
        return em.createQuery(
                        "SELECT COUNT(b) FROM Booking b WHERE b.screening.id = :screeningId AND b.userId = :userId",
                        Long.class)
                .setParameter("screeningId", screeningId)
                .setParameter("userId", userId)
                .getSingleResult()
                .intValue();
    }
}