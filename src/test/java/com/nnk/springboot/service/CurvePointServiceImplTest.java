package com.nnk.springboot.service;

import com.nnk.springboot.domain.CurvePoint;
import com.nnk.springboot.exceptions.CustomDataAccessException;
import com.nnk.springboot.exceptions.NotFoundException;
import com.nnk.springboot.repositories.CurvePointRepository;
import com.nnk.springboot.service.serviceImpl.CurvePointServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CurvePointServiceImplTest {

    private final Logger LOGGER = LoggerFactory.getLogger(CurvePointServiceImplTest.class);

    @InjectMocks
    private CurvePointServiceImpl curvePointService;

    @Mock
    private CurvePointRepository curvePointRepository;

    private CurvePoint curvePoint;

    @BeforeEach
    void setUp() {

        LOGGER.info("Setting up CurvePointServiceImplTest");
        curvePoint = new CurvePoint();
        curvePoint.setId(1);
        curvePoint.setCurveId(10);
        curvePoint.setTerm(1.0);
        curvePoint.setValue(100.0);
        curvePoint.setAsOfDate(LocalDateTime.now());
    }

    @Test
    void testFindAll_shouldReturnCurvePointList() {
        // Arrange
        List<CurvePoint> mockList = List.of(curvePoint);
        when(curvePointRepository.findAll()).thenReturn(mockList);

        // Act
        List<CurvePoint> result = curvePointService.findAll();

        // Assert
        assertEquals(1, result.size());
    }

    @Test
    public void testFindAll_shouldThrowCustomException_whenRepositoryFails() {
        // Arrange
        when(curvePointRepository.findAll()).thenThrow(new RuntimeException("DB error"));

        // Act & Assert
        CustomDataAccessException exception = assertThrows(CustomDataAccessException.class, () -> curvePointService.findAll());

        assertEquals("Error retrieving CurvePoint(s) from the database", exception.getMessage());
        verify(curvePointRepository, times(1)).findAll();
    }

    @Test
    void testFindById_shouldReturnCurvePointIdWhenFound() {
        when(curvePointRepository.findById(1)).thenReturn(Optional.of(curvePoint));

        CurvePoint result = curvePointService.findById(1);

        assertThat(result).isEqualTo(curvePoint);
    }

    @Test
    void testFindById_whenNotFound() {
        when(curvePointRepository.findById(1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> curvePointService.findById(1))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("CurvePoint not found");
    }

    @Test
    void testSave_success() {
        when(curvePointRepository.save(any(CurvePoint.class))).thenReturn(curvePoint);

        curvePointService.save(curvePoint);

        verify(curvePointRepository, times(1)).save(any(CurvePoint.class));
    }

    @Test
    void testSave_withException() {
        when(curvePointRepository.save(any(CurvePoint.class))).thenThrow(RuntimeException.class);

        assertThatThrownBy(() -> curvePointService.save(curvePoint))
                .isInstanceOf(CustomDataAccessException.class)
                .hasMessageContaining("Error saving CurvePoint to the database");
    }

    @Test
    void testUpdate_whenFound() {
        when(curvePointRepository.existsById(1)).thenReturn(true);
        when(curvePointRepository.findById(1)).thenReturn(Optional.of(new CurvePoint()));
        when(curvePointRepository.save(any(CurvePoint.class))).thenReturn(curvePoint);

        curvePointService.update(curvePoint);

        verify(curvePointRepository, times(1)).save(any(CurvePoint.class));
    }

    @Test
    void testUpdate_whenNotFound() {
        when(curvePointRepository.existsById(1)).thenReturn(false);

        assertThatThrownBy(() -> curvePointService.update(curvePoint))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("CurvePoint");
    }

    @Test
    void testUpdate_whenExistsButFindByIdFails() {
        // Arrange
        when(curvePointRepository.existsById(curvePoint.getId())).thenReturn(true);
        when(curvePointRepository.findById(curvePoint.getId())).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> curvePointService.update(curvePoint))
                .isInstanceOf(CustomDataAccessException.class)
                .hasMessageContaining("Error updating CurvePoint to the database")
                .hasCauseInstanceOf(NotFoundException.class);
    }


    @Test
    void testUpdate_whenSaveThrowsException() {
        // Arrange
        when(curvePointRepository.existsById(curvePoint.getId())).thenReturn(true);
        when(curvePointRepository.findById(curvePoint.getId())).thenReturn(Optional.of(new CurvePoint()));
        when(curvePointRepository.save(any(CurvePoint.class))).thenThrow(new RuntimeException("DB error"));

        // Act + Assert
        assertThatThrownBy(() -> curvePointService.update(curvePoint))
                .isInstanceOf(CustomDataAccessException.class)
                .hasMessageContaining("Error updating CurvePoint");
    }



    @Test
    void testDeleteById_whenFound() {
        when(curvePointRepository.existsById(1)).thenReturn(true);

        curvePointService.deleteById(1);

        verify(curvePointRepository, times(1)).deleteById(1);
    }

    @Test
    void testDeleteById_whenNotFound() {
        when(curvePointRepository.existsById(1)).thenReturn(false);

        assertThatThrownBy(() -> curvePointService.deleteById(1))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("CurvePoint");
    }
}
