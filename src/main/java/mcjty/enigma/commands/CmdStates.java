package mcjty.enigma.commands;

import mcjty.enigma.Enigma;
import mcjty.enigma.progress.PlayerProgress;
import mcjty.enigma.progress.Progress;
import mcjty.enigma.progress.ProgressHolder;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

import java.util.Map;
import java.util.UUID;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static mcjty.enigma.varia.StringRegister.STRINGS;

import org.json.JSONObject;
import org.json.JSONException;

import mcjty.enigma.web.Data;

public class CmdStates extends CommandBase {
    @Override
    public String getName() {
        return "e_states";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "e_states";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        JSONObject JSONVars = new JSONObject();        
        JSONObject JSONStates = new JSONObject();
        JSONObject JSONPStates = new JSONObject();
        JSONObject JSONWebResponse = new JSONObject();

        Enigma.setup.getLogger().info("Current status:");
        Progress progress = ProgressHolder.getProgress(server.getEntityWorld());

        for (Map.Entry<Integer, Integer> entry : progress.getStates().entrySet()) {
            String name = STRINGS.get(entry.getKey());
            String value = STRINGS.get(entry.getValue());
            Enigma.setup.getLogger().info("State: " + name + " = " + value);
            JSONStates.put(name, value);
        }

        for (Map.Entry<UUID, PlayerProgress> entry : progress.getPlayerProgress().entrySet()) {
            EntityPlayerMP player = server.getPlayerList().getPlayerByUUID(entry.getKey());
            Enigma.setup.getLogger().info("Player: " + entry.getKey() + (player == null ? "" : (" (" + player.getDisplayNameString() + ")")));
            JSONObject JSONPlayerStates = new JSONObject();
            for (Map.Entry<Integer, Integer> pp : entry.getValue().getStates().entrySet()) {
                String name = STRINGS.get(pp.getKey());
                String value = STRINGS.get(pp.getValue());
                Enigma.setup.getLogger().info("    State: " + name + " = " + value);
                JSONPlayerStates.put(name, value);
            }
            if (player != null) {
                JSONPStates.put(player.getDisplayNameString(),JSONPlayerStates);
            }
        }

        for (Map.Entry<Integer, Object> entry : progress.getNamedVariables().entrySet()) {
            String name = STRINGS.get(entry.getKey());
            JSONObject value = new JSONObject(entry.getValue());

            try {
                byte[] bytes = value.get("bytes");
                if (bytes != null) {
                    String stringFromBytes = new String(bytes, StandardCharsets.UTF_8);
                    value.put("bytes",stringFromBytes);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            //String(value.get("bytes"), "UTF-8")
            JSONVars.put(name, value);
        }

        JSONWebResponse.put("vars", JSONVars);
        JSONWebResponse.put("states", JSONStates);
        JSONWebResponse.put("pstates", JSONPStates);        
        Data.webResponse = JSONWebResponse.toString();

        ITextComponent component = new TextComponentString(TextFormatting.GREEN + "The e_states command has been executed.");
        if (sender instanceof EntityPlayer) {
            ((EntityPlayer) sender).sendStatusMessage(component, false);
        } else {
            sender.sendMessage(component);
        }
    }
}
