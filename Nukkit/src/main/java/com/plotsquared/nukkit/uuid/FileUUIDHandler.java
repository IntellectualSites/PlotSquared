package com.plotsquared.nukkit.uuid;

import com.google.common.collect.HashBiMap;
import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.object.RunnableVal;
import com.intellectualcrafters.plot.object.StringWrapper;
import com.intellectualcrafters.plot.util.TaskManager;
import com.intellectualcrafters.plot.util.UUIDHandlerImplementation;
import com.intellectualcrafters.plot.util.expiry.ExpireManager;
import com.intellectualcrafters.plot.uuid.UUIDWrapper;
import java.io.File;
import java.util.HashMap;
import java.util.UUID;

public class FileUUIDHandler extends UUIDHandlerImplementation {

    public FileUUIDHandler(UUIDWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public boolean startCaching(Runnable whenDone) {
        return super.startCaching(whenDone) && cache(whenDone);
    }

    public boolean cache(final Runnable whenDone) {
        final File container = new File("players");
        TaskManager.runTaskAsync(() -> {
            PS.debug(C.PREFIX + "&6Starting player data caching for: " + container);
            HashBiMap<StringWrapper, UUID> toAdd = HashBiMap.create(new HashMap<StringWrapper, UUID>());
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
        });
        return true;
    }

    @Override
    public void fetchUUID(final String name, final RunnableVal<UUID> ifFetch) {
        TaskManager.runTaskAsync(() -> {
            ifFetch.value = FileUUIDHandler.this.uuidWrapper.getUUID(name);
            TaskManager.runTask(ifFetch);
        });
    }
}
