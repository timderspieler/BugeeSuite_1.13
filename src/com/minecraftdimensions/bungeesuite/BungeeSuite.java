package com.minecraftdimensions.bungeesuite;

import com.minecraftdimensions.bungeesuite.commands.BSVersionCommand;
import com.minecraftdimensions.bungeesuite.commands.MOTDCommand;
import com.minecraftdimensions.bungeesuite.commands.ReloadCommand;
import com.minecraftdimensions.bungeesuite.listeners.*;
import com.minecraftdimensions.bungeesuite.managers.*;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.plugin.Plugin;

import java.sql.SQLException;

public class BungeeSuite extends Plugin {
    public static BungeeSuite instance;
    public static ProxyServer proxy;
    private String prefix = "[BungeeSuite] ";

    public void onEnable() {
        instance = this;
        LoggingManager.log( prefix + "Starting BungeeSuite" );
        proxy = ProxyServer.getInstance();
        LoggingManager.log( prefix + "Initialising Managers" );
        initialiseManagers();
        registerListeners();
        registerCommands();
        reloadServersPlugins();
        
        // All you have to do is adding this line in your onEnable method:
        Metrics metrics = new Metrics(this);
        
    }

    private void registerCommands() {
        //        proxy.getPluginManager().registerCommand( this, new WhoIsCommand() );
        proxy.getPluginManager().registerCommand( this, new BSVersionCommand() );
        proxy.getPluginManager().registerCommand( this, new MOTDCommand() );
        proxy.getPluginManager().registerCommand( this, new ReloadCommand() );
    }

    private void initialiseManagers() {
        if ( SQLManager.initialiseConnections() ) {
            DatabaseTableManager.createDefaultTables();
            AnnouncementManager.loadAnnouncements();
            //            if ( MainConfig.UserSocketPort ) {
            //                SocketManager.startServer();
            //            }
            ChatManager.loadChannels();
            PrefixSuffixManager.loadPrefixes();
            PrefixSuffixManager.loadSuffixes();
            TeleportManager.initialise();
            try {
                WarpsManager.loadWarpLocations();
                PortalManager.loadPortals();
                SpawnManager.loadSpawns();
            } catch ( SQLException e ) {
                e.printStackTrace();
            }
            //test
        } else {
            //            setupSQL();
            LoggingManager.log( prefix + "Your BungeeSuite is unable to connect to your SQL database specified in the config" );
        }
    }

    void registerListeners() {
        this.getProxy().registerChannel( "bsuite:chat-in" );//in
        this.getProxy().registerChannel( "bsuite:chat-out" );//out
        this.getProxy().registerChannel( "bsuite:bans-in" );//in
        this.getProxy().registerChannel( "bsuite:bans-out" ); //out
        this.getProxy().registerChannel( "bsuite:tp-in" );//in
        this.getProxy().registerChannel( "bsuite:tp-out" );//out
        this.getProxy().registerChannel( "bsuite:warps-in" );//in
        this.getProxy().registerChannel( "bsuite:warps-out" );//out
        this.getProxy().registerChannel( "bsuite:homes-in" );//in
        this.getProxy().registerChannel( "bsuite:homes-out" );//out
        this.getProxy().registerChannel( "bsuite:portals-in" );//in
        this.getProxy().registerChannel( "bsuite:portals-out" );//out
        this.getProxy().registerChannel( "bsuite:spawns-in" );//in
        this.getProxy().registerChannel( "bsuite:spawns-out" );//out
        proxy.getPluginManager().registerListener( this, new PlayerListener() );
        proxy.getPluginManager().registerListener( this, new ChatListener() );
        proxy.getPluginManager().registerListener( this, new ChatMessageListener() );
        proxy.getPluginManager().registerListener( this, new BansMessageListener() );
        proxy.getPluginManager().registerListener( this, new BansListener() );
        proxy.getPluginManager().registerListener( this, new TeleportsMessageListener() );
        proxy.getPluginManager().registerListener( this, new WarpsMessageListener() );
        proxy.getPluginManager().registerListener( this, new HomesMessageListener() );
        proxy.getPluginManager().registerListener( this, new PortalsMessageListener() );
        proxy.getPluginManager().registerListener( this, new SpawnListener() );
        proxy.getPluginManager().registerListener( this, new SpawnMessageListener() );
    }


    private void reloadServersPlugins() {
        for ( ServerInfo s : ProxyServer.getInstance().getServers().values() ) {
            ChatManager.checkForPlugins( s );
        }
    }

    public void onDisable() {
        SQLManager.closeConnections();
    }
}
