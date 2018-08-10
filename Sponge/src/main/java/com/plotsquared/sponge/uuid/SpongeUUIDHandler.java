package com.plotsquared.sponge.uuid;

import com.intellectualcrafters.plot.object.RunnableVal;
import com.intellectualcrafters.plot.object.StringWrapper;
import com.intellectualcrafters.plot.util.TaskManager;
import com.intellectualcrafters.plot.util.UUIDHandlerImplementation;
import com.intellectualcrafters.plot.uuid.UUIDWrapper;
import com.plotsquared.sponge.SpongeMain;
import org.spongepowered.api.profile.GameProfile;

import java.util.UUID;

public class SpongeUUIDHandler extends UUIDHandlerImplementation {

    public SpongeUUIDHandler(UUIDWrapper wrapper) {
        super(wrapper);
    }

    @Override public boolean startCaching(Runnable whenDone) {
        if (!super.startCaching(whenDone)) {
            return false;
        }
        return cache(whenDone);
    }

    public boolean cache(Runnable whenDone) {
        for (GameProfile profile : SpongeMain.THIS.getServer().getGameProfileManager().getCache()
            .getProfiles()) {
            String name = profile.getName().orElse(null);
            if (name != null) {
                add(new StringWrapper(name), profile.getUniqueId());
            }
        }
        return true;
    }

    @Override public void fetchUUID(String name, RunnableVal<UUID> ifFetch) {
        TaskManager.runTaskAsync(new Runnable() {
            @Override public void run() {
                ifFetch.value = SpongeUUIDHandler.this.uuidWrapper.getUUID(name);
                TaskManager.runTask(ifFetch);
            }
        });
    }

}
