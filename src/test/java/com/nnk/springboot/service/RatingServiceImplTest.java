package com.nnk.springboot.service;

import com.nnk.springboot.domain.Rating;
import com.nnk.springboot.exceptions.CustomDataAccessException;
import com.nnk.springboot.exceptions.NotFoundException;
import com.nnk.springboot.repositories.RatingRepository;
import com.nnk.springboot.service.serviceImpl.RatingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RatingServiceImplTest {

    @Mock
    private RatingRepository ratingRepository;

    @InjectMocks
    private RatingServiceImpl ratingService;

    private Rating rating;

    @BeforeEach
    void setUp() {
        rating = new Rating();
        rating.setId(1);
        rating.setOrderNumber(10);
        rating.setMoodysRating("Moody");
        rating.setSandPRating("SandP");
        rating.setFitchRating("Fitch");
    }

    @Test
    void testFindAll_success() {
        when(ratingRepository.findAll()).thenReturn(List.of(rating));

        List<Rating> result = ratingService.findAll();

        assertThat(result).containsExactly(rating);
    }

    @Test
    void testFindAll_exception() {
        when(ratingRepository.findAll()).thenThrow(new RuntimeException("DB error"));

        assertThatThrownBy(() -> ratingService.findAll())
                .isInstanceOf(CustomDataAccessException.class)
                .hasMessageContaining("Error retrieving Rating");
    }

    @Test
    void testFindById_success() {
        when(ratingRepository.findById(1)).thenReturn(Optional.of(rating));

        Rating result = ratingService.findById(1);

        assertThat(result).isEqualTo(rating);
    }

    @Test
    void testFindById_notFound() {
        when(ratingRepository.findById(1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ratingService.findById(1))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Rating not found");
    }

    @Test
    void testSave_success() {
        // Pas de retour car save() est void
        ratingService.save(rating);

        verify(ratingRepository).save(rating);
    }

    @Test
    void testSave_exception() {
        doThrow(new RuntimeException("DB error")).when(ratingRepository).save(any(Rating.class));

        assertThatThrownBy(() -> ratingService.save(rating))
                .isInstanceOf(CustomDataAccessException.class)
                .hasMessageContaining("Error saving Rating");
    }

    @Test
    void testUpdate_success() {
        when(ratingRepository.existsById(1)).thenReturn(true);
        when(ratingRepository.findById(1)).thenReturn(Optional.of(rating));

        rating.setMoodysRating("Updated Moody");
        ratingService.update(rating);

        verify(ratingRepository).save(any(Rating.class));
    }

    @Test
    void testUpdate_notFound_onExistsCheck() {
        when(ratingRepository.existsById(1)).thenReturn(false);

        assertThatThrownBy(() -> ratingService.update(rating))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Rating");
    }

    @Test
    void testUpdate_notFound_onFind() {
        when(ratingRepository.existsById(1)).thenReturn(true);
        when(ratingRepository.findById(1)).thenReturn(Optional.empty());

        Throwable thrown = catchThrowable(() -> ratingService.update(rating));

        assertThat(thrown)
                .isInstanceOf(CustomDataAccessException.class)
                .hasCauseInstanceOf(NotFoundException.class)
                .hasMessageContaining("Error updating Rating to the database");
    }

    @Test
    void testUpdate_exception() {
        when(ratingRepository.existsById(1)).thenReturn(true);
        when(ratingRepository.findById(1)).thenReturn(Optional.of(rating));
        doThrow(new RuntimeException("DB error")).when(ratingRepository).save(any(Rating.class));

        assertThatThrownBy(() -> ratingService.update(rating))
                .isInstanceOf(CustomDataAccessException.class)
                .hasMessageContaining("Error updating Rating");
    }

    @Test
    void testDeleteById_success() {
        when(ratingRepository.existsById(1)).thenReturn(true);

        ratingService.deleteById(1);

        verify(ratingRepository).deleteById(1);
    }

    @Test
    void testDeleteById_notFound() {
        when(ratingRepository.existsById(1)).thenReturn(false);

        assertThatThrownBy(() -> ratingService.deleteById(1))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Rating");
    }
}