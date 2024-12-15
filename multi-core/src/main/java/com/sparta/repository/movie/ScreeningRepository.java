package com.sparta.repository.movie;

import com.sparta.domain.movie.Screening;
import java.util.List;
import java.util.Optional;
import jakarta.persistence.EntityManager;

public class ScreeningRepository {
    private final EntityManager em;

    public ScreeningRepository(EntityManager em) {
        this.em = em;
    }

    public void save(Screening screening) {
        em.persist(screening);
    }

    public Optional<Screening> findById(Long id) {
        Screening screening = em.find(Screening.class, id);
        return Optional.ofNullable(screening);
    }

    public Optional<Screening> findByIdWithTheaterAndMovie(Long id) {
        List<Screening> result = em.createQuery(
                        "SELECT s FROM Screening s " +
                                "JOIN FETCH s.movie " +
                                "JOIN FETCH s.theater " +
                                "WHERE s.id = :id",
                        Screening.class)
                .setParameter("id", id)
                .getResultList();
        return result.stream().findFirst();
    }
}