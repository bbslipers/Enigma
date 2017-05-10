package mcjty.enigma.fxanim.animations;

import io.netty.buffer.ByteBuf;
import mcjty.enigma.Enigma;
import mcjty.enigma.blocks.MimicTE;
import mcjty.enigma.fxanim.FxAnimation;
import mcjty.enigma.varia.NetworkTools;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class MoveBlockAnimation implements FxAnimation {

    public static final String FXANIM_MOVEBLOCK = "moveblock";

    private final BlockPos mimicPos;
    private final Vec3d start;
    private final Vec3d end;
    private final int totalTicks;
    private int currentTick = 0;

    public MoveBlockAnimation(BlockPos mimicPos, Vec3d start, Vec3d end, int totalTicks) {
        this.mimicPos = mimicPos;
        this.start = start;
        this.end = end;
        this.totalTicks = totalTicks;
    }

    public MoveBlockAnimation(ByteBuf buf) {
        mimicPos = NetworkTools.readPos(buf);
        start = new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
        end = new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
        totalTicks = buf.readInt();
    }

    @Override
    public String getID() {
        return FXANIM_MOVEBLOCK;
    }

    @Override
    public void writeToBuf(ByteBuf buf) {
        NetworkTools.writePos(buf, mimicPos);
        buf.writeDouble(start.xCoord);
        buf.writeDouble(start.yCoord);
        buf.writeDouble(start.zCoord);
        buf.writeDouble(end.xCoord);
        buf.writeDouble(end.yCoord);
        buf.writeDouble(end.zCoord);
        buf.writeInt(totalTicks);
    }

    @Override
    public boolean isDead() {
        return currentTick >= totalTicks;
    }

    @Override
    public void tick() {
        World world = Enigma.proxy.getClientWorld();
        TileEntity te = world.getTileEntity(mimicPos);
        if (te instanceof MimicTE) {
            MimicTE mimicTE = (MimicTE)te;
            double factor = ((double) currentTick) / totalTicks;
            double x = (end.xCoord - start.xCoord) * factor;
            double y = (end.yCoord - start.yCoord) * factor;
            double z = (end.zCoord - start.zCoord) * factor;
            mimicTE.setOffset(x, y, z);
        }
        currentTick++;
    }
}