/*
 * This file is/was part of Treasury. To read more information about Treasury such as its licensing, see <https://github.com/ArcanePlugins/Treasury>.
 */

package me.lokka30.treasury.plugin.bukkit;

import java.io.File;
import java.util.Optional;
import me.lokka30.treasury.api.common.service.Service;
import me.lokka30.treasury.api.common.service.ServiceRegistry;
import me.lokka30.treasury.api.economy.EconomyProvider;
import me.lokka30.treasury.plugin.bukkit.command.TreasuryCommand;
import me.lokka30.treasury.plugin.bukkit.hooks.HookRegistrar;
import me.lokka30.treasury.plugin.bukkit.listeners.BukkitServiceRegistrationListener;
import me.lokka30.treasury.plugin.bukkit.vendor.BukkitVendor;
import me.lokka30.treasury.plugin.bukkit.vendor.paper.PaperEnhancements;
import me.lokka30.treasury.plugin.core.TreasuryPlugin;
import me.lokka30.treasury.plugin.core.utils.QuickTimer;
import me.lokka30.treasury.plugin.core.utils.UpdateChecker;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * This is the plugin's main class, loaded by Bukkit's plugin manager.
 * It contains direct and indirect links to everything accessed within
 * the plugin.
 *
 * @author lokka30
 * @since v1.0.0
 */
public class TreasuryBukkit extends JavaPlugin {

    private BukkitTreasuryPlugin treasuryPlugin;

    /**
     * Run the start-up procedure for the plugin.
     * This is called by Bukkit's plugin manager.
     *
     * @author lokka30
     * @since v1.0.0
     */
    @Override
    public void onEnable() {
        final QuickTimer startupTimer = new QuickTimer();

        getServer().getPluginManager().registerEvents(new BukkitServiceRegistrationListener(
                getLogger()), this);

        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }
        treasuryPlugin = new BukkitTreasuryPlugin(this);
        TreasuryPlugin.setInstance(treasuryPlugin);
        treasuryPlugin.loadMessages();
        treasuryPlugin.loadSettings();
        TreasuryCommand.register(this);

        getServer().getPluginManager().registerEvents(new HookRegistrar(this), this);

        if (BukkitVendor.isPaper()) {
            PaperEnhancements.enhance(this);
        }

        UpdateChecker.checkForUpdates();

        loadMetrics();

        treasuryPlugin.logStartupMessage(startupTimer, false);
    }

    private void loadMetrics() {
        Metrics metrics = new Metrics(this, 12927);

        Optional<Service<EconomyProvider>> service = ServiceRegistry.INSTANCE.serviceFor(
                EconomyProvider.class);

        EconomyProvider economyProvider;
        String pluginName;

        if (!service.isPresent()) {
            RegisteredServiceProvider<EconomyProvider> serviceProvider = getServer()
                    .getServicesManager()
                    .getRegistration(EconomyProvider.class);

            economyProvider = serviceProvider == null ? null : serviceProvider.getProvider();
            pluginName = serviceProvider == null ? null : serviceProvider.getPlugin().getName();
        } else {
            Service<EconomyProvider> serv = service.get();
            economyProvider = serv.get();
            pluginName = serv.registrarName();
        }

        metrics.addCustomChart(new SimplePie("economy-provider-name",
                () -> economyProvider == null ? "None" : pluginName
        ));

        metrics.addCustomChart(new SimplePie("plugin-update-checking-enabled",
                () -> Boolean.toString(treasuryPlugin
                        .configAdapter()
                        .getSettings()
                        .checkForUpdates())
        ));

        metrics.addCustomChart(new SimplePie("economy-provider-currencies", () -> {
            if (economyProvider == null) {
                return null;
            }

            final int size = economyProvider.getCurrencies().size();

            if (size >= 10) {
                return "10+";
            } else {
                return Integer.toString(size);
            }
        }));
    }

    /**
     * Run the shut-down procedure for the plugin.
     * This is called by Bukkit's plugin manager.
     *
     * @author lokka30
     * @since v1.0.0
     */
    @Override
    public void onDisable() {
        treasuryPlugin.shutdown(false);
    }

    /**
     * Returns the file of the plugin.
     *
     * @return plugin file
     */
    public File getPluginFile() {
        return super.getFile();
    }

}
