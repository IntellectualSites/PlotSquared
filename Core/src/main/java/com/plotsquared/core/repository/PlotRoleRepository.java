package com.plotsquared.core.repository;

import com.plotsquared.core.repository.dbo.PlotDBO;
import com.plotsquared.core.repository.dbo.PlotRoleDBO;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;

public interface PlotRoleRepository extends Repository<PlotRoleDBO, PlotRoleDBO.Key> {

    List<PlotRoleDBO> findAllFor(@NonNull PlotDBO plotDBO);
}
