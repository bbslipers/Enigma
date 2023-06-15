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

import mcjty.enigma.varia.BlockPosDim;
import mcjty.enigma.parser.ObjectTools;
import net.minecraft.util.math.BlockPos;
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
            String value = ObjectTools.asStringSafe(entry.getValue());
            JSONVars.put(name, value);

            if (value == "") {
                JSONObject JSONValue = new JSONObject(entry.getValue());
                try {
                    if (JSONValue.get("world") != null & JSONValue.get("pos") != null & JSONValue.get("dimension") != null) {
                        JSONObject JSONPos = new JSONObject();
                        BlockPosDim BPDPos = (BlockPosDim)entry.getValue();
                        
                        BlockPos BPos = BPDPos.getPos();
                        JSONPos.put("x",ObjectTools.asStringSafe(BPos.getX()));
                        JSONPos.put("y",ObjectTools.asStringSafe(BPos.getY()));
                        JSONPos.put("z",ObjectTools.asStringSafe(BPos.getZ()));
                        JSONValue.put("pos", JSONPos);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                JSONVars.put(name, JSONValue);

            }
            else {
                JSONVars.put(name, value);
            }
        }

        JSONWebResponse.put("vars", JSONVars);
        JSONWebResponse.put("states", JSONStates);
        JSONWebResponse.put("pstates", JSONPStates);        
        Data.webResponse = JSONWebResponse.toString(4);

        ITextComponent component = new TextComponentString(TextFormatting.GREEN + "The e_states command has been executed.");
        if (sender instanceof EntityPlayer) {
            ((EntityPlayer) sender).sendStatusMessage(component, false);
        } else {
            sender.sendMessage(component);
        }
    }
}
