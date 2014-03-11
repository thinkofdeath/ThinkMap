package uk.co.thinkofdeath.thinkmap.bukkit.web;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class Packets {
    public static ByteBuf writeSpawnPosition(int x, int y, int z) {
        ByteBuf buf = Unpooled.buffer(1 + 4 + 1 + 4);
        buf.writeByte(1);
        buf.writeInt(x);
        buf.writeByte(y);
        buf.writeInt(z);
        return buf;
    }

    public static ByteBuf writeTimeUpdate(int time) {
        ByteBuf buf = Unpooled.buffer(5);
        buf.writeByte(0);
        buf.writeInt(time);
        return buf;
    }

    public static ByteBuf writeChunkBytes(int x, int z, byte[] bytes) {
        ByteBuf buf = Unpooled.buffer(1 + 4 + 4 + bytes.length);
        buf.writeByte(2);
        buf.writeInt(x);
        buf.writeInt(z);
        buf.writeBytes(bytes);
        return buf;
    }
}
