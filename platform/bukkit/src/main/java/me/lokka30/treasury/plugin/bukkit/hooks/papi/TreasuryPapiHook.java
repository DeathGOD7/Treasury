/*
 * This file is/was part of Treasury. To read more information about Treasury such as its licensing, see <https://github.com/ArcanePlugins/Treasury>.
 */

package me.lokka30.treasury.plugin.bukkit.hooks.papi;

import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface TreasuryPapiHook {

    String getPrefix();

    boolean setup();

    void clear();

    @Nullable String onRequest(@Nullable OfflinePlayer player, @NotNull String param);

}
