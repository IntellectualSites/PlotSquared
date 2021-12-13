/*
 *       _____  _       _    _____                                _
 *      |  __ \| |     | |  / ____|                              | |
 *      | |__) | | ___ | |_| (___   __ _ _   _  __ _ _ __ ___  __| |
 *      |  ___/| |/ _ \| __|\___ \ / _` | | | |/ _` | '__/ _ \/ _` |
 *      | |    | | (_) | |_ ____) | (_| | |_| | (_| | | |  __/ (_| |
 *      |_|    |_|\___/ \__|_____/ \__, |\__,_|\__,_|_|  \___|\__,_|
 *                                    | |
 *                                    |_|
 *            PlotSquared plot management system for Minecraft
 *                  Copyright (C) 2021 IntellectualSites
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
/**
 * Future versions of MiniMessage, starting of 4.10.0, have a different naming convention. If PlotSquared updates
 * MiniMessage, certain methods are going to be renamed to keep up consistency with the new naming convention.
 * <br>
 * The existing methods have not been deprecated yet, since we don't know when we will update MiniMessage, but we know
 * it likely will be a major update, because MiniMessage 4.10.0 did not retain backwards compatibility.
 * <br>
 * <table border="1">
 *     <caption>MiniMessage naming convention relevant for PlotSquared API</caption>
 *   <tr>
 *     <td>Current method</td>
 *     <td>Future change</td>
 *   </tr>
 *   <tr>
 *     <td>{@link net.kyori.adventure.text.minimessage.Template}</td> <td>{@code Placeholder}</td>
 *   </tr>
 *   <tr>
 *     <td>{@link net.kyori.adventure.text.minimessage.Template#template(java.lang.String, java.lang.String)}</td> <td>{@code Placeholder#component(String, ComponentLike)}</td>
 *   </tr>
 *   <tr>
 *     <td>{@link net.kyori.adventure.text.minimessage.template.TemplateResolver}</td> <td>{@code PlaceholderResolver}</td>
 *   </tr>
 *   <tr>
 *     <td>{@link net.kyori.adventure.text.minimessage.template.TemplateResolver#templates(net.kyori.adventure.text.minimessage.Template...)}</td> <td>{@code PlaceholderResolver#placeholders}</td>
 *   </tr>
 *   <tr>
 *     <td>{@link net.kyori.adventure.text.minimessage.MiniMessage#parse(java.lang.String)}</td> <td>{@code MiniMessage#deserialize()}</td>
 *   </tr>
 *   <tr>
 *     <td>{@link net.kyori.adventure.text.minimessage.MiniMessage#get()}</td> <td>{@code MiniMessage#miniMessage()}</td>
 *   </tr>
 * </table>
 * <br>
 * This table has been added in 6.2.0
 *
 */
package com.plotsquared.core.configuration.caption;
