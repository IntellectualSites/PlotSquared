package com.plotsquared.core.services.impl;

import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.persistence.entity.PlotEntity;
import com.plotsquared.core.persistence.entity.PlotFlagEntity;
import com.plotsquared.core.persistence.repository.api.PlotFlagRepository;
import com.plotsquared.core.persistence.repository.api.PlotRepository;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.flag.PlotFlag;
import com.plotsquared.core.services.api.FlagService;
import jakarta.inject.Inject;

import java.util.Optional;

public class FlagDefaultService implements FlagService {

    private final PlotRepository plotRepository;
    private final PlotFlagRepository flagRepository;

    @Inject
    public FlagDefaultService(final PlotRepository repository, final PlotFlagRepository flagRepository) {
        this.plotRepository = repository;
        this.flagRepository = flagRepository;
    }

    @Override
    public void setFlag(final Plot plot, final PlotFlag<?, ?> flag) {
        Optional<PlotEntity> pe = this.plotRepository.findByWorldAndId(plot.getWorldName(), plot.getId().getX(), plot.getId().getY());
        pe.ifPresent(entity -> {
            long plotId = entity.getId();
            String name = flag.getName();
            String value = flag.toString();
            var existing = this.flagRepository.findByPlotAndName(plotId, name);
            if (existing.isPresent()) {
                var e = existing.get();
                e.setFlagValue(value);
                this.flagRepository.save(e);
            } else {
                PlotFlagEntity e = new PlotFlagEntity();
                PlotEntity pref = new PlotEntity();
                pref.setId(entity.getId());
                e.setPlot(pref);
                e.setFlag(name);
                e.setFlagValue(value);
                this.flagRepository.save(e);
            }
        });
    }

    @Override
    public void removeFlag(final Plot plot, final PlotFlag<?, ?> flag) {
        Optional<PlotEntity> pe = this.plotRepository.findByWorldAndId(plot.getWorldName(), plot.getId().getX(),
                plot.getId().getY());
        pe.ifPresent(entity -> this.flagRepository.deleteByPlotAndName(entity.getId(), flag.getName()));
    }

}
