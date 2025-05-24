package com.nnk.springboot.service.serviceImpl;

import com.nnk.springboot.domain.CurvePoint;
import com.nnk.springboot.exceptions.CustomDataAccessException;
import com.nnk.springboot.exceptions.NotFoundException;
import com.nnk.springboot.repositories.CurvePointRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CurvePointServiceImpl {

    private static final Logger LOGGER = LoggerFactory.getLogger(CurvePointServiceImpl.class);

    private final CurvePointRepository curvePointRepository;

    public CurvePointServiceImpl(CurvePointRepository curvePointRepository) {
        this.curvePointRepository = curvePointRepository;
    }

    @Transactional(readOnly = true)
    public List<CurvePoint> findAll() {
        try {
            return curvePointRepository.findAll();
        } catch (Exception e) {
            throw new CustomDataAccessException("Error retrieving CurvePoint(s) from the database", e);
        }
    }

    @Transactional(readOnly = true)
    public CurvePoint findById(Integer id) {
        return curvePointRepository.findById(id).orElseThrow(() -> new NotFoundException("CurvePoint not found", id));
    }

    @Transactional
    public void save(CurvePoint curvePoint) {

        try {
            curvePoint.setCreationDate(LocalDateTime.now());
            curvePointRepository.save(curvePoint);
            LOGGER.info("Bid saved successfully {}", curvePoint);
        } catch (Exception e) {
            throw new CustomDataAccessException("Error saving CurvePoint to the database", e);
        }
    }

    @Transactional
    public void update(CurvePoint curvePoint) {

        if (!curvePointRepository.existsById(curvePoint.getId())) {
            throw new NotFoundException("CurvePoint", curvePoint.getId());
        }

        try {

            CurvePoint existingCurvePoint = curvePointRepository.findById(curvePoint.getId()).orElseThrow(()
                    -> new NotFoundException("CurvePoint", curvePoint.getId()));

            existingCurvePoint.setCurveId(curvePoint.getCurveId());
            existingCurvePoint.setTerm(curvePoint.getTerm());
            existingCurvePoint.setValue(curvePoint.getValue());
            existingCurvePoint.setAsOfDate(curvePoint.getAsOfDate());

            curvePointRepository.save(existingCurvePoint);
            LOGGER.info("Bid updated successfully {}", curvePoint);
        } catch (Exception e) {
            throw new CustomDataAccessException("Error updating CurvePoint to the database", e);
        }
    }

    @Transactional
    public void deleteById(Integer id) {
        if (!curvePointRepository.existsById(id)) {
            throw new NotFoundException("CurvePoint", id);
        }
        curvePointRepository.deleteById(id);
        LOGGER.info("Bid deleted successfully " + id);
    }
}
