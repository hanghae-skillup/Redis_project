
package com.example.infrastructure.repository;

import com.example.domain.entity.Movies;
import com.example.domain.repository.MovieRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface JpaMovieRepository extends JpaRepository<Movies, Long>, MovieRepository {
    // 추가 메서드 선언
    @Override
    List<Movies> findByGenre(String genre);

    @Override
    List<Movies> findByShowingOrderByReleaseDateDesc(Boolean showing);
}
