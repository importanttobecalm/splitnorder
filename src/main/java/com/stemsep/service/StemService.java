package com.stemsep.service;

import com.stemsep.dao.StemDao;
import com.stemsep.model.Stem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StemService {

    @Autowired
    private StemDao stemDao;

    @Transactional(readOnly = true)
    public Stem getStemById(Long id) {
        return stemDao.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Stem> getStemsByJobId(Long jobId) {
        return stemDao.findByJobId(jobId);
    }

    @Transactional(readOnly = true)
    public Stem getStemByJobAndType(Long jobId, String stemType) {
        return stemDao.findByJobIdAndType(jobId, stemType);
    }
}
