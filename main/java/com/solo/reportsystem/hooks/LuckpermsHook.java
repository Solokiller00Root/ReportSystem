package com.solo.reportsystem.hooks;

import net.luckperms.api.LuckPerms;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

public class LuckpermsHook {
    private LuckPerms luckPerms;

    public LuckpermsHook() {
        RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        if (provider != null) {
            luckPerms = provider.getProvider();
        }
    }

    public LuckPerms getLuckPerms() {
        return luckPerms;
    }
}