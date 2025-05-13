package com.nnk.springboot.service.serviceImpl;

import com.nnk.springboot.domain.Rating;
import com.nnk.springboot.exceptions.CustomDataAccessException;
import com.nnk.springboot.exceptions.NotFoundException;
import com.nnk.springboot.repositories.RatingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RatingServiceImpl {

    private static final Logger LOGGER = LoggerFactory.getLogger(RatingServiceImpl.class);
    private final RatingRepository ratingRepository;

    public RatingServiceImpl(RatingRepository ratingRepository) {
        this.ratingRepository = ratingRepository;
    }

    public List<Rating> findAll() {

        try {
            return ratingRepository.findAll();
        } catch (Exception e) {
            LOGGER.error("Error retrieving Rating(s) from the database", e);
            throw new CustomDataAccessException("Error retrieving Rating(s) from the database", e);
        }
    }

    public Rating findById(Integer id) {
        return ratingRepository.findById(id).orElseThrow(() -> new NotFoundException("Rating not found", id));
    }

    public void save(Rating rating) {

        try {
            ratingRepository.save(rating);
            LOGGER.info("Rating saved successfully {}", rating);
        } catch (Exception e) {
            LOGGER.error("Error saving Rating to the database", e);
            throw new CustomDataAccessException("Error saving Rating to the database", e);
        }
    }

    public void update(Rating rating) {

        if (!ratingRepository.existsById(rating.getId())) {
            throw new NotFoundException("Rating", rating.getId());
        }

        try {
            Rating existingRating = ratingRepository.findById(rating.getId()).orElseThrow(()
                    -> new NotFoundException("Rating", rating.getId()));

            existingRating.setOrderNumber(rating.getOrderNumber());
            existingRating.setMoodysRating(rating.getMoodysRating());
            existingRating.setSandPRating(rating.getSandPRating());
            existingRating.setFitchRating(rating.getFitchRating());

            ratingRepository.save(existingRating);
            LOGGER.info("Rating updated successfully {}", rating);

        } catch (Exception e) {
            LOGGER.error("Error updating Rating to the database", e);
            throw new CustomDataAccessException("Error updating Rating to the database", e);
        }
    }

    public void deleteById(Integer id) {
        if (!ratingRepository.existsById(id)) {
            LOGGER.error("Rating with id {} not found", id);
            throw new NotFoundException("Rating", id);
        }
        ratingRepository.deleteById(id);
        LOGGER.info("Rating with id {} deleted successfully", id);
    }

}
