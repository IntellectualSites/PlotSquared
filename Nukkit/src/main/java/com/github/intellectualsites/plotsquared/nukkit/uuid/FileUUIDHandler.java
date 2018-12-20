package com.github.intellectualsites.plotsquared.nukkit.uuid;

import com.github.intellectualsites.plotsquared.plot.PlotSquared;
import com.github.intellectualsites.plotsquared.plot.config.C;
import com.github.intellectualsites.plotsquared.plot.object.RunnableVal;
import com.github.intellectualsites.plotsquared.plot.object.StringWrapper;
import com.github.intellectualsites.plotsquared.plot.util.TaskManager;
import com.github.intellectualsites.plotsquared.plot.util.UUIDHandlerImplementation;
import com.github.intellectualsites.plotsquared.plot.util.expiry.ExpireManager;
import com.github.intellectualsites.plotsquared.plot.uuid.UUIDWrapper;
import com.google.common.collect.HashBiMap;

import java.io.File;
import java.util.HashMap;
import java.util.UUID;

public class FileUUIDHandler extends UUIDHandlerImplementation {

    public FileUUIDHandler(UUIDWrapper wrapper) {
        super(wrapper);
    }

    @Override public boolean startCaching(Runnable whenDone) {
        return super.startCaching(whenDone) && cache(whenDone);
    }

    public boolean cache(final Runnable whenDone) {
        final File container = new File("players");
        TaskManager.runTaskAsync(new Runnable() {
            @Override public void run() {
                PlotSquared.debug(C.PREFIX + "&6Starting player data caching for: " + container);
                HashBiMap<StringWrapper, UUID> toAdd =
                    HashBiMap.create(new HashMap<StringWrapper, UUID>());
                for (File file : container.listFiles(new DatFileFilter())) {
                    String fileName = file.getName();
                    String name = fileName.substring(0, fileName.length() - 4);
                    UUID uuid = uuidWrapper.getUUID(name);
                    toAdd.put(new StringWrapper(name), uuid);
                    long last = file.lastModified();
                    if (ExpireManager.IMP != null) {
                        ExpireManager.IMP.storeDate(uuid, last);
                    }
                }
                add(toAdd);
                if (whenDone != null) {
                    whenDone.run();
                }
            }
        });
        return true;
    }

    @Override public void fetchUUID(final String name, final RunnableVal<UUID> ifFetch) {
        TaskManager.runTaskAsync(new Runnable() {
            @Override public void run() {
                ifFetch.value = FileUUIDHandler.this.uuidWrapper.getUUID(name);
                TaskManager.runTask(ifFetch);
            }
        });
    }
}
