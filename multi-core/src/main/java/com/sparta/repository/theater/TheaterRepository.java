package com.sparta.repository.theater;

import com.sparta.domain.theater.Theater;
import java.util.List;
import java.util.Optional;
import jakarta.persistence.EntityManager;

public class TheaterRepository {
    private final EntityManager em;

    public TheaterRepository(EntityManager em) {
        this.em = em;
    }

    public void save(Theater theater) {
        em.persist(theater);
    }

    public Optional<Theater> findById(Long id) {
        return Optional.ofNullable(em.find(Theater.class, id));
    }

    public Optional<Theater> findByIdWithSeats(Long id) {
        List<Theater> result = em.createQuery(
                        "SELECT DISTINCT t FROM Theater t " +
                                "LEFT JOIN FETCH t.seats " +
                                "WHERE t.id = :id",
                        Theater.class)
                .setParameter("id", id)
                .getResultList();
        return result.stream().findFirst();
    }
}