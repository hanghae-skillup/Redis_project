package com.example.domain.repository;

import com.example.common.dto.MovieDto;
import com.example.common.enums.EGenre;
import com.example.domain.entity.Movies;
import com.example.domain.entity.QMovies;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CustomMovieRepositoryImpl implements CustomMovieRepository {

    private final JPAQueryFactory queryFactory;

    public CustomMovieRepositoryImpl(JPAQueryFactory jpaQueryFactory) {
        this.queryFactory = jpaQueryFactory;
    }

    @Override
    public List<MovieDto> searchMovies(String title, EGenre genre) {
        QMovies movies = QMovies.movies;
        BooleanBuilder builder = new BooleanBuilder();

        // 동적 조건 추가
        if (title != null && !title.isEmpty()) {
            builder.and(movies.title.containsIgnoreCase(title));
        }
        if (genre != null) {
            builder.and(movies.genre.eq(genre));
        }

        builder.and(movies.showing.isTrue());

        // Projection을 사용한 결과 반환
        return queryFactory
                .select(Projections.constructor(MovieDto.class,
                        movies.id,                        // Long
                        movies.title,                     // String
                        movies.genre.stringValue(),       // String
                        movies.thumbnailUrl,              // String
                        movies.runningMinutes,            // Integer
                        movies.showing,                   // Boolean
                        movies.releaseDate.stringValue()  // String
                ))
                .from(movies)
                .where(builder)
                .orderBy(movies.releaseDate.desc())
                .fetch();
    }


}
