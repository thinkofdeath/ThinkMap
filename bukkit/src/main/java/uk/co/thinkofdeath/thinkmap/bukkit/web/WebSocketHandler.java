package uk.co.thinkofdeath.thinkmap.bukkit.web;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.*;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import uk.co.thinkofdeath.thinkmap.bukkit.ThinkMapPlugin;

import java.util.logging.Logger;

@RequiredArgsConstructor
public class WebSocketHandler extends SimpleChannelInboundHandler<BinaryWebSocketFrame> {
    private final static Logger logger = Logger.getLogger(WebSocketHandler.class.getName());

    private final ThinkMapPlugin plugin;

    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, BinaryWebSocketFrame msg) throws Exception {
        ByteBuf data = msg.content();
        switch (data.readUnsignedByte()) {
            case 0: // Start
                plugin.getServer().getScheduler().runTask(plugin, new Runnable() {
                    @Override
                    public void run() {
                        final Location spawn = plugin.getTargetWorld().getSpawnLocation();

                        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
                            @Override
                            public void run() {
                                ctx.writeAndFlush(new BinaryWebSocketFrame(
                                        Packets.writeSpawnPosition(spawn.getBlockX(), spawn.getBlockY(), spawn.getBlockZ())
                                ));

                                ctx.writeAndFlush(new BinaryWebSocketFrame(
                                        Packets.writeTimeUpdate((int) plugin.getTargetWorld().getTime())
                                ));
                            }
                        });
                    }
                });

                break;
            case 1: // Get new Chunk over Websocket
                int x = data.readInt();
                int z = data.readInt();

                ByteBuf out = Unpooled.buffer();
                plugin.getChunkManager(plugin.getTargetWorld()).getChunkBytes(x, z, out);

                ctx.writeAndFlush(new BinaryWebSocketFrame(
                        Packets.writeChunkBytes(x, z, out.array())
                ));

                break;
        }
    }
}
