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

import org.json.JSONObject;
import org.json.JSONException;
import mcjty.enigma.web.Data;
// try {} catch (JSONException e) {e.printStackTrace();}
                
import mcjty.enigma.parser.ObjectTools;
import static mcjty.enigma.varia.StringRegister.STRINGS;

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
        JSONObject JSONPVars = new JSONObject();                
        JSONObject JSONStates = new JSONObject();
        JSONObject JSONPStates = new JSONObject();
        JSONObject JSONWebResponse = new JSONObject();

        Progress progress = ProgressHolder.getProgress(server.getEntityWorld());

        for (Map.Entry<Integer, Integer> entry : progress.getStates().entrySet()) {
            String name = STRINGS.get(entry.getKey());
            String value = STRINGS.get(entry.getValue());
            JSONStates.put(name, value);
        }

        for (Map.Entry<Integer, Object> entry : progress.getNamedVariables().entrySet()) {
            String name = STRINGS.get(entry.getKey());
            String value = ObjectTools.asStringSafe(entry.getValue());
            if (value == "") {
                JSONObject jsonvalue = ObjectTools.asJsonSafe(entry.getValue());
                JSONVars.put(name, jsonvalue);    
            } else {
                JSONVars.put(name, value);    
            }
        }

        for (Map.Entry<UUID, PlayerProgress> entry : progress.getPlayerProgress().entrySet()) {
            EntityPlayerMP player = server.getPlayerList().getPlayerByUUID(entry.getKey());
            JSONObject JSONPlayerVars = new JSONObject();
            JSONObject JSONPlayerStates = new JSONObject();
            for (Map.Entry<Integer, Integer> pp : entry.getValue().getStates().entrySet()) {
                String name = STRINGS.get(pp.getKey());
                String value = STRINGS.get(pp.getValue());
                JSONPlayerStates.put(name, value);
            }
            for (Map.Entry<Integer, Object> pp : entry.getValue().getNamedVariables().entrySet()) {
                String name = STRINGS.get(pp.getKey());
                String value = ObjectTools.asStringSafe(pp.getValue());
                JSONPlayerVars.put(name, value);
                if (value == "") {
                    JSONObject jsonvalue = ObjectTools.asJsonSafe(pp.getValue());
                    JSONPlayerVars.put(name, jsonvalue);    
                } else {
                    JSONPlayerVars.put(name, value);    
                }                
            }
            if (player != null) {
                JSONPVars.put(player.getDisplayNameString(),JSONPlayerVars);                
                JSONPStates.put(player.getDisplayNameString(),JSONPlayerStates);
            }
        }

        JSONWebResponse.put("vars", JSONVars);
        JSONWebResponse.put("pvars", JSONPVars);        
        JSONWebResponse.put("states", JSONStates);
        JSONWebResponse.put("pstates", JSONPStates);        
        Data.webResponse = JSONWebResponse.toString(4);
        
        if (server != sender) {
            Enigma.setup.getLogger().info(Data.webResponse);
            ITextComponent component = new TextComponentString(TextFormatting.GREEN + "The e_states command has been executed");
            if (sender instanceof EntityPlayer) {
                ((EntityPlayer) sender).sendStatusMessage(component, false);
            } else {
                sender.sendMessage(component);
            }
        } else {
            Enigma.setup.getLogger().info("The e_states command has been executed");
        }

    }
}