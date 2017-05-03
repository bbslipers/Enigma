package mcjty.enigma.commands;

import mcjty.enigma.snapshot.SnapshotTools;
import mcjty.lib.compat.CompatCommandBase;
import mcjty.lib.tools.ChatTools;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

public class CmdSnapshot extends CompatCommandBase {
    @Override
    public String getName() {
        return "e_snapshot";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "e_snapshot";
    }

    public static byte[] temporaryTest;

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        ChatTools.addChatMessage(sender, new TextComponentString(TextFormatting.GREEN + "Making a snapshot!"));
        World world = sender.getEntityWorld();
        BlockPos pos = sender.getPosition();
        Chunk curchunk = world.getChunkFromBlockCoords(pos);
        byte[] output = SnapshotTools.makeChunkSnapshot(world, curchunk);
        System.out.println("bytes = " + output.length);
        temporaryTest = output;
    }
}
