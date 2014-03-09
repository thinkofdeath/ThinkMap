package uk.co.thinkofdeath.thinkmap.bukkit.web;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import uk.co.thinkofdeath.thinkmap.bukkit.ThinkMapPlugin;

public class WebHandler extends Thread {

    private ThinkMapPlugin plugin;

    public WebHandler(ThinkMapPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup(plugin.getConfig().getInt("webserver.netty-threads"));

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup).
                    channel(NioServerSocketChannel.class).
                    childHandler(new ServerHandler(plugin));

            Channel channel = bootstrap.bind(
                    plugin.getConfig().getString("webserver.bind-address"),
                    plugin.getConfig().getInt("webserver.port")
            ).sync().channel();

            channel.closeFuture().sync();
        } catch (InterruptedException e) {
            interrupt();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
