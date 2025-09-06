/*
 * PlotSquared, a land and world management plugin for Minecraft.
 * Copyright (C) IntellectualSites <https://intellectualsites.com>
 * Copyright (C) IntellectualSites team and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.plotsquared.core.services.impl;

import com.plotsquared.core.persistence.entity.PlotEntity;
import com.plotsquared.core.persistence.repository.api.PlotDeniedRepository;
import com.plotsquared.core.persistence.repository.api.PlotMembershipRepository;
import com.plotsquared.core.persistence.repository.api.PlotRepository;
import com.plotsquared.core.persistence.repository.api.PlotTrustedRepository;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotId;
import com.plotsquared.core.services.api.MemberService;
import jakarta.inject.Inject;

import java.util.Optional;
import java.util.UUID;

public class MemberDefaultService implements MemberService {

    private final PlotRepository plotRepository;
    private final PlotTrustedRepository trustedRepository;
    private final PlotMembershipRepository membershipRepository;
    private final PlotDeniedRepository deniedRepository;

    @Inject
    public MemberDefaultService(final PlotRepository repository, final PlotTrustedRepository trustedRepository,
                                final PlotMembershipRepository repo, final PlotDeniedRepository deniedRepository
    ) {
        this.plotRepository = repository;
        this.trustedRepository = trustedRepository;
        this.membershipRepository = repo;
        this.deniedRepository = deniedRepository;
    }

    @Override
    public void removeTrusted(final Plot plot, final UUID uuid) {
        String world = null;
        if (plot.getArea() != null) {
            world = plot.getArea().toString();
        }
        if (world == null) {
            return;
        }
        int x = plot.getId().getX();
        int z = plot.getId().getY();
        Optional<PlotEntity> ent = this.plotRepository.findByWorldAndId(world, x, z);
        if (ent.isPresent() && ent.get().getId() != null) {
            this.trustedRepository.remove(ent.get().getId(), uuid.toString());
        }
    }

    @Override
    public void removeMember(final Plot plot, final UUID uuid) {
        String world = null;
        if (plot.getArea() != null) {
            world = plot.getArea().toString();
        }
        if (world == null) {
            return;
        }
        PlotId pid = plot.getId();
        Optional<PlotEntity> pe = this.plotRepository.findByWorldAndId(world, pid.getX(), pid.getY());
        pe.ifPresent(entity -> this.membershipRepository.remove(entity.getId(), uuid.toString()));
    }

    @Override
    public void setTrusted(final Plot plot, final UUID uuid) {
        String world = null;
        if (plot.getArea() != null) {
            world = plot.getArea().toString();
        }
        if (world == null) {
            return;
        }
        PlotId pid = plot.getId();
        Optional<PlotEntity> pe = this.plotRepository.findByWorldAndId(world, pid.getX(), pid.getY());
        pe.ifPresent(entity -> this.trustedRepository.add(entity.getId(), uuid.toString()));
    }

    @Override
    public void setMember(final Plot plot, final UUID uuid) {
        String world = null;
        if (plot.getArea() != null) {
            world = plot.getArea().toString();
        }
        if (world == null) {
            return;
        }
        PlotId pid = plot.getId();
        Optional<PlotEntity> pe = this.plotRepository.findByWorldAndId(world, pid.getX(), pid.getY());
        pe.ifPresent(entity -> membershipRepository.add(entity.getId(), uuid.toString()));
    }

    @Override
    public void removeDenied(final Plot plot, final UUID uuid) {
        String world = null;
        if (plot.getArea() != null) {
            world = plot.getArea().toString();
        }
        if (world == null) {
            return;
        }
        PlotId pid = plot.getId();
        Optional<PlotEntity> pe = this.plotRepository.findByWorldAndId(world, pid.getX(), pid.getY());
        pe.ifPresent(entity -> this.deniedRepository.remove(entity.getId(), uuid.toString()));
    }

    @Override
    public void setDenied(final Plot plot, final UUID uuid) {
        String world = null;
        if (plot.getArea() != null) {
            world = plot.getArea().toString();
        }
        if (world == null) {
            return;
        }
        PlotId pid = plot.getId();
        Optional<PlotEntity> pe = this.plotRepository.findByWorldAndId(world, pid.getX(), pid.getY());
        pe.ifPresent(entity -> this.deniedRepository.add(entity.getId(), uuid.toString()));
    }

}
