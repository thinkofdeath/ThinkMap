package uk.co.thinkofdeath.thinkmap.bukkit.world;

import gnu.trove.map.hash.TLongByteHashMap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import lombok.Cleanup;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.World;
import uk.co.thinkofdeath.thinkmap.bukkit.ThinkMapPlugin;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.*;
import java.util.zip.GZIPOutputStream;

public class ChunkManager implements Runnable {

    private final ThinkMapPlugin plugin;
    private final World world;
    private final TLongByteHashMap activeChunks = new TLongByteHashMap();
    private final LinkedTransferQueue<FutureTask> jobQueue = new LinkedTransferQueue<FutureTask>();

    public ChunkManager(ThinkMapPlugin plugin, World world) {
        this.plugin = plugin;
        this.world = world;
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, this);
    }

    private static long chunkKey(int x, int z) {
        return ((long) x << 32) | z & 0xFFFFFFFL;
    }

    public void activateChunk(Chunk chunk) {
        synchronized (activeChunks) {
            activeChunks.put(chunkKey(chunk.getX(), chunk.getZ()), (byte) 1);
        }
    }

    public void deactivateChunk(Chunk chunk) {
        synchronized (activeChunks) {
            activeChunks.remove(chunkKey(chunk.getX(), chunk.getZ()));
        }
        final ChunkSnapshot snapshot = chunk.getChunkSnapshot();
        jobQueue.add(new FutureTask<Void>(new Runnable() {

            @Override
            public void run() {
                try {
                    ByteBuf data = Unpooled.buffer();
                    gzipChunk(snapshot, data);
                    File worldFolder = new File(plugin.getDataFolder(), world.getName());
                    worldFolder.mkdirs();
                    @Cleanup RandomAccessFile region = new RandomAccessFile(new File(worldFolder,
                            String.format("region_%d-%d.dat", snapshot.getX() >> 5, snapshot.getZ() >> 5)
                    ), "rw");
                    if (region.length() < 4096 * 3) {
                        // Init header
                        region.seek(4096 * 3);
                        region.writeByte(0);
                    }
                    int id = ((snapshot.getX() & 0x1F) | ((snapshot.getZ() & 0x1F) << 5));
                    region.seek(8 * id);
                    int offset = region.readInt();
                    int size = region.readInt();
                    if (offset != 0) {
                        if (data.readableBytes() < ((size / 4096) + 1) * 4096) {
                            size = data.readableBytes();
                            region.seek(8 * id);
                            region.writeInt(offset);
                            region.writeInt(size);
                            region.seek(offset * 4096);
                            region.write(data.array());
                            return;
                        }
                    }
                    boolean[] usedSpace = new boolean[(int) ((region.length() / 4096) + 1)];
                    usedSpace[0] = usedSpace[1] = usedSpace[2] = true;
                    for (int i = 0; i < 32 * 32; i++) {
                        if (i == id) continue;
                        region.seek(8 * i);
                        int oo = region.readInt();
                        int os = region.readInt();
                        for (int j = oo; j < oo + ((os / 4096) + 1); j++) {
                            usedSpace[j] = true;
                        }
                    }
                    offset = usedSpace.length;
                    size = data.readableBytes();
                    search:
                    for (int i = 2; i < usedSpace.length; i++) {
                        if (!usedSpace[i]) {
                            for (int j = i + 1; j < i + ((size / 4096) + 1); j++) {
                                if (j >= usedSpace.length || usedSpace[j]) {
                                    i += ((size / 4096) + 1);
                                    continue search;
                                }
                            }
                            offset = i;
                            break;
                        }
                    }
                    region.seek(offset * 4096);
                    region.write(data.array());
                    region.seek(8 * id);
                    region.writeInt(offset);
                    region.writeInt(size);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, null));
    }

    private Future<ByteBuf> getChunkData(final int x, final int z) {
        FutureTask<ByteBuf> task = new FutureTask<ByteBuf>(new Callable<ByteBuf>() {
            @Override
            public ByteBuf call() throws Exception {
                try {
                    File worldFolder = new File(plugin.getDataFolder(), world.getName());
                    @Cleanup RandomAccessFile region = new RandomAccessFile(new File(worldFolder,
                            String.format("region_%d-%d.dat", x >> 5, z >> 5)
                    ), "r");
                    if (region.length() < 4096 * 3) return null;
                    int id = ((x & 0x1F) | ((z & 0x1F) << 5));
                    region.seek(8 * id);
                    int offset = region.readInt();
                    int size = region.readInt();
                    if (offset == 0) {
                        return null;
                    }
                    region.seek(offset * 4096);
                    byte[] data = new byte[size];
                    region.read(data);
                    return Unpooled.wrappedBuffer(data);
                } catch (Exception e) {
                    return null;
                }
            }
        });
        jobQueue.add(task);
        return task;
    }

    public void cleanup() {

    }

    public boolean getChunkBytes(final int x, final int z, ByteBuf out) {
        ChunkSnapshot chunk = null;
        synchronized (activeChunks) {
            if (activeChunks.containsKey(chunkKey(x, z))) {
                try {
                    chunk = plugin.getServer().getScheduler().callSyncMethod(plugin, new Callable<ChunkSnapshot>() {
                        @Override
                        public ChunkSnapshot call() throws Exception {
                            return world.getChunkAt(x, z).getChunkSnapshot(false, true, false);
                        }
                    }).get();
                } catch (InterruptedException e) {
                    Thread.interrupted();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
        }
        if (chunk == null) {
            ByteBuf data = null;
            try {
                data = getChunkData(x, z).get();
            } catch (InterruptedException e) {
                Thread.interrupted();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            if (data == null) {
                return false;
            }
            out.writeBytes(data);
            return true;
        }
        gzipChunk(chunk, out);
        return true;
    }

    private void gzipChunk(ChunkSnapshot chunk, ByteBuf out) {
        int mask = 0;
        int count = 0;

        for (int i = 0; i < 16; i++) {
            if (!chunk.isSectionEmpty(i)) {
                mask |= 1 << i;
                count++;
            }
        }

        ByteBuf data = Unpooled.buffer(16 * 16 * 16 * 5 * count);
        data.writeInt(chunk.getX());
        data.writeInt(chunk.getZ());
        data.writeShort(mask);
        for (int i = 0; i < 16; i++) {
            if (!chunk.isSectionEmpty(i)) {
                for (int oy = 0; oy < 16; oy++) {
                    for (int oz = 0; oz < 16; oz++) {
                        for (int ox = 0; ox < 16; ox++) {
                            data.writeShort(chunk.getBlockTypeId(ox, oy + i * 16, oz));
                            data.writeByte(chunk.getBlockData(ox, oy + i * 16, oz));
                            data.writeByte(chunk.getBlockEmittedLight(ox, oy + i * 16, oz));
                            data.writeByte(chunk.getBlockSkyLight(ox, oy + i * 16, oz));
                        }
                    }
                }
            }
        }

        try {
            GZIPOutputStream gzip = new GZIPOutputStream(new ByteBufOutputStream(out));
            gzip.write(data.array());
            gzip.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (true) {
            if (Thread.interrupted()) {
                return;
            }
            try {
                FutureTask job = jobQueue.take();
                job.run();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
