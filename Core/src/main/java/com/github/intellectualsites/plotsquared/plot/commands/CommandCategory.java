package com.github.intellectualsites.plotsquared.plot.commands;

import com.github.intellectualsites.plotsquared.plot.util.block.BlockUtil;

import com.github.intellectualsites.plotsquared.plot.config.Captions;
import lombok.RequiredArgsConstructor;

/**
 * CommandCategory.
 */
@RequiredArgsConstructor public enum CommandCategory {
    /**
     * Claiming CommandConfig.
     * Such as: /plot claim
     */
    CLAIMING(Captions.COMMAND_CATEGORY_CLAIMING),
    /**
     * Teleportation CommandConfig.
     * Such as: /plot visit
     */
    TELEPORT(Captions.COMMAND_CATEGORY_TELEPORT),
    /**
     * Protection.
     */
    SETTINGS(Captions.COMMAND_CATEGORY_SETTINGS),
    /**
     * Chat.
     */
    CHAT(Captions.COMMAND_CATEGORY_CHAT),
    /**
     * Web.
     */
    SCHEMATIC(Captions.COMMAND_CATEGORY_SCHEMATIC),
    /**
     * Cosmetic.
     */
    APPEARANCE(Captions.COMMAND_CATEGORY_APPEARANCE),
    /**
     * Information CommandConfig.
     * Such as: /plot info
     */
    INFO(Captions.COMMAND_CATEGORY_INFO),
    /**
     * Debug CommandConfig.
     * Such as: /plot debug
     */
    DEBUG(Captions.COMMAND_CATEGORY_DEBUG),
    /**
     * Administration commands.
     */
    ADMINISTRATION(Captions.COMMAND_CATEGORY_ADMINISTRATION);
    /**
     * The category name (Readable).
     */
    private final Captions caption;

    @Override public String toString() {
        return this.caption.getTranslated();
    }
}
